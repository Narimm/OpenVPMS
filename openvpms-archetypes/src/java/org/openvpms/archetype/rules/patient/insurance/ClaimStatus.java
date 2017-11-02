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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.patient.insurance;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.workflow.WorkflowStatus;

/**
 * Insurance claim statuses.
 *
 * @author Tim Anderson
 */
public class ClaimStatus {

    /**
     * Pending claim.
     */
    public static final String PENDING = WorkflowStatus.PENDING;

    /**
     * Finalised claim.
     */
    public static final String POSTED = ActStatus.POSTED;

    /**
     * Submitted claim.
     */
    public static final String SUBMITTED = "SUBMITTED";

    /**
     * Accepted claim. This indicates the insurance service has accepted the claim for processing.
     */
    public static final String ACCEPTED = "ACCEPTED";

    /**
     * Settled claim.
     */
    public static final String SETTLED = "SETTLED";

    /**
     * Cancelled claim.
     */
    public static final String CANCELLED = ActStatus.CANCELLED;

    /**
     * Declined claim.
     */
    public static final String DECLINED = "DECLINED";

}
