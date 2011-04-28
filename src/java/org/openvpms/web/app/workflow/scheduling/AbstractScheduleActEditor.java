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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.workflow.scheduling;

import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.bound.BoundDateTimeField;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.edit.act.CustomerParticipationEditor;
import org.openvpms.web.component.im.edit.act.ParticipationCollectionEditor;
import org.openvpms.web.component.im.edit.act.PatientParticipationEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.DateTimePropertyTransformer;
import org.openvpms.web.component.util.DateTimeFieldFactory;
import org.openvpms.web.component.util.ErrorHelper;


/**
 * An editor for <em>act.customerAppointment</em> and <em>act.customerTask</em> acts.
 * <p/>
 * This displays a date/time for the start and end times.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AbstractScheduleActEditor extends AbstractActEditor {

    /**
     * Constructs a new <tt>AbstractScheduleActEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context. May be <tt>null</tt>
     */
    public AbstractScheduleActEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
    }

    /**
     * Sets the customer.
     *
     * @param customer the customer. May be <tt>null</tt>
     */
    public void setCustomer(Party customer) {
        getCustomerEditor().setEntity(customer);
    }

    /**
     * Returns the customer.
     *
     * @return the customer. May be <tt>null</tt>
     */
    public Party getCustomer() {
        return (Party) getParticipant("customer");
    }

    /**
     * Returns the patient.
     *
     * @return the patient. May be <tt>null</tt>
     */
    public Party getPatient() {
        return (Party) getParticipant("patient");
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new LayoutStrategy();
    }

    /**
     * Invoked when layout has completed. All editors have been created.
     * <p/>
     * This adds a listener to invoke {@link #onCustomerChanged()} when the customer changes.
     */
    @Override
    protected void onLayoutCompleted() {
        CustomerParticipationEditor customer = getCustomerEditor();
        PatientParticipationEditor patient = getPatientEditor();
        customer.setPatientParticipationEditor(patient);
        customer.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onCustomerChanged();
            }
        });
    }

    /**
     * Returns the patient editor.
     *
     * @return the patient editor
     */
    protected PatientParticipationEditor getPatientEditor() {
        ParticipationCollectionEditor editor = (ParticipationCollectionEditor) getEditor("patient");
        return (PatientParticipationEditor) editor.getCurrentEditor();
    }

    /**
     * Returns the customer editor.
     *
     * @return the customer editor
     */
    protected CustomerParticipationEditor getCustomerEditor() {
        return (CustomerParticipationEditor) getEditor("customer");
    }

    /**
     * Invoked when the customer changes. Sets the patient to null if no
     * relationship exists between the two.
     */
    protected void onCustomerChanged() {
        try {
            Party customer = getCustomerEditor().getEntity();
            Party patient = getPatientEditor().getEntity();
            PatientRules rules = new PatientRules();
            if (customer != null && patient != null) {
                if (!rules.isOwner(customer, patient)) {
                    getPatientEditor().setEntity(null);
                }
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Layout strategy to create a date/time for act the start end end times.
     */
    protected class LayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Creates a component for a property.
         *
         * @param property the property
         * @param parent   the parent object
         * @param context  the layout context
         * @return a component to display <code>property</code>
         */
        @Override
        protected ComponentState createComponent(Property property, IMObject parent, LayoutContext context) {
            ComponentState result;
            String name = property.getName();
            if (name.equals(START_TIME) || name.equals(END_TIME)) {
                BoundDateTimeField field = DateTimeFieldFactory.create(property);
                DateTimePropertyTransformer transformer = (DateTimePropertyTransformer) property.getTransformer();
                transformer.setKeepSeconds(false);
                // remove seconds as these screw up appointment slot comparisons, and they aren't needed for tasks 
                result = new ComponentState(field.getComponent(), property, field.getFocusGroup());
            } else {
                result = super.createComponent(property, parent, context);
            }
            return result;
        }
    }
}
