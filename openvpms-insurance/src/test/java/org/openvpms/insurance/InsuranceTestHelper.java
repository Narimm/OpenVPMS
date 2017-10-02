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
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActIdentity;
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
     * @param identity the policy identity. May be {@code null}
     * @return a new policy
     */
    public static Act createPolicy(Party customer, Party patient, Party insurer, Entity type, ActIdentity identity) {
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
        if (identity != null) {
            policy.addIdentity(identity);
        }
        return policy;
    }

    /**
     * Creates a claim for a policy.
     *
     * @param policy    the policy
     * @param clinician the clinician
     * @param items     the claim items. A list of <em>act.patientInsuranceClaimItem</em>
     * @return a new claim
     */
    public static Act createClaim(Act policy, User clinician, Act... items) {
        Act claim = (Act) create(InsuranceArchetypes.CLAIM);
        ActBean bean = new ActBean(claim);
        ActBean policyBean = new ActBean(policy);
        bean.setNodeParticipant("patient", policyBean.getNodeParticipantRef("patient"));
        bean.setNodeParticipant("author", clinician);
        bean.setNodeParticipant("clinician", clinician);
        bean.addNodeRelationship("policy", policy);
        for (Act item : items) {
            bean.addNodeRelationship("items", item);
        }
        return claim;
    }

    /**
     * Creates a new claim item.
     *
     * @param diagnosis    the VeNom diagnosis code
     * @param startTime    the treatment start time
     * @param endTime      the treatment end time
     * @param author       the author of the claim item
     * @param invoiceItems the invoice items being claimed
     * @return a new claim item
     */
    public static Act createClaimItem(String diagnosis, Date startTime, Date endTime, User author,
                                      Act... invoiceItems) {
        Act item = (Act) create(InsuranceArchetypes.CLAIM_ITEM);
        ActBean bean = new ActBean(item);
        item.setReason(TestHelper.getLookup("lookup.diagnosisVeNom", diagnosis).getCode());
        item.setActivityStartTime(startTime);
        item.setActivityEndTime(endTime);
        bean.setNodeParticipant("author", author);
        for (Act invoiceItem : invoiceItems) {
            bean.addNodeRelationship("items", invoiceItem);
        }
        return item;
    }
}
