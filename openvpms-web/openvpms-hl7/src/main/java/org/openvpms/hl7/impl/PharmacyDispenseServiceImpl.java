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
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import ca.uhn.hl7v2.protocol.Transportable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.hl7.Connector;
import org.springframework.beans.factory.DisposableBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to process pharmacy dispense events.
 *
 * @author Tim Anderson
 */
public class PharmacyDispenseServiceImpl implements ReceivingApplication, DisposableBean {

    /**
     * The pharmacies.
     */
    private final Pharmacies pharmacies;

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
     * The pharmacies that are being listened to.
     */
    private final Map<Long, Connector> listening = Collections.synchronizedMap(new HashMap<Long, Connector>());

    /**
     * Listener for pharmacy additions/deletions.
     */
    private final Pharmacies.Listener listener;

    /**
     * The logger.
     */
    private final Log log = LogFactory.getLog(PharmacyDispenseServiceImpl.class);


    /**
     * Constructs an {@link PharmacyDispenseServiceImpl}.
     *
     * @param pharmacies the pharmacies
     * @param dispatcher the dispatcher
     * @param connectors the connectors
     * @param service    the archetype service
     */
    public PharmacyDispenseServiceImpl(Pharmacies pharmacies, MessageDispatcher dispatcher,
                                       Connectors connectors, IArchetypeService service) {
        this.pharmacies = pharmacies;
        this.dispatcher = dispatcher;
        this.connectors = connectors;
        this.service = service;

        // NOTE: methods may be called before construction is complete
        for (Entity pharmacy : pharmacies.getPharmacies()) {
            listen(pharmacy);
        }

        listener = new Pharmacies.Listener() {
            @Override
            public void added(Entity pharmacy) {
                listen(pharmacy);
            }

            @Override
            public void removed(Entity pharmacy) {
                stop(pharmacy);
            }
        };
        pharmacies.addListener(listener);
    }

    /**
     * Invoked by a BeanFactory on destruction of a singleton.
     */
    @Override
    public void destroy() {
        pharmacies.removeListener(listener);

        List<Connector> connectors = new ArrayList<Connector>(listening.values());
        for (Connector connector : connectors) {
            dispatcher.stop(connector);
        }
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
        log(theMessage);
        try {
            return theMessage.generateACK();
        } catch (IOException e) {
            throw new HL7Exception(e);
        }
    }

    /**
     * @param theMessage an inbound HL7 message
     * @return true if this ReceivingApplication wishes to accept the message.  By returning
     *         true, this Application declares itself the recipient of the message, accepts
     *         responsibility for it, and must be able to respond appropriately to the sending system.
     */
    @Override
    public boolean canProcess(Message theMessage) {
        return true;
    }

    /**
     * Start listening to messages from a pharmacy.
     * <p/>
     * If the pharmacy is already being listened via a different connector, the existing connection will be terminated.
     *
     * @param pharmacy the pharmacy
     */
    private void listen(Entity pharmacy) {
        Connector current = listening.get(pharmacy.getId());
        Connector connector = getConnector(pharmacy);
        boolean listen = true;
        if (current != null && connector != null) {
            if (!current.equals(connector)) {
                stop(pharmacy);
            } else {
                // same connector - do nothing
                listen = false;
            }
        }
        if (connector != null) {
            if (listen) {
                try {
                    dispatcher.listen(connector, this);
                    listening.put(pharmacy.getId(), connector);
                } catch (Throwable exception) {
                    log.warn("Failed to start listening to connections from pharmacy, name="
                             + pharmacy.getName() + ", id=" + pharmacy.getId() + ")", exception);
                }
            }
        } else if (current != null) {
            // terminate the existing connection. No new connector defined
            stop(pharmacy);
        } else {
            log.info("Pharmacy (name=" + pharmacy.getName() + ", id=" + pharmacy.getId()
                     + ") has no dispense connection defined, skipping");
        }
    }

    /**
     * Stops listening to messages from a pharmacy.
     *
     * @param pharmacy the pharmacy
     */
    private void stop(Entity pharmacy) {
        Connector connector = listening.remove(pharmacy.getId());
        if (connector != null) {
            log.info("Stopping listener for pharmacy (name=" + pharmacy.getName() + ", id=" + pharmacy.getId() + ")");
            dispatcher.stop(connector);
        }
    }

    /**
     * Returns the dispense connection for a pharmacy.
     *
     * @param pharmacy the pharmacy
     * @return the dispense connector, or {@code null} if none is defined
     */
    private Connector getConnector(Entity pharmacy) {
        EntityBean bean = new EntityBean(pharmacy, service);
        IMObjectReference ref = bean.getNodeTargetObjectRef("dispenseConnection");
        return (ref != null) ? connectors.getConnector(ref) : null;
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

}
