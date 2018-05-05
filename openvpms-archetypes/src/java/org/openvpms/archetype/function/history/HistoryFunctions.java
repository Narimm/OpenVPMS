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

package org.openvpms.archetype.function.history;

import org.apache.commons.jxpath.Function;
import org.openvpms.archetype.rules.patient.PatientHistory;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.jxpath.AbstractObjectFunctions;

import java.util.Collections;
import java.util.Date;

/**
 * Patient history functions, for use in reporting.
 * <p/>
 * This extends {@link AbstractObjectFunctions} in order to translate the "medication" function to the appropriate
 * implementation to avoid ambiguous method calls.
 *
 * @author Tim Anderson
 */
public class HistoryFunctions extends AbstractObjectFunctions {

    /**
     * The archetype service.
     */
    private final PatientHistory history;

    /**
     * Constructs a {@link HistoryFunctions}.
     *
     * @param service the archetype service
     */
    public HistoryFunctions(IArchetypeService service) {
        super("history");
        setObject(this);
        history = new PatientHistory(service);
    }

    /**
     * Returns a Function, if any, for the specified namespace name and parameter types.
     * <p/>
     * This implementation changes:
     * <ul>
     * <li>medication to {@link #medicationByProductType} if the second argument is a string.</li>
     * <li>charges to {@link #chargesByProductType} if the second argument is a string.</li>
     * </ul>
     * This is required as JXPath can't resolve which method to call if arguments are null.
     *
     * @param namespace if it is not the namespace specified in the constructor, the method returns null
     * @param name      is a function name.
     * @return a MethodFunction, or {@code null} if there is no such function.
     */
    @Override
    public Function getFunction(String namespace, String name, Object[] parameters) {
        if ("medication".equals(name) && parameters != null && parameters.length >= 2
            && parameters[1] instanceof String) {
            name = "medicationByProductType";
        } else if ("charges".equals(name) && parameters != null && parameters.length >= 2
                   && parameters[1] instanceof String) {
            name = "chargesByProductType";
        }
        return super.getFunction(namespace, name, parameters);
    }

    /**
     * Returns all invoice item acts for a patient.
     *
     * @param patient the patient. May be {@code null}
     * @return the invoice items acts for the patient, or an empty list if patient is {@code null}
     */
    public Iterable<Act> charges(Party patient) {
        return patient != null ? history.getCharges(patient) : Collections.<Act>emptyList();
    }

    /**
     * Returns invoice item acts for a patient, on the specified date.
     *
     * @param patient the patient. May be {@code null}
     * @param date    the date. May be {@code null}
     * @return the medication acts for the patient, or an empty list if an argument is {@code null}
     */
    public Iterable<Act> charges(Party patient, Date date) {
        if (patient != null && date != null) {
            Date from = DateRules.getDate(date);
            Date to = DateRules.getDate(from, 1, DateUnits.DAYS);
            return history.getCharges(patient, from, to);
        }
        return Collections.emptyList();
    }

    /**
     * Returns invoice item acts for a patient, between the specified dates.
     *
     * @param patient the patient. May be {@code null}
     * @param from    the start of the date range, inclusive. May be {@code null}
     * @param to      the end of the date range, exclusive. May be {@code null}
     * @return the invoice item acts for the patient, or an empty list if patient is {@code null}
     */
    public Iterable<Act> charges(Party patient, Date from, Date to) {
        return patient != null ? history.getCharges(patient, from, to) : Collections.<Act>emptyList();
    }

    /**
     * Returns invoice item acts for a patient, with the specified product type name.
     *
     * @param patient         the patient. May be {@code null}
     * @param productTypeName the product type name. May be {@code null}, or contain wildcards
     * @return the invoice item acts for the patient, or an empty list if patient is {@code null}
     */
    public Iterable<Act> chargesByProductType(Party patient, String productTypeName) {
        return patient != null ? history.getCharges(patient, productTypeName) : Collections.<Act>emptyList();
    }

    /**
     * Returns invoice item acts for a patient, with the specified product type name on the specified date.
     *
     * @param patient         the patient. May be {@code null}
     * @param productTypeName the product type name. May be {@code null}, or contain wildcards
     * @param date            the date
     * @return the invoice item acts for the patient, or an empty list if patient is {@code null}
     */
    public Iterable<Act> chargesByProductType(Party patient, String productTypeName, Date date) {
        if (patient != null && date != null) {
            Date from = DateRules.getDate(date);
            Date to = DateRules.getDate(from, 1, DateUnits.DAYS);
            return history.getCharges(patient, productTypeName, from, to);
        }
        return Collections.emptyList();
    }

    /**
     * Returns invoice item acts for a patient, with the specified product type name, between the specified dates.
     *
     * @param patient         the patient. May be {@code null}
     * @param productTypeName the product type name. May be {@code null}, or contain wildcards
     * @param from            the start of the date range, inclusive. May be {@code null}
     * @param to              the end of the date range, exclusive. May be {@code null}
     * @return the medication acts for the patient, or an empty list if patient is {@code null}
     */
    public Iterable<Act> chargesByProductType(Party patient, String productTypeName, Date from, Date to) {
        return patient != null ? history.getCharges(patient, productTypeName, from, to)
                               : Collections.<Act>emptyList();
    }

    /**
     * Returns all medication acts for a patient.
     *
     * @param patient the patient. May be {@code null}
     * @return the medication acts for the patient, or an empty list if patient is {@code null}
     */
    public Iterable<Act> medication(Party patient) {
        return patient != null ? history.getMedication(patient) : Collections.<Act>emptyList();
    }

    /**
     * Returns medication acts for a patient, on the specified date.
     *
     * @param patient the patient. May be {@code null}
     * @param date    the date. May be {@code null}
     * @return the medication acts for the patient, or an empty list if an argument is {@code null}
     */
    public Iterable<Act> medication(Party patient, Date date) {
        if (patient != null && date != null) {
            Date from = DateRules.getDate(date);
            Date to = DateRules.getDate(from, 1, DateUnits.DAYS);
            return history.getMedication(patient, from, to);
        }
        return Collections.emptyList();
    }

    /**
     * Returns medication acts for a patient, between the specified dates.
     *
     * @param patient the patient. May be {@code null}
     * @param from    the start of the date range, inclusive. May be {@code null}
     * @param to      the end of the date range, exclusive. May be {@code null}
     * @return the medication acts for the patient, or an empty list if patient is {@code null}
     */
    public Iterable<Act> medication(Party patient, Date from, Date to) {
        return patient != null ? history.getMedication(patient, from, to) : Collections.<Act>emptyList();
    }

    /**
     * Returns medication acts for a patient, with the specified product type name.
     *
     * @param patient         the patient. May be {@code null}
     * @param productTypeName the product type name. May be {@code null}, or contain wildcards
     * @return the medication acts for the patient, or an empty list if patient is {@code null}
     */
    public Iterable<Act> medicationByProductType(Party patient, String productTypeName) {
        return patient != null ? history.getMedication(patient, productTypeName) : Collections.<Act>emptyList();
    }

    /**
     * Returns medication acts for a patient, with the specified product type name on the specified date.
     *
     * @param patient         the patient. May be {@code null}
     * @param productTypeName the product type name. May be {@code null}, or contain wildcards
     * @param date            the date
     * @return the medication acts for the patient, or an empty list if patient is {@code null}
     */
    public Iterable<Act> medicationByProductType(Party patient, String productTypeName, Date date) {
        if (patient != null && date != null) {
            Date from = DateRules.getDate(date);
            Date to = DateRules.getDate(from, 1, DateUnits.DAYS);
            return history.getMedication(patient, productTypeName, from, to);
        }
        return Collections.emptyList();
    }

    /**
     * Returns medication acts for a patient, with the specified product type name, between the specified dates.
     *
     * @param patient         the patient. May be {@code null}
     * @param productTypeName the product type name. May be {@code null}, or contain wildcards
     * @param from            the start of the date range, inclusive. May be {@code null}
     * @param to              the end of the date range, exclusive. May be {@code null}
     * @return the medication acts for the patient, or an empty list if patient is {@code null}
     */
    public Iterable<Act> medicationByProductType(Party patient, String productTypeName, Date from, Date to) {
        return patient != null ? history.getMedication(patient, productTypeName, from, to)
                               : Collections.<Act>emptyList();
    }
}
