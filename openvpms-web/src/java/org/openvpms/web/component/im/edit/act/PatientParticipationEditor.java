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

package org.openvpms.web.component.im.edit.act;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.app.patient.PatientBrowser;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextHelper;
import org.openvpms.web.component.im.edit.AbstractIMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.PatientObjectSetQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryAdapter;
import org.openvpms.web.component.property.Property;


/**
 * Participation editor for patients. This defaults the patient to that
 * contained in the context if the none is selected, and the parent object
 * is new.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PatientParticipationEditor extends ParticipationEditor<Party> {

    /**
     * The associated customer participation editor. May be <tt>null</tt>.
     */
    private CustomerParticipationEditor customerEditor;


    /**
     * Constructs a <tt>PatientParticipationEditor</tt>.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param layout        the layout context. May be <tt>null</tt>
     */
    public PatientParticipationEditor(Participation participation,
                                      Act parent,
                                      LayoutContext layout) {
        super(participation, parent, layout);
        if (!TypeHelper.isA(participation, "participation.patient")) {
            throw new IllegalArgumentException(
                    "Invalid participation type:"
                    + participation.getArchetypeId().getShortName());
        }
        Context context = getLayoutContext().getContext();
        IMObjectReference patientRef = participation.getEntity();
        if (patientRef == null && parent.isNew()) {
            setEntity(context.getPatient());
        } else {
            // add the existing patient to the context
            Party patient = (Party) getObject(patientRef);
            if (patient != null && patient != context.getPatient()) {
                ContextHelper.setPatient(context, patient);
            }
        }
    }

    /**
     * Associates a customer participation editor with this.
     * <p/>
     * If non-null, the customer will be updated when a patient is selected in the browser.
     *
     * @param editor the editor. May be <tt>null</tt>
     */
    public void setCustomerParticipationEditor(CustomerParticipationEditor editor) {
        customerEditor = editor;
    }

    /**
     * Creates a new object reference editor.
     *
     * @param property the reference property
     * @return a new object reference editor
     */
    @Override
    protected IMObjectReferenceEditor<Party> createEntityEditor(Property property) {
        return new AbstractIMObjectReferenceEditor<Party>(property, getParent(), getLayoutContext(), true) {

            @Override
            public boolean setObject(Party object) {
                ContextHelper.setPatient(getLayoutContext().getContext(), object);
                return super.setObject(object);
            }

            /**
             * Invoked when an object is selected from a brwoser.
             * <p/>
             * This updates the patient, and if specified, the associated customer participation editor's customer.
             *
             * @param object  the selected object. May be <tt>null</tt>
             * @param browser the browser
             */
            @Override
            protected void onSelected(Party object, Browser<Party> browser) {
                super.onSelected(object, browser);
                if (customerEditor != null && browser instanceof PatientBrowser) {
                    Party customer = ((PatientBrowser) browser).getCustomer();
                    if (customer != null && !ObjectUtils.equals(customer, customerEditor.getEntity())) {
                        customerEditor.setEntity(customer);
                    }
                }
            }

            /**
             * Determines if a reference is valid.
             * <p/>
             * This implementation allows both active and inactive patients.
             *
             * @param reference the reference to check
             * @return <tt>true</tt> if the query selects the reference
             */
            @Override
            protected boolean isValidReference(IMObjectReference reference) {
                Query<Party> query = createQuery(null);
                if (query instanceof QueryAdapter && ((QueryAdapter) query).getQuery() instanceof PatientObjectSetQuery) {
                    PatientObjectSetQuery q = (PatientObjectSetQuery) ((QueryAdapter) query).getQuery();
                    q.setActiveOnly(false);
                }
                return query.selects(reference);
            }
        };
    }

}
