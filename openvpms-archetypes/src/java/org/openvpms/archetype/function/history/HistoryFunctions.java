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

import java.util.Date;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class HistoryFunctions {

    /**
     * The archetype service.
     */
    private final PatientHistory history;

    public HistoryFunctions(IArchetypeService service) {
        history = new PatientHistory(service);
    }

    public Iterable<Act> medication(Party patient) {
        return history.getMedication(patient);
    }

    public Iterable<Act> medication(Party patient, Date from, Date to) {
        return history.getMedication(patient, from, to);
    }

    public Iterable<Act> medication(Party patient, String productTypeName) {
        return history.getMedication(patient, productTypeName);
    }

    public Iterable<Act> medication(Party patient, String productTypeName, Date from, Date to) {
        return history.getMedication(patient, productTypeName, from, to);
    }
}
