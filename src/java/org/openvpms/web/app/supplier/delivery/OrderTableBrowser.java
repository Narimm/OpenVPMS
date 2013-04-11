/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2008-2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.supplier.delivery;

import org.openvpms.archetype.rules.supplier.DeliveryStatus;
import org.openvpms.archetype.rules.supplier.OrderRules;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.app.supplier.SupplierHelper;
import org.openvpms.web.component.im.act.ActHierarchyFilter;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.AbstractFilteredResultSet;
import org.openvpms.web.component.im.query.IMObjectTableBrowser;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.query.ResultSetIterator;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.IMTable;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMObjectTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Order browser.
 *
 * @author Tim Anderson
 */
public class OrderTableBrowser extends IMObjectTableBrowser<FinancialAct> {


    /**
     * Determines the types of order to query. If {@code true} query orders
     * suitable for a delivery (i.e have a delivery status that is not
     * {@link DeliveryStatus#FULL}. Otherwise query orders for a return,
     * (i.e have delivery status of {@link DeliveryStatus#FULL}</em>
     */
    private final boolean delivery;


    /**
     * Constructs an {@code OrderTableBrowser}.
     *
     * @param delivery if {@code true} query orders for a delivery, otherwise query orders for a return
     * @param context  the layout context
     */
    public OrderTableBrowser(boolean delivery, LayoutContext context) {
        super(new PostedOrderQuery(!delivery, context), new OrderSelectionTableModel(context), context);
        this.delivery = delivery;
    }

    /**
     * Returns the supplier.
     *
     * @return the supplier, or {@code null} if none is selected
     */
    public Party getSupplier() {
        return ((PostedOrderQuery) getQuery()).getSupplier();
    }

    /**
     * Returns the stock location.
     *
     * @return the stock location, or {@code null} if none is selected
     */
    public Party getStockLocation() {
        return ((PostedOrderQuery) getQuery()).getStockLocation();
    }

    /**
     * Returns the selected order items.
     *
     * @return the selected order items
     */
    public List<FinancialAct> getSelectedOrderItems() {
        List<FinancialAct> result = new ArrayList<FinancialAct>();
        PagedModel model = (PagedModel) getTable().getTable().getModel();
        for (FinancialAct act : model.getSelections()) {
            if (TypeHelper.isA(act, "act.supplierOrderItem")) {
                result.add(act);
            }
        }
        return result;
    }

    /**
     * Performs the query.
     *
     * @return the query result set
     */
    @Override
    protected ResultSet<FinancialAct> doQuery() {
        ResultSet<FinancialAct> set = super.doQuery();
        return new AbstractFilteredResultSet<FinancialAct>(set) {
            private Filter filter = new Filter();

            protected void filter(FinancialAct act,
                                  List<FinancialAct> results) {
                List<FinancialAct> acts = filter.filter(act);
                if (!acts.isEmpty()) {
                    results.add(act);
                    results.addAll(acts);
                }
            }
        };
    }

    /**
     * Creates a new paged table.
     *
     * @param model the table model
     * @return a new paged table
     */
    @Override
    protected PagedIMTable<FinancialAct> createTable(IMTableModel<FinancialAct> model) {
        OrderSelectionTableModel orderModel
                = (OrderSelectionTableModel) model;
        IMObjectTableModel<FinancialAct> pagedModel
                = new PagedModel(orderModel);

        PagedIMTable<FinancialAct> result = super.createTable(pagedModel);
        IMTable<FinancialAct> table = result.getTable();
        OrderSelectionTableCellRenderer renderer
                = new OrderSelectionTableCellRenderer(orderModel);
        table.setDefaultRenderer(Object.class, renderer);
        table.setRolloverEnabled(false);
        table.setSelectionEnabled(false);
        return result;
    }

    /**
     * A paged table model that tracks order selections.
     * <p/>
     * NOTE: this must always be supplied with a result set that guarantees to return the same objects when iterated
     * over multiple times, to ensure internal state consistency.
     */
    private static class PagedModel extends PagedIMObjectTableModel<FinancialAct> {

        /**
         * The orders.
         */
        private List<Order> orders = new ArrayList<Order>();

        /**
         * Map of acts to their corresponding orders.
         */
        private Map<FinancialAct, Order> actsToOrders = new HashMap<FinancialAct, Order>();

        /**
         * Constructs a {@code PagedModel}.
         *
         * @param model the underlying table model
         */
        public PagedModel(OrderSelectionTableModel model) {
            super(model);
            model.setSelectionListener(new OrderSelectionTableModel.OrderSelectionListener() {
                public void onSelected(FinancialAct act, int row, boolean selected) {
                    PagedModel.this.onSelected(act, selected);
                }
            });
        }

        /**
         * Sets the result set.
         *
         * @param set the result set
         */
        @Override
        public void setResultSet(ResultSet<FinancialAct> set) {
            orders.clear();
            actsToOrders.clear();
            ResultSetIterator<FinancialAct> iter = new ResultSetIterator<FinancialAct>(set);
            Order order = null;
            while (iter.hasNext()) {
                FinancialAct act = iter.next();
                if (TypeHelper.isA(act, SupplierArchetypes.ORDER)) {
                    order = new Order();
                    orders.add(order);
                } else {
                    if (order == null) {
                        throw new IllegalStateException("Order item without associated order");
                    }
                    order.addItem(act);
                }
                actsToOrders.put(act, order);
            }
            super.setResultSet(set);
        }

        /**
         * Returns the selected order items.
         *
         * @return the selected order items
         */
        public Set<FinancialAct> getSelections() {
            Set<FinancialAct> result = new HashSet<FinancialAct>();
            for (Order order : orders) {
                for (FinancialAct item : order.getItems()) {
                    if (order.isSelected(item)) {
                        result.add(item);
                    }
                }
            }
            return result;
        }

        /**
         * Sets the objects for the current page.
         *
         * @param objects the objects to set
         */
        @Override
        protected void setPage(List<FinancialAct> objects) {
            OrderSelectionTableModel model = (OrderSelectionTableModel) getModel();
            super.setPage(objects);
            for (int i = 0; i < objects.size(); ++i) {
                FinancialAct act = objects.get(i);
                Order order = actsToOrders.get(act);
                if (order != null) {
                    if (TypeHelper.isA(act, SupplierArchetypes.ORDER) && order.isSelected()) {
                        model.setSelected(i, true);
                    } else if (order.isSelected(act)) {
                        model.setSelected(i, true);
                    }
                }
            }
        }

        /**
         * Invoked when an act is selected/deselected.
         *
         * @param act      the act
         * @param selected if {@code true} indicates the act was selected; if {@code false} indicates deselection
         */
        private void onSelected(FinancialAct act, boolean selected) {
            OrderSelectionTableModel model = (OrderSelectionTableModel) getModel();
            Order order = actsToOrders.get(act);
            if (TypeHelper.isA(act, SupplierArchetypes.ORDER)) {
                order.setSelected(selected);
                for (FinancialAct item : order.getItems()) {
                    order.setSelected(item, selected);
                    int index = model.getObjects().indexOf(item);
                    if (index != -1) {
                        model.setSelected(index, selected);
                    }
                }
            } else {
                order.setSelected(act, selected);
            }
        }

        /**
         * Tracks the selection state of an order and its items.
         */
        private static class Order {

            /**
             * Determines if the order is selected.
             */
            private boolean selected;

            /**
             * The order items, and their selection status.
             */
            private Map<FinancialAct, Boolean> items = new LinkedHashMap<FinancialAct, Boolean>();

            /**
             * Adds an order item.
             *
             * @param item the order item
             */
            public void addItem(FinancialAct item) {
                setSelected(item, false);
            }

            /**
             * Flags the order as selected/deselected. All of the items selection statuses are updated.
             *
             * @param selected if {@code true} selects the order and its items, otherwise deselects them
             */
            public void setSelected(boolean selected) {
                this.selected = selected;
                for (FinancialAct item : getItems()) {
                    setSelected(item, selected);
                }
            }

            /**
             * Determines if the order is selected.
             *
             * @return {@code true} if the order is selected
             */
            public boolean isSelected() {
                return selected;
            }

            /**
             * Flags an order item as selected/deselected.
             *
             * @param item     the order item
             * @param selected if {@code true} selects the items, otherwise deselects it
             */
            public void setSelected(FinancialAct item, boolean selected) {
                items.put(item, selected);
            }

            /**
             * Determines if an order item is selected.
             *
             * @param item the order item
             * @return {@code true} if the order item is selected
             */
            public boolean isSelected(FinancialAct item) {
                Boolean result = items.get(item);
                return result != null ? result : false;
            }

            /**
             * Returns the order items.
             *
             * @return the order items
             */
            public Collection<FinancialAct> getItems() {
                return items.keySet();
            }
        }

    }

    /**
     * An {@link ActHierarchyFilter} that excludes all acts that have FULL
     * delivery status.
     */
    private class Filter extends ActHierarchyFilter<FinancialAct> {

        /**
         * The order rules.
         */
        private final OrderRules rules;


        /**
         * Creates a new {@code Filter}.
         */
        public Filter() {
            super(null);
            rules = SupplierHelper.createOrderRules(getContext().getContext().getPractice());
        }

        /**
         * Determines if an act should be included, after the child items have
         * been determined.
         *
         * @param parent   the top level act
         * @param children the child acts
         * @return {@code true} if there are child acts
         */
        @Override
        protected boolean include(FinancialAct parent, List<FinancialAct> children) {
            return !children.isEmpty();
        }

        /**
         * Determines if a child act should be included.
         *
         * @param child  the child act
         * @param parent the parent act
         */
        @Override
        protected boolean include(FinancialAct child, FinancialAct parent) {
            DeliveryStatus status = rules.getDeliveryStatus(child);
            return (delivery)
                    ? status != DeliveryStatus.FULL
                    : status == DeliveryStatus.FULL;
        }
    }
}
