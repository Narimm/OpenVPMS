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
import org.openvpms.web.component.im.query.QueryBrowserListener;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.subsystem.AbstractViewWorkspace;
import org.openvpms.web.component.util.GroupBoxFactory;
import org.openvpms.web.component.util.SplitPaneFactory;

import java.util.List;


/**
 * Scheduling workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SchedulingWorkspace extends AbstractViewWorkspace<Entity> {

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
     * Construct a new <tt>SchedulingWorkspace</tt>.
     */
    public SchedulingWorkspace() {
        super("workflow", "scheduling",
              Archetypes.create("entity.scheduleViewType", Entity.class),
              false);
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <tt>null</tt>
     */
    @Override
    public void setObject(Entity object) {
        super.setObject(object);
        // GlobalContext.getInstance().setSchedule(object);
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
     * Returns the latest version of the current schedule context object.
     *
     * @return the latest version of the schedule context object, or
     *         {@link #getObject()} if they are the same
     */
    /*   protected Party getLatest() {
        return getLatest(GlobalContext.getInstance().getSchedule());
    }*/

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
        browser.query();
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    protected void onDeleted(IMObject object) {
        browser.query();
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when the object needs to be refreshed.
     *
     * @param object the object
     */
    protected void onRefresh(IMObject object) {
        browser.query();
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when an appointment is selected.
     *
     * @param appointment the appointment. May be <tt>null</tt>
     */
    protected void actSelected(ObjectSet appointment) {
        if (appointment != null) {
            IMObjectReference actRef = appointment.getReference(
                    Appointment.ACT_REFERENCE);
            Act object = (Act) IMObjectHelper.getObject(actRef);
            window.setObject(object);
            firePropertyChange(SUMMARY_PROPERTY, null, null);
        } else {
            window.setObject(null);
        }
        window.setStartTime(browser.getSelectedTime());

        // update the context schedule
        GlobalContext.getInstance().setSchedule(browser.getSelectedSchedule());
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
     * Returns a component representing the acts.
     * This implementation returns the acts displayed in a group box.
     *
     * @param acts the act browser
     * @return a component representing the acts
     */
    protected Component getActs(Browser acts) {
        return GroupBoxFactory.create(this.browser.getComponent());
    }

    /**
     * Creates the workspace split pane.
     *
     * @return a new workspace split pane
     */
    protected Component createWorkspace() {
        Component acts = getActs(getBrowser());
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
        this.browser.addQueryListener(new QueryBrowserListener<ObjectSet>() {
            public void query() {
                onQuery();
            }

            public void selected(ObjectSet object) {
                actSelected(object);
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
        browser.query();
        onQuery();
    }

    /**
     * Returns the workspace.
     *
     * @return the workspace. May be <code>null</code>
     */
    protected Component getWorkspace() {
        return workspace;
    }

    /**
     * Invoked when acts are queried. Selects the first available act, if any.
     */
    protected void onQuery() {
        selectFirst();
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
            browser.query();

            // need to add the existing workspace to the container
            Component workspace = getWorkspace();
            if (workspace != null) {
                container.add(workspace);
            }
        }
    }

    /**
     * Selects the first available act, if any.
     */
    private void selectFirst() {
        List<ObjectSet> objects = browser.getObjects();
        if (!objects.isEmpty()) {
            ObjectSet current = objects.get(0);
            browser.setSelected(current);
            actSelected(current);
        } else {
            window.setObject(null);
            firePropertyChange(SUMMARY_PROPERTY, null, null);
        }
    }

}
