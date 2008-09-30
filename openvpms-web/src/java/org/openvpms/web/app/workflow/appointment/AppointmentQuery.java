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

package org.openvpms.web.app.workflow.appointment;

import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.workflow.scheduling.ScheduleQuery;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Appointment query.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class AppointmentQuery extends ScheduleQuery {

    /**
     * Appointment rules.
     */
    private AppointmentRules rules;


    /**
     * Creates a new <tt>AppointmentQuery</tt>.
     */
    public AppointmentQuery() {
        super(ServiceHelper.getAppointmentService());
        rules = new AppointmentRules();
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
        Party location = GlobalContext.getInstance().getLocation();
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
     * @return the default schedule view. May be <tt>null</tt>
     */
    protected Entity getDefaultScheduleView() {
        Party location = GlobalContext.getInstance().getLocation();
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
}
