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

import ca.uhn.hl7v2.DefaultHapiContext;
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
import org.openvpms.hl7.Connector;
import org.openvpms.hl7.MLLPReceiver;
import org.openvpms.hl7.MLLPSender;
import org.springframework.beans.factory.DisposableBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default implementation of the {@link MessageDispatcher} interface.
 *
 * @author Tim Anderson
 */
public class MessageDispatcherImpl implements MessageDispatcher, DisposableBean {

    private final HeaderPopulator populator;

    private final HapiContext messageContext;

    private AtomicLong sequence = new AtomicLong(1);

    private List<ConnectorManagerListener> listeners
            = Collections.synchronizedList(new ArrayList<ConnectorManagerListener>());

    private final Map<Integer, HL7Service> services = new HashMap<Integer, HL7Service>();

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(MessageDispatcherImpl.class);

    /**
     * Constructs a {@link MessageDispatcherImpl}.
     */
    public MessageDispatcherImpl() {
        populator = new HeaderPopulator();
        messageContext = new DefaultHapiContext();

        // override the default sequence handling
        messageContext.getParserConfiguration().setIdGenerator(new IDGenerator() {
            @Override
            public String getID() {
                return "";
            }
        });
    }

    /**
     * Adds a listener to be notified when a message is sent.
     *
     * @param listener the listener
     */
    @Override
    public void addListener(ConnectorManagerListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    @Override
    public void removeListener(ConnectorManagerListener listener) {
        listeners.remove(listener);
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
                Message response = sendAndReceive(message, (MLLPSender) connector);
                for (ConnectorManagerListener listener : listeners) {
                    listener.sent(message, response);
                }
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
     * @param connector the connector
     * @param receiver  the receiver
     * @throws IllegalStateException if the connector is registered
     */
    @Override
    public void listen(Connector connector, ReceivingApplication receiver) throws InterruptedException {
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
            service.registerApplication(receiver);
            service.startAndWait();
            synchronized (services) {
                if (service.isRunning()) {
                    services.put(port, service);
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
            synchronized (services) {
                HL7Service service = services.get(port);
                if (service != null) {
                    service.stopAndWait();
                }
                services.remove(port);
            }
        }
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
        synchronized (services) {
            for (HL7Service service : services.values()) {
                service.stop();
            }
        }
    }

    protected Date getTimestamp() {
        return new Date();
    }

    protected long getSequence(Connector connector) {
        return sequence.incrementAndGet();
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

    protected Message sendAndReceive(Message message, MLLPSender sender)
            throws HL7Exception, LLPException, IOException {
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

}
