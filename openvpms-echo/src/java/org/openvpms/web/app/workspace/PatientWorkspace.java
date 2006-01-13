package org.openvpms.web.app.workspace;

/**
 * Patient workspace.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class PatientWorkspace extends CRUDWorkspace {

    /**
     * Construct a new <code>PatientWorkspace</code>.
     */
    public PatientWorkspace() {
        super("party", "animal", "pet", "patient");
    }

}
