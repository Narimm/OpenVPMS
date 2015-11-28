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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment.reminder;

import org.openvpms.component.system.common.exception.OpenVPMSException;

/**
 * An exception for appointment reminders.
 *
 * @author Tim Anderson
 */
public class AppointmentReminderException extends OpenVPMSException {

    /**
     * Constructs an {@link AppointmentReminderException}.
     *
     * @param message the error message
     * @param cause   the cause of the error
     */
    public AppointmentReminderException(String message, Throwable cause) {
        super(message, cause);
    }
}
