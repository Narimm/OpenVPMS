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

package org.openvpms.web.app.workflow;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.subsystem.ActWorkspace;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Work list workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class WorkListWorkspace extends ActWorkspace {

    /**
     * Construct a new <code>SchedulingWorkspace</code>.
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
        Context.getInstance().setSchedule((Party) object);
        Party party = (Party) object;
        layoutWorkspace(party, getRootComponent());
        initQuery(party);
        AppointmentQuery query = (AppointmentQuery) getQuery();
        if (query != null) {
            Context.getInstance().setScheduleDate(query.getDate());
        }
    }

    /**
     * Creates the workspace split pane.
     *
     * @param browser the act browser
     * @param window  the CRUD window
     * @return a new workspace split pane
     */
    protected SplitPane createWorkspace(Browser browser, CRUDWindow window) {
        Component acts = getActs(browser);
        return SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
                "WorkflowWorkspace.Layout", window.getComponent(), acts);
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow createCRUDWindow() {
        String type = Messages.get("workflow.scheduling.createtype");
        ShortNameList shortNames = new ShortNameList("act.customerAppointment");
        return new AppointmentCRUDWindow(type, shortNames);
    }

    /**
     * Creates a new query.
     *
     * @param party the party to query acts for
     * @return a new query
     */
    protected ActQuery createQuery(Party party) {
        return new AppointmentQuery(party);
    }

    /**
     * Creates a new table model to display acts.
     *
     * @return a new table model.
     */
    @Override
    protected IMObjectTableModel<Act> createTableModel() {
        // todo - replace this method with call to IMObjectTableModelFactory
        LayoutContext context = new DefaultLayoutContext();
        TableComponentFactory factory = new TableComponentFactory(context);
        context.setComponentFactory(factory);
        return new AppointmentTableModel(context);
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
    }
}
