package org.openvpms.web.app.patient;

import java.util.Date;
import java.util.Set;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
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
     * Patient/owner relationship short name.
     */
    private static final String PATIENT_OWNER
            = "entityRelationship.patientOwner";


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
            if (!hasRelationship(PATIENT_OWNER, patient, customer)) {
                EntityRelationship relationship
                        = (EntityRelationship) service.create(PATIENT_OWNER);
                relationship.setActiveStartTime(new Date());
                relationship.setSequence(1);
                relationship.setSource(new IMObjectReference(customer));
                relationship.setTarget(new IMObjectReference(patient));

                customer.addEntityRelationship(relationship);
                service.save(customer);
            }
        }
    }

    /**
     * Invoked when an object is deleted
     *
     * @param object the deleted object
     */
    public void deleted(IMObject object) {
    }

    /**
     * Determines if a relationship of the specified type exists.
     *
     * @param shortName the relationship short name
     * @param patient   the patient
     * @param customer  the customer
     * @return <code>true</code> if a relationship exists; otherwsie
     *         <code>false</code>
     */
    private boolean hasRelationship(String shortName, Entity patient,
                                    Entity customer) {
        boolean result = false;
        Set<EntityRelationship> relationships
                = patient.getEntityRelationships();
        IMObjectReference source = new IMObjectReference(customer);
        IMObjectReference target = new IMObjectReference(patient);

        for (EntityRelationship relationship : relationships) {
            ArchetypeId id = relationship.getArchetypeId();
            if (id.getShortName().equals(shortName)) {
                if (source.equals(relationship.getSource())
                        && target.equals(relationship.getTarget())) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

}
