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

package org.openvpms.web.component.im.product;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableModel;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.system.ServiceHelper;

import java.util.Collections;
import java.util.List;

/**
 * Table model for product prices.
 *
 * @author Tim Anderson
 */
public class ProductPriceTableModel extends DescriptorTableModel<IMObject> {

    /**
     * Determines if pricing groups should be displayed.
     */
    private boolean showPricingGroups;

    /**
     * The pricing group column index.
     */
    private final int pricingGroupIndex;

    /**
     * The product price rules.
     */
    private final ProductPriceRules rules;

    /**
     * Constructs a {@link ProductPriceTableModel}.
     *
     * @param shortNames the archetype short names
     * @param context    the layout context
     */
    public ProductPriceTableModel(String[] shortNames, LayoutContext context) {
        super(shortNames, context);
        pricingGroupIndex = getNextModelIndex(getColumnModel());
        rules = ServiceHelper.getBean(ProductPriceRules.class);
    }

    /**
     * Determines if pricing groups should be displayed.
     *
     * @param show if {@code true}, adds a column to display pricing groups, else removes it
     */
    public void setShowPricingGroups(boolean show) {
        if (showPricingGroups != show) {
            showPricingGroups = show;
            DefaultTableColumnModel model = (DefaultTableColumnModel) getColumnModel();
            if (show) {
                model.addColumn(createTableColumn(pricingGroupIndex, "product.pricingGroup"));
            } else {
                model.removeColumn(getColumn(pricingGroupIndex));
            }
            fireTableStructureChanged();
        }
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the table column
     * @param row    the table row
     */
    @Override
    protected Object getValue(IMObject object, TableColumn column, int row) {
        Object result = null;
        if (column.getModelIndex() == pricingGroupIndex) {
            List<Lookup> pricingGroups = rules.getPricingGroups((ProductPrice) object);
            int size = pricingGroups.size();
            if (size == 1) {
                result = pricingGroups.get(0).getName();
            } else if (size > 1) {
                Collections.sort(pricingGroups, IMObjectSorter.getNameComparator(true));
                Column col = ColumnFactory.create();
                for (Lookup lookup : pricingGroups) {
                    Label label = LabelFactory.create();
                    label.setText(lookup.getName());
                    col.add(label);
                }
                result = col;
            }
        } else {
            result = super.getValue(object, column, row);
        }
        return result;
    }
}
