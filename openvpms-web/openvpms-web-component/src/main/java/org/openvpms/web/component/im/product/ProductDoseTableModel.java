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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.product;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.DescriptorTableModel;
import org.openvpms.web.resource.i18n.Messages;

import java.util.List;

/**
 * Table model for <em>entityLink.productDoses</em>.
 *
 * @author Tim Anderson
 */
public class ProductDoseTableModel extends DescriptorTableModel<IMObject> {

    /**
     * The species index.
     */
    private static final int SPECIES_INDEX = ACTIVE_INDEX + 1;

    /**
     * The weight index.
     */
    private static final int WEIGHT_INDEX = SPECIES_INDEX + 1;

    /**
     * The rate index.
     */
    private static final int RATE_INDEX = WEIGHT_INDEX + 1;

    /**
     * The quantity index.
     */
    private static final int QUANTITY_INDEX = RATE_INDEX + 1;


    /**
     * Constructs a {@link ProductDoseTableModel}.
     * <p>
     * Enables selection if the context is in edit mode.
     *
     * @param shortNames the archetype short names
     * @param context    layout context
     */
    public ProductDoseTableModel(String[] shortNames, LayoutContext context) {
        super(shortNames, context);
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
    protected Object getValue(IMObject object, TableColumn column, int row) {
        Object result;
        switch (column.getModelIndex()) {
            case SPECIES_INDEX:
                result = getSpecies(object);
                break;
            case WEIGHT_INDEX:
                result = WeightRangeTableHelper.getWeightRange(object);
                break;
            default:
                result = super.getValue(object, column, row);
        }
        return result;
    }

    /**
     * Creates a column model for one or more archetypes.
     * If there are multiple archetypes, the intersection of the descriptors
     * will be used.
     *
     * @param archetypes the archetypes
     * @param context    the layout context
     * @return a new column model
     */
    @Override
    protected TableColumnModel createColumnModel(List<ArchetypeDescriptor> archetypes, LayoutContext context) {
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(createColumn(SPECIES_INDEX, ProductArchetypes.DOSE, "species"));
        model.addColumn(createTableColumn(WEIGHT_INDEX, "product.weight"));
        model.addColumn(new DescriptorTableColumn(RATE_INDEX, "rate", archetypes));
        model.addColumn(new DescriptorTableColumn(QUANTITY_INDEX, "quantity", archetypes));
        return model;
    }

    /**
     * Returns the species that the dose applies to.
     *
     * @param object the dose
     * @return the species
     */
    private String getSpecies(IMObject object) {
        IMObjectBean bean = new IMObjectBean(object);
        List<Lookup> values = bean.getValues("species", Lookup.class);
        return !values.isEmpty() ? values.get(0).getName() : Messages.get("list.all");
    }

    /**
     * Helper to create a column using a node display name as the title.
     *
     * @param modelIndex the column model index
     * @param shortName  the archetype short name
     * @param node       the node name
     * @return a new column
     */
    private TableColumn createColumn(int modelIndex, String shortName, String node) {
        TableColumn column = new TableColumn(modelIndex);
        column.setHeaderValue(DescriptorHelper.getDisplayName(shortName, node));
        return column;
    }

}
