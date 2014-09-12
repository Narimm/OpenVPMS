package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.segment.PV1;
import org.openvpms.hl7.PatientContext;

import java.util.Date;

/**
 * Populates a {@code PV1} segment.
 *
 * @author Tim Anderson
 */
class PV1Populator {

    public void populate(PV1 pv1, PatientContext context) throws HL7Exception {
        pv1.getSetIDPV1().setValue("1");
        pv1.getPatientClass().setValue("U"); // i.e. Unknown - see 3.4.3.2 PV1-2 Patient Class
        pv1.getAssignedPatientLocation().getFacility().getNamespaceID().setValue(context.getLocationName());

        if (context.getClinicianId() != -1) {
            PopulateHelper.populateClinicianSegment(pv1.getAdmittingDoctor(0), context);
        }
        pv1.getVisitNumber().getIDNumber().setValue(Long.toString(context.getVisitId()));
        pv1.getAdmitDateTime().getTime().setValue(context.getVisitStartTime());
        Date endTime = context.getVisitEndTime();
        if (endTime != null) {
            pv1.getDischargeDateTime(0).getTime().setValue(endTime);
        }
    }
}
