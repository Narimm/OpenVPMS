package org.openvpms.web.app.supplier;

import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.CRUDWorkspace;


/**
 * Supplier information workspace.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class InformationWorkspace extends CRUDWorkspace {

    /**
     * Construct a new <code>InformationWorkspace</code>.
     */
    public InformationWorkspace() {
        super("supplier", "info", "party", "party", "supplier*");
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
    protected CRUDWindow createCRUDWindow(String subsystemId, String workspaceId,
                                          String refModelName, String entityName,
                                          String conceptName) {
        CRUDWindow window = super.createCRUDWindow(subsystemId, workspaceId,
                refModelName, entityName, conceptName);
        window.setCRUDWindowListener(new SupplierCRUDWindowListener());
        return window;
    }

}
