package org.openvpms.smartflow.i18n;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.i18n.Message;
import org.openvpms.component.system.common.i18n.Messages;

/**
 * Messages reported by the Smart Flow Sheet interface.
 *
 * @author Tim Anderson
 */
public class FlowSheetMessages {

    /**
     * The messages.
     */
    private static Messages messages = new Messages("SFS", FlowSheetMessages.class.getName());

    /**
     * Creates a message indicating that a hospitalization couuldn't be retrieved for a patient.
     *
     * @param patient the patient
     * @return a new message
     */
    public static Message failedToGetHospitalization(Party patient) {
        return messages.getMessage(100, patient.getName());

    }

    /**
     * Creates a message indicating that a flow sheet couldn't be created for a patient.
     *
     * @param patient the patient
     * @return a new message
     */
    public static Message failedToCreateFlowSheet(Party patient) {
        return messages.getMessage(101, patient.getName());
    }

    /**
     * Creates a message indicating that a PDF couldn't be downloaded for a patient.
     *
     * @param patient the patient
     * @param name    the pdf name
     * @return a new message
     */
    public static Message failedToDownloadPDF(Party patient, String name) {
        return messages.getMessage(102, patient.getName(), name);
    }

    /**
     * Creates a message indicating that there is
     *
     * @param patient
     * @return
     */
    public static Message noAuthToCreateFlowSheet(Party patient) {
        return messages.getMessage(103, patient.getName());
    }
}
