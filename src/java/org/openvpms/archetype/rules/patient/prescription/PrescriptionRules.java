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

package org.openvpms.archetype.rules.patient.prescription;

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeSortConstraint;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.gte;
import static org.openvpms.component.system.common.query.Constraints.join;

/**
 * Prescription rules.
 *
 * @author Tim Anderson
 */
public class PrescriptionRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs a {@link PrescriptionRules}.
     *
     * @param service the archetype service
     */
    public PrescriptionRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Cancels a prescription.
     * <p/>
     * This sets its expiry date to yesterday. <br/>
     * A prescription may only be cancelled if it can be dispensed.
     *
     * @param prescription the prescription to cancel
     * @return {@code true} if the prescription was cancelled, {@code false} if it has already expired, or has been
     *         fully dispensed
     */
    public boolean cancel(Act prescription) {
        boolean result = false;
        if (canDispense(prescription)) {
            prescription.setActivityEndTime(DateRules.getYesterday());
            service.save(prescription);
            result = true;
        }
        return result;
    }

    /**
     * Determines if a prescription can be dispensed.
     *
     * @param prescription the prescription
     * @return {@code true} if the prescription hasn't expired and has remaining quantity to dispense
     */
    public boolean canDispense(Act prescription) {
        boolean result = false;
        if (DateRules.compareDateToToday(prescription.getActivityEndTime()) >= 0) {
            if (getRemainingRepeats(prescription) > 0) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Returns the quantity to dispense.
     *
     * @param prescription the prescription
     * @return the quantity to dispense
     */
    public BigDecimal getQuantity(Act prescription) {
        IMObjectBean bean = new IMObjectBean(prescription, service);
        return bean.getBigDecimal("quantity", BigDecimal.ZERO);
    }

    /**
     * Returns the repeats.
     *
     * @param prescription the prescription
     * @return the number of repeats
     */
    public int getRepeats(Act prescription) {
        IMObjectBean bean = new IMObjectBean(prescription, service);
        return bean.getInt("repeats");
    }

    /**
     * Returns the number of times a prescription has been dispensed.
     *
     * @param prescription the prescription
     * @return the number of remaining repeats
     */
    public int getDispensed(Act prescription) {
        ActBean bean = new ActBean(prescription, service);
        return bean.getValues("dispensing").size();
    }

    /**
     * Returns the remaining repeats on the prescription.
     * <p/>
     * This is {@code 1 + repeats - times dispensed}
     *
     * @param prescription the prescription
     * @return the remaining repeats
     */
    public int getRemainingRepeats(Act prescription) {
        return 1 + getRepeats(prescription) - getDispensed(prescription);
    }

    /**
     * Returns the first prescription for a patient and product that may be dispensed.
     * Excludes all prescriptions that have an expiry date less than today.
     *
     * @param patient the patient
     * @param product the product
     * @return a prescription for the patient and product, or {@code null} if none is found
     */
    public Act getPrescription(Party patient, Product product) {
        return getPrescription(patient, product, Collections.<Act>emptyList());
    }

    /**
     * Returns the first prescription for a patient and product that may be dispensed.
     * Excludes all prescriptions that have an expiry date less than today.
     * <p/>
     * The {@code exclude} argument may be used to exclude prescriptions; use this if a prescription has been
     * dispensed but not saved, and should not be considered.
     *
     * @param patient the patient
     * @param product the product
     * @param exclude exclude the specified prescriptions
     * @return a prescription for the patient and product, or {@code null} if none is found
     */
    public Act getPrescription(Party patient, Product product, List<Act> exclude) {
        return getPrescription(patient, product, new Date(), exclude);
    }

    /**
     * Returns the first prescription for a patient and product that may be dispensed.
     *
     * @param patient    the patient
     * @param product    the product
     * @param expiryDate excludes prescriptions that have an expiry date {@code < expiryDate}
     * @return a prescription for the patient and product, or {@code null} if none is found
     */
    public Act getPrescription(Party patient, Product product, Date expiryDate) {
        return getPrescription(patient, product, expiryDate, Collections.<Act>emptyList());
    }

    /**
     * Returns the first prescription for a patient and product that may be dispensed.
     * <p/>
     * The {@code exclude} argument may be used to exclude prescriptions; use this if a prescription has been
     * dispensed but not saved, and should not be considered.
     *
     * @param patient    the patient
     * @param product    the product
     * @param expiryDate excludes prescriptions that have an expiry date {@code < expiryDate}
     * @param exclude    exclude the specified prescriptions
     * @return a prescription for the patient and product, or {@code null} if none is found
     */
    public Act getPrescription(Party patient, Product product, Date expiryDate, List<Act> exclude) {
        Act result = null;
        ArchetypeQuery query = new ArchetypeQuery(PatientArchetypes.PRESCRIPTION);
        query.add(gte("endTime", DateRules.getDate(expiryDate)));
        query.add(join("patient").add(eq("entity", patient)));
        query.add(join("product").add(eq("entity", product)));
        if (!exclude.isEmpty()) {
            for (Act act : exclude) {
                query.add(Constraints.ne("id", act.getId()));
            }
        }
        query.add(new NodeSortConstraint("id"));
        Iterator<Act> iterator = new IMObjectQueryIterator<Act>(service, query);
        while (iterator.hasNext()) {
            Act act = iterator.next();
            if (getRemainingRepeats(act) > 0) {
                result = act;
                break;
            }
        }
        return result;
    }

}
