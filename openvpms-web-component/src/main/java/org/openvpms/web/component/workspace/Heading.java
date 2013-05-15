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

package org.openvpms.web.component.workspace;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.web.echo.button.ShortcutHelper;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.resource.i18n.Messages;


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
     * @param workspacesId the workspace group localisation identifier
     * @param workspaceId  the workspace localisation identifier
     * @return the heading component
     */
    public static Component getHeading(String workspacesId, String workspaceId) {
        String workspaces = Messages.get("workspaces." + workspacesId);
        String workspace = Messages.get("workspace." + workspacesId + "." + workspaceId);
        workspaces = ShortcutHelper.getText(workspaces);
        workspace = ShortcutHelper.getText(workspace);
        String text = Messages.get("workspace.heading", workspaces, workspace);

        Label heading = LabelFactory.create(null, "Workspace.Heading");
        heading.setText(text);
        return RowFactory.create("ControlRow", heading);
    }


}
