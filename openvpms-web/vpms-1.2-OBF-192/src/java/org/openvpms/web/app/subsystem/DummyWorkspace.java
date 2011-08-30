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

package org.openvpms.web.app.subsystem;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.subsystem.AbstractWorkspace;


/**
 * Dummy workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class DummyWorkspace extends AbstractWorkspace<IMObject> {

    /**
     * Construct a new <tt>DummyWorkspace</tt>.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identfifier
     */
    public DummyWorkspace(String subsystemId, String workspaceId) {
        super(subsystemId, workspaceId);
    }

    /**
     * Returns the class type that this operates on.
     *
     * @return the class type that this operates on
     */
    protected Class<IMObject> getType() {
        return IMObject.class;
    }

}
