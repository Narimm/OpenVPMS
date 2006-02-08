package org.openvpms.web.app.subsystem;

import org.openvpms.web.component.subsystem.AbstractWorkspace;


/**
 * Dummy workspace.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class DummyWorkspace extends AbstractWorkspace {


    /**
     * Construct a new <code>DummyWorkspace</code>.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identfifier
     */
    public DummyWorkspace(String subsystemId, String workspaceId) {
        super(subsystemId, workspaceId);
    }

}
