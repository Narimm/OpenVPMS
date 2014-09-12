package org.openvpms.hl7;

/**
 * Notifies of patient admission and discharge events.
 *
 * @author Tim Anderson
 */
public interface AdmissionService {

    /**
     * Notifies that a patient has been admitted.
     *
     * @param context the patient context
     */
    void admitted(PatientContext context);

    /**
     * Notifies that an admission has been cancelled.
     *
     * @param context the patient context
     */
    void admissionCancelled(PatientContext context);

    /**
     * Notifies that a patient has been discharged.
     *
     * @param context the patient context
     */
    void discharged(PatientContext context);

    /**
     * Notifies that a patient has been updated.
     *
     * @param context the patient context
     */
    void updated(PatientContext context);

}