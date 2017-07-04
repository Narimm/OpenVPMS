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

package org.openvpms.web.workspace.admin.template;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.sms.util.SMSLengthCalculator;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.customer.CustomerReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.PatientReferenceEditor;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.sms.BoundSMSTextArea;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import static org.openvpms.archetype.rules.workflow.ScheduleArchetypes.APPOINTMENT;

/**
 * A component to test the expression evaluation of SMS templates.
 *
 * @author Tim Anderson
 */
public abstract class SMSTemplateSampler {

    /**
     * The template.
     */
    private final Entity template;

    /**
     * The maximum no. of parts in SMS messages.
     */
    private final int maxParts;

    /**
     * The layout context.
     */
    private final LayoutContext layoutContext;

    /**
     * The customer to test against.
     */
    private final SimpleProperty customer = new SimpleProperty(
            "customer", null, IMObjectReference.class, DescriptorHelper.getDisplayName(APPOINTMENT, "customer"));

    /**
     * The patient to test against.
     */
    private final SimpleProperty patient = new SimpleProperty(
            "patient", null, IMObjectReference.class, DescriptorHelper.getDisplayName(APPOINTMENT, "patient"));

    /**
     * The property to hold the generated message.
     */
    private final SimpleProperty message = new SimpleProperty("message", null, String.class,
                                                              Messages.get("sms.message"));

    /**
     * The evaluation status.
     */
    private Label status;

    /**
     * The focus group.
     */
    private FocusGroup group = new FocusGroup("Sampler");

    /**
     * Constructs an {@link SMSTemplateSampler}.
     *
     * @param template      the template
     * @param layoutContext the layout context
     */
    public SMSTemplateSampler(Entity template, LayoutContext layoutContext) {
        this.template = template;
        maxParts = ServiceHelper.getSMSConnectionFactory().getMaxParts();
        message.setMaxLength(-1); // don't limit message length

        // create a local context so changes aren't propagated
        Context local = new LocalContext(layoutContext.getContext());
        this.layoutContext = new DefaultLayoutContext(true, local, layoutContext.getHelpContext());

        customer.setArchetypeRange(new String[]{CustomerArchetypes.PERSON});
        patient.setArchetypeRange(new String[]{PatientArchetypes.PATIENT});
        ModifiableListener listener = new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                evaluate();
            }
        };
        customer.addModifiableListener(listener);
        patient.addModifiableListener(listener);
        status = LabelFactory.create(true, true);
    }

    /**
     * Evaluates the template.
     */
    public void evaluate() {
        String value;
        String statusText = null;
        try {
            value = evaluate(template, layoutContext.getContext());
            if (value != null) {
                int maxLength = SMSLengthCalculator.getMaxLength(maxParts, value);
                if (value.length() > maxLength) {
                    value = value.substring(0, maxLength);
                    statusText = Messages.get("sms.truncated");
                }
            }
        } catch (Throwable exception) {
            value = null;
            statusText = (exception.getCause() != null) ? exception.getCause().getMessage()
                                                        : exception.getMessage();
        }
        message.setValue(value);
        status.setText(statusText);
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return group;
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        FocusGroup group = getFocusGroup();
        BoundSMSTextArea message = new BoundSMSTextArea(this.message, 40, 15);
        message.setMaxParts(maxParts);
        message.setStyleName(Styles.DEFAULT);
        message.setEnabled(false);

        Label messageLabel = LabelFactory.create();
        messageLabel.setText(this.message.getDisplayName());

        ComponentGrid grid = new ComponentGrid();
        layoutFields(grid, group);
        grid.add(LabelFactory.create("sms.title", Styles.BOLD), LabelFactory.create(),
                 LabelFactory.create("sms.appointment.status", Styles.BOLD));
        status.setTextAlignment(Alignment.ALIGN_TOP);
        status.setLayoutData(ComponentGrid.layout(new Alignment(Alignment.LEFT, Alignment.TOP)));
        grid.add(LabelFactory.create("sms.message"), RowFactory.create(message));
        grid.set(2, 2, 2, status);
        group.add(message);
        return ColumnFactory.create(Styles.INSET, grid.createGrid());
    }

    /**
     * Lays out the editable fields in a grid.
     *
     * @param grid  the grid
     * @param group the focus group
     */
    protected void layoutFields(ComponentGrid grid, FocusGroup group) {
        IMObjectReferenceEditor customerSelector = new CustomerReferenceEditor(customer, null, layoutContext);
        IMObjectReferenceEditor patientSelector = new PatientReferenceEditor(patient, null, layoutContext);
        Label customerLabel = LabelFactory.create();
        customerLabel.setText(customer.getDisplayName());

        Label patientLabel = LabelFactory.create();
        patientLabel.setText(patient.getDisplayName());

        grid.add(customerLabel, customerSelector.getComponent(), patientLabel, patientSelector.getComponent());
        group.add(customerSelector.getFocusGroup());
        group.add(patientSelector.getFocusGroup());
    }

    /**
     * Evaluates the template.
     *
     * @param template the template
     * @param context  the context
     * @return the result of the evaluation. May be {@code null}
     */
    protected abstract String evaluate(Entity template, Context context);

    /**
     * Returns the selected customer.
     *
     * @return the customer. May be {@code null}
     */
    protected IMObjectReference getCustomer() {
        return customer.getReference();
    }

    /**
     * Returns the selected patient.
     *
     * @return the patient. May be {@code null}
     */
    protected IMObjectReference getPatient() {
        return patient.getReference();
    }

    /**
     * Returns the layout context.
     *
     * @return the layout context
     */
    protected LayoutContext getContext() {
        return layoutContext;
    }

}
