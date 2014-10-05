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

import ca.uhn.hl7v2.model.Message;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.hl7.io.Connector;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.patient.PatientEventServices;
import org.openvpms.hl7.patient.PatientInformationService;

import java.util.Collection;

/**
 * Default implementation of the {@link PatientInformationService}.
 *
 * @author Tim Anderson
 */
public class PatientInformationServiceImpl implements PatientInformationService {

    /**
     * The patient event listeners.
     */
    private final PatientEventServices services;

    /**
     * The connector manager.
     */
    private final MessageDispatcher manager;

    /**
     * The message factory.
     */
    private ADTMessageFactory factory;


    /**
     * Constructs an {@link PatientInformationServiceImpl}.
     *
     * @param service  the archetype service
     * @param lookups  the lookup service
     * @param services the patient event listeners
     * @param manager  the connector manager
     */
    public PatientInformationServiceImpl(IArchetypeService service, ILookupService lookups,
                                         PatientEventServices services, MessageDispatcherImpl manager) {
        this.factory = new ADTMessageFactory(manager.getMessageContext(), service, lookups);
        this.services = services;
        this.manager = manager;
    }

    /**
     * Notifies that a patient has been admitted.
     *
     * @param context the patient context
     */
    @Override
    public void admitted(PatientContext context) {
        Collection<Connector> senders = services.getConnections(context.getLocation());
        for (Connector connector : senders) {
            MessageConfig config = MessageConfigFactory.create(connector);
            Message message = factory.createAdmit(context, config);
            queue(message, connector, config);
        }
    }


    /**
     * Notifies that an admission has been cancelled.
     *
     * @param context the patient context
     */
    @Override
    public void admissionCancelled(PatientContext context) {
        Collection<Connector> senders = services.getConnections(context.getLocation());
        for (Connector connector : senders) {
            MessageConfig config = MessageConfigFactory.create(connector);
            Message message = factory.createCancelAdmit(context, config);
            queue(message, connector, config);
        }
    }

    /**
     * Notifies that a patient has been discharged.
     *
     * @param context the patient context
     */
    @Override
    public void discharged(PatientContext context) {
        Collection<Connector> senders = services.getConnections(context.getLocation());
        for (Connector connector : senders) {
            MessageConfig config = MessageConfigFactory.create(connector);
            Message message = factory.createDischarge(context, config);
            queue(message, connector, config);
        }
    }

    /**
     * Notifies that a patient has been updated.
     *
     * @param context the patient context
     */
    @Override
    public void updated(PatientContext context) {
        Collection<Connector> senders = services.getConnections(context.getLocation());
        for (Connector connector : senders) {
            MessageConfig config = MessageConfigFactory.create(connector);
            Message message = factory.createUpdate(context, config);
            queue(message, connector, config);
        }
    }

    /**
     * Queues a message for dispatch.
     *
     * @param message   the message to queue
     * @param connector the connector
     * @param config    the message config
     */
    protected void queue(Message message, Connector connector, MessageConfig config) {
        manager.queue(message, connector, config);
    }

}
