/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.hl7;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.hl7.HL7Archetypes;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.workspace.ResultSetCRUDWorkspace;


/**
 * HL7 workspace.
 *
 * @author Tim Anderson
 */
public class HL7Workspace extends ResultSetCRUDWorkspace<Entity> {

    /**
     * Constructs an {@code UserWorkspace}.
     */
    public HL7Workspace(Context context) {
        super("admin", "hl7", context);
        setArchetypes(Entity.class, HL7Archetypes.SENDERS, HL7Archetypes.RECEIVERS, HL7Archetypes.SERVICES);
    }

}
