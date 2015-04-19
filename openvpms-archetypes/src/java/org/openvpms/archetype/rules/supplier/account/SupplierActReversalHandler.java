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

package org.openvpms.archetype.rules.supplier.account;

import org.openvpms.archetype.rules.finance.account.AbstractActReversalHandler;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopyHandler;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;


/**
 * An {@link IMObjectCopyHandler} that creates reversals for supplier acts.
 *
 * @author Tim Anderson
 */
public class SupplierActReversalHandler extends AbstractActReversalHandler {

    /**
     * Map of debit types to their corresponding credit types.
     */
    private static final String[][] TYPE_MAP = {
            {SupplierArchetypes.INVOICE, SupplierArchetypes.CREDIT},
            {SupplierArchetypes.INVOICE_ITEM, SupplierArchetypes.CREDIT_ITEM},
            {SupplierArchetypes.INVOICE_ITEM_RELATIONSHIP, SupplierArchetypes.CREDIT_ITEM_RELATIONSHIP},
            {SupplierArchetypes.PAYMENT, SupplierArchetypes.REFUND},
            {SupplierArchetypes.PAYMENT_ITEM_RELATIONSHIP, SupplierArchetypes.REFUND_ITEM_RELATIONSHIP},
            {SupplierArchetypes.PAYMENT_CASH, SupplierArchetypes.REFUND_CASH},
            {SupplierArchetypes.PAYMENT_CHEQUE, SupplierArchetypes.REFUND_CHEQUE},
            {SupplierArchetypes.PAYMENT_CREDIT, SupplierArchetypes.REFUND_CREDIT},
            {SupplierArchetypes.PAYMENT_EFT, SupplierArchetypes.REFUND_EFT}
    };


    /**
     * Construct a new <code>SupplierActReversalHandler</code>.
     *
     * @param act the act to reverse
     */
    public SupplierActReversalHandler(Act act) {
        super(!TypeHelper.isA(act, SupplierArchetypes.CREDIT, SupplierArchetypes.REFUND), TYPE_MAP);
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
        return !"credit".equals(name) && !"printed".equals(name) && super.isCopyable(archetype, node, source);
    }

}
