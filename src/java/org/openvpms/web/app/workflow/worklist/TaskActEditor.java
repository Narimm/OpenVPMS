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

import nextapp.echo2.app.Component;
import static org.openvpms.archetype.rules.act.ActStatus.COMPLETED;
import static org.openvpms.archetype.rules.act.FinancialActStatus.CANCELLED;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.edit.Modifiable;
import org.openvpms.web.component.edit.ModifiableListener;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.edit.act.CustomerParticipationEditor;
import org.openvpms.web.component.im.edit.act.ParticipationCollectionEditor;
import org.openvpms.web.component.im.edit.act.PatientParticipationEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.util.DateTimeFieldFactory;
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

        Property startTime = getProperty("startTime");
        if (startTime.getValue() == null) {
            Date date = context.getContext().getWorkListDate();
            startTime.setValue(getDefaultStartTime(date));
        }
        startTime.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onStartTimeChanged();
            }
        });

        Property endTime = getProperty("endTime");
        endTime.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onEndTimeChanged();
            }
        });

        getProperty("status").addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onStatusChanged();
            }
        });
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
            Party customer = (Party) getCustomerEditor().getEntity();
            Party patient = (Party) getPatientEditor().getEntity();
            PatientRules rules = new PatientRules();
            if (customer != null && patient != null) {
                if (!rules.isOwner(customer, patient)) {
                    getPatientEditor().getEditor().setObject(null);
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
        Property endTime = getProperty("endTime");
        String value = (String) status.getValue();
        if (COMPLETED.equals(value) || CANCELLED.equals(value)) {
            endTime.setValue(new Date());
        } else {
            endTime.setValue(null);
        }
    }

    /**
     * Invoked when the start time changes. Sets the value to end time if
     * start time > end time.
     */
    private void onStartTimeChanged() {
        Date start = getStartTime();
        Date end = getEndTime();
        if (start != null && end != null) {
            if (start.compareTo(end) > 0) {
                getProperty("startTime").setValue(end);
            }
        }
    }

    /**
     * Invoked when the end time changes. Sets the value to start time if
     * end time < start time.
     */
    private void onEndTimeChanged() {
        Date start = getStartTime();
        Date end = getEndTime();
        if (start != null && end != null) {
            if (end.compareTo(start) < 0) {
                getProperty("endTime").setValue(start);
            }
        }
    }

    /**
     * Returns the start time.
     *
     * @return the start time. May be <code>null</code>
     */
    private Date getStartTime() {
        Property property = getProperty("startTime");
        Object value = property.getValue();
        return (value instanceof Date) ? (Date) value : null;
    }

    /**
     * Returns the end time.
     *
     * @return the end time. May be <code>null</code>
     */
    private Date getEndTime() {
        Property property = getProperty("endTime");
        Object value = property.getValue();
        return (value instanceof Date) ? (Date) value : null;
    }

    /**
     * Determines if the task overlaps an existing appointment.
     * If so, and double scheduling is allowed, a confirmation dialog is shown
     * prompting to save or continue editing. If double scheduling is not
     * allowed, an error dialog is shown and no save is performed.
     *
     * @return <code>true</code> if there are less than maxSlots tasks, otherwise
     *         <code>false</code>
     */
    private boolean checkMaxSlots() {
        IMObject object = getObject();
        boolean result = true;
        if (isValid()) {
            Act act = (Act) object;
            if (tooManyTasks(act)) {
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
     * Determines there are too many outstanding tasks.
     *
     * @return <code>true</code> if there are too many outstanding tasks;
     *         otherwise <code>false</code>
     */
    private boolean tooManyTasks(Act act) {
        boolean result = false;
        ActBean actBean = new ActBean(act);
        Party workList = (Party) actBean.getParticipant(
                "participation.worklist");
        if (workList != null) {
            IMObjectBean bean = new IMObjectBean(workList);
            int maxSlots = bean.getInt("maxSlots");
            IArchetypeService service
                    = ArchetypeServiceHelper.getArchetypeService();
            ArchetypeQuery query
                    = new ArchetypeQuery("act.customerTask", false, true);
            query.add(new ParticipantConstraint("worklist",
                                                "participation.worklist",
                                                workList));
            query.add(new NodeConstraint("uid", RelationalOp.NE, act.getUid()));
            query.add(
                    new NodeConstraint("status", RelationalOp.NE, CANCELLED));
            query.setFirstResult(0);
            query.setMaxResults(1);
            query.setCountResults(true);
            IPage<IMObject> page = service.get(query);
            if (page.getTotalResults() >= maxSlots) {
                result = true;
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
        protected Component createComponent(Property property, IMObject parent,
                                            LayoutContext context) {
            Component component;
            String name = property.getDescriptor().getName();
            if (name.equals("startTime") || name.equals("endTime")) {
                component = DateTimeFieldFactory.create(property);
            } else {
                component = super.createComponent(property, parent, context);
            }
            return component;
        }
    }

}
