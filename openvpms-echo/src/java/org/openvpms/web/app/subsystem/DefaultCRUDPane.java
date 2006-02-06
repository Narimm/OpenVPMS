package org.openvpms.web.app.subsystem;


/**
 * Generic CRUD pane.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class DefaultCRUDPane extends AbstractCRUDPane {

    /**
     * Construct a new <code>DefaultCRUDPane</code>.
     *
     * @param subsystemId  the subsystem localisation identifier
     * @param workspaceId  the workspace localisation identfifier
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public DefaultCRUDPane(String subsystemId, String workspaceId, String refModelName,
                           String entityName, String conceptName) {
        super(subsystemId, workspaceId, refModelName, entityName, conceptName);
    }

}
