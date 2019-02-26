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

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.bean.IMObjectBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.insurance.InsuranceTestHelper.createClaim;
import static org.openvpms.archetype.rules.insurance.InsuranceTestHelper.createClaimItem;

/**
 * Tests the {@link InsuranceRules} class.
 *
 * @author Tim Anderson
 */
public class InsuranceRulesTestCase extends ArchetypeServiceTest {

    /**
     * The archetype service.
     */
    @Autowired
    private IArchetypeRuleService service;

    /**
     * The transaction manager.
     */
    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * The insurance rules.
     */
    private InsuranceRules rules;

    /**
     * The test customer.
     */
    private Party customer;

    /**
     * The test patient.
     */
    private Party patient;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        rules = new InsuranceRules(service, transactionManager);
        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient(customer);
        InsuranceTestHelper.createDiagnosis("VENOM_328", "Abcess", "328"); // diagnosis codes
    }

    /**
     * Tests the {@link InsuranceRules#createPolicy} method.
     */
    @Test
    public void testCreatePolicy() {
        Party insurer = (Party) InsuranceTestHelper.createInsurer();
        User author = TestHelper.createUser();

        Act policy1 = rules.createPolicy(customer, patient, insurer, null, null);
        assertTrue(policy1.isNew());
        assertTrue(policy1.isA(InsuranceArchetypes.POLICY));
        save(policy1);
        checkPolicy(policy1, customer, patient, insurer, null, null);

        Act policy2 = rules.createPolicy(customer, patient, insurer, "POL1234", null);
        save(policy2);
        checkPolicy(policy2, customer, patient, insurer, "POL1234", null);

        Act policy3 = rules.createPolicy(customer, patient, insurer, "POL987651", author);
        save(policy3);
        checkPolicy(policy3, customer, patient, insurer, "POL987651", author);
    }

    /**
     * Tests the {@link InsuranceRules#getPolicyForClaim} method when a policy is not associated with any claims.
     * <p>
     * These policies can be re-used.
     */
    @Test
    public void testGetPolicyForClaimForUnusedPolicy() {
        Party insurer1 = (Party) InsuranceTestHelper.createInsurer();
        Party insurer2 = (Party) InsuranceTestHelper.createInsurer();
        User author1 = TestHelper.createUser();
        User author2 = TestHelper.createUser();

        // verify that when no policy exists, a new one will be created
        Act policy1 = rules.getPolicyForClaim(customer, patient, insurer1, null, author1, null, null);
        assertNotNull(policy1);
        assertTrue(policy1.isNew());
        save(policy1);
        checkPolicy(policy1, customer, patient, insurer1, null, author1);

        // verify that the same policy is returned when just the author changes
        Act policy2 = rules.getPolicyForClaim(customer, patient, insurer1, null, author2, null, null);
        assertEquals(policy1, policy2);
        checkPolicy(policy2, customer, patient, insurer1, null, author1); // author not updated, as policy is re-used

        // verify that the policy is reused when just the policy number changes
        Act policy3 = rules.getPolicyForClaim(customer, patient, insurer1, "POL1234", author1, null, null);
        assertEquals(policy1, policy3);
        checkPolicy(policy3, customer, patient, insurer1, "POL1234", author1);

        // verify that the policy is reused when just the policy number changes
        Act policy4 = rules.getPolicyForClaim(customer, patient, insurer1, "POL5678", author1, null, null);
        assertEquals(policy1, policy4);
        checkPolicy(policy4, customer, patient, insurer1, "POL5678", author1);

        // verify that the policy is reused when the insurer changes
        Act policy5 = rules.getPolicyForClaim(customer, patient, insurer2, "POL5678", author1, null, null);
        assertEquals(policy1, policy5);
        checkPolicy(policy5, customer, patient, insurer2, "POL5678", author1);

        // verify that the policy is reused when the insurer and policy number changes
        Act policy6 = rules.getPolicyForClaim(customer, patient, insurer1, "POL1234", author1, null, null);
        assertEquals(policy1, policy6);
        checkPolicy(policy6, customer, patient, insurer1, "POL1234", author1);
    }

    /**
     * Tests the {@link InsuranceRules#getPolicyForClaim} method.
     * <p>
     * Verifies that a policy won't be re-used if there is an existing policy for a different patient belonging
     * to the same customer.
     */
    @Test
    public void testGetPolicyForClaimForDifferentPatient() {
        Party patient2 = TestHelper.createPatient(customer);
        Party insurer1 = (Party) InsuranceTestHelper.createInsurer();
        User author = TestHelper.createUser();
        Act policy1 = rules.getPolicyForClaim(customer, patient, insurer1, null, author, null, null);
        save(policy1);

        Act policy2 = rules.getPolicyForClaim(customer, patient2, insurer1, null, author, null, null);
        assertNotEquals(policy1, policy2);
    }

    /**
     * Tests the behaviour of {@link InsuranceRules#getPolicyForClaim} when the patient has policies with
     * associated claims.
     */
    @Test
    public void testGetPolicyForClaimWithExistingClaims() {
        Party insurer1 = (Party) InsuranceTestHelper.createInsurer();
        User clinician = TestHelper.createClinician();
        Act policy1 = rules.createPolicy(customer, patient, insurer1, "POL12345", clinician);
        FinancialAct claim1Item1 = (FinancialAct) createClaimItem("VENOM_328", new Date(), new Date());
        FinancialAct claim1 = (FinancialAct) createClaim(policy1, TestHelper.createLocation(),
                                                         clinician, clinician, claim1Item1);
        save(claim1, claim1Item1, policy1);

        Act same1 = rules.getPolicyForClaim(customer, patient, insurer1, "POL12345", null, null, null);
        assertEquals(policy1, same1);
        checkPolicy(same1, customer, patient, insurer1, "POL12345", clinician); // verify the details haven't changed

        // verify a new policy is created if the policy number changes
        Act policy2 = rules.getPolicyForClaim(customer, patient, insurer1, "POL98765", clinician, null, null);
        assertNotEquals(policy1, policy2);
        checkPolicy(policy2, customer, patient, insurer1, "POL98765", clinician);

        // associate policy2 with a new claim
        FinancialAct claim2item1 = (FinancialAct) createClaimItem("VENOM_328", new Date(), new Date());
        FinancialAct claim2 = (FinancialAct) createClaim(policy2, TestHelper.createLocation(),
                                                         clinician, clinician, claim2item1);
        save(claim2, claim2item1, policy2);

        // verify policy2 is updated when the policy number changes, and policy2 is passed as the existing policy
        Act same2 = rules.getPolicyForClaim(customer, patient, insurer1, "POL222222", null, claim2, policy2);
        assertEquals(policy2, same2);
        checkPolicy(policy2, customer, patient, insurer1, "POL222222", clinician);
    }

    /**
     * Tests the {@link InsuranceRules#getCurrentPolicy} method.
     */
    @Test
    public void testGetCurrentPolicy() {
        assertNull(rules.getCurrentPolicy(customer, patient));
        Act policy = rules.createPolicy(customer, patient, InsuranceTestHelper.createInsurer(), null, null);
        save(policy);
        assertEquals(policy, rules.getCurrentPolicy(customer, patient));

        policy.setActivityStartTime(DateRules.getYesterday());
        policy.setActivityEndTime(DateRules.getToday());
        save(policy);
        assertNull(rules.getCurrentPolicy(customer, patient));

        policy.setActivityEndTime(null);
        save(policy);
        assertEquals(policy, rules.getCurrentPolicy(customer, patient));
    }

    /**
     * Tests the {@link InsuranceRules#createClaim(Act)} method.
     */
    @Test
    public void testCreateClaim() {
        Act policy = rules.createPolicy(customer, patient, InsuranceTestHelper.createInsurer(), null, null);
        save(policy);
        FinancialAct claim = (FinancialAct) rules.createClaim(policy);
        assertNotNull(claim);
        assertTrue(claim.isA(InsuranceArchetypes.CLAIM));
        IMObjectBean bean = service.getBean(claim);
        assertEquals(policy, bean.getTarget("policy"));
    }

    /**
     * Tests the {@link InsuranceRules#getCurrentClaims} method.
     */
    @Test
    public void testGetCurrentClaims() {
        Act policy = rules.createPolicy(customer, patient, InsuranceTestHelper.createInsurer(), null, null);
        save(policy);

        User clinician = TestHelper.createClinician();
        FinancialAct invoice1Item1 = createInvoiceItem();
        FinancialAct invoice1Item2 = createInvoiceItem();
        FinancialAct invoice1Item3 = createInvoiceItem();
        FinancialAct invoice1 = createInvoice(FinancialActStatus.POSTED, invoice1Item1, invoice1Item2, invoice1Item3);

        FinancialAct invoice2Item1 = createInvoiceItem();
        FinancialAct invoice2 = createInvoice(FinancialActStatus.POSTED, invoice2Item1);

        FinancialAct claim1Item1 = (FinancialAct) createClaimItem(invoice1Item1);
        FinancialAct claim1Item2 = (FinancialAct) createClaimItem(invoice1Item2);
        FinancialAct claim1 = (FinancialAct) createClaim(policy, TestHelper.createLocation(), clinician, clinician,
                                                         claim1Item1, claim1Item2);
        FinancialAct claim2Item1 = (FinancialAct) createClaimItem(invoice1Item3);
        FinancialAct claim2 = (FinancialAct) createClaim(policy, TestHelper.createLocation(), clinician, clinician,
                                                         claim2Item1);
        save(claim1, claim1Item1, claim1Item2, invoice1Item1, invoice1Item2, invoice1Item3, claim2Item1, claim2);

        checkCurrentClaims(invoice1, claim1, claim2);

        checkCurrentClaims(invoice2); // not associated with any claims

        claim1.setStatus(ClaimStatus.ACCEPTED);
        save(claim1);
        checkCurrentClaims(invoice1, claim1, claim2);

        claim1.setStatus(ClaimStatus.SETTLED);
        save(claim1);
        checkCurrentClaims(invoice1, claim2);

        claim1.setStatus(ClaimStatus.DECLINED);
        save(claim1);
        checkCurrentClaims(invoice1, claim2);

        claim1.setStatus(ClaimStatus.CANCELLED);
        save(claim1);
        checkCurrentClaims(invoice1, claim2);
    }

    /**
     * Tests the {@link InsuranceRules#getCurrentGapClaims} method.
     */
    @Test
    public void testGetCurrentGapClaims() {
        Act policy = rules.createPolicy(customer, patient, InsuranceTestHelper.createInsurer(), null, null);
        save(policy);

        User clinician = TestHelper.createClinician();
        FinancialAct invoice1Item1 = createInvoiceItem();
        FinancialAct invoice1Item2 = createInvoiceItem();
        FinancialAct invoice1Item3 = createInvoiceItem();
        FinancialAct invoice1 = createInvoice(FinancialActStatus.POSTED, invoice1Item1, invoice1Item2, invoice1Item3);

        FinancialAct invoice2Item1 = createInvoiceItem();
        FinancialAct invoice2 = createInvoice(FinancialActStatus.POSTED, invoice2Item1);

        FinancialAct claim1Item1 = (FinancialAct) createClaimItem(invoice1Item1);
        FinancialAct claim1Item2 = (FinancialAct) createClaimItem(invoice1Item2);
        FinancialAct claim1 = (FinancialAct) createClaim(policy, TestHelper.createLocation(), clinician, clinician,
                                                         true, claim1Item1, claim1Item2);
        FinancialAct claim2Item1 = (FinancialAct) createClaimItem(invoice1Item3);
        FinancialAct claim2 = (FinancialAct) createClaim(policy, TestHelper.createLocation(), clinician, clinician,
                                                         true, claim2Item1);
        save(claim1, claim1Item1, claim1Item2, invoice1Item1, invoice1Item2, invoice1Item3, claim2Item1, claim2);

        checkCurrentGapClaims(invoice1, claim1, claim2);

        checkCurrentGapClaims(invoice2); // not associated with any claims

        claim1.setStatus(ClaimStatus.ACCEPTED);
        save(claim1);
        checkCurrentGapClaims(invoice1, claim1, claim2);

        claim1.setStatus2(ClaimStatus.GAP_CLAIM_PENDING);
        save(claim1);
        checkCurrentGapClaims(invoice1, claim1, claim2);

        claim1.setStatus2(ClaimStatus.GAP_CLAIM_RECEIVED);
        save(claim1);
        checkCurrentGapClaims(invoice1, claim1, claim2);

        claim1.setStatus2(ClaimStatus.GAP_CLAIM_PAID);
        save(claim1);
        checkCurrentGapClaims(invoice1, claim2);

        claim1.setStatus2(ClaimStatus.GAP_CLAIM_NOTIFIED);
        save(claim1);
        checkCurrentGapClaims(invoice1, claim2);

        claim1.setStatus(ClaimStatus.SETTLED);
        save(claim1);
        checkCurrentGapClaims(invoice1, claim2);

        claim1.setStatus(ClaimStatus.DECLINED);
        save(claim1);
        checkCurrentGapClaims(invoice1, claim2);

        claim1.setStatus(ClaimStatus.CANCELLED);
        save(claim1);
        checkCurrentGapClaims(invoice1, claim2);
    }

    /**
     * Tests the {@link InsuranceRules#canChangePolicyNumber(Act)} method.
     */
    @Test
    public void testCanChangePolicyNumber() {
        Act policy = rules.createPolicy(customer, patient, InsuranceTestHelper.createInsurer(), null, null);
        save(policy);

        assertTrue(rules.canChangePolicyNumber(policy));

        User clinician = TestHelper.createClinician();
        FinancialAct item1 = (FinancialAct) createClaimItem("VENOM_328", new Date(), new Date());
        FinancialAct claim = (FinancialAct) createClaim(policy, TestHelper.createLocation(),
                                                        clinician, clinician, item1);
        save(claim, item1, policy);

        assertEquals(ClaimStatus.PENDING, claim.getStatus());
        assertTrue(rules.canChangePolicyNumber(policy));

        claim.setStatus(ClaimStatus.POSTED);
        save(claim);
        assertTrue(rules.canChangePolicyNumber(policy));

        claim.setStatus(ClaimStatus.SUBMITTED);
        save(claim);
        assertFalse(rules.canChangePolicyNumber(policy));

        claim.setStatus(ClaimStatus.ACCEPTED);
        save(claim);
        assertFalse(rules.canChangePolicyNumber(policy));

        claim.setStatus(ClaimStatus.CANCELLING);
        save(claim);
        assertFalse(rules.canChangePolicyNumber(policy));

        claim.setStatus(ClaimStatus.CANCELLED);
        save(claim);
        assertFalse(rules.canChangePolicyNumber(policy));

        claim.setStatus(ClaimStatus.SETTLED);
        save(claim);
        assertFalse(rules.canChangePolicyNumber(policy));

        claim.setStatus(ClaimStatus.DECLINED);
        save(claim);
        assertFalse(rules.canChangePolicyNumber(policy));
    }

    /**
     * Verifies that {@link InsuranceRules#getCurrentClaims} returns the expected claims.
     *
     * @param invoice the invoice
     * @param claims  the expected claims
     */
    private void checkCurrentClaims(FinancialAct invoice, FinancialAct... claims) {
        List<org.openvpms.component.model.act.FinancialAct> currentClaims = rules.getCurrentClaims(invoice);
        assertEquals(claims.length, currentClaims.size());
        for (FinancialAct claim : claims) {
            assertTrue(currentClaims.contains(claim));
        }
    }

    /**
     * Verifies that {@link InsuranceRules#getCurrentGapClaims} returns the expected claims.
     *
     * @param invoice the invoice
     * @param claims  the expected claims
     */
    private void checkCurrentGapClaims(FinancialAct invoice, FinancialAct... claims) {
        List<org.openvpms.component.model.act.FinancialAct> currentClaims = rules.getCurrentGapClaims(invoice);
        assertEquals(claims.length, currentClaims.size());
        for (FinancialAct claim : claims) {
            assertTrue(currentClaims.contains(claim));
        }
    }

    /**
     * Creates and saves an invoice.
     *
     * @param status the invoice status
     * @param items  the invoice items
     * @return the invoice
     */
    private FinancialAct createInvoice(String status, FinancialAct... items) {
        List<FinancialAct> invoice = FinancialTestHelper.createChargesInvoice(customer, TestHelper.createClinician(),
                                                                              status, items);
        save(invoice);
        return invoice.get(0);
    }

    /**
     * Creates an invoice item, with quantity=1, price=10, discount=1, tax=0.82, total=9
     *
     * @return the new invoice item
     */
    private FinancialAct createInvoiceItem() {
        BigDecimal discount = BigDecimal.ONE;
        BigDecimal tax = new BigDecimal("0.82");
        return createInvoiceItem(new Date(), TestHelper.createProduct(), ONE, BigDecimal.TEN, discount, tax);
    }

    /**
     * Creates an invoice item.
     *
     * @param date     the date
     * @param product  the product
     * @param quantity the quantity
     * @param price    the unit price
     * @param discount the discount
     * @param tax      the tax
     * @return the new invoice item
     */
    private FinancialAct createInvoiceItem(Date date, Product product, BigDecimal quantity, BigDecimal price,
                                           BigDecimal discount, BigDecimal tax) {
        User clinician = TestHelper.createClinician();
        return FinancialTestHelper.createInvoiceItem(date, patient, clinician, product, quantity, ZERO, price,
                                                     discount, tax);
    }

    /**
     * Checks a policy.
     *
     * @param policy       the policy
     * @param customer     the expected customer
     * @param patient      the expected patient
     * @param insurer      the expected insurer
     * @param policyNumber the expected policy number. May be {@code null}
     * @param author       the expected author. May be {@code null}
     */
    private void checkPolicy(Act policy, Party customer, Party patient, Party insurer, String policyNumber,
                             User author) {
        IMObjectBean bean = service.getBean(policy);
        assertEquals(customer, bean.getTarget("customer"));
        assertEquals(patient, bean.getTarget("patient"));
        assertEquals(insurer, bean.getTarget("insurer"));
        assertEquals(policyNumber, rules.getPolicyNumber(policy));
        assertEquals(author, bean.getTarget("author"));
        assertNotNull(policy.getActivityStartTime());
        assertNull(policy.getActivityEndTime());
    }

}
