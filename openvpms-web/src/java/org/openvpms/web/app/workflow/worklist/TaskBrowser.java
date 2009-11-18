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

import echopointng.TableEx;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.app.workflow.scheduling.Schedule;
import org.openvpms.web.app.workflow.scheduling.ScheduleBrowser;
import org.openvpms.web.app.workflow.scheduling.ScheduleEventGrid;
import org.openvpms.web.app.workflow.scheduling.ScheduleTableModel;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Task browser.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TaskBrowser extends ScheduleBrowser {

    /**
     * Creates a new <tt>TaskBrowser</tt>.
     */
    public TaskBrowser() {
        super(new TaskQuery());
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    @Override
    public void query() {
        doQuery(false);
    }

    /**
     * Attempts to select a task.
     *
     * @param task the task
     * @return <tt>true</tt> if the task was selected, <tt>false</tt> if it was
     *         not found
     */
    public boolean setSelected(Act task) {
        ActBean bean = new ActBean(task);
        PropertySet selected = null;
        IMObjectReference worklist = bean.getNodeParticipantRef("worklist");
        if (worklist != null) {
            IMObjectReference taskRef = task.getObjectReference();
            ScheduleTableModel model = getModel();
            int column = model.getColumn(worklist);
            if (column != -1) {
                Schedule schedule = model.getSchedule(column);
                int row = model.getRow(schedule, taskRef);
                if (row != -1) {
                    model.setSelectedCell(column, row);
                    selected = model.getEvent(column, row);
                }
            }
        }
        setSelected(selected);
        return selected != null;
    }


    /**
     * Creates a new grid for a set of events.
     *
     * @param date   the query date
     * @param events the events
     */
    protected ScheduleEventGrid createEventGrid(
            Date date, Map<Entity, List<PropertySet>> events) {
        return new TaskGrid(getScheduleView(), date, events);
    }

    /**
     * Creates a new table model.
     *
     * @param grid the schedule event grid
     * @return the table model
     */
    protected ScheduleTableModel createTableModel(ScheduleEventGrid grid) {
        if (grid.getSchedules().size() == 1) {
            return new SingleScheduleTaskTableModel((TaskGrid) grid);
        }
        return new TaskTableModel((TaskGrid) grid);
    }

    /**
     * Creates a new table.
     *
     * @param model the model
     * @return a new table
     */
    @Override
    protected TableEx createTable(ScheduleTableModel model) {
        TableEx table = super.createTable(model);
        table.setDefaultRenderer(new TaskTableCellRenderer());
        return table;
    }

}
