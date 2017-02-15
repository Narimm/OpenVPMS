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

package org.openvpms.web.component.im.edit.reminder;

import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.patient.PatientActEditor;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;

/**
 * An editor for {@link Act}s which have an archetype of <em>act.patientReminder</em>.
 *
 * @author Tim Anderson
 */
public class ReminderEditor extends PatientActEditor {

    /**
     * The reminder rules.
     */
    private final ReminderRules rules;

    /**
     * Determines if matching reminders should be marked completed on save.
     */
    private boolean markCompleted = true;

    /**
     * Constructs a {@link ReminderEditor}.
     *
     * @param act     the reminder act
     * @param parent  the parent. May be {@code null}
     * @param context the layout context
     */
    public ReminderEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
        addStartEndTimeListeners();
        if (!TypeHelper.isA(act, ReminderArchetypes.REMINDER)) {
            throw new IllegalArgumentException(
                    "Invalid act type:" + act.getArchetypeId().getShortName());
        }
        rules = ServiceHelper.getBean(ReminderRules.class);
    }

    /**
     * Sets the created time.
     * <p/>
     * Due dates are calculated relative to this.
     * TODO - this won't work when createdTime is populated by the persistence layer. A separate date will be required
     *
     * @param created the created time
     */
    public void setCreatedTime(Date created) {
        getProperty("createdTime").setValue(created);
    }

    /**
     * Returns the created time.
     *
     * @return the created time. May be {@code null}
     */
    public Date getCreatedTime() {
        return getProperty("createdTime").getDate();
    }

    /**
     * Sets the reminder type.
     *
     * @param reminderType the reminder type. May be {@code null}
     */
    public void setReminderType(Entity reminderType) {
        setParticipant("reminderType", reminderType);
    }

    /**
     * Returns the reminder type.
     *
     * @return the reminder type
     */
    public Entity getReminderType() {
        return (Entity) getParticipant("reminderType");
    }

    /**
     * Sets the product.
     *
     * @param product the product. May be {@code null}
     */
    public void setProduct(Product product) {
        setParticipant("product", product);
    }

    /**
     * Returns the product.
     *
     * @return the product. May be {@code null}
     */
    public Product getProduct() {
        return (Product) getParticipant("product");
    }

    /**
     * Returns the reminder count.
     *
     * @return the reminder count
     */
    public int getReminderCount() {
        return getProperty("reminderCount").getInt();
    }

    /**
     * Determines if matching reminders should be marked completed, if the reminder is new and IN_PROGRESS when it is
     * saved.
     * <p/>
     * Defaults to {@code true}.
     *
     * @param markCompleted if {@code true}, mark matching reminders as completed
     */
    public void setMarkMatchingRemindersCompleted(boolean markCompleted) {
        this.markCompleted = markCompleted;
    }

    /**
     * Invoked when layout has completed. All editors have been created.
     */
    @Override
    protected void onLayoutCompleted() {
        Editor reminderType = getEditor("reminderType");

        if (reminderType != null) {
            // add a listener to update the due date when the reminder type is modified
            ModifiableListener listener = new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    onReminderTypeChanged();
                }
            };
            reminderType.addModifiableListener(listener);
        }
    }

    /**
     * Validates the object.
     * <p/>
     * This extends validation by ensuring that the start time is less than the end time, if non-null.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        Date nextDueDate = getStartTime();
        Date firstDueDate = getEndTime();
        if (firstDueDate != null && nextDueDate != null) {
            if (DateRules.compareTo(firstDueDate, nextDueDate) > 0) {

            }
        }
        return super.doValidation(validator);
    }

    /**
     * Invoked when the start time changes.
     * <p/>
     * For reminders, the start time represents the next due date. It must be the same as or greater than the original
     * due date (endTime).
     */
    @Override
    protected void onStartTimeChanged() {
        Date start = getStartTime();
        Date end = getEndTime();
        if (start != null && end != null) {
            if (start.compareTo(end) < 0) {
                setStartTime(end, true);
            }
        }
    }

    /**
     * Invoked when the end time changes. For reminders, the end represents the original due date.
     * <p/>
     * This populates the start time if with the same value, if it is unset.
     */
    @Override
    protected void onEndTimeChanged() {
        Date start = getStartTime();
        Date end = getEndTime();
        if (end != null || getReminderCount() == 0) {
            setStartTime(end, true);
        }
    }

    /**
     * Save any edits.
     *
     * @throws OpenVPMSException if the save fails
     */
    @Override
    protected void doSave() {
        boolean isNew = getObject().isNew();
        super.doSave();
        if (markCompleted && isNew) {
            rules.markMatchingRemindersCompleted(getObject());
        }
    }

    /**
     * Updates the Due Date based on the reminderType reminder interval.
     */
    private void onReminderTypeChanged() {
        try {
            Date created = getCreatedTime();
            Entity reminderType = getReminderType();
            if (created != null && reminderType != null) {
                Date dueDate = rules.calculateReminderDueDate(created, reminderType);
                setStartTime(dueDate);
                setEndTime(dueDate);
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

}
