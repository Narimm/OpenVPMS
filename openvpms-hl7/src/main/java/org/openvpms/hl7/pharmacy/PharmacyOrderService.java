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

package org.openvpms.hl7.pharmacy;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.hl7.patient.PatientContext;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Pharmacy Order service.
 *
 * @author Tim Anderson
 */
public interface PharmacyOrderService {

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
    void createOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                     Date date, Entity pharmacy);

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
    void updateOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                     Date date, Entity pharmacy);

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
    void cancelOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                     Date date, Entity pharmacy);

}