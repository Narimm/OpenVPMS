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

package org.openvpms.web.app.workflow.worklist;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.app.workflow.scheduling.ScheduleBrowser;
import org.openvpms.web.app.workflow.scheduling.ScheduleCRUDWindow;
import org.openvpms.web.app.workflow.scheduling.SchedulingWorkspace;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.util.Archetypes;


/**
 * Task workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TaskWorkspace extends SchedulingWorkspace {

    /**
     * Creates a new <tt>TaskWorkspace</tt>.
     */
    public TaskWorkspace() {
        super("workflow", "worklist",
              Archetypes.create("entity.organisationWorkListView",
                                Entity.class));
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <tt>null</tt>
     */
    @Override
    public void setObject(Entity object) {
        GlobalContext.getInstance().setWorkListView(object);
        super.setObject(object);
    }

    /**
     * Creates a new browser.
     *
     * @return a new browser
     */
    protected ScheduleBrowser createBrowser() {
        return new TaskBrowser();
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    protected ScheduleCRUDWindow createCRUDWindow() {
        return new TaskCRUDWindow();
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
        context.setWorkListDate(browser.getDate());
        context.setWorkList((Party) browser.getSelectedSchedule());
        super.onQuery();
    }

    /**
     * Invoked when an event is selected.
     *
     * @param event the event. May be <tt>null</tt>
     */
    @Override
    protected void eventSelected(ObjectSet event) {
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
        return getLatest(GlobalContext.getInstance().getWorkListView());
    }

    /**
     * Updates the global context with the selected work list.
     */
    private void updateContext() {
        Party workList = (Party) getBrowser().getSelectedSchedule();
        GlobalContext.getInstance().setWorkList(workList);
    }

}
