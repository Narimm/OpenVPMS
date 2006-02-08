package org.openvpms.web.component.subsystem;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;

import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.component.RowFactory;
import org.openvpms.web.util.Messages;


/**
 * Helper class to generate a workspace heading.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
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
        String text = Messages.get("workspace.heading", subsystem, workspace);

        Label heading = LabelFactory.create();
        heading.setText(text);
        return RowFactory.create("Workspace.Heading", heading);
    }


}
