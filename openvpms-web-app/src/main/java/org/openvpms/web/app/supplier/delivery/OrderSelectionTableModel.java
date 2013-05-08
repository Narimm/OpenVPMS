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
 */

package org.openvpms.web.app.supplier.delivery;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.DescriptorTableModel;
import org.openvpms.web.echo.event.ActionListener;

import java.util.ArrayList;
import java.util.List;


/**
 * Supplier order table model that supports the selection of multiple orders and their items via a checkbox column.
 *
 * @author Tim Anderson
 */
public class OrderSelectionTableModel extends DescriptorTableModel<FinancialAct> {

    /**
     * Listener for order/order item selection events
     */
    public interface OrderSelectionListener {

        /**
         * Invoked when an order/order item is selected or deselected.
         *
         * @param act      the order/order item
         * @param row      the selected row
         * @param selected if {@code true} indicates the act was selected; if {@code false} indicates deselection
         */
        void onSelected(FinancialAct act, int row, boolean selected);
    }

    /**
     * The selected column index.
     */
    private int selectedIndex;

    /**
     * The start time column index.
     */
    private int startTimeIndex;

    /**
     * The title column index.
     */
    private int titleIndex;

    /**
     * Selection check boxes.
     */
    private List<CheckBox> selected = new ArrayList<CheckBox>();

    /**
     * The selection listener.
     */
    private OrderSelectionListener listener;


    /**
     * Constructs an {@code OrderSelectionTableModel}.
     */
    public OrderSelectionTableModel(LayoutContext context) {
        super(new String[]{"act.supplierOrderItem"}, context);
    }

    /**
     * Sets the objects to display.
     *
     * @param objects the objects to display
     */
    @Override
    public void setObjects(List<FinancialAct> objects) {
        super.setObjects(objects);
        selected = new ArrayList<CheckBox>(objects.size());
        for (int i = 0; i < objects.size(); ++i) {
            selected.add(new Toggle(i));
        }
    }

    /**
     * Selects/deselects an act.
     *
     * @param index    the row of the object to select
     * @param selected if {@code true}, select the act, otherwise deselect it
     */
    public void setSelected(int index, boolean selected) {
        this.selected.get(index).setSelected(selected);
    }

    /**
     * Returns the row selection index.
     *
     * @return the row selection index
     */
    public int getSelectionColumnIndex() {
        return selectedIndex;
    }

    /**
     * Returns the list of selected acts.
     *
     * @return the selected acts
     */
    public List<FinancialAct> getSelected() {
        List<FinancialAct> result = new ArrayList<FinancialAct>();
        List<FinancialAct> acts = getObjects();

        for (int i = 0; i < selected.size(); ++i) {
            CheckBox box = selected.get(i);
            if (box.isSelected()) {
                result.add(acts.get(i));
            }
        }
        return result;
    }

    /**
     * Listener to be notified when a item is selected.
     *
     * @param listener the listener. May be {@code null}
     */
    public void setSelectionListener(OrderSelectionListener listener) {
        this.listener = listener;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the table column
     * @param row    the table row
     */
    @Override
    protected Object getValue(FinancialAct object, TableColumn column, final int row) {
        if (column.getModelIndex() == selectedIndex) {
            return selected.get(row);
        }
        return super.getValue(object, column, row);
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
    protected Object getValue(FinancialAct object, DescriptorTableColumn column, int row) {
        Object result = null;
        int index = column.getModelIndex();
        boolean orderColumn = (index == startTimeIndex || index == titleIndex);
        if (TypeHelper.isA(object, "act.supplierOrder")) {
            if (orderColumn) {
                result = super.getValue(object, column, row);
            }
        } else if (!orderColumn) {
            result = super.getValue(object, column, row);
        }
        return result;
    }

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if <code>true</code> sort in ascending order; otherwise
     *                  sort in <code>descending</code> order
     * @return the sort criteria, or <code>null</code> if the column isn't
     *         sortable
     */
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        return null;
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
        TableColumnModel model = super.createColumnModel(archetypes, context);
        TableColumn selected = new TableColumn(getNextModelIndex(model));
        selectedIndex = selected.getModelIndex();
        model.addColumn(selected);
        model.moveColumn(model.getColumnCount() - 1, 0);

        ArchetypeDescriptor archetype
            = DescriptorHelper.getArchetypeDescriptor("act.supplierOrder");
        if (archetype != null) {
            TableColumn startTime = addColumn(archetype, "startTime", model);
            startTimeIndex = startTime.getModelIndex();
            model.moveColumn(model.getColumnCount() - 1, 1);

            TableColumn title = addColumn(archetype, "title", model);
            titleIndex = title.getModelIndex();
            model.moveColumn(model.getColumnCount() - 1, 2);
        }
        return model;
    }

    /**
     * Invoked when a checkbox is selected.
     *
     * @param row      the row
     * @param selected indicates if the checkbox was selected or deselected
     */
    private void onSelected(int row, boolean selected) {
        if (listener != null) {
            List<FinancialAct> acts = getObjects();
            FinancialAct act = acts.get(row);
            listener.onSelected(act, row, selected);
        }

    }

    private class Toggle extends CheckBox {

        public Toggle(final int row) {
            addActionListener(new ActionListener() {
                public void onAction(ActionEvent e) {
                    onSelected(row, isSelected());
                }
            });
        }

    }
}
