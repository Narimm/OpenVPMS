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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.account;

import org.openvpms.archetype.rules.act.ActCopyHandler;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopyHandler;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.BAD_DEBT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.COUNTER;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.COUNTER_ITEM;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.COUNTER_ITEM_RELATIONSHIP;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.CREDIT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.CREDIT_ADJUST;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.CREDIT_ITEM;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.CREDIT_ITEM_RELATIONSHIP;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.DEBIT_ADJUST;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.INITIAL_BALANCE;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.INVOICE;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.INVOICE_ITEM;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.INVOICE_ITEM_RELATIONSHIP;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT_CASH;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT_CHEQUE;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT_CREDIT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT_DISCOUNT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT_EFT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT_ITEM_RELATIONSHIP;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT_OTHER;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND_CASH;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND_CHEQUE;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND_CREDIT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND_DISCOUNT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND_EFT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND_ITEM_RELATIONSHIP;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND_OTHER;


/**
 * {@link IMObjectCopyHandler} that creates reversals for customer acts.
 *
 * @author Tim Anderson
 */
class CustomerActReversalHandler extends ActCopyHandler {

    /**
     * Map of debit types to their corresponding credit types.
     */
    private static final String[][] TYPE_MAP = {
            {INVOICE, CREDIT},
            {INVOICE_ITEM, CREDIT_ITEM},
            {INVOICE_ITEM_RELATIONSHIP, CREDIT_ITEM_RELATIONSHIP},
            {COUNTER, CREDIT},
            {COUNTER_ITEM, CREDIT_ITEM},
            {COUNTER_ITEM_RELATIONSHIP, CREDIT_ITEM_RELATIONSHIP},
            {PAYMENT, REFUND},
            {PAYMENT_ITEM_RELATIONSHIP, REFUND_ITEM_RELATIONSHIP},
            {PAYMENT_CASH, REFUND_CASH},
            {PAYMENT_CHEQUE, REFUND_CHEQUE},
            {PAYMENT_CREDIT, REFUND_CREDIT},
            {PAYMENT_DISCOUNT, REFUND_DISCOUNT},
            {PAYMENT_EFT, REFUND_EFT},
            {PAYMENT_OTHER, REFUND_OTHER},
            {DEBIT_ADJUST, CREDIT_ADJUST},
            {DEBIT_ADJUST, BAD_DEBT},
            {INITIAL_BALANCE, CREDIT_ADJUST}
    };


    /**
     * Constructs a {@link CustomerActReversalHandler}.
     *
     * @param act the act to reverse
     */
    public CustomerActReversalHandler(Act act) {
        super(TYPE_MAP);
        setReverse(TypeHelper.isA(act, CREDIT, REFUND, CREDIT_ADJUST, BAD_DEBT));
    }

    /**
     * Determines how {@link IMObjectCopier} should treat an object.
     *
     * @param object  the source object
     * @param service the archetype service
     * @return {@code object} if the object shouldn't be copied, {@code null} if it should be replaced with
     *         {@code null}, or a new instance if the object should be copied
     */
    @Override
    public IMObject getObject(IMObject object, IArchetypeService service) {
        IMObject result = super.getObject(object, service);
        if (TypeHelper.isA(object, REFUND_CASH)
            && TypeHelper.isA(result, PAYMENT_CASH)) {
            FinancialAct refund = (FinancialAct) object;
            ActBean payment = new ActBean((Act) result, service);
            payment.setValue("tendered", refund.getTotal());
        }
        return result;
    }

    /**
     * Helper to determine if a node is copyable.
     *
     * @param archetype the archetype descriptor
     * @param node      the node descriptor
     * @param source    if {@code true} the node is the source; otherwise its the target
     * @return {@code true} if the node is copyable; otherwise {@code false}
     */
    @Override
    protected boolean isCopyable(ArchetypeDescriptor archetype, NodeDescriptor node, boolean source) {
        String name = node.getName();
        if ("credit".equals(name) || "allocatedAmount".equals(name)
            || "accountBalance".equals(name) || "allocation".equals(name) || "reversals".equals(name)
            || "reverses".equals(name) || "hide".equals(name)) {
            return false;
        } else {
            return super.isCopyable(archetype, node, source);
        }
    }
}