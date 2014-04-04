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

import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleBrowser;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleCRUDWindow;
import org.openvpms.web.workspace.workflow.scheduling.SchedulingWorkspace;


/**
 * Appointment workspace.
 *
 * @author Tim Anderson
 */
public class AppointmentWorkspace extends SchedulingWorkspace {

    /**
     * Constructs an {@code AppointmentWorkspace}.
     *
     * @param context the context
     */
    public AppointmentWorkspace(Context context) {
        super("workflow", "scheduling", Archetypes.create("entity.organisationScheduleView", Entity.class),
              context);
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be {@code null}
     */
    @Override
    public void setObject(Entity object) {
        ScheduleBrowser browser = getBrowser();
        PropertySet marked = null;
        boolean isCut = false;
        if (browser != null) {
            marked = browser.getMarked();
            isCut = browser.isCut();
        }
        getContext().setScheduleView(object);
        super.setObject(object);
        browser = getBrowser();
        if (browser != null && marked != null) {
            browser.setMarked(marked, isCut);
        }
    }

    /**
     * Determines if the workspace can be updated with instances of the specified archetype.
     *
     * @param shortName the archetype's short name
     * @return {@code true} if the workspace can be updated by the archetype; otherwise {@code false}
     * @see #update
     */
    @Override
    public boolean canUpdate(String shortName) {
        return super.canUpdate(shortName) || ScheduleArchetypes.APPOINTMENT.equals(shortName);
    }

    /**
     * Updates the workspace with the specified object.
     *
     * @param object the object to update the workspace with
     */
    @Override
    public void update(IMObject object) {
        if (TypeHelper.isA(object, "entity.organisationScheduleView")) {
            setObject((Entity) object);
        } else if (TypeHelper.isA(object, ScheduleArchetypes.APPOINTMENT)) {
            Act act = (Act) object;
            ActBean bean = new ActBean(act);
            Entity schedule = bean.getNodeParticipant("schedule");
            if (schedule != null) {
                EntityBean entity = new EntityBean(schedule);
                Entity view = entity.getNodeSourceEntity("views");
                if (view != null) {
                    setScheduleView(view, act.getActivityStartTime());
                    ScheduleBrowser scheduleBrowser = getBrowser();
                    scheduleBrowser.setSelected(scheduleBrowser.getEvent(act));
                    getCRUDWindow().setObject(act);
                }
            }
        }
    }

    /**
     * Creates a new browser.
     *
     * @return a new browser
     */
    protected ScheduleBrowser createBrowser() {
        Context context = getContext();
        return new AppointmentBrowser(context.getLocation(), context);
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    protected ScheduleCRUDWindow createCRUDWindow() {
        return new AppointmentCRUDWindow((AppointmentBrowser) getBrowser(), getContext(), getHelpContext());
    }

    /**
     * Invoked when events are queried.
     * <p/>
     * This implementation updates the context with the selected schedule date and schedule.
     */
    @Override
    protected void onQuery() {
        Context context = getContext();
        ScheduleBrowser browser = getBrowser();
        context.setScheduleDate(browser.getDate());
        context.setSchedule((Party) browser.getSelectedSchedule());
        Act act = browser.getAct(browser.getSelected());
        getCRUDWindow().setObject(act);
    }

    /**
     * Invoked when an event is selected.
     *
     * @param event the event. May be {@code null}
     */
    @Override
    protected void eventSelected(PropertySet event) {
        // update the context schedule
        updateContext();
        super.eventSelected(event);
    }

    /**
     * Invoked to edit an event.
     *
     * @param event the event
     */
    @Override
    protected void onEdit(PropertySet event) {
        updateContext();
        super.onEdit(event);
    }

    /**
     * Returns the latest version of the current schedule view context object.
     *
     * @return the latest version of the schedule view context object, or {@link #getObject()} if they are the same
     */
    @Override
    protected Entity getLatest() {
        return getLatest(getContext().getScheduleView());
    }

    /**
     * Returns the default schedule view for the specified practice location.
     *
     * @param location the practice location
     * @return the default schedule view, or {@code null} if there is no default
     */
    protected Entity getDefaultView(Party location) {
        LocationRules locationRules = ServiceHelper.getBean(LocationRules.class);
        return locationRules.getDefaultScheduleView(location);
    }

    /**
     * Updates the context with the selected schedule.
     */
    private void updateContext() {
        Party schedule = (Party) getBrowser().getSelectedSchedule();
        getContext().setSchedule(schedule);
    }

}
