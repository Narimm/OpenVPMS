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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.util.idgenerator.IDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.security.RunAs;
import org.openvpms.hl7.io.Connector;
import org.openvpms.hl7.io.Connectors;
import org.openvpms.hl7.io.MessageDispatcher;
import org.openvpms.hl7.io.Statistics;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of the {@link MessageDispatcher} interface.
 *
 * @author Tim Anderson
 */
public class MessageDispatcherImpl implements MessageDispatcher, DisposableBean, InitializingBean {

    /**
     * The connectors.
     */
    private final ConnectorsImpl connectors;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The practice rules.
     */
    private final PracticeRules rules;

    /**
     * The message header populator.
     */
    private final HeaderPopulator populator;

    /**
     * The message context.
     */
    private final HapiContext messageContext;

    /**
     * The message control ID generator.
     */
    private final IDGenerator generator;

    /**
     * The queues, keyed on connector reference.
     */
    private final Map<IMObjectReference, MessageQueue> queueMap = new HashMap<IMObjectReference, MessageQueue>();

    /**
     * The receivers, keyed on connector reference.
     */
    private final Map<IMObjectReference, MessageReceiver> receiverMap = new HashMap<IMObjectReference, MessageReceiver>();

    /**
     * The listeners, keyed on port.
     */
    private final Map<Integer, HL7Service> services = new HashMap<Integer, HL7Service>();

    /**
     * The service to schedule dispatching.
     */
    private final ExecutorService executor;

    /**
     * The handler for <em>document.hl7</em> acts.
     */
    private final HL7DocumentHandler handler;

    /**
     * Listener for connector updates.
     */
    private final ConnectorsImpl.Listener listener;


    /**
     * Used to restricted the number of tasks that can be scheduled via the executor.
     */
    private Semaphore scheduled = new Semaphore(1);

    /**
     * Used to indicate that the dispatcher has been shut down.
     */
    private volatile boolean shutdown = false;

    /**
     * Used to wait if errors have occurred sending messages. This waits until a time expires or a message is queued,
     */
    private final Semaphore waiter = new Semaphore(0);

    /**
     * The user to initialise the security context in the dispatch thread.
     */
    private volatile User user;

    /**
     * Limits the no. of times an error is logged if the user isn't configured.
     */
    private boolean missingUser;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(MessageDispatcherImpl.class);

    /**
     * Constructs a {@link MessageDispatcherImpl}.
     *
     * @param connectors the connectors
     * @param service    the archetype service
     * @param rules      the practice rules
     */
    public MessageDispatcherImpl(ConnectorsImpl connectors, IArchetypeService service, PracticeRules rules) {
        this.connectors = connectors;
        this.service = service;
        this.rules = rules;
        populator = new HeaderPopulator();
        messageContext = HapiContextFactory.create();
        generator = messageContext.getParserConfiguration().getIdGenerator();
        executor = Executors.newSingleThreadExecutor();
        handler = new HL7DocumentHandler(service);

        user = getServiceUser();

        listener = new ConnectorsImpl.Listener() {
            @Override
            public void added(Connector connector) {
                update(connector);
            }

            @Override
            public void removed(Connector connector) {
                remove(connector);
            }
        };
    }

    /**
     * Returns the message context.
     *
     * @return the message context
     */
    public HapiContext getMessageContext() {
        return messageContext;
    }

    /**
     * Queues a message to a connector.
     *
     * @param message   the message to queue
     * @param connector the connector
     * @param config    the message population configuration
     * @param user      the user responsible for the message
     * @return the queued message
     */
    @Override
    public DocumentAct queue(Message message, Connector connector, MessageConfig config, User user) {
        DocumentAct result;
        if (!(connector instanceof MLLPSender)) {
            throw new IllegalArgumentException("Unsupported connector: " + connector);
        }
        try {
            populate(message, (MLLPSender) connector, config);
            result = queue(message, (MLLPSender) connector, user);
        } catch (Throwable exception) {
            throw new IllegalStateException(exception);
        }
        return result;
    }

    /**
     * Registers an application to handle messages from the specified connector.
     * <p/>
     * Only one application can be registered to handle messages.
     *
     * @param connector   the connector
     * @param application the receiving application
     * @throws IllegalStateException if the connector is registered
     */
    @Override
    public void listen(Connector connector, ReceivingApplication application) throws InterruptedException {
        if (connector instanceof MLLPReceiver) {
            int port = ((MLLPReceiver) connector).getPort();
            HL7Service service;
            synchronized (services) {
                service = services.get(port);
                if (service == null || !service.isRunning()) {
                    service = messageContext.newServer(port, false);
                } else {
                    throw new IllegalStateException("Already receiving requests on port " + port);
                }
            }
            log.info("Starting listener for " + connector);
            MessageReceiver receiver = new MessageReceiver(application, connector);
            service.registerApplication(receiver);
            service.setExceptionHandler(receiver);
            service.startAndWait();
            boolean added = false;
            synchronized (services) {
                if (service.isRunning()) {
                    services.put(port, service);
                    added = true;
                } else {
                    log.error("Failed to start listener for " + connector);
                }
            }
            if (added) {
                synchronized (receiverMap) {
                    receiverMap.put(connector.getReference(), receiver);
                }
            }
        }
    }

    /**
     * Stops listening to messages from a connector.
     *
     * @param connector the connector
     */
    @Override
    public void stop(Connector connector) {
        if (connector instanceof MLLPReceiver) {
            int port = ((MLLPReceiver) connector).getPort();
            HL7Service service;
            synchronized (services) {
                service = services.remove(port);
            }
            synchronized (receiverMap) {
                receiverMap.remove(connector.getReference());
            }
            if (service != null) {
                log.info("Stopping listener for " + connector);
                service.stopAndWait();
            }
        }
    }

    /**
     * Returns the statistics for a connector.
     *
     * @param connector the connector reference
     * @return the statistics, or {@code null} if the connector doesn't exist or is inactive
     */
    @Override
    public Statistics getStatistics(IMObjectReference connector) {
        Statistics statistics;
        synchronized (queueMap) {
            statistics = queueMap.get(connector);
        }
        if (statistics == null) {
            synchronized (receiverMap) {
                statistics = receiverMap.get(connector);
            }
        }
        return statistics;
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p/>
     * This delegates to {@link #initialise}
     */
    @Override
    public void afterPropertiesSet() {
        initialise();
    }

    /**
     * Initialises the dispatcher.
     */
    public void initialise() {
        initialise(connectors);
        connectors.addListener(listener);

        log.info("HL7 message dispatcher initialised");
        schedule();
    }

    /**
     * Invoked by a BeanFactory on destruction of a singleton.
     *
     * @throws Exception in case of shutdown errors.
     *                   Exceptions will get logged but not rethrown to allow
     *                   other beans to release their resources too.
     */
    @Override
    public void destroy() throws Exception {
        shutdown = true;
        connectors.removeListener(listener);
        executor.shutdown();  // Disable new tasks from being submitted
        waiter.release();     // wake from sleep
        try {
            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.error("Pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
        synchronized (services) {
            for (HL7Service service : services.values()) {
                service.stop();
            }
        }
    }

    /**
     * Creates a new timestamp for MSH-7.
     *
     * @return a new timestamp
     */
    protected Date createMessageTimestamp() {
        return new Date();
    }

    /**
     * Creates a new message control ID, for MSH-10.
     *
     * @return a new ID
     * @throws IOException if the ID cannot be generated
     */
    protected String createMessageControlID() throws IOException {
        return generator.getID();
    }

    /**
     * Initialise the connector queues.
     *
     * @param connectors the connectors
     */
    protected void initialise(Connectors connectors) {
        // initialise the queues
        for (Connector connector : connectors.getConnectors()) {
            if (connector instanceof MLLPSender) {
                getMessageQueue((MLLPSender) connector);
            }
        }
    }

    /**
     * Queues a message to a sender.
     *
     * @param message the message
     * @param sender  the sender
     * @param user    the user responsible for the message
     * @return the queued message
     * @throws HL7Exception if the message cannot be encoded
     */
    protected DocumentAct queue(Message message, final MLLPSender sender, User user) throws HL7Exception {
        DocumentAct result;
        MessageQueue queue;
        synchronized (queueMap) {
            queue = getMessageQueue(sender);
        }

        result = queue.add(message, user);
        schedule();
        return result;
    }

    /**
     * Sends a message, and returns the response.
     *
     * @param message the message to send
     * @param sender  the sender configuration
     * @return the response
     * @throws HL7Exception if the connection can not be initialised for any reason
     * @throws LLPException for any LLP error
     * @throws IOException  for any I/O error
     */
    protected Message send(Message message, MLLPSender sender) throws HL7Exception, LLPException, IOException {
        Message response;
        boolean debug = log.isDebugEnabled();
        if (debug) {
            log("sending", message);
        }
        Connection connection = null;
        try {
            connection = messageContext.newClient(sender.getHost(), sender.getPort(), false);
            connection.getInitiator().setTimeout(30, TimeUnit.SECONDS);
            response = connection.getInitiator().sendAndReceive(message);
            if (debug) {
                log("response", response);
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        return response;
    }

    /**
     * Populates the header of a message.
     *
     * @param message the message
     * @param sender  the sender
     * @param config  the message population configuration
     * @throws HL7Exception for any error
     * @throws IOException  if a message control ID cannot be generated
     */
    private void populate(Message message, MLLPSender sender, MessageConfig config) throws HL7Exception, IOException {
        populator.populate(message, sender, createMessageTimestamp(), createMessageControlID(), config);
    }

    /**
     * Returns a queue for a sender, creating one if none exists.
     *
     * @param sender the sender
     * @return the queue
     */
    private MessageQueue getMessageQueue(MLLPSender sender) {
        MessageQueue queue;
        queue = queueMap.get(sender.getReference());
        if (queue == null) {
            queue = new MessageQueue(sender, service, handler, messageContext);
            queueMap.put(sender.getReference(), queue);
        }
        return queue;
    }

    /**
     * Schedules {@link #dispatch()} to be run, unless it is already running.
     */
    private void schedule() {
        waiter.release(); // wakes up dispatch() if it is waiting

        final User user = getServiceUser();

        if (!shutdown && user != null && scheduled.tryAcquire()) {
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
        }
    }

    /**
     * Sends all queued messages.
     */
    private void dispatch() {
        boolean processed;
        int waiting;
        long minWait;
        do {
            processed = false;
            waiting = 0;
            minWait = 0;
            List<MessageQueue> queues;
            synchronized (queueMap) {
                queues = new ArrayList<MessageQueue>(queueMap.values());
            }
            for (MessageQueue queue : queues) {
                // process each queue in a round robin fashion
                if (!queue.isSuspended()) {
                    long wait = queue.getWaitUntil();
                    if (wait == -1 || wait <= System.currentTimeMillis()) {
                        processed |= sendFirst(queue);
                    } else {
                        ++waiting;
                        if (minWait == 0 || wait < minWait) {
                            minWait = wait;
                        }
                    }
                }
            }
        } while (processed && !shutdown);
        if (waiting != 0 && !shutdown) {
            long wait = minWait - System.currentTimeMillis();
            if (wait > 0) {
                // wait until the minimum wait time has expired, or a message is queued
                try {
                    waiter.drainPermits();
                    waiter.tryAcquire(wait, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ignore) {
                    // do nothing
                }
            }
            schedule();
        }
    }

    /**
     * Sends the first message in a queue, if any are present.
     *
     * @param queue the queue
     * @return {@code true} if there was a message
     */
    protected boolean sendFirst(MessageQueue queue) {
        boolean processed = false;
        Message message = queue.peekFirst();
        MLLPSender connector = queue.getConnector();
        if (message != null) {
            processed = true;
            try {
                Message response = send(message, connector);
                queue.sent(response);
            } catch (Throwable exception) {
                log.error("Failed to send message", exception);
                // failed to send the message, so don't queue for another 30 seconds
                queue.setWaitUntil(System.currentTimeMillis() + 30 * 1000);
                queue.error(exception);
            }
        }
        return processed;
    }

    /**
     * Invoked when a connector is updated.
     *
     * @param connector the connector
     */
    private void update(Connector connector) {
        if (connector instanceof MLLPSender) {
            restartSender(connector);
        } else if (connector instanceof MLLPReceiver) {
            restartReceiver(connector);
        }
    }

    /**
     * Invoked to restart a receiver.
     *
     * @param connector the connector to use
     */
    private void restartReceiver(Connector connector) {
        MessageReceiver receiver;
        synchronized (receiverMap) {
            receiver = receiverMap.get(connector.getReference());
        }
        if (receiver != null) {
            ReceivingApplication app = receiver.getReceivingApplication();
            stop(connector);
            try {
                listen(connector, app);
            } catch (InterruptedException exception) {
                log.error("Failed to update " + connector, exception);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Invoked to restart a sender.
     *
     * @param connector the connector
     */
    private void restartSender(Connector connector) {
        synchronized (queueMap) {
            MessageQueue queue = queueMap.get(connector.getReference());
            if (queue != null) {
                log.info("Updating " + connector);
                queue.setConnector((MLLPSender) connector);
                queue.setWaitUntil(-1);
                schedule();
            }
        }
    }

    /**
     * Invoked when a connector is removed or de-activated.
     *
     * @param connector the connector
     */
    private void remove(Connector connector) {
        if (connector instanceof MLLPSender) {
            synchronized (queueMap) {
                MessageQueue queue = queueMap.remove(connector.getReference());
                if (queue != null) {
                    // Note that a call to queue() could re-add the queue, even if it is inactive.
                    log.info("Removed queue for " + connector);
                    queue.setSuspended(true);
                }
            }
        } else if (connector instanceof MLLPReceiver) {
            stop(connector);
        }
    }

    /**
     * Logs a message.
     *
     * @param prefix  the logging prefix
     * @param message the message
     */
    private void log(String prefix, Message message) {
        String formatted;
        try {
            formatted = message.encode();
            formatted = formatted.replaceAll("\r", "\n");
        } catch (HL7Exception exception) {
            formatted = exception.getMessage();
        }
        log.debug(prefix + ": " + formatted);
    }

    /**
     * Returns the user to set the security context in the dispatch thread.
     *
     * @return the user, or {@code null} if none has been configured
     */
    private User getServiceUser() {
        if (user == null) {
            synchronized (this) {
                if (user == null) {
                    Party practice = rules.getPractice();
                    if (practice != null) {
                        user = rules.getServiceUser(practice);
                    }
                    if (user == null && !missingUser) {
                        log.error("Missing party.organisationPractice serviceUser. Messages cannot be sent until "
                                  + "this is configured");
                        missingUser = true;
                    }
                }
            }
        }
        return user;
    }
}
