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

package org.openvpms.web.workspace.customer.order;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.finance.order.OrderArchetypes;
import org.openvpms.archetype.rules.finance.order.OrderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.act.PagedActHierarchyTableModel;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.AbstractQueryBrowser;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.DescriptorTableModel;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Browser for orders requiring charging.
 *
 * @author Tim Anderson
 */
public class PendingOrderBrowser extends AbstractQueryBrowser<Act> {

    /**
     * The order item short names.
     */
    private static final String[] ITEM_SHORT_NAMES = new String[]{OrderArchetypes.PHARMACY_ORDER_ITEM,
                                                                  OrderArchetypes.PHARMACY_RETURN_ITEM};

    /**
     * Constructs an {@link PendingOrderBrowser}.
     *
     * @param query   the query
     * @param context the layout context
     */
    public PendingOrderBrowser(PendingOrderQuery query, LayoutContext context) {
        super(query, null, new PagedActHierarchyTableModel<Act>(new OrderTableModel(context), context.getContext(),
                                                                ITEM_SHORT_NAMES), context);
    }

    /**
     * Lays out this component.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        container.add(LabelFactory.create("customer.order.invoice.message"));
    }

    /**
     * Returns the selected orders.
     *
     * @return the selected orders
     */
    public List<Act> getOrders() {
        PagedActHierarchyTableModel<Act> paged = (PagedActHierarchyTableModel<Act>) getTableModel();
        OrderTableModel model = (OrderTableModel) paged.getModel();
        return model.getOrders();
    }


    private static class OrderTableModel extends DescriptorTableModel<Act> {

        /**
         * The patient column model index.
         */
        private int patientIndex;

        /**
         * The product column model index.
         */
        private int productIndex;

        /**
         * The quantity column model index.
         */
        private int quantityIndex;

        /**
         * The clinician column model index.
         */
        private int clinicianIndex;

        /**
         * The invoiced quantity model index.
         */
        private int invoicedIndex;

        /**
         * The selected column model index.
         */
        private int selectedIndex;

        /**
         * The selected orders.
         */
        private Set<IMObjectReference> selected = new HashSet<IMObjectReference>();

        /**
         * The order rules.
         */
        private final OrderRules rules;

        /**
         * Constructs an {@link OrderTableModel}.
         *
         * @param context the layout context
         */
        public OrderTableModel(LayoutContext context) {
            super(PendingOrderQuery.SHORT_NAMES, context);
            rules = ServiceHelper.getBean(OrderRules.class);
        }

        /**
         * Returns the selected orders.
         *
         * @return the selected orders
         */
        public List<Act> getOrders() {
            List<Act> result = new ArrayList<Act>();
            for (IMObjectReference reference : selected) {
                IMObject object = IMObjectHelper.getObject(reference, null);
                if (object != null) {
                    result.add((Act) object);
                }
            }
            return result;
        }

        /**
         * Returns a list of node descriptor names to include in the table.
         * This implementation returns {@code null} to indicate that the
         * intersection should be calculated from all descriptors.
         *
         * @return the list of node descriptor names to include in the table
         */
        @Override
        protected String[] getNodeNames() {
            return new String[]{"id", "status", "startTime", "clinician"};
        }

        /**
         * Returns the value found at the given coordinate within the table.
         *
         * @param object the object
         * @param column the table column
         * @param row    the table row
         */
        @Override
        protected Object getValue(Act object, TableColumn column, int row) {
            Object result = null;
            int index = column.getModelIndex();
            if (TypeHelper.isA(object, ITEM_SHORT_NAMES)) {
                if (index == clinicianIndex || index == patientIndex || index == productIndex
                    || index == quantityIndex) {
                    result = super.getValue(object, column, row);
                } else if (index == invoicedIndex) {
                    result = rules.getInvoicedQuantity(object);
                } else {
                    result = null;
                }
            } else if (index == selectedIndex) {
                result = getCheckBox(object);
            } else if (index != invoicedIndex) {
                result = super.getValue(object, column, row);
            }
            return result;
        }

        /**
         * Creates a column model for a set of archetypes.
         *
         * @param shortNames the archetype short names
         * @param context    the layout context
         * @return a new column model
         */
        @Override
        protected TableColumnModel createColumnModel(String[] shortNames, LayoutContext context) {
            List<ArchetypeDescriptor> archetypes = DescriptorHelper.getArchetypeDescriptors(ITEM_SHORT_NAMES);
            TableColumnModel columns = super.createColumnModel(shortNames, context);
            DescriptorTableColumn clinician = getColumn(columns, "clinician");
            clinicianIndex = clinician != null ? clinician.getModelIndex() : -1;

            selectedIndex = getNextModelIndex(columns);
            columns.addColumn(new TableColumn(selectedIndex));
            columns.moveColumn(columns.getColumnCount() - 1, 0);

            patientIndex = selectedIndex + 1;
            productIndex = patientIndex + 1;
            quantityIndex = productIndex + 1;
            invoicedIndex = quantityIndex + 1;
            addColumn(archetypes, "patient", patientIndex, columns);
            addColumn(archetypes, "product", productIndex, columns);
            addColumn(archetypes, "quantity", quantityIndex, columns);
            columns.addColumn(createTableColumn(invoicedIndex, "customer.order.invoiceqty"));
            return columns;
        }

        /**
         * Returns a check box to select an order.
         *
         * @param object the order
         * @return a new check box
         */
        private CheckBox getCheckBox(final Act object) {
            final CheckBox checkBox = CheckBoxFactory.create(selected.contains(object.getObjectReference()));
            checkBox.addActionListener(new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    IMObjectReference reference = object.getObjectReference();
                    if (checkBox.isSelected()) {
                        selected.add(reference);
                    } else {
                        selected.remove(reference);
                    }
                }
            });
            return checkBox;
        }

    }
}
