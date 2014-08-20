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

package org.openvpms.web.workspace.customer.account;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.account.AccountActTableModel;
import org.openvpms.web.echo.factory.CheckBoxFactory;

/**
 * Customer account table model.
 * <p/>
 * Includes a column for the 'hide' node.
 *
 * @author Tim Anderson
 */
public class CustomerAccountActTableModel extends AccountActTableModel {

    /**
     * The hide column model index.
     */
    private static int HIDE_INDEX = AMOUNT_INDEX + 1;


    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param act    the act
     * @param column the table column
     * @param row    the table row
     * @return the value at the given coordinate
     */
    @Override
    protected Object getValue(FinancialAct act, TableColumn column, int row) {
        Object result = null;
        if (column.getModelIndex() == HIDE_INDEX) {
            IMObjectBean bean = new IMObjectBean(act);
            if (bean.hasNode("hide") && bean.getBoolean("hide")) {
                CheckBox checkBox = CheckBoxFactory.create(true);
                checkBox.setEnabled(false);
                result = checkBox;
            }
        } else {
            result = super.getValue(act, column, row);
        }
        return result;
    }

    /**
     * Helper to create a column model.
     * <p/>
     * Adds a hide column to indicate if an act is being suppressed in statements.
     *
     * @param showArchetype determines if the archetype column should be displayed
     * @param showStatus    determines if the status column should be displayed
     * @param showAmount    determines if the credit/debit amount should be displayed
     * @return a new column model
     */
    @Override
    protected TableColumnModel createColumnModel(boolean showArchetype, boolean showStatus, boolean showAmount) {
        DefaultTableColumnModel model = (DefaultTableColumnModel) super.createColumnModel(showArchetype, showStatus,
                                                                                          showAmount);
        TableColumn column = createTableColumn(HIDE_INDEX, "customer.account.table.hide");
        model.addColumn(column);
        return model;
    }

}
