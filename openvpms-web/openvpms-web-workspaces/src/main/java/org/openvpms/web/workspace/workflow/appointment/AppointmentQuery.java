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

package org.openvpms.web.workspace.workflow.appointment;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import org.joda.time.DateTime;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleServiceQuery;

import java.util.Date;


/**
 * Appointment query.
 *
 * @author Tim Anderson
 */
class AppointmentQuery extends ScheduleServiceQuery {

    public enum TimeRange {

        ALL(0, 24), MORNING(8, 12), AFTERNOON(12, 17), EVENING(17, 24),
        AM(0, 12), PM(12, 24);

        TimeRange(int startHour, int endHour) {
            this.startMins = startHour * 60;
            this.endMins = endHour * 60;
        }

        public int getStartMins() {
            return startMins;
        }

        public int getEndMins() {
            return endMins;
        }

        /**
         * Returns the time range that the specified time falls into.
         *
         * @param time the time
         * @return the corresponding time range
         */
        public static TimeRange getRange(Date time) {
            DateTime dateTime = new DateTime(time);
            int hour = dateTime.getHourOfDay();
            if (hour < 8) {
                return AM;
            } else if (hour >= 8 && hour < 12) {
                return MORNING;
            } else if (hour >= 12 && hour < 17) {
                return AFTERNOON;
            } else if (hour >= 17) {
                return EVENING;
            }
            return ALL;
        }

        private final int startMins;

        private final int endMins;
    }

    /**
     * Time range selector.
     */
    private SelectField timeSelector;


    /**
     * Constructs an {@link AppointmentQuery}.
     *
     * @param location the practice location. May be {@code null}
     */
    public AppointmentQuery(Party location) {
        super(ServiceHelper.getAppointmentService(), new AppointmentSchedules(location));
    }

    /**
     * Returns the selected time range.
     *
     * @return the selected time range
     */
    public TimeRange getTimeRange() {
        int index = timeSelector.getSelectedIndex();
        return index >= 0 && index < TimeRange.values().length ? TimeRange.values()[index] : TimeRange.ALL;
    }

    /**
     * Sets the selected time range.
     *
     * @param range the time range
     */
    public void setTimeRange(TimeRange range) {
        timeSelector.setSelectedIndex(range.ordinal());
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        super.doLayout(container);
        // the order of the items must correspond to the order of TimeRange values.
        String[] timeSelectorItems = {
                Messages.get("workflow.scheduling.time.all"),
                Messages.get("workflow.scheduling.time.morning"),
                Messages.get("workflow.scheduling.time.afternoon"),
                Messages.get("workflow.scheduling.time.evening"),
                Messages.get("workflow.scheduling.time.AM"),
                Messages.get("workflow.scheduling.time.PM")};

        timeSelector = SelectFieldFactory.create(timeSelectorItems);
        timeSelector.setSelectedItem(timeSelectorItems[0]);
        timeSelector.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onQuery();
            }
        });
        container.add(LabelFactory.create("workflow.scheduling.time"));
        container.add(timeSelector);
        getFocusGroup().add(timeSelector);
    }
}
