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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.patient.mr;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DateRangeActQuery;


/**
 * Queries <em>act.patientDocumentForm</em>, <em>act.patientDocumentLetter</em>, <em>act.patientDocumentAttachment</em>
 * <em>act.patientDocumentImage</em> and <em>act.patientInvestigation</em> acts for a patient.
 *
 * @author Tim Anderson
 */
public class PatientDocumentQuery<T extends Act> extends DateRangeActQuery<T> {

    /**
     * Patient document shortnames.
     */
    public static final String[] DOCUMENT_SHORT_NAMES = {
            "act.patientDocumentForm",
            "act.patientDocumentLetter",
            "act.patientDocumentAttachment",
            "act.patientDocumentImage",
            "act.patientInvestigation"
    };

    /**
     * The document statuses to query.
     */
    private static final ActStatuses DOC_STATUSES;

    static {
        DOC_STATUSES = new ActStatuses("act.patientDocumentLetter");
        DOC_STATUSES.setDefault((Lookup) null);
    }

    /**
     * Constructs a <tt>PatientDocumentQuery</tt>.
     *
     * @param patient the patient
     */
    public PatientDocumentQuery(Party patient) {
        super(patient, "patient", "participation.patient", DOCUMENT_SHORT_NAMES, DOC_STATUSES, Act.class);
        setAuto(true);
    }
}
