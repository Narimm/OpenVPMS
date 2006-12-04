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

package org.openvpms.web.app.subsystem;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryBrowserListener;
import org.openvpms.web.component.im.query.TableBrowser;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.act.ActAmountTableModel;
import org.openvpms.web.component.subsystem.AbstractViewWorkspace;
import org.openvpms.web.component.util.GroupBoxFactory;
import org.openvpms.web.component.util.SplitPaneFactory;

import java.util.List;


/**
 * Act workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class ActWorkspace extends AbstractViewWorkspace {

    /**
     * The workspace.
     */
    private Component workspace;

    /**
     * The query.
     */
    private ActQuery<Act> query;

    /**
     * The act browser.
     */
    private Browser<Act> acts;

    /**
     * The CRUD window.
     */
    private CRUDWindow window;


    /**
     * Construct a new <code>ActWorkspace</code>.
     *
     * @param subsystemId  the subsystem localisation identifier
     * @param workspaceId  the workspace localisation identfifier
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public ActWorkspace(String subsystemId, String workspaceId,
                        String refModelName, String entityName,
                        String conceptName) {
        super(subsystemId, workspaceId, refModelName, entityName, conceptName);
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    protected void onSaved(IMObject object, boolean isNew) {
        acts.query();
        acts.setSelected((Act) object);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    protected void onDeleted(IMObject object) {
        acts.query();
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when the object needs to be refreshed.
     *
     * @param object the object
     */
    protected void onRefresh(IMObject object) {
        acts.query();
        acts.setSelected((Act) object);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when an act is selected.
     *
     * @param act the act
     */
    protected void actSelected(Act act) {
        window.setObject(act);
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
        return GroupBoxFactory.create(this.acts.getComponent());
    }

    /**
     * Creates the workspace component.
     *
     * @return a new workspace
     */
    protected Component createWorkspace() {
        Component acts = getActs(getBrowser());
        return SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL,
                                       "ActWorkspace.Layout", acts,
                                       window.getComponent());
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected abstract CRUDWindow createCRUDWindow();

    /**
     * Creates a new query.
     *
     * @param party the party to query acts for
     * @return a new query
     */
    protected abstract ActQuery<Act> createQuery(Party party);

    /**
     * Creates a new browser to query and display acts.
     *
     * @param query the query
     * @return a new browser
     */
    protected Browser<Act> createBrowser(ActQuery<Act> query) {
        return new TableBrowser<Act>(query, null, createTableModel());
    }

    /**
     * Creates a new table model to display acts.
     *
     * @return a new table model.
     */
    protected IMObjectTableModel<Act> createTableModel() {
        return new ActAmountTableModel();
    }

    /**
     * Registers a new query.
     *
     * @param query the new query
     */
    protected void setQuery(ActQuery<Act> query) {
        this.query = query;
    }

    /**
     * Returns the query.
     *
     * @return the query
     */
    protected Query<Act> getQuery() {
        return query;
    }

    /**
     * Registers a new browser.
     *
     * @param browser the new browser
     */
    protected void setBrowser(Browser<Act> browser) {
        acts = browser;
        acts.addQueryListener(new QueryBrowserListener<Act>() {
            public void query() {
                onQuery();
            }

            public void selected(Act object) {
                actSelected(object);
            }
        });
    }

    /**
     * Returns the browser.
     *
     * @return the browser
     */
    protected Browser<Act> getBrowser() {
        return acts;
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
    protected void setCRUDWindow(CRUDWindow window) {
        this.window = window;
        this.window.setListener(new CRUDWindowListener() {
            public void saved(IMObject object, boolean isNew) {
                onSaved(object, isNew);
            }

            public void deleted(IMObject object) {
                onDeleted(object);
            }

            public void refresh(IMObject object) {
                onRefresh(object);
            }
        });
    }

    /**
     * Returns the CRUD window.
     *
     * @return the CRUD window
     */
    protected CRUDWindow getCRUDWindow() {
        return window;
    }

    /**
     * Perform an initial query, selecting the first available act.
     *
     * @param party the party
     */
    protected void initQuery(Party party) {
        query.setEntity(party);
        acts.query();
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
    }

    /**
     * Selects the first available act, if any.
     */
    private void selectFirst() {
        List<Act> objects = acts.getObjects();
        if (!objects.isEmpty()) {
            Act current = objects.get(0);
            acts.setSelected(current);
            window.setObject(current);
        } else {
            window.setObject(null);
        }
    }
}
