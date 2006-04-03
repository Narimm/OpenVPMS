package org.openvpms.web.app.admin;

import org.openvpms.web.app.subsystem.CRUDWorkspace;


/**
 * User workspace.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class LookupWorkspace extends CRUDWorkspace {

    /**
     * Construct a new <code>ClassificationWorkspace</code>.
     */
    public LookupWorkspace() {
        super("admin", "lookup", "lookup", null, null);
    }

}
