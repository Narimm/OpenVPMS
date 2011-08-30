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

import nextapp.echo2.app.Table;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.table.IMTable;
import org.openvpms.web.component.table.AbstractTableCellRenderer;
import org.openvpms.web.component.table.EvenOddTableCellRenderer;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OrderSelectionTableCellRenderer extends AbstractTableCellRenderer {


    private final OrderSelectionTableModel model;


    public OrderSelectionTableCellRenderer(OrderSelectionTableModel model) {
        this.model = model;
    }

    /**
     * Returns the style name for a column and row.
     *
     * @param table  the <code>Table</code> for which the rendering is
     *               occurring
     * @param value  the value retrieved from the <code>TableModel</code> for
     *               the specified coordinate
     * @param column the column
     * @param row    the row
     * @return a style name for the given column and row.
     */
    @SuppressWarnings("unchecked")
    protected String getStyle(Table table, Object value, int column, int row) {
        String result;
        IMTable<Act> actTable = (IMTable<Act>) table;
        Act act = actTable.getObjects().get(row);
        boolean isOrder = TypeHelper.isA(act, "act.supplierOrder");
        if (isOrder) {
            result = EvenOddTableCellRenderer.EVEN_STYLE;
        } else {
            if (model.getSelectionColumnIndex() == column) {
                result = EvenOddTableCellRenderer.ODD_STYLE_INSET;
            } else {
                result = EvenOddTableCellRenderer.ODD_STYLE;
            }
        }
        return result;
    }

}
