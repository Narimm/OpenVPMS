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

package org.openvpms.archetype.rules.finance.statement;

import org.apache.commons.collections4.IteratorUtils;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.AccountType;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link StatementRules}.
 *
 * @author Tim Anderson
 */
public class StatementRulesTestCase extends AbstractStatementTest {

    /**
     * The statement rules.
     */
    private StatementRules rules;

    /**
     * Sets up the test case.
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();
        rules = new StatementRules(getPractice(), getArchetypeService(), getRules());
    }

    /**
     * Tests the {@link StatementRules#getStatement(Party, Date)} and
     * {@link StatementRules#getStatementPreview(Party, Date, Date, boolean, boolean)} methods.
     */
    @Test
    public void testGetStatementPreview() {
        Party customer = getCustomer();
        BigDecimal feeAmount = new BigDecimal("25.00");
        Lookup accountType = FinancialTestHelper.createAccountType(
                30, DateUnits.DAYS, feeAmount, AccountType.FeeType.FIXED, 30, ZERO, "Test Accounting Fee");
        customer.addClassification(accountType);
        save(customer);

        List<FinancialAct> invoices1 = createChargesInvoice(BigDecimal.TEN);
        FinancialAct invoice1 = invoices1.get(0);
        invoice1.setActivityStartTime(getDatetime("2017-12-31 10:00:00"));
        save(invoices1);

        FinancialAct openingBalance = getRules().createOpeningBalance(customer, getDate("2018-01-01"), BigDecimal.TEN);
        save(openingBalance);

        List<FinancialAct> invoices2 = createChargesInvoice(MathRules.ONE_HUNDRED);
        FinancialAct invoice2 = invoices2.get(0);
        invoice2.setActivityStartTime(getDatetime("2018-01-01 10:00:00"));
        save(invoices2);

        List<FinancialAct> invoices3 = createChargesInvoice(MathRules.ONE_HUNDRED);
        FinancialAct invoice3 = invoices3.get(0);
        invoice3.setActivityStartTime(getDatetime("2018-01-01 10:30:00"));
        invoice3.setStatus(ActStatus.COMPLETED);
        save(invoices3);

        List<FinancialAct> invoices4 = createChargesInvoice(BigDecimal.TEN);
        FinancialAct invoice4 = invoices4.get(0);
        invoice4.setActivityStartTime(getDatetime("2018-01-01 11:00:00"));
        invoice4.setStatus(ActStatus.IN_PROGRESS);
        save(invoices4);

        List<FinancialAct> invoices5 = createChargesInvoice(BigDecimal.TEN);
        FinancialAct invoice5 = invoices5.get(0);
        invoice5.setActivityStartTime(getDatetime("2018-01-03 11:00:00"));
        invoice5.setStatus(ActStatus.POSTED);
        save(invoices5);

        // process the customer's statement. Should just return the POSTED and COMPLETED invoice acts.
        // The invoice1 and invoice5 invoices won't be included as they are outside the range.
        checkStatementPreview(customer, getDate("2018-01-01"), getDate("2018-01-02"), openingBalance, invoice2,
                              invoice3);
        // this time, just pass the to date. Should work back to the opening balance
        checkStatementPreview(customer, getDate("2018-01-02"), openingBalance, invoice2, invoice3);

        // process the customer's statement for 5/2. Amounts should be overdue
        // and a fee generated.
        List<FinancialAct> acts1 = getStatementPreview(customer, getDate("2018-01-01"), getDate("2018-02-05"));
        assertEquals(5, acts1.size());
        checkActs(acts1.subList(0, 4), openingBalance, invoice2, invoice3, invoice5);
        checkFee(acts1.get(4), feeAmount);

        // now just pass the to date. Should work back to the opening balance
        List<FinancialAct> acts2 = getStatementPreview(customer, getDate("2018-02-05"), true, true);
        assertEquals(5, acts2.size());
        checkActs(acts2.subList(0, 4), openingBalance, invoice2, invoice3, invoice5);
        checkFee(acts2.get(4), feeAmount);

        // now exclude completed charges and the fee
        List<FinancialAct> acts3 = getStatementPreview(customer, getDate("2018-02-05"), false, false);
        checkActs(acts3, openingBalance, invoice2, invoice5);
    }

    /**
     * Tests the {@link StatementRules#getStatement(Party, Date)} and
     * {@link StatementRules#getStatement(Party, Date, Date)} and
     * {@link StatementRules#getStatementRange(Party, Date, Date)} methods.
     */
    @Test
    public void testGetStatement() {
        IArchetypeService service = getArchetypeService();
        Party customer = getCustomer();

        // create an invoice
        Money amount = new Money(950);
        List<FinancialAct> invoices1 = createChargesInvoice(amount);
        FinancialAct invoice1 = invoices1.get(0);
        invoice1.setActivityStartTime(getDatetime("2017-12-29 10:00:00"));
        save(invoices1);

        // run EOP for 31/12/2017.
        Date statementDate1 = getDate("2017-12-31");
        EndOfPeriodProcessor eop = new EndOfPeriodProcessor(statementDate1, true, getPractice(), service, accountRules);
        eop.process(customer);

        // create a payment for 14/1/2018
        FinancialAct payment = createPayment(amount);
        payment.setActivityStartTime(getDatetime("2018-01-14 14:52:00"));
        save(payment);

        FinancialAct opening1 = getRules().getOpeningBalanceAfter(customer, statementDate1);
        assertNotNull(opening1);
        checkEquals(amount, opening1.getTotal());

        // backdate the statement to 1/1/2018. Should only include the opening balance
        Date statementDate3 = getDate("2018-01-01");
        checkStatement(customer, opening1.getActivityStartTime(), statementDate3, opening1);

        // now just pass the statement date. Should give the same results
        checkStatement(customer, statementDate3, opening1);

        // now forward to the payment date. Statement should include opening balance and payment
        Date statementDate4 = getDate("2018-01-14");
        checkStatement(customer, opening1.getActivityStartTime(), statementDate4, opening1, payment);

        // again, just passing the statement date
        checkStatement(customer, statementDate4, opening1, payment);

        // run EOP for the 31/1
        Date statementDate5 = getDate("2018-01-31");
        eop = new EndOfPeriodProcessor(statementDate5, true, getPractice(), service, accountRules);
        eop.process(customer);

        FinancialAct opening2 = getRules().getOpeningBalanceAfter(customer, statementDate5);
        assertNotNull(opening2);
        checkEquals(ZERO, opening2.getTotal());

        // check statement for 1/2.
        Date statementDate6 = getDate("2018-02-01");
        checkStatement(customer, statementDate6, opening2);

        // backdate the statement to 14/1/2018. Should only include the opening balance and payment
        checkStatement(customer, statementDate4, opening1, payment);
        checkStatement(customer, opening1.getActivityStartTime(), statementDate4, opening1, payment);

        // now check ranges
        checkStatementRange(customer, null, null, invoice1, payment);
        checkStatementRange(customer, getDate("2017-12-31"), null, amount, payment);  // dummy opening balance inserted
        checkStatementRange(customer, getDate("2017-12-31"), getDate("2018-01-05"), amount);          // "" ""
        checkStatementRange(customer, getDate("2017-12-31"), getDate("2018-01-15"), amount, payment); // "" ""
    }

    /**
     * Verifies a fee matches that expected.
     *
     * @param fee       the fee act
     * @param feeAmount the expected fee amount
     */
    private void checkFee(FinancialAct fee, BigDecimal feeAmount) {
        // check the fee. This should not have been saved
        checkDebitAdjust(fee, feeAmount, "Test Accounting Fee");
        assertTrue(fee.isNew());
    }

    /**
     * Helper to call {@link StatementRules#getStatementPreview(Party, Date, Date, boolean, boolean)} and verify the results match
     * those expected.
     *
     * @param customer the customer
     * @param from     the from date. If non-null, should corresponding to an opening balance timestamp
     * @param to       the to date. This corresponds to the statement date
     * @param expected the expected acts
     */
    private void checkStatementPreview(Party customer, Date from, Date to, FinancialAct... expected) {
        List<FinancialAct> actual = getStatementPreview(customer, from, to);
        checkActs(actual, expected);
    }

    private List<FinancialAct> getStatementPreview(Party customer, Date from, Date to) {
        return IteratorUtils.toList(rules.getStatementPreview(customer, from, to, true, true).iterator());
    }

    /**
     * Helper to call {@link StatementRules#getStatementPreview(Party, Date, Date, boolean, boolean)} and verify the
     * results match those expected.
     *
     * @param customer the customer
     * @param date     the statement date, used to calculate the account fee
     * @param expected the expected acts
     */
    private void checkStatementPreview(Party customer, Date date, FinancialAct... expected) {
        List<FinancialAct> actual = getStatementPreview(customer, date, true, true);
        checkActs(actual, expected);
    }

    /**
     * Preview acts that will be in a customer's next statement.
     *
     * @param customer                the customer
     * @param date                    the statement date, used to calculate the account fee
     * @param includeCompletedCharges if {@code true} include COMPLETED charges
     * @param includeFee              if {@code true}, include an accounting fee if one is required
     * @return the statement acts
     */
    private List<FinancialAct> getStatementPreview(Party customer, Date date, boolean includeCompletedCharges,
                                                   boolean includeFee) {
        Iterable<FinancialAct> preview = rules.getStatementPreview(customer, date, includeCompletedCharges, includeFee);
        return IteratorUtils.toList(preview.iterator());
    }

    private List<FinancialAct> getStatement(Party customer, Date date) {
        return IteratorUtils.toList(rules.getStatement(customer, date).iterator());
    }

    private void checkStatement(Party customer, Date date, FinancialAct... expected) {
        List<FinancialAct> actual = getStatement(customer, date);
        checkActs(actual, expected);
    }

    private void checkStatement(Party customer, Date from, Date to, FinancialAct... expected) {
        List<FinancialAct> actual = IteratorUtils.toList(rules.getStatement(customer, from, to).iterator());
        checkActs(actual, expected);
    }

    private void checkStatementRange(Party customer, Date from, Date to, FinancialAct... expected) {
        List<FinancialAct> actual = IteratorUtils.toList(rules.getStatementRange(customer, from, to).iterator());
        checkActs(actual, expected);
    }

    private void checkStatementRange(Party customer, Date from, Date to, BigDecimal balance,
                                     FinancialAct... expected) {
        List<FinancialAct> actual = IteratorUtils.toList(rules.getStatementRange(customer, from, to).iterator());
        assertEquals(expected.length + 1, actual.size());
        FinancialAct opening = actual.remove(0);
        assertEquals(from, opening.getActivityStartTime());
        checkEquals(balance, opening.getTotal());
        checkActs(actual, expected);
    }


    private void checkActs(List<FinancialAct> actual, FinancialAct... expected) {
        assertEquals(expected.length, actual.size());
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], actual.get(i));
        }
    }

}
