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

package org.openvpms.web.workspace.workflow.appointment;

import echopointng.KeyStrokes;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.RadioButton;
import nextapp.echo2.app.button.ButtonGroup;
import nextapp.echo2.app.event.ActionEvent;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.patient.PatientInformationService;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.contact.ContactHelper;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.TabbedBrowserListener;
import org.openvpms.web.component.im.sms.SMSDialog;
import org.openvpms.web.component.im.sms.SMSHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.im.view.Selection;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workflow.DefaultTaskListener;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.Workflow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.dialog.MessageDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.info.PatientContextHelper;
import org.openvpms.web.workspace.workflow.LocalClinicianContext;
import org.openvpms.web.workspace.workflow.WorkflowFactory;
import org.openvpms.web.workspace.workflow.appointment.reminder.AppointmentReminderEvaluator;
import org.openvpms.web.workspace.workflow.appointment.repeat.AppointmentSeriesState;
import org.openvpms.web.workspace.workflow.appointment.repeat.RepeatCondition;
import org.openvpms.web.workspace.workflow.appointment.repeat.RepeatExpression;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleCRUDWindow;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Appointment CRUD window.
 *
 * @author Tim Anderson
 */
public class AppointmentCRUDWindow extends ScheduleCRUDWindow {

    /**
     * The browser.
     */
    private final AppointmentBrowser browser;

    /**
     * The rules.
     */
    private final AppointmentRules rules;

    /**
     * The original status of the appointment being edited.
     */
    private String oldStatus;

    /**
     * Check-in button identifier.
     */
    private static final String CHECKIN_ID = "button.checkin";

    /**
     * SMS button identifier.
     */
    private static final String SMS_ID = "button.sms.send";

    /**
     * Constructs an {@link AppointmentCRUDWindow}.
     *
     * @param browser the browser
     * @param context the context
     * @param help    the help context
     */
    public AppointmentCRUDWindow(AppointmentBrowser browser, Context context, HelpContext help) {
        this(browser, AppointmentActions.INSTANCE, context, help);
    }

    /**
     * Constructs an {@link AppointmentCRUDWindow}.
     *
     * @param browser the browser
     * @param context the context
     * @param help    the help context
     */
    protected AppointmentCRUDWindow(AppointmentBrowser browser, AppointmentActions actions, Context context,
                                    HelpContext help) {
        super(Archetypes.create("act.customerAppointment", Act.class, Messages.get("workflow.scheduling.createtype")),
              actions, context, help);
        this.browser = browser;
        browser.setListener(new TabbedBrowserListener() {
            @Override
            public void onBrowserChanged() {
                enableButtons(getButtons(), getObject() != null);
            }
        });
        rules = ServiceHelper.getBean(AppointmentRules.class);
    }

    /**
     * Creates and edits a new appointment, if a slot has been selected.
     */
    @Override
    public void create() {
        if (canCreateAppointment()) {
            super.create();
        }
    }

    /**
     * Deletes an object.
     *
     * @param object the object to delete
     */
    @Override
    protected void delete(final Act object) {
        final AppointmentSeriesState state = new AppointmentSeriesState(object, ServiceHelper.getArchetypeService());
        if (state.hasSeries() && state.canEditFuture()) {
            final DeleteSeriesDialog dialog = new DeleteSeriesDialog(state, getHelpContext().subtopic("deleteseries"));
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    boolean deleted = false;
                    if (dialog.single()) {
                        deleted = state.delete();
                    } else if (dialog.future()) {
                        deleted = state.deleteFuture();
                    } else if (dialog.all()) {
                        deleted = state.deleteSeries();
                    }
                    if (deleted) {
                        onDeleted(object);
                    }
                }
            });
            dialog.show();
        } else {
            super.delete(object);
        }
    }

    /**
     * Determines the actions that may be performed on the selected object.
     *
     * @return the actions
     */
    @Override
    protected AppointmentActions getActions() {
        return (AppointmentActions) super.getActions();
    }

    /**
     * Edits an object.
     *
     * @param object the object to edit
     * @param path   the selection path. May be {@code null}
     */
    @Override
    protected void edit(final Act object, final List<Selection> path) {
        oldStatus = object.getStatus();
        final AppointmentSeriesState state = new AppointmentSeriesState(object, ServiceHelper.getArchetypeService());
        if (state.hasSeries()) {
            if (state.canEditFuture()) {
                final EditSeriesDialog dialog = new EditSeriesDialog(state, getHelpContext().subtopic("editseries"));
                dialog.addWindowPaneListener(new PopupDialogListener() {
                    @Override
                    public void onOK() {
                        if (dialog.single()) {
                            edit(object, path, false);
                        } else if (dialog.future()) {
                            edit(object, path, true);
                        } else if (dialog.all()) {
                            edit(state.getFirst(), path, true);
                        }
                    }
                });
                dialog.show();
            } else {
                // can't edit the future appointments, so disable series editing
                edit(object, path, false);
            }
        } else {
            // not part of a series, so enable series editing
            edit(object, path, true);
        }
    }

    /**
     * Edits an appointment.
     *
     * @param object     the appointment
     * @param path       the selection path. May be {@code null}
     * @param editSeries if {@code true}, edit the appointment series, otherwise edit the appointment
     */
    protected void edit(Act object, List<Selection> path, boolean editSeries) {
        try {
            HelpContext edit = createEditTopic(object);
            LayoutContext context = createLayoutContext(edit);
            IMObjectEditor editor = new AppointmentActEditor(object, null, editSeries, context);
            editor.getComponent();
            edit(editor, path);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Edits an object.
     *
     * @param editor the object editor
     * @param path   the selection path. May be {@code null}
     * @return the edit dialog
     */
    @Override
    protected EditDialog edit(IMObjectEditor editor, List<Selection> path) {
        Date startTime = browser.getSelectedTime();
        if (startTime != null && editor.getObject().isNew() && editor instanceof AppointmentActEditor) {
            ((AppointmentActEditor) editor).setStartTime(startTime);
        }
        return super.edit(editor, path);
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(Act object, boolean isNew) {
        super.onSaved(object, isNew);
        String newStatus = object.getStatus();
        User user = getContext().getUser();
        if (!AppointmentStatus.CANCELLED.equals(oldStatus) && AppointmentStatus.CANCELLED.equals(newStatus)) {
            PatientContext context = getPatientContext(object);
            if (context != null) {
                PatientInformationService service = ServiceHelper.getBean(PatientInformationService.class);
                service.admissionCancelled(context, user);
            }
        } else if (!isAdmitted(oldStatus) && isAdmitted(newStatus)) {
            PatientContext context = getPatientContext(object);
            if (context != null) {
                PatientInformationService service = ServiceHelper.getBean(PatientInformationService.class);
                service.admitted(context, user);
            }
        } else if (isAdmitted(oldStatus) && !isAdmitted(newStatus)) {
            PatientContext context = getPatientContext(object);
            if (context != null) {
                PatientInformationService service = ServiceHelper.getBean(PatientInformationService.class);
                service.discharged(context, user);
            }
        }
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        Button checkIn = ButtonFactory.create(CHECKIN_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onCheckIn();
            }
        });
        buttons.add(checkIn);
        buttons.add(createConsultButton());
        buttons.add(createCheckOutButton());
        buttons.add(createOverTheCounterButton());
        buttons.add(createFlowSheetButton());
        buttons.addKeyListener(KeyStrokes.CONTROL_MASK | 'C', new ActionListener() {
            public void onAction(ActionEvent event) {
                onCopy();
            }
        });
        buttons.addKeyListener(KeyStrokes.CONTROL_MASK | 'X', new ActionListener() {
            public void onAction(ActionEvent event) {
                onCut();
            }
        });
        buttons.addKeyListener(KeyStrokes.CONTROL_MASK | 'V', new ActionListener() {
            public void onAction(ActionEvent event) {
                onPaste();
            }
        });
        if (SMSHelper.isSMSEnabled(getContext().getPractice())) {
            buttons.add(ButtonFactory.create(SMS_ID, new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    onSMS();
                }
            }));
        }
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        enable = browser.isAppointmentsSelected() && enable;
        super.enableButtons(buttons, enable);
        boolean checkInEnabled = false;
        boolean checkoutConsultEnabled = false;
        boolean smsEnabled = false;
        if (enable) {
            Act act = getObject();
            AppointmentActions actions = getActions();
            if (actions.canCheckIn(act)) {
                checkInEnabled = true;
                checkoutConsultEnabled = false;
            } else if (actions.canCheckoutOrConsult(act)) {
                checkInEnabled = false;
                checkoutConsultEnabled = true;
            }
            if (actions.canSMS(act)) {
                smsEnabled = true;
            }
        }
        buttons.setEnabled(NEW_ID, canCreateAppointment());
        buttons.setEnabled(CHECKIN_ID, checkInEnabled);
        buttons.setEnabled(CONSULT_ID, checkoutConsultEnabled);
        buttons.setEnabled(CHECKOUT_ID, checkoutConsultEnabled);
        buttons.setEnabled(OVER_THE_COUNTER_ID, browser.isAppointmentsSelected());
        buttons.setEnabled(SMS_ID, smsEnabled);
    }

    /**
     * Creates a layout context for editing an object.
     *
     * @param help the help context
     * @return a new layout context.
     */
    @Override
    protected LayoutContext createLayoutContext(HelpContext help) {
        // create a local context - don't want to pick up the current clinician
        Context local = new LocalClinicianContext(getContext());
        return new DefaultLayoutContext(true, local, help);
    }

    /**
     * Creates a new edit dialog.
     *
     * @param editor the editor
     */
    @Override
    protected EditDialog createEditDialog(IMObjectEditor editor) {
        return new AppointmentEditDialog((AppointmentActEditor) editor, getContext());
    }

    /**
     * Determines if an appointment can be created.
     *
     * @return {@code true} if a schedule and slot has been selected
     */
    private boolean canCreateAppointment() {
        return browser.isAppointmentsSelected() && browser.getSelectedSchedule() != null
               && browser.getSelectedTime() != null;
    }

    /**
     * Invoked when the 'check-in' button is pressed.
     */
    private void onCheckIn() {
        Act act = IMObjectHelper.reload(getObject());
        // make sure the act is still available and can be checked in prior to beginning workflow
        if (act != null && getActions().canCheckIn(act)) {
            WorkflowFactory factory = ServiceHelper.getBean(WorkflowFactory.class);
            Workflow workflow = factory.createCheckInWorkflow(act, getContext(), getHelpContext());
            workflow.addTaskListener(new DefaultTaskListener() {
                public void taskEvent(TaskEvent event) {
                    onRefresh(getObject());
                }
            });
            workflow.start();
        } else {
            onRefresh(getObject());
        }
    }

    /**
     * Invoked to copy an appointment.
     */
    private void onCopy() {
        if (browser.isAppointmentsSelected()) {
            browser.clearMarked();
            PropertySet selected = browser.getSelected();
            Act appointment = browser.getAct(selected);
            if (appointment != null) {
                browser.setMarked(selected, false);
            } else {
                InformationDialog.show(Messages.get("workflow.scheduling.appointment.copy.title"),
                                       Messages.get("workflow.scheduling.appointment.copy.select"));
            }
        }
    }

    /**
     * Invoked to cut an appointment.
     */
    private void onCut() {
        if (browser.isAppointmentsSelected()) {
            browser.clearMarked();
            PropertySet selected = browser.getSelected();
            Act appointment = browser.getAct(selected);
            if (appointment != null) {
                if (AppointmentStatus.PENDING.equals(appointment.getStatus())) {
                    browser.setMarked(selected, true);
                } else {
                    InformationDialog.show(Messages.get("workflow.scheduling.appointment.cut.title"),
                                           Messages.get("workflow.scheduling.appointment.cut.pending"));
                }
            } else {
                InformationDialog.show(Messages.get("workflow.scheduling.appointment.cut.title"),
                                       Messages.get("workflow.scheduling.appointment.cut.select"));
            }
        }
    }

    /**
     * Invoked to paste an appointment.
     * <p>
     * For the paste to be successful:
     * <ul>
     * <li>the appointment must still exist
     * <li>for cut appointments, the appointment must be PENDING
     * <li>a schedule must be selected
     * <li>a time slot must be selected
     * </ul>
     */
    private void onPaste() {
        if (browser.isAppointmentsSelected()) {
            if (browser.getMarked() == null) {
                InformationDialog.show(Messages.get("workflow.scheduling.appointment.paste.title"),
                                       Messages.get("workflow.scheduling.appointment.paste.select"));
            } else {
                final Act appointment = browser.getAct(browser.getMarked());
                final Entity schedule = browser.getSelectedSchedule();
                final Date startTime = browser.getSelectedTime();
                if (appointment == null) {
                    InformationDialog.show(Messages.get("workflow.scheduling.appointment.paste.title"),
                                           Messages.get("workflow.scheduling.appointment.paste.noexist"));
                    onRefresh(null);        // force redraw
                    browser.clearMarked();
                } else if (browser.isCut() && !AppointmentStatus.PENDING.equals(appointment.getStatus())) {
                    InformationDialog.show(Messages.get("workflow.scheduling.appointment.paste.title"),
                                           Messages.get("workflow.scheduling.appointment.paste.pending"));
                    onRefresh(appointment); // force redraw
                    browser.clearMarked();
                } else if (schedule == null || startTime == null) {
                    InformationDialog.show(Messages.get("workflow.scheduling.appointment.paste.title"),
                                           Messages.get("workflow.scheduling.appointment.paste.noslot"));
                } else {
                    final AppointmentSeriesState state = new AppointmentSeriesState(
                            appointment, ServiceHelper.getArchetypeService());
                    HelpContext help = getHelpContext();
                    if (browser.isCut()) {
                        if (state.hasSeries() && state.canEditFuture()) {
                            final MoveSeriesDialog dialog = new MoveSeriesDialog(state, help.subtopic("moveseries"));
                            dialog.addWindowPaneListener(new PopupDialogListener() {
                                @Override
                                public void onOK() {
                                    if (dialog.single()) {
                                        cut(appointment, schedule, startTime, null);
                                    } else if (dialog.future()) {
                                        cut(appointment, schedule, startTime, state);
                                    } else if (dialog.all()) {
                                        cut(state.getFirst(), schedule, startTime, state);
                                    }
                                }
                            });
                            dialog.show();
                        } else {
                            cut(appointment, schedule, startTime, null);
                        }
                    } else {
                        if (state.hasSeries() && state.canEditFuture()) {
                            final CopySeriesDialog dialog = new CopySeriesDialog(state, help.subtopic("copyseries"));
                            dialog.addWindowPaneListener(new PopupDialogListener() {
                                @Override
                                public void onOK() {
                                    if (dialog.single()) {
                                        copy(appointment, schedule, startTime, null, 0);
                                    } else if (dialog.future()) {
                                        copy(appointment, schedule, startTime, state, state.getIndex());
                                    } else if (dialog.all()) {
                                        copy(state.getFirst(), schedule, startTime, state, 0);
                                    }
                                }
                            });
                            dialog.show();
                        } else {
                            copy(appointment, schedule, startTime, null, 0);
                        }
                    }
                }
            }
        }
    }

    /**
     * Invoked to send an SMS reminder for the selected appointment.
     */
    private void onSMS() {
        final Act object = getObject();
        final ActBean bean = new ActBean(object);
        Party customer = (Party) bean.getNodeParticipant("customer");
        Party patient = (Party) bean.getNodeParticipant("patient");
        Context context = getContext();
        Party location = context.getLocation();
        final List<Contact> contacts = ContactHelper.getSMSContacts(customer);
        if (!contacts.isEmpty() && location != null) {
            final Context local = new LocalContext(context);
            local.setCustomer(customer);
            local.setPatient(patient);
            Entity template = SMSHelper.getAppointmentTemplate(location);
            SMSDialog dialog = new SMSDialog(contacts, local, getHelpContext().subtopic("sms"));
            dialog.show();
            if (template != null) {
                try {
                    AppointmentReminderEvaluator evaluator = ServiceHelper.getBean(AppointmentReminderEvaluator.class);
                    String message = evaluator.evaluate(template, object, location, context.getPractice());
                    dialog.setMessage(message);
                } catch (Throwable exception) {
                    ErrorHelper.show(exception);
                }
            }
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    bean.setValue("reminderSent", new Date());
                    bean.setValue("reminderError", null);
                    bean.save();
                    onSaved(object, false);
                }
            });
        }
    }

    /**
     * Cuts an appointment and pastes it to the specified schedule and start time.
     *
     * @param appointment the appointment
     * @param schedule    the new schedule
     * @param startTime   the new start time
     * @param series      the appointment series. May be {@code null}
     */
    private void cut(Act appointment, Entity schedule, Date startTime, AppointmentSeriesState series) {
        int duration = getDuration(appointment.getActivityStartTime(), appointment.getActivityEndTime());
        paste(appointment, schedule, startTime, duration, series, false, null, null);
        browser.clearMarked();
    }

    /**
     * Copies an appointment and pastes it to the specified schedule and start time.
     *
     * @param appointment the appointment
     * @param schedule    the new schedule
     * @param startTime   the new start time
     * @param series      the appointment series. May be {@code null}
     * @param index       the index of the appointment in the series
     */
    private void copy(Act appointment, Entity schedule, Date startTime, AppointmentSeriesState series, int index) {
        int duration = getDuration(appointment.getActivityStartTime(), appointment.getActivityEndTime());
        appointment = rules.copy(appointment);
        ActBean bean = new ActBean(appointment);
        bean.setValue("status", AppointmentStatus.PENDING);
        bean.setValue("arrivalTime", null);
        bean.setValue("reminderSent", null);
        bean.setValue("reminderError", null);
        bean.setParticipant(UserArchetypes.AUTHOR_PARTICIPATION, getContext().getUser());
        RepeatExpression expression = (series != null) ? series.getExpression() : null;
        RepeatCondition condition = (series != null) ? series.getCondition(index) : null;
        paste(appointment, schedule, startTime, duration, series, true, expression, condition);
    }

    /**
     * Pastes an appointment to the specified schedule and start time.
     *
     * @param appointment the appointment
     * @param schedule    the new schedule
     * @param startTime   the new start time
     * @param duration    the duration of the appointment, in minutes
     * @param series      the appointment series. May be {@code null}
     * @param copy        if {@code true}, the appointment is being copied, otherwise it is being moved
     * @param expression  the new repeat expression. Only relevant if the series is being copied. May be {@code null}
     * @param condition   the new repeat condition. Only relevant if the series is being copied. May be {@code null}
     */
    private void paste(Act appointment, Entity schedule, Date startTime, int duration, AppointmentSeriesState series,
                       boolean copy, RepeatExpression expression, RepeatCondition condition) {
        HelpContext edit = createEditTopic(appointment);
        DefaultLayoutContext context = new DefaultLayoutContext(getContext(), edit);
        AppointmentActEditor editor = new AppointmentActEditor(appointment, null, series != null, context);
        EditDialog dialog = edit(editor, null);  // NOTE: need to update the start time after dialog is created
        editor.setSchedule(schedule);            //       See AppointmentEditDialog.timesModified().
        editor.setStartTime(startTime);   // will recalc end time. May be rounded to nearest slot
        startTime = editor.getStartTime();
        Date endTime = editor.getEndTime();
        if (endTime != null) {
            // if the new appointment is shorter than the old, try and adjust it
            int newLength = getDuration(editor.getStartTime(), endTime);
            if (newLength < duration) {
                editor.setEndTime(DateRules.getDate(startTime, duration, DateUnits.MINUTES));
            }
        }
        if (copy) {
            editor.setExpression(expression);
            editor.setCondition(condition);
        } else {
            editor.getSeries().setUpdateTimesOnly(true);
        }
        dialog.save(true);              // checks for overlapping appointments
        browser.setSelected(browser.getEvent(appointment));
    }

    /**
     * Returns the duration in minutes between two times.
     *
     * @param startTime the start time
     * @param endTime   the end time
     * @return the duration in minutes
     */
    private int getDuration(Date startTime, Date endTime) {
        return Minutes.minutesBetween(new DateTime(startTime), new DateTime(endTime)).getMinutes();
    }

    /**
     * Returns the patient context for an appointment.
     *
     * @param appointment the appointment
     * @return the patient context, or {@code null} if the patient can't be found, or has no current visit
     */
    private PatientContext getPatientContext(Act appointment) {
        return PatientContextHelper.getAppointmentContext(appointment, getContext());
    }

    /**
     * Determines if an appointment status indicates the patient has been admitted.
     *
     * @param status the appointment status
     * @return {@code true} if the patient has been admitted
     */
    private boolean isAdmitted(String status) {
        return AppointmentStatus.CHECKED_IN.equals(status) || AppointmentStatus.ADMITTED.equals(status)
               || AppointmentStatus.IN_PROGRESS.equals(status) || AppointmentStatus.BILLED.equals(status);
    }

    protected static class AppointmentActions extends ScheduleActions {

        public static AppointmentActions INSTANCE = new AppointmentActions();

        /**
         * Determines if an appointment can be checked in.
         *
         * @param act the appointment
         * @return {@code true} if it can be checked in
         */
        public boolean canCheckIn(Act act) {
            return AppointmentStatus.PENDING.equals(act.getStatus());
        }

        /**
         * Determines if a consultation or checkout can be performed on an act.
         *
         * @param act the act
         * @return {@code true} if consultation can be performed
         */
        @Override
        public boolean canCheckoutOrConsult(Act act) {
            String status = act.getStatus();
            return AppointmentStatus.CHECKED_IN.equals(status)
                   || AppointmentStatus.IN_PROGRESS.equals(status)
                   || AppointmentStatus.COMPLETED.equals(status)
                   || AppointmentStatus.BILLED.equals(status);
        }

        /**
         * Determines if a customer can receive SMS messages.
         *
         * @param act the appointment
         * @return {@code true} if the customer can receive SMS messages
         */
        public boolean canSMS(Act act) {
            ActBean bean = new ActBean(act);
            return AppointmentStatus.PENDING.equals(act.getStatus()) && bean.getBoolean("sendReminder")
                   && SMSHelper.canSMS((Party) bean.getNodeParticipant("customer"));
        }
    }

    private static class SeriesDialog extends MessageDialog {

        private final AppointmentSeriesState series;
        private RadioButton single;
        private RadioButton future;
        private RadioButton all;

        public SeriesDialog(String title, String message, AppointmentSeriesState series, HelpContext help) {
            super(title, message, OK_CANCEL, help);
            this.series = series;
            ButtonGroup group = new ButtonGroup();
            ActionListener listener = new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    onOK();
                }
            };
            single = ButtonFactory.create("workflow.scheduling.appointment.series.single", group, listener);
            single.setSelected(true);
            if (series.canEditFuture()) {
                future = ButtonFactory.create("workflow.scheduling.appointment.series.future", group, listener);
            }
            if (series.canEditSeries()) {
                all = ButtonFactory.create("workflow.scheduling.appointment.series.all", group, listener);
            }
        }

        public boolean single() {
            return single.isSelected();
        }

        public boolean future() {
            return future != null && future.isSelected();
        }

        public boolean all() {
            return all != null && all.isSelected();
        }

        /**
         * Invoked when the 'OK' button is pressed. This sets the action and closes
         * the window.
         */
        @Override
        protected void onOK() {
            if (future()) {
                List<String> statuses = series.getFutureNonPendingStatuses();
                if (!statuses.isEmpty()) {
                    confirm(statuses);
                } else {
                    super.onOK();
                }
            } else if (all()) {
                List<String> statuses = series.getNonPendingStatuses();
                if (!statuses.isEmpty()) {
                    confirm(statuses);
                } else {
                    super.onOK();
                }
            } else {
                super.onOK();
            }
        }

        /**
         * Lays out the component prior to display.
         */
        @Override
        protected void doLayout() {
            Label message = LabelFactory.create(true, true);
            message.setText(getMessage());
            Column column = ColumnFactory.create(Styles.WIDE_CELL_SPACING, message, single);
            if (future != null) {
                column.add(future);
            }
            if (all != null) {
                column.add(all);
            }
            getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, column));
        }


        /**
         * Confirms an operation if there are appointments with non-Pending status.
         *
         * @param statuses the statuses
         */
        private void confirm(List<String> statuses) {
            Map<String, String> names = LookupNameHelper.getLookupNames(ScheduleArchetypes.APPOINTMENT, "status");

            String message;
            if (statuses.size() == 1) {
                message = Messages.format("workflow.scheduling.appointment.series.nonpending1",
                                          names.get(statuses.get(0)));
            } else {
                StringBuilder buffer = new StringBuilder();
                for (int i = 0; i < statuses.size() - 1; ++i) {
                    if (i > 0) {
                        buffer.append(", ");
                    }
                    buffer.append(names.get(statuses.get(i)));
                }
                String last = names.get(statuses.get(statuses.size() - 1));
                message = Messages.format("workflow.scheduling.appointment.series.nonpending2", buffer, last);
            }
            ConfirmationDialog dialog = new ConfirmationDialog(getTitle(), message);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    SeriesDialog.super.onOK();
                }

                @Override
                public void onCancel() {
                    SeriesDialog.super.onCancel();
                }
            });
            dialog.show();
        }
    }

    private static class EditSeriesDialog extends SeriesDialog {

        /**
         * Constructs a {@link EditSeriesDialog}.
         *
         * @param series the appointment series
         * @param help   the help context
         */
        public EditSeriesDialog(AppointmentSeriesState series, HelpContext help) {
            super(Messages.get("workflow.scheduling.appointment.editseries.title"),
                  Messages.get("workflow.scheduling.appointment.editseries.message"), series, help);
        }
    }

    private static class DeleteSeriesDialog extends SeriesDialog {

        /**
         * Constructs a {@link DeleteSeriesDialog}.
         *
         * @param series the appointment series
         * @param help   the help context
         */
        public DeleteSeriesDialog(AppointmentSeriesState series, HelpContext help) {
            super(Messages.get("workflow.scheduling.appointment.deleteseries.title"),
                  Messages.get("workflow.scheduling.appointment.deleteseries.message"), series, help);
        }
    }

    private static class CopySeriesDialog extends SeriesDialog {

        /**
         * Constructs a {@link CopySeriesDialog}.
         *
         * @param series the appointment series
         * @param help   the help context
         */
        public CopySeriesDialog(AppointmentSeriesState series, HelpContext help) {
            super(Messages.get("workflow.scheduling.appointment.copyseries.title"),
                  Messages.get("workflow.scheduling.appointment.copyseries.message"), series, help);
        }
    }

    private static class MoveSeriesDialog extends SeriesDialog {

        /**
         * Constructs a {@link MoveSeriesDialog}.
         *
         * @param series the appointment series
         * @param help   the help context
         */
        public MoveSeriesDialog(AppointmentSeriesState series, HelpContext help) {
            super(Messages.get("workflow.scheduling.appointment.moveseries.title"),
                  Messages.get("workflow.scheduling.appointment.moveseries.message"), series, help);
        }
    }

}
