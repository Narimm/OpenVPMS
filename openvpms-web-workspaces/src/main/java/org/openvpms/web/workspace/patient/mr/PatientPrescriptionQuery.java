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

package org.openvpms.web.workspace.patient.mr;

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.DateRangeActQuery;

import static org.openvpms.archetype.rules.patient.PatientArchetypes.PATIENT_PARTICIPATION;


/**
 * Queries <em>act.patientPrescription</em> for a patient.
 *
 * @author Tim Anderson
 */
public class PatientPrescriptionQuery extends DateRangeActQuery<Act> {

    /**
     * Patient prescription short names.
     */
    private static final String[] SHORT_NAMES = {PatientArchetypes.PRESCRIPTION};

    /**
     * The default sort constraint.
     */
    private static final SortConstraint[] DEFAULT_SORT
            = new SortConstraint[]{new NodeSortConstraint("startTime", false)};


    /**
     * Constructs a {@link PatientPrescriptionQuery}.
     *
     * @param patient the patient
     */
    public PatientPrescriptionQuery(Party patient) {
        super(patient, "patient", PATIENT_PARTICIPATION, SHORT_NAMES, Act.class);
        setAuto(true);
        setDefaultSortConstraint(DEFAULT_SORT);
    }
}
