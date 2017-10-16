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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.insurance;

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.insurance.InsuranceArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.im.query.DateRangeActQuery;

/**
 * Queries <em>act.patientInsurancePolicy</em> and <em>act.patientInsuranceClaim</em> acts for a patient.
 *
 * @author Tim Anderson
 */
public class InsuranceQuery extends DateRangeActQuery<Act> {

    /**
     * The short names of the archetypes that this queries.
     */
    public static final String[] ARCHETYPES = {InsuranceArchetypes.POLICY, InsuranceArchetypes.CLAIM};

    /**
     * Constructs a {@link InsuranceQuery}.
     *
     * @param patient the patient to search for
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public InsuranceQuery(Party patient) {
        super(patient, "patient", PatientArchetypes.PATIENT_PARTICIPATION, ARCHETYPES, Act.class);
    }
}
