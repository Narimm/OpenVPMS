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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.workflow.worklist;

import echopointng.xhtml.XhtmlFragment;
import nextapp.echo2.app.Table;
import org.openvpms.web.app.workflow.scheduling.ScheduleTableCellRenderer;


/**
 * Cell renderer for tasks.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TaskTableCellRenderer extends ScheduleTableCellRenderer {

    /**
     * Creates a new <tt>TaskTableCellRenderer</tt>.
     */
    public TaskTableCellRenderer() {
        super("entity.taskType");
    }

    /**
     * Returns a <tt>XhtmlFragment</tt> that will be displayed as the
     * content at the specified co'ordinate in the table.
     *
     * @param table  the <tt>Table</tt> for which the rendering is occurring
     * @param value  the value retrieved from the <tt>TableModel</tt> for the
     *               specified coordinate
     * @param column the column index to render
     * @param row    the row index to render
     * @return a <tt>XhtmlFragment</tt> representation of the value
     */
    public XhtmlFragment getTableCellRendererContent(Table table, Object value,
                                                     int column, int row) {
        return null;
    }
}
