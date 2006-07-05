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

import org.openvpms.web.component.subsystem.AbstractSubsystem;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;


/**
 * Administration subsystem.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class AdminSubsystem extends AbstractSubsystem {

    /**
     * Construct a new <code>AdminSubsystem</code>.
     */
    public AdminSubsystem() {
        super("admin");

        Authentication auth
                = SecurityContextHolder.getContext().getAuthentication();

        addWorkspace(new OrganisationWorkspace());
        addWorkspace(new DocumentTemplateWorkspace());
        addWorkspace(new ClassificationWorkspace());
        if (auth != null && "admin".equals(auth.getName())) {
            addWorkspace(new UserWorkspace());
            addWorkspace(new RoleWorkspace());
            addWorkspace(new AuthorityWorkspace());
            addWorkspace(new LookupWorkspace());
            addWorkspace(new ArchetypeWorkspace());
        }

    }
}
