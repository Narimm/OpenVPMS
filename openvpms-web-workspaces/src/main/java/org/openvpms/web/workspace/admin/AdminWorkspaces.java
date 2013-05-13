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
 */

package org.openvpms.web.workspace.admin;

import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.subsystem.AbstractWorkspaces;
import org.openvpms.web.workspace.admin.archetype.ArchetypeWorkspace;
import org.openvpms.web.workspace.admin.group.GroupWorkspace;
import org.openvpms.web.workspace.admin.lookup.LookupWorkspace;
import org.openvpms.web.workspace.admin.template.DocumentTemplateWorkspace;
import org.openvpms.web.workspace.admin.user.UserWorkspace;


/**
 * Administration workspaces.
 *
 * @author Tim Anderson
 */
public class AdminWorkspaces extends AbstractWorkspaces {

    /**
     * Constructs an {@code AdminWorkspaces}.
     *
     * @param context the context
     */
    public AdminWorkspaces(Context context) {
        super("admin");

        addWorkspace(new OrganisationWorkspace(context));
        addWorkspace(new TypeWorkspace(context));
        addWorkspace(new DocumentTemplateWorkspace(context));
        addWorkspace(new LookupWorkspace(context));
        addWorkspace(new UserWorkspace(context));
        addWorkspace(new GroupWorkspace(context));
        addWorkspace(new RoleWorkspace(context));
        addWorkspace(new AuthorityWorkspace(context));
        addWorkspace(new ArchetypeWorkspace(context));
        addWorkspace(new StyleSheetWorkspace(context));
    }
}
