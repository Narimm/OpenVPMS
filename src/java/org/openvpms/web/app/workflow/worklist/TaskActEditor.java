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

package org.openvpms.web.app.workflow.worklist;

import static org.openvpms.archetype.rules.act.ActStatus.COMPLETED;
import static org.openvpms.archetype.rules.act.FinancialActStatus.CANCELLED;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.bound.BoundDateTimeField;
import org.openvpms.web.component.dialog.ErrorDialog;
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
import org.openvpms.web.component.util.DateTimeFieldFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * An editor for <em>act.customerTask</em>s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TaskActEditor extends AbstractActEditor {

    /**
     * Construct a new <code>TaskActEditor</code>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context
     */
    public TaskActEditor(Act act, IMObject parent,
                         LayoutContext context) {
        super(act, parent, context);
        initParticipant("customer", context.getContext().getCustomer());
        initParticipant("patient", context.getContext().getPatient());
        initParticipant("worklist", context.getContext().getWorkList());

        if (getStartTime() == null) {
            Date date = context.getContext().getWorkListDate();
            setStartTime(getDefaultStartTime(date));
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
     * Save any edits.
     *
     * @return <code>true</code> if the save was successful
     */
    @Override
    public boolean save() {
        if (checkMaxSlots()) {
            return super.save();
        }
        return false;
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
     */
    @Override
    protected void onLayoutCompleted() {
        getCustomerEditor().addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onCustomerChanged();
            }
        });

        Party workList = (Party) getParticipant("worklist");
        TaskTypeParticipationEditor editor = getTaskTypeEditor();
        editor.setWorkList(workList);
    }


    /**
     * Returns the customer editor.
     *
     * @return the customer editor
     */
    private CustomerParticipationEditor getCustomerEditor() {
        return (CustomerParticipationEditor) getEditor("customer");
    }

    /**
     * Returns the patient editor.
     *
     * @return the patient editor
     */
    private PatientParticipationEditor getPatientEditor() {
        ParticipationCollectionEditor editor = (ParticipationCollectionEditor)
                getEditor("patient");
        return (PatientParticipationEditor) editor.getCurrentEditor();
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
     * Invoked when the customer changes. Sets the patient to null if no
     * relationship exists between the two..
     */
    private void onCustomerChanged() {
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
     * Invoked when the status changes. Sets the end time to today if the
     * status is 'Completed' or 'Cancelled', or <code>null</code> if it is
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
     * @return <code>true</code> if there are less than maxSlots tasks,
     *         otherwise <code>false</code>
     */
    private boolean checkMaxSlots() {
        IMObject object = getObject();
        boolean result = true;
        if (isValid()) {
            Act act = (Act) object;
            if (TaskQueryHelper.tooManyTasks(act)) {
                String title = Messages.get(
                        "workflow.worklist.toomanytasks.title");
                String message = Messages.get(
                        "workflow.worklist.toomanytasks.message");
                ErrorDialog.show(title, message);
                result = false;
            }
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

    private class LayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Creates a component for a property. This maintains a cache of created
         * components, in order for the focus to be set on an appropriate
         * component.
         *
         * @param property the property
         * @param parent   the parent object
         * @param context  the layout context
         * @return a component to display <code>property</code>
         */
        @Override
        protected ComponentState createComponent(Property property,
                                                 IMObject parent,
                                                 LayoutContext context) {
            ComponentState result;
            String name = property.getName();
            if (name.equals("startTime") || name.equals("endTime")) {
                BoundDateTimeField field = DateTimeFieldFactory.create(
                        property);
                result = new ComponentState(field.getComponent(), property,
                                            field.getFocusGroup());
            } else {
                result = super.createComponent(property, parent, context);
            }
            return result;
        }
    }

}
