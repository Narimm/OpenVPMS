/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.smartflow.event.impl;

import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.security.RunAs;
import org.openvpms.component.system.common.event.Listener;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.client.ReferenceDataService;
import org.openvpms.smartflow.event.EventDispatcher;
import org.openvpms.smartflow.event.SmartFlowSheetEventService;
import org.openvpms.smartflow.model.ServiceBusConfig;
import org.openvpms.smartflow.model.event.Event;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of the {@link SmartFlowSheetEventService}.
 *
 * @author Tim Anderson
 */
public class SmartFlowSheetEventServiceImpl implements InitializingBean, DisposableBean, SmartFlowSheetEventService {

    /**
     * The practice service.
     */
    private final PracticeService practiceService;

    /**
     * The SFS service factory.
     */
    private final FlowSheetServiceFactory factory;

    /**
     * The event dispatcher.
     */
    private final EventDispatcher dispatcher;

    /**
     * Used to schedule dispatching.
     */
    private final ExecutorService executor;

    /**
     * Listener for practice update events.
     */
    private final Listener<PracticeService.Update> listener;

    /**
     * The default interval between polls, in seconds.
     */
    private int interval = 30;

    /**
     * The user assigned to the dispatch thread.
     */
    private User user;

    /**
     * The SFS Azure Service Bus configuration.
     */
    private ServiceBusConfig config;

    /**
     * Used to indicate that the dispatcher has been shut down.
     */
    private volatile boolean shutdown = false;

    /**
     * Used to restricted the number of tasks that can be scheduled via the executor.
     */
    private final Semaphore scheduled = new Semaphore(1);

    /**
     * Used to wait for scheduling the next poll.
     */
    private final Semaphore waiter = new Semaphore(0);

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(SmartFlowSheetEventServiceImpl.class);

    /**
     * Constructs a {@link SmartFlowSheetEventServiceImpl}.
     *
     * @param practiceService the practice service
     * @param factory         the factory for SFS services
     */
    public SmartFlowSheetEventServiceImpl(PracticeService practiceService, FlowSheetServiceFactory factory) {
        this(practiceService, factory, new DefaultEventDispatcher());
    }

    /**
     * Constructs a {@link SmartFlowSheetEventServiceImpl}.
     *
     * @param practiceService the practice service
     * @param factory         the factory for SFS services
     * @param dispatcher      the event dispatcher
     */
    protected SmartFlowSheetEventServiceImpl(PracticeService practiceService, FlowSheetServiceFactory factory,
                                             EventDispatcher dispatcher) {
        this.practiceService = practiceService;
        this.factory = factory;
        this.dispatcher = dispatcher;
        listener = new Listener<PracticeService.Update>() {
            @Override
            public void onEvent(PracticeService.Update event) {
                practiceChanged();
            }
        };
        practiceService.addListener(listener);
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Sets the interval between each poll.
     *
     * @param interval the interval, in seconds
     */
    @Override
    public void setPollInterval(int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Argument 'interval' must be > 0");
        }
        this.interval = interval;
        schedule();
    }

    /**
     * Triggers a poll for events.
     */
    @Override
    public void poll() {
        schedule();
    }

    /**
     * Monitors for a single event with the specified id.
     *
     * @param id       the event identifier
     * @param listener the listener
     */
    @Override
    public void addListener(String id, Listener<Event> listener) {
        dispatcher.addListener(id, listener);
        poll();
    }

    /**
     * Removes a listener for an event identifier.
     *
     * @param id the event identifier
     */
    @Override
    public void removeListener(String id) {
        dispatcher.removeListener(id);
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        poll();
    }

    /**
     * Invoked by a BeanFactory on destruction of a singleton.
     *
     * @throws Exception in case of shutdown errors
     */
    @Override
    public void destroy() throws Exception {
        practiceService.removeListener(listener);
        shutdown = true;
        executor.shutdown();
        waiter.release(); // wake from sleep
        try {
            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.error("Pool did not terminate");
                }
            }
        } catch (InterruptedException exception) {
            // (Re-)Cancel if current thread also interrupted
            executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Schedules a dispatch.
     */
    protected void schedule() {
        waiter.release(); // wakes up dispatch() if it is waiting

        final User user = getServiceUser();
        if (shutdown) {
            log.debug("SmartFlowSheetEventServiceImpl shutting down. Schedule request ignored");
        } else if (user == null) {
            log.debug("No service user. Schedule request ignored");
        } else if (scheduled.tryAcquire()) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    scheduled.release(); // need to release here to enable dispatch() to reschedule
                    try {
                        RunAs.run(user, new Runnable() {
                            @Override
                            public void run() {
                                dispatch();
                            }
                        });
                    } catch (Throwable exception) {
                        log.error(exception.getMessage(), exception);
                    }
                }
            });
        } else {
            log.debug("SmartFlowSheetEventServiceImpl already scheduled");
        }
    }

    /**
     * Creates a queue.
     *
     * @param connectionString the connection string
     * @param queueName        the queue name
     * @return a new queue
     */
    protected Queue createQueue(String connectionString, String queueName) {
        return new ServiceBusQueue(connectionString, queueName);
    }

    /**
     * Dispatches all messages on the queue.
     */
    protected void dispatch() {
        ServiceBusConfig config = getConfig();
        if (config == null) {
            log.info("Smart Flow Sheet is not configured");
        } else {
            Queue queue = createQueue(config.getConnectionString(), config.getQueueName());
            boolean done = false;
            do {
                try {
                    BrokeredMessage message = queue.next();
                    if (message != null) {
                        dispatcher.dispatch(message);
                        queue.remove(message);
                    } else {
                        done = true;
                    }
                } catch (Throwable exception) {
                    log.error(exception, exception);
                    done = true;
                }
            } while (!done);
            if (!shutdown) {
                if (interval > 0) {
                    log.debug("dispatch() waiting for " + interval + "s");
                    // wait until the minimum wait time has expired, or a poll() occurs
                    try {
                        waiter.drainPermits();
                        waiter.tryAcquire(interval, TimeUnit.SECONDS);
                    } catch (InterruptedException ignore) {
                        // do nothing
                    }
                }
                schedule();
            }
            log.debug("dispatch() - end");
        }
    }

    /**
     * Returns the user to set the security context in the dispatch thread.
     *
     * @return the user, or {@code null} if none has been configured
     */
    private User getServiceUser() {
        if (user == null) {
            synchronized (this) {
                user = practiceService.getServiceUser();
                if (user == null) {
                    log.error("Missing party.organisationPractice serviceUser. Messages cannot be sent until "
                              + "this is configured");
                }
            }
        }
        return user;
    }

    /**
     * Returns the Azure Service Bus configuration.
     *
     * @return the configuration, or {@code null} if it is not configured
     */
    private ServiceBusConfig getConfig() {
        if (config == null) {
            synchronized (this) {
                Party practice = practiceService.getPractice();
                if (practice != null && factory.supportsSmartFlowSheet(practice)) {
                    try {
                        ReferenceDataService referenceData = factory.getReferenceDataService(practice);
                        config = referenceData.getServiceBusConfig();
                    } catch (Exception exception) {
                        log.error("Failed to retrieve SFS Azure Service Bus configuration", exception);
                    }
                }
            }
        }
        return config;
    }

    /**
     * Invoked when the practice changes.
     * <p/>
     * This clears the existing configuration, and reschedules dispatching.
     */
    private void practiceChanged() {
        synchronized (this) {
            user = null;
            config = null;
        }
        schedule();
    }
}
