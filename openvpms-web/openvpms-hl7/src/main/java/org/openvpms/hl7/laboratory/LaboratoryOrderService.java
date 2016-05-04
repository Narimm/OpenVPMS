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

package org.openvpms.hl7.laboratory;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.hl7.patient.PatientContext;

import java.util.Date;

/**
 * Laboratory order service.
 *
 * @author Tim Anderson
 */
public interface LaboratoryOrderService {

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
    boolean createOrder(PatientContext context, long placerOrderNumber, String serviceId, Date date, Entity laboratory,
                        User user);

    /**
     * Cancels an order.
     *  @param context           the patient context
     * @param placerOrderNumber the placer order number, to uniquely identify the order
     * @param serviceId         the universal service identifier
     * @param date              the order date
     * @param laboratory        the laboratory. An <em>entity.HL7ServiceLaboratory</em>
     * @param user              the user that generated the cancellation
     * @return {@code true} if a cancellation was sent
     */
    boolean cancelOrder(PatientContext context, long placerOrderNumber, String serviceId, Date date, Entity laboratory,
                        User user);
}