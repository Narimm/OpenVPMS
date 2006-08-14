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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.table.act;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.edit.act.ActHelper;
import org.openvpms.web.component.im.table.BaseIMObjectTableModel;
import org.openvpms.web.component.util.DateFormatter;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.NumberFormatter;
import org.openvpms.web.resource.util.Messages;

import java.math.BigDecimal;
import java.util.Date;


/**
 * Table model for {@link Act}s containing amount fields.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ActAmountTableModel extends BaseIMObjectTableModel {
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
     * Construct a new <code>ActAmountTableModel</code>.
     */
    public ActAmountTableModel() {
        this(true, false);
    }

    /**
     * Construct a new <code>ActAmountTableModel</code>.
     *
     * @param showStatus determines if the status colunn should be displayed
     * @param showAmount determines if the credit/debit amount should be
     *                   displayed
     */
    public ActAmountTableModel(boolean showStatus, boolean showAmount) {
        super(createColumnModel(showStatus, showAmount));
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
                result = null; // can only sort on top level nodes
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
     * @param showStatus determines if the status colunn should be displayed
     * @param showAmount determines if the credit/debit amount should be
     *                   displayed
     * @return a new column model
     */
    protected static TableColumnModel createColumnModel(boolean showStatus,
                                                        boolean showAmount) {
        TableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(createTableColumn(DATE_INDEX, "table.act.date"));
        model.addColumn(createTableColumn(ARCHETYPE_INDEX, "table.act.type"));
        if (showStatus) {
            model.addColumn(createTableColumn(STATUS_INDEX,
                                              "table.act.status"));
        }
        if (showAmount) {
            model.addColumn(createTableColumn(AMOUNT_INDEX,
                                              "table.act.amount"));
        }
        model.addColumn(createTableColumn(DESCRIPTION_INDEX,
                                          "table.act.description"));
        return model;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object the object
     * @param column
     * @param row    the table row
     */
    @Override
    protected Object getValue(IMObject object, int column, int row) {
        Act act = (Act) object;
        Object result = null;
        switch (column) {
            case DATE_INDEX:
                Date date = act.getActivityStartTime();
                if (date != null) {
                    result = DateFormatter.format(date, false);
                }
                break;
            case STATUS_INDEX:
                result = act.getStatus();
                break;
            case AMOUNT_INDEX:
                result = getAmount(act);
                break;
            default:
                result = super.getValue(object, column, row);
                break;
        }
        return result;
    }

    /**
     * Helper to create a table column.
     *
     * @param index the column model index
     * @param name  the column name
     * @return a new column
     */
    protected static TableColumn createTableColumn(int index, String name) {
        TableColumn column = new TableColumn(index);
        column.setHeaderValue(Messages.get(name));
        return column;
    }

    /**
     * Returns an amount from an act.
     *
     * @param act the act
     * @return the stringified amount
     */
    private Label getAmount(Act act) {
        BigDecimal amount = ActHelper.getAmount(act, "amount");
        String result = NumberFormatter.format(amount);
        Label label = LabelFactory.create();
        label.setText(result);
        TableLayoutData layout = new TableLayoutData();
        Alignment right = new Alignment(Alignment.RIGHT, Alignment.DEFAULT);
        layout.setAlignment(right);
        label.setLayoutData(layout);
        return label;
    }

}
