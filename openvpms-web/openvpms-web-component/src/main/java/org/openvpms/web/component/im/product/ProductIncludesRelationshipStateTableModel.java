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

package org.openvpms.web.component.im.product;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.RelationshipState;
import org.openvpms.web.component.im.relationship.RelationshipStateTableModel;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.resource.i18n.Messages;

import java.math.BigDecimal;

/**
 * Table model for <em>entityLink.productIncludes</em>.
 *
 * @author Tim Anderson
 */
public class ProductIncludesRelationshipStateTableModel extends RelationshipStateTableModel {

    /**
     * The low quantity index.
     */
    private static final int LOW_QUANTITY_INDEX = ACTIVE_INDEX + 1;

    /**
     * The high quantity index.
     */
    private static final int HIGH_QUANTITY_INDEX = LOW_QUANTITY_INDEX + 1;

    /**
     * The weight range index.
     */
    private static final int WEIGHT_INDEX = HIGH_QUANTITY_INDEX + 1;

    /**
     * The zero price index.
     */
    private static final int ZERO_PRICE_INDEX = WEIGHT_INDEX + 1;


    /**
     * Constructs a {@link ProductIncludesRelationshipStateTableModel}.
     * <p/>
     * Enables selection if the context is in edit mode.
     *
     * @param context layout context
     */
    public ProductIncludesRelationshipStateTableModel(LayoutContext context) {
        super(context, true);
    }

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if {@code true} sort in ascending order; otherwise sort in {@code descending} order
     * @return the sort criteria, or {@code null} if the column isn't sortable
     */
    @Override
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        return null;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the table column
     * @param row    the table row
     */
    @Override
    protected Object getValue(RelationshipState object, TableColumn column, int row) {
        Object result;
        if (column instanceof DescriptorTableColumn) {
            result = ((DescriptorTableColumn) column).getComponent(object.getRelationship(), getContext());
        } else if (column.getModelIndex() == WEIGHT_INDEX) {
            result = getWeightRange(object);
        } else {
            result = super.getValue(object, column, row);
        }
        return result;
    }

    /**
     * Returns the weight range.
     *
     * @param state the relationship state
     * @return the weight range
     */
    private Object getWeightRange(RelationshipState state) {
        IMObjectBean bean = state.getBean();
        BigDecimal min = bean.getBigDecimal("minWeight", BigDecimal.ZERO);
        BigDecimal max = bean.getBigDecimal("maxWeight", BigDecimal.ZERO);
        if (min.compareTo(BigDecimal.ZERO) == 0 && max.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return Messages.format("product.template.weightrange", min, max,
                               LookupNameHelper.getName(state.getRelationship(), "weightUnits"));
    }

    /**
     * Creates a new column model.
     *
     * @return a new column model
     */
    @Override
    protected TableColumnModel createTableColumnModel() {
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        ArchetypeDescriptor archetype = DescriptorHelper.getArchetypeDescriptor(ProductArchetypes.PRODUCT_INCLUDES);

        model.addColumn(createTableColumn(NAME_INDEX, "table.imobject.name"));
        model.addColumn(new DescriptorTableColumn(LOW_QUANTITY_INDEX, "lowQuantity", archetype));
        model.addColumn(new DescriptorTableColumn(HIGH_QUANTITY_INDEX, "highQuantity", archetype));
        model.addColumn(createTableColumn(WEIGHT_INDEX, "product.template.weight"));
        model.addColumn(new DescriptorTableColumn(ZERO_PRICE_INDEX, "zeroPrice", archetype));
        if (getShowActive()) {
            model.addColumn(createTableColumn(ACTIVE_INDEX, ACTIVE));
        }
        return model;
    }

}
