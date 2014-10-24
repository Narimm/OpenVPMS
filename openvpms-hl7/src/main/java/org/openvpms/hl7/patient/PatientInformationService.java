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

package org.openvpms.hl7.patient;

import org.openvpms.component.business.domain.im.security.User;

/**
 * Notifies registered services of patient events.
 *
 * @author Tim Anderson
 */
public interface PatientInformationService {

    /**
     * Notifies that a patient has been admitted.
     *
     * @param context the patient context
     * @param user    the user that triggered the notification
     */
    void admitted(PatientContext context, User user);

    /**
     * Notifies that an admission has been cancelled.
     *
     * @param context the patient context
     * @param user    the user that triggered the notification
     */
    void admissionCancelled(PatientContext context, User user);

    /**
     * Notifies that a patient has been discharged.
     *
     * @param context the patient context
     * @param user    the user that triggered the notification
     */
    void discharged(PatientContext context, User user);

    /**
     * Notifies that a patient has been updated.
     *
     * @param context the patient context
     * @param user    the user that triggered the notification
     */
    void updated(PatientContext context, User user);

}