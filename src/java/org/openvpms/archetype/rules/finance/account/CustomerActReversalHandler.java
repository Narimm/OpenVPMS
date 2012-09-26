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

import org.openvpms.archetype.rules.act.ActCopyHandler;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.*;
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


/**
 * {@link IMObjectCopyHandler} that creates reversals for customer acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-12-12 04:10:40Z $
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
     * Constructs a new <tt>CustomerActReversalHandler</tt>.
     *
     * @param act the act to reverse
     */
    public CustomerActReversalHandler(Act act) {
        super(TYPE_MAP);
        setReverse(TypeHelper.isA(act, CREDIT, REFUND, CREDIT_ADJUST,
                                  BAD_DEBT));
    }

    /**
     * Determines how {@link IMObjectCopier} should treat an object.
     *
     * @param object  the source object
     * @param service the archetype service
     * @return <tt>object</tt> if the object shouldn't be copied,
     *         <tt>null</tt> if it should be replaced with
     *         <tt>null</tt>, or a new instance if the object should be
     *         copied
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
     * @param source    if <tt>true</tt> the node is the source; otherwise its
     *                  the target
     * @return <tt>true</tt> if the node is copyable; otherwise <tt>false</tt>
     */
    @Override
    protected boolean isCopyable(ArchetypeDescriptor archetype,
                                 NodeDescriptor node, boolean source) {
        String name = node.getName();
        if ("credit".equals(name) || "allocatedAmount".equals(name)
                || "accountBalance".equals(name) || "allocation".equals(name)) {
            return false;
        } else {
            return super.isCopyable(archetype, node, source);
        }
    }
}