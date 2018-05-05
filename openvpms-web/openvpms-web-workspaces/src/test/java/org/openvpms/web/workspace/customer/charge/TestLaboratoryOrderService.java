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

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.hl7.laboratory.LaboratoryOrderService;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.pharmacy.PharmacyOrderService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Test implementation of the {@link PharmacyOrderService}.
 *
 * @author Tim Anderson
 */
public class TestLaboratoryOrderService implements LaboratoryOrderService {

    public static class LabOrder {

        enum Type {
            CREATE,
            CANCEL
        }

        private final Type type;

        private final Party patient;

        private final long placerOrderNumber;

        private final Date date;

        private final User clinician;

        private final Entity laboratory;

        public LabOrder(Type type, Party patient, long placerOrderNumber, Date date, User clinician, Entity laboratory) {
            this.type = type;
            this.patient = patient;
            this.placerOrderNumber = placerOrderNumber;
            this.date = date;
            this.clinician = clinician;
            this.laboratory = laboratory;
        }


        public Type getType() {
            return type;
        }

        public Party getPatient() {
            return patient;
        }

        public long getPlacerOrderNumber() {
            return placerOrderNumber;
        }

        public Date getDate() {
            return date;
        }

        public User getClinician() {
            return clinician;
        }

        public Entity getLaboratory() {
            return laboratory;
        }
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
        orders.add(new LabOrder(LabOrder.Type.CREATE, context.getPatient(), placerOrderNumber, date,
                                context.getClinician(), laboratory));
        return true;
    }

    private List<LabOrder> orders = new ArrayList<>();

    /**
     * Cancels an order.
     *  @param context           the patient context
     * @param placerOrderNumber the placer order number, to uniquely identify the order
     * @param serviceId         the universal service identifier
     * @param date              the order date
     * @param laboratory        the laboratory. An <em>entity.HL7ServiceLaboratory</em>
     * @param user              the user that generated the cancellation
     */
    @Override
    public boolean cancelOrder(PatientContext context, long placerOrderNumber, String serviceId, Date date,
                               Entity laboratory, User user) {
        orders.add(new LabOrder(LabOrder.Type.CANCEL, context.getPatient(), placerOrderNumber, date,
                                context.getClinician(), laboratory));
        return true;
    }

    /**
     * Returns the orders.
     *
     * @return the orders
     */
    public List<LabOrder> getOrders() {
        return orders;
    }

    public void clear() {
        orders.clear();
    }

}
