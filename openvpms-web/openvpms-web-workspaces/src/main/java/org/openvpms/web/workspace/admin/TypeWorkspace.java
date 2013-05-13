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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.workspace.subsystem.ResultSetCRUDWorkspace;


/**
 * Type workspace.
 *
 * @author Tim Anderson
 */
public class TypeWorkspace extends ResultSetCRUDWorkspace<Entity> {

    /**
     * Constructs a {@code TypeWorkspace}.
     *
     * @param context the context
     */
    public TypeWorkspace(Context context) {
        super("admin", "type", context);
        setArchetypes(Entity.class, "entity.*Type");
    }

}
