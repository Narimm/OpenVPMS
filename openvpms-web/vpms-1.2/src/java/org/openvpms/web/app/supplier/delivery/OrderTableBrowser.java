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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.supplier.delivery;

import org.openvpms.archetype.rules.supplier.DeliveryStatus;
import org.openvpms.archetype.rules.supplier.OrderRules;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.act.ActHierarchyFilter;
import org.openvpms.web.component.im.query.AbstractFilteredResultSet;
import org.openvpms.web.component.im.query.IMObjectTableBrowser;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.IMTable;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMObjectTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Patient medical record browser.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OrderTableBrowser extends IMObjectTableBrowser<FinancialAct> {


    /**
     * Determines the types of order to query. If <tt>true</tt> query orders
     * suitable for a delivery (i.e have a delivery status that is not
     * {@link DeliveryStatus#FULL}. Otherwise query orders for a return,
     * (i.e have delivery status of {@link DeliveryStatus#FULL}</em>
     */
    private final boolean delivery;


    /**
     * Creates a new <tt>OrderTableBrowser</tt>.
     *
     * @param delivery if <tt>true</tt> query orders for a delivery, otherwise
     *                 query orders for a return
     */
    public OrderTableBrowser(boolean delivery) {
        super(new PostedOrderQuery(!delivery), new OrderSelectionTableModel());
        this.delivery = delivery;
    }

    /**
     * Returns the supplier.
     *
     * @return the supplier, or <tt>null</tt> if none is selected
     */
    public Party getSupplier() {
        return ((PostedOrderQuery) getQuery()).getSupplier();
    }

    /**
     * Returns the stock location.
     *
     * @return the stock location, or <tt>null</tt> if none is selected
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
                results.addAll(acts);
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
    protected PagedIMTable<FinancialAct>
            createTable(IMTableModel<FinancialAct> model) {
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
     */
    private static class PagedModel
            extends PagedIMObjectTableModel<FinancialAct> {

        private Set<FinancialAct> selections = new HashSet<FinancialAct>();

        /**
         * Construct a new <tt>PagedModel</tt>.
         *
         * @param model the underlying table model
         */
        public PagedModel(OrderSelectionTableModel model) {
            super(model);
        }

        /**
         * Returns the selected orders.
         *
         * @return the selected orders
         */
        public Set<FinancialAct> getSelections() {
            OrderSelectionTableModel model
                    = (OrderSelectionTableModel) getModel();
            updateSelections(model);
            return selections;
        }

        /**
         * Sets the objects for the current page.
         *
         * @param objects the objects to set
         */
        @Override
        protected void setPage(List<FinancialAct> objects) {
            OrderSelectionTableModel model
                    = (OrderSelectionTableModel) getModel();
            updateSelections(model);
            super.setPage(objects);
            for (int i = 0; i < objects.size(); ++i) {
                if (selections.contains(objects.get(i))) {
                    model.setSelected(i);
                }
            }
        }

        /**
         * Updates selections from the model.
         *
         * @param model the model
         */
        private void updateSelections(OrderSelectionTableModel model) {
            List<FinancialAct> selected = model.getSelected();
            List<FinancialAct> unselected
                    = new ArrayList<FinancialAct>(model.getObjects());
            unselected.removeAll(selected);
            selections.addAll(selected);
            selections.removeAll(unselected);
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
         * Creates a new <tt>Flattener</tt>.
         */
        public Filter() {
            super(null);
            rules = new OrderRules();
        }

        /**
         * Determines if an act should be included, after the child items have
         * been determined.
         *
         * @param parent   the top level act
         * @param children the child acts
         * @return <tt>true</tt> if there are child acts
         */
        @Override
        protected boolean include(FinancialAct parent,
                                  List<FinancialAct> children) {
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
