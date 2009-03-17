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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.workflow.investigation;

import nextapp.echo2.app.Component;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.app.reporting.AbstractReportingWorkspace;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.DefaultIMObjectTableBrowser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.component.util.GroupBoxFactory;

import echopointng.GroupBox;


/**
 * Workspace to display list of investigation results.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class InvestigationsWorkspace
        extends AbstractReportingWorkspace<Act> {

    /**
     * The query.
     */
    private Query<Act> query;


    /**
     * Creates a new <tt>InvestigationsWorkspace</tt>.
     */
    public InvestigationsWorkspace() {
        super("workflow", "investigation", Act.class);
    }

    /**
     * Lays out the components.
     *
     * @param container the container
     * @param group     the focus group
     */
    @Override
    protected void doLayout(Component container, FocusGroup group) {
        query = new InvestigationsQuery();
        Browser<Act> browser = new DefaultIMObjectTableBrowser<Act>(
                query, new InvestigationsTableModel());
        GroupBox box = GroupBoxFactory.create(browser.getComponent());
        container.add(box);
        group.add(browser.getFocusGroup());
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

    class InvestigationsTableModel extends AbstractActTableModel {

        public InvestigationsTableModel() {
            super(InvestigationsQuery.SHORT_NAMES);
        }

        /**
         * Returns a list of descriptor names to include in the table.
         *
         * @return the list of descriptor names to include in the table
         */
        @Override
        protected String[] getNodeNames() {
            return new String[]{"startTime", "investigationType", "patient", "status", "docReference"};
        }
    }
}
