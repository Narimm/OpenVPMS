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

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.web.app.subsystem.ShortNames;


/**
 * Customer Financial Act workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class CustomerFinancialActWorkspace
        extends CustomerActWorkspace<FinancialAct> {

    /**
     * Constructs a new <tt>CustomerFinancialActWorkspace</tt>.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identfifier
     */
    public CustomerFinancialActWorkspace(String subsystemId,
                                         String workspaceId) {
        super(subsystemId, workspaceId);
    }

    /**
     * Constructs a new <tt>CustomerFinancialActWorkspace</tt>.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identfifier
     * @param shortNames  the archetype short names that this operates on
     */
    public CustomerFinancialActWorkspace(String subsystemId,
                                         String workspaceId,
                                         ShortNames shortNames) {
        super(subsystemId, workspaceId, shortNames);
    }

}
