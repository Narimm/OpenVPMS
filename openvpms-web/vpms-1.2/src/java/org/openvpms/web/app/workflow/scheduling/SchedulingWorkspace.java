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
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.app.patient.CustomerPatientSummary;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.CRUDWindowListener;
import org.openvpms.web.app.workflow.WorkflowQuery;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryBrowserListener;
import org.openvpms.web.component.im.table.IMTableModel;
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
public class SchedulingWorkspace extends AbstractViewWorkspace<Party> {

    /**
     * The workspace.
     */
    private Component workspace;

    /**
     * The query.
     */
    private ActQuery<ObjectSet> query;

    /**
     * The act browser.
     */
    private Browser<ObjectSet> browser;

    /**
     * The CRUD window.
     */
    private CRUDWindow<Act> window;


    /**
     * Construct a new <tt>SchedulingWorkspace</tt>.
     */
    public SchedulingWorkspace() {
        super("workflow", "scheduling",
              Archetypes.create("party.organisationSchedule", Party.class));
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <tt>null</tt>
     */
    @Override
    public void setObject(Party object) {
        super.setObject(object);
        GlobalContext.getInstance().setSchedule(object);
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
    @Override
    protected Party getLatest() {
        return getLatest(GlobalContext.getInstance().getSchedule());
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
     * Invoked when an act is selected.
     *
     * @param act the act
     */
    protected void actSelected(ObjectSet act) {
        IMObjectReference actRef
                = (IMObjectReference) act.get("act.objectReference");
        Act object = (Act) IMObjectHelper.getObject(actRef);
        window.setObject(object);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Lays out the workspace.
     *
     * @param party the party
     */
    protected void layoutWorkspace(Party party) {
        setQuery(createQuery(party));
        setBrowser(createBrowser(query));
        setCRUDWindow(createCRUDWindow());
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
     * Creates a new browser to query and display acts.
     *
     * @param query the query
     * @return a new browser
     */
    protected Browser<ObjectSet> createBrowser(Query<ObjectSet> query) {
        return new AppointmentBrowser((WorkflowQuery<ObjectSet>) query,
                                      createTableModel());
    }

    /**
     * Registers a new query.
     *
     * @param query the new query
     */
    protected void setQuery(ActQuery<ObjectSet> query) {
        this.query = query;
    }

    /**
     * Returns the query.
     *
     * @return the query
     */
    protected Query<ObjectSet> getQuery() {
        return query;
    }

    /**
     * Registers a new browser.
     *
     * @param browser the new browser
     */
    protected void setBrowser(Browser<ObjectSet> browser) {
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
    protected void setCRUDWindow(CRUDWindow<Act> window) {
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
     * @param party the party
     */
    protected void initQuery(Party party) {
        query.setEntity(party);
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
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<Act> createCRUDWindow() {
        return new AppointmentCRUDWindow();
    }

    /**
     * Creates a new query.
     *
     * @param party the party to query acts for
     * @return a new query
     */
    protected ActQuery<ObjectSet> createQuery(Party party) {
        return new CustomerAppointmentQuery(party);
    }

    /**
     * Invoked when acts are queried. Selects the first available act, if any.
     */
    protected void onQuery() {
        selectFirst();
        CustomerAppointmentQuery query = (CustomerAppointmentQuery) getQuery();
        if (query != null) {
            GlobalContext.getInstance().setScheduleDate(query.getDate());
        }
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Creates a new table model to display acts.
     *
     * @return a new table model.
     */
    protected IMTableModel<ObjectSet> createTableModel() {
        return new AppointmentTableModel();
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        Party latest = getLatest();
        if (latest != getObject()) {
            setObject(latest);
        } else {
            if (browser != null) {
                browser.query();
            }

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
