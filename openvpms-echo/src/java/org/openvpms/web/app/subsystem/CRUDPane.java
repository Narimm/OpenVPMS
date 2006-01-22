package org.openvpms.web.app.subsystem;


/**
 * Generic CRUD pane.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
 */
public class CRUDPane extends AbstractCRUDPane {

    /**
     * Construct a new <code>CRUDPane</code>.
     *
     * @param subsystemId  the subsystem localisation identifier
     * @param id           the workspace localisation identfifier
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public CRUDPane(String subsystemId, String id, String refModelName,
                    String entityName, String conceptName) {
        super(subsystemId, id, refModelName, entityName, conceptName);
    }

}
