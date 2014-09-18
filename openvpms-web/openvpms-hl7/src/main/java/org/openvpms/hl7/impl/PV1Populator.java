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

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.segment.PV1;
import org.openvpms.hl7.PatientContext;

import java.util.Date;

import static org.openvpms.hl7.impl.PopulateHelper.populateDTM;

/**
 * Populates a {@code PV1} segment.
 *
 * @author Tim Anderson
 */
class PV1Populator {

    /**
     * Populates a PV1 segment.
     *
     * @param pv1     the segment
     * @param context the patient context
     * @param config  the message population configuration
     * @throws HL7Exception for any error
     */
    public void populate(PV1 pv1, PatientContext context, MessageConfig config) throws HL7Exception {
        pv1.getSetIDPV1().setValue("1");
        pv1.getPatientClass().setValue("U"); // i.e. Unknown - see 3.4.3.2 PV1-2 Patient Class
        pv1.getAssignedPatientLocation().getFacility().getNamespaceID().setValue(context.getLocationName());

        if (context.getClinicianId() != -1) {
            PopulateHelper.populateClinician(pv1.getAdmittingDoctor(0), context);
        }
        pv1.getVisitNumber().getIDNumber().setValue(Long.toString(context.getVisitId()));
        populateDTM(pv1.getAdmitDateTime().getTime(), context.getVisitStartTime(), config);
        Date endTime = context.getVisitEndTime();
        if (endTime != null) {
            populateDTM(pv1.getDischargeDateTime(0).getTime(), endTime, config);
        }
    }
}
