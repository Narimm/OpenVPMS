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

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.insurance.internal.InsuranceArchetypes;

import java.util.Date;

/**
 * Helper for insurance tests.
 *
 * @author Tim Anderson
 */
public class InsuranceTestHelper {

    /**
     * Creates a new policy starting today, and expiring in 12 months.
     *
     * @param customer  the customer
     * @param patient   the patient
     * @param insurer   the insurer
     * @param type      the policy type
     * @param clinician the clinician
     * @return a new policy
     */
    public static Act createPolicy(Party customer, Party patient, Party insurer, Party type, User clinician) {
        Act policy = (Act) TestHelper.create(InsuranceArchetypes.POLICY);
        ActBean bean = new ActBean(policy);
        Date from = new Date();
        Date to = DateRules.getDate(from, 1, DateUnits.YEARS);
        policy.setActivityStartTime(from);
        policy.setActivityEndTime(to);
        bean.setNodeParticipant("customer", customer);
        bean.setNodeParticipant("patient", patient);
        bean.setNodeParticipant("insurer", insurer);
        bean.setNodeParticipant("type", type);
        bean.setNodeParticipant("clinician", clinician);
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
        Act claim = (Act) TestHelper.create(InsuranceArchetypes.CLAIM);
        ActBean bean = new ActBean(claim);
        ActBean policyBean = new ActBean(policy);
        bean.setNodeParticipant("patient", policyBean.getNodeParticipantRef("patient"));
        bean.setNodeParticipant("clinician", clinician);
        return claim;
    }
}
