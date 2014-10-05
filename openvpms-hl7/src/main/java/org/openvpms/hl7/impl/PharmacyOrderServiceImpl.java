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
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.hl7.Connector;
import org.openvpms.hl7.PatientContext;
import org.openvpms.hl7.pharmacy.Pharmacies;
import org.openvpms.hl7.pharmacy.PharmacyOrderService;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Default implementation of the {@link PharmacyOrderService}.
 *
 * @author Tim Anderson
 */
public class PharmacyOrderServiceImpl implements PharmacyOrderService {

    /**
     * The pharmacies.
     */
    private final Pharmacies pharmacies;

    /**
     * The message dispatcher.
     */
    private final MessageDispatcher dispatcher;

    /**
     * The message factory.
     */
    private final RDEMessageFactory factory;

    /**
     * Constructs a {@link PharmacyOrderServiceImpl}.
     *
     * @param service    the archetype service
     * @param lookups    the lookup service
     * @param pharmacies the pharmacies
     * @param dispatcher the message dispatcher
     */
    public PharmacyOrderServiceImpl(IArchetypeService service, ILookupService lookups, Pharmacies pharmacies,
                                    MessageDispatcherImpl dispatcher) {
        this.pharmacies = pharmacies;
        this.dispatcher = dispatcher;
        factory = new RDEMessageFactory(dispatcher.getMessageContext(), service, lookups);
    }

    /**
     * Creates an order, placing it with the specified pharmacy.
     *
     * @param context           the patient context
     * @param product           the product to order
     * @param quantity          the quantity to order
     * @param placerOrderNumber the placer order number, to uniquely identify the order
     * @param date              the order date
     * @param pharmacy          the pharmacy. A <em>party.organisationPharmacy</em>
     */
    @Override
    public void createOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                            Date date, Entity pharmacy) {
        Connector connector = getConnector(pharmacy);
        if (connector != null) {
            MessageConfig config = MessageConfigFactory.create(connector);
            Message message = factory.createOrder(context, product, quantity, placerOrderNumber, date, config);
            dispatcher.queue(message, connector, config);
        }
    }

    /**
     * Updates an order.
     *
     * @param context           the patient context
     * @param product           the product to order
     * @param quantity          the quantity to order
     * @param placerOrderNumber the placer order number, to uniquely identify the order
     * @param date              the order date
     * @param pharmacy          the pharmacy. A <em>party.organisationPharmacy</em>
     */
    @Override
    public void updateOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                            Date date, Entity pharmacy) {
        Connector connector = getConnector(pharmacy);
        if (connector != null) {
            MessageConfig config = MessageConfigFactory.create(connector);
            Message message = factory.updateOrder(context, product, quantity, placerOrderNumber, date, config);
            dispatcher.queue(message, connector, config);
        }
    }

    /**
     * Cancels an order.
     *
     * @param context           the patient context
     * @param product           the product to order
     * @param quantity          the quantity to order
     * @param placerOrderNumber the placer order number, to uniquely identify the order
     * @param date              the order date
     * @param pharmacy          the pharmacy. A <em>party.organisationPharmacy</em>
     */
    @Override
    public void cancelOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                            Date date, Entity pharmacy) {
        Connector connector = getConnector(pharmacy);
        if (connector != null) {
            MessageConfig config = MessageConfigFactory.create(connector);
            Message message = factory.cancelOrder(context, product, quantity, placerOrderNumber, config, date);
            dispatcher.queue(message, connector, config);
        }
    }

    /**
     * Returns a connection for a pharmacy.
     *
     * @param pharmacy the pharmacy
     * @return the connection, or {@code null} if none is found
     */
    private Connector getConnector(Entity pharmacy) {
        return pharmacies.getOrderConnection(pharmacy);
    }

}
