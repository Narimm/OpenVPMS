package org.openvpms.web.app.patient;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.Context;
import org.openvpms.web.app.subsystem.AbstractCRUDPane;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.query.Browser;


/**
 * Patient CRUD pane.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class PatientCRUDPane extends AbstractCRUDPane {

    /**
     * Construct a new <code>DefaultCRUDPane</code>.
     *
     * @param subsystemId  the subsystem localisation identifier
     * @param workspaceId  the workspace localisation identfifier
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public PatientCRUDPane(String subsystemId, String workspaceId,
                           String refModelName,
                           String entityName, String conceptName) {
        super(subsystemId, workspaceId, refModelName, entityName, conceptName);
    }

    /**
     * Create a new browser.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     * @return a new browser
     */
    @Override
    protected Browser createBrowser(String refModelName, String entityName,
                                    String conceptName) {
        Party customer = Context.getInstance().getCustomer();
        Query query = new PatientQuery(refModelName, entityName, conceptName,
                customer);
        Browser result = new Browser(query);
        if (customer != null) {
            result.query();
        }
        return result;
    }

}
