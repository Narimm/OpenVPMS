package org.openvpms.web.app.workspace;

/**
 * Archetype workspace.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class LookupWorkspace extends CRUDWorkspace {

    /**
     * Construct a new <code>ClassificationWorkspace</code>.
     */
    public LookupWorkspace() {
        super("lookup", null, null, "lookup");
    }

}
