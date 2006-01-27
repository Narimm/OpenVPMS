package org.openvpms.web.app.patient;

import java.util.Date;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.app.Context;
import org.openvpms.web.app.subsystem.CRUDWindowListener;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Patient CRUD pane listener.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class PatientCRUDWindowListener implements CRUDWindowListener {

    /**
     * Invoked when a new object is selected.
     *
     * @param object the selcted object
     */
    public void selected(IMObject object) {
    }

    /**
     * Invoked when an object is saved.
     *
     * @param object the saved object
     */
    public void saved(IMObject object) {
        Entity patient = (Entity) object;
        Context context = Context.getInstance();
        Entity customer = context.getCustomer();
        if (customer != null) {
            IArchetypeService service = ServiceHelper.getArchetypeService();
            EntityRelationship relationship
                    = (EntityRelationship) service.create("entityRelationship.patientOwner");
            relationship.setActiveStartTime(new Date());
            relationship.setSequence(1);
            relationship.setSource(new IMObjectReference(customer));
            relationship.setTarget(new IMObjectReference(patient));

            EntityRelationship copy;
            try {
                copy = (EntityRelationship) relationship.clone();
            } catch (CloneNotSupportedException exception) {
                throw new IllegalArgumentException(exception);
            }
            customer.addEntityRelationship(relationship);
            patient.addEntityRelationship(copy);
            service.save(customer);
            service.save(patient);
        }
    }

    /**
     * Invoked when an object is deleted
     *
     * @param object the deleted object
     */
    public void deleted(IMObject object) {
    }
}
