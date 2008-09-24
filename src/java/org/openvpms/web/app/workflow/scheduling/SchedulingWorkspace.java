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

package org.openvpms.web.app.workflow.scheduling;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.apache.commons.lang.time.DateUtils;
import org.openvpms.archetype.rules.workflow.Appointment;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.app.patient.CustomerPatientSummary;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.CRUDWindowListener;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.subsystem.AbstractViewWorkspace;
import org.openvpms.web.component.subsystem.Refreshable;
import org.openvpms.web.component.util.GroupBoxFactory;
import org.openvpms.web.component.util.SplitPaneFactory;


/**
 * Scheduling workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SchedulingWorkspace extends AbstractViewWorkspace<Entity>
        implements Refreshable {

    /**
     * The workspace.
     */
    private Component workspace;

    /**
     * The act browser.
     */
    private AppointmentBrowser browser;

    /**
     * The CRUD window.
     */
    private AppointmentCRUDWindow window;

    /**
     * The last query time, used to determine if a refresh is necessary.
     */
    private long lastQueryTime = -1;


    /**
     * Construct a new <tt>SchedulingWorkspace</tt>.
     */
    public SchedulingWorkspace() {
        super("workflow", "scheduling",
              Archetypes.create("entity.organisationScheduleView",
                                Entity.class), false);
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <tt>null</tt>
     */
    @Override
    public void setObject(Entity object) {
        super.setObject(object);
        GlobalContext.getInstance().setScheduleView(object);
        layoutWorkspace(object);
        initQuery(object);
    }

    /**
     * Renders the workspace summary.
     *
     * @return the component representing the workspace summary, or
     *         <code>null</code> if there is no summary
     */
    @Override
    public Component getSummary() {
        if (window != null) {
            Act act = window.getObject();
            return CustomerPatientSummary.getSummary(act);
        }
        return null;
    }

    /**
     * Determines if a refresh is required.
     *
     * @return <tt>true</tt> if at least a minute has elapsed since the last
     *         refresh
     */
    public boolean needsRefresh() {
        if (lastQueryTime == -1) {
            return true;
        }
        long now = System.currentTimeMillis();
        return (now - lastQueryTime) >= DateUtils.MILLIS_PER_MINUTE;
    }

    /**
     * Refreshes the workspace.
     */
    public void refresh() {
        doQuery();
    }

    /**
     * Returns the latest version of the current schedule view context object.
     *
     * @return the latest version of the schedule view context object, or
     *         {@link #getObject()} if they are the same
     */
    protected Entity getLatest() {
        return getLatest(GlobalContext.getInstance().getScheduleView());
    }

    /**
     * Determines if the workspace should be refreshed.
     * This implementation always returns <code>true</code>.
     *
     * @return <code>true</code>
     */
    @Override
    protected boolean refreshWorkspace() {
        return true;
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    protected void onSaved(IMObject object, boolean isNew) {
        doQuery();
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    protected void onDeleted(IMObject object) {
        doQuery();
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when the object needs to be refreshed.
     *
     * @param object the object
     */
    protected void onRefresh(IMObject object) {
        doQuery();
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when an appointment is selected.
     *
     * @param appointment the appointment. May be <tt>null</tt>
     */
    protected void actSelected(ObjectSet appointment) {
        // update the context schedule
        GlobalContext.getInstance().setSchedule(browser.getSelectedSchedule());

        if (appointment != null) {
            IMObjectReference actRef = appointment.getReference(
                    Appointment.ACT_REFERENCE);
            Act act = (Act) IMObjectHelper.getObject(actRef);
            window.setObject(act);
        } else {
            window.setObject(null);
        }
        window.setStartTime(browser.getSelectedTime());
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked to edit an appointment.
     *
     * @param appointment the appointment
     */
    protected void onEdit(ObjectSet appointment) {
        // update the context schedule
        GlobalContext.getInstance().setSchedule(browser.getSelectedSchedule());
        IMObjectReference actRef = appointment.getReference(
                Appointment.ACT_REFERENCE);
        Act act = (Act) IMObjectHelper.getObject(actRef);
        window.setObject(act);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
        window.setStartTime(browser.getSelectedTime());
        if (act != null) {
            window.edit();
        }
    }

    /**
     * Lays out the workspace.
     *
     * @param view the schedule view
     */
    protected void layoutWorkspace(Entity view) {
        setBrowser(new AppointmentBrowser());
        setCRUDWindow(new AppointmentCRUDWindow());
        setWorkspace(createWorkspace());
    }

    /**
     * Creates the workspace split pane.
     *
     * @return a new workspace split pane
     */
    protected Component createWorkspace() {
        Component acts = GroupBoxFactory.create(browser.getComponent());
        Component window = getCRUDWindow().getComponent();
        return SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
                "WorkflowWorkspace.Layout", window, acts);
    }

    /**
     * Registers a new browser.
     *
     * @param browser the new browser
     */
    protected void setBrowser(AppointmentBrowser browser) {
        this.browser = browser;
        browser.addAppointmentListener(new AppointmentListener() {
            public void query() {
                onQuery();
            }

            public void selected(ObjectSet object) {
                actSelected(object);
            }

            public void edit(ObjectSet set) {
                onEdit(set);
            }

            public void create() {
                window.create();
            }
        });
    }

    /**
     * Returns the browser.
     *
     * @return the browser
     */
    protected Browser<ObjectSet> getBrowser() {
        return browser;
    }

    /**
     * Registers a new workspace.
     *
     * @param workspace the workspace
     */
    protected void setWorkspace(Component workspace) {
        SplitPane root = getRootComponent();
        if (this.workspace != null) {
            root.remove(this.workspace);
        }
        this.workspace = workspace;
        root.add(this.workspace);
    }

    /**
     * Registers a new CRUD window.
     *
     * @param window the window
     */
    protected void setCRUDWindow(AppointmentCRUDWindow window) {
        this.window = window;
        this.window.setListener(new CRUDWindowListener<Act>() {
            public void saved(Act object, boolean isNew) {
                onSaved(object, isNew);
            }

            public void deleted(Act object) {
                onDeleted(object);
            }

            public void refresh(Act object) {
                onRefresh(object);
            }
        });
    }

    /**
     * Returns the CRUD window.
     *
     * @return the CRUD window
     */
    protected CRUDWindow<Act> getCRUDWindow() {
        return window;
    }

    /**
     * Perform an initial query, selecting the first available act.
     *
     * @param view the party
     */
    protected void initQuery(Entity view) {
        browser.setScheduleView(view);
        doQuery();
        onQuery();
    }

    /**
     * Returns the workspace.
     *
     * @return the workspace. May be <tt>null</tt>
     */
    protected Component getWorkspace() {
        return workspace;
    }

    /**
     * Invoked when acts are queried.
     */
    protected void onQuery() {
        GlobalContext context = GlobalContext.getInstance();
        context.setScheduleDate(browser.getDate());
        context.setSchedule(browser.getSelectedSchedule());
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        Entity latest = getLatest();
        if (latest != getObject()) {
            setObject(latest);
        } else if (browser == null) {
            layoutWorkspace(null);
            latest = browser.getScheduleView();
            setObject(latest);
        } else {
            doQuery();

            // need to add the existing workspace to the container
            Component workspace = getWorkspace();
            if (workspace != null) {
                container.add(workspace);
            }
        }
    }

    /**
     * Queries the appointments, recording the query time.
     */
    private void doQuery() {
        browser.query();
        lastQueryTime = System.currentTimeMillis();
    }

}
