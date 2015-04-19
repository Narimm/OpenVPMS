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

import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.scheduling.AbstractSchedules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Appointment schedules.
 *
 * @author Tim Anderson
 */
public class AppointmentSchedules extends AbstractSchedules {

    /**
     * Appointment rules.
     */
    private AppointmentRules rules;

    /**
     * Constructs an {@link AppointmentSchedules}.
     *
     * @param location the location. May be {@code null}
     */
    public AppointmentSchedules(Party location) {
        super(location, ScheduleArchetypes.SCHEDULE_VIEW);
        rules = ServiceHelper.getBean(AppointmentRules.class);
    }

    /**
     * Returns the schedule views.
     * <p/>
     * This returns the <em>entity.organisationScheduleView</em> entities for the current location.
     *
     * @return the schedule views
     */
    @Override
    public List<Entity> getScheduleViews() {
        Party location = getLocation();
        return (location != null) ? getLocationRules().getScheduleViews(location) : Collections.<Entity>emptyList();
    }

    /**
     * Returns the default schedule view for the specified location
     *
     * @return the default schedule view. May be {@code null}
     */
    @Override
    public Entity getDefaultScheduleView() {
        Party location = getLocation();
        return (location != null) ? getLocationRules().getDefaultScheduleView(location) : null;
    }

    /**
     * Returns the schedules for the specified schedule view.
     *
     * @param view the schedule view
     * @return the corresponding schedules
     */
    @Override
    public List<Entity> getSchedules(Entity view) {
        List<Party> schedules = rules.getSchedules(view);
        return new ArrayList<Entity>(schedules);
    }

    /**
     * Returns a display name for the schedule selector.
     *
     * @return a display name for the schedule selector
     */
    @Override
    public String getScheduleDisplayName() {
        return Messages.get("workflow.scheduling.query.schedule");
    }

}
