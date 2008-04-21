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
import java.util.Random;


/**
 * Financial test helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class FinancialTestHelper extends TestHelper {

    /**
     * Helper to create a POSTED <em>act.customerAccountChargesInvoice</em>.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param patient  the patient
     * @param product  the product
     * @return a new act
     */
    public static FinancialAct createChargesInvoice(Money amount,
                                                    Party customer,
                                                    Party patient,
                                                    Product product) {
        return createCharges("act.customerAccountChargesInvoice",
                             "act.customerAccountInvoiceItem",
                             "actRelationship.customerAccountInvoiceItem",
                             amount, customer, patient, product);
    }

    /**
     * Helper to create a POSTED <em>act.customerAccountChargesCounter</em>.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param product  the product
     * @return a new act
     */
    public static FinancialAct createChargesCounter(Money amount,
                                                    Party customer,
                                                    Product product) {
        return createCharges("act.customerAccountChargesCounter",
                             "act.customerAccountCounterItem",
                             "actRelationship.customerAccountCounterItem",
                             amount, customer, null, product);
    }

    /**
     * Helper to create a POSTED <em>act.customerAccountChargesCredit</em>.
     *
     * @param amount   the act total
     * @param customer the customer
     * @param patient  the patient. May be <tt>null</tt>
     * @param product  the product
     * @return a new act
     */
    public static FinancialAct createChargesCredit(Money amount, Party customer,
                                                   Party patient,
                                                   Product product) {
        return createCharges("act.customerAccountChargesCredit",
                             "act.customerAccountCreditItem",
                             "actRelationship.customerAccountCreditItem",
                             amount, customer, patient, product);
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
        return createPaymentRefund("act.customerAccountRefund",
                                   "act.customerAccountRefundCash",
                                   "actRelationship.customerAccountRefundItem",
                                   amount, customer, till);
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
        return createPaymentRefund("act.customerAccountRefund",
                                   "act.customerAccountRefundCheque",
                                   "actRelationship.customerAccountRefundItem",
                                   amount, customer, till);
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
        return createPaymentRefund("act.customerAccountRefund",
                                   "act.customerAccountRefundCredit",
                                   "actRelationship.customerAccountRefundItem",
                                   amount, customer, till);
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
        return createPaymentRefund("act.customerAccountRefund",
                                   "act.customerAccountRefundDiscount",
                                   "actRelationship.customerAccountRefundItem",
                                   amount, customer, till);
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
        return createPaymentRefund("act.customerAccountRefund",
                                   "act.customerAccountRefundEFT",
                                   "actRelationship.customerAccountRefundItem",
                                   amount, customer, till);
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
        Lookup lookup = (Lookup) create("lookup.customerAccountType");
        IMObjectBean bean = new IMObjectBean(lookup);
        bean.setValue("code", "XCUSTOMER_ACCOUNT_TYPE"
                + Math.abs(new Random().nextInt()));
        bean.setValue("paymentTerms", paymentTerms);
        bean.setValue("paymentUom", paymentUom.toString());
        bean.setValue("accountFeeAmount", accountFeeAmount);
        bean.setValue("accountFeeDays", accountFeeDays);
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
     * @param shortName the act short name
     * @param amount    the act total
     * @param customer  the customer
     * @param patient   the patient. May be <tt>null</tt>
     * @param product   the product. May be <tt>null</tt>
     * @return a new act
     */
    private static FinancialAct createCharges(String shortName,
                                              String itemShortName,
                                              String relationshipShortName,
                                              Money amount, Party customer,
                                              Party patient, Product product) {
        FinancialAct act = createAct(shortName, amount, customer);
        ActBean bean = new ActBean(act);
        FinancialAct item = (FinancialAct) create(itemShortName);
        item.setUnitAmount(amount);
        ActBean itemBean = new ActBean(item);
        if (patient != null) {
            itemBean.addParticipation("participation.patient", patient);
        }
        if (product != null) {
            itemBean.addParticipation("participation.product", product);
        }
        itemBean.save();
        bean.addRelationship(relationshipShortName, item);
        return act;
    }

    /**
     * Helper to create a payment or refund act.
     *
     * @param shortName the act short name
     * @param amount    the act total
     * @param customer  the customer
     * @param till      the till
     * @return a new act
     */
    private static FinancialAct createPaymentRefund(
            String shortName, String itemShortName,
            String relationshipShortName, Money amount, Party customer,
            Party till) {
        FinancialAct act = createAct(shortName, amount, customer);
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
     * Helper to create a new act, setting the total, adding a customer
     * participation and setting the status to 'POSTED'.
     *
     * @param shortName the act short name
     * @param amount    the act total
     * @param customer  the customer
     * @return a new act
     */
    private static FinancialAct createAct(String shortName, Money amount,
                                          Party customer) {
        FinancialAct act = (FinancialAct) create(shortName);
        act.setStatus(FinancialActStatus.POSTED);
        act.setTotal(amount);
        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.customer", customer);
        return act;
    }

}
