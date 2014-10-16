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
import ca.uhn.hl7v2.model.v25.datatype.DTM;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import ca.uhn.hl7v2.protocol.ReceivingApplicationExceptionHandler;
import ca.uhn.hl7v2.protocol.Transportable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.hl7.io.Connector;
import org.openvpms.hl7.io.MessageDispatcher;
import org.openvpms.hl7.io.Statistics;
import org.springframework.beans.factory.DisposableBean;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default implementation of the {@link MessageDispatcher} interface.
 *
 * @author Tim Anderson
 */
public class MessageDispatcherImpl implements MessageDispatcher, DisposableBean {

    /**
     * The connectors.
     */
    private final ConnectorsImpl connectors;

    /**
     * The message header populator.
     */
    private final HeaderPopulator populator;

    /**
     * The message context.
     */
    private final HapiContext messageContext;

    /**
     * Used to assign the message control id.
     */
    private AtomicLong seed = new AtomicLong(0);

    private final Map<IMObjectReference, Queue> queueMap = new HashMap<IMObjectReference, Queue>();

    private final Map<IMObjectReference, Receiver> receiverMap = new HashMap<IMObjectReference, Receiver>();

    private final Map<Integer, HL7Service> services = new HashMap<Integer, HL7Service>();

    private final ExecutorService executor;

    /**
     * Listener for connector updates.
     */
    private final ConnectorsImpl.Listener listener;

    private volatile boolean shutdown = false;


    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(MessageDispatcherImpl.class);

    /**
     * Constructs a {@link MessageDispatcherImpl}.
     *
     * @param connectors the connectors
     */
    public MessageDispatcherImpl(ConnectorsImpl connectors) {
        this.connectors = connectors;
        populator = new HeaderPopulator();
        messageContext = HapiContextFactory.create();
        executor = Executors.newSingleThreadExecutor();
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
        connectors.addListener(listener);
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
     */
    @Override
    public void queue(Message message, Connector connector, MessageConfig config) {
        try {
            if (connector instanceof MLLPSender) {
                populate(message, (MLLPSender) connector, config);
                queue(message, (MLLPSender) connector);
            }
        } catch (Throwable exception) {
            throw new IllegalStateException(exception);
        }
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
            Receiver receiver = new Receiver(application, connector);
            service.registerApplication(receiver);
            service.setExceptionHandler(receiver);
            service.startAndWait();
            boolean added = false;
            synchronized (services) {
                if (service.isRunning()) {
                    services.put(port, service);
                    added = true;
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
     * Invoked by a BeanFactory on destruction of a singleton.
     *
     * @throws Exception in case of shutdown errors.
     *                   Exceptions will get logged but not rethrown to allow
     *                   other beans to release their resources too.
     */
    @Override
    public void destroy() throws Exception {
        shutdown = true;
        executor.shutdown(); // Disable new tasks from being submitted
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
        connectors.removeListener(listener);
    }

    protected Date getTimestamp() {
        return new Date();
    }

    protected long getSequence(Connector connector) {
        return seed.incrementAndGet();
    }

    /**
     * Populates the header of a message.
     *
     * @param message the message
     * @param sender  the sender
     * @param config  the message population configuration
     * @throws HL7Exception for any error
     */
    private void populate(Message message, MLLPSender sender, MessageConfig config) throws HL7Exception {
        populator.populate(message, sender, getTimestamp(), getSequence(sender), config);
    }

    protected void queue(Message message, final MLLPSender sender)
            throws HL7Exception, LLPException, IOException {
        Queue queue;
        synchronized (queueMap) {
            queue = queueMap.get(sender.getReference());
            if (queue == null) {
                queue = new Queue(sender);
                queueMap.put(sender.getReference(), queue);
            }
        }

        queue.add(message.encode());
        schedule();
    }

    private void schedule() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                sendAll();
            }
        });
    }

    /**
     * Sends all queued messages.
     */
    private void sendAll() {
        boolean processed;
        int waiting;
        long minWait;
        do {
            processed = false;
            waiting = 0;
            minWait = 0;
            List<Queue> queues;
            synchronized (queueMap) {
                queues = new ArrayList<Queue>(queueMap.values());
            }
            for (Queue queue : queues) {
                // process each queue in a round robin fashion
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
        } while (processed && !shutdown);
        if (waiting != 0 && !shutdown) {
            long wait = minWait - System.currentTimeMillis();
            if (wait > 0) {
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException ignore) {
                    // do nothing
                }
            }
            schedule();
        }
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
        boolean debug = log.isDebugEnabled();
        if (debug) {
            log("sending", message);
        }
        Connection connection = messageContext.newClient(sender.getHost(), sender.getPort(), false);
        connection.getInitiator().setTimeout(30, TimeUnit.SECONDS);
        Message response = connection.getInitiator().sendAndReceive(message);
        if (debug) {
            log("response", response);
        }
        return response;
    }

    /**
     * Logs a message.
     *
     * @param prefix  the logging prefix
     * @param message the message
     */
    protected void log(String prefix, Message message) {
        String formatted;
        try {
            formatted = message.encode();
            formatted = formatted.replaceAll("\\r", "\n");
        } catch (HL7Exception exception) {
            formatted = exception.getMessage();
        }
        log.debug(prefix + ": " + formatted);
    }

    /**
     * Sends the first message in a queue, if any are present.
     *
     * @param queue the queue
     * @return {@code true} if there was a message
     */
    private boolean sendFirst(Queue queue) {
        boolean processed = false;
        String queued = queue.peekFirst();
        MLLPSender connector = queue.getConnector();
        if (queued != null && connector != null) {
            processed = true;
            Message message = null;
            try {
                message = messageContext.getPipeParser().parse(queued);
            } catch (HL7Exception exception) {
                // TODO - need to queue these to an error queue
                log.error("Failed to parse queued message", exception);
                queue.processed();
            }
            if (message != null) {
                try {
                    send(message, connector);
                    queue.processed();
                } catch (Throwable exception) {
                    // failed to send the message, so don't queue for another 30 seconds
                    queue.setWaitUntil(System.currentTimeMillis() + 30 * 1000);
                    queue.error(exception);
                }
            }
        }
        return processed;
    }

    private void update(Connector connector) {
        if (connector instanceof MLLPSender) {
            restartSender(connector);
        } else if (connector instanceof MLLPReceiver) {
            restartReceiver(connector);
        }
    }

    private void restartReceiver(Connector connector) {
        Receiver receiver;
        synchronized (receiverMap) {
            receiver = receiverMap.get(connector.getReference());
        }
        if (receiver != null) {
            ReceivingApplication app = receiver.receiver;
            stop(connector);
            try {
                listen(connector, app);
            } catch (InterruptedException exception) {
                log.error("Failed to update " + connector, exception);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void restartSender(Connector connector) {
        synchronized (queueMap) {
            Queue queue = queueMap.get(connector.getReference());
            if (queue != null) {
                log.info("Updating " + connector);
                queue.setConnector((MLLPSender) connector);
                queue.setWaitUntil(-1);
                schedule();
            }
        }
    }

    private void remove(Connector connector) {
        if (connector instanceof MLLPSender) {
            synchronized (queueMap) {
                Queue queue = queueMap.get(connector.getReference());
                if (queue != null) {
                    log.info("Suspending sending messages to " + connector);
                    queue.setConnector(null);
                }
            }
        } else if (connector instanceof MLLPReceiver) {
            stop(connector);
        }
    }


    /**
     * Workaround to ensure that the message header has the correct format for the message date/time.
     * <p/>
     * NOTE: this doesn't yet work if the receiver throws an exception and the nak is generated by
     * ApplicationRouterImpl.
     */
    private static class Receiver implements ReceivingApplication, ReceivingApplicationExceptionHandler, Statistics {

        private final Connector connector;

        private final ReceivingApplication receiver;

        private final MessageConfig config;

        private Date lastReceived;

        private Date lastError;

        private String lastErrorMessage;

        public Receiver(ReceivingApplication receiver, Connector connector) {
            this.connector = connector;
            config = new MessageConfig();
            config.setIncludeMillis(connector.isIncludeMillis());
            config.setIncludeTimeZone(connector.isIncludeTimeZone());
            this.receiver = receiver;

        }

        /**
         * Uses the contents of the message for whatever purpose the application
         * has for this message, and returns an appropriate response message.
         *
         * @param theMessage  an inbound HL7 message
         * @param theMetadata message metadata (which may include information about where the message comes
         *                    from, etc).  This is the same metadata as in {@link Transportable#getMetadata()}.
         * @return an appropriate application response (for example an application ACK or query response).
         *         Appropriate responses to different types of incoming messages are defined by HL7.
         * @throws ReceivingApplicationException if there is a problem internal to the application (for example
         *                                       a database problem)
         * @throws HL7Exception                  if there is a problem with the message
         */
        @Override
        public Message processMessage(Message theMessage, Map<String, Object> theMetadata)
                throws ReceivingApplicationException, HL7Exception {
            Message message = receiver.processMessage(theMessage, theMetadata);
            if (!connector.isIncludeMillis() || !connector.isIncludeTimeZone()) {
                // correct the date/time format
                try {
                    MSH msh = (MSH) message.get("MSH");
                    DTM time = msh.getDateTimeOfMessage().getTime();
                    Calendar calendar = time.getValueAsCalendar();
                    PopulateHelper.populateDTM(time, calendar, config);
                } catch (HL7Exception ignore) {
                    // do nothing
                }
            }
            processed();
            return message;
        }

        /**
         * @param theMessage an inbound HL7 message
         * @return true if this ReceivingApplication wishes to accept the message.  By returning
         *         true, this Application declares itself the recipient of the message, accepts
         *         responsibility for it, and must be able to respond appropriately to the sending system.
         */
        @Override
        public boolean canProcess(Message theMessage) {
            return receiver.canProcess(theMessage);
        }

        /**
         * Process an exception.
         *
         * @param incomingMessage  the incoming message. This is the raw message which was received from the external
         *                         system
         * @param incomingMetadata Any metadata that accompanies the incoming message.
         * @param outgoingMessage  the outgoing message. The response NAK message generated by HAPI.
         * @param e                the exception which was received
         * @return The new outgoing message. This can be set to the value provided by HAPI in {@code outgoingMessage},
         *         or may be replaced with another message. <b>This method may not return {@code null}</b>.
         */
        @Override
        public String processException(String incomingMessage, Map<String, Object> incomingMetadata,
                                       String outgoingMessage, Exception e) throws HL7Exception {
            error(e.getMessage());
            return outgoingMessage;
        }

        /**
         * Returns the number of messages in the queue.
         *
         * @return {@code 0} - the receiver doesn't support queuing
         */
        @Override
        public int getQueued() {
            return 0;
        }

        /**
         * Returns the time when a message was last successfully received or sent.
         *
         * @return the time when a message was last successfully sent, or {@code null} if none have been sent
         */
        @Override
        public synchronized Date getProcessedTimestamp() {
            return lastReceived;
        }

        /**
         * Returns the time of the last failure to send a message.
         *
         * @return the time of the last failure, or {@code null} if the last send was successful
         */
        @Override
        public synchronized Date getErrorTimestamp() {
            return lastError;
        }

        /**
         * Returns the last error message, if the last send was unsuccessful.
         *
         * @return the last error message. MAy be {@code null}
         */
        @Override
        public synchronized String getErrorMessage() {
            return lastErrorMessage;
        }

        /**
         * Returns the connector used to send messages via this queue.
         *
         * @return the connector
         */
        @Override
        public Connector getConnector() {
            return connector;
        }

        private synchronized void processed() {
            lastReceived = new Date();
            lastError = null;
            lastErrorMessage = null;
        }

        private synchronized void error(String message) {
            lastError = new Date();
            lastErrorMessage = message;
        }

    }

    private static class Queue implements Statistics {

        private Deque<String> queue = new ArrayDeque<String>();

        private MLLPSender connector;

        private Date lastSent;

        private Date lastError;

        private String lastErrorMessage;

        private long waitUntil = -1;

        public Queue(MLLPSender connector) {
            this.connector = connector;
        }

        public synchronized void add(String message) {
            queue.addLast(message);
        }

        public synchronized String peekFirst() {
            return queue.peekFirst();
        }

        public synchronized void processed() {
            if (queue.pollFirst() != null) {
                lastSent = new Date();
                lastError = null;
                lastErrorMessage = null;
            }
            waitUntil = -1;
        }

        public synchronized void setConnector(MLLPSender connector) {
            this.connector = connector;
        }

        public synchronized MLLPSender getConnector() {
            return connector;
        }

        public synchronized boolean isActive() {
            return connector != null;
        }

        public synchronized void setWaitUntil(long millis) {
            this.waitUntil = millis;
        }

        public synchronized long getWaitUntil() {
            return waitUntil;
        }

        public synchronized void error(Throwable exception) {
            lastError = new Date();
            lastErrorMessage = exception.getMessage();
        }

        /**
         * Returns the number of messages in the queue.
         *
         * @return the number of messages
         */
        public synchronized int getQueued() {
            return queue.size();
        }

        /**
         * Returns the time of the last processed message.
         * <p/>
         * For senders, this indicates the time when a message was last sent, and an acknowledgment received.
         * <p/>
         * For receivers, this indicates the time when a message was last received and processed.
         *
         * @return the time when a message was last processed, or {@code null} if none have been processed
         */
        @Override
        public synchronized Date getProcessedTimestamp() {
            return lastSent;
        }

        /**
         * Returns the time of the last error.
         *
         * @return the time of the last error, or {@code null} if the last message was not successfully processed
         */
        @Override
        public synchronized Date getErrorTimestamp() {
            return lastError;
        }

        /**
         * Returns the error message of the last error.
         *
         * @return the last error message. May be {@code null}
         */
        @Override
        public synchronized String getErrorMessage() {
            return lastErrorMessage;
        }

    }

}
