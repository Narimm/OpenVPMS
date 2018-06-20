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

package org.openvpms.web.workspace.patient.insurance.claim;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.insurance.InsuranceTestHelper;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.internal.InsuranceFactory;
import org.openvpms.insurance.service.InsuranceServices;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.resource.i18n.format.NumberFormatter;
import org.openvpms.web.test.AbstractAppTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.openvpms.archetype.rules.act.ActStatus.POSTED;
import static org.openvpms.archetype.rules.finance.account.FinancialTestHelper.createChargesInvoice;
import static org.openvpms.archetype.test.TestHelper.createActIdentity;

/**
 * Tests the {@link ClaimSubmitter}.
 *
 * @author Tim Anderson
 */
public class ClaimSubmitterTestCase extends AbstractAppTest {

    /**
     * The transaction manager.
     */
    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * The customer rules.
     */
    @Autowired
    private CustomerRules customerRules;

    /**
     * The patient rules.
     */
    @Autowired
    private PatientRules patientRules;

    /**
     * The document handlers.
     */
    @Autowired
    private DocumentHandlers documentHandlers;

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
     * The policy.
     */
    private Act policyAct;

    /**
     * Tracks errors logged.
     */
    private List<String> errors = new ArrayList<>();

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

        // insurer
        Party insurer = InsuranceTestHelper.createInsurer(TestHelper.randomName("ZInsurer-"));

        // policy
        policyAct = InsuranceTestHelper.createPolicy(customer, patient, insurer,
                                                     createActIdentity("actIdentity.insurancePolicy", "POL123456"));
        save(policyAct);

        initErrorHandler(errors);
    }

    /**
     * Tests the {@link ClaimSubmitter#submit(ClaimEditor, Consumer)} method when supplied with a claim that references
     * invoice items claimed by another claim.
     */
    @Test
    public void testDuplicate() {
        FinancialAct invoiceItem1 = createInvoiceItem();
        FinancialAct invoiceItem2 = createInvoiceItem();
        createInvoice(POSTED, invoiceItem1, invoiceItem2);

        FinancialAct claim1Item = InsuranceTestHelper.createClaimItem("VENOM_328", new Date(), new Date(),
                                                                      invoiceItem1);
        FinancialAct claim1 = InsuranceTestHelper.createClaim(policyAct, location, clinician, clinician, claim1Item);
        claim1.setStatus(Claim.Status.PENDING.toString());
        save(claim1, claim1Item, invoiceItem1);

        FinancialAct claim2Item = InsuranceTestHelper.createClaimItem("VENOM_328", new Date(), new Date(),
                                                                      invoiceItem1);
        FinancialAct claim2 = InsuranceTestHelper.createClaim(policyAct, location, clinician, clinician, claim2Item);
        claim2.setStatus(Claim.Status.PENDING.toString());
        save(claim2, claim2Item, invoiceItem1);

        LocalContext context = new LocalContext();
        context.setLocation(location);
        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));
        ClaimEditor editor = new ClaimEditor(claim1, null, layoutContext);
        editor.getComponent();

        checkDuplicate(editor, claim2, false);

        claim2.setStatus(Claim.Status.POSTED.toString());
        save(claim2);
        checkDuplicate(editor, claim2, false);

        claim2.setStatus(Claim.Status.ACCEPTED.toString());
        save(claim2);
        checkDuplicate(editor, claim2, false);

        claim2.setStatus(Claim.Status.SETTLED.toString());
        save(claim2);
        checkDuplicate(editor, claim2, false);

        claim2.setStatus(Claim.Status.DECLINED.toString());
        save(claim2);
        checkDuplicate(editor, claim2, true);

        claim2.setStatus(Claim.Status.CANCELLED.toString());
        save(claim2);
        checkDuplicate(editor, claim2, true);
    }

    /**
     * Tests the {@link ClaimSubmitter#submit(Act, Consumer)} method when supplied with a claim that references
     * invoice items claimed by another claim.
     */
    @Test
    public void testDuplicateAct() {
        FinancialAct invoiceItem1 = createInvoiceItem();
        FinancialAct invoiceItem2 = createInvoiceItem();
        createInvoice(POSTED, invoiceItem1, invoiceItem2);

        FinancialAct claim1Item = InsuranceTestHelper.createClaimItem("VENOM_328", new Date(), new Date(),
                                                                      invoiceItem1);
        FinancialAct claim1 = InsuranceTestHelper.createClaim(policyAct, location, clinician, clinician, claim1Item);
        claim1.setStatus(Claim.Status.POSTED.toString());
        save(claim1, claim1Item, invoiceItem1);

        FinancialAct claim2Item = InsuranceTestHelper.createClaimItem("VENOM_328", new Date(), new Date(),
                                                                      invoiceItem1);
        FinancialAct claim2 = InsuranceTestHelper.createClaim(policyAct, location, clinician, clinician, claim2Item);
        claim2.setStatus(Claim.Status.PENDING.toString());
        save(claim2, claim2Item, invoiceItem1);

        checkDuplicate(claim1, claim2, false);

        claim2.setStatus(Claim.Status.POSTED.toString());
        save(claim2);
        checkDuplicate(claim1, claim2, false);

        claim2.setStatus(Claim.Status.ACCEPTED.toString());
        save(claim2);
        checkDuplicate(claim1, claim2, false);

        claim2.setStatus(Claim.Status.SETTLED.toString());
        save(claim2);
        checkDuplicate(claim1, claim2, false);

        claim2.setStatus(Claim.Status.DECLINED.toString());
        save(claim2);
        checkDuplicate(claim1, claim2, true);

        claim2.setStatus(Claim.Status.CANCELLED.toString());
        save(claim2);
        checkDuplicate(claim1, claim2, true);
    }

    /**
     * Verifies that claims can/can't be submitted if they refer to invoices in another claim.
     *
     * @param editor    the editor
     * @param duplicate the claim the references the same invoice
     * @param allowed   if {@code true} duplicates are allowed (because the duplicate claim is CANCELLED, or DECLINED)
     */
    private void checkDuplicate(ClaimEditor editor, Act duplicate, boolean allowed) {
        errors.clear();
        ClaimSubmitter submitter = createSubmitter();
        submitter.submit(editor, Assert::assertNull);

        checkDuplicate(duplicate, allowed);
    }

    /**
     * Verifies that claims can/can't be submitted if they refer to invoices in another claim.
     *
     * @param claim     the claim
     * @param duplicate the claim the references the same invoice
     * @param allowed   if {@code true} duplicates are allowed (because the duplicate claim is CANCELLED, or DECLINED)
     */
    private void checkDuplicate(Act claim, Act duplicate, boolean allowed) {
        errors.clear();
        ClaimSubmitter submitter = createSubmitter();
        submitter.submit(claim, Assert::assertNull);

        checkDuplicate(duplicate, allowed);
    }

    /**
     * Verifies a duplicate error is/isn't present.
     *
     * @param duplicate the duplicate claim
     * @param allowed   if {@code true} if duplicates are allowed. If so, no error should be present
     */
    private void checkDuplicate(Act duplicate, boolean allowed) {
        if (!allowed) {
            assertEquals(1, errors.size());
            String error = "Cannot submit this claim. It contains charges already claimed by claim "
                           + NumberFormatter.format(duplicate.getId())
                           + ", dated " + DateFormatter.formatDate(duplicate.getActivityStartTime(), false) + ".";
            assertEquals(error, errors.get(0));
        } else {
            assertEquals(0, errors.size());
        }
    }

    /**
     * Creates a claim submitter.
     *
     * @return a new submitter
     */
    private ClaimSubmitter createSubmitter() {
        InsuranceFactory factory = new InsuranceFactory((IArchetypeRuleService) getArchetypeService(), customerRules,
                                                        patientRules, documentHandlers, transactionManager);
        InsuranceServices insuranceServices = Mockito.mock(InsuranceServices.class);
        return new ClaimSubmitter(getArchetypeService(), factory, insuranceServices,
                                  new LocalContext(), new HelpContext("foo", null));
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
