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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.model.Message;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.hl7.io.Connector;
import org.openvpms.hl7.io.MessageDispatcher;
import org.openvpms.hl7.laboratory.Laboratories;
import org.openvpms.hl7.laboratory.LaboratoryOrderService;
import org.openvpms.hl7.patient.PatientContext;

import java.util.Date;

/**
 * Default implementation of the {@link LaboratoryOrderService}.
 *
 * @author Tim Anderson
 */
public class LaboratoryOrderServiceImpl implements LaboratoryOrderService {

    /**
     * The laboratories.
     */
    private final Laboratories laboratories;

    /**
     * The message dispatcher.
     */
    private final MessageDispatcher dispatcher;

    /**
     * The message factory.
     */
    private final ORMMessageFactory factory;

    /**
     * Constructs a {@link LaboratoryOrderServiceImpl}.
     *
     * @param service      the archetype service
     * @param lookups      the lookup service
     * @param laboratories the laboratories
     * @param dispatcher   the message dispatcher
     */
    public LaboratoryOrderServiceImpl(IArchetypeService service, ILookupService lookups, Laboratories laboratories,
                                      MessageDispatcherImpl dispatcher) {
        this.laboratories = laboratories;
        this.dispatcher = dispatcher;
        factory = new ORMMessageFactory(dispatcher.getMessageContext(), service, lookups);
    }

    /**
     * Creates an order, placing it with the specified laboratory.
     *
     * @param context           the patient context
     * @param placerOrderNumber the placer order number, to uniquely identify the order
     * @param serviceId         the universal service identifier
     * @param date              the order date
     * @param laboratory        the laboratory. An <em>entity.HL7ServiceLaboratory</em>
     * @param user              the user that generated the order
     * @return {@code true} if the order was placed
     */
    @Override
    public boolean createOrder(PatientContext context, long placerOrderNumber, String serviceId, Date date,
                               Entity laboratory, User user) {
        boolean result = false;
        Connector connector = laboratories.getSender(laboratory);
        if (connector != null) {
            HL7Mapping config = connector.getMapping();
            Message message = factory.createOrder(context, placerOrderNumber, serviceId, date, config);
            dispatcher.queue(message, connector, config, user);
            result = true;
        }
        return result;
    }

    /**
     * Cancels an order.
     *
     * @param context           the patient context
     * @param placerOrderNumber the placer order number, to uniquely identify the order
     * @param serviceId         the universal service identifier
     * @param date              the order date
     * @param laboratory        the laboratory. An <em>entity.HL7ServiceLaboratory</em>
     * @param user              the user that generated the cancellation
     * @return {@code true} if a cancellation was sent
     */
    @Override
    public boolean cancelOrder(PatientContext context, long placerOrderNumber, String serviceId, Date date,
                               Entity laboratory, User user) {
        boolean result = false;
        Connector connector = laboratories.getSender(laboratory);
        if (connector != null) {
            HL7Mapping config = connector.getMapping();
            Message message = factory.cancelOrder(context, placerOrderNumber, serviceId, date, config);
            dispatcher.queue(message, connector, config, user);
            result = true;
        }
        return result;
    }
}
