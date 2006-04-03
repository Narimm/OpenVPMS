package org.openvpms.web.app.admin;

import org.openvpms.web.app.subsystem.CRUDWorkspace;


/**
 * User workspace.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class UserWorkspace extends CRUDWorkspace {

    /**
     * Construct a new <code>ClassificationWorkspace</code>.
     */
    public UserWorkspace() {
        super("admin", "user", "system", "security", "*");
    }

}
