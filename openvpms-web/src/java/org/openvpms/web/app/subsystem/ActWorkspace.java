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
import org.openvpms.web.component.im.query.*;
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
    private SplitPane _workspace;

    /**
     * The query.
     */
    private ActQuery _query;

    /**
     * The act browser.
     */
    private Browser<Act> _acts;

    /**
     * The CRUD window.
     */
    private CRUDWindow _window;


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
        _acts.query();
        _acts.setSelected((Act) object);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    protected void onDeleted(IMObject object) {
        _acts.query();
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when an act is selected.
     *
     * @param act the act
     */
    protected void actSelected(Act act) {
        _window.setObject(act);
    }

    /**
     * Lays out the workspace.
     *
     * @param party     the party
     * @param container the container
     */
    protected void layoutWorkspace(Party party, Component container) {
        setQuery(createQuery(party));
        setBrowser(createBrowser(_query));
        setCRUDWindow(createCRUDWindow());
        if (_workspace != null) {
            container.remove(_workspace);
        }
        _workspace = createWorkspace(_acts, _window);
        container.add(_workspace);
    }

    /**
     * Returns a component representing the acts.
     * This implementation returns the acts displayed in a group box.
     *
     * @param acts the act browser
     * @return a component representing the acts
     */
    protected Component getActs(Browser acts) {
        return GroupBoxFactory.create(_acts.getComponent());
    }

    /**
     * Creates the workspace split pane.
     *
     * @param browser the act browser
     * @param window the CRUD window
     * @return a new workspace split pane
     */
    protected SplitPane createWorkspace(Browser browser, CRUDWindow window) {
        Component acts = getActs(browser);
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
    protected abstract ActQuery createQuery(Party party);

    /**
     * Creates a new browser to query and display acts.
     *
     * @param query the query
     * @return a new browser
     */
    protected Browser<Act> createBrowser(Query<Act> query) {
        return new TableBrowser(query, null, createTableModel());
    }

    /**
     * Creates a new table model to display acts.
     *
     * @return a new table model.
     */
    protected IMObjectTableModel createTableModel() {
        return new ActAmountTableModel();
    }

    /**
     * Registers a new query.
     *
     * @param query the new query
     */
    protected void setQuery(ActQuery query) {
        _query = query;
    }

    /**
     * Returns the query.
     *
     * @return the query
     */
    protected Query<Act> getQuery() {
        return _query;
    }

    /**
     * Registers a new browser.
     *
     * @param browser the new browser
     */
    protected void setBrowser(Browser<Act> browser) {
        _acts = browser;
        _acts.addQueryListener(new QueryBrowserListener() {
            public void query() {
                selectFirst();
            }

            public void selected(IMObject object) {
                actSelected((Act) object);
            }
        });
        if (_workspace != null) {
            _workspace.remove(0);
            _workspace.add(getActs(_acts), 0);
        }
    }

    /**
     * Returns the browser.
     *
     * @return the browser
     */
    protected Browser<Act> getBrowser() {
        return _acts;
    }

    /**
     * Registers a new CRUD window.
     *
     * @param window the window
     */
    protected void setCRUDWindow(CRUDWindow window) {
        _window = window;
        _window.setListener(new CRUDWindowListener() {
            public void saved(IMObject object, boolean isNew) {
                onSaved(object, isNew);
            }

            public void deleted(IMObject object) {
                onDeleted(object);
            }
        });
        if (_workspace != null) {
            _workspace.remove(1);
            _workspace.add(_window.getComponent());
        }
    }

    /**
     * Returns the CRUD window.
     *
     * @return the CRUD window
     */
    protected CRUDWindow getCRUDWindow() {
        return _window;
    }

    /**
     * Perform an initial query, selecting the first available act.
     *
     * @param party the party
     */
    protected void initQuery(Party party) {
        _query.setEntity(party);
        _acts.query();
        selectFirst();
    }

    /**
     * Returns the workspace.
     *
     * @return the workspace. May be <code>null</code>
     */
    protected SplitPane getWorkspace() {
        return _workspace;
    }

    /**
     * Selects the first available act, if any.
     */
    private void selectFirst() {
        List<Act> objects = _acts.getObjects();
        if (!objects.isEmpty()) {
            Act current = objects.get(0);
            _acts.setSelected(current);
            _window.setObject(current);
        } else {
            _window.setObject(null);
        }
    }
}
