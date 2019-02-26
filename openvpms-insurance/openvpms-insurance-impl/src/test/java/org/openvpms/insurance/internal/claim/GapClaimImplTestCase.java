package org.openvpms.insurance.internal.claim;

import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.finance.credit.CreditActAllocator;
import org.openvpms.archetype.rules.insurance.InsuranceTestHelper;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.claim.GapClaim;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.openvpms.archetype.rules.insurance.InsuranceTestHelper.createClaimItem;
import static org.openvpms.archetype.rules.math.MathRules.ONE_HUNDRED;

/**
 * Tests the {@link GapClaimImpl} class.
 *
 * @author Tim Anderson
 */
public class GapClaimImplTestCase extends AbstractClaimTest {


    /**
     * The till used for payments.
     */
    private Party till;

    /**
     * Sets up the test case.
     */
    @Override
    public void setUp() {
        super.setUp();
        till = TestHelper.createTill(location);
    }

    /**
     * Verifies that paying the gap generates an adjustment.
     */
    @Test
    public void testPayGap() {
        checkPayClaim(true, BigDecimal.valueOf(80));
    }

    /**
     * Verifies that paying the gap generates an adjustment.
     */
    @Test
    public void testPayFullClaim() {
        checkPayClaim(false, BigDecimal.valueOf(80));
    }

    /**
     * Verifies that no adjustment is created if there is a zero benefit amount.
     */
    @Test
    public void testZeroBenefit() {
        checkPayClaim(true, ZERO);
        checkPayClaim(false, ZERO);
    }

    /**
     * Checks payment of a gap claim.
     *
     * When a credit adjustment is created, verifies that it is allocated against the invoice.
     *
     * @param payGap  if {@code true}, pay the gap, otherwise pay the full claim
     * @param benefit the benefit
     */
    protected void checkPayClaim(boolean payGap, BigDecimal benefit) {
        BigDecimal total = BigDecimal.valueOf(95);
        BigDecimal gap = total.subtract(benefit);
        Product product1 = TestHelper.createProduct();
        BigDecimal tax = new BigDecimal("8.64");
        BigDecimal discount = BigDecimal.valueOf(5);

        FinancialAct invoiceItem1 = createInvoiceItem(new Date(), product1, ONE, ONE_HUNDRED, discount,
                                                      tax);
        List<FinancialAct> invoice1Acts = createInvoice(new Date(), invoiceItem1);
        FinancialAct invoice1 = invoice1Acts.get(0);
        save(invoice1Acts);

        FinancialAct invoiceItem2 = createInvoiceItem(new Date(), product1, ONE, ONE_HUNDRED, discount,
                                                      tax);
        List<FinancialAct> invoice2Acts = createInvoice(new Date(), invoiceItem2);
        FinancialAct invoice2 = invoice2Acts.get(0);
        save(invoice2Acts);

        FinancialAct item1Act = (FinancialAct) createClaimItem(invoiceItem2);
        FinancialAct claimAct = (FinancialAct) InsuranceTestHelper.createClaim(policyAct, location, clinician, user,
                                                                               true, item1Act);
        save(claimAct, item1Act);

        GapClaimImpl claim = createGapClaim(claimAct);

        // claim not submitted
        assertEquals(Claim.Status.PENDING, claim.getStatus());
        assertEquals(GapClaim.GapStatus.PENDING, claim.getGapStatus());
        checkEquals(total, claim.getTotal());
        checkEquals(ZERO, claim.getBenefitAmount());
        checkEquals(total, claim.getGapAmount());
        checkEquals(discount, claim.getDiscount());
        checkEquals(tax, claim.getTotalTax());
        assertNull(claim.getBenefitNotes());

        // simulate claim acceptance.
        claim.setStatus(Claim.Status.ACCEPTED);
        claim.setBenefit(benefit, "Accepted");

        assertEquals(GapClaim.GapStatus.RECEIVED, claim.getGapStatus());
        assertEquals("Accepted", claim.getBenefitNotes());
        checkEquals(benefit, claim.getBenefitAmount());
        checkEquals(gap, claim.getGapAmount());
        checkEquals(total, claim.getTotal());

        FinancialAct adjustment = null;

        BigDecimal pay = (payGap) ? gap : total;

        // allocate the payment against the claim
        FinancialAct payment = createPayment(pay); //
        CreditActAllocator allocator = new CreditActAllocator(getArchetypeService(), insuranceRules);
        List<FinancialAct> updated = allocator.allocate(payment, invoice2);
        assertEquals(2, updated.size());
        save(updated);

        // check allocations
        checkAllocation(payment, pay);
        checkAllocation(invoice2, pay);
        checkAllocation(invoice1, ZERO);

        if (payGap) {
            // now record the gap as being paid
            adjustment = claim.gapPaid(practice, location, user, "Some notes");
        } else {
            claim.fullyPaid();
        }

        assertEquals(GapClaim.GapStatus.PAID, claim.getGapStatus());

        if (payGap) {
            checkEquals(gap, claim.getPaid());
            if (!MathRules.isZero(benefit)) {
                assertNotNull(adjustment);
                assertEquals(ActStatus.POSTED, adjustment.getStatus());
                checkEquals(benefit, adjustment.getTotal());
                checkEquals(new BigDecimal("7.273"), adjustment.getTaxAmount());
                IMObjectBean bean = getBean(adjustment);
                assertEquals(user, bean.getTarget("author"));
                assertEquals("Some notes", bean.getString("notes"));

                checkAllocation(adjustment, benefit); // adjustment should be fully allocated
            } else {
                assertNull(adjustment);
            }
        } else {
            checkEquals(total, claim.getPaid());
        }

        // check allocations
        checkAllocation(payment, pay);
        checkAllocation(invoice2, total);
        checkAllocation(invoice1, ZERO);
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

    /**
     * Verifies that the latest instance of an act is allocated correctly.
     *
     * @param act      the act
     * @param expected the expected allocated amount
     */
    private void checkAllocation(FinancialAct act, BigDecimal expected) {
        checkEquals(expected, get(act).getAllocatedAmount());
    }

}
