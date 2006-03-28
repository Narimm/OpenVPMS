package org.openvpms.web.app.subsystem;

import org.openvpms.component.business.domain.im.common.IMObject;
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


    /**
     * Determines if the workspace supports an archetype.
     *
     * @param shortName the archetype's short name
     * @return <code>true</code> if the workspace can handle the archetype;
     *         otherwise <code>false</code>
     */
    public boolean canHandle(String shortName) {
        return false;
    }

    /**
     * Sets the object to be viewed/edited by the workspace.
     *
     * @param object the object. May be <code>null</code>
     */
    public void setObject(IMObject object) {
        // no-op
    }

    /**
     * Returns the object to to be viewed/edited by the workspace.
     *
     * @return the the object. May be <oode>null</code>
     */
    public IMObject getObject() {
        return null;
    }
}
