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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.table.act;

import echopointng.layout.TableLayoutDataEx;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.act.ActHelper;
import org.openvpms.web.component.im.table.BaseIMObjectTableModel;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.resource.i18n.format.NumberFormatter;

import java.math.BigDecimal;
import java.util.Date;


/**
 * Table model for {@link Act}s containing amount fields.
 *
 * @author Tim Anderson
 */
public class ActAmountTableModel<T extends Act> extends BaseIMObjectTableModel<T> {

    /**
     * Date column index.
     */
    protected static final int DATE_INDEX = NEXT_INDEX;

    /**
     * Status column index.
     */
    protected static final int STATUS_INDEX = DATE_INDEX + 1;

    /**
     * Amount column index.
     */
    protected static final int AMOUNT_INDEX = STATUS_INDEX + 1;

    /**
     * Determines if the credit/debit amount should be negated.
     */
    private final boolean negateAmount;


    /**
     * Constructs an {@code ActAmountTableModel}.
     */
    public ActAmountTableModel() {
        this(true, false);
    }

    /**
     * Constructs a {@code ActAmountTableModel}.
     *
     * @param showStatus determines if the status column should be displayed
     * @param showAmount determines if the credit/debit amount should be displayed
     */
    public ActAmountTableModel(boolean showStatus, boolean showAmount) {
        this(true, showStatus, showAmount);
    }

    /**
     * Constructs a {@code ActAmountTableModel}.
     *
     * @param showArchetype determines if the archetype column should be displayed
     * @param showStatus    determines if the status column should be displayed
     * @param showAmount    determines if the credit/debit amount should be displayed
     */
    public ActAmountTableModel(boolean showArchetype, boolean showStatus, boolean showAmount) {
        this(showArchetype, showStatus, showAmount, false);
    }

    /**
     * Constructs a {@code ActAmountTableModel}.
     *
     * @param showArchetype determines if the archetype column should be displayed
     * @param showStatus    determines if the status column should be displayed
     * @param showAmount    determines if the credit/debit amount should be displayed
     * @param negateAmount  determines if the credit/debit amount should be negated
     */
    public ActAmountTableModel(boolean showArchetype, boolean showStatus, boolean showAmount,
                               boolean negateAmount) {
        super(null);
        setTableColumnModel(createColumnModel(showArchetype, showStatus, showAmount));
        this.negateAmount = negateAmount;
    }

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if {@code true} sort in ascending order; otherwise
     *                  sort in {@code descending} order
     * @return the sort criteria, or {@code null} if the column isn't
     *         sortable
     */
    @Override
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        SortConstraint[] result;
        switch (column) {
            case DATE_INDEX:
                result = new SortConstraint[]{
                        new NodeSortConstraint("startTime", ascending)
                };
                break;
            case STATUS_INDEX:
                result = new SortConstraint[]{
                        new NodeSortConstraint("status", ascending)
                };
                break;
            case AMOUNT_INDEX:
                result = new SortConstraint[]{
                        new NodeSortConstraint("amount", ascending)};
                break;
            default:
                result = super.getSortConstraints(column, ascending);
                break;
        }
        return result;
    }

    /**
     * Helper to create a column model.
     *
     * @param showArchetype determines if the archetype column should be displayed
     * @param showStatus    determines if the status column should be displayed
     * @param showAmount    determines if the credit/debit amount should be
     *                      displayed   @return a new column model
     * @return a new column model
     */
    protected TableColumnModel createColumnModel(boolean showArchetype, boolean showStatus, boolean showAmount) {
        TableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(createTableColumn(DATE_INDEX, "table.act.date"));
        if (showArchetype) {
            model.addColumn(createTableColumn(ARCHETYPE_INDEX, "table.act.type"));
        }
        if (showStatus) {
            model.addColumn(createTableColumn(STATUS_INDEX, "table.act.status"));
        }
        if (showAmount) {
            model.addColumn(createTableColumn(AMOUNT_INDEX, "table.act.amount"));
        }
        model.addColumn(createTableColumn(DESCRIPTION_INDEX, "table.act.description"));
        return model;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param act    the act
     * @param column the table column
     * @param row    the table row
     * @return the value at the given coordinate
     */
    @Override
    protected Object getValue(T act, TableColumn column, int row) {
        Object result = null;
        switch (column.getModelIndex()) {
            case DATE_INDEX:
                Date date = act.getActivityStartTime();
                if (date != null) {
                    result = DateFormatter.formatDate(date, false);
                }
                break;
            case STATUS_INDEX:
                result = getStatus(act);
                break;
            case AMOUNT_INDEX:
                result = getAmount(act);
                break;
            default:
                result = super.getValue(act, column, row);
                break;
        }
        return result;
    }

    /**
     * Helper to determine the next available model index.
     *
     * @param columns the columns
     * @return the next available model index.
     */
    @Override
    protected int getNextModelIndex(TableColumnModel columns) {
        return super.getNextModelIndex(columns, AMOUNT_INDEX + 1);
    }

    /**
     * Returns an amount from an act.
     *
     * @param act the act
     * @return the stringified amount
     */
    protected Label getAmount(Act act) {
        BigDecimal amount = ActHelper.getAmount(act, "amount");
        if (negateAmount) {
            amount = amount.negate();
        }
        String result = NumberFormatter.format(amount);
        Label label = LabelFactory.create();
        label.setText(result);
        TableLayoutData layout = new TableLayoutDataEx();
        Alignment right = new Alignment(Alignment.RIGHT, Alignment.DEFAULT);
        layout.setAlignment(right);
        label.setLayoutData(layout);
        return label;
    }

    /**
     * Helper to return the display version of an act's status.
     *
     * @param act the act
     * @return the display version of an act's status, or the status if it can't be determined
     */
    protected String getStatus(Act act) {
        String result = null;
        ArchetypeDescriptor archetype = DescriptorHelper.getArchetypeDescriptor(act);
        if (archetype != null) {
            NodeDescriptor status = archetype.getNodeDescriptor("status");
            if (status != null) {
                result = LookupNameHelper.getLookupName(status, act);
            }
        }
        if (result == null) {
            result = act.getStatus();
        }
        return result;
    }
}
