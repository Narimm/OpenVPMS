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
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeSortConstraint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Iterator;

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
     * Returns the total quantity that may be dispensed on a prescription.
     *
     * @param prescription the prescription
     * @return the total quantity
     */
    public BigDecimal getTotalQuantity(Act prescription) {
        IMObjectBean bean = new IMObjectBean(prescription, service);
        BigDecimal quantity = bean.getBigDecimal("quantity", BigDecimal.ZERO);
        BigDecimal repeats = bean.getBigDecimal("repeats", BigDecimal.ZERO);
        return repeats.add(BigDecimal.ONE).multiply(quantity);
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
            BigDecimal remaining = getRemainingQuantity(prescription);
            if (remaining.compareTo(BigDecimal.ZERO) > 0) {
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
        BigDecimal dispensedQuantity = getDispensedQuantity(prescription);
        int result = 0;
        if (dispensedQuantity.compareTo(BigDecimal.ZERO) > 0) {
            result = dispensedQuantity.divide(getQuantity(prescription), 0, RoundingMode.CEILING).intValue();
        }
        return result;
    }

    /**
     * Returns the quantity dispensed on a prescription.
     *
     * @param prescription the prescription
     * @return the quantity remaining to be dispensed
     */
    public BigDecimal getDispensedQuantity(Act prescription) {
        BigDecimal result = BigDecimal.ZERO;
        ActBean bean = new ActBean(prescription, service);
        for (Act act : bean.getNodeActs("dispensing")) {
            IMObjectBean medication = new IMObjectBean(act, service);
            BigDecimal quantity = medication.getBigDecimal("quantity", BigDecimal.ZERO);
            result = result.add(quantity);
        }
        return result;
    }

    /**
     * Returns the quantity remaining to be dispensed on a prescription.
     *
     * @param prescription the prescription
     * @return the quantity remaining to be dispensed
     */
    public BigDecimal getRemainingQuantity(Act prescription) {
        BigDecimal total = getTotalQuantity(prescription);
        BigDecimal dispensed = getDispensedQuantity(prescription);
        return total.subtract(dispensed);
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
        return getPrescription(patient, product, new Date());
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
        Act result = null;
        ArchetypeQuery query = new ArchetypeQuery(PatientArchetypes.PRESCRIPTION);
        query.add(gte("endTime", DateRules.getDate(expiryDate)));
        query.add(join("patient").add(eq("entity", patient.getObjectReference())));
        query.add(join("product").add(eq("entity", product.getObjectReference())));
        query.add(new NodeSortConstraint("id"));
        Iterator<Act> iterator = new IMObjectQueryIterator<Act>(service, query);
        while (iterator.hasNext()) {
            Act act = iterator.next();
            if (getRemainingQuantity(act).compareTo(BigDecimal.ZERO) > 0) {
                result = act;
                break;
            }
        }
        return result;
    }

}
