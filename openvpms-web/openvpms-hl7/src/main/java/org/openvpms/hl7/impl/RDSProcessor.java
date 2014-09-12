package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.AcknowledgmentCode;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.message.RDS_O13;
import ca.uhn.hl7v2.model.v25.segment.PID;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.io.IOException;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class RDSProcessor {

    private final IArchetypeService service;

    public RDSProcessor(IArchetypeService service) {
        this.service = service;
    }

    public Message process(RDS_O13 rds) throws HL7Exception, IOException {
        PID pid = rds.getPATIENT().getPID();
        try {
            Party patient = getPatient(pid);
            return rds.generateACK();
        } catch (HL7Exception exception) {
            return rds.generateACK(AcknowledgmentCode.AR, exception);
        }
    }

    /**
     * Returns the patient associated with a PID segment.
     *
     * @param pid the pid
     * @return the corresponding patient
     * @throws HL7Exception if the patient does not exist
     */
    private Party getPatient(PID pid) throws HL7Exception {
        String value = pid.getPatientID().getIDNumber().getValue();
        long id;
        if (!StringUtils.isEmpty(value)) {
            try {
                id = Long.valueOf(value);
            } catch (NumberFormatException exception) {
                throw new HL7Exception(HL7Messages.invalidPatientIdentifier(value).toString());
            }
        } else {
            throw new HL7Exception(HL7Messages.invalidPatientIdentifier(value).toString());
        }
        IMObjectReference reference = new IMObjectReference(PatientArchetypes.PATIENT, id);
        Party patient = getPatient(reference);
        if (patient == null) {
            throw new HL7Exception(HL7Messages.invalidPatientIdentifier(value).toString());
        }
        return patient;
    }

    protected Party getPatient(IMObjectReference reference) {
        return (Party) service.get(reference);
    }
}
