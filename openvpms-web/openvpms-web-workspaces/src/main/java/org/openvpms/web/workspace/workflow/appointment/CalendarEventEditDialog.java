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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.AppointmentService;
import org.openvpms.archetype.rules.workflow.Times;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.property.DefaultValidator;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.appointment.repeat.CalendarEventSeries;
import org.openvpms.web.workspace.workflow.appointment.repeat.RepeatCondition;
import org.openvpms.web.workspace.workflow.appointment.repeat.RepeatExpression;

import java.util.Date;
import java.util.List;

/**
 * An edit dialog for <em>act.customerAppointment</em> and <em>act.calendarBlock</em> acts.
 *
 * @author Tim Anderson
 */
public abstract class CalendarEventEditDialog extends EditDialog {

    /**
     * The appointment rules.
     */
    private final AppointmentService service;

    /**
     * The event start time.
     */
    private Date startTime;

    /**
     * The event end time.
     */
    private Date endTime;

    /**
     * The repeat expression.
     */
    private RepeatExpression expression;

    /**
     * The repeat condition.
     */
    private RepeatCondition condition;

    /**
     * Determines if overlap checks should always be performed.
     */
    private boolean alwaysCheckOverlap = false;

    /**
     * Constructs a {@link CalendarEventEditDialog}.
     *
     * @param editor  the editor
     * @param context the context
     */
    public CalendarEventEditDialog(CalendarEventEditor editor, Context context) {
        super(editor, context);
        this.service = ServiceHelper.getBean(AppointmentService.class);
        getState();
    }

    /**
     * Returns the editor.
     *
     * @return the editor, or {@code null} if none has been set
     */
    @Override
    public CalendarEventEditor getEditor() {
        return (CalendarEventEditor) super.getEditor();
    }

    /**
     * Determines if overlap checking should always be performed.
     *
     * @param checkOverlap if {@code true}, always check for overlaps
     */
    public void setAlwaysCheckOverlap(boolean checkOverlap) {
        this.alwaysCheckOverlap = checkOverlap;
    }

    /**
     * Returns the appointment service.
     *
     * @return the appointment service.
     */
    protected AppointmentService getService() {
        return service;
    }

    /**
     * Save the current object.
     */
    @Override
    protected void onApply() {
        if (noTimeCheckRequired()) {
            save();
        } else if (checkEventTimes(false)) { // TODO - should check and save in same transaction
            if (save()) {
                getState();
            }
        }
    }

    /**
     * Save the current object, and close the editor.
     */
    @Override
    protected void onOK() {
        if (noTimeCheckRequired()) {
            super.onOK();
        } else if (checkEventTimes(true)) {
            super.onOK();
        }
    }

    /**
     * Returns the event.
     *
     * @return the event
     */
    protected Act getEvent() {
        return getEditor().getObject();
    }

    /**
     * Caches the event start and end times and repeat details.
     */
    protected void getState() {
        Act event = getEvent();
        startTime = event.getActivityStartTime();
        endTime = event.getActivityEndTime();
        CalendarEventSeries series = getEditor().getSeries();
        expression = series.getExpression();
        condition = series.getCondition();
    }

    /**
     * Verifies that the event or events in the series don't overlap existing events.
     * <p/>
     * If they do, and double scheduling is allowed, a confirmation dialog is shown prompting to save or continue
     * editing.
     * <br/>
     * If double scheduling is not allowed, an error dialog is shown and no save is performed.
     *
     * @param close determines if the dialog should close if the user OKs overlapping appointments
     * @return {@code true} if the event and series is valid, otherwise {@code false}
     */
    protected boolean checkEventTimes(boolean close) {
        CalendarEventEditor editor = getEditor();
        Validator validator = new DefaultValidator();
        boolean result = editor.validate(validator);
        if (result) {
            List<Times> times = editor.getEventTimes();
            result = times != null && checkEventTimes(times, close);
        } else {
            ValidationHelper.showError(validator);
        }
        return result;
    }

    /**
     * Verifies that the event or events in the series don't overlap existing events.
     * <p/>
     * If they do, and double scheduling is allowed, a confirmation dialog is shown prompting to save or continue
     * editing.
     * <br/>
     * If double scheduling is not allowed, an error dialog is shown and no save is performed.
     *
     * @param times the event and series times
     * @param close determines if the dialog should close if the user OKs overlapping appointments
     * @return {@code true} if the event and series is valid, otherwise {@code false}
     */
    protected abstract boolean checkEventTimes(List<Times> times, boolean close);

    /**
     * Determines if the event can be saved without checking the event times.
     *
     * @return {@code true} if the event can be saved
     */
    protected boolean noTimeCheckRequired() {
        Act event = getEvent();
        return !alwaysCheckOverlap && !event.isNew() && !timeSeriesModified();
    }

    /**
     * Determines if the event times or series have been modified since the act was saved.
     *
     * @return {@code true} if the event times have been modified, otherwise {@code false}
     */
    protected boolean timeSeriesModified() {
        CalendarEventSeries series = getEditor().getSeries();
        Act act = getEvent();
        return DateRules.compareTo(startTime, act.getActivityStartTime()) != 0
               || DateRules.compareTo(endTime, act.getActivityEndTime()) != 0
               || !ObjectUtils.equals(expression, series.getExpression())
               || !ObjectUtils.equals(condition, series.getCondition());
    }

    /**
     * Displays an error message if the event or its series overlaps another.
     *
     * @param overlap the overlapping event
     */
    protected void displayOverlapError(Times overlap) {
        String displayName = DescriptorHelper.getDisplayName(getEvent());
        String title = Messages.format("workflow.scheduling.nodoubleschedule.title", displayName);
        String message = Messages.format("workflow.scheduling.nodoubleschedule.message", displayName);
        OverlappingCalendarEventDialog dialog = new OverlappingCalendarEventDialog(title, message, overlap,
                                                                                   PopupDialog.OK);
        dialog.show();
    }

}
