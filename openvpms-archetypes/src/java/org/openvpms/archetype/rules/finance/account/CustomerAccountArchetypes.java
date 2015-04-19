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


/**
 * Customer account archetypes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerAccountArchetypes {

    /**
     * Bad debt act short name.
     */
    public static final String BAD_DEBT = "act.customerAccountBadDebt";

    /**
     * Counter charge act short name.
     */
    public static final String COUNTER
            = "act.customerAccountChargesCounter";

    /**
     * Counter charge item act short name.
     */
    public static final String COUNTER_ITEM
            = "act.customerAccountCounterItem";

    /**
     * Counter charge item act relationship short name.
     */
    public static final String COUNTER_ITEM_RELATIONSHIP
            = "actRelationship.customerAccountCounterItem";

    /**
     * Invoice charge act short name.
     */
    public static final String INVOICE
            = "act.customerAccountChargesInvoice";

    /**
     * Invoice charge item act short name.
     */
    public static final String INVOICE_ITEM
            = "act.customerAccountInvoiceItem";

    /**
     * Invoice charge item act relationship short name.
     */
    public static final String INVOICE_ITEM_RELATIONSHIP
            = "actRelationship.customerAccountInvoiceItem";

    /**
     * Invoice charge item dispensing act relationship short name.
     */
    public static final String DISPENSING_ITEM_RELATIONSHIP
            = "actRelationship.invoiceItemDispensing";

    /**
     * Invoice charge item investigation act relationship short name.
     */
    public static final String INVESTIGATION_ITEM_RELATIONSHIP
            = "actRelationship.invoiceItemInvestigation";

    /**
     * Credit charge act short name.
     */
    public static final String CREDIT
            = "act.customerAccountChargesCredit";

    /**
     * Credit charge item act short name.
     */
    public static final String CREDIT_ITEM
            = "act.customerAccountCreditItem";

    /**
     * Credit charge item act relationship short name.
     */
    public static final String CREDIT_ITEM_RELATIONSHIP
            = "actRelationship.customerAccountCreditItem";

    /**
     * Credit adjust act short name.
     */
    public static final String CREDIT_ADJUST
            = "act.customerAccountCreditAdjust";

    /**
     * Payment act short name.
     */
    public static final String PAYMENT
            = "act.customerAccountPayment";

    /**
     * Cash payment act short name.
     */
    public static final String PAYMENT_CASH
            = "act.customerAccountPaymentCash";

    /**
     * Cheque payment act short name.
     */
    public static final String PAYMENT_CHEQUE
            = "act.customerAccountPaymentCheque";

    /**
     * Credit payment act short name.
     */
    public static final String PAYMENT_CREDIT
            = "act.customerAccountPaymentCredit";

    /**
     * Discount payment act short name.
     */
    public static final String PAYMENT_DISCOUNT
            = "act.customerAccountPaymentDiscount";

    /**
     * EFT payment act short name.
     */
    public static final String PAYMENT_EFT = "act.customerAccountPaymentEFT";

    /**
     * Custom payment act short name.
     */
    public static final String PAYMENT_OTHER = "act.customerAccountPaymentOther";

    /**
     * Payment item act relationship act short name.
     */
    public static final String PAYMENT_ITEM_RELATIONSHIP
            = "actRelationship.customerAccountPaymentItem";

    /**
     * Debit adjust act short name.
     */
    public static final String DEBIT_ADJUST = "act.customerAccountDebitAdjust";

    /**
     * Refund act short name.
     */
    public static final String REFUND = "act.customerAccountRefund";

    /**
     * Refund item act relationship act short name.
     */
    public static final String REFUND_ITEM_RELATIONSHIP
            = "actRelationship.customerAccountRefundItem";

    /**
     * Cash refund act short name.
     */
    public static final String REFUND_CASH
            = "act.customerAccountRefundCash";

    /**
     * Cheque refund act short name.
     */
    public static final String REFUND_CHEQUE
            = "act.customerAccountRefundCheque";

    /**
     * Credit refund act short name.
     */
    public static final String REFUND_CREDIT
            = "act.customerAccountRefundCredit";

    /**
     * Discount refund act short name.
     */
    public static final String REFUND_DISCOUNT
            = "act.customerAccountRefundDiscount";

    /**
     * EFT refund act short name.
     */
    public static final String REFUND_EFT = "act.customerAccountRefundEFT";

    /**
     * 'Other' refund act short name.
     */
    public static final String REFUND_OTHER = "act.customerAccountRefundOther";

    /**
     * Initial balance act short name.
     */
    public static final String INITIAL_BALANCE
            = "act.customerAccountInitialBalance";

    /**
     * Short names of the credit and debit acts the affect the balance.
     */
    public static final String[] DEBITS_CREDITS;

    /**
     * All customer debit act short names.
     */
    public static final String[] DEBITS = {
            COUNTER,
            INVOICE,
            DEBIT_ADJUST,
            REFUND,
            INITIAL_BALANCE
    };

    /**
     * All customer credit act short names.
     */
    public static final String[] CREDITS = {
            CREDIT,
            CREDIT_ADJUST,
            PAYMENT,
            BAD_DEBT};

    /**
     * The customer account balance participation short name.
     */
    public static final String BALANCE_PARTICIPATION
            = "participation.customerAccountBalance";

    /**
     * The customer account balance act relationship short name.
     */
    public static final String ACCOUNT_ALLOCATION_RELATIONSHIP
            = "actRelationship.customerAccountAllocation";

    /**
     * The customer account opening balance short name.
     */
    public static final String OPENING_BALANCE
            = "act.customerAccountOpeningBalance";

    /**
     * The customer account closing balance short name.
     */
    public static final String CLOSING_BALANCE
            = "act.customerAccountClosingBalance";

    /**
     * All debit and credit acts, and the initial, opening and closing balance
     * short names.
     */
    public static final String[] ACCOUNT_ACTS;

    static {
        // populate DEBITS_CREDITS
        int dLength = DEBITS.length;
        int cLength = CREDITS.length;
        int dcLength = dLength + cLength;
        DEBITS_CREDITS = new String[dcLength];
        System.arraycopy(DEBITS, 0, DEBITS_CREDITS, 0, dLength);
        System.arraycopy(CREDITS, 0, DEBITS_CREDITS, dLength, cLength);

        // populate ACCOUNT_ACTS
        ACCOUNT_ACTS = new String[dcLength + 3];
        System.arraycopy(DEBITS_CREDITS, 0, ACCOUNT_ACTS, 0,
                         dcLength);
        ACCOUNT_ACTS[dcLength] = INITIAL_BALANCE;
        ACCOUNT_ACTS[dcLength + 1] = OPENING_BALANCE;
        ACCOUNT_ACTS[dcLength + 2] = CLOSING_BALANCE;
    }
}
