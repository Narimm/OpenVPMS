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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.account;

import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.COUNTER;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.COUNTER_ITEM;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.CREDIT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.CREDIT_ITEM;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.INVOICE;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.INVOICE_ITEM;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT_CASH;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT_CHEQUE;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT_CREDIT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT_DISCOUNT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT_EFT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT_OTHER;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND_CASH;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND_CHEQUE;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND_CREDIT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND_DISCOUNT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND_EFT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND_OTHER;


/**
 * Financial test helper.
 *
 * @author Tim Anderson
 */
public class FinancialTestHelper extends TestHelper {

    /**
     * Helper to create a new <em>act.customerAccountChargesInvoice</em>
     * and corresponding <em>act.customerAccountInvoiceItem</em>.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param patient  the patient
     * @param product  the product
     * @param status   the act status
     * @return a list containing the invoice act and its item
     */
    public static List<FinancialAct> createChargesInvoice(BigDecimal amount, Party customer, Party patient,
                                                          Product product, String status) {
        return createChargesInvoice(customer, patient, product, ONE, ZERO, amount, ZERO, status);
    }

    /**
     * Helper to create a new <em>act.customerAccountChargesInvoice</em>
     * and corresponding <em>act.customerAccountInvoiceItem</em>.
     *
     * @param customer   the customer
     * @param patient    the patient
     * @param product    the product
     * @param quantity   the quantity
     * @param fixedPrice the fixed price
     * @param unitPrice  the unit price
     * @param tax        the tax
     * @param status     the act status
     * @return a list containing the invoice act and its item
     */
    public static List<FinancialAct> createChargesInvoice(Party customer,
                                                          Party patient, Product product, BigDecimal quantity,
                                                          BigDecimal fixedPrice, BigDecimal unitPrice, BigDecimal tax,
                                                          String status) {
        return createChargesInvoice(customer, patient, null, product, quantity, fixedPrice, unitPrice, tax, status);
    }

    /**
     * Helper to create a new <em>act.customerAccountChargesInvoice</em>
     * and corresponding <em>act.customerAccountInvoiceItem</em>.
     *
     * @param customer   the customer
     * @param patient    the patient
     * @param clinician  the clinician. May be {@code null}
     * @param product    the product
     * @param quantity   the quantity
     * @param fixedPrice the fixed price
     * @param unitPrice  the unit price
     * @param tax        the tax
     * @param status     the act status
     * @return a list containing the invoice act and its item
     */
    public static List<FinancialAct> createChargesInvoice(Party customer, Party patient, User clinician,
                                                          Product product, BigDecimal quantity, BigDecimal fixedPrice,
                                                          BigDecimal unitPrice, BigDecimal tax, String status) {
        return createCharges(INVOICE, INVOICE_ITEM, customer, patient, clinician, product, quantity, fixedPrice,
                             unitPrice, tax, status);
    }


    /**
     * Helper to create a new <em>act.customerAccountChargesCounter</em>
     * and corresponding <em>act.customerAccountCounterItem</em>.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param product  the product
     * @param status   the status
     * @return a list containing the counter act and its item
     */
    public static List<FinancialAct> createChargesCounter(BigDecimal amount, Party customer, Product product,
                                                          String status) {
        return createCharges(COUNTER, COUNTER_ITEM, amount, customer, null, product, status);
    }

    /**
     * Helper to create a new <em>act.customerAccountChargesCredit</em>
     * and corresponding <em>act.customerAccountCreditItem</em>.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param patient  the patient. May be {@code null}
     * @param product  the product
     * @param status   the act statues
     * @return a list containing the credit act and its item
     */
    public static List<FinancialAct> createChargesCredit(BigDecimal amount, Party customer, Party patient,
                                                         Product product, String status) {
        List<FinancialAct> result = createCharges(CREDIT, CREDIT_ITEM, amount, customer, patient, product, status);
        ActBean bean = new ActBean(result.get(0));
        bean.setValue("notes", "Dummy notes");
        return result;
    }

    /**
     * Creates a new <em>act.customerAmountPayment</em>.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param till     the till
     * @param status   the status
     * @return a new payment
     */
    public static FinancialAct createPayment(BigDecimal amount, Party customer, Party till, String status) {
        return createPaymentRefund(PAYMENT, amount, customer, till, status);
    }

    /**
     * Helper to create a new <em>act.customerAccountPayment</em> and
     * corresponding <em>act.customerAccountPaymentCash</em>.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param till     the till
     * @param status   the status
     * @return a list containing the refund act and its item
     */
    public static List<FinancialAct> createPaymentCash(BigDecimal amount, Party customer, Party till, String status) {
        return createPaymentRefund(PAYMENT, PAYMENT_CASH, amount, customer, till, status, false);
    }

    /**
     * Helper to create a new <em>act.customerAccountRefund</em> and
     * corresponding <em>act.customerAccountRefundCash</em>.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param till     the till
     * @param status   the status
     * @return a list containing the refund act and its item
     */
    public static List<FinancialAct> createRefundCash(BigDecimal amount, Party customer, Party till, String status) {
        List<FinancialAct> result = createPaymentRefund(REFUND, REFUND_CASH, amount, customer, till, status, false);
        ActBean bean = new ActBean(result.get(0));
        bean.setValue("notes", "Dummy notes");
        return result;
    }

    /**
     * Helper to create a POSTED <em>act.customerAccountPayment</em>
     * with a single <em>act.customerAccountPaymentCash</em> item.
     * The <em>roundedAmount</em> and <em>tendered</em> nodes are set to
     * the same value as <em>amount</em>.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param till     the till
     * @return a new act
     */
    public static FinancialAct createPaymentCash(BigDecimal amount, Party customer, Party till) {
        return createPaymentRefund(PAYMENT, PAYMENT_CASH, amount, customer, till);
    }

    /**
     * Helper to create a POSTED <em>act.customerAccountPayment</em>
     * with a single <em>act.customerAccountPaymentCheque</em> item.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param till     the till
     * @return a new act
     */
    public static FinancialAct createPaymentCheque(BigDecimal amount, Party customer, Party till) {
        return createPaymentRefund(PAYMENT, PAYMENT_CHEQUE, amount, customer, till);
    }

    /**
     * Helper to create a POSTED <em>act.customerAccountPayment</em>
     * with a single <em>act.customerAccountPaymentCredit</em> item.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param till     the till
     * @return a new act
     */
    public static FinancialAct createPaymentCredit(BigDecimal amount, Party customer, Party till) {
        return createPaymentRefund(PAYMENT, PAYMENT_CREDIT, amount, customer, till);
    }

    /**
     * Helper to create a POSTED <em>act.customerAccountPayment</em>
     * with a single <em>act.customerAccountPaymentDiscount</em> item.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param till     the till
     * @return a new act
     */
    public static FinancialAct createPaymentDiscount(BigDecimal amount, Party customer, Party till) {
        return createPaymentRefund(PAYMENT, PAYMENT_DISCOUNT, amount, customer, till);
    }

    /**
     * Helper to create a POSTED <em>act.customerAccountPayment</em>
     * with a single <em>act.customerAccountPaymentEFT</em> item.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param till     the till
     * @return a new act
     */
    public static FinancialAct createPaymentEFT(BigDecimal amount, Party customer, Party till) {
        return createPaymentRefund(PAYMENT, PAYMENT_EFT, amount, customer, till);
    }

    /**
     * Helper to create a POSTED <em>act.customerAccountPayment</em>
     * with a single <em>act.customerAccountPaymentOther</em> item.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param till     the till
     * @return a new act
     */
    public static FinancialAct createPaymentOther(BigDecimal amount, Party customer, Party till) {
        return createPaymentRefund(PAYMENT, PAYMENT_OTHER, amount, customer, till);
    }

    /**
     * Helper to create a <em>act.customerAccountRefund</em>.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param till     the till
     * @return a new act
     */
    public static FinancialAct createRefund(BigDecimal amount, Party customer, Party till, String status) {
        return createPaymentRefund(CustomerAccountArchetypes.REFUND, amount, customer, till, status);
    }

    /**
     * Helper to create a POSTED <em>act.customerAccountRefund</em> with
     * a single <em>act.customerAccountRefundCash</em> item.
     * The <em>roundedAmount</em> node is set to the same value as
     * <em>amount</em>.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param till     the till
     * @return a new act
     */
    public static FinancialAct createRefundCash(BigDecimal amount, Party customer, Party till) {
        FinancialAct result = createPaymentRefund(REFUND, REFUND_CASH, amount, customer, till);
        ActBean bean = new ActBean(result);
        bean.setValue("notes", "Dummy notes");
        return result;
    }

    /**
     * Helper to create a POSTED <em>act.customerAccountRefund</em> with
     * a single <em>act.customerAccountRefundCheque</em> item.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param till     the till
     * @return a new act
     */
    public static FinancialAct createRefundCheque(BigDecimal amount, Party customer, Party till) {
        FinancialAct result = createPaymentRefund(REFUND, REFUND_CHEQUE, amount, customer, till);
        ActBean bean = new ActBean(result);
        bean.setValue("notes", "Dummy notes");
        return result;
    }

    /**
     * Helper to create a POSTED <em>act.customerAccountRefund</em> with
     * a single <em>act.customerAccountRefundCredit</em> item.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param till     the till
     * @return a new act
     */
    public static FinancialAct createRefundCredit(BigDecimal amount, Party customer, Party till) {
        FinancialAct result = createPaymentRefund(REFUND, REFUND_CREDIT, amount, customer, till);
        ActBean bean = new ActBean(result);
        bean.setValue("notes", "Dummy notes");
        return result;
    }

    /**
     * Helper to create a POSTED <em>act.customerAccountRefund</em> with
     * a single <em>act.customerAccountRefundDiscount</em> item.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param till     the till
     * @return a new act
     */
    public static FinancialAct createRefundDiscount(BigDecimal amount, Party customer, Party till) {
        FinancialAct result = createPaymentRefund(REFUND, REFUND_DISCOUNT, amount, customer, till);
        ActBean bean = new ActBean(result);
        bean.setValue("notes", "Dummy notes");
        return result;
    }

    /**
     * Helper to create a POSTED <em>act.customerAccountRefund</em> with
     * a single <em>act.customerAccountRefundEFT</em> item.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param till     the till
     * @return a new act
     */
    public static FinancialAct createRefundEFT(BigDecimal amount, Party customer, Party till) {
        FinancialAct result = createPaymentRefund(REFUND, REFUND_EFT, amount, customer, till);
        ActBean bean = new ActBean(result);
        bean.setValue("notes", "Dummy notes");
        return result;
    }

    /**
     * Helper to create a POSTED <em>act.customerAccountRefund</em> with
     * a single <em>act.customerAccountRefundOther</em> item.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param till     the till
     * @return a new act
     */
    public static FinancialAct createRefundOther(BigDecimal amount, Party customer, Party till) {
        FinancialAct result = createPaymentRefund(REFUND, REFUND_OTHER, amount, customer, till);
        ActBean bean = new ActBean(result);
        bean.setValue("notes", "Dummy notes");
        return result;
    }

    /**
     * Helper to create a charge item.
     *
     * @param itemShortName the charge item short name
     * @param patient       the patient. May be {@code null}
     * @param product       the product. May be {@code null}
     * @param unitPrice     the the unit price
     * @return a new charge item
     */
    public static FinancialAct createChargeItem(String itemShortName, Party patient, Product product,
                                                BigDecimal unitPrice) {
        return createChargeItem(itemShortName, patient, null, product, ONE, unitPrice, ZERO, ZERO);
    }

    /**
     * Helper to create a charge item.
     *
     * @param itemShortName the charge item short name
     * @param patient       the patient. May be {@code null}
     * @param clinician     the clinician. May be {@code null}
     * @param product       the product. May be {@code null}
     * @param quantity      the quantity
     * @param fixedPrice    the fixed price
     * @param unitPrice     the unit price
     * @param tax           the tax
     * @return a new charge item
     */
    public static FinancialAct createChargeItem(String itemShortName, Party patient, User clinician, Product product,
                                                BigDecimal quantity, BigDecimal fixedPrice, BigDecimal unitPrice,
                                                BigDecimal tax) {
        FinancialAct item = (FinancialAct) create(itemShortName);
        ActBean itemBean = new ActBean(item);
        if (patient != null) {
            itemBean.addNodeParticipation("patient", patient);
        }
        if (clinician != null) {
            itemBean.addNodeParticipation("clinician", clinician);
        }
        if (product != null) {
            itemBean.addNodeParticipation("product", product);
        }
        itemBean.setValue("quantity", quantity);
        itemBean.setValue("fixedPrice", fixedPrice);
        itemBean.setValue("unitPrice", unitPrice);
        itemBean.setValue("tax", tax);
        return item;
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
    public static Lookup createAccountType(int paymentTerms, DateUnits paymentUom, BigDecimal accountFeeAmount) {
        return createAccountType(paymentTerms, paymentUom, accountFeeAmount, 0);
    }

    /**
     * Helper to create and save a new <em>lookup.customerAccountType</em>
     * classification.
     *
     * @param paymentTerms     the payment terms
     * @param paymentUom       the payment units
     * @param accountFeeAmount the account fee
     * @param accountFeeDays   the account fee days
     * @return a new classification
     */
    public static Lookup createAccountType(int paymentTerms, DateUnits paymentUom, BigDecimal accountFeeAmount,
                                           int accountFeeDays) {
        return createAccountType(paymentTerms, paymentUom, accountFeeAmount, AccountType.FeeType.FIXED, accountFeeDays,
                                 ZERO);
    }

    /**
     * Helper to create and save a new <em>lookup.customerAccountType</em>
     * classification.
     *
     * @param paymentTerms     the payment terms
     * @param paymentUom       the payment units
     * @param accountFeeAmount the account fee
     * @param accountFeeType   the account fee type
     * @param accountFeeDays   the account fee days
     * @param feeBalance       the minimum balance when an account fee applies
     * @return a new classification
     */
    public static Lookup createAccountType(int paymentTerms, DateUnits paymentUom, BigDecimal accountFeeAmount,
                                           AccountType.FeeType accountFeeType, int accountFeeDays,
                                           BigDecimal feeBalance) {
        Lookup lookup = (Lookup) create("lookup.customerAccountType");
        IMObjectBean bean = new IMObjectBean(lookup);
        bean.setValue("code", "XCUSTOMER_ACCOUNT_TYPE" + Math.abs(new Random().nextInt()));
        bean.setValue("paymentTerms", paymentTerms);
        bean.setValue("paymentUom", paymentUom.toString());
        bean.setValue("accountFee", accountFeeType.toString());
        bean.setValue("accountFeeAmount", accountFeeAmount);
        bean.setValue("accountFeeDays", accountFeeDays);
        bean.setValue("accountFeeBalance", feeBalance);
        save(lookup);
        return lookup;
    }

    /**
     * Helper to create a new <em>party.organisationTill</em>.
     *
     * @return a new till
     */
    public static Party createTill() {
        Party till = (Party) create("party.organisationTill");
        till.setName("XTill-" + System.currentTimeMillis());
        save(till);
        return till;
    }

    /**
     * Helper to create a charges act.
     *
     * @param shortName     the act short name
     * @param itemShortName the act item short name
     * @param amount        the act total
     * @param customer      the customer
     * @param patient       the patient. May be {@code null}
     * @param product       the product. May be {@code null}
     * @param status        the act status
     * @return a list containing the charges act and its item
     */
    private static List<FinancialAct> createCharges(String shortName, String itemShortName, BigDecimal amount,
                                                    Party customer, Party patient, Product product, String status) {
        return createCharges(shortName, itemShortName, customer, patient, null, product, ONE, ZERO, amount, ZERO,
                             status);
    }

    /**
     * Helper to create a charges act.
     *
     * @param shortName     the act short name
     * @param itemShortName the act item short name
     * @param customer      the customer
     * @param patient       the patient. May be {@code null}
     * @param clinician     the clinician. May be {@code null}
     * @param product       the product. May be {@code null}
     * @param quantity      the quantity
     * @param fixedPrice    the fixed price
     * @param unitPrice     the unit price
     * @param tax           the tax
     * @param status        the act status       @return a list containing the charges act and its item
     */
    private static List<FinancialAct> createCharges(String shortName, String itemShortName, Party customer,
                                                    Party patient, User clinician, Product product, BigDecimal quantity,
                                                    BigDecimal fixedPrice, BigDecimal unitPrice, BigDecimal tax,
                                                    String status) {
        BigDecimal amount = fixedPrice.add(unitPrice.multiply(quantity));
        FinancialAct act = createAct(shortName, amount, customer, clinician, status);
        ActBean bean = new ActBean(act);
        FinancialAct item = createChargeItem(itemShortName, patient, clinician, product, quantity, fixedPrice,
                                             unitPrice, tax);
        bean.addNodeRelationship("items", item);
        return Arrays.asList(act, item);
    }

    /**
     * Helper to create a POSTED payment or refund act.
     *
     * @param shortName     the act short name
     * @param itemShortName the act item short name
     * @param amount        the act total
     * @param customer      the customer
     * @param till          the till
     * @return a new act
     */
    private static FinancialAct createPaymentRefund(String shortName, String itemShortName, BigDecimal amount,
                                                    Party customer, Party till) {
        List<FinancialAct> acts = createPaymentRefund(shortName, itemShortName, amount, customer, till,
                                                      FinancialActStatus.POSTED, true);
        return acts.get(0);
    }

    /**
     * Creates a new payment/refund.
     *
     * @param shortName the act short name
     * @param amount    the act amount
     * @param customer  the customer
     * @param till      the till
     * @param status    the act status
     * @return the payment/refund act
     */
    private static FinancialAct createPaymentRefund(String shortName, BigDecimal amount, Party customer, Party till,
                                                    String status) {
        FinancialAct act = createAct(shortName, amount, customer, null, status);
        ActBean bean = new ActBean(act);
        bean.setValue("amount", amount);
        bean.addNodeParticipation("till", till);
        return act;
    }

    /**
     * Creates a new payment/refund act and related item.
     *
     * @param shortName     the act short name
     * @param itemShortName the item short name
     * @param amount        the act amount
     * @param customer      the customer
     * @param till          the till
     * @param status        the act status
     * @param saveItem      determines if the item should be saved
     * @return a list containing the payment/refund act and its item
     */
    private static List<FinancialAct> createPaymentRefund(String shortName, String itemShortName, BigDecimal amount,
                                                          Party customer, Party till, String status, boolean saveItem) {
        FinancialAct act = createPaymentRefund(shortName, amount, customer, till, status);
        ActBean bean = new ActBean(act);
        FinancialAct item = createPaymentRefundItem(itemShortName, amount);
        if (saveItem) {
            ActBean itemBean = new ActBean(item);
            itemBean.save();
        }
        bean.addNodeRelationship("items", item);
        return Arrays.asList(act, item);
    }

    /**
     * Creates a new payment/refund act item.
     *
     * @param itemShortName the item short name
     * @param amount        the act amount
     * @return the payment/refund item
     */
    public static FinancialAct createPaymentRefundItem(String itemShortName, BigDecimal amount) {
        FinancialAct item = (FinancialAct) create(itemShortName);
        ActBean itemBean = new ActBean(item);
        itemBean.setValue("amount", amount);
        if (itemBean.isA(PAYMENT_CASH, REFUND_CASH)) {
            itemBean.setValue("roundedAmount", amount);
            if (itemBean.isA(PAYMENT_CASH)) {
                itemBean.setValue("tendered", amount);
            }
        }
        return item;
    }

    /**
     * Helper to create a new act, setting the total, adding a customer and clinician participation and setting the
     * status.
     *
     * @param shortName the act short name
     * @param amount    the act total
     * @param customer  the customer
     * @param clinician the clinician. May be {@code null}
     * @param status    the status
     * @return a new act
     */
    private static FinancialAct createAct(String shortName, BigDecimal amount, Party customer, User clinician,
                                          String status) {
        FinancialAct act = (FinancialAct) create(shortName);
        act.setStatus(status);
        ActBean bean = new ActBean(act);
        bean.setValue("amount", amount);
        bean.addNodeParticipation("customer", customer);
        if (clinician != null) {
            bean.addNodeParticipation("clinician", clinician);
        }
        return act;
    }

}
