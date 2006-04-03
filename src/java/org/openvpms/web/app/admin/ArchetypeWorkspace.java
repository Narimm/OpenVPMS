package org.openvpms.web.app.admin;

import org.openvpms.web.app.subsystem.CRUDWorkspace;


/**
 * Archetype workspace.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ArchetypeWorkspace extends CRUDWorkspace {

    /**
     * Construct a new <code>ArchetypeWorkspace</code>.
     */
    public ArchetypeWorkspace() {
        super("admin", "archetype", "system", "descriptor", null);
    }

}
