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
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.app.workflow.scheduling.ScheduleBrowser;
import org.openvpms.web.app.workflow.scheduling.ScheduleCRUDWindow;
import org.openvpms.web.app.workflow.scheduling.SchedulingWorkspace;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.util.Archetypes;


/**
 * Appointment workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentWorkspace extends SchedulingWorkspace {

    /**
     * Creates a new <tt>AppointmentWorkspace</tt>.
     */
    public AppointmentWorkspace() {
        super("workflow", "scheduling",
              Archetypes.create("entity.organisationScheduleView",
                                Entity.class));
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <tt>null</tt>
     */
    @Override
    public void setObject(Entity object) {
        GlobalContext.getInstance().setScheduleView(object);
        super.setObject(object);
    }

    /**
     * Creates a new browser.
     *
     * @return a new browser
     */
    protected ScheduleBrowser createBrowser() {
        return new AppointmentBrowser();
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    protected ScheduleCRUDWindow createCRUDWindow() {
        return new AppointmentCRUDWindow();
    }

    /**
     * Invoked when events are queried.
     * <p/>
     * This implementation updates the global context with the selected work
     * list date and work list
     */
    @Override
    protected void onQuery() {
        GlobalContext context = GlobalContext.getInstance();
        ScheduleBrowser browser = getBrowser();
        context.setScheduleDate(browser.getDate());
        context.setSchedule((Party) browser.getSelectedSchedule());
        super.onQuery();
    }

    /**
     * Invoked when an event is selected.
     *
     * @param event the event. May be <tt>null</tt>
     */
    @Override
    protected void eventSelected(ObjectSet event) {
        // update selected start time
        AppointmentCRUDWindow window = (AppointmentCRUDWindow) getCRUDWindow();
        window.setStartTime(getBrowser().getSelectedTime());

        // update the context work list
        updateContext();
        super.eventSelected(event);
    }

    /**
     * Invoked to edit an event.
     *
     * @param event the event
     */
    @Override
    protected void onEdit(ObjectSet event) {
        updateContext();
        super.onEdit(event);
    }

    /**
     * Returns the latest version of the current schedule view context object.
     *
     * @return the latest version of the schedule view context object, or
     *         {@link #getObject()} if they are the same
     */
    @Override
    protected Entity getLatest() {
        return getLatest(GlobalContext.getInstance().getScheduleView());
    }

    /**
     * Returns the default schedule view for the specified practice location.
     *
     * @param location the practice location
     * @return the default schedule view, or <tt>null</tt> if there is no
     *         default
     */
    protected Entity getDefaultView(Party location) {
        LocationRules locationRules = new LocationRules();
        return locationRules.getDefaultScheduleView(location);
    }

    /**
     * Updates the global context with the selected schedule.
     */
    private void updateContext() {
        Party schedule = (Party) getBrowser().getSelectedSchedule();
        GlobalContext.getInstance().setSchedule(schedule);
    }

}
