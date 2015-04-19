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

package org.openvpms.archetype.function.history;

import org.openvpms.archetype.rules.patient.PatientHistory;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.Collections;
import java.util.Date;

/**
 * Patient history functions, for use in reporting.
 *
 * @author Tim Anderson
 */
public class HistoryFunctions {

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
        history = new PatientHistory(service);
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
     * Returns medication acts for a patient, between the specified dates, inclusive.
     *
     * @param patient the patient. May be {@code null}
     * @param from    the from date-time. May be {@code null}
     * @param to      the to date-time. May be {@code null}
     * @return the medication acts for the patient, or an empty list if patient is {@code null}
     */
    public Iterable<Act> medication(Party patient, Date from, Date to) {
        return patient != null ? history.getMedication(patient, from, to) : Collections.<Act>emptyList();
    }

    /**
     * Returns medication acts for a patient, with the specified product type name.
     *
     * @param patient         the patient. May be {@code null}
     * @param productTypeName the product type name. May be {@code null}
     * @return the medication acts for the patient, or an empty list if patient is {@code null}
     */
    public Iterable<Act> medication(Party patient, String productTypeName) {
        return patient != null ? history.getMedication(patient, productTypeName) : Collections.<Act>emptyList();
    }

    /**
     * Returns medication acts for a patient, with the specified product type name, between the specified dates,
     * inclusive.
     *
     * @param patient         the patient. May be {@code null}
     * @param productTypeName the product type name. May be {@code null}
     * @param from            the from date-time. May be {@code null}
     * @param to              the to date-time. May be {@code null}
     * @return the medication acts for the patient, or an empty list if patient is {@code null}
     */
    public Iterable<Act> medication(Party patient, String productTypeName, Date from, Date to) {
        return patient != null ? history.getMedication(patient, productTypeName, from, to)
                               : Collections.<Act>emptyList();
    }
}
