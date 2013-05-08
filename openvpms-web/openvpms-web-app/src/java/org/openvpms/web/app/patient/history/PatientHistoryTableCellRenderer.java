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

package org.openvpms.web.app.patient.history;

import nextapp.echo2.app.Table;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.table.IMTable;
import org.openvpms.web.echo.table.AbstractTableCellRenderer;


/**
 * Patient history record summary table cell renderer.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientHistoryTableCellRenderer extends AbstractTableCellRenderer {

    /**
     * The default row style.
     */
    private static final String DEFAULT_STYLE = "MedicalRecordSummary";


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
        String style = DEFAULT_STYLE;
        if (table instanceof IMTable && column == 1) {
            style = getStyle((IMTable) table, row);
        }
        return style;
    }

    /**
     * Returns the style for the specified row.
     *
     * @param table the table
     * @param row   the row
     * @return the style name for the specified row
     */
    private String getStyle(IMTable<IMObject> table, int row) {
        IMObject object = table.getObjects().get(row);
        return DEFAULT_STYLE + "." + object.getArchetypeId().getShortName();
    }
}
