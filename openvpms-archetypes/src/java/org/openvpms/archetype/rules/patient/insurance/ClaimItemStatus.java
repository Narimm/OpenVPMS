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

import org.openvpms.archetype.rules.patient.ProblemActStatus;

/**
 * Status for <em>act.patientInsuranceClaimItem</em>.
 *
 * @author Tim Anderson
 */
public class ClaimItemStatus {

    /**
     * Resolved condition status.
     */
    public static final String RESOLVED = ProblemActStatus.RESOLVED;

    /**
     * Unresolved condition status.
     */
    public static final String UNRESOLVED = ProblemActStatus.UNRESOLVED;

    /**
     * Patient is deceased due to the conditon.
     */
    public static final String DECEASED = "DECEASED";

    /**
     * Patient was euthanased due to the conditon.
     */
    public static final String EUTHANASED = "EUTHANASED";
}
