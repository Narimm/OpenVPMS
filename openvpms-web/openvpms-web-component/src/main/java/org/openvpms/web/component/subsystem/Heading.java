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

package org.openvpms.web.component.subsystem;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.web.component.button.ShortcutHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Helper class to generate a workspace heading.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public final class Heading {

    /**
     * Returns the heading component.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identifier
     * @return the heading component
     */
    public static Component getHeading(String subsystemId, String workspaceId) {
        String subsystem = Messages.get("subsystem." + subsystemId);
        String workspace = Messages.get("workspace." + subsystemId
                                        + "." + workspaceId);
        subsystem = ShortcutHelper.getText(subsystem);
        workspace = ShortcutHelper.getText(workspace);
        String text = Messages.get("workspace.heading", subsystem, workspace);

        Label heading = LabelFactory.create(null, "Workspace.Heading");
        heading.setText(text);
        return RowFactory.create("ControlRow", heading);
    }


}
