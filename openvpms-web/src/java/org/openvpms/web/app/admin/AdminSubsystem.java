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

package org.openvpms.web.app.admin;

import org.openvpms.web.app.admin.archetype.ArchetypeWorkspace;
import org.openvpms.web.app.admin.group.GroupWorkspace;
import org.openvpms.web.app.admin.lookup.LookupWorkspace;
import org.openvpms.web.app.admin.template.DocumentTemplateWorkspace;
import org.openvpms.web.app.admin.user.UserWorkspace;
import org.openvpms.web.component.subsystem.AbstractSubsystem;


/**
 * Administration subsystem.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class AdminSubsystem extends AbstractSubsystem {

    /**
     * Creates a new <tt>AdminSubsystem</tt>.
     */
    public AdminSubsystem() {
        super("admin");

        addWorkspace(new OrganisationWorkspace());
        addWorkspace(new TypeWorkspace());
        addWorkspace(new DocumentTemplateWorkspace());
        addWorkspace(new LookupWorkspace());
        addWorkspace(new UserWorkspace());
        addWorkspace(new GroupWorkspace());
        addWorkspace(new RoleWorkspace());
        addWorkspace(new AuthorityWorkspace());
        addWorkspace(new ArchetypeWorkspace());
        addWorkspace(new StyleSheetWorkspace());
    }
}
