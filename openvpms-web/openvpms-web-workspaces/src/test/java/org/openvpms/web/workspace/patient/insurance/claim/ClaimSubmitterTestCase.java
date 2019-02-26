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

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountQueryFactory;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.insurance.InsuranceTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.claim.GapClaim;
import org.openvpms.insurance.internal.InsuranceFactory;
import org.openvpms.insurance.internal.claim.GapClaimImpl;
import org.openvpms.insurance.service.InsuranceServices;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.doc.DocumentTestHelper;
import org.openvpms.web.component.im.edit.payment.PaymentEditor;
import org.openvpms.web.component.im.edit.payment.PaymentItemEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.print.PrintDialog;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.resource.i18n.format.NumberFormatter;
import org.openvpms.web.test.AbstractAppTest;
import org.openvpms.web.test.EchoTestHelper;
import org.openvpms.web.workspace.customer.credit.CreditActEditDialog;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openvpms.archetype.rules.act.ActStatus.POSTED;
import static org.openvpms.archetype.rules.finance.account.FinancialTestHelper.createChargesInvoice;
import static org.openvpms.archetype.rules.finance.account.FinancialTestHelper.createPayment;
import static org.openvpms.archetype.test.TestHelper.createActIdentity;
import static org.openvpms.web.test.EchoTestHelper.fireDialogButton;

/**
 * Tests the {@link ClaimSubmitter}.
 *
 * @author Tim Anderson
 */
public class ClaimSubmitterTestCase extends AbstractAppTest {

    /**
     * The insurance factory.
     */
    @Autowired
    private InsuranceFactory insuranceFactory;

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
     * The insurer.
     */
    private Party insurer;

    /**
     * The insurance service.
     */
    private TestGapInsuranceService insuranceService;

    /**
     * The practice.
     */
    private Party practice;

    /**
     * Sets up the test case.
     */
    @Before
    @Override
    public void setUp() {
        // NOTE: need to create the practice prior to the application as it caches the practice in the context
        practice = TestHelper.getPractice();
        super.setUp();
        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient(customer);
        clinician = TestHelper.createClinician();
        location = TestHelper.createLocation();

        // insurer
        insurer = (Party) InsuranceTestHelper.createInsurer(TestHelper.randomName("ZInsurer-"));

        // policy
        policyAct = (Act) InsuranceTestHelper.createPolicy(customer, patient, insurer,
                                                           createActIdentity("actIdentity.insurancePolicy", "POL123456"));
        save(policyAct);

        insuranceService = new TestGapInsuranceService();

        initErrorHandler(errors);
    }

    /**
     * Tests the {@link ClaimSubmitter#submit(ClaimEditor, Consumer)} method when supplied with a claim that references
     * invoice items claimed by another claim.
     */
    @Test
    public void testDuplicate() {
        initDocumentTemplate("INSURANCE_CLAIM_MEDICAL_RECORDS", "Insurance Claim Medical Records");
        initDocumentTemplate("INSURANCE_CLAIM_INVOICE", "Insurance Claim Invoice");

        FinancialAct invoiceItem1 = createInvoiceItem();
        FinancialAct invoiceItem2 = createInvoiceItem();
        FinancialAct invoice = createInvoice(POSTED, invoiceItem1, invoiceItem2);
        FinancialAct payment = createPayment(invoice.getTotal(), customer, TestHelper.createTill(), POSTED);
        save(payment);

        FinancialAct claim1Item = (FinancialAct) InsuranceTestHelper.createClaimItem("VENOM_328", new Date(),
                                                                                     new Date(), invoiceItem1);
        FinancialAct claim1 = (FinancialAct) InsuranceTestHelper.createClaim(policyAct, location, clinician, clinician,
                                                                             claim1Item);
        claim1.setStatus(Claim.Status.PENDING.toString());
        save(claim1, claim1Item, invoiceItem1);

        FinancialAct claim2Item = (FinancialAct) InsuranceTestHelper.createClaimItem("VENOM_328", new Date(),
                                                                                     new Date(), invoiceItem1);
        FinancialAct claim2 = (FinancialAct) InsuranceTestHelper.createClaim(policyAct, location, clinician, clinician,
                                                                             claim2Item);
        claim2.setStatus(Claim.Status.PENDING.toString());
        save(claim2, claim2Item, invoiceItem1);

        LocalContext context = new LocalContext();
        context.setPractice(practice);
        context.setLocation(location);
        context.setUser(clinician);
        context.setCustomer(customer);
        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));
        ClaimEditor editor = new TestClaimEditor(claim1, layoutContext);
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

        FinancialAct claim1Item = (FinancialAct) InsuranceTestHelper.createClaimItem("VENOM_328", new Date(),
                                                                                     new Date(), invoiceItem1);
        FinancialAct claim1 = (FinancialAct) InsuranceTestHelper.createClaim(policyAct, location, clinician, clinician,
                                                                             claim1Item);
        claim1.setStatus(Claim.Status.POSTED.toString());
        save(claim1, claim1Item, invoiceItem1);

        FinancialAct claim2Item = (FinancialAct) InsuranceTestHelper.createClaimItem("VENOM_328", new Date(),
                                                                                     new Date(), invoiceItem1);
        FinancialAct claim2 = (FinancialAct) InsuranceTestHelper.createClaim(policyAct, location, clinician, clinician,
                                                                             claim2Item);
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
     * Tests paying the gap in a gap claim.
     */
    @Test
    public void testPayGapClaim() {
        initDocumentTemplate(CustomerAccountArchetypes.PAYMENT, "Receipt");
        Party till = TestHelper.createTill(location);
        Context context = new LocalContext();
        context.setUser(clinician);
        context.setTill(till);
        context.setLocation(location);
        context.setCustomer(customer);
        context.setPractice(practice);
        FinancialAct invoiceItem1 = createInvoiceItem();
        FinancialAct invoiceItem2 = createInvoiceItem();
        createInvoice(POSTED, invoiceItem1, invoiceItem2);

        FinancialAct claimItem = (FinancialAct) InsuranceTestHelper.createClaimItem("VENOM_328", new Date(),
                                                                                    new Date(), invoiceItem1);
        FinancialAct claim = (FinancialAct) InsuranceTestHelper.createClaim(policyAct, location, clinician, clinician,
                                                                            true, claimItem);
        claim.setStatus(Claim.Status.POSTED.toString());
        save(claim, claimItem, invoiceItem1);

        ClaimSubmitter submitter = createSubmitter(context);
        submitter.submit(claim, Assert::assertNull);
        ConfirmationDialog confirm = findComponent(ConfirmationDialog.class);
        assertEquals("Submit Claim", confirm.getTitle());
        String message = "This claim will be submitted to " + insurer.getName() + " using Test Service.\n\n" +
                         "Submit claim?";
        assertEquals(message, confirm.getMessage());
        EchoTestHelper.fireDialogButton(confirm, ConfirmationDialog.YES_ID);

        BenefitDialog benefit = findComponent(BenefitDialog.class);
        assertEquals("Waiting for Claim Benefit", benefit.getTitle());
        assertEquals("The claim has been submitted to " + insurer.getName() + ".\n\n" +
                     "Please wait for them to determine the benefit amount.", benefit.getMessage());

        BigDecimal benefitAmount = BigDecimal.valueOf(5);
        BigDecimal gapAmount = BigDecimal.valueOf(4);
        benefit.getClaim().setBenefit(benefitAmount, "Approved");
        benefit.refresh();

        GapPaymentPrompt prompt = findComponent(GapPaymentPrompt.class);
        assertEquals("Pay Claim", prompt.getTitle());
        prompt.setPayGap(true);
        EchoTestHelper.fireDialogButton(prompt, ConfirmationDialog.OK_ID);

        CreditActEditDialog payment = findComponent(CreditActEditDialog.class);
        assertEquals("New Payment", payment.getTitle());
        PaymentEditor paymentEditor = (PaymentEditor) payment.getEditor();
        PaymentItemEditor paymentItemEditor = paymentEditor.addItem();
        paymentItemEditor.setAmount(gapAmount);
        assertTrue(paymentEditor.isValid());
        fireDialogButton(payment, PopupDialog.OK_ID);

        // verify the claim has been updated
        claim = get(claim);
        checkEquals(gapAmount, getBean(claim).getBigDecimal("paid"));
        assertEquals(GapClaim.GapStatus.PAID.toString(), claim.getStatus2());

        PrintDialog print = findComponent(PrintDialog.class);
        assertEquals("Print Receipt?", print.getTitle());
        fireDialogButton(print, PopupDialog.CANCEL_ID);

        // verify there are three account acts for the customer, the original invoice, the claim payment and
        // a credit adjustment for the benefit amount
        ArchetypeQuery query = CustomerAccountQueryFactory.createQuery(customer,
                                                                       CustomerAccountArchetypes.ACCOUNT_ACTS);
        List<IMObject> objects = QueryHelper.query(query, getArchetypeService());
        assertEquals(3, objects.size());
        checkAccount(CustomerAccountArchetypes.INVOICE, BigDecimal.valueOf(18), objects);
        checkAccount(CustomerAccountArchetypes.PAYMENT, gapAmount, objects);
        checkAccount(CustomerAccountArchetypes.CREDIT_ADJUST, benefitAmount, objects);

        // verify the gap status has been updated
        claim = get(claim);
        assertEquals(GapClaim.GapStatus.NOTIFIED.toString(), claim.getStatus2());

        // and the insurer notified
        assertEquals(1, insuranceService.getPaymentNotified());
    }

    /**
     * Tests paying a gap claim in full.
     */
    @Test
    public void testPayFullGapClaim() {
        initDocumentTemplate(CustomerAccountArchetypes.PAYMENT, "Receipt");
        Party till = TestHelper.createTill(location);
        Context context = new LocalContext();
        context.setUser(clinician);
        context.setTill(till);
        context.setLocation(location);
        context.setCustomer(customer);
        context.setPractice(practice);
        FinancialAct invoiceItem1 = createInvoiceItem();
        FinancialAct invoiceItem2 = createInvoiceItem();
        createInvoice(POSTED, invoiceItem1, invoiceItem2);

        FinancialAct claimItem = (FinancialAct) InsuranceTestHelper.createClaimItem("VENOM_328", new Date(),
                                                                                    new Date(), invoiceItem1);
        FinancialAct claim = (FinancialAct) InsuranceTestHelper.createClaim(policyAct, location, clinician, clinician,
                                                                            true, claimItem);
        claim.setStatus(Claim.Status.POSTED.toString());
        save(claim, claimItem, invoiceItem1);

        ClaimSubmitter submitter = createSubmitter(context);
        submitter.submit(claim, Assert::assertNull);
        ConfirmationDialog confirm = findComponent(ConfirmationDialog.class);
        assertEquals("Submit Claim", confirm.getTitle());
        String message = "This claim will be submitted to " + insurer.getName() + " using Test Service.\n\n" +
                         "Submit claim?";
        assertEquals(message, confirm.getMessage());
        EchoTestHelper.fireDialogButton(confirm, ConfirmationDialog.YES_ID);

        BenefitDialog benefit = findComponent(BenefitDialog.class);
        assertEquals("Waiting for Claim Benefit", benefit.getTitle());
        assertEquals("The claim has been submitted to " + insurer.getName() + ".\n\n" +
                     "Please wait for them to determine the benefit amount.", benefit.getMessage());
        EchoTestHelper.fireDialogButton(benefit, BenefitDialog.PAY_FULL_CLAIM_ID);
        GapPaymentPrompt prompt = findComponent(GapPaymentPrompt.class);
        assertEquals("Pay Claim", prompt.getTitle());
        prompt.setPayFull(true);
        EchoTestHelper.fireDialogButton(prompt, ConfirmationDialog.OK_ID);

        // pay the claim
        CreditActEditDialog payment = pay(claim.getTotal());
        assertTrue(payment.getEditor().isValid());
        fireDialogButton(payment, PopupDialog.OK_ID);

        // verify the claim has been updated
        assertEquals(claim.getTotal(), getBean(claim).getBigDecimal("paid"));
        assertEquals(GapClaim.GapStatus.PAID.toString(), claim.getStatus2());

        PrintDialog print = findComponent(PrintDialog.class);
        assertEquals("Print Receipt?", print.getTitle());
        fireDialogButton(print, PopupDialog.CANCEL_ID);

        // verify there are only two account acts for the customer, the original invoice, and the claim payment
        ArchetypeQuery query = CustomerAccountQueryFactory.createQuery(customer,
                                                                       CustomerAccountArchetypes.ACCOUNT_ACTS);
        List<IMObject> objects = QueryHelper.query(query, getArchetypeService());
        assertEquals(2, objects.size());
        checkAccount(CustomerAccountArchetypes.INVOICE, BigDecimal.valueOf(18), objects);
        checkAccount(CustomerAccountArchetypes.PAYMENT, BigDecimal.valueOf(9), objects);

        // verify the gap status has been updated
        claim = get(claim);
        assertEquals(GapClaim.GapStatus.NOTIFIED.toString(), claim.getStatus2());

        // and the insurer notified
        assertEquals(1, insuranceService.getPaymentNotified());
    }

    /**
     * Tests paying a gap claim in full, at the same time as the claim benefit is received.
     * <p/>
     * The payment dialog should roll back, but submission subsequently succeed.
     */
    @Test
    public void testUpdateBenefitWhenPayingFullGapClaim() {
        initDocumentTemplate(CustomerAccountArchetypes.PAYMENT, "Receipt");
        Party till = TestHelper.createTill(location);
        Context context = new LocalContext();
        context.setUser(clinician);
        context.setTill(till);
        context.setLocation(location);
        context.setCustomer(customer);
        context.setPractice(practice);
        FinancialAct invoiceItem1 = createInvoiceItem();
        FinancialAct invoiceItem2 = createInvoiceItem();
        createInvoice(POSTED, invoiceItem1, invoiceItem2);

        FinancialAct claimItem = (FinancialAct) InsuranceTestHelper.createClaimItem("VENOM_328", new Date(),
                                                                                    new Date(), invoiceItem1);
        FinancialAct claim = (FinancialAct) InsuranceTestHelper.createClaim(policyAct, location, clinician, clinician,
                                                                            true, claimItem);
        claim.setStatus(Claim.Status.POSTED.toString());
        save(claim, claimItem, invoiceItem1);

        ClaimSubmitter submitter = createSubmitter(context);
        submitter.submit(claim, Assert::assertNull);
        ConfirmationDialog confirm = findComponent(ConfirmationDialog.class);
        assertEquals("Submit Claim", confirm.getTitle());
        String message = "This claim will be submitted to " + insurer.getName() + " using Test Service.\n\n" +
                         "Submit claim?";
        assertEquals(message, confirm.getMessage());
        EchoTestHelper.fireDialogButton(confirm, ConfirmationDialog.YES_ID);

        BenefitDialog benefit = findComponent(BenefitDialog.class);
        assertEquals("Waiting for Claim Benefit", benefit.getTitle());
        assertEquals("The claim has been submitted to " + insurer.getName() + ".\n\n" +
                     "Please wait for them to determine the benefit amount.", benefit.getMessage());
        EchoTestHelper.fireDialogButton(benefit, BenefitDialog.PAY_FULL_CLAIM_ID);
        GapPaymentPrompt prompt = findComponent(GapPaymentPrompt.class);
        assertEquals("Pay Claim", prompt.getTitle());
        prompt.setPayFull(true);
        EchoTestHelper.fireDialogButton(prompt, ConfirmationDialog.OK_ID);

        CreditActEditDialog payment1 = pay(claim.getTotal());

        // now simulate the claim benefit being received from the insurer
        claim = get(claim);
        claim.setStatus(Claim.Status.ACCEPTED.toString());
        claim.setStatus2(GapClaim.GapStatus.RECEIVED.toString());
        save(claim);

        assertTrue(payment1.getEditor().isValid());
        fireDialogButton(payment1, PopupDialog.OK_ID);

        assertEquals(1, errors.size());
        assertEquals("The Payment could not be saved as it has been modified by another user.\n\n" +
                     "Your changes have been reverted.", errors.get(0));

        // attempt payment again. This time it should go through
        CreditActEditDialog payment2 = pay(claim.getTotal());
        fireDialogButton(payment2, PopupDialog.OK_ID);

        // verify the claim has been updated
        claim = get(claim);
        assertEquals(claim.getTotal(), getBean(claim).getBigDecimal("paid"));
        assertEquals(GapClaim.GapStatus.PAID.toString(), claim.getStatus2());

        PrintDialog print = findComponent(PrintDialog.class);
        assertEquals("Print Receipt?", print.getTitle());
        fireDialogButton(print, PopupDialog.CANCEL_ID);

        // verify there are only two account acts for the customer, the original invoice, and the claim payment
        ArchetypeQuery query = CustomerAccountQueryFactory.createQuery(customer,
                                                                       CustomerAccountArchetypes.ACCOUNT_ACTS);
        List<IMObject> objects = QueryHelper.query(query, getArchetypeService());
        assertEquals(2, objects.size());
        checkAccount(CustomerAccountArchetypes.INVOICE, BigDecimal.valueOf(18), objects);
        checkAccount(CustomerAccountArchetypes.PAYMENT, BigDecimal.valueOf(9), objects);

        // verify the gap status has been updated
        claim = get(claim);
        assertEquals(GapClaim.GapStatus.NOTIFIED.toString(), claim.getStatus2());

        // and the insurer notified
        assertEquals(1, insuranceService.getPaymentNotified());
    }

    /**
     * Pays a claim.
     *
     * @param amount the amount to pay
     * @return the payment edit dialog
     */
    private CreditActEditDialog pay(BigDecimal amount) {
        CreditActEditDialog payment = findComponent(CreditActEditDialog.class);
        assertEquals("New Payment", payment.getTitle());
        PaymentEditor paymentEditor = (PaymentEditor) payment.getEditor();
        PaymentItemEditor paymentItemEditor = paymentEditor.addItem();
        paymentItemEditor.getProperty("amount").setValue(amount);
        return payment;
    }

    /**
     * Verifies that a {@code POSTED} customer account act exists with the specified archetype and total, in a list of
     * acts.
     *
     * @param archetype the expected archetype
     * @param total     the expected total
     * @param acts      the acts to search
     */
    private void checkAccount(String archetype, BigDecimal total, List<IMObject> acts) {
        boolean found = false;
        for (IMObject object : acts) {
            if (object.isA(archetype)) {
                FinancialAct act = (FinancialAct) object;
                assertEquals(ActStatus.POSTED, act.getStatus());
                if (act.getTotal().compareTo(total) == 0) {
                    found = true;
                    break;
                }
            }
        }
        assertTrue(found);
    }

    /**
     * Creates a dummy document template, if one doesn't exist.
     *
     * @param type the template type
     * @param name the template name
     */
    private void initDocumentTemplate(String type, String name) {
        Entity template = new TemplateHelper(getArchetypeService()).getTemplateForArchetype(type);
        if (template == null) {
            template = DocumentTestHelper.createDocumentTemplate(type, name);
            Document document = DocumentTestHelper.createDocument("/blank.jrxml");
            DocumentTestHelper.createDocumentTemplate(template, document);
        } else {
            if (!StringUtils.equals(name, template.getName())) {
                template.setName(name);
                save(template);
            }
        }
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
        LocalContext context = new LocalContext();
        context.setPractice(practice);
        context.setLocation(location);
        context.setUser(clinician);
        return createSubmitter(context);
    }

    /**
     * Creates a claim submitter.
     *
     * @param context the context
     * @return a new submitter
     */
    private ClaimSubmitter createSubmitter(Context context) {
        InsuranceServices insuranceServices = mock(InsuranceServices.class);
        when(insuranceServices.canSubmit(Mockito.any())).thenReturn(true);
        when(insuranceServices.getService(Mockito.any())).thenReturn(insuranceService);
        return new ClaimSubmitter(getArchetypeService(), insuranceFactory, insuranceServices,
                                  context, new HelpContext("foo", null)) {
            @Override
            protected BenefitDialog createBenefitDialog(GapClaimImpl claim) {
                return new BenefitDialog(claim, new HelpContext("foo", null)) {
                    @Override
                    protected boolean reload(long now) {
                        return true;
                    }
                };
            }
        };

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
