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
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.patient.CustomerPatientSummary;
import org.openvpms.web.app.subsystem.BrowserCRUDWorkspace;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.util.GroupBoxFactory;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Work list workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class WorkListWorkspace extends BrowserCRUDWorkspace<Party, Act> {

    /**
     * Short name of acts that this may create.
     */
    private static final String ACT_SHORTNAME = "act.customerTask";


    /**
     * Construct a new <tt>WorkListWorkspace</tt>.
     */
    public WorkListWorkspace() {
        super("workflow", "worklist",
              Archetypes.create("party.organisationWorkList", Party.class,
                                Messages.get("workflow.worklist.type")),
              Archetypes.create(ACT_SHORTNAME, Act.class,
                                Messages.get("workflow.worklist.createtype")));
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
        TaskQuery query = (TaskQuery) getQuery();
        if (query != null) {
            GlobalContext.getInstance().setWorkListDate(query.getDate());
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
            return CustomerPatientSummary.getSummary(act);
        }
        return null;
    }

    /**
     * Determines if the workspace should be refreshed.
     *
     * @return <tt>true</tt>
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
        Component acts = GroupBoxFactory.create(getBrowser().getComponent());
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
        return new TaskCRUDWindow(getChildArchetypes());
    }

    /**
     * Creates a new query.
     *
     * @return a new query
     */
    protected ActQuery<Act> createQuery() {
        return new TaskQuery(getObject());
    }

    /**
     * Invoked when acts are queried. Selects the first available act, if any.
     */
    @Override
    protected void onBrowserQuery() {
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
    protected void onBrowserSelected(Act act) {
        super.onBrowserSelected(act);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

}
