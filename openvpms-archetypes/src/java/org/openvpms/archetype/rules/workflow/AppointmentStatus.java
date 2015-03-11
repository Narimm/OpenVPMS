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

package org.openvpms.archetype.rules.workflow;


/**
 * Act status types for <em>act.customerAppointment</em> acts.
 *
 * @author Tim Anderson
 */
public class AppointmentStatus extends WorkflowStatus {

    /**
     * Admitted status.
     */
    public static final String ADMITTED = "ADMITTED";

    /**
     * Checked-in status.
     */
    public static final String CHECKED_IN = "CHECKED_IN";

}
