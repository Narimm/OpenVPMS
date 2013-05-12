/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.patient.history;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

/**
 * A factory for {@link PatientHistoryQuery} instances.
 *
 * @author Tim Anderson
 */
public class PatientHistoryQueryFactory {

    /**
     * Creates a new {@link PatientHistoryQuery}.
     *
     * @param patient  the patient to query history for
     * @param practice the practice, used to determine the default sort order. May be {@code null}
     * @return a new history query for the patient
     */
    public static PatientHistoryQuery create(Party patient, Party practice) {
        PatientHistoryQuery query = new PatientHistoryQuery(patient);
        boolean sortAscending = false;
        if (practice != null) {
            IMObjectBean bean = new IMObjectBean(practice);
            String medicalRecordsSortOrder = bean.getString("medicalRecordsSortOrder");
            sortAscending = "ASC".equals(medicalRecordsSortOrder);
        }
        query.setSortAscending(sortAscending);
        return query;
    }
}
