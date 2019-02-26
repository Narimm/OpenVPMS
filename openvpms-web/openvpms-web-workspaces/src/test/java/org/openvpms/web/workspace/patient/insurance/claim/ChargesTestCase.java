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

package org.openvpms.web.workspace.patient.insurance.claim;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.insurance.InsuranceArchetypes;
import org.openvpms.archetype.rules.insurance.InsuranceRules;
import org.openvpms.archetype.rules.insurance.InsuranceTestHelper;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.rule.ArchetypeRuleService;
import org.openvpms.insurance.internal.InsuranceFactory;
import org.openvpms.insurance.service.InsuranceServices;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.test.AbstractAppTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.act.ActStatus.IN_PROGRESS;
import static org.openvpms.archetype.rules.act.ActStatus.POSTED;
import static org.openvpms.archetype.rules.finance.account.FinancialTestHelper.createChargesInvoice;

/**
 * Tests the {@link Charges} class.
 *
 * @author Tim Anderson
 */
public class ChargesTestCase extends AbstractAppTest {

    /**
     * The transaction manager.
     */
    @Autowired
    PlatformTransactionManager transactionManager;

    /**
     * The customer account rules.
     */
    @Autowired
    CustomerAccountRules accountRules;

    /**
     * The test customer.
     */
    private Party customer;

    /**
     * The test patient.
     */
    private Party patient;

    /**
     * The test clinician.
     */
    private User clinician;

    /**
     * The practice location.
     */
    private Party location;

    /**
     * The till.
     */
    private Party till;

    /**
     * The insurance rules.
     */
    private InsuranceRules insuranceRules;

    private Context context;

    /**
     * Sets up the test case.
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();
        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient(customer);
        clinician = TestHelper.createClinician();
        location = TestHelper.createLocation();
        till = TestHelper.createTill();
        insuranceRules = new InsuranceRules((ArchetypeRuleService) getArchetypeService(),
                                            transactionManager);
        context = new LocalContext();
        context.setPractice(TestHelper.getPractice());
        context.setLocation(TestHelper.createLocation());
        context.setUser(TestHelper.createUser());

        // diagnosis codes
        InsuranceTestHelper.createDiagnosis("VENOM_328", "Abcess", "328");
    }

    /**
     * Tests the {@link Charges#canClaimInvoice(FinancialAct)} method.
     */
    @Test
    public void testCanClaimInvoice() {
        FinancialAct claim = (FinancialAct) create(InsuranceArchetypes.CLAIM);
        Charges charges = new Charges(createClaimContext(claim));
        FinancialAct item1 = createInvoiceItem();
        FinancialAct item2 = createInvoiceItem();
        FinancialAct invoice = createInvoice(IN_PROGRESS, item1, item2);
        assertFalse(charges.canClaimInvoice(invoice));
        invoice.setStatus(FinancialActStatus.ON_HOLD);
        assertFalse(charges.canClaimInvoice(invoice));
        invoice.setStatus(FinancialActStatus.COMPLETED);
        assertFalse(charges.canClaimInvoice(invoice));

        // now set the status to POSTED. Can't claim as it is not paid.
        invoice.setStatus(POSTED);
        assertFalse(charges.canClaimInvoice(invoice));

        // now pay the invoice, and verify it can be claimed.
        save(invoice);
        createPayment(invoice.getTotal());
        invoice = get(invoice);
        assertTrue(charges.canClaimInvoice(invoice));

        // now reverse the invoice, and verify it can't be claimed
        assertFalse(charges.isReversed(invoice));
        accountRules.reverse(invoice, new Date());
        assertTrue(charges.isReversed(invoice));
        assertFalse(charges.canClaimInvoice(invoice));
    }

    /**
     * Tests the {@link Charges#canClaimItem(Act)} method.
     */
    @Test
    public void testCanClaimItem() {
        FinancialAct claim1 = (FinancialAct) create(InsuranceArchetypes.CLAIM);
        Charges charges1 = new Charges(createClaimContext(claim1));

        FinancialAct item1 = createInvoiceItem();
        FinancialAct item2 = createInvoiceItem();
        FinancialAct invoice = createInvoice(IN_PROGRESS, item1, item2);

        assertFalse(charges1.canClaimItem(item1));
        assertFalse(charges1.canClaimItem(item2));
        invoice.setStatus(POSTED);
        save(invoice);

        assertFalse(charges1.canClaimItem(item1));
        assertFalse(charges1.canClaimItem(item2));
        createPayment(invoice.getTotal());

        // invoices are cached, so check will still return false
        assertFalse(charges1.canClaimItem(item1));
        assertFalse(charges1.canClaimItem(item2));

        // need to reload
        Charges charges2 = new Charges(createClaimContext(claim1));
        assertTrue(charges2.canClaimItem(item1));
        assertTrue(charges2.canClaimItem(item2));

        charges2.add(item1);
        assertFalse(charges2.canClaimItem(item1));
        assertTrue(charges2.canClaimItem(item2));

        charges2.add(item2);
        assertFalse(charges2.canClaimItem(item1));
        assertFalse(charges2.canClaimItem(item2));

        charges2.remove(item1);
        assertTrue(charges2.canClaimItem(item1));
        assertFalse(charges2.canClaimItem(item2));

        // now add item1 to a claim. It should not be able to be claimed in another claim.
        Act policy = (Act) InsuranceTestHelper.createPolicy(customer, patient, InsuranceTestHelper.createInsurer("Foo"),
                                                            "POL123456");
        save(policy);
        FinancialAct claimItem1 = (FinancialAct) InsuranceTestHelper.createClaimItem("VENOM_328", new Date(),
                                                                                     new Date(), item1);
        FinancialAct claim2 = (FinancialAct) InsuranceTestHelper.createClaim(policy, location, clinician, clinician,
                                                                             claimItem1);
        save(claim2, claimItem1, item1);

        Charges charges3 = new Charges(createClaimContext(claim2));
        assertFalse(charges3.canClaimItem(item1));
        assertTrue(charges3.canClaimItem(item2));

        // now cancel the claim, and verify the item can be claimed. Need to recreate due to caching.
        claim2.setStatus(ActStatus.CANCELLED);
        save(claim2);
        assertFalse(charges3.canClaimItem(item1));

        Charges charges4 = new Charges(createClaimContext(claim2));
        assertTrue(charges4.canClaimItem(item1));
    }

    /**
     * Tests the {@link Charges#isPaid(FinancialAct)} method.
     */
    @Test
    public void testIsPaid() {
        FinancialAct claim = (FinancialAct) create(InsuranceArchetypes.CLAIM);
        Charges charges = new Charges(createClaimContext(claim));

        FinancialAct invoice = createInvoice(POSTED, createInvoiceItem());
        assertFalse(charges.isPaid(invoice));

        FinancialAct payment = createPayment(MathRules.divide(invoice.getTotal(), 2, 2));
        invoice = get(invoice);
        assertFalse(charges.isPaid(invoice));

        // create another payment, making sure the invoice is fully paid
        createPayment(invoice.getTotal().subtract(payment.getTotal()));
        invoice = get(invoice);
        assertTrue(charges.isPaid(invoice));
    }

    /**
     * Tests the {@link Charges#isReversed(FinancialAct)} method.
     */
    @Test
    public void testIsReversed() {
        FinancialAct claim = (FinancialAct) create(InsuranceArchetypes.CLAIM);
        Charges charges = new Charges(createClaimContext(claim));

        FinancialAct invoice = createInvoice(POSTED, createInvoiceItem());
        assertFalse(charges.isReversed(invoice));

        accountRules.reverse(invoice, new Date());
        assertTrue(charges.isReversed(invoice));
    }

    /**
     * Creates a claim context.
     *
     * @param claim the claim
     * @return a new claim context
     */
    private ClaimContext createClaimContext(FinancialAct claim) {
        return new ClaimContext(claim, customer, patient, clinician, context.getPractice(), getArchetypeService(),
                                insuranceRules, Mockito.mock(InsuranceServices.class),
                                Mockito.mock(InsuranceFactory.class));
    }

    /**
     * Creates and saves a payment.
     *
     * @param total the payment total
     * @return the payment
     */
    private FinancialAct createPayment(BigDecimal total) {
        List<FinancialAct> payment = FinancialTestHelper.createPaymentCash(total, customer, till, POSTED);
        save(payment);
        return payment.get(0);
    }

    /**
     * Creates and saves an invoice.
     *
     * @param status the invoice status
     * @param items  the invoice items
     * @return the invoice
     */
    private FinancialAct createInvoice(String status, FinancialAct... items) {
        List<FinancialAct> invoice = createChargesInvoice(customer, clinician, status, items);
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
        return FinancialTestHelper.createInvoiceItem(date, patient, clinician, product, quantity, ZERO, price,
                                                     discount, tax);
    }

}
