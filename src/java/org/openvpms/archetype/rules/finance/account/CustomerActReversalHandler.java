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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopyHandler;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;


/**
 * {@link IMObjectCopyHandler} that creates reversals for customer acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-12-12 04:10:40Z $
 */
class CustomerActReversalHandler extends AbstractActReversalHandler {

    /**
     * Invoice act short name.
     */
    private static final String INVOICE_TYPE
            = "act.customerAccountChargesInvoice";

    /**
     * Invoice act item short name.
     */
    private static final String INVOICE_ITEM_TYPE
            = "act.customerAccountInvoiceItem";

    /**
     * Invoice act item relationship short name.
     */
    private static final String INVOICE_ITEM_RELATIONSHIP_TYPE
            = "actRelationship.customerAccountInvoiceItem";

    /**
     * Counter act short name.
     */
    private static final String COUNTER_TYPE
            = "act.customerAccountChargesCounter";

    /**
     * Counter act item short name.
     */
    private static final String COUNTER_ITEM_TYPE
            = "act.customerAccountCounterItem";

    /**
     * Counter act item relationship type.
     */
    private static final String COUNTER_ITEM_RELATIONSHIP_TYPE
            = "actRelationship.customerAccountCounterItem";

    /**
     * Credit act type.
     */
    private static final String CREDIT_TYPE
            = "act.customerAccountChargesCredit";

    /**
     * Credit item act type.
     */
    private static final String CREDIT_ITEM_TYPE
            = "act.customerAccountCreditItem";

    /**
     * Credit item act relationship type.
     */
    private static final String CREDIT_ITEM_RELATIONSHIP_TYPE
            = "actRelationship.customerAccountCreditItem";

    /**
     * Payment act type.
     */
    private static final String PAYMENT_TYPE = "act.customerAccountPayment";

    /**
     * Payment act relationship item type.
     */
    private static final String PAYMENT_ITEM_RELATIONSHIP_TYPE
            = "actRelationship.customerAccountPaymentItem";

    /**
     * Cash payment type.
     */
    private static final String PAYMENT_CASH_TYPE
            = "act.customerAccountPaymentCash";

    /**
     * Cheque payment type.
     */
    private static final String PAYMENT_CHEQUE_TYPE
            = "act.customerAccountPaymentCheque";

    /**
     * Credit payment type.
     */
    private static final String PAYMENT_CREDIT_TYPE
            = "act.customerAccountPaymentCredit";

    /**
     * Discount payment type.
     */
    private static final String PAYMENT_DISCOUNT_TYPE
            = "act.customerAccountPaymentDiscount";

    /**
     * EFT payment type.
     */
    private static final String PAYMENT_EFT_TYPE
            = "act.customerAccountPaymentEFT";

    /**
     * Refund act type.
     */
    private static final String REFUND_TYPE = "act.customerAccountRefund";

    /**
     * Refund act relationship item type.
     */
    private static final String REFUND_ITEM_RELATIONSHIP_TYPE
            = "actRelationship.customerAccountRefundItem";

    /**
     * Cash refund type.
     */
    private static final String REFUND_CASH_TYPE
            = "act.customerAccountRefundCash";

    /**
     * Cheque refund type.
     */
    private static final String REFUND_CHEQUE_TYPE
            = "act.customerAccountRefundCheque";

    /**
     * Credit refund type.
     */
    private static final String REFUND_CREDIT_TYPE
            = "act.customerAccountRefundCredit";

    /**
     * Discount refund type.
     */
    private static final String REFUND_DISCOUNT_TYPE
            = "act.customerAccountRefundDiscount";

    /**
     * EFT refund type.
     */
    private static final String REFUND_EFT_TYPE
            = "act.customerAccountRefundEFT";

    /**
     * Debit Adjust type.
     */
    private static final String DEBIT_ADJUST_TYPE
            = "act.customerAccountDebitAdjust";

    /**
     * Credit Adjust type.
     */
    private static final String CREDIT_ADJUST_TYPE
            = "act.customerAccountCreditAdjust";

    /**
     * Bad Debt Adjust type.
     */
    private static final String BADDEBT_ADJUST_TYPE
            = "act.customerAccountBadDebt";

    /**
     * Initial Balance type.
     */
    private static final String INITIAL_BALANCE_TYPE
            = "act.customerAccountInitialBalance";

    /**
     * Map of debit types to their corresponding credit types.
     */
    private static final String[][] TYPE_MAP = {
            {INVOICE_TYPE, CREDIT_TYPE},
            {INVOICE_ITEM_TYPE, CREDIT_ITEM_TYPE},
            {INVOICE_ITEM_RELATIONSHIP_TYPE, CREDIT_ITEM_RELATIONSHIP_TYPE},
            {COUNTER_TYPE, CREDIT_TYPE},
            {COUNTER_ITEM_TYPE, CREDIT_ITEM_TYPE},
            {COUNTER_ITEM_RELATIONSHIP_TYPE, CREDIT_ITEM_RELATIONSHIP_TYPE},
            {PAYMENT_TYPE, REFUND_TYPE},
            {PAYMENT_ITEM_RELATIONSHIP_TYPE, REFUND_ITEM_RELATIONSHIP_TYPE},
            {PAYMENT_CASH_TYPE, REFUND_CASH_TYPE},
            {PAYMENT_CHEQUE_TYPE, REFUND_CHEQUE_TYPE},
            {PAYMENT_CREDIT_TYPE, REFUND_CREDIT_TYPE},
            {PAYMENT_DISCOUNT_TYPE, REFUND_DISCOUNT_TYPE},
            {PAYMENT_EFT_TYPE, REFUND_EFT_TYPE},
            {DEBIT_ADJUST_TYPE, CREDIT_ADJUST_TYPE},
            {DEBIT_ADJUST_TYPE, BADDEBT_ADJUST_TYPE},
            {INITIAL_BALANCE_TYPE, CREDIT_ADJUST_TYPE}
    };


    /**
     * Constructs a new <tt>CustomerActReversalHandler</tt>.
     *
     * @param act the act to reverse
     */
    public CustomerActReversalHandler(Act act) {
        super(!TypeHelper.isA(act, CREDIT_TYPE, REFUND_TYPE, CREDIT_ADJUST_TYPE,
                              BADDEBT_ADJUST_TYPE), TYPE_MAP);
    }

    /**
     * Helper to determine if a node is copyable.
     *
     * @param node   the node descriptor
     * @param source if <code>true</code> the node is the source; otherwise its
     *               the target
     * @return <tt>true</tt> if the node is copyable; otherwise <tt>false</tt>
     */
    @Override
    protected boolean isCopyable(NodeDescriptor node, boolean source) {
        String name = node.getName();
        if ("credit".equals(name) || "allocatedAmount".equals(name)
                || "accountBalance".equals(name) || "allocation".equals(name)
                || "printed".equals(name)) {
            return false;
        } else {
            return super.isCopyable(node, source);
        }
    }
}