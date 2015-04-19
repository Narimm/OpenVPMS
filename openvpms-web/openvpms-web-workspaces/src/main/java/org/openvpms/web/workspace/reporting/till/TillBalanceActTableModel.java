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

package org.openvpms.web.workspace.reporting.till;

import nextapp.echo2.app.table.TableColumn;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.web.component.im.table.act.ActAmountTableModel;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.util.Date;

/**
 * A table model for <em>act.tillBalance</em> acts.
 * <p/>
 * This displays the date/time of the act.
 *
 * @author Tim Anderson
 */
public class TillBalanceActTableModel extends ActAmountTableModel<FinancialAct> {

    /**
     * Constructs an {@link TillBalanceActTableModel}.
     */
    public TillBalanceActTableModel() {
        super(true, true);
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
    protected Object getValue(FinancialAct act, TableColumn column, int row) {
        Object result = null;
        if (column.getModelIndex() == DATE_INDEX) {
            Date date = act.getActivityStartTime();
            if (date != null) {
                result = DateFormatter.formatDateTime(date, false);
            }
        } else {
            result = super.getValue(act, column, row);
        }
        return result;
    }
}
