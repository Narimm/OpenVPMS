package org.openvpms.web.app.patient;

import java.util.Date;
import java.util.Set;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.app.subsystem.CRUDWorkspace;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Patient information workspace.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class InformationWorkspace extends CRUDWorkspace {

    /**
     * Patient/owner relationship short name.
     */
    private static final String PATIENT_OWNER
            = "entityRelationship.patientOwner";

    /**
     * Construct a new <code>InformationWorkspace</code>.
     */
    public InformationWorkspace() {
        super("patient", "info", "party", "party", "patient*");
    }

    /**
     * Determines if the workspace should be refreshed. This implementation
     * returns true if the current customer has changed.
     *
     * @return <code>true</code> if the workspace should be refreshed, otherwise
     *         <code>false</code>
     */
    @Override
    protected boolean refreshWorkspace() {
        Party patient = Context.getInstance().getPatient();
        return (patient != getObject());
    }

    /**
     * Invoked when an object is selected.
     *
     * @param object the selected object
     */
    @Override
    protected void onSelected(IMObject object) {
        super.onSelected(object);
        Context.getInstance().setPatient((Party) object);
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(IMObject object, boolean isNew) {
        Party patient = (Party) object;
        Context.getInstance().setPatient(patient);
        Context context = Context.getInstance();
        Party customer = context.getCustomer();
        IArchetypeService service = ServiceHelper.getArchetypeService();
        if (customer != null && isNew) {
            if (!hasRelationship(PATIENT_OWNER, patient, customer)) {
                addRelationship(PATIENT_OWNER, patient, customer, service);
                // refresh the customer
                try {
                    customer = (Party) service.getById(
                            customer.getArchetypeId(),
                            customer.getUid());
                    context.setCustomer(customer);
                } catch (ArchetypeServiceException exception) {
                    ErrorDialog.show(exception);
                }
            }
        }
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    @Override
    protected void onDeleted(IMObject object) {
        super.onDeleted(object);
        Context.getInstance().setPatient(null);
    }

    /**
     * Add a new relationship.
     *
     * @param shortName the relationship short name
     * @param patient   the patient
     * @param customer  the customer
     */
    private void addRelationship(String shortName, Entity patient,
                                 Party customer, IArchetypeService service) {
        try {
            EntityRelationship relationship;
            relationship = (EntityRelationship) service.create(shortName);
            if (relationship != null) {
                relationship.setActiveStartTime(new Date());
                relationship.setSequence(1);
                relationship.setSource(new IMObjectReference(customer));
                relationship.setTarget(new IMObjectReference(patient));

                patient.addEntityRelationship(relationship);
                service.save(patient);
            } else {
                ErrorDialog.show("Failed to create relationship of type="
                                 + shortName);
            }
        } catch (ArchetypeServiceException exception) {
            ErrorDialog.show(exception);
        }
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
