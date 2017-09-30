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

package org.openvpms.insurance;

import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.insurance.internal.InsuranceArchetypes;

import java.util.Date;

import static org.openvpms.archetype.test.TestHelper.create;
import static org.openvpms.archetype.test.TestHelper.randomName;
import static org.openvpms.archetype.test.TestHelper.save;

/**
 * Helper for insurance tests.
 *
 * @author Tim Anderson
 */
public class InsuranceTestHelper {

    /**
     * Creates and saves a new insurer.
     *
     * @param name the insurer name
     * @return a new insurer
     */
    public static Party createInsurer(String name) {
        Party result = (Party) create(SupplierArchetypes.INSURER);
        result.setName(name);
        save(result);
        return result;
    }

    /**
     * Creates and saves a new policy type.
     *
     * @param insurer the insurer that issues the policy
     * @return the policy type
     */
    public static Entity createPolicyType(Party insurer) {
        Entity result = (Entity) create(InsuranceArchetypes.POLICY_TYPE);
        result.setName(randomName("ZPolicyType-"));
        IMObjectBean bean = new IMObjectBean(result);
        bean.addNodeTarget("insurer", insurer);
        bean.save();
        return result;
    }

    /**
     * Creates a new policy starting today, and expiring in 12 months.
     *
     * @param customer the customer
     * @param patient  the patient
     * @param insurer  the insurer
     * @param type     the policy type
     * @return a new policy
     */
    public static Act createPolicy(Party customer, Party patient, Party insurer, Entity type) {
        Act policy = (Act) create(InsuranceArchetypes.POLICY);
        ActBean bean = new ActBean(policy);
        Date from = new Date();
        Date to = DateRules.getDate(from, 1, DateUnits.YEARS);
        policy.setActivityStartTime(from);
        policy.setActivityEndTime(to);
        bean.setNodeParticipant("customer", customer);
        bean.setNodeParticipant("patient", patient);
        bean.setNodeParticipant("insurer", insurer);
        bean.setNodeParticipant("type", type);
        return policy;
    }

    /**
     * Creates a claim for a policy.
     *
     * @param policy    the policy
     * @param clinician the clinician
     * @return a new claim
     */
    public static Act createClaim(Act policy, User clinician) {
        Act claim = (Act) create(InsuranceArchetypes.CLAIM);
        ActBean bean = new ActBean(claim);
        ActBean policyBean = new ActBean(policy);
        bean.setNodeParticipant("patient", policyBean.getNodeParticipantRef("patient"));
        bean.setNodeParticipant("author", clinician);
        bean.setNodeParticipant("clinician", clinician);
        bean.addNodeRelationship("policy", policy);
        return claim;
    }
}
