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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.template;

import nextapp.echo2.app.Label;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.DefaultIMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.reporting.reminder.ReminderSMSEvaluator;

/**
 * A component to test the expression evaluation of an <em>entity.documentTemplateSMSAppointment</em>.
 *
 * @author Tim Anderson
 */
public class SMSReminderTemplateSampler extends SMSTemplateSampler {

    /**
     * The reminder type to test against.
     */
    private final SimpleProperty reminderType = new SimpleProperty(
            "reminderType", null, IMObjectReference.class,
            DescriptorHelper.getDisplayName(ReminderArchetypes.REMINDER, "reminderType"));

    /**
     * The template evaluator.
     */
    private ReminderSMSEvaluator evaluator;

    /**
     * Constructs an {@link SMSReminderTemplateSampler}.
     *
     * @param template      the template
     * @param layoutContext the layout context
     */
    public SMSReminderTemplateSampler(Entity template, LayoutContext layoutContext) {
        super(template, layoutContext);
        evaluator = ServiceHelper.getBean(ReminderSMSEvaluator.class);
        reminderType.setArchetypeRange(new String[]{ReminderArchetypes.REMINDER_TYPE});
        ModifiableListener listener = new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                evaluate();
            }
        };
        reminderType.addModifiableListener(listener);
    }

    /**
     * Evaluates the template.
     *
     * @param template the template
     * @param context  the context
     * @return the result of the evaluation. May be {@code null}
     */
    @Override
    protected String evaluate(Entity template, Context context) {
        String result = null;
        Party customer = (Party) IMObjectHelper.getObject(getCustomer(), getContext().getContext());
        Party patient = (Party) IMObjectHelper.getObject(getPatient(), getContext().getContext());
        Entity type = getReminderType();
        if (customer != null && patient != null && type != null) {
            Act reminder = (Act) IMObjectCreator.create(ReminderArchetypes.REMINDER);
            ActBean bean = new ActBean(reminder);
            bean.setNodeParticipant("patient", patient);
            bean.setNodeParticipant("reminderType", type);
            result = evaluator.evaluate(template, reminder, customer, patient, context.getLocation(),
                                        context.getPractice());
        }
        return result;
    }

    /**
     * Lays out the editable fields in a grid.
     *
     * @param grid  the grid
     * @param group the focus group
     */
    @Override
    protected void layoutFields(ComponentGrid grid, FocusGroup group) {
        super.layoutFields(grid, group);
        IMObjectReferenceEditor selector = new DefaultIMObjectReferenceEditor(reminderType, null, getContext());
        Label label = LabelFactory.create();
        label.setText(reminderType.getDisplayName());
        grid.add(label, selector.getComponent());
        group.add(selector.getFocusGroup());
    }

    /**
     * Returns the reminder type.
     *
     * @return the reminder type. May be {@code nul}
     */
    protected Entity getReminderType() {
        return (Entity) IMObjectHelper.getObject(reminderType.getReference());
    }

}
