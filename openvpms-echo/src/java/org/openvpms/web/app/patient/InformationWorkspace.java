package org.openvpms.web.app.patient;

import org.openvpms.web.app.subsystem.CRUDWorkspace;


/**
 * Patient information workspace.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
 */
public class InformationWorkspace extends CRUDWorkspace {

    /**
     * Construct a new <code>InformationWorkspace</code>.
     */
    public InformationWorkspace() {
        super("patient.info", "party", "animal", "pet");
    }

}
