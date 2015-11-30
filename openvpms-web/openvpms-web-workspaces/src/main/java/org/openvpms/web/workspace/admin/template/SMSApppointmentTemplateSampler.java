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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.template;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.customer.CustomerReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.PatientReferenceEditor;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.sms.BoundCountedTextArea;
import org.openvpms.web.component.im.sms.SMSEditor;
import org.openvpms.web.component.im.util.IMObjectCreator;
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
import org.openvpms.web.workspace.workflow.appointment.reminder.AppointmentReminderEvaluator;

import java.util.Date;

import static org.openvpms.archetype.rules.workflow.ScheduleArchetypes.APPOINTMENT;

/**
 * A component to test the expression evaluation of an <em>entity.documentTemplateSMSAppointment</em>.
 *
 * @author Tim Anderson
 */
public class SMSApppointmentTemplateSampler {

    /**
     * The template to use.
     */
    private Entity template;

    /**
     * The property to hold the generated message.
     */
    private final SimpleProperty messageProperty = new SimpleProperty("message", null, String.class,
                                                                      Messages.get("sms.message"));

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
     * The focus group.
     */
    private FocusGroup group = new FocusGroup("Sampler");

    /**
     * The evaluation status.
     */
    private Label status;

    /**
     * Constructs an {@link SMSApppointmentTemplateSampler}.
     *
     * @param layoutContext the layout context
     */
    public SMSApppointmentTemplateSampler(LayoutContext layoutContext) {
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
     * Sets the template.
     *
     * @param template the template. An instance of <em>entity.documentTemplateSMSAppointmentReminder</em>
     */
    public void setTemplate(Entity template) {
        this.template = template;
    }

    /**
     * Evaluates the template against the selected customer and patient.
     */
    public void evaluate() {
        String value;
        Act act = (Act) IMObjectCreator.create(APPOINTMENT);
        ActBean bean = new ActBean(act);
        bean.setNodeParticipant("customer", customer.getReference());
        bean.setNodeParticipant("patient", patient.getReference());
        act.setActivityStartTime(new Date());
        act.setActivityEndTime(new Date());
        AppointmentReminderEvaluator evaluator = ServiceHelper.getBean(AppointmentReminderEvaluator.class);
        try {
            value = evaluator.evaluate(template, act, layoutContext.getContext().getLocation(),
                                       layoutContext.getContext().getPractice());
            if (value != null && value.length() > SMSEditor.MAX_LENGTH) {
                value = value.substring(0, SMSEditor.MAX_LENGTH);
                status.setText(Messages.get("sms.truncated"));
            } else {
                status.setText(null);
            }
        } catch (Throwable exception) {
            value = null;
            status.setText(exception.getMessage());
        }
        messageProperty.setValue(value);
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        IMObjectReferenceEditor customerSelector = new CustomerReferenceEditor(customer, null, layoutContext);
        IMObjectReferenceEditor patientSelector = new PatientReferenceEditor(patient, null, layoutContext);
        BoundCountedTextArea message = new BoundCountedTextArea(messageProperty, 40, 15);
        message.setMaximumLength(SMSEditor.MAX_LENGTH);
        message.setStyleName(Styles.DEFAULT);
        message.setEnabled(false);

        Label customerLabel = LabelFactory.create();
        customerLabel.setText(customer.getDisplayName());

        Label patientLabel = LabelFactory.create();
        patientLabel.setText(patient.getDisplayName());

        Label messageLabel = LabelFactory.create();
        messageLabel.setText(messageProperty.getDisplayName());

        ComponentGrid grid = new ComponentGrid();
        grid.add(customerLabel, customerSelector.getComponent(), patientLabel, patientSelector.getComponent());
        grid.add(LabelFactory.create("sms.title", Styles.BOLD), LabelFactory.create(),
                 LabelFactory.create("sms.appointment.status", Styles.BOLD));
        status.setTextAlignment(Alignment.ALIGN_TOP);
        status.setLayoutData(ComponentGrid.layout(new Alignment(Alignment.LEFT, Alignment.TOP)));
        grid.add(LabelFactory.create("sms.message"), RowFactory.create(message));
        grid.set(2, 2, 2, status);
        group.add(customerSelector.getFocusGroup());
        group.add(patientSelector.getFocusGroup());
        group.add(message);
        return ColumnFactory.create(Styles.INSET, grid.createGrid());
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return group;
    }
}
