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

package org.openvpms.web.app.customer;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.TableBrowser;

/**
 * Customer Financial Act workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */

public abstract class CustomerFinancialActWorkspace
        extends CustomerActWorkspace {

    /**
     * @param subsystemId
     * @param workspaceId
     * @param refModelName
     * @param entityName
     * @param conceptName
     */
    public CustomerFinancialActWorkspace(String subsystemId,
                                         String workspaceId,
                                         String refModelName, String entityName,
                                         String conceptName) {
        super(subsystemId, workspaceId, refModelName, entityName, conceptName);
    }

    /**
     * Creates a new browser to query and display acts.
     * Default sort order is by descending starttime.
     *
     * @param query the query
     * @return a new browser
     */
    @Override
    protected Browser<Act> createBrowser(ActQuery<Act> query) {
        SortConstraint[] sort = {new NodeSortConstraint("startTime", false)};
        return new TableBrowser<Act>(query, sort, createTableModel());
    }

}
