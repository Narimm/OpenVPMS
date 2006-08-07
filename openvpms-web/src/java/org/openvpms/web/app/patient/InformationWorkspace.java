/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.patient;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.subsystem.CRUDWorkspace;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.spring.ServiceHelper;

import java.util.Date;
import java.util.Set;


/**
 * Patient information workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
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
     * Sets the current object.
     *
     * @param object the object. May be <code>null</code>
     */
    @Override
    public void setObject(IMObject object) {
        super.setObject(object);
        Context.getInstance().setPatient((Party) object);
    }

    /**
     * Renders the workspace summary.
     *
     * @return the component representing the workspace summary, or
     *         <code>null</code> if there is no summary
     */
    @Override
    public Component getSummary() {
        return PatientSummary.getSummary((Party) getObject());
    }


    /**
     * Determines if the workspace should be refreshed. This implementation
     * returns true if the current patient has changed.
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
     * Lays out the component.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        super.doLayout(container);
        Party patient = Context.getInstance().getPatient();
        if (patient != getObject()) {
            setObject(patient);
        }
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(IMObject object, boolean isNew) {
        super.onSaved(object, isNew);
        Party patient = (Party) object;
        Context context = Context.getInstance();
        Party customer = context.getCustomer();
        IArchetypeService service = ServiceHelper.getArchetypeService();
        if (customer != null && isNew) {
            if (!hasRelationship(PATIENT_OWNER, patient, customer)) {
                addRelationship(PATIENT_OWNER, patient, customer, service);
                // refresh the customer
                customer = (Party) IMObjectHelper.reload(customer);
                context.setCustomer(customer);
            }
        }
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
                String msg = Messages.get("imobject.create.failed", shortName);
                ErrorHelper.show(msg);
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
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
