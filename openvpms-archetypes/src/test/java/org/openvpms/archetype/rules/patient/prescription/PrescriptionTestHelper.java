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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.patient.prescription;

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.util.Date;

/**
 * Prescription test helper methods.
 *
 * @author Tim Anderson
 */
public class PrescriptionTestHelper {

    /**
     * Creates a new prescription with a quantity of 1 and 5 repeats, expiring in 12 months.
     *
     * @param patient   the patient
     * @param product   the product to dispense
     * @param clinician the clinician
     */
    public static Act createPrescription(Party patient, Product product, User clinician) {
        return createPrescription(patient, product, clinician, 1, 5, DateRules.getDate(new Date(), 1, DateUnits.YEARS));
    }

    /**
     * Creates a new prescription.
     *
     * @param patient    the patient
     * @param product    the product to dispense
     * @param clinician  the clinician
     * @param quantity   the quantity
     * @param repeats    the no. of repeats
     * @param expiryDate the expiry date
     * @return a new prescription
     */
    public static Act createPrescription(Party patient, Product product, User clinician, int quantity, int repeats,
                                         Date expiryDate) {
        Act act = (Act) TestHelper.create(PatientArchetypes.PRESCRIPTION);
        ActBean bean = new ActBean(act);
        bean.setValue("quantity", quantity);
        bean.setValue("repeats", repeats);
        bean.setValue("endTime", expiryDate);
        bean.addNodeParticipation("patient", patient);
        bean.addNodeParticipation("product", product);
        bean.addNodeParticipation("clinician", clinician);
        bean.addNodeParticipation("author", clinician);
        bean.save();
        return act;
    }
}
