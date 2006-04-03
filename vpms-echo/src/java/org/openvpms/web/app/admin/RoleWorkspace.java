package org.openvpms.web.app.admin;

import org.openvpms.web.app.subsystem.CRUDWorkspace;


/**
 * Role workspace.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class RoleWorkspace extends CRUDWorkspace {

    /**
     * Construct a new <code>ClassificationWorkspace</code>.
     */
    public RoleWorkspace() {
        super("admin", "role", "system", "security", "role");
    }

}
