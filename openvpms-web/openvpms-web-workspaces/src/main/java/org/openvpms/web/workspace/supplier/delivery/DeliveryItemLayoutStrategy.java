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

package org.openvpms.web.workspace.supplier.delivery;

import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.system.ServiceHelper;

/**
 * Layout strategy for <em>act.supplierDeliveryItem</em> acts.
 * <p/>
 * This suppresses the order nodes if is empty and the and supplierInvoiceLineId if it is empty and the node is
 * read-only.
 *
 * @author Tim Anderson
 */
public class DeliveryItemLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Supplier invoice line identifier node.
     */
    private static final String SUPPLIER_INVOICE_LINE_ID = "supplierInvoiceLineId";

    /**
     * Default constructor.
     */
    public DeliveryItemLayoutStrategy() {
        NodeDescriptor node = DescriptorHelper.getNode(SupplierArchetypes.DELIVERY_ITEM, SUPPLIER_INVOICE_LINE_ID,
                                                       ServiceHelper.getArchetypeService());
        ArchetypeNodes nodes = new ArchetypeNodes().excludeIfEmpty("order");
        if (node != null && node.isReadOnly()) {
            nodes.excludeIfEmpty(SUPPLIER_INVOICE_LINE_ID);
        }
        setArchetypeNodes(nodes);
    }

}
