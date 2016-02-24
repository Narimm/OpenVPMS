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

package org.openvpms.web.workspace.patient.communication;

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.im.query.DateRangeActQuery;
import org.openvpms.web.workspace.customer.communication.CommunicationArchetypes;

/**
 * Queries <em>act.customerCommunication*</em> acts for a patient.
 *
 * @author Tim Anderson
 */
public class PatientCommunicationQuery extends DateRangeActQuery<Act> {

    /**
     * The short names of the archetypes that this queries.
     */
    private static final String[] SHORT_NAMES = {CommunicationArchetypes.ACTS};

    /**
     * Constructs a {@link PatientCommunicationQuery}.
     *
     * @param patient the patient to search for
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public PatientCommunicationQuery(Party patient) {
        super(patient, "patient", PatientArchetypes.PATIENT_PARTICIPATION, SHORT_NAMES, Act.class);
    }
}
