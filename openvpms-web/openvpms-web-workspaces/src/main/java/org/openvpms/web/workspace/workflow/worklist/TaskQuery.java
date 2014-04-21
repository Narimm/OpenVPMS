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

package org.openvpms.web.workspace.workflow.worklist;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.archetype.rules.workflow.TaskStatus;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.list.AbstractListCellRenderer;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleServiceQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Queries <em>act.customerTask</em> acts.
 *
 * @author Tim Anderson
 */
public class TaskQuery extends ScheduleServiceQuery {

    /**
     * The status range selector.
     */
    private SelectField statusRange;

    /**
     * Listener for status range listener.
     */
    private final ActionListener statusRangeListener;

    /**
     * Act status range.
     */
    private enum StatusRange {
        ALL,         // All acts
        INCOMPLETE,  // Incomplete acts
        COMPLETE     // Complete acts
    }


    /**
     * Constructs {@code TaskQuery}.
     *
     * @param context the context
     */
    public TaskQuery(Context context) {
        super(ServiceHelper.getTaskService(), new TaskSchedules(context.getLocation()));
        statusRangeListener = new ActionListener() {
            public void onAction(ActionEvent event) {
                onQuery();
            }
        };
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        super.doLayout(container);
        statusRange = SelectFieldFactory.create(StatusRange.values());
        statusRange.setCellRenderer(new StatusRangeListCellRenderer());
        statusRange.setSelectedItem(StatusRange.INCOMPLETE);
        statusRange.addActionListener(statusRangeListener);

        container.add(LabelFactory.create("actquery.status"));
        container.add(statusRange);
        getFocusGroup().add(statusRange);
    }

    /**
     * Invoked when the date is updated.
     * Updates the status range selector to:
     * <ul>
     * <li>select INCOMPLETE appointments for the current date; or</li>
     * <li>ALL appointments for any other date</li>
     * </ul>
     * and then invokes {@link #onQuery()}.
     */
    protected void onDateChanged() {
        Date date = getDate();
        Date today = DateRules.getToday();
        statusRange.removeActionListener(statusRangeListener);
        if (date.equals(today)) {
            statusRange.setSelectedItem(StatusRange.INCOMPLETE);
        } else {
            statusRange.setSelectedItem(StatusRange.ALL);
        }
        statusRange.addActionListener(statusRangeListener);
        onQuery();
    }

    /**
     * Returns the events for a schedule and date.
     *
     * @param schedule the schedule
     * @param date     the date
     * @return the events
     */
    @Override
    protected List<PropertySet> getEvents(Entity schedule, Date date) {
        List<PropertySet> events = super.getEvents(schedule, date);
        List<PropertySet> result;
        StatusRange range = getStatusRange();
        if (!events.isEmpty() && range != StatusRange.ALL) {
            boolean complete = range == StatusRange.COMPLETE;
            result = new ArrayList<PropertySet>();
            for (PropertySet event : events) {
                String status = event.getString(ScheduleEvent.ACT_STATUS);
                if (complete) {
                    if (TaskStatus.isComplete(status)) {
                        result.add(event);
                    }
                } else {
                    if (TaskStatus.isIncomplete(status)) {
                        result.add(event);
                    }
                }
            }
        } else {
            result = events;
        }
        return result;
    }

    /**
     * Returns the selected status range.
     *
     * @return the selected status range
     */
    private StatusRange getStatusRange() {
        return (StatusRange) statusRange.getSelectedItem();
    }

    /**
     * Cell renderer for the status range combo.
     */
    private static class StatusRangeListCellRenderer
            extends AbstractListCellRenderer<StatusRange> {


        /**
         * Constructs a new {@code StatusRangeListCellRenderer}.
         */
        public StatusRangeListCellRenderer() {
            super(StatusRange.class);
        }

        /**
         * Renders an object.
         *
         * @param list   the list component
         * @param object the object to render. May be {@code null}
         * @param index  the object index
         * @return the rendered object
         */
        protected Object getComponent(Component list, StatusRange object,
                                      int index) {
            return Messages.get("workflow.scheduling.statusrange."
                                + object.name());
        }

        /**
         * Determines if an object represents 'All'.
         *
         * @param list   the list component
         * @param object the object. May be {@code null}
         * @param index  the object index
         * @return <code>true</code> if the object represents 'All'.
         */
        protected boolean isAll(Component list, StatusRange object, int index) {
            return StatusRange.ALL == object;
        }

        /**
         * Determines if an object represents 'None'.
         *
         * @param list   the list component
         * @param object the object. May be {@code null}
         * @param index  the object index
         * @return <code>true</code> if the object represents 'None'.
         */
        protected boolean isNone(Component list, StatusRange object,
                                 int index) {
            return false;
        }
    }
}
