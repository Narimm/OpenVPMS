package org.openvpms.web.app.admin;

import org.openvpms.web.app.subsystem.CRUDWorkspace;


/**
 * Classification workspace.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ClassificationWorkspace extends CRUDWorkspace {

    /**
     * Construct a new <code>ClassificationWorkspace</code>.
     */
    public ClassificationWorkspace() {
        super("admin", "classification", "common", "classification", null);
    }

}
