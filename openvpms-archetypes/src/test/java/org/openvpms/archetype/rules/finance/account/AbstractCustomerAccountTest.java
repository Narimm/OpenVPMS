/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.finance.account;

import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.math.BigDecimal;
import java.util.Date;


/**
 * Base class for customer account test cases.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractCustomerAccountTest extends ArchetypeServiceTest {

    /**
     * The customer.
     */
    private Party customer;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The product.
     */
    private Product product;

    /**
     * The till.
     */
    private Party till;


    /**
     * Returns the customer.
     *
     * @return the customer
     */
    protected Party getCustomer() {
        if (customer == null) {
            customer = TestHelper.createCustomer();
        }
        return customer;
    }

    /**
     * Returns the patient.
     *
     * @return the patient
     */
    protected Party getPatient() {
        if (patient == null) {
            patient = TestHelper.createPatient();
        }
        return patient;
    }

    /**
     * Returns the product.
     *
     * @return the product
     */
    protected Product getProduct() {
        if (product == null) {
            product = TestHelper.createProduct();
        }
        return product;
    }

    /**
     * Returns the till.
     *
     * @return the till
     */
    protected Party getTill() {
        if (till == null) {
            till = FinancialTestHelper.createTill();
        }
        return till;
    }

    /**
     * Helper to create an <em>act.customerAccountChargesInvoice</em>.
     *
     * @param amount the act total
     * @return a new act
     */
    protected FinancialAct createChargesInvoice(Money amount) {
        return createChargesInvoice(amount, getCustomer());
    }

    /**
     * Helper to create an <em>act.customerAccountChargesInvoice</em>.
     *
     * @param amount   the act total
     * @param customer the customer
     * @return a new act
     */
    protected FinancialAct createChargesInvoice(Money amount, Party customer) {
        return FinancialTestHelper.createChargesInvoice(amount, customer,
                                                        getPatient(),
                                                        getProduct());
    }

    /**
     * Helper to create an <em>act.customerAccountChargesInvoice</em>.
     *
     * @param amount    the act total
     * @param startTime the act start time
     * @return a new act
     */
    protected FinancialAct createChargesInvoice(Money amount,
                                                Date startTime) {
        FinancialAct invoice = createChargesInvoice(amount);
        invoice.setActivityStartTime(startTime);
        return invoice;
    }

    /**
     * Helper to create an <em>act.customerAccountChargesCounter</em>.
     *
     * @param amount the act total
     * @return a new act
     */
    protected FinancialAct createChargesCounter(Money amount) {
        return FinancialTestHelper.createChargesCounter(amount,
                                                        getCustomer(),
                                                        getProduct());
    }

    /**
     * Helper to create an <em>act.customerAccountChargesCredit</em>.
     *
     * @param amount the act total
     * @return a new act
     */
    protected FinancialAct createChargesCredit(Money amount) {
        return FinancialTestHelper.createChargesCredit(amount, getCustomer(),
                                                       getPatient(),
                                                       getProduct());
    }

    /**
     * Helper to create an <em>act.customerAccountPayment</em>.
     *
     * @param amount the act total
     * @return a new payment
     */
    protected FinancialAct createPayment(Money amount) {
        return createPayment(amount, getCustomer());
    }

    /**
     * Helper to create an <em>act.customerAccountPayment</em>.
     *
     * @param amount   the act total
     * @param customer the customer
     * @return a new payment
     */
    protected FinancialAct createPayment(Money amount, Party customer) {
        return FinancialTestHelper.createPayment(amount, customer, getTill());
    }

    /**
     * Helper to create an <em>act.customerAccountPayment</em>.
     *
     * @param amount    the act total
     * @param startTime the act start time
     * @return a new act
     */
    protected FinancialAct createPayment(Money amount, Date startTime) {
        FinancialAct payment = createPayment(amount);
        payment.setActivityStartTime(startTime);
        return payment;
    }

    /**
     * Helper to create an <em>act.customerAccountPayment</em> containing an
     * <em>act.customerAccountPaymentCash</em>.
     */
    protected FinancialAct createPaymentCash(Money amount) {
        return FinancialTestHelper.createPaymentCash(amount, getCustomer(),
                                                     getTill());
    }

    /**
     * Helper to create an <em>act.customerAccountPayment</em> containing an
     * <em>act.customerAccountPaymentCheque</em>.
     */
    protected FinancialAct createPaymentCheque(Money amount) {
        return FinancialTestHelper.createPaymentCheque(amount, getCustomer(),
                                                       getTill());
    }

    /**
     * Helper to create an <em>act.customerAccountPayment</em> containing an
     * <em>act.customerAccountPaymentCredit</em>.
     */
    protected FinancialAct createPaymentCredit(Money amount) {
        return FinancialTestHelper.createPaymentCredit(amount, getCustomer(),
                                                       getTill());
    }

    /**
     * Helper to create an <em>act.customerAccountPayment</em> containing an
     * <em>act.customerAccountPaymentDiscount</em>.
     */
    protected FinancialAct createPaymentDiscount(Money amount) {
        return FinancialTestHelper.createPaymentDiscount(amount, getCustomer(),
                                                         getTill());
    }

    /**
     * Helper to create an <em>act.customerAccountPayment</em> containing an
     * <em>act.customerAccountPaymentEFT</em>.
     */
    protected FinancialAct createPaymentEFT(Money amount) {
        return FinancialTestHelper.createPaymentEFT(amount, getCustomer(),
                                                    getTill());
    }

    /**
     * Helper to create an <em>act.customerAccountRefund</em>.
     *
     * @param amount the act total
     * @return a new payment
     */
    protected FinancialAct createRefund(Money amount) {
        return FinancialTestHelper.createRefund(amount, getCustomer(),
                                                getTill());
    }

    /**
     * Helper to create an <em>act.customerAccountRefund</em> containing an
     * <em>act.customerAccountRefundCash</em>.
     *
     * @param amount the act total
     * @return a new payment
     */
    protected FinancialAct createRefundCash(Money amount) {
        return FinancialTestHelper.createRefundCash(amount, getCustomer(),
                                                    getTill());
    }

    /**
     * Helper to create an <em>act.customerAccountRefund</em> containing an
     * <em>act.customerAccountRefundCheque</em>.
     *
     * @param amount the act total
     * @return a new payment
     */
    protected FinancialAct createRefundCheque(Money amount) {
        return FinancialTestHelper.createRefundCheque(amount, getCustomer(),
                                                      getTill());
    }

    /**
     * Helper to create an <em>act.customerAccountRefund</em> containing an
     * <em>act.customerAccountRefundCredit</em>.
     *
     * @param amount the act total
     * @return a new payment
     */
    protected FinancialAct createRefundCredit(Money amount) {
        return FinancialTestHelper.createRefundCredit(amount, getCustomer(),
                                                      getTill());
    }

    /**
     * Helper to create an <em>act.customerAccountRefund</em> containing an
     * <em>act.customerAccountRefundDiscount</em>.
     *
     * @param amount the act total
     * @return a new payment
     */
    protected FinancialAct createRefundDiscount(Money amount) {
        return FinancialTestHelper.createRefundDiscount(amount, getCustomer(),
                                                        getTill());
    }

    /**
     * Helper to create an <em>act.customerAccountRefund</em> containing an
     * <em>act.customerAccountRefundEFT</em>.
     *
     * @param amount the act total
     * @return a new payment
     */
    protected FinancialAct createRefundEFT(Money amount) {
        return FinancialTestHelper.createRefundEFT(amount, getCustomer(),
                                                   getTill());
    }

    /**
     * Helper to create an <em>act.customerAccountCreditAdjust</em>.
     *
     * @param amount the act total
     * @return a new credit adjustment
     */
    protected FinancialAct createCreditAdjust(Money amount) {
        return createAct("act.customerAccountCreditAdjust", amount);
    }

    /**
     * Helper to create an <em>act.customerAccountDebitAdjust</em>.
     *
     * @param amount the act total
     * @return a new debit adjustment
     */
    protected FinancialAct createDebitAdjust(Money amount) {
        return createAct("act.customerAccountDebitAdjust", amount);
    }

    /**
     * Helper to create an <em>act.customerAccountInitialBalance</em>.
     *
     * @param amount the act total
     * @return a new initial balance
     */
    protected FinancialAct createInitialBalance(Money amount) {
        return createAct("act.customerAccountInitialBalance", amount);
    }

    /**
     * Helper to create an <em>act.customerAccountBadDebt</em>.
     *
     * @param amount the act total
     * @return a new bad debt
     */
    protected FinancialAct createBadDebt(Money amount) {
        return createAct("act.customerAccountBadDebt", amount);
    }

    /**
     * Helper to create and save a new <em>lookup.customerAccountType</em>
     * classification.
     *
     * @param paymentTerms the payment terms
     * @param paymentUom   the payment units
     * @return a new classification
     */
    protected Lookup createAccountType(int paymentTerms, DateUnits paymentUom) {
        return createAccountType(paymentTerms, paymentUom, BigDecimal.ZERO);
    }

    /**
     * Helper to create and save a new <em>lookup.customerAccountType</em>
     * classification.
     *
     * @param paymentTerms     the payment terms
     * @param paymentUom       the payment units
     * @param accountFeeAmount the account fee
     * @return a new classification
     */
    protected Lookup createAccountType(int paymentTerms, DateUnits paymentUom,
                                       BigDecimal accountFeeAmount) {
        return FinancialTestHelper.createAccountType(paymentTerms, paymentUom,
                                                     accountFeeAmount);
    }

    /**
     * Helper to create a new act, setting the total, adding a customer
     * participation and setting the status to 'POSTED'.
     *
     * @param shortName the act short name
     * @param amount    the act total
     * @return a new act
     */
    private FinancialAct createAct(String shortName, Money amount) {
        return createAct(shortName, amount, getCustomer());
    }

    /**
     * Helper to create a new act, setting the total, adding a customer
     * participation and setting the status to 'POSTED'.
     *
     * @param shortName the act short name
     * @param amount    the act total
     * @param customer  the customer
     * @return a new act
     */
    private FinancialAct createAct(String shortName, Money amount,
                                   Party customer) {
        FinancialAct act = (FinancialAct) create(shortName);
        act.setStatus(FinancialActStatus.POSTED);
        act.setTotal(amount);
        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.customer", customer);
        return act;
    }

}
