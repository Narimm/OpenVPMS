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
     * The insurance claim archetype.
     */
    public static final String CLAIM = "act.patientInsuranceClaim";

    /**
     * The default insurance claim identity, used when claims are submitted manually.
     */
    public static final String CLAIM_IDENTITY = "actIdentity.insuranceClaim";

    /**
     * Insurance claim identity archetypes.
     */
    public static final String CLAIM_IDENTITIES = "actIdentity.insuranceClaim*";

    /**
     * The insurance claim item archetype.
     */
    public static final String CLAIM_ITEM = "act.patientInsuranceClaimItem";

    /**
     * The insurance claim attachment archetype.
     */
    public static final String ATTACHMENT = "act.patientInsuranceClaimAttachment";

    /**
     * The insurance claim invoice item relationship archetype.
     */
    public static final String CLAIM_INVOICE_ITEM = "actRelationship.insuranceClaimInvoiceItem";

    /**
     * The insurance services archetypes.
     */
    public static String INSURANCE_SERVICES = "entity.insuranceService*";
}
