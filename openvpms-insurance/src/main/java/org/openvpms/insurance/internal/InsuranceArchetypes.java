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

package org.openvpms.insurance.internal;

/**
 * Insurance archetype.
 *
 * @author Tim Anderson
 */
public class InsuranceArchetypes {

    /**
     * The insurance policy archetype.
     */
    public static final String POLICY = "act.patientInsurancePolicy";

    /**
     * The insurance policy type archetype.
     */
    public static final String POLICY_TYPE = "entity.insurancePolicyType";

    /**
     * The insurance claim archetype.
     */
    public static final String CLAIM = "act.patientInsuranceClaim";

    /**
     * The insurance claim item archetype.
     */
    public static final String CLAIM_ITEM = "act.patientInsuranceClaimItem";

}
