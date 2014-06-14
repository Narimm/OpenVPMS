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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.PeriodRelationship;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.ContextSwitchListener;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.AbstractIMObjectTableModel;
import org.openvpms.web.component.im.util.VirtualNodeSortConstraint;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.util.Date;
import java.util.List;

import static org.openvpms.component.system.common.query.Constraints.sort;


/**
 * Product batch table model.
 *
 * @author Tim Anderson
 */
public class ProductBatchTableModel extends AbstractIMObjectTableModel<Entity> {

    /**
     * The layout context.
     */
    private final LayoutContext context;

    /**
     * The id column index.
     */
    private static final int ID_INDEX = 0;

    /**
     * The batch number column index.
     */
    private static final int BATCH_NUMBER_INDEX = 1;

    /**
     * The product column index.
     */
    private static final int PRODUCT_INDEX = 2;

    /**
     * The expiry date column index.
     */
    private static final int EXPIRY_DATE_INDEX = 3;

    /**
     * The manufacturer column index.
     */
    private static final int MANUFACTURER_INDEX = 4;


    /**
     * Constructs a {@link ProductBatchTableModel}.
     *
     * @param context the layout context
     */
    public ProductBatchTableModel(LayoutContext context) {
        this(context, true, true);
    }

    /**
     * Constructs a {@link ProductBatchTableModel}.
     *
     * @param context     the layout context
     * @param showId      if {@code true}, show the id column
     * @param showProduct if {@code true}, show the product column
     */
    public ProductBatchTableModel(LayoutContext context, boolean showId, boolean showProduct) {
        this.context = context;
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        if (showId) {
            model.addColumn(createTableColumn(ID_INDEX, ID));
        }
        model.addColumn(createTableColumn(BATCH_NUMBER_INDEX, ProductArchetypes.PRODUCT_BATCH, "name"));
        if (showProduct) {
            model.addColumn(createTableColumn(PRODUCT_INDEX, ProductArchetypes.PRODUCT_BATCH, "product"));
        }
        model.addColumn(createTableColumn(EXPIRY_DATE_INDEX, "entityLink.batchProduct", "activeEndTime"));
        model.addColumn(createTableColumn(MANUFACTURER_INDEX, ProductArchetypes.PRODUCT_BATCH, "manufacturer"));
        setTableColumnModel(model);
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
        SortConstraint[] result;
        switch (column) {
            case ID_INDEX:
                result = new SortConstraint[]{sort("id", ascending)};
                break;
            case BATCH_NUMBER_INDEX:
                result = new SortConstraint[]{sort("name", ascending), sort("id", true)};
                break;
            case PRODUCT_INDEX:
                result = new SortConstraint[]{sort("product", ascending), sort("name", ascending), sort("id", true)};
                break;
            case EXPIRY_DATE_INDEX:
                result = new SortConstraint[]{new VirtualNodeSortConstraint("expiryDate", ascending),
                                              sort("name", true), sort("id", true)};
                break;
            case MANUFACTURER_INDEX:
                result = new SortConstraint[]{sort("manufacturer", ascending), sort("name", ascending),
                                              sort("id", true)};
                break;
            default:
                result = null;
        }
        return result;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the column
     * @param row    the row
     * @return the value at the given coordinate.
     */
    @Override
    protected Object getValue(Entity object, TableColumn column, int row) {
        Object result = null;
        int index = column.getModelIndex();
        if (index == ID_INDEX) {
            result = object.getId();
        } else if (index == BATCH_NUMBER_INDEX) {
            result = object.getName();
        } else if (index == PRODUCT_INDEX) {
            result = createReferenceViewer(object, "product");
        } else if (index == EXPIRY_DATE_INDEX) {
            result = getExpiryDate(object);
        } else if (index == MANUFACTURER_INDEX) {
            result = createReferenceViewer(object, "manufacturer");
        }
        return result;
    }

    /**
     * Creates a reference viewer for a relationship target.
     *
     * @param object the batch
     * @param node   the relationship node
     * @return a new viewer
     */
    private Component createReferenceViewer(Entity object, String node) {
        EntityBean bean = new EntityBean(object);
        ContextSwitchListener listener = (context.isEdit()) ? null : context.getContextSwitchListener();

        IMObjectReference ref = bean.getNodeTargetObjectRef(node, false);
        IMObjectReferenceViewer viewer = new IMObjectReferenceViewer(ref, listener, context.getContext());
        return viewer.getComponent();
    }

    /**
     * Returns the expiry date.
     *
     * @param object the batch
     * @return the batch expiry. May be {@code null}
     */
    private String getExpiryDate(Entity object) {
        EntityBean bean = new EntityBean(object);
        List<PeriodRelationship> values = bean.getValues("product", PeriodRelationship.class);
        Date expiry = null;
        if (!values.isEmpty()) {
            expiry = values.get(0).getActiveEndTime();
        }
        return (expiry != null) ? DateFormatter.formatDate(expiry, false) : null;
    }

    /**
     * Creates a table column, using a node display name as the header value.
     *
     * @param index     the column index
     * @param shortName the archetype short name
     * @param node      the archetype node
     * @return a new column
     */
    private TableColumn createTableColumn(int index, String shortName, String node) {
        TableColumn column = new TableColumn(index);
        column.setHeaderValue(DescriptorHelper.getDisplayName(shortName, node));
        return column;
    }

}
