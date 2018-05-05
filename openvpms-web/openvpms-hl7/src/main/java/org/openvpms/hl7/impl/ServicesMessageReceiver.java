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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import ca.uhn.hl7v2.protocol.Transportable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.hl7.io.Connector;
import org.openvpms.hl7.io.Connectors;
import org.openvpms.hl7.io.MessageDispatcher;
import org.openvpms.hl7.service.Services;
import org.springframework.beans.factory.DisposableBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link ReceivingApplication} that receives messages from {@link Services}.
 *
 * @author Tim Anderson
 */
public abstract class ServicesMessageReceiver implements DisposableBean {

    /**
     * The services.
     */
    private final Services services;

    /**
     * The dispatcher.
     */
    private final MessageDispatcher dispatcher;

    /**
     * The connectors.
     */
    private final Connectors connectors;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Listener for service additions/deletions.
     */
    private final Services.Listener listener;

    /**
     * The services that are being listened to.
     */
    private final Map<Long, Connector> listening = Collections.synchronizedMap(new HashMap<Long, Connector>());

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ServicesMessageReceiver.class);


    /**
     * Constructs a {@link ServicesMessageReceiver}.
     *
     * @param services   the services
     * @param dispatcher the dispatcher
     * @param connectors the connectors
     * @param service    the archetype service
     */
    public ServicesMessageReceiver(Services services, IArchetypeService service, MessageDispatcher dispatcher,
                                   Connectors connectors) {
        this.services = services;
        this.service = service;
        this.dispatcher = dispatcher;
        this.connectors = connectors;
        listener = new Services.Listener() {
            @Override
            public void added(Entity service) {
                listen(service, true);
            }

            @Override
            public void removed(Entity service) {
                stop(service);
            }
        };
    }

    /**
     * Invoked by a BeanFactory on destruction of a singleton.
     */
    @Override
    public void destroy() {
        services.removeListener(listener);

        List<Connector> connectors = getConnectors();
        for (Connector connector : connectors) {
            dispatcher.stop(connector);
        }
    }

    /**
     * Determines if this can process a message.
     *
     * @param message an inbound HL7 message
     * @return {@code true} if this wishes to accept the message.
     */
    public abstract boolean canProcess(Message message);

    /**
     * Processes a message.
     *
     * @param message  the message
     * @param location the practice location
     * @throws HL7Exception for any HL7 error
     */
    public abstract void process(Message message, IMObjectReference location) throws HL7Exception;

    /**
     * Listen to services.
     * <p/>
     * Note that methods may be invoked before this method returns.
     */
    protected void listen() {
        // NOTE: methods may be called before construction is complete
        for (Entity service : services.getServices()) {
            listen(service, false);
        }

        services.addListener(listener);
        dispatcher.start();
    }

    /**
     * Returns the connectors that the service is listening on.
     *
     * @return the connectors
     */
    protected List<Connector> getConnectors() {
        return new ArrayList<>(listening.values());
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
    }

    /**
     * Processes a message, and generates a response.
     *
     * @param message   the message
     * @param reference the service reference
     * @return the response
     * @throws ReceivingApplicationException
     * @throws HL7Exception                  for any HL7 error
     */
    protected Message processMessage(Message message, IMObjectReference reference)
            throws ReceivingApplicationException, HL7Exception {
        log(message);

        Entity entity = services.getService(reference);
        if (entity == null) {
            // service has been disabled
            throw new ReceivingApplicationException("Service not found: " + reference);
        }

        IMObjectBean bean = new IMObjectBean(entity, service);
        IMObjectReference location = bean.getNodeTargetObjectRef("location");

        Message response;

        try {
            process(message, location);
            response = message.generateACK();
        } catch (HL7Exception exception) {
            throw exception;
        } catch (Exception exception) {
            throw new HL7Exception(exception);
        }
        return response;
    }


    /**
     * Start listening to messages from a service.
     * <p/>
     * If the service is already being listened via a different connector, the existing connection will be terminated.
     *
     * @param service the service
     * @param start   if {@code true}, start the dispatcher
     */
    private void listen(Entity service, boolean start) {
        Connector current = listening.get(service.getId());
        EntityBean bean = new EntityBean(service, this.service);
        Connector connector = getConnector(bean);
        User user = getUser(bean);
        boolean listen = true;
        if (current != null && connector != null) {
            if (!current.equals(connector)) {
                stop(service);
            } else {
                // same connector - do nothing
                listen = false;
            }
        }
        if (connector != null && user != null) {
            if (listen) {
                try {
                    dispatcher.listen(connector, new Receiver(service), user);
                    if (start) {
                        dispatcher.start();
                    }
                    listening.put(service.getId(), connector);
                } catch (Throwable exception) {
                    log.warn("Failed to start listening to connections from service, name="
                             + service.getName() + ", id=" + service.getId() + ")", exception);
                }
            }
        } else if (current != null) {
            // terminate the existing connection. No new connector defined
            stop(service);
        } else {
            log.info("Service (name=" + service.getName() + ", id=" + service.getId() +
                     ") has no receiver connection or user defined, skipping");
        }
    }

    /**
     * Stops listening to messages from a service.
     *
     * @param service the service
     */
    private void stop(Entity service) {
        Connector connector = listening.remove(service.getId());
        if (connector != null) {
            log.info("Stopping listener for service (name=" + service.getName() + ", id=" + service.getId() + ")");
            dispatcher.stop(connector);
        }
    }

    /**
     * Returns the receive connection for a service.
     *
     * @param service the service
     * @return the dispense connector, or {@code null} if none is defined
     */
    private Connector getConnector(EntityBean service) {
        IMObjectReference ref = service.getNodeTargetObjectRef("receiver");
        return (ref != null) ? connectors.getConnector(ref) : null;
    }

    /**
     * Returns the user for a service.
     *
     * @param service the service
     * @return the user. May be {@code null}
     */
    private User getUser(EntityBean service) {
        return (User) service.getNodeTargetEntity("user");
    }

    /**
     * Logs a message.
     *
     * @param message the message
     */
    private void log(Message message) {
        if (log.isDebugEnabled()) {
            String formatted;
            try {
                formatted = message.encode();
                formatted = formatted.replaceAll("\\r", "\n");
            } catch (HL7Exception exception) {
                formatted = exception.getMessage();
            }
            log.debug("Received message: \n" + formatted);
        }
    }

    private class Receiver implements ReceivingApplication {

        private final IMObjectReference reference;

        public Receiver(Entity entity) {
            reference = entity.getObjectReference();
        }

        /**
         * Uses the contents of the message for whatever purpose the application
         * has for this message, and returns an appropriate response message.
         *
         * @param message     an inbound HL7 message
         * @param theMetadata message metadata (which may include information about where the message comes
         *                    from, etc).  This is the same metadata as in {@link Transportable#getMetadata()}.
         * @return an appropriate application response (for example an application ACK or query response).
         * Appropriate responses to different types of incoming messages are defined by HL7.
         * @throws ReceivingApplicationException if there is a problem internal to the application (for example
         *                                       a database problem)
         * @throws HL7Exception                  if there is a problem with the message
         */
        @Override
        public Message processMessage(Message message, Map<String, Object> theMetadata)
                throws ReceivingApplicationException, HL7Exception {
            return ServicesMessageReceiver.this.processMessage(message, reference);
        }

        /**
         * @param theMessage an inbound HL7 message
         * @return true if this ReceivingApplication wishes to accept the message.  By returning
         * true, this Application declares itself the recipient of the message, accepts
         * responsibility for it, and must be able to respond appropriately to the sending system.
         */
        @Override
        public boolean canProcess(Message theMessage) {
            return ServicesMessageReceiver.this.canProcess(theMessage);
        }

    }


}
