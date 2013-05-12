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

package org.openvpms.web.app.admin;

import org.openvpms.component.business.domain.im.security.ArchetypeAwareGrantedAuthority;
import org.openvpms.web.app.subsystem.ResultSetCRUDWorkspace;
import org.openvpms.web.component.app.Context;


/**
 * Archetype authority workspace.
 *
 * @author Tim Anderson
 */
public class AuthorityWorkspace extends ResultSetCRUDWorkspace<ArchetypeAwareGrantedAuthority> {

    /**
     * Constructs an {@code AuthorityWorkspace}.
     */
    public AuthorityWorkspace(Context context) {
        super("admin", "authority", context);
        setArchetypes(ArchetypeAwareGrantedAuthority.class, "security.archetypeAuthority");
    }

}
