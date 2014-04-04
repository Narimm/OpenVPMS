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

import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleBrowser;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleCRUDWindow;
import org.openvpms.web.workspace.workflow.scheduling.SchedulingWorkspace;


/**
 * Task workspace.
 *
 * @author Tim Anderson
 */
public class TaskWorkspace extends SchedulingWorkspace {

    /**
     * Constructs a {@code TaskWorkspace}.
     *
     * @param context the context
     */
    public TaskWorkspace(Context context) {
        super("workflow", "worklist", Archetypes.create("entity.organisationWorkListView", Entity.class), context);
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be {@code null}
     */
    @Override
    public void setObject(Entity object) {
        getContext().setWorkListView(object);
        super.setObject(object);
    }

    /**
     * Creates a new browser.
     *
     * @return a new browser
     */
    protected ScheduleBrowser createBrowser() {
        return new TaskBrowser(getContext());
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    protected ScheduleCRUDWindow createCRUDWindow() {
        return new TaskCRUDWindow(getContext(), getHelpContext());
    }

    /**
     * Invoked when events are queried.
     * <p/>
     * This implementation updates the context with the selected work list date and work list
     */
    @Override
    protected void onQuery() {
        Context context = getContext();
        ScheduleBrowser browser = getBrowser();
        context.setWorkListDate(browser.getDate());
        context.setWorkList((Party) browser.getSelectedSchedule());
        super.onQuery();
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    protected void onSaved(IMObject object, boolean isNew) {
        TaskBrowser browser = (TaskBrowser) getBrowser();
        browser.query();
        browser.setSelected((Act) object);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    protected void onDeleted(IMObject object) {
        getBrowser().query();
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when the object needs to be refreshed.
     *
     * @param object the object
     */
    protected void onRefresh(IMObject object) {
        TaskBrowser browser = (TaskBrowser) getBrowser();
        browser.query();
        if (!browser.setSelected((Act) object)) {
            // task no longer visible in the work list view, so remove it
            // from the CRUD window
            getCRUDWindow().setObject(null);
        }
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when an event is selected.
     *
     * @param event the event. May be {@code null}
     */
    @Override
    protected void eventSelected(PropertySet event) {
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
        return getLatest(getContext().getWorkListView());
    }

    /**
     * Returns the default schedule view for the specified practice location.
     *
     * @param location the practice location
     * @return the default schedule view, or {@code null} if there is no default
     */
    protected Entity getDefaultView(Party location) {
        LocationRules locationRules = ServiceHelper.getBean(LocationRules.class);
        return locationRules.getDefaultWorkListView(location);
    }

    /**
     * Updates the global context with the selected work list.
     */
    private void updateContext() {
        Party workList = (Party) getBrowser().getSelectedSchedule();
        getContext().setWorkList(workList);
    }

}
