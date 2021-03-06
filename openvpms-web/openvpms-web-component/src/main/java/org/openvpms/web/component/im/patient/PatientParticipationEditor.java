/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.patient;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.alert.MandatoryAlerts;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextHelper;
import org.openvpms.web.component.im.customer.CustomerParticipationEditor;
import org.openvpms.web.component.im.edit.AbstractIMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryAdapter;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;


/**
 * Participation editor for patients. This defaults the patient to that
 * contained in the context if the none is selected, and the parent object
 * is new.
 *
 * @author Tim Anderson
 */
public class PatientParticipationEditor extends ParticipationEditor<Party> {

    /**
     * Displays mandatory alerts when the patient is selected.
     */
    private final MandatoryAlerts alerts;

    /**
     * The associated customer participation editor. May be {@code null}.
     */
    private CustomerParticipationEditor customerEditor;

    /**
     * The container row.
     */
    private Row row;

    /**
     * Constructs a {@link PatientParticipationEditor}.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param layout        the layout context
     */
    public PatientParticipationEditor(Participation participation, Act parent, LayoutContext layout) {
        super(participation, parent, layout);
        if (!TypeHelper.isA(participation, PatientArchetypes.PATIENT_PARTICIPATION)) {
            throw new IllegalArgumentException("Invalid participation type:" + participation.getArchetype());
        }
        Context context = getLayoutContext().getContext();
        alerts = new MandatoryAlerts(context, layout.getHelpContext());
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
     * <p>
     * If non-null, the customer will be updated when a patient is selected in the browser.
     *
     * @param editor the editor. May be {@code null}
     */
    public void setCustomerParticipationEditor(CustomerParticipationEditor editor) {
        customerEditor = editor;
    }

    /**
     * Displays any unacknowledged alerts for the patient.
     */
    public void showAlerts() {
        alerts.show(getEntity());
    }

    /**
     * Returns the rendered object.
     *
     * @return the rendered object
     */
    @Override
    public Component getComponent() {
        if (row == null) {
            Button info = PatientSummaryViewer.createButton(getLayoutContext(), this::getEntity);
            row = RowFactory.create(Styles.CELL_SPACING, super.getComponent(), info);
        }
        return row;
    }

    /**
     * Creates a new object reference editor.
     *
     * @param property the reference property
     * @return a new object reference editor
     */
    @Override
    protected IMObjectReferenceEditor<Party> createEntityEditor(Property property) {
        LayoutContext context = getLayoutContext();
        LayoutContext subContext = new DefaultLayoutContext(context, context.getHelpContext().topic("patient"));
        return new AbstractIMObjectReferenceEditor<Party>(property, getParent(), subContext, true) {

            @Override
            public boolean setObject(Party object) {
                ContextHelper.setPatient(getLayoutContext().getContext(), object);
                return super.setObject(object);
            }

            /**
             * Invoked when an object is selected.
             * <p/>
             * Any mandatory alerts will be displayed.
             *
             * @param object the selected object. May be {@code null}
             */
            @Override
            protected void onSelected(Party object) {
                super.onSelected(object);
                alerts.show(object);
            }

            /**
             * Invoked when an object is selected from a browser.
             * <p/>
             * This updates the patient, and if specified, the associated customer participation editor's customer.
             * <p/>
             * Any mandatory alerts will be displayed.
             *
             * @param object  the selected object. May be {@code null}
             * @param browser the browser
             */
            @Override
            protected void onSelected(Party object, Browser<Party> browser) {
                super.onSelected(object, browser);
                alerts.show(object);
                if (customerEditor != null && browser instanceof PatientBrowser) {
                    Party customer = ((PatientBrowser) browser).getCustomer();
                    if (customer != null && !ObjectUtils.equals(customer, customerEditor.getEntity())) {
                        customerEditor.setEntity(customer);
                        customerEditor.showAlerts();
                    }
                }
            }

            /**
             * Determines if a reference is valid.
             * <p/>
             * This implementation allows both active and inactive patients.
             *
             * @param reference the reference to check
             * @return {@code true} if the query selects the reference
             */
            @Override
            protected boolean isValidReference(IMObjectReference reference) {
                Query<Party> query = createQuery(null);
                if (query instanceof QueryAdapter
                    && ((QueryAdapter) query).getQuery() instanceof PatientObjectSetQuery) {
                    PatientObjectSetQuery q = (PatientObjectSetQuery) ((QueryAdapter) query).getQuery();
                    q.setActiveOnly(false);
                }
                return query.selects(reference);
            }
        };
    }

}
