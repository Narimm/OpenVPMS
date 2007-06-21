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

package org.openvpms.web.app.workflow.scheduling;

import echopointng.DateChooser;
import echopointng.DateField;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.edit.act.CustomerParticipationEditor;
import org.openvpms.web.component.im.edit.act.ParticipationCollectionEditor;
import org.openvpms.web.component.im.edit.act.PatientParticipationEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ComponentSet;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.TimePropertyTransformer;
import org.openvpms.web.component.util.DateFieldFactory;
import org.openvpms.web.component.util.DateHelper;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.TimeFieldFactory;
import org.openvpms.web.resource.util.Messages;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * An editor for <em>act.customerAppointment</em>s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentActEditor extends AbstractActEditor {

    /**
     * The date component.
     */
    private DateField date;

    /**
     * Listener for date changes.
     */
    private PropertyChangeListener dateListener;

    /**
     * The startTime transformer. Used to set the date component of the start
     * time.
     */
    private TimePropertyTransformer startTimeXform;

    /**
     * The endTime transfomer. Used to set the date component of the end time.
     */
    private TimePropertyTransformer endTimeXform;


    /**
     * Constructs a new <tt>AppointmentActEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context
     */
    public AppointmentActEditor(Act act, IMObject parent,
                                LayoutContext context) {
        super(act, parent, context);
        initParticipant("schedule", context.getContext().getSchedule());

        Entity appointmentType = (Entity) getParticipant("appointmentType");
        if (appointmentType == null) {
            // set the appointment type to the default for the schedule
            Party schedule = (Party) getParticipant("schedule");
            if (schedule != null) {
                appointmentType = getDefaultAppointmentType(schedule);
                setParticipant("appointmentType", appointmentType);
            }
        }

        Date startTime = getStartTime();
        if (startTime == null) {
            Date scheduleDate = context.getContext().getScheduleDate();
            startTime = getDefaultStartTime(scheduleDate);
            setStartTime(startTime);
        }

        date = DateFieldFactory.create();
        dateListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                String name = event.getPropertyName();
                if ("selectedDate".equals(name)) {
                    updateDates();
                }
            }
        };
        setDate(startTime);

        startTimeXform = new TimePropertyTransformer(getProperty("startTime"));
        startTimeXform.setDate(startTime);
        endTimeXform = new TimePropertyTransformer(getProperty("endTime"));
        endTimeXform.setDate(startTime);

        getProperty("status").addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onStatusChanged();
            }
        });
        addStartEndTimeListeners();
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

        Party schedule = (Party) getParticipant("schedule");
        AppointmentTypeParticipationEditor editor = getAppointmentTypeEditor();
        editor.setSchedule(schedule);
        editor.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onAppointmentTypeChanged();
            }
        });

        if (getEndTime() == null) {
            calculateEndTime();
        }
    }

    /**
     * Invoked when the start time changes. Calculates the end time.
     */
    @Override
    protected void onStartTimeChanged() {
        try {
            calculateEndTime();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
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
                    getPatientEditor().getEditor().setObject(null);
                }
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when the appointment type changes. Calculates the end time
     * if the start time is set.
     */
    private void onAppointmentTypeChanged() {
        try {
            calculateEndTime();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when the status changes. Sets the arrivalTime to now if
     * the status is CHECKED_IN.
     */
    private void onStatusChanged() {
        String status = (String) getProperty("status").getValue();
        if (AppointmentStatus.CHECKED_IN.equals(status)) {
            getProperty("arrivalTime").setValue(new Date());
        }
    }

    /**
     * Calculates the default start time of an appointment, using the supplied
     * date and current time.
     * The start time is rounded to the next nearest 'slot-size' interval.
     *
     * @param date the start date
     * @return the start time
     */
    private Date getDefaultStartTime(Date date) {
        int slotSize = 0;
        Party schedule = (Party) getParticipant("schedule");
        if (schedule != null) {
            AppointmentRules rules = new AppointmentRules();
            slotSize = rules.getSlotSize(schedule);
        }

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        Calendar timeCal = new GregorianCalendar();
        timeCal.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (slotSize != 0) {
            int mins = calendar.get(Calendar.MINUTE);
            mins = ((mins / slotSize) * slotSize) + slotSize;
            calendar.set(Calendar.MINUTE, mins);
        }
        return calendar.getTime();
    }

    /**
     * Calculates the end time if the start time and appointment type are set.
     *
     * @throws OpenVPMSException for any error
     */
    private void calculateEndTime() {
        Date start = getStartTime();
        Party schedule = (Party) getParticipant("schedule");
        AppointmentTypeParticipationEditor editor
                = getAppointmentTypeEditor();
        Entity appointmentType = editor.getEntity();
        if (start != null && schedule != null && appointmentType != null) {
            AppointmentRules rules = new AppointmentRules();
            Date end = rules.calculateEndTime(start, schedule, appointmentType);
            setEndTime(end);
        }
    }

    /**
     * Sets the appointment date.
     *
     * @param selected the appointment date
     */
    private void setDate(Date selected) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selected);
        DateChooser chooser = date.getDateChooser();
        chooser.removePropertyChangeListener(dateListener);
        chooser.setSelectedDate(calendar);
        chooser.addPropertyChangeListener(dateListener);
    }

    /**
     * Synchronizes the date portion of the <em>startTime</em> and
     * <em>endTime</em> nodes with the {@link #date} field.
     */
    private void updateDates() {
        Date now = DateHelper.getDayMonthYear(new Date());
        Date selected = DateHelper.getDayMonthYear(
                date.getSelectedDate().getTime());
        if (selected.compareTo(now) < 0) {
            // don't permit backdating of appointments
            selected = now;
            setDate(selected);
        }
        startTimeXform.setDate(selected);
        endTimeXform.setDate(selected);
        removeStartEndTimeListeners();
        Property startTime = getProperty("startTime");
        startTime.setValue(startTime.getValue());

        Property endTime = getProperty("endTime");
        endTime.setValue(endTime.getValue());
        addStartEndTimeListeners();
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
     * Returns the appointment type editor.
     *
     * @return the appointment type editor
     */
    private AppointmentTypeParticipationEditor getAppointmentTypeEditor() {
        return (AppointmentTypeParticipationEditor) getEditor(
                "appointmentType");
    }

    /**
     * Returns the default appointment type associated with a schedule.
     *
     * @param schedule the schedule
     * @return a default appointment associated with <code>schedule</code>,
     *         or <code>null</code> if there is no default appointment type
     */
    private Entity getDefaultAppointmentType(Party schedule) {
        Entity type = null;
        EntityBean bean = new EntityBean(schedule);
        List<IMObject> relationships = bean.getValues("appointmentTypes");
        for (IMObject object : relationships) {
            EntityRelationship relationship = (EntityRelationship) object;
            IMObjectBean relBean = new IMObjectBean(relationship);
            if (relBean.getBoolean("default")) {
                type = (Entity) IMObjectHelper.getObject(
                        relationship.getTarget());
                if (type != null) {
                    break;
                }
            }
        }
        return type;
    }

    private class LayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Creates a set of components to be rendered from the supplied descriptors.
         *
         * @param object      the parent object
         * @param descriptors the property descriptors
         * @param properties  the properties
         * @param context     the layout context
         * @return the components
         */
        @Override
        protected ComponentSet createComponentSet(IMObject object,
                                                  List<NodeDescriptor> descriptors,
                                                  PropertySet properties,
                                                  LayoutContext context) {
            ComponentSet result = new ComponentSet();
            for (NodeDescriptor descriptor : descriptors) {
                Property property = properties.get(descriptor);
                String name = property.getName();
                if (name.equals("startTime")) {
                    // insert the date component prior to the start time
                    ComponentState dateComp = new ComponentState(date);
                    result.add(dateComp, Messages.get("appointment.date"));
                }
                ComponentState component = createComponent(property, object,
                                                           context);
                result.add(component, descriptor.getDisplayName());
            }
            return result;
        }

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
            if (name.equals("startTime")) {
                property.setTransformer(startTimeXform);
                result = new ComponentState(TimeFieldFactory.create(property),
                                            property);
            } else if (name.equals("endTime")) {
                property.setTransformer(endTimeXform);
                result = new ComponentState(TimeFieldFactory.create(property),
                                            property);
            } else {
                result = super.createComponent(property, parent, context);
            }
            return result;
        }
    }
}
