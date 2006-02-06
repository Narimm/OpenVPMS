package org.openvpms.web.app.customer;

import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.CRUDWorkspace;


/**
 * Customer information workspace.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
 */
public class InformationWorkspace extends CRUDWorkspace {

    /**
     * Construct a new <code>InformationWorkspace</code>.
     */
    public InformationWorkspace() {
        super("customer", "info", "party", "party", "customer*");
    }

    /**
     * Create a new CRUD component.
     *
     * @param subsystemId  the subsystem localisation identifier
     * @param workspaceId  the workspace localisation identfifier
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    @Override
    protected CRUDWindow createCRUDWindow(String subsystemId,
                                          String workspaceId,
                                          String refModelName,
                                          String entityName,
                                          String conceptName) {
        CRUDWindow window = super.createCRUDWindow(subsystemId, workspaceId,
                refModelName, entityName, conceptName);
        window.setCRUDWindowListener(new CustomerCRUDWindowListener());
        return window;
    }
}
