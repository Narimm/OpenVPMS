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

import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.act.ActIdentity;
import org.openvpms.component.model.act.ActRelationship;
import org.openvpms.component.model.act.DocumentAct;
import org.openvpms.component.model.act.FinancialAct;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.object.Relationship;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.model.user.User;

import java.math.BigDecimal;
import java.util.Date;

import static org.openvpms.archetype.test.TestHelper.create;
import static org.openvpms.archetype.test.TestHelper.createActIdentity;
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
     * @return a new insurer
     */
    public static Party createInsurer() {
        return createInsurer(TestHelper.randomName("ZInsurer-"));
    }

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
     * Creates a new policy.
     *
     * @param customer     the customer
     * @param patient      the patient
     * @param insurer      the insurer
     * @param policyNumber the policy number. May be {@code null}
     * @return a new policy
     */
    public static Act createPolicy(Party customer, Party patient, Party insurer, String policyNumber) {
        ActIdentity identity = null;
        if (policyNumber != null) {
            identity = createActIdentity(InsuranceArchetypes.POLICY_IDENTITY, policyNumber);
        }
        return createPolicy(customer, patient, insurer, identity);
    }

    /**
     * Creates a new policy.
     *
     * @param customer the customer
     * @param patient  the patient
     * @param insurer  the insurer
     * @param identity the policy identity. May be {@code null}
     * @return a new policy
     */
    public static Act createPolicy(Party customer, Party patient, Party insurer, ActIdentity identity) {
        Act policy = (Act) create(InsuranceArchetypes.POLICY);
        IMObjectBean bean = new IMObjectBean(policy);
        bean.setTarget("customer", customer);
        bean.setTarget("patient", patient);
        bean.setTarget("insurer", insurer);
        if (identity != null) {
            policy.addIdentity(identity);
        }
        return policy;
    }

    /**
     * Creates a claim for a policy.
     *
     * @param policy       the policy
     * @param location     the practice location
     * @param clinician    the clinician
     * @param claimHandler the claim handler
     * @param items        the claim items. A list of <em>act.patientInsuranceClaimItem</em>
     * @return a new claim
     */
    public static FinancialAct createClaim(Act policy, Party location, User clinician, User claimHandler,
                                           FinancialAct... items) {
        return createClaim(policy, location, clinician, claimHandler, false, items);
    }

    /**
     * Creates a claim for a policy.
     *
     * @param policy       the policy
     * @param location     the practice location
     * @param clinician    the clinician
     * @param claimHandler the claim handler
     * @param gapClaim     if {@code true}, the claim is a gap claim
     * @param items        the claim items. A list of <em>act.patientInsuranceClaimItem</em>
     * @return a new claim
     */
    public static FinancialAct createClaim(Act policy, Party location, User clinician, User claimHandler,
                                           boolean gapClaim, FinancialAct... items) {
        FinancialAct claim = (FinancialAct) create(InsuranceArchetypes.CLAIM);
        IMObjectBean bean = new IMObjectBean(claim);
        IMObjectBean policyBean = new IMObjectBean(policy);
        bean.setTarget("patient", policyBean.getTargetRef("patient"));
        bean.setTarget("author", claimHandler);
        bean.setTarget("clinician", clinician);
        bean.setTarget("location", location);
        bean.setTarget("user", claimHandler);
        bean.addTarget("policy", policy, "claims");
        bean.setValue("gapClaim", gapClaim);
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        for (FinancialAct item : items) {
            bean.addTarget("items", item, "claim");
            total = total.add(item.getTotal());
            tax = tax.add(item.getTaxAmount());
        }
        bean.setValue("amount", total);
        bean.setValue("tax", tax);
        return claim;
    }

    /**
     * Creates a new claim item dated today.
     *
     * @param invoiceItems the invoice items being claimed
     * @return a new claim item
     */
    public static FinancialAct createClaimItem(FinancialAct... invoiceItems) {
        createDiagnosis("VENOM_328", "Abcess", "328");
        return createClaimItem("VENOM_328", new Date(), new Date(), invoiceItems);
    }

    /**
     * Creates a new claim item.
     *
     * @param diagnosis    the VeNom diagnosis code
     * @param startTime    the treatment start time
     * @param endTime      the treatment end time
     * @param invoiceItems the invoice items being claimed
     * @return a new claim item
     */
    public static FinancialAct createClaimItem(String diagnosis, Date startTime, Date endTime,
                                               FinancialAct... invoiceItems) {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        FinancialAct item = (FinancialAct) create(InsuranceArchetypes.CLAIM_ITEM);
        IMObjectBean bean = new IMObjectBean(item);
        item.setReason(TestHelper.getLookup("lookup.diagnosisVeNom", diagnosis, diagnosis, true).getCode());
        item.setActivityStartTime(startTime);
        item.setActivityEndTime(endTime);
        item.setDescription("Condition description");
        for (FinancialAct invoiceItem : invoiceItems) {
            total = total.add(invoiceItem.getTotal());
            tax = tax.add(invoiceItem.getTaxAmount());
            bean.addTarget("items", invoiceItem, "claims");
        }
        bean.setValue("amount", total);
        bean.setValue("tax", tax);
        return item;
    }

    /**
     * Creates an insurance claim attachment.
     *
     * @param document the document to link to
     * @return a new attachment
     */
    public static DocumentAct createAttachment(DocumentAct document) {
        DocumentAct result = (DocumentAct) create(InsuranceArchetypes.ATTACHMENT);
        IMObjectBean bean = new IMObjectBean(result);
        bean.setValue("name", document.getName());
        Relationship original = bean.addTarget("original", document);
        document.addActRelationship((ActRelationship) original);
        save(result, document);
        return result;
    }

    /**
     * Creates a VeNom diagnosis lookup.
     *
     * @param code         the lookup code
     * @param name         the lookup name
     * @param dictionaryId the VeNom dictionary identifier for the code
     * @return the lookup
     */
    public static Lookup createDiagnosis(String code, String name, String dictionaryId) {
        Lookup diagnosis = TestHelper.getLookup("lookup.diagnosisVeNom", code, false);
        IMObjectBean bean = new IMObjectBean(diagnosis);
        bean.setValue("name", name);
        bean.setValue("dataDictionaryId", dictionaryId);
        bean.save();
        return diagnosis;
    }

}
