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

package org.openvpms.web.workspace.customer.charge;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.cache.IMObjectCache;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.UserPreferences;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.DescriptorTableModel;
import org.openvpms.web.component.im.util.VirtualNodeSortConstraint;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;

/**
 * A table model for charges and estimates that allows the template and product type columns to be shown or suppressed.
 * <p/>
 * The context {@link UserPreferences} initially determine if the columns are shown or hidden.
 *
 * @author Tim Anderson
 */
public class ChargeItemTableModel<T extends IMObject> extends DescriptorTableModel<T> {

    /**
     * Determines if the template column should be shown.
     */
    private boolean showTemplate;

    /**
     * Determines if the product type column should be shown.
     */
    private boolean showProductType;

    /**
     * The product column index.
     */
    private int productIndex;

    /**
     * The template column index.
     */
    private int templateIndex;

    /**
     * The product type column index.
     */
    private int productTypeIndex;

    /**
     * The product type column.
     */
    private TableColumn productType;

    /**
     * The template column.
     */
    private TableColumn template;

    /**
     * Used to access the product type name node from a charge/estimate item.
     */
    private static final String PRODUCT_TYPE_NODE = "product.entity.type.source.name";

    /**
     * Constructs a {@link ChargeItemTableModel}.
     *
     * @param shortNames the archetype short names
     * @param context    the layout context
     */
    public ChargeItemTableModel(String[] shortNames, LayoutContext context) {
        super(context);
        UserPreferences preferences = ServiceHelper.getPreferences();
        showTemplate = preferences.getShowTemplateDuringCharging();
        showProductType = preferences.getShowProductTypeDuringCharging();
        setTableColumnModel(createColumnModel(shortNames, context));
        if (showTemplate) {
            setDefaultSortColumn(templateIndex);
        } else if (showProductType) {
            setDefaultSortColumn(productTypeIndex);
        } else {
            DescriptorTableColumn startTime = getColumn("startTime");
            if (startTime != null) {
                setDefaultSortColumn(startTime.getModelIndex());
                setDefaultSortAscending(false);
            } else {
                setDefaultSortColumn(getColumnModel().getColumn(0).getModelIndex());
            }
        }
    }

    /**
     * Determines if the product type column is shown.
     *
     * @param show if {@code true}, show the column
     */
    public void setShowProductType(boolean show) {
        if (show != showProductType) {
            doShowProductType(show, getColumnModel());
        }
    }

    /**
     * Determines if the template column is shown.
     *
     * @param show if {@code true}, show the column
     */
    public void setShowTemplate(boolean show) {
        if (show != showTemplate) {
            doShowTemplate(show, getColumnModel());
        }
    }

    /**
     * Returns the column model.
     *
     * @return the column model
     */
    @Override
    public DefaultTableColumnModel getColumnModel() {
        return (DefaultTableColumnModel) super.getColumnModel();
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the table column
     * @param row    the table row
     */
    @Override
    protected Object getValue(T object, TableColumn column, int row) {
        if (column.getModelIndex() == productTypeIndex) {
            return getProductType(object);
        }
        return super.getValue(object, column, row);
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
        if (column == productTypeIndex) {
            return new SortConstraint[]{new VirtualNodeSortConstraint(PRODUCT_TYPE_NODE, ascending),
                                        new NodeSortConstraint("startTime", false)};
        } else if (column == templateIndex) {
            return new SortConstraint[]{new NodeSortConstraint("template", ascending),
                                        new VirtualNodeSortConstraint(PRODUCT_TYPE_NODE, ascending),
                                        new NodeSortConstraint("startTime", false)};
        }
        return super.getSortConstraints(column, ascending);
    }

    /**
     * Creates a column model.
     *
     * @param archetypes the archetypes
     * @param context    the layout context
     * @return a new column model
     */
    @Override
    protected TableColumnModel createColumnModel(List<ArchetypeDescriptor> archetypes, LayoutContext context) {
        DefaultTableColumnModel model = (DefaultTableColumnModel) super.createColumnModel(archetypes, context);
        productIndex = getColumn(model, "product").getModelIndex();
        productTypeIndex = templateIndex + 1;
        productType = new TableColumn(productTypeIndex);
        productType.setHeaderValue(DescriptorHelper.getDisplayName(ProductArchetypes.PRODUCT_TYPE));
        templateIndex = getNextModelIndex(model);
        template = new DescriptorTableColumn(templateIndex, "template", archetypes);

        if (showProductType) {
            doShowProductType(true, model);
        }
        if (showTemplate) {
            doShowTemplate(true, model);
        }
        return model;
    }

    /**
     * Returns a component representing the product type.
     *
     * @param object the parent object
     * @return the product type component, or {@code null} if the product has no product type
     */
    private Component getProductType(IMObject object) {
        ActBean bean = new ActBean((Act) object);
        LayoutContext context = getLayoutContext();
        IMObjectCache cache = context.getCache();
        Product product = (Product) cache.get(bean.getNodeParticipantRef("product"));
        if (product != null) {
            IMObjectBean productBean = new IMObjectBean(product);
            IMObjectReference type = productBean.getNodeSourceObjectRef("type");
            if (type != null) {
                return new IMObjectReferenceViewer(type, null, context.getContext()).getComponent();
            }
        }
        return null;
    }

    /**
     * Shows/hides the template column.
     *
     * @param show  if {@code true}, show it, otherwise hide it
     * @param model the model
     */
    private void doShowTemplate(boolean show, DefaultTableColumnModel model) {
        int before = showProductType ? productTypeIndex : productIndex;
        showTemplate = show(template, show, before, model);
    }

    /**
     * Shows/hides the product type column.
     *
     * @param show  if {@code true}, show it, otherwise hide it
     * @param model the model
     */
    private void doShowProductType(boolean show, DefaultTableColumnModel model) {
        showProductType = show(productType, show, productIndex, model);
    }

    /**
     * Shows/hides a column.
     *
     * @param column the column
     * @param show   if {@code true}, show it, otherwise hide it
     * @param before the model index of the column to place the column before, if its being shown
     * @param model  the model
     * @return {@code show}
     */
    private boolean show(TableColumn column, boolean show, int before, DefaultTableColumnModel model) {
        if (show) {
            model.addColumn(column);
            int columnOffset = getColumnOffset(model, before);
            if (columnOffset != -1) {
                model.moveColumn(model.getColumnCount() - 1, columnOffset);
            }
        } else {
            model.removeColumn(column);
        }
        fireTableStructureChanged();
        return show;
    }

}
