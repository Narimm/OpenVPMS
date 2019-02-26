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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.insurance;

import org.openvpms.archetype.rules.workflow.WorkflowStatus;

/**
 * Insurance claim status.
 *
 * @author Tim Anderson
 */
public class ClaimStatus {

    /**
     * Claim is pending. User can make changes
     */
    public static final String PENDING = WorkflowStatus.PENDING;

    /**
     * Claim is finalised. No further changes may be made prior to submission.
     */
    public static final String POSTED = WorkflowStatus.POSTED;

    /**
     * Claim has been submitted to the insurer.
     */
    public static final String SUBMITTED = "SUBMITTED";

    /**
     * Claim has been accepted, and is being processed.
     */
    public static final String ACCEPTED = "ACCEPTED";

    /**
     * Claim has been settled by the insurer.
     */
    public static final String SETTLED = "SETTLED";

    /**
     * Claim has been declined by the insurer.
     */
    public static final String DECLINED = "DECLINED";

    /**
     * Claim is in the process of being cancelled.
     */
    public static final String CANCELLING = "CANCELLING";

    /**
     * Claim has been cancelled.
     */
    public static final String CANCELLED = WorkflowStatus.CANCELLED;

    /**
     * Gap claim benefit pending.
     */
    public static final String GAP_CLAIM_PENDING = PENDING;

    /**
     * Gap claim benefit amount has been received from the insurer.
     */
    public static final String GAP_CLAIM_RECEIVED = "RECEIVED";

    /**
     * Gap claim paid.
     */
    public static final String GAP_CLAIM_PAID = "PAID";

    /**
     * Insurer has been notified of gap claim payment.
     */
    public static final String GAP_CLAIM_NOTIFIED = "NOTIFIED";
}
