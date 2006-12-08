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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.ArchetypeAwareGrantedAuthority;
import org.openvpms.web.app.subsystem.CRUDWorkspace;


/**
 * Archetype authority workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class AuthorityWorkspace
        extends CRUDWorkspace<ArchetypeAwareGrantedAuthority> {

    /**
     * Construct a new <code>ClassificationWorkspace</code>.
     */
    public AuthorityWorkspace() {
        super("admin", "authority", "system", "security", "archetypeAuthority");
    }

    /**
     * Sets the current object.
     * This is analagous to  {@link #setObject} but performs a safe cast
     * to the required type.
     *
     * @param object the current object. May be <code>null</code>
     */
    public void setIMObject(IMObject object) {
        if (object == null
                || object instanceof ArchetypeAwareGrantedAuthority) {
            setObject((ArchetypeAwareGrantedAuthority) object);
        } else {
            throw new IllegalArgumentException(
                    "Argument 'object' must be an instance of "
                            + ArchetypeAwareGrantedAuthority.class.getName());
        }
    }

}
