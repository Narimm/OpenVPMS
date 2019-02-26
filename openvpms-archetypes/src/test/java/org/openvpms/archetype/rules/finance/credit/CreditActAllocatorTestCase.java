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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.credit;


import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.BalanceCalculator;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.insurance.InsuranceRules;
import org.openvpms.archetype.rules.insurance.InsuranceTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.act.Act;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link CreditActAllocator}.
 *
 * @author Tim Anderson
 */
public class CreditActAllocatorTestCase extends ArchetypeServiceTest {

    /**
     * The transaction manager.
     */
    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * The balance calculator.
     */
    private BalanceCalculator calculator;

    /**
     * The test customer.
     */
    private Party customer;

    /**
     * The test patient.
     */
    private Party patient;

    /**
     * The test product.
     */
    private Product product;

    /**
     * The test till.
     */
    private Party till;

    /**
     * The test location.
     */
    private Party location;

    /**
     * The credit allocator.
     */
    private CreditActAllocator allocator;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        InsuranceRules insuranceRules = new InsuranceRules((IArchetypeRuleService) getArchetypeService(),
                                                           transactionManager);
        calculator = new BalanceCalculator(getArchetypeService());
        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient(customer);
        product = TestHelper.createProduct();
        till = TestHelper.createTill();
        location = TestHelper.createLocation();
        allocator = new CreditActAllocator(getArchetypeService(), insuranceRules);
    }

    /**
     * Tests the {@link CreditActAllocator#allocate(FinancialAct)} method to an empty account.
     */
    @Test
    public void testAllocatePaymentToEmptyAccount() {
        FinancialAct payment = createPayment(TEN);
        assertFalse(calculator.isAllocated(payment));

        CreditAllocation allocation = allocator.allocate(payment);
        assertEquals(payment, allocation.getCredit());
        assertTrue(allocation.getDebits().isEmpty());
        assertTrue(allocation.getBlocked().isEmpty());
        assertFalse(allocation.isModified());
        assertFalse(allocation.overrideDefaultAllocation());
        assertFalse(calculator.isAllocated(payment));
        save(payment);
        assertFalse(calculator.isAllocated(get(payment)));
    }

    /**
     * Tests payment allocation when there is a single saved invoice with no gap claims, and a single payment.
     * <p>
     * Here, default allocation is used i.e. the same that would be triggered by the payment save rule.
     */
    @Test
    public void testDefaultAllocation() {
        checkDefaultAllocation(TEN, TEN);         // invoice = payment
        checkDefaultAllocation(TEN, BigDecimal.valueOf(20)); // invoice < payment
        checkDefaultAllocation(BigDecimal.valueOf(20), TEN); // invoice > payment
    }

    /**
     * Tests payment allocation when the invoice is associated with a gap claim.
     */
    @Test
    public void testAllocateWithGapClaim() {
        List<FinancialAct> acts = createInvoice(TEN);
        FinancialAct invoice = acts.get(0);
        FinancialAct item = acts.get(1);
        assertFalse(calculator.isAllocated(invoice));
        Party insurer = (Party) InsuranceTestHelper.createInsurer("ZInsurer");
        User clinician = TestHelper.createClinician();
        Act policy = InsuranceTestHelper.createPolicy(customer, patient, insurer, "12345");
        FinancialAct claimItem = (FinancialAct) InsuranceTestHelper.createClaimItem(item);
        FinancialAct claim = (FinancialAct) InsuranceTestHelper.createClaim(policy, location, clinician, clinician,
                                                                            true, claimItem);
        save(policy, claim, claimItem);

        FinancialAct payment = createPayment(TEN);
        assertFalse(calculator.isAllocated(payment));

        CreditAllocation allocation = allocator.allocate(payment);
        assertEquals(1, allocation.getDebits().size());
        assertEquals(invoice, allocation.getDebits().get(0));
        assertEquals(1, allocation.getBlocked().size());
        assertEquals(invoice, allocation.getBlocked().keySet().iterator().next());
        assertFalse(allocation.isModified());
        assertTrue(allocation.overrideDefaultAllocation()); // indicates default allocation cannot be used
    }

    /**
     * Verifies that when there is multiple invoices that can be allocated against, and one of those is linked
     * to a gap claim, the other invoice will be used.
     */
    @Test
    public void testAllocateWithGapClaimAndMultipleInvoices() {
        List<FinancialAct> invoice1Acts = createInvoice(TEN);
        FinancialAct invoice1 = invoice1Acts.get(0);
        FinancialAct item1 = invoice1Acts.get(1);
        List<FinancialAct> invoice2Acts = createInvoice(TEN);
        FinancialAct invoice2 = invoice2Acts.get(0);
        assertFalse(calculator.isAllocated(invoice1));
        assertFalse(calculator.isAllocated(invoice2));

        // create a claim for the invoice item
        Party insurer = (Party) InsuranceTestHelper.createInsurer("ZInsurer");
        User clinician = TestHelper.createClinician();
        Act policy = InsuranceTestHelper.createPolicy(customer, patient, insurer, "12345");
        FinancialAct claimItem = (FinancialAct) InsuranceTestHelper.createClaimItem(item1);
        FinancialAct claim = (FinancialAct) InsuranceTestHelper.createClaim(policy, location, clinician, clinician,
                                                                            true, claimItem);
        save(policy, claim, claimItem);

        // create a payment
        FinancialAct payment = createPayment(TEN);
        assertFalse(calculator.isAllocated(payment));

        CreditAllocation allocation = allocator.allocate(payment);
        assertEquals(1, allocation.getDebits().size());
        assertEquals(invoice2, allocation.getDebits().get(0));
        assertEquals(1, allocation.getBlocked().size());
        assertEquals(invoice1, allocation.getBlocked().keySet().iterator().next());
        assertTrue(allocation.isModified());
        assertEquals(2, allocation.getModified().size());
        assertTrue(allocation.getModified().contains(payment));
        assertTrue(allocation.getModified().contains(invoice2));
        assertFalse(allocation.overrideDefaultAllocation()); // false as no gap claims affected

        save(allocation.getModified());
        assertFalse(calculator.isAllocated(get(invoice1)));
        assertTrue(calculator.isAllocated(get(invoice2)));
        assertTrue(calculator.isAllocated(get(payment)));
    }

    /**
     * Tests the {@link CreditActAllocator#allocate(FinancialAct, List)} method.
     */
    @Test
    public void testAllocateAgainstSpecifiedInvoices() {
        FinancialAct invoice1 = createInvoice(TEN).get(0);
        FinancialAct invoice2 = createInvoice(TEN).get(0);
        FinancialAct invoice3 = createInvoice(TEN).get(0);
        FinancialAct invoice4 = createInvoice(TEN).get(0);
        assertFalse(calculator.isAllocated(invoice1));
        assertFalse(calculator.isAllocated(invoice2));
        assertFalse(calculator.isAllocated(invoice3));
        assertFalse(calculator.isAllocated(invoice3));

        // create a payment
        BigDecimal twenty1 = BigDecimal.valueOf(21);
        FinancialAct payment = createPayment(twenty1);
        assertFalse(calculator.isAllocated(payment));

        List<FinancialAct> acts = allocator.allocate(payment, Arrays.asList(invoice2, invoice3, invoice4));
        assertEquals(4, acts.size());
        assertEquals(invoice2, acts.get(0));
        assertEquals(invoice3, acts.get(1));
        assertEquals(invoice4, acts.get(2));
        assertEquals(payment, acts.get(3));
        checkAllocation(invoice1, ZERO);
        checkAllocation(invoice2, TEN);
        checkAllocation(invoice3, TEN);
        checkAllocation(invoice4, ONE);
        checkAllocation(payment, twenty1);
        checkEquals(twenty1, payment.getAllocatedAmount());

        save(acts);
        checkAllocation(get(invoice1), ZERO);
        checkAllocation(get(invoice2), TEN);
        checkAllocation(get(invoice3), TEN);
        checkAllocation(get(invoice4), ONE);
        checkAllocation(get(payment), twenty1);
    }

    /**
     * Verifies that the allocation of an act matches that expected.
     *
     * @param act    the act
     * @param amount the expected allocation
     */
    private void checkAllocation(FinancialAct act, BigDecimal amount) {
        checkEquals(amount, act.getAllocatedAmount());
    }

    /**
     * Tests payment allocation when there is a single saved invoice with no gap claims, and a single payment.
     * <p>
     * Here, default allocation is used i.e. the same that would be triggered by the payment save rule.
     *
     * @param invoiceAmount the invoice amount
     * @param paymentAmount the payment amount
     */
    private void checkDefaultAllocation(BigDecimal invoiceAmount, BigDecimal paymentAmount) {
        FinancialAct invoice = createInvoice(invoiceAmount).get(0);
        assertFalse(calculator.isAllocated(invoice));

        FinancialAct payment = createPayment(paymentAmount);
        assertFalse(calculator.isAllocated(payment));

        CreditAllocation allocation = allocator.allocate(payment);
        assertEquals(payment, allocation.getCredit());
        assertEquals(1, allocation.getDebits().size());
        assertTrue(allocation.getDebits().contains(invoice));
        assertTrue(allocation.getBlocked().isEmpty());
        assertFalse(allocation.isModified());
        assertFalse(allocation.overrideDefaultAllocation());
        assertFalse(calculator.isAllocated(payment));
        assertFalse(calculator.isAllocated(invoice));
        save(payment);
        boolean allocated = paymentAmount.compareTo(invoiceAmount) <= 0;
        assertEquals(allocated, calculator.isAllocated(get(payment)));
    }

    /**
     * Creates and saves a POSTED invoice.
     *
     * @param amount the invoice amount
     * @return the invoice acts
     */
    private List<FinancialAct> createInvoice(BigDecimal amount) {
        List<FinancialAct> invoice = FinancialTestHelper.createChargesInvoice(amount, customer, patient,
                                                                              product, ActStatus.POSTED);
        save(invoice);
        return invoice;
    }

    /**
     * Creates but does not save, a POSTED payment.
     *
     * @param amount the payment amount
     * @return the payment
     */
    private FinancialAct createPayment(BigDecimal amount) {
        return FinancialTestHelper.createPayment(amount, customer, till, ActStatus.POSTED);
    }

}
