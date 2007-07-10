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
 * Customer account act types.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerAccountActTypes {

    /**
     * Bad debt act short name.
     */
    public static final String BAD_DEBT = "act.customerAccountBadDebt";

    /**
     * Counter charge act short name.
     */
    public static final String CHARGES_COUNTER
            = "act.customerAccountChargesCounter";

    /**
     * Invoice charge act short name.
     */
    public static final String CHARGES_INVOICE
            = "act.customerAccountChargesInvoice";

    /**
     * Credit charge act short name.
     */
    public static final String CHARGES_CREDIT
            = "act.customerAccountChargesCredit";

    /**
     * Credit adjust act short name.
     */
    public static final String CREDIT_ADJUST
            = "act.customerAccountCreditAdjust";

    /**
     * Payment act short name.
     */
    public static final String PAYMENT = "act.customerAccountPayment";

    /**
     * Debit adjust act short name.
     */
    public static final String DEBIT_ADJUST = "act.customerAccountDebitAdjust";

    /**
     * Refund act short name.
     */
    public static final String REFUND = "act.customerAccountRefund";

    /**
     * Initial balance act short name.
     */
    public static final String INITIAL_BALANCE
            = "act.customerAccountInitialBalance";

    /**
     * Short names of the credit and debit acts the affect the balance.
     */
    public static final String[] DEBIT_CREDIT_SHORT_NAMES = {
            CHARGES_COUNTER,
            CHARGES_CREDIT,
            CHARGES_INVOICE,
            CREDIT_ADJUST,
            DEBIT_ADJUST,
            PAYMENT,
            REFUND,
            INITIAL_BALANCE,
            BAD_DEBT};

    /**
     * All customer debit act short names.
     */
    public static final String[] DEBIT_SHORT_NAMES = {
            CHARGES_COUNTER,
            CHARGES_INVOICE,
            DEBIT_ADJUST,
            REFUND,
            INITIAL_BALANCE
    };

    /**
     * All customer credit act short names.
     */
    public static final String[] CREDIT_SHORT_NAMES = {
            CHARGES_CREDIT,
            CREDIT_ADJUST,
            PAYMENT,
            BAD_DEBT};

    /**
     * The customer account balance participation short name.
     */
    public static final String ACCOUNT_BALANCE_SHORTNAME
            = "participation.customerAccountBalance";

    /**
     * The customer account balance act relationship short name.
     */
    public static final String ACCOUNT_ALLOCATION_SHORTNAME
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
}
