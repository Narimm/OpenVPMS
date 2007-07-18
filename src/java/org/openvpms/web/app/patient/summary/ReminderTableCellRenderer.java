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

package org.openvpms.web.app.patient.summary;

import nextapp.echo2.app.Table;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.table.IMTable;
import org.openvpms.web.component.table.AbstractTableCellRenderer;

import java.sql.Timestamp;
import java.util.Date;


/**
 * Reminder table cell renderer.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReminderTableCellRenderer extends AbstractTableCellRenderer {

    /**
     * The default row style.
     */
    private static final String DEFAULT_STYLE
            = "PatientSummary.Reminder.Normal";

    /**
     * The style for overdue reminders.
     */
    private static final String OVERDUE_STYLE
            = "PatientSummary.Reminder.Overdue";

    /**
     * The style for reminders.
     */
    private static final String REMIND_STYLE
            = "PatientSummary.Reminder.Remind";

    /**
     * The logger.
     */
    private static final Log log
            = LogFactory.getLog(ReminderTableCellRenderer.class);


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
        if (table instanceof IMTable) {
            style = getStyle((IMTable<IMObject>) table, row);
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
        String style = DEFAULT_STYLE;
        try {
            IMObject object = table.getObjects().get(row);
            if (object instanceof Act) {
                ActBean act = new ActBean((Act) object);
                Entity reminderType = act.getParticipant(
                        "participation.reminderType");
                if (reminderType != null) {
                    EntityBean bean = new EntityBean(reminderType);
                    String sensitivityUnits
                            = bean.getString("sensitivityUnits");
                    if (sensitivityUnits != null) {
                        int interval = bean.getInt("sensitivityInterval");
                        DateUnits units = DateUnits.valueOf(sensitivityUnits);
                        Date now = new Date();
                        Date from = DateRules.getDate(now, -interval, units);
                        Date to = DateRules.getDate(now, interval, units);
                        Date dueDate = act.getAct().getActivityEndTime();
                        if (dueDate != null) {
                            if (dueDate instanceof Timestamp) {
                                dueDate = new Date(dueDate.getTime());
                            }
                            if (dueDate.compareTo(from) < 0) {
                                style = OVERDUE_STYLE;
                            } else if (dueDate.compareTo(to) < 0) {
                                style = REMIND_STYLE;
                            }
                        }
                    }
                }
            }
        } catch (OpenVPMSException exception) {
            log.error(exception, exception);
        }
        return style;
    }
}
