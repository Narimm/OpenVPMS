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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.product;

import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.RelationshipDescriptorTableModel;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.system.ServiceHelper;


/**
 * Table model for <em>entityLink.productSupplier</em> objects.
 *
 * @author Tim Anderson
 */
public class ProductSupplierTableModel extends RelationshipDescriptorTableModel<EntityLink> {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The nodes to include in the table.
     */
    private static final String[] NODES = {"description", "preferred", "packageSize", "packageUnits", "listPrice",
                                           "nettPrice"};

    /**
     * Constructs a {@link ProductSupplierTableModel}.
     * <p/>
     * Enables selection if the context is in edit mode.
     *
     * @param shortNames the archetype short names
     * @param context    the layout context
     */
    public ProductSupplierTableModel(String[] shortNames, LayoutContext context) {
        super(shortNames, context, true);
        service = ServiceHelper.getArchetypeService();
    }

    /**
     * Returns a value for a given column.
     *
     * @param object the object to operate on
     * @param column the column
     * @param row    the row
     * @return the value for the column
     */
    @Override
    protected Object getValue(EntityLink object, DescriptorTableColumn column, int row) {
        Object result;
        if ("description".equals(column.getName())) {
            // render the reorder description if present, falling back to the reorder code if there is none.
            IMObjectBean bean = service.getBean(object);
            result = bean.getString("reorderDescription");
            if (result == null) {
                result = bean.getString("reorderCode");
            }
        } else {
            result = super.getValue(object, column, row);
        }
        return result;
    }

    /**
     * Returns a list of node descriptor names to include in the table.
     *
     * @return the list of node descriptor names to include in the table
     */
    @Override
    protected String[] getNodeNames() {
        return NODES;
    }
}
