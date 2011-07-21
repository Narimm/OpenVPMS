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
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


/**
 * Financial test helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
    public static List<FinancialAct> createChargesInvoice(Money amount,
                                                          Party customer,
                                                          Party patient,
                                                          Product product,
                                                          String status) {
        return createCharges("act.customerAccountChargesInvoice",
                             "act.customerAccountInvoiceItem",
                             "actRelationship.customerAccountInvoiceItem",
                             amount, customer, patient, product, status);
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
    public static List<FinancialAct> createChargesCounter(Money amount,
                                                          Party customer,
                                                          Product product,
                                                          String status) {
        return createCharges("act.customerAccountChargesCounter",
                             "act.customerAccountCounterItem",
                             "actRelationship.customerAccountCounterItem",
                             amount, customer, null, product, status);
    }

    /**
     * Helper to create a new <em>act.customerAccountChargesCredit</em>
     * and corresponding <em>act.customerAccountCreditItem</em>.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param patient  the patient. May be <tt>null</tt>
     * @param product  the product
     * @param status   the act statues
     * @return a list containing the credit act and its item
     */
    public static List<FinancialAct> createChargesCredit(Money amount,
                                                         Party customer,
                                                         Party patient,
                                                         Product product,
                                                         String status) {
        List<FinancialAct> result = createCharges("act.customerAccountChargesCredit",
                                                  "act.customerAccountCreditItem",
                                                  "actRelationship.customerAccountCreditItem",
                                                  amount, customer, patient, product, status);
        ActBean bean = new ActBean(result.get(0));
        bean.setValue("notes", "Dummy notes");
        return result;
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
    public static List<FinancialAct> createPayment(Money amount,
                                                   Party customer,
                                                   Party till,
                                                   String status) {
        return createPaymentRefund("act.customerAccountPayment",
                                   "act.customerAccountPaymentCash",
                                   "actRelationship.customerAccountPaymentItem",
                                   amount, customer, till, status);
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
    public static List<FinancialAct> createRefund(Money amount,
                                                  Party customer,
                                                  Party till,
                                                  String status) {
        List<FinancialAct> result = createPaymentRefund("act.customerAccountRefund",
                                                        "act.customerAccountRefundCash",
                                                        "actRelationship.customerAccountRefundItem",
                                                        amount, customer, till, status);
        ActBean bean = new ActBean(result.get(0));
        bean.setValue("notes", "Dummy notes");
        return result;
    }

    /**
     * Helper to create a POSTED <em>act.customerAccountPayment</em>.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param till     the till
     * @return a new act
     */
    public static FinancialAct createPayment(Money amount, Party customer,
                                             Party till) {
        return createPaymentCash(amount, customer, till);
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
    public static FinancialAct createPaymentCash(Money amount,
                                                 Party customer,
                                                 Party till) {
        return createPaymentRefund("act.customerAccountPayment",
                                   "act.customerAccountPaymentCash",
                                   "actRelationship.customerAccountPaymentItem",
                                   amount, customer, till);
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
    public static FinancialAct createPaymentCheque(Money amount,
                                                   Party customer,
                                                   Party till) {
        return createPaymentRefund("act.customerAccountPayment",
                                   "act.customerAccountPaymentCheque",
                                   "actRelationship.customerAccountPaymentItem",
                                   amount, customer, till);
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
    public static FinancialAct createPaymentCredit(Money amount,
                                                   Party customer,
                                                   Party till) {
        return createPaymentRefund("act.customerAccountPayment",
                                   "act.customerAccountPaymentCredit",
                                   "actRelationship.customerAccountPaymentItem",
                                   amount, customer, till);
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
    public static FinancialAct createPaymentDiscount(Money amount,
                                                     Party customer,
                                                     Party till) {
        return createPaymentRefund("act.customerAccountPayment",
                                   "act.customerAccountPaymentDiscount",
                                   "actRelationship.customerAccountPaymentItem",
                                   amount, customer, till);
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
    public static FinancialAct createPaymentEFT(Money amount,
                                                Party customer,
                                                Party till) {
        return createPaymentRefund("act.customerAccountPayment",
                                   "act.customerAccountPaymentEFT",
                                   "actRelationship.customerAccountPaymentItem",
                                   amount, customer, till);
    }

    /**
     * Helper to create a POSTED <em>act.customerAccountRefund</em>.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param till     the till
     * @return a new act
     */
    public static FinancialAct createRefund(Money amount, Party customer,
                                            Party till) {
        return createRefundCash(amount, customer, till);
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
    public static FinancialAct createRefundCash(Money amount, Party customer,
                                                Party till) {
        FinancialAct result = createPaymentRefund("act.customerAccountRefund",
                                                  "act.customerAccountRefundCash",
                                                  "actRelationship.customerAccountRefundItem",
                                                  amount, customer, till);
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
    public static FinancialAct createRefundCheque(Money amount, Party customer,
                                                  Party till) {
        FinancialAct result = createPaymentRefund("act.customerAccountRefund",
                                                  "act.customerAccountRefundCheque",
                                                  "actRelationship.customerAccountRefundItem",
                                                  amount, customer, till);
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
    public static FinancialAct createRefundCredit(Money amount, Party customer,
                                                  Party till) {
        FinancialAct result = createPaymentRefund("act.customerAccountRefund",
                                                  "act.customerAccountRefundCredit",
                                                  "actRelationship.customerAccountRefundItem",
                                                  amount, customer, till);
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
    public static FinancialAct createRefundDiscount(Money amount,
                                                    Party customer,
                                                    Party till) {
        FinancialAct result = createPaymentRefund("act.customerAccountRefund",
                                                  "act.customerAccountRefundDiscount",
                                                  "actRelationship.customerAccountRefundItem",
                                                  amount, customer, till);
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
    public static FinancialAct createRefundEFT(Money amount, Party customer,
                                               Party till) {
        FinancialAct result = createPaymentRefund("act.customerAccountRefund",
                                                  "act.customerAccountRefundEFT",
                                                  "actRelationship.customerAccountRefundItem",
                                                  amount, customer, till);
        ActBean bean = new ActBean(result);
        bean.setValue("notes", "Dummy notes");
        return result;
    }

    /**
     * Helper to create a charge item.
     *
     * @param itemShortName the charge item short name
     * @param amount        the amount
     * @param patient       the patient. May be <tt>null</tt>
     * @param product       the product. May be <tt>null</tt>
     * @return a new charge item
     */
    public static FinancialAct createItem(String itemShortName, Money amount, Party patient, Product product) {
        FinancialAct item = (FinancialAct) create(itemShortName);
        item.setUnitAmount(amount);
        ActBean itemBean = new ActBean(item);
        if (patient != null) {
            itemBean.addParticipation("participation.patient", patient);
        }
        if (product != null) {
            itemBean.addParticipation("participation.product", product);
        }
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
    public static Lookup createAccountType(int paymentTerms,
                                           DateUnits paymentUom,
                                           BigDecimal accountFeeAmount) {
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
    public static Lookup createAccountType(int paymentTerms,
                                           DateUnits paymentUom,
                                           BigDecimal accountFeeAmount,
                                           int accountFeeDays) {
        return createAccountType(paymentTerms, paymentUom, accountFeeAmount,
                                 AccountType.FeeType.FIXED, accountFeeDays, BigDecimal.ZERO);
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
     * @param feeBalance       the minumum balance when an account fee applies
     * @return a new classification
     */
    public static Lookup createAccountType(int paymentTerms,
                                           DateUnits paymentUom,
                                           BigDecimal accountFeeAmount,
                                           AccountType.FeeType accountFeeType,
                                           int accountFeeDays,
                                           BigDecimal feeBalance) {
        Lookup lookup = (Lookup) create("lookup.customerAccountType");
        IMObjectBean bean = new IMObjectBean(lookup);
        bean.setValue("code", "XCUSTOMER_ACCOUNT_TYPE"
                              + Math.abs(new Random().nextInt()));
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
     * @param shortName             the act short name
     * @param itemShortName         the act item short name
     * @param relationshipShortName the act relationship short name
     * @param amount                the act total
     * @param customer              the customer
     * @param patient               the patient. May be <tt>null</tt>
     * @param product               the product. May be <tt>null</tt>
     * @param status                the act status
     * @return a list containing the charges act and its item
     */
    private static List<FinancialAct> createCharges(
            String shortName, String itemShortName,
            String relationshipShortName, Money amount, Party customer,
            Party patient, Product product, String status) {
        FinancialAct act = createAct(shortName, amount, customer, status);
        ActBean bean = new ActBean(act);
        FinancialAct item = createItem(itemShortName, amount, patient, product);
        bean.addRelationship(relationshipShortName, item);
        return Arrays.asList(act, item);
    }

    /**
     * Helper to create a POSTED payment or refund act.
     *
     * @param shortName             the act short name
     * @param itemShortName         the act item short name
     * @param relationshipShortName the act relationshipShortName
     * @param amount                the act total
     * @param customer              the customer
     * @param till                  the till
     * @return a new act
     */
    private static FinancialAct createPaymentRefund(
            String shortName, String itemShortName,
            String relationshipShortName, Money amount, Party customer,
            Party till) {
        FinancialAct act = createAct(shortName, amount, customer,
                                     FinancialActStatus.POSTED);
        ActBean bean = new ActBean(act);
        FinancialAct item = (FinancialAct) create(itemShortName);
        item.setTotal(amount);
        ActBean itemBean = new ActBean(item);
        if (itemBean.isA("act.customerAccountPaymentCash",
                         "act.customerAccountRefundCash")) {
            itemBean.setValue("roundedAmount", amount);
            if (itemBean.isA("act.customerAccountPaymentCash")) {
                itemBean.setValue("tendered", amount);
            }
        }
        bean.addParticipation("participation.till", till);
        itemBean.save();
        bean.addRelationship(relationshipShortName, item);
        return act;
    }

    /**
     * Creates a new payment/refund act and related item.
     *
     * @param shortName             the act short name
     * @param itemShortName         the item short name
     * @param relationshipShortName the relationship short name
     * @param amount                the act amount
     * @param customer              the customer
     * @param till                  the till
     * @param status                the act status
     * @return a list containing the payment/refund act and its item
     */
    private static List<FinancialAct> createPaymentRefund(
            String shortName, String itemShortName,
            String relationshipShortName, Money amount, Party customer,
            Party till, String status) {
        FinancialAct act = createAct(shortName, amount, customer, status);
        ActBean bean = new ActBean(act);
        FinancialAct item = (FinancialAct) create(itemShortName);
        item.setTotal(amount);
        ActBean itemBean = new ActBean(item);
        if (itemBean.isA("act.customerAccountPaymentCash",
                         "act.customerAccountRefundCash")) {
            itemBean.setValue("roundedAmount", amount);
            if (itemBean.isA("act.customerAccountPaymentCash")) {
                itemBean.setValue("tendered", amount);
            }
        }
        bean.addParticipation("participation.till", till);
        bean.addRelationship(relationshipShortName, item);
        return Arrays.asList(act, item);
    }

    /**
     * Helper to create a new act, setting the total, adding a customer
     * participation and setting the status.
     *
     * @param shortName the act short name
     * @param amount    the act total
     * @param customer  the customer
     * @param status    the status
     * @return a new act
     */
    private static FinancialAct createAct(String shortName, Money amount,
                                          Party customer, String status) {
        FinancialAct act = (FinancialAct) create(shortName);
        act.setStatus(status);
        act.setTotal(amount);
        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.customer", customer);
        return act;
    }

}
