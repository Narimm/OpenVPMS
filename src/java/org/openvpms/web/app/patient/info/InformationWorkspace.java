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

package org.openvpms.web.app.patient.info;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.patient.summary.PatientSummary;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.CRUDWorkspace;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.app.subsystem.ShortNames;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.Set;


/**
 * Patient information workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class InformationWorkspace extends CRUDWorkspace {

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
        GlobalContext.getInstance().setPatient((Party) object);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
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
        Party patient = GlobalContext.getInstance().getPatient();
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
        Party patient = GlobalContext.getInstance().getPatient();
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
        if (isNew) {
            // todo  need to do the following to force a refresh of the customer
            // to reflect any new entity relationships with the current patient.
            // This is a poor solution - need a better way of indicating that
            // an object has changed.
            Party patient = (Party) object;
            GlobalContext context = GlobalContext.getInstance();
            Party customer = context.getCustomer();
            if (customer != null && isNew) {
                if (hasRelationship(patient, customer)) {
                    // refresh the customer
                    customer = (Party) IMObjectHelper.reload(customer);
                    context.setCustomer(customer);
                }
            }
        }
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    @Override
    protected CRUDWindow createCRUDWindow
            () {
        ShortNames shortNames = new ShortNameList(getRefModelName(),
                                                  getEntityName(),
                                                  getConceptName());
        return new InformationCRUDWindow(getType(), shortNames);
    }

    /**
     * Determines if a relationship between a customer and patient exists.
     *
     * @param patient  the patient
     * @param customer the customer
     * @return <code>true</code> if a relationship exists; otherwsie
     *         <code>false</code>
     */
    private boolean hasRelationship(Party patient, Party customer) {
        boolean result = false;
        Set<EntityRelationship> relationships
                = patient.getEntityRelationships();
        IMObjectReference source = new IMObjectReference(customer);
        IMObjectReference target = new IMObjectReference(patient);

        for (EntityRelationship relationship : relationships) {
            if (source.equals(relationship.getSource())
                    && target.equals(relationship.getTarget())) {
                result = true;
                break;
            }
        }
        return result;
    }

}
