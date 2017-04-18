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
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.security.RunAs;
import org.openvpms.smartflow.event.EventDispatcher;
import org.openvpms.smartflow.model.ServiceBusConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Reads and dispatches messages on a {@link Queue} using a {@link EventDispatcher}.
 *
 * @author Tim Anderson
 */
public class QueueReader {

    /**
     * The Azure Service Bus configuration.
     */
    private final ServiceBusConfig config;

    /**
     * The event dispatcher.
     */
    private final EventDispatcher dispatcher;

    /**
     * The user to associate with the {@link EventDispatcher} thread.
     */
    private final User user;

    /**
     * The executor service.
     */
    private final ExecutorService executor;

    /**
     * Determines if {@link #destroy()} has been invoked.
     */
    private volatile boolean shutdown;

    /**
     * The default interval between polls, in seconds.
     */
    private int interval = 30;

    /**
     * Used to restricted the number of tasks that can be scheduled via the executor.
     */
    private final Semaphore running = new Semaphore(1);

    /**
     * Used to wait for scheduling the next poll.
     */
    private final Semaphore pause = new Semaphore(0);

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(QueueReader.class);

    /**
     * Constructs a {@link QueueReader}.
     *
     * @param config     the Azure Service Bus configuration
     * @param dispatcher the event dispatcher
     * @param user       the user to process events as
     * @param executor   the thread pool
     */
    public QueueReader(ServiceBusConfig config, EventDispatcher dispatcher, User user,
                       ExecutorService executor) {
        this.config = config;
        this.dispatcher = dispatcher;
        this.user = user;
        this.executor = executor;
    }

    /**
     * Sets the interval between each poll.
     *
     * @param interval the interval, in seconds
     */
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
    public void poll() {
        schedule();
    }

    /**
     * Destroys this reader.
     */
    public void destroy() {
        shutdown = true;
        pause.release(); // wakes up pause() if it is waiting
        try {
            running.tryAcquire(30, TimeUnit.SECONDS);
        } catch (InterruptedException ignore) {
            // do nothing
        }
    }

    /**
     * Schedules a dispatch.
     */
    protected void schedule() {
        pause.release(); // wakes up pause() if it is waiting

        if (isShutdown()) {
            log.debug("QueueReader shut down. Schedule request ignored");
        } else if (running.tryAcquire()) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        RunAs.run(user, new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    dispatch();
                                    if (!isShutdown()) {
                                        pause();
                                    }
                                } finally {
                                    running.release();
                                }
                                if (!isShutdown()) {
                                    schedule();
                                }
                            }
                        });
                    } catch (Throwable exception) {
                        log.error(exception.getMessage(), exception);
                    }
                }
            });
        } else {
            log.debug("QueueReader already scheduled");
        }
    }

    /**
     * Dispatches all messages on the queue.
     */
    protected void dispatch() {
        Queue queue = createQueue(config.getConnectionString(), config.getQueueName());
        while (!isShutdown()) {
            try {
                BrokeredMessage message = queue.next();
                if (message != null) {
                    dispatcher.dispatch(message);
                    queue.remove(message);
                } else {
                    break;
                }
            } catch (Throwable exception) {
                log.error(exception, exception);
                break;
            }
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
     * Pauses between polls.
     */
    private void pause() {
        if (interval > 0) {
            log.debug("QueueReader waiting for " + interval + "s");
            // wait until the interval has expired, or a poll() occurs
            pause.drainPermits();
            if (!isShutdown()) {
                try {
                    pause.tryAcquire(interval, TimeUnit.SECONDS);
                } catch (InterruptedException ignore) {
                    // do nothing
                }
            }
        }
    }

    /**
     * Determines if the reader has been shut down.
     *
     * @return {@code true} if {@link #destroy()} has been invoked, or the thread has been interrupted
     */
    private boolean isShutdown() {
        return shutdown || Thread.currentThread().isInterrupted();
    }

}
