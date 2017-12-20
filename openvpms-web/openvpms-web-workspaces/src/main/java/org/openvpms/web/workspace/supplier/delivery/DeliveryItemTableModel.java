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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.workspace.supplier.delivery;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableModel;

/**
 * Table model for <em>act.supplierDeliveryItem</em>s.
 * <p>
 * This suppresses the supplierInvoiceLineId if the parent act is supplied and the supplierInvoiceId node is empty.
 *
 * @author Tim Anderson
 */
public class DeliveryItemTableModel extends DescriptorTableModel<Act> {

    /**
     * The nodes to display.
     */
    private final ArchetypeNodes nodes;

    /**
     * Constructs an {@link DeliveryItemTableModel}.
     *
     * @param shortNames the act archetype short names
     * @param context    the layout context
     */
    public DeliveryItemTableModel(String[] shortNames, LayoutContext context) {
        this(shortNames, null, context);
    }

    /**
     * Constructs an {@link DeliveryItemTableModel}.
     *
     * @param shortNames the act archetype short names
     * @param context    the layout context
     */
    public DeliveryItemTableModel(String[] shortNames, IMObject parent, LayoutContext context) {
        super(context);
        nodes = allSimpleNodesMinusIdAndLongText();
        if (TypeHelper.isA(parent, SupplierArchetypes.DELIVERY)) {
            IMObjectBean bean = new IMObjectBean(parent);
            if (StringUtils.isEmpty(bean.getString("supplierInvoiceId"))) {
                nodes.exclude("supplierInvoiceLineId");
            }
        }
        setTableColumnModel(createColumnModel(shortNames, context));
    }

    /**
     * Returns an {@link ArchetypeNodes} that determines what nodes appear in the table.
     *
     * @return the nodes to include
     */
    @Override
    protected ArchetypeNodes getArchetypeNodes() {
        return nodes;
    }
}