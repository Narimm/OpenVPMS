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

package org.openvpms.web.workspace.customer.credit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.insurance.InsuranceTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.claim.GapClaim;
import org.openvpms.insurance.service.InsuranceServices;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.payment.CustomerPaymentEditor;
import org.openvpms.web.component.im.edit.payment.PaymentItemEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;
import org.openvpms.web.workspace.patient.insurance.claim.TestGapInsuranceService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openvpms.web.test.EchoTestHelper.fireDialogButton;

/**
 * Tests the {@link CreditActEditDialog}.
 *
 * @author Tim Anderson
 */
public class CreditActEditDialogTestCase extends AbstractAppTest {

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
     * The test location.
     */
    private Party location;

    /**
     * The test context.
     */
    private Context context;

    /**
     * The test policy.
     */
    private Act policy;

    /**
     * The insurance service.
     */
    private TestGapInsuranceService insuranceService;

    /**
     * Tracks errors logged.
     */
    private List<String> errors = new ArrayList<>();

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        Party practice = TestHelper.getPractice();
        super.setUp();
        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient();
        clinician = TestHelper.createClinician();
        location = TestHelper.createLocation();
        Party till = TestHelper.createTill(location);

        context = new LocalContext();
        context.setPractice(practice);
        context.setLocation(location);
        context.setTill(till);
        context.setClinician(clinician);
        context.setUser(clinician);
        context.setCustomer(customer);
        context.setPatient(patient);

        Party insurer = (Party) InsuranceTestHelper.createInsurer("ZInsurer");
        policy = (Act) InsuranceTestHelper.createPolicy(customer, patient, insurer, "12345");
        insuranceService = new TestGapInsuranceService();

        initErrorHandler(errors);
    }

    /**
     * Tests allocation when a payment allocates to multiple gap claims, and no benefit has been received.
     */
    @Test
    public void testChangeAllocationToGapClaimsWithNoReceivedBenefit() {
        List<FinancialAct> invoice1Acts = createInvoice(TEN);
        FinancialAct invoice1 = invoice1Acts.get(0);
        FinancialAct item1 = invoice1Acts.get(1);
        List<FinancialAct> invoice2Acts = createInvoice(TEN);
        FinancialAct invoice2 = invoice2Acts.get(0);
        FinancialAct item2 = invoice2Acts.get(1);

        // create a claim for each invoice item
        FinancialAct claim1 = createClaim(item1, Claim.Status.SUBMITTED);
        FinancialAct claim2 = createClaim(item2, Claim.Status.SUBMITTED);

        CreditActEditDialog editDialog = pay(TEN);
        FinancialAct payment = (FinancialAct) editDialog.getEditor().getObject();

        // verify an AllocationDialog has been displayed. Payment will be allocated to invoice1 then invoice2
        AllocationDialog allocationDialog = findComponent(AllocationDialog.class);
        checkDebits(allocationDialog, invoice1, invoice2);

        // change the allocation order
        allocationDialog.swap(invoice1, invoice2);
        checkDebits(allocationDialog, invoice2, invoice1);

        // verify the allocation
        List<GapClaimAllocation> allocations = allocationDialog.getGapClaimAllocations();
        assertEquals(2, allocations.size());
        checkAllocation(claim1, allocations, BigDecimal.ZERO); // not allocated
        checkAllocation(claim2, allocations, TEN);             // allocated

        fireDialogButton(allocationDialog, PopupDialog.OK_ID);

        GapClaimAllocationDialog claimAllocationDialog = findComponent(GapClaimAllocationDialog.class);
        assertEquals(claim2.getId(), claimAllocationDialog.getAllocation().getClaim().getId());
        assertEquals("The benefit amount for this Gap Claim has not been received.\n\n" +
                     "The claim will be fully paid and the insurer will reimburse the customer.",
                     claimAllocationDialog.getMessage());

        fireDialogButton(claimAllocationDialog, PopupDialog.OK_ID);

        checkAllocation(get(invoice1), BigDecimal.ZERO);
        checkAllocation(get(invoice2), TEN);
        checkAllocation(get(payment), TEN);
        checkClaim(claim1, Claim.Status.SUBMITTED, null);
        checkClaim(claim2, Claim.Status.SUBMITTED, GapClaim.GapStatus.NOTIFIED);
        assertTrue(errors.isEmpty());
        assertEquals(1, insuranceService.getPaymentNotified());
    }

    /**
     * Tests allocation when a payment allocates to a gap claim that hasn't been submitted.
     */
    @Test
    public void testPartialAllocationToUnsubmittedGapClaim() {
        List<FinancialAct> invoice1Acts = createInvoice(TEN);
        FinancialAct invoice1 = invoice1Acts.get(0);
        FinancialAct item1 = invoice1Acts.get(1);

        // create a claim for each invoice item
        FinancialAct claim1 = createClaim(item1, Claim.Status.PENDING);

        BigDecimal five = BigDecimal.valueOf(5);
        CreditActEditDialog editDialog = pay(five);
        FinancialAct payment = (FinancialAct) editDialog.getEditor().getObject();

        // verify an AllocationDialog has been displayed. Payment will be allocated to invoice1
        AllocationDialog allocationDialog = findComponent(AllocationDialog.class);
        checkDebits(allocationDialog, invoice1);

        // verify the allocation
        List<GapClaimAllocation> allocations = allocationDialog.getGapClaimAllocations();
        assertEquals(1, allocations.size());
        checkAllocation(claim1, allocations, five);

        fireDialogButton(allocationDialog, PopupDialog.OK_ID);

        GapClaimAllocationDialog claimAllocationDialog = findComponent(GapClaimAllocationDialog.class);
        assertEquals(claim1.getId(), claimAllocationDialog.getAllocation().getClaim().getId());
        assertEquals("The benefit amount for this Gap Claim has not been received.\n\n" +
                     "The customer may have to pay the full claim amount and be reimbursed by the insurer.",
                     claimAllocationDialog.getMessage());

        fireDialogButton(claimAllocationDialog, PopupDialog.OK_ID);

        checkAllocation(get(invoice1), five);
        checkAllocation(get(payment), five);
        checkClaim(claim1, Claim.Status.PENDING, null);
        assertTrue(errors.isEmpty());
        assertEquals(0, insuranceService.getPaymentNotified());
    }

    /**
     * Tests allocation when a payment allocates to a gap claim where the allocation is equal to the gap amount.
     * <p/>
     * This is the same as accepting the gap claim. A credit adjustment should be created.
     */
    @Test
    public void testAllocationToClaimEqualToGapAmount() {
        List<FinancialAct> invoice1Acts = createInvoice(TEN);
        FinancialAct invoice1 = invoice1Acts.get(0);
        FinancialAct item1 = invoice1Acts.get(1);
        BigDecimal five = BigDecimal.valueOf(5);
        FinancialAct claim1 = createClaim(item1, Claim.Status.ACCEPTED, GapClaim.GapStatus.RECEIVED, five);

        CreditActEditDialog editDialog = pay(five);
        FinancialAct payment = (FinancialAct) editDialog.getEditor().getObject();

        // verify an AllocationDialog has been displayed. Payment will be allocated to invoice1
        AllocationDialog allocationDialog = findComponent(AllocationDialog.class);
        checkDebits(allocationDialog, invoice1);

        // verify the allocation
        List<GapClaimAllocation> allocations = allocationDialog.getGapClaimAllocations();
        assertEquals(1, allocations.size());
        checkAllocation(claim1, allocations, five);

        fireDialogButton(allocationDialog, PopupDialog.OK_ID);

        GapClaimAllocationDialog claimAllocationDialog = findComponent(GapClaimAllocationDialog.class);
        assertEquals(claim1.getId(), claimAllocationDialog.getAllocation().getClaim().getId());
        assertEquals("By making this payment, the customer is accepting the benefit amount.",
                     claimAllocationDialog.getMessage());

        fireDialogButton(claimAllocationDialog, PopupDialog.OK_ID);

        checkAllocation(get(invoice1), TEN); //
        checkAllocation(get(payment), five);
        List<FinancialAct> adjustments = editDialog.getAdjustments();
        assertEquals(1, adjustments.size());
        checkAllocation(get(adjustments.get(0)), five);
        checkClaim(claim1, Claim.Status.ACCEPTED, GapClaim.GapStatus.NOTIFIED);
        assertTrue(errors.isEmpty());
        assertEquals(1, insuranceService.getPaymentNotified());
    }

    /**
     * Tests allocation when a payment allocates to a gap claim where the allocation is less than the gap amount.
     */
    @Test
    public void testAllocationToClaimLessThanGapAmount() {
        List<FinancialAct> invoice1Acts = createInvoice(TEN);
        FinancialAct invoice1 = invoice1Acts.get(0);
        FinancialAct item1 = invoice1Acts.get(1);
        BigDecimal five = BigDecimal.valueOf(5);
        FinancialAct claim1 = createClaim(item1, Claim.Status.ACCEPTED, GapClaim.GapStatus.RECEIVED, five);

        CreditActEditDialog editDialog = pay(ONE);
        FinancialAct payment = (FinancialAct) editDialog.getEditor().getObject();

        // verify an AllocationDialog has been displayed. Payment will be allocated to invoice1
        AllocationDialog allocationDialog = findComponent(AllocationDialog.class);
        checkDebits(allocationDialog, invoice1);

        // verify the allocation
        List<GapClaimAllocation> allocations = allocationDialog.getGapClaimAllocations();
        assertEquals(1, allocations.size());
        checkAllocation(claim1, allocations, ONE);

        fireDialogButton(allocationDialog, PopupDialog.OK_ID);

        GapClaimAllocationDialog claimAllocationDialog = findComponent(GapClaimAllocationDialog.class);
        assertEquals(claim1.getId(), claimAllocationDialog.getAllocation().getClaim().getId());
        assertEquals("The customer must pay $4.00 more to receive the benefit amount.",
                     claimAllocationDialog.getMessage());

        fireDialogButton(claimAllocationDialog, PopupDialog.OK_ID);

        checkAllocation(get(invoice1), ONE);
        checkAllocation(get(payment), ONE);
        assertEquals(0, editDialog.getAdjustments().size());
        checkClaim(claim1, Claim.Status.ACCEPTED, GapClaim.GapStatus.RECEIVED);
        assertTrue(errors.isEmpty());
        assertEquals(0, insuranceService.getPaymentNotified());
    }

    /**
     * Tests allocation when a payment allocates to a gap claim where the allocation is greater than the gap amount.
     */
    @Test
    public void testAllocationToClaimGreaterThanGapAmount() {
        List<FinancialAct> invoice1Acts = createInvoice(TEN);
        FinancialAct invoice1 = invoice1Acts.get(0);
        FinancialAct item1 = invoice1Acts.get(1);
        BigDecimal five = BigDecimal.valueOf(5);
        BigDecimal six = BigDecimal.valueOf(6);
        FinancialAct claim1 = createClaim(item1, Claim.Status.ACCEPTED, GapClaim.GapStatus.RECEIVED, five);

        CreditActEditDialog editDialog = pay(six);
        FinancialAct payment = (FinancialAct) editDialog.getEditor().getObject();

        // verify an AllocationDialog has been displayed. Payment will be allocated to invoice1
        AllocationDialog allocationDialog = findComponent(AllocationDialog.class);
        checkDebits(allocationDialog, invoice1);

        // verify the allocation
        List<GapClaimAllocation> allocations = allocationDialog.getGapClaimAllocations();
        assertEquals(1, allocations.size());
        checkAllocation(claim1, allocations, six);

        fireDialogButton(allocationDialog, PopupDialog.OK_ID);

        GapClaimAllocationDialog claimAllocationDialog = findComponent(GapClaimAllocationDialog.class);
        assertEquals(claim1.getId(), claimAllocationDialog.getAllocation().getClaim().getId());
        assertEquals("By making this payment, the customer is declining the benefit amount.\n\n" +
                     "The customer needs to pay a further $4.00 to be reimbursed by the insurer.",
                     claimAllocationDialog.getMessage());

        fireDialogButton(claimAllocationDialog, PopupDialog.OK_ID);

        checkAllocation(get(invoice1), six);
        checkAllocation(get(payment), six);
        assertEquals(0, editDialog.getAdjustments().size());
        checkClaim(claim1, Claim.Status.ACCEPTED, GapClaim.GapStatus.RECEIVED);
        assertTrue(errors.isEmpty());
        assertEquals(0, insuranceService.getPaymentNotified());
    }

    /**
     * Tests allocation when a payment allocates to a gap claim where the allocation is greater than the gap amount.
     */
    @Test
    public void testAllocationToClaimEqualToAmount() {
        List<FinancialAct> invoice1Acts = createInvoice(TEN);
        FinancialAct invoice1 = invoice1Acts.get(0);
        FinancialAct item1 = invoice1Acts.get(1);
        BigDecimal five = BigDecimal.valueOf(5);
        FinancialAct claim1 = createClaim(item1, Claim.Status.ACCEPTED, GapClaim.GapStatus.RECEIVED, five);

        CreditActEditDialog editDialog = pay(TEN);
        FinancialAct payment = (FinancialAct) editDialog.getEditor().getObject();

        // verify an AllocationDialog has been displayed. Payment will be allocated to invoice1
        AllocationDialog allocationDialog = findComponent(AllocationDialog.class);
        checkDebits(allocationDialog, invoice1);

        // verify the allocation
        List<GapClaimAllocation> allocations = allocationDialog.getGapClaimAllocations();
        assertEquals(1, allocations.size());
        checkAllocation(claim1, allocations, TEN);

        fireDialogButton(allocationDialog, PopupDialog.OK_ID);

        GapClaimAllocationDialog claimAllocationDialog = findComponent(GapClaimAllocationDialog.class);
        assertEquals(claim1.getId(), claimAllocationDialog.getAllocation().getClaim().getId());
        assertEquals("By making this payment, the customer is declining the benefit amount.",
                     claimAllocationDialog.getMessage());

        fireDialogButton(claimAllocationDialog, PopupDialog.OK_ID);

        checkAllocation(get(invoice1), TEN);
        checkAllocation(get(payment), TEN);
        assertEquals(0, editDialog.getAdjustments().size());
        checkClaim(claim1, Claim.Status.ACCEPTED, GapClaim.GapStatus.NOTIFIED);
        assertTrue(errors.isEmpty());
        assertEquals(1, insuranceService.getPaymentNotified());
    }

    /**
     * Tests allocation to multiple claims where no benefit has been received.
     */
    @Test
    public void testAllocationToMultipleClaims() {
        List<FinancialAct> invoice1Acts = createInvoice(TEN);
        FinancialAct invoice1 = invoice1Acts.get(0);
        FinancialAct item1 = invoice1Acts.get(1);
        List<FinancialAct> invoice2Acts = createInvoice(TEN);
        FinancialAct invoice2 = invoice2Acts.get(0);
        FinancialAct item2 = invoice2Acts.get(1);

        // create a claim for each invoice item
        FinancialAct claim1 = createClaim(item1, Claim.Status.SUBMITTED);
        FinancialAct claim2 = createClaim(item2, Claim.Status.SUBMITTED);

        BigDecimal twenty = BigDecimal.valueOf(20);
        CreditActEditDialog editDialog = pay(twenty);
        FinancialAct payment = (FinancialAct) editDialog.getEditor().getObject();

        // verify an AllocationDialog has been displayed. Payment will be allocated to invoice1 then invoice2
        AllocationDialog allocationDialog = findComponent(AllocationDialog.class);
        checkDebits(allocationDialog, invoice1, invoice2);

        // change the allocation order
        allocationDialog.swap(invoice1, invoice2);
        checkDebits(allocationDialog, invoice2, invoice1);

        // verify the allocation
        List<GapClaimAllocation> allocations = allocationDialog.getGapClaimAllocations();
        assertEquals(2, allocations.size());
        checkAllocation(claim1, allocations, TEN);
        checkAllocation(claim2, allocations, TEN);
        fireDialogButton(allocationDialog, PopupDialog.OK_ID);

        GapClaimAllocationDialog claimAllocationDialog1 = findComponent(GapClaimAllocationDialog.class);
        assertEquals("The benefit amount for this Gap Claim has not been received.\n\n" +
                     "The claim will be fully paid and the insurer will reimburse the customer.",
                     claimAllocationDialog1.getMessage());

        fireDialogButton(claimAllocationDialog1, PopupDialog.OK_ID);
        GapClaimAllocationDialog claimAllocationDialog2 = findComponent(GapClaimAllocationDialog.class);
        assertEquals("The benefit amount for this Gap Claim has not been received.\n\n" +
                     "The claim will be fully paid and the insurer will reimburse the customer.",
                     claimAllocationDialog2.getMessage());
        fireDialogButton(claimAllocationDialog2, PopupDialog.OK_ID);

        checkAllocation(get(invoice1), TEN);
        checkAllocation(get(invoice2), TEN);
        checkAllocation(get(payment), twenty);
        checkClaim(claim1, Claim.Status.SUBMITTED, GapClaim.GapStatus.NOTIFIED);
        checkClaim(claim2, Claim.Status.SUBMITTED, GapClaim.GapStatus.NOTIFIED);
        assertTrue(errors.isEmpty());
        assertEquals(2, insuranceService.getPaymentNotified());
    }

    /**
     * Verifies that when a claim is updated during payment allocation, the payment rolls back, but can be subsequently
     * paid.
     */
    @Test
    public void testClaimUpdatedDuringPaymentAllocation() {
        List<FinancialAct> invoice1Acts = createInvoice(TEN);
        FinancialAct invoice1 = invoice1Acts.get(0);
        FinancialAct item1 = invoice1Acts.get(1);
        BigDecimal five = BigDecimal.valueOf(5);
        FinancialAct claim1 = createClaim(item1, Claim.Status.SUBMITTED);

        CreditActEditDialog editDialog = pay(TEN);
        FinancialAct payment = (FinancialAct) editDialog.getEditor().getObject();

        // verify an AllocationDialog has been displayed. Payment will be allocated to invoice1
        AllocationDialog allocationDialog = findComponent(AllocationDialog.class);
        checkDebits(allocationDialog, invoice1);

        // verify the allocation
        List<GapClaimAllocation> allocations = allocationDialog.getGapClaimAllocations();
        assertEquals(1, allocations.size());
        checkAllocation(claim1, allocations, TEN);

        fireDialogButton(allocationDialog, PopupDialog.OK_ID);

        // now update the claim
        claim1.setStatus(Claim.Status.ACCEPTED.toString());
        setGapStatus(claim1, GapClaim.GapStatus.RECEIVED, five);
        save(claim1);

        GapClaimAllocationDialog claimAllocationDialog = findComponent(GapClaimAllocationDialog.class);
        assertEquals(claim1.getId(), claimAllocationDialog.getAllocation().getClaim().getId());
        assertEquals("The benefit amount for this Gap Claim has not been received.\n\n" +
                     "The claim will be fully paid and the insurer will reimburse the customer.",
                     claimAllocationDialog.getMessage());
        assertTrue(errors.isEmpty());

        // now try and save the payment and allocations. This should fail as the claim has been updated
        checkEquals(TEN, payment.getTotal());
        checkEquals(ZERO, payment.getAllocatedAmount());
        fireDialogButton(claimAllocationDialog, PopupDialog.OK_ID);
        assertEquals(1, errors.size());
        assertEquals("The Payment could not be saved as it has been modified by another user.\n\n" +
                     "Your changes have been reverted.", errors.get(0));

        // amounts should reset
        checkEquals(ZERO, payment.getTotal());
        checkEquals(ZERO, payment.getAllocatedAmount());
        errors.clear();

        // now pay the gap amount
        pay(editDialog, five);

        // verify an AllocationDialog has been displayed. Payment will be allocated to invoice1
        AllocationDialog allocationDialog2 = findComponent(AllocationDialog.class);
        checkDebits(allocationDialog2, invoice1);

        // verify the allocation
        List<GapClaimAllocation> allocations2 = allocationDialog2.getGapClaimAllocations();
        assertEquals(1, allocations2.size());
        checkAllocation(claim1, allocations2, five);

        fireDialogButton(allocationDialog2, PopupDialog.OK_ID);

        GapClaimAllocationDialog claimAllocationDialog2 = findComponent(GapClaimAllocationDialog.class);
        assertEquals(claim1.getId(), claimAllocationDialog2.getAllocation().getClaim().getId());
        assertEquals("By making this payment, the customer is accepting the benefit amount.",
                     claimAllocationDialog2.getMessage());
        fireDialogButton(claimAllocationDialog2, PopupDialog.OK_ID);

        checkAllocation(get(invoice1), TEN); //
        checkAllocation(get(payment), five);
        List<FinancialAct> adjustments = editDialog.getAdjustments();
        assertEquals(1, adjustments.size());
        checkAllocation(get(adjustments.get(0)), five);
        checkClaim(claim1, Claim.Status.ACCEPTED, GapClaim.GapStatus.NOTIFIED);
        assertTrue(errors.isEmpty());
        assertEquals(1, insuranceService.getPaymentNotified());
    }

    /**
     * Pays an amount.
     *
     * @param amount the amount to pay
     * @return the edit dialog
     */
    private CreditActEditDialog pay(BigDecimal amount) {
        FinancialAct payment = (FinancialAct) create(CustomerAccountArchetypes.PAYMENT);
        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        CustomerPaymentEditor paymentEditor = new CustomerPaymentEditor(payment, null, layout);
        CreditActEditDialog editDialog = new TestCreditActEditDialog(paymentEditor);
        pay(editDialog, amount);
        return editDialog;
    }

    private void pay(CreditActEditDialog editDialog, BigDecimal amount) {
        CustomerPaymentEditor paymentEditor = (CustomerPaymentEditor) editDialog.getEditor();
        editDialog.show();

        PaymentItemEditor itemEditor = paymentEditor.addItem();
        itemEditor.setAmount(amount);
        assertTrue(paymentEditor.isValid());

        // try and save the payment
        fireDialogButton(editDialog, PopupDialog.OK_ID);
    }

    /**
     * Verifies a claim matches that expected.
     *
     * @param claim     the claim
     * @param status    the expected status
     * @param gapStatus the expected gap status. May be {@code null}
     */
    private void checkClaim(FinancialAct claim, Claim.Status status, GapClaim.GapStatus gapStatus) {
        claim = get(claim);
        assertEquals(status.toString(), claim.getStatus());
        if (gapStatus == null) {
            assertNull(claim.getStatus2());
        } else {
            assertEquals(gapStatus.toString(), claim.getStatus2());
        }
    }

    /**
     * Verifies allocations match those expected.
     *
     * @param claim       the claim
     * @param allocations the expected allocations
     * @param amount      the expected amount
     */
    private void checkAllocation(FinancialAct claim, List<GapClaimAllocation> allocations, BigDecimal amount) {
        GapClaimAllocation result = null;
        for (GapClaimAllocation allocation : allocations) {
            if (claim.getId() == allocation.getClaim().getId()) {
                checkEquals(amount, allocation.getNewAllocation());
                result = allocation;
                break;
            }
        }
        assertNotNull(result);
    }

    /**
     * Verifies the allocated debits match those expected.
     *
     * @param dialog the allocation dialog
     * @param debits the expected debits, in allocation order
     */
    private void checkDebits(AllocationDialog dialog, FinancialAct... debits) {
        List<FinancialAct> actual = dialog.getDebits();
        assertEquals(Arrays.asList(debits), actual);
    }

    /**
     * Creates a new claim.
     *
     * @param invoiceItem the invoice item being claimed
     * @param status      the claim status
     * @return a new claim
     */
    private FinancialAct createClaim(FinancialAct invoiceItem, Claim.Status status) {
        return createClaim(invoiceItem, status, null, null);
    }

    /**
     * Creates a new claim.
     *
     * @param invoiceItem   the invoice item being claimed
     * @param status        the claim status
     * @param gapStatus     the gap status. May be {@code null}
     * @param benefitAmount the benefit amount. Ignored if {@code gapStatus} is {@code null}
     * @return a new claim
     */
    private FinancialAct createClaim(FinancialAct invoiceItem, Claim.Status status, GapClaim.GapStatus gapStatus,
                                     BigDecimal benefitAmount) {
        FinancialAct claimItem = (FinancialAct) InsuranceTestHelper.createClaimItem(invoiceItem);
        FinancialAct claim = (FinancialAct) InsuranceTestHelper.createClaim(policy, location, clinician, clinician,
                                                                            true, claimItem);
        claim.setStatus(status.toString());
        if (gapStatus != null) {
            setGapStatus(claim, gapStatus, benefitAmount);
        }
        save(policy, claim, claimItem);
        return claim;
    }

    /**
     * Sets the gap status and benefit amount.
     *
     * @param claim         the claim
     * @param gapStatus     the gap status
     * @param benefitAmount the benefit amount
     */
    private void setGapStatus(FinancialAct claim, GapClaim.GapStatus gapStatus, BigDecimal benefitAmount) {
        claim.setStatus2(gapStatus.toString());
        IMObjectBean bean = getBean(claim);
        bean.setValue("benefitAmount", benefitAmount);
    }

    /**
     * Creates and saves a POSTED invoice.
     *
     * @param amount the invoice amount
     * @return the invoice acts
     */
    private List<FinancialAct> createInvoice(BigDecimal amount) {
        Product product = TestHelper.createProduct();
        List<FinancialAct> invoice = FinancialTestHelper.createChargesInvoice(amount, customer, patient,
                                                                              product, ActStatus.POSTED);
        save(invoice);
        return invoice;
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

    private class TestCreditActEditDialog extends CreditActEditDialog {
        public TestCreditActEditDialog(CustomerPaymentEditor paymentEditor) {
            super(paymentEditor, CreditActEditDialogTestCase.this.context);
        }

        /**
         * Returns the insurance services.
         *
         * @return the insurance services
         */
        @Override
        protected InsuranceServices getInsuranceServices() {
            InsuranceServices insuranceServices = mock(InsuranceServices.class);
            when(insuranceServices.canSubmit(Mockito.any())).thenReturn(true);
            when(insuranceServices.getService(Mockito.any())).thenReturn(insuranceService);
            return insuranceServices;
        }
    }
}
