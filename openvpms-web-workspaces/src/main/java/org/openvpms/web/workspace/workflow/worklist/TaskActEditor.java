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
 */

package org.openvpms.web.workspace.workflow.worklist;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.workflow.scheduling.AbstractScheduleActEditor;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.openvpms.archetype.rules.act.ActStatus.COMPLETED;
import static org.openvpms.archetype.rules.act.FinancialActStatus.CANCELLED;


/**
 * An editor for <em>act.customerTask</em>s.
 *
 * @author Tim Anderson
 */
public class TaskActEditor extends AbstractScheduleActEditor {

    /**
     * Construct a new <tt>TaskActEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context
     */
    public TaskActEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
        initParticipant("customer", context.getContext().getCustomer());
        initParticipant("patient", context.getContext().getPatient());
        initParticipant("worklist", context.getContext().getWorkList());

        if (getStartTime() == null) {
            Date date = context.getContext().getWorkListDate();
            if (date != null) {
                setStartTime(getDefaultStartTime(date), true);
            }
        }

        addStartEndTimeListeners();

        getProperty("status").addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onStatusChanged();
            }
        });
    }

    /**
     * Sets the work list.
     *
     * @param workList the work list
     */
    public void setWorkList(Party workList) {
        setParticipant("worklist", workList);
        TaskTypeParticipationEditor editor = getTaskTypeEditor();
        editor.setWorkList(workList);
    }

    /**
     * Returns the work list.
     *
     * @return the work list. May be <tt>null</tt>
     */
    public Party getWorkList() {
        return (Party) getParticipant("worklist");
    }

    /**
     * Sets the task type.
     *
     * @param taskType the task type
     */
    public void setTaskType(Entity taskType) {
        getTaskTypeEditor().setEntity(taskType);
    }

    /**
     * Returns the task type.
     *
     * @return the task type
     */
    public Entity getTaskType() {
        return (Entity) getParticipant("taskType");
    }

    /**
     * Save any edits.
     *
     * @return <tt>true</tt> if the save was successful
     */
    @Override
    public boolean save() {
        return checkMaxSlots() && super.save();
    }

    /**
     * Validates the object.
     * <p/>
     * This extends validation by ensuring that there are not too many tasks.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        boolean result = super.doValidation(validator);
        if (result) {
            result = checkMaxSlots();
        }
        return result;
    }

    /**
     * Invoked when layout has completed. All editors have been created.
     */
    @Override
    protected void onLayoutCompleted() {
        super.onLayoutCompleted();

        Party workList = (Party) getParticipant("worklist");
        TaskTypeParticipationEditor editor = getTaskTypeEditor();
        editor.setWorkList(workList);
    }

    /**
     * Returns the task type editor.
     *
     * @return the task type editor
     */
    private TaskTypeParticipationEditor getTaskTypeEditor() {
        return (TaskTypeParticipationEditor) getEditor("taskType");
    }

    /**
     * Invoked when the status changes. Sets the end time to today if the
     * status is 'Completed' or 'Cancelled', or <tt>null</tt> if it is
     * 'Pending'
     */
    private void onStatusChanged() {
        Property status = getProperty("status");
        Date time = null;
        String value = (String) status.getValue();
        if (COMPLETED.equals(value) || CANCELLED.equals(value)) {
            time = new Date();
        }
        setEndTime(time, false);
    }

    /**
     * Determines if there are enough slots available to save the task.
     *
     * @return {@code true} if there are less than maxSlots tasks, otherwise {@code false}
     */
    private boolean checkMaxSlots() {
        boolean result;
        Act act = (Act) getObject();
        if (TaskQueryHelper.tooManyTasks(act)) {
            String title = Messages.get("workflow.worklist.toomanytasks.title");
            String message = Messages.get("workflow.worklist.toomanytasks.message");
            ErrorDialog.show(title, message);
            result = false;
        } else {
            result = true;
        }
        return result;
    }

    /**
     * Calculates the default start time of a task, using the supplied date
     * and current time.
     *
     * @param date the start date
     * @return the start time
     */
    private Date getDefaultStartTime(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        Calendar timeCal = new GregorianCalendar();
        timeCal.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

}
