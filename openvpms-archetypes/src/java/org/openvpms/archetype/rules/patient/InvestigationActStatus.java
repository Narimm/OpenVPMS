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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.patient;

/**
 * Order status types for investigation acts.
 *
 * @author Tim Anderson
 */
public class InvestigationActStatus {

    /**
     * Order pending status.
     */
    public static final String PENDING = "PENDING";

    /**
     * Order sent status.
     */
    public static final String SENT = "SENT";

    /**
     * Results received status.
     */
    public static final String RECEIVED = "RECEIVED";

    /**
     * Results reviewed status.
     */
    public static final String REVIEWED = "REVIEWED";

}
