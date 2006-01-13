package org.openvpms.web.app.workspace;


/**
 * Generic CRUD pane.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class CRUDPane extends AbstractCRUDPane {

    /**
     * Construct a new <code>CRUDPane</code>.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     * @param id           the localisation identfifier
     */
    public CRUDPane(String refModelName, String entityName,
                    String conceptName,
                    String id) {
        super(refModelName, entityName, conceptName, id);
    }

}
