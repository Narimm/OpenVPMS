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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.app.subsystem.ActWorkspace;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.util.SplitPaneFactory;


/**
 * Work list workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class WorkListWorkspace extends ActWorkspace {

    /**
     * Short name of acts that this may create.
     */
    private static final String ACT_SHORTNAME = "act.customerTask";


    /**
     * Construct a new <code>WorkListWorkspace</code>.
     */
    public WorkListWorkspace() {
        super("workflow", "worklist", "party", "party",
              "organisationWorkList");
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <code>null</code>
     */
    @Override
    public void setObject(IMObject object) {
        super.setObject(object);
        GlobalContext.getInstance().setWorkList((Party) object);
        Party party = (Party) object;
        layoutWorkspace(party);
        initQuery(party);
        TaskQuery query = (TaskQuery) getQuery();
        if (query != null) {
            GlobalContext.getInstance().setWorkListDate(query.getDate());
        }
    }

    /**
     * Creates the workspace split pane.
     *
     * @return a new workspace split pane
     */
    @Override
    protected Component createWorkspace() {
        Component acts = getActs(getBrowser());
        Component window = getCRUDWindow().getComponent();
        return SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
                "WorkflowWorkspace.Layout", window, acts);
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow createCRUDWindow() {
        String type = DescriptorHelper.getDisplayName(ACT_SHORTNAME);
        ShortNameList shortNames = new ShortNameList(ACT_SHORTNAME);
        return new TaskCRUDWindow(type, shortNames);
    }

    /**
     * Creates a new query.
     *
     * @param party the party to query acts for
     * @return a new query
     */
    protected ActQuery<Act> createQuery(Party party) {
        return new TaskQuery(party);
    }

    /**
     * Invoked when acts are queried. Selects the first available act, if any.
     */
    @Override
    protected void onQuery() {
        super.onQuery();
        TaskQuery query = (TaskQuery) getQuery();
        if (query != null) {
            GlobalContext.getInstance().setWorkListDate(query.getDate());
        }
    }

    /**
     * Creates a new table model to display acts.
     *
     * @return a new table model.
     */
    @Override
    protected IMObjectTableModel<Act> createTableModel() {
        // todo - replace this method with call to IMObjectTableModelFactory
        return new TaskTableModel();
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
    }
}
