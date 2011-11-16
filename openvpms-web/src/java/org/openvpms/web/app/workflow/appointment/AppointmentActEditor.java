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

package org.openvpms.web.app.workflow.appointment;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.alert.AlertSummary;
import org.openvpms.web.app.customer.CustomerSummary;
import org.openvpms.web.app.patient.summary.PatientSummary;
import org.openvpms.web.app.workflow.scheduling.AbstractScheduleActEditor;
import org.openvpms.web.component.im.edit.act.PatientParticipationEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.app.Context;

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
public class AppointmentActEditor extends AbstractScheduleActEditor {

    /**
     * The alerts row.
     */
    private Row alerts;

    /**
     * Listener notified when the patient changes.
     */
    private ModifiableListener patientListener;


    /**
     * Constructs an <tt>AppointmentActEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context
     */
    public AppointmentActEditor(Act act, IMObject parent, LayoutContext context) {
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
            if (scheduleDate != null) {
                startTime = getDefaultStartTime(scheduleDate);

                setStartTime(startTime, true);
            }
        }

        getProperty("status").addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onStatusChanged();
            }
        });
        addStartEndTimeListeners();
        updateAlerts();
    }

    /**
     * Sets the schedule.
     *
     * @param schedule the schedule
     */
    public void setSchedule(Entity schedule) {
        setParticipant("schedule", schedule);
        AppointmentTypeParticipationEditor editor = getAppointmentTypeEditor();
        editor.setSchedule(schedule);
        calculateEndTime();
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new AppointmentLayoutStrategy();
    }

    /**
     * Invoked when layout has completed. All editors have been created.
     */
    @Override
    protected void onLayoutCompleted() {
        super.onLayoutCompleted();

        Party schedule = (Party) getParticipant("schedule");
        AppointmentTypeParticipationEditor editor = getAppointmentTypeEditor();
        editor.setSchedule(schedule);
        editor.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onAppointmentTypeChanged();
            }
        });
        getPatientEditor().addModifiableListener(getPatientListener());

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
     * Invoked when the end time changes. Recalculates the end time if it
     * is less than the start time.
     */
    @Override
    protected void onEndTimeChanged() {
        Date start = getStartTime();
        Date end = getEndTime();
        if (start != null && end != null) {
            if (end.compareTo(start) < 0) {
                calculateEndTime();
            }
        }
    }

    /**
     * Invoked when the customer changes. Sets the patient to null if no
     * relationship exists between the two.
     * <p/>
     * The alerts will be updated.
     */
    @Override
    protected void onCustomerChanged() {
        PatientParticipationEditor editor = getPatientEditor();
        editor.removeModifiableListener(patientListener);
        super.onCustomerChanged();
        editor.addModifiableListener(patientListener);
        updateAlerts();
    }

    /**
     * Invoked when the patient changes. This updates the alerts.
     */
    private void onPatientChanged() {
        updateAlerts();
    }

    /**
     * Updates the alerts associated with the custopmer and patient.
     */
    private void updateAlerts() {
        Component container = getAlertsContainer();
        container.removeAll();
        Component alerts = createAlerts();
        if (alerts != null) {
            container.add(alerts);
        }
    }

    /**
     * Creates a component representing the customerr and patient alerts.
     *
     * @return the alerts component or <tt>null</tt> if neither has alerts
     */
    private Component createAlerts() {
        Component result = null;
        Component customerSummary = null;
        Component patientSummary = null;
        Party customer = getCustomer();
        Party patient = getPatient();
        if (customer != null) {
            customerSummary = getCustomerAlerts(customer);
        }
        if (patient != null) {
            patientSummary = getPatientAlerts(patient);
        }
        if (customerSummary != null || patientSummary != null) {
            result = RowFactory.create("CellSpacing");
            if (customerSummary != null) {
                result.add(customerSummary);
            }
            if (patientSummary != null) {
                result.add(patientSummary);
//                RowLayoutData layout = new RowLayoutData();
//                layout.setAlignment(Alignment.ALIGN_TOP);
//                patientSummary.setLayoutData(layout);
            }
            result = RowFactory.create("Inset", result);
        }
        return result;
    }

    /**
     * Returns any alerts associated with the customer.
     *
     * @param customer the customer
     * @return any alerts associated with the customer, or <tt>null</tt> if the customer has no alerts
     */
    private Component getCustomerAlerts(Party customer) {
        Component result = null;
        Context context = getLayoutContext().getContext();
        AlertSummary alerts = new CustomerSummary(context).getAlertSummary(customer);
        if (alerts != null) {
            result = ColumnFactory.create("AppointmentActEditor.Alerts", LabelFactory.create("alerts.customer", "bold"),
                                          alerts.getComponent());
        }
        return result;
    }

    /**
     * Returns any alerts associated with the patient.
     *
     * @param patient the patient
     * @return any alerts associated with the patient, or <tt>null</tt> if the patient has no alerts
     */
    private Component getPatientAlerts(Party patient) {
        Component result = null;
        AlertSummary alerts = new PatientSummary().getAlertSummary(patient);
        if (alerts != null) {
            result = ColumnFactory.create("AppointmentActEditor.Alerts", LabelFactory.create("alerts.patient", "bold"),
                                          alerts.getComponent());
        }
        return result;
    }

    /**
     * Returns the patient listener.
     *
     * @return the patient listener
     */
    private ModifiableListener getPatientListener() {
        if (patientListener == null) {
            patientListener = new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    onPatientChanged();
                }
            };
        }
        return patientListener;
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
     * @return the default appointment type, or the the first appointment type
     *         if there is no default, or <tt>null</tt> if none is found
     */
    private Entity getDefaultAppointmentType(Party schedule) {
        return new AppointmentRules().getDefaultAppointmentType(schedule);
    }

    /**
     * Returns the alerts container.
     *
     * @return the alerts container
     */
    private Component getAlertsContainer() {
        if (alerts == null) {
            alerts = new Row();
        }
        return alerts;
    }

    private class AppointmentLayoutStrategy extends LayoutStrategy {

        /**
         * Lay out out the object in the specified container.
         *
         * @param object     the object to lay out
         * @param properties the object's properties
         * @param parent     the parent object. May be <tt>null</tt>
         * @param container  the container to use
         * @param context    the layout context
         */
        @Override
        protected void doLayout(IMObject object, PropertySet properties, IMObject parent, Component container,
                                LayoutContext context) {
            super.doLayout(object, properties, parent, container, context);
            container.add(getAlertsContainer());
        }

        /**
         * Returns the default focus component.
         * <p/>
         * This implementation returns the customer component.
         *
         * @param components the components
         * @return the customer component, or <tt>null</tt> if none is found
         */
        @Override
        protected Component getDefaultFocus(List<ComponentState> components) {
            for (ComponentState state : components) {
                Property property = state.getProperty();
                if (property != null && "customer".equals(property.getName())) {
                    return state.getFocusable();
                }
            }
            return null;
        }
    }
}
