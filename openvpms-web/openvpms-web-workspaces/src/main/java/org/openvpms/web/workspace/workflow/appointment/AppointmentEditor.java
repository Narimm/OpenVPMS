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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import org.joda.time.Period;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.bound.BoundCheckBox;
import org.openvpms.web.component.bound.BoundDateTimeField;
import org.openvpms.web.component.bound.BoundDateTimeFieldFactory;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.ComponentSet;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.patient.PatientParticipationEditor;
import org.openvpms.web.component.im.sms.SMSHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.alert.AlertSummary;
import org.openvpms.web.workspace.customer.CustomerSummary;
import org.openvpms.web.workspace.patient.summary.CustomerPatientSummaryFactory;
import org.openvpms.web.workspace.workflow.appointment.repeat.AppointmentSeries;
import org.openvpms.web.workspace.workflow.appointment.repeat.CalendarEventSeries;

import java.util.Date;

import static org.openvpms.archetype.rules.workflow.ScheduleEvent.REMINDER_ERROR;
import static org.openvpms.archetype.rules.workflow.ScheduleEvent.REMINDER_SENT;
import static org.openvpms.archetype.rules.workflow.ScheduleEvent.SEND_REMINDER;
import static org.openvpms.web.echo.style.Styles.BOLD;
import static org.openvpms.web.echo.style.Styles.CELL_SPACING;
import static org.openvpms.web.echo.style.Styles.INSET;


/**
 * An editor for <em>act.customerAppointment</em>s.
 *
 * @author Tim Anderson
 */
public class AppointmentEditor extends CalendarEventEditor {

    /**
     * The appointment rules.
     */
    private final AppointmentRules rules;

    /**
     * The alerts row.
     */
    private Row alerts;

    /**
     * Listener notified when the patient changes.
     */
    private ModifiableListener patientListener;

    /**
     * Determines if SMS is enabled at the practice.
     */
    private boolean smsPractice;

    /**
     * Determines if reminders are enabled on the schedule.
     */
    private boolean scheduleReminders;


    /**
     * Determines if reminders are enabled on the appointment type.
     */
    private boolean appointmentTypeReminders;

    /**
     * Determines if the sendReminder checkbox should be enabled.
     */
    private Period noReminder;

    /**
     * The send reminder flag.
     */
    private BoundCheckBox sendReminder;

    /**
     * The online booking notes.
     */
    private static final String BOOKING_NOTES = ScheduleEvent.BOOKING_NOTES;

    /**
     * Constructs an {@link AppointmentEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public AppointmentEditor(Act act, IMObject parent, LayoutContext context) {
        this(act, parent, false, context);
    }

    /**
     * Constructs an {@link AppointmentEditor}.
     *
     * @param act        the act to edit
     * @param parent     the parent object. May be {@code null}
     * @param editSeries if {@code true}, edit the series
     * @param context    the layout context
     */
    public AppointmentEditor(Act act, IMObject parent, boolean editSeries, LayoutContext context) {
        super(act, parent, editSeries, context);
        rules = ServiceHelper.getBean(AppointmentRules.class);
        if (act.isNew()) {
            initParticipant("customer", context.getContext().getCustomer());
        }

        Entity appointmentType = (Entity) getParticipant("appointmentType");
        Entity schedule = getSchedule();
        smsPractice = SMSHelper.isSMSEnabled(getLayoutContext().getContext().getPractice());
        scheduleReminders = rules.isRemindersEnabled(schedule);
        noReminder = rules.getNoReminderPeriod();
        getSeries().setNoReminderPeriod(noReminder);

        if (appointmentType == null) {
            // set the appointment type to the default for the schedule
            if (schedule != null) {
                appointmentType = getDefaultAppointmentType(schedule);
                setParticipant("appointmentType", appointmentType);
            }
        }
        appointmentTypeReminders = rules.isRemindersEnabled(appointmentType);

        getProperty("status").addModifiableListener(
                modifiable -> onStatusChanged());
        sendReminder = new BoundCheckBox(getProperty(SEND_REMINDER));
        addStartEndTimeListeners();
        if (act.isNew()) {
            updateSendReminder(true);
        }
    }

    /**
     * Returns the appointment type.
     *
     * @return the appointment type. May be {@code null}
     */
    public Entity getAppointmentType() {
        return (Entity) getParticipant("appointmentType");
    }

    /**
     * Returns the clinician.
     *
     * @return the clinician. May be {@code null}
     */
    public User getClinician() {
        return (User) getParticipant("clinician");
    }

    /**
     * Creates a new instance of the editor, with the latest instance of the object to edit.
     *
     * @return {@code null}
     */
    @Override
    public IMObjectEditor newInstance() {
        boolean editSeries = getSeriesEditor() != null;
        return new AppointmentEditor(reload(getObject()), getParent(), editSeries, getLayoutContext());
    }

    /**
     * Returns the event series.
     *
     * @return the series
     */
    @Override
    public AppointmentSeries getSeries() {
        return (AppointmentSeries) super.getSeries();
    }

    /**
     * Creates a new event series.
     *
     * @return a new event series
     */
    @Override
    protected CalendarEventSeries createSeries() {
        return new AppointmentSeries(getObject(), ServiceHelper.getArchetypeService());
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
        Entity schedule = getSchedule();
        initSchedule(schedule);
        getAppointmentTypeEditor().addModifiableListener(
                modifiable -> onAppointmentTypeChanged());
        getPatientEditor().addModifiableListener(getPatientListener());

        if (getEndTime() == null) {
            calculateEndTime();
        }
        updateAlerts();
    }

    /**
     * Invoked when the start time changes. Calculates the end time.
     */
    @Override
    protected void onStartTimeChanged() {
        super.onStartTimeChanged();
        updateSendReminder(sendReminder.isSelected());
    }

    /**
     * Invoked when the customer changes. Sets the patient to null if no relationship exists between the two.
     * <p>
     * The alerts will be updated.
     */
    @Override
    protected void onCustomerChanged() {
        PatientParticipationEditor editor = getPatientEditor();
        editor.removeModifiableListener(patientListener);
        super.onCustomerChanged();
        editor.addModifiableListener(patientListener);
        updateSendReminder(true);
        getProperty(REMINDER_SENT).setValue(null);
        getProperty(REMINDER_ERROR).setValue(null);
        updateAlerts();
    }

    /**
     * Calculates the end time if the start time and appointment type are set.
     */
    protected void calculateEndTime() {
        Date start = getStartTime();
        Entity schedule = getSchedule();
        AppointmentTypeParticipationEditor editor = getAppointmentTypeEditor();
        Entity appointmentType = editor.getEntity();
        if (start != null && schedule != null && appointmentType != null) {
            Date end = rules.calculateEndTime(start, schedule, appointmentType);
            setEndTime(end);
        }
    }

    /**
     * Invoked when the schedule is updated. This propagates it to the appointment type editor, and gets the new slot
     * size.
     *
     * @param schedule the schedule. May be {@code null}
     */
    protected void onScheduleChanged(Entity schedule) {
        super.onScheduleChanged(schedule);
        updateSendReminder(sendReminder.isSelected());
    }

    /**
     * Initialises the appointment type editor with the schedule, updates the slot size, and determines if schedule
     * reminders are enabled.
     *
     * @param schedule the schedule. May be {@code null}
     */
    @Override
    protected void initSchedule(Entity schedule) {
        AppointmentTypeParticipationEditor editor = getAppointmentTypeEditor();
        editor.setSchedule(schedule);
        scheduleReminders = schedule != null && rules.isRemindersEnabled(schedule);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        return validateCustomer(validator) && super.doValidation(validator);
    }

    /**
     * Ensures a customer is selected.
     *
     * @param validator the validator
     * @return {@code true} if a customer is selected
     */
    private boolean validateCustomer(Validator validator) {
        boolean result = true;
        if (getCustomer() == null) {
            result = reportRequired("customer", validator);
        }
        return result;
    }

    /**
     * Invoked when the patient changes. This updates the alerts.
     */
    private void onPatientChanged() {
        updateAlerts();
    }

    /**
     * Updates the alerts associated with the customer and patient.
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
     * Creates a component representing the customer and patient alerts.
     *
     * @return the alerts component or {@code null} if neither has alerts
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
            result = RowFactory.create(CELL_SPACING);
            if (customerSummary != null) {
                result.add(customerSummary);
            }
            if (patientSummary != null) {
                result.add(patientSummary);
            }
            result = RowFactory.create(INSET, result);
        }
        return result;
    }

    /**
     * Returns any alerts associated with the customer.
     *
     * @param customer the customer
     * @return any alerts associated with the customer, or {@code null} if the customer has no alerts
     */
    private Component getCustomerAlerts(Party customer) {
        Component result = null;
        LayoutContext context = getLayoutContext();
        CustomerSummary summary = new CustomerSummary(context.getContext(), context.getHelpContext(),
                                                      context.getPreferences());
        AlertSummary alerts = summary.getAlertSummary(customer);
        if (alerts != null) {
            result = ColumnFactory.create("AppointmentActEditor.Alerts", LabelFactory.create("alerts.customer", BOLD),
                                          alerts.getComponent());
        }
        return result;
    }

    /**
     * Returns any alerts associated with the patient.
     *
     * @param patient the patient
     * @return any alerts associated with the patient, or {@code null} if the patient has no alerts
     */
    private Component getPatientAlerts(Party patient) {
        Component result = null;
        LayoutContext layout = getLayoutContext();
        Context context = layout.getContext();
        HelpContext help = layout.getHelpContext();
        Preferences preferences = layout.getPreferences();
        CustomerPatientSummaryFactory factory = ServiceHelper.getBean(CustomerPatientSummaryFactory.class);
        AlertSummary alerts = factory.createPatientSummary(context, help, preferences).getAlertSummary(patient);
        if (alerts != null) {
            result = ColumnFactory.create("AppointmentActEditor.Alerts", LabelFactory.create("alerts.patient", BOLD),
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
            patientListener = modifiable -> onPatientChanged();
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
        appointmentTypeReminders = rules.isRemindersEnabled(getAppointmentType());
        updateSendReminder(true);
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
     * Returns the appointment type editor.
     *
     * @return the appointment type editor
     */
    private AppointmentTypeParticipationEditor getAppointmentTypeEditor() {
        ParticipationEditor<Entity> result = getParticipationEditor("appointmentType", true);
        return (AppointmentTypeParticipationEditor) result;
    }

    /**
     * Returns the default appointment type associated with a schedule.
     *
     * @param schedule the schedule
     * @return the default appointment type, or the the first appointment type
     * if there is no default, or {@code null} if none is found
     */
    private Entity getDefaultAppointmentType(Entity schedule) {
        return rules.getDefaultAppointmentType(schedule);
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

    /**
     * Updates the send reminder flag, based on the practice, schedule, customer and appointment type.
     * <p>
     * If SMS is disabled for the practice, schedule or customer, the flag is toggled off and disabled.
     * <p>
     * If not, it is enabled.
     *
     * @param select if {@code true} and reminders may be sent, select the flag, otherwise leave it
     */
    private void updateSendReminder(boolean select) {
        Date startTime = getStartTime();
        Date now = new Date();
        Party customer = getCustomer();
        boolean enabled = smsPractice && scheduleReminders && appointmentTypeReminders && noReminder != null
                          && customer != null && startTime != null && startTime.after(now)
                          && SMSHelper.canSMS(customer);
        if (!enabled) {
            sendReminder.setSelected(false);
        }
        sendReminder.setEnabled(enabled);
        if (enabled && select) {
            if (getObject().isNew()) {
                // for new appointments only select the reminder if the start time is after the no reminder period
                Date to = DateRules.plus(now, noReminder);
                select = startTime.after(to);
            }
            sendReminder.setSelected(select);
        }
    }

    private class AppointmentLayoutStrategy extends LayoutStrategy {

        /**
         * Constructs an {@link AppointmentLayoutStrategy}.
         */
        public AppointmentLayoutStrategy() {
            ArchetypeNodes archetypeNodes = getArchetypeNodes();
            archetypeNodes.excludeIfEmpty(REMINDER_SENT, REMINDER_ERROR, BOOKING_NOTES);
            if (!smsPractice || !scheduleReminders) {
                archetypeNodes.exclude(SEND_REMINDER);
            } else {
                addComponent(new ComponentState(sendReminder, sendReminder.getProperty()));
            }
            BoundDateTimeField reminderSent = BoundDateTimeFieldFactory.create(getProperty(REMINDER_SENT));
            reminderSent.setStyleName(Styles.EDIT);
            addComponent(new ComponentState(reminderSent));
        }

        /**
         * Lay out out the object in the specified container.
         *
         * @param object     the object to lay out
         * @param properties the object's properties
         * @param parent     the parent object. May be {@code null}
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
         * <p>
         * This implementation returns the customer component.
         *
         * @param components the components
         * @return the customer component, or {@code null} if none is found
         */
        @Override
        protected Component getDefaultFocus(ComponentSet components) {
            return components.getFocusable("customer");
        }
    }

}
