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
import org.openvpms.web.app.workflow.WorkflowSummary;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.util.SplitPaneFactory;


/**
 * Work list workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class WorkListWorkspace extends ActWorkspace<Party, Act> {

    /**
     * Short name of acts that this may create.
     */
    private static final String ACT_SHORTNAME = "act.customerTask";


    /**
     * Construct a new <tt>WorkListWorkspace</tt>.
     */
    public WorkListWorkspace() {
        super("workflow", "worklist",
              new ShortNameList("party.organisationWorkList"));
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <tt>null</tt>
     */
    @Override
    public void setObject(Party object) {
        super.setObject(object);
        GlobalContext.getInstance().setWorkList(object);
        layoutWorkspace(object);
        initQuery(object);
        TaskQuery query = (TaskQuery) getQuery();
        if (query != null) {
            GlobalContext.getInstance().setWorkListDate(query.getDate());
        }
    }

    /**
     * Sets the current object.
     * This is analagous to  {@link #setObject} but performs a safe cast
     * to the required type.
     *
     * @param object the current object. May be <tt>null</tt>
     */
    public void setIMObject(IMObject object) {
        if (object == null || object instanceof Party) {
            setObject((Party) object);
        } else {
            throw new IllegalArgumentException(
                    "Argument 'object' must be an instance of "
                            + Party.class.getName());
        }
    }

    /**
     * Renders the workspace summary.
     *
     * @return the component representing the workspace summary, or
     *         <code>null</code> if there is no summary
     */
    @Override
    public Component getSummary() {
        CRUDWindow<Act> window = getCRUDWindow();
        if (window != null) {
            Act act = window.getObject();
            return WorkflowSummary.getSummary(act);
        }
        return null;
    }

    /**
     * Determines if the workspace should be refreshed.
     * This implementation always returns <tt>true</tt>.
     *
     * @return <code>true</code>
     */
    @Override
    protected boolean refreshWorkspace() {
        return true;
    }

    /**
     * Returns the latest version of the current work list context object.
     *
     * @return the latest version of the work list context object, or
     *         {@link #getObject()} if they are the same
     */
    @Override
    protected Party getLatest() {
        return getLatest(GlobalContext.getInstance().getWorkList());
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
    protected CRUDWindow<Act> createCRUDWindow() {
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
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when an act is selected.
     *
     * @param act the act
     */
    @Override
    protected void actSelected(Act act) {
        super.actSelected(act);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
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
        Party latest = getLatest();
        if (latest != getObject()) {
            setObject(latest);
        } else {
            Browser<Act> browser = getBrowser();
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
}
