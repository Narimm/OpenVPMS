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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.communication;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.PatientReferenceEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.DateRangeActQuery;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.focus.FocusGroup;


/**
 * Query for <em>act.customerCommunication</em> acts.
 *
 * @author Tim Anderson
 */
public class CommunicationQuery extends DateRangeActQuery<Act> {

    /**
     * The patient.
     */
    private final SimpleProperty patient = new SimpleProperty(
            DescriptorHelper.getDisplayName(CommunicationArchetypes.EMAIL, "patient"), IMObjectReference.class);

    /**
     * The patient selector.
     */
    private PatientReferenceEditor patientSelector;

    /**
     * Constructs a {@link CommunicationQuery}.
     *
     * @param customer the customer to query notes for
     */
    public CommunicationQuery(Party customer, LayoutContext context) {
        super(customer, "customer", "participation.customer", new String[]{CommunicationArchetypes.ACTS}, Act.class);
        patient.setArchetypeRange(new String[]{PatientArchetypes.PATIENT});
        DefaultLayoutContext layoutContext = new DefaultLayoutContext(new LocalContext(context.getContext()),
                                                                      context.getHelpContext());
        patientSelector = new PatientReferenceEditor(patient, null, layoutContext);
        setAuto(true);
    }

    /**
     * Returns query constraints.
     *
     * @return the constraints. May be {@code null}
     */
    @Override
    public IConstraint getConstraints() {
        IMObjectReference patientRef = patient.getReference();
        if (patientRef != null) {
            return new ParticipantConstraint("patient", PatientArchetypes.PATIENT_PARTICIPATION, patientRef);
        }
        return null;
    }

    /**
     * Lays out the component in a container.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        Label patientLabel = LabelFactory.create();
        patientLabel.setText(patient.getDisplayName());

        container.add(patientLabel);
        container.add(patientSelector.getComponent());
        FocusGroup group = getFocusGroup();
        group.add(patientSelector.getFocusGroup());
        super.doLayout(container);
        group.setFocus();
    }

}
