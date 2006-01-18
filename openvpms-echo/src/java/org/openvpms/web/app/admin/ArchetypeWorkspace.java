package org.openvpms.web.app.admin;

import org.openvpms.web.app.subsystem.CRUDWorkspace;


/**
 * Archetype workspace.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
 */
public class ArchetypeWorkspace extends CRUDWorkspace {

    /**
     * Construct a new <code>ArchetypeWorkspace</code>.
     */
    public ArchetypeWorkspace() {
        super("admin.archetype", "system", "descriptor", null);
    }

}
