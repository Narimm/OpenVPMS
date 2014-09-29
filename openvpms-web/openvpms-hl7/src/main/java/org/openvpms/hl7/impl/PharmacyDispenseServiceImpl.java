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

import ca.uhn.hl7v2.AcknowledgmentCode;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.message.RDS_O13;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import ca.uhn.hl7v2.protocol.Transportable;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.hl7.Connector;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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
     * The payment processor.
     */
    private final RDSProcessor processor;

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
     * @param rules      the patient rules
     */
    public PharmacyDispenseServiceImpl(Pharmacies pharmacies, MessageDispatcher dispatcher,
                                       Connectors connectors, IArchetypeService service,
                                       PatientRules rules) {
        this.pharmacies = pharmacies;
        this.dispatcher = dispatcher;
        this.connectors = connectors;
        this.service = service;
        processor = new RDSProcessor(service, rules);

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

        List<Connector> connectors = getConnectors();
        for (Connector connector : connectors) {
            dispatcher.stop(connector);
        }
    }

    /**
     * Uses the contents of the message for whatever purpose the application
     * has for this message, and returns an appropriate response message.
     *
     * @param message     an inbound HL7 message
     * @param theMetadata message metadata (which may include information about where the message comes
     *                    from, etc).  This is the same metadata as in {@link Transportable#getMetadata()}.
     * @return an appropriate application response (for example an application ACK or query response).
     *         Appropriate responses to different types of incoming messages are defined by HL7.
     * @throws ReceivingApplicationException if there is a problem internal to the application (for example
     *                                       a database problem)
     * @throws HL7Exception                  if there is a problem with the message
     */
    @Override
    public Message processMessage(Message message, Map<String, Object> theMetadata)
            throws ReceivingApplicationException, HL7Exception {
        log(message);
        RDS_O13 dispense = (RDS_O13) message;
        String sendingFacility = dispense.getMSH().getSendingFacility().getNamespaceID().getValue();
        String sendingApplication = dispense.getMSH().getSendingApplication().getNamespaceID().getValue();
        String receivingFacility = dispense.getMSH().getReceivingFacility().getNamespaceID().getValue();
        String receivingApplication = dispense.getMSH().getReceivingApplication().getNamespaceID().getValue();
        boolean found = false;
        for (Connector connector : getConnectors()) {
            if (ObjectUtils.equals(sendingFacility, connector.getSendingFacility())
                && ObjectUtils.equals(sendingApplication, connector.getSendingApplication())
                && ObjectUtils.equals(receivingFacility, connector.getReceivingFacility())
                && ObjectUtils.equals(receivingApplication, connector.getReceivingApplication())) {
                found = true;
                break;
            }
        }
        if (!found) {
            try {
                return message.generateACK(AcknowledgmentCode.AR, new HL7Exception("Unrecognised application details"));
            } catch (IOException exception) {
                throw new HL7Exception(exception);
            }
        }

        IMObjectReference reference = (IMObjectReference) theMetadata.get("pharmacy");
        Entity pharmacy = pharmacies.getPharmacy(reference);
        if (pharmacy == null) {
            throw new HL7Exception("Pharmacy not found");
        }
        User user = getUser(pharmacy);
        if (user == null) {
            throw new HL7Exception("User not found");
        }

        try {
            initSecurityContext(user);
            process((RDS_O13) message);
            return message.generateACK();
        } catch (IOException exception) {
            throw new HL7Exception(exception);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * Determines if this can process a message.
     *
     * @param message an inbound HL7 message
     * @return {@code true} if this ReceivingApplication wishes to accept the message.
     */
    @Override
    public boolean canProcess(Message message) {
        return message instanceof RDS_O13;
    }

    /**
     * Initialises the security context.
     *
     * @param user the user
     */
    private void initSecurityContext(User user) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken authentication
                = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
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
                    dispatcher.listen(connector, new Receiver(this, pharmacy));
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
     * Processes an RDS message, returning the corresponding <em>act.customerOrderPharmacy</em> and child item
     * acts.
     *
     * @param message the message
     * @return the pharmacy order acts
     * @throws HL7Exception any HL7 error
     */
    protected List<Act> process(RDS_O13 message) throws HL7Exception {
        List<Act> order = processor.process(message);
        service.save(order);
        return order;
    }

    /**
     * Returns the connectors that the service is listening on.
     *
     * @return the connectors
     */
    protected List<Connector> getConnectors() {
        return new ArrayList<Connector>(listening.values());
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
     * Returns the user for a pharmacy.
     */
    private User getUser(Entity pharmacy) {
        EntityBean bean = new EntityBean(pharmacy, service);
        return (User) bean.getNodeTargetEntity("user");

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

    private static class Receiver implements ReceivingApplication {

        private final ReceivingApplication receiver;
        private final IMObjectReference reference;

        public Receiver(ReceivingApplication receiver, Entity pharmacy) {
            this.receiver = receiver;
            reference = pharmacy.getObjectReference();
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
        public Message processMessage(Message theMessage, Map<String, Object> theMetadata) throws ReceivingApplicationException, HL7Exception {
            Map<String, Object> copy = new HashMap<String, Object>(theMetadata);
            copy.put("pharmacy", reference);
            return receiver.processMessage(theMessage, copy);
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
    }

}
