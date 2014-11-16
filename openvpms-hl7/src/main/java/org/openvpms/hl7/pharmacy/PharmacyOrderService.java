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
import org.openvpms.component.business.domain.im.security.User;
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
     * @param pharmacy          the pharmacy. An <em>entity.HL7ServicePharmacy</em>
     * @param user              the user that generated the order
     * @return {@code true} if the order was placed
     */
    boolean createOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                        Date date, Entity pharmacy, User user);

    /**
     * Updates an order.
     *
     * @param context           the patient context
     * @param product           the product to order
     * @param quantity          the quantity to order
     * @param placerOrderNumber the placer order number, to uniquely identify the order
     * @param date              the order date
     * @param pharmacy          the pharmacy. An <em>entity.HL7ServicePharmacy</em>
     * @param user              the user that generated the update
     */
    void updateOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                     Date date, Entity pharmacy, User user);

    /**
     * Cancels an order.
     *
     * @param context           the patient context
     * @param product           the product to order
     * @param quantity          the quantity to order
     * @param placerOrderNumber the placer order number, to uniquely identify the order
     * @param date              the order date
     * @param pharmacy          the pharmacy. An <em>entity.HL7ServicePharmacy</em>
     * @param user              the user that generated the cancellation
     */
    void cancelOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                     Date date, Entity pharmacy, User user);

    /**
     * Discontinues an order.
     *
     * @param context           the patient context
     * @param product           the product to order
     * @param quantity          the quantity to order
     * @param placerOrderNumber the placer order number, to uniquely identify the order
     * @param date              the order date
     * @param pharmacy          the pharmacy. An <em>entity.HL7ServicePharmacy</em>
     * @param user              the user that generated the discontinue request
     */
    void discontinueOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                          Date date, Entity pharmacy, User user);
}