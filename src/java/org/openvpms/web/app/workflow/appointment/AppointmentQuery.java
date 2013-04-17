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
 */

package org.openvpms.web.app.workflow.appointment;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.workflow.scheduling.ScheduleQuery;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Appointment query.
 *
 * @author Tim Anderson
 */
class AppointmentQuery extends ScheduleQuery {

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

        private final int startMins;

        private final int endMins;
    }

    /**
     * The practice location.
     */
    private final Party location;

    /**
     * Appointment rules.
     */
    private AppointmentRules rules;

    /**
     * Time range selector.
     */
    private SelectField timeSelector;


    /**
     * Constructs a {@code AppointmentQuery}.
     *
     * @param location the practice location. May be {@code null}
     */
    public AppointmentQuery(Party location) {
        super(ServiceHelper.getAppointmentService(), "entity.organisationScheduleView");
        this.location = location;
        rules = new AppointmentRules();
    }

    /**
     * Returns the selected time range.
     *
     * @return the selected time range
     */
    public TimeRange getTimeRange() {
        int index = timeSelector.getSelectedIndex();
        TimeRange range;
        switch (index) {
            case 0:
                range = TimeRange.ALL;
                break;
            case 1:
                range = TimeRange.MORNING;
                break;
            case 2:
                range = TimeRange.AFTERNOON;
                break;
            case 3:
                range = TimeRange.EVENING;
                break;
            case 4:
                range = TimeRange.AM;
                break;
            case 5:
                range = TimeRange.PM;
                break;
            default:
                range = TimeRange.ALL;
        }
        return range;
    }

    /**
     * Returns the schedule views.
     * <p/>
     * This returns the <em>entity.organisationWorkListView</em> entities for
     * the current location.
     *
     * @return the schedule views
     */
    protected List<Entity> getScheduleViews() {
        List<Entity> views;
        if (location != null) {
            LocationRules locationRules = new LocationRules();
            views = locationRules.getScheduleViews(location);
        } else {
            views = Collections.emptyList();
        }
        return views;
    }

    /**
     * Returns the default schedule view.
     *
     * @return the default schedule view. May be {@code null}
     */
    protected Entity getDefaultScheduleView() {
        if (location != null) {
            LocationRules locationRules = new LocationRules();
            return locationRules.getDefaultScheduleView(location);
        }
        return null;
    }

    /**
     * Returns the schedules for the specified schedule view.
     *
     * @param view the schedule view
     * @return the corresponding schedules
     */
    protected List<Entity> getSchedules(Entity view) {
        List<Party> schedules = rules.getSchedules(view);
        return new ArrayList<Entity>(schedules);
    }

    /**
     * Returns a display name for the schedule selector.
     *
     * @return a display name for the schedule selector
     */
    protected String getScheduleDisplayName() {
        return Messages.get("workflow.scheduling.query.schedule");
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        super.doLayout(container);
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
