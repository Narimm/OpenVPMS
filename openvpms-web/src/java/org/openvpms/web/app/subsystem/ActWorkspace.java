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

import java.util.List;

import echopointng.GroupBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.QueryBrowserListener;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.act.ActTableModel;
import org.openvpms.web.component.subsystem.AbstractViewWorkspace;
import org.openvpms.web.component.util.GroupBoxFactory;
import org.openvpms.web.component.util.SplitPaneFactory;


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
    private Browser _acts;

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
        _acts.setSelected(object);
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
        _query = createQuery(party);
        _acts = new Browser(_query, null, createTableModel());
        _acts.addQueryListener(new QueryBrowserListener() {
            public void query() {
                selectFirst();
            }

            public void selected(IMObject object) {
                actSelected((Act) object);
            }
        });
        GroupBox actsBox = GroupBoxFactory.create(_acts.getComponent());

        _window = createCRUDWindow();
        if (_workspace != null) {
            container.remove(_workspace);
        }
        _workspace = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL,
                                             "ActWorkspace.Layout", actsBox,
                                             _window.getComponent());
        container.add(_workspace);

        _window.setListener(new CRUDWindowListener() {
            public void saved(IMObject object, boolean isNew) {
                onSaved(object, isNew);
            }

            public void deleted(IMObject object) {
                onDeleted(object);
            }
        });
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
     * Creates a new table model to display acts.
     *
     * @return a new table model.
     */
    protected IMObjectTableModel createTableModel() {
        return new ActTableModel();
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
        List<IMObject> objects = _acts.getObjects();
        if (!objects.isEmpty()) {
            IMObject current = objects.get(0);
            _acts.setSelected(current);
            _window.setObject(current);
        } else {
            _window.setObject(null);
        }
    }
}
