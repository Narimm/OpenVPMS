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

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.AppointmentService;
import org.openvpms.archetype.rules.workflow.Times;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.appointment.repeat.AppointmentSeries;
import org.openvpms.web.workspace.workflow.appointment.repeat.RepeatCondition;
import org.openvpms.web.workspace.workflow.appointment.repeat.RepeatExpression;

import java.util.Date;
import java.util.List;


/**
 * Edit dialog for appointment acts.
 *
 * @author Tim Anderson
 */
public class AppointmentEditDialog extends EditDialog {

    /**
     * The appointment start time.
     */
    private Date startTime;

    /**
     * The appointment end time.
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
     * Constructs a {@link AppointmentEditDialog}.
     *
     * @param editor  the editor
     * @param context the context
     */
    public AppointmentEditDialog(AppointmentActEditor editor, Context context) {
        super(editor, context);
        getState();
    }

    /**
     * Returns the editor.
     *
     * @return the editor, or {@code null} if none has been set
     */
    @Override
    public AppointmentActEditor getEditor() {
        return (AppointmentActEditor) super.getEditor();
    }

    /**
     * Save the current object.
     */
    @Override
    protected void onApply() {
        if (canSave()) {
            save();
        } else if (!checkForOverlappingAppointment(false)) {
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
        if (canSave()) {
            super.onOK();
        } else if (!checkForOverlappingAppointment(true)) {
            super.onOK();
        }
    }

    /**
     * Determines if the appointment overlaps an existing appointment.
     * <p/>
     * If so, and double scheduling is allowed, a confirmation dialog is shown prompting to save or continue editing.
     * If double scheduling is not allowed, an error dialog is shown and no save is performed.
     *
     * @param close determines if the dialog should close if the user OKs overlapping appointments
     * @return {@code true} if there are overlapping appointments, otherwise {@code false}
     */
    private boolean checkForOverlappingAppointment(final boolean close) {
        final AppointmentActEditor editor = getEditor();
        boolean result = false;
        if (editor.isValid()) {
            List<Times> times = editor.getSeries().getAppointmentTimes();
            if (times != null) {
                AppointmentService rules = ServiceHelper.getBean(AppointmentService.class);
                Entity schedule = editor.getSchedule();
                Times overlap = rules.getOverlappingAppointment(times, schedule.getObjectReference());
                if (overlap != null) {
                    result = true;
                    if (!allowDoubleBooking(schedule)) {
                        String title = Messages.get("workflow.scheduling.nodoubleschedule.title");
                        String message = Messages.get("workflow.scheduling.nodoubleschedule.message");
                        ErrorDialog.show(title, message);
                    } else {
                        String title = Messages.get("workflow.scheduling.doubleschedule.title");
                        String message = Messages.get("workflow.scheduling.doubleschedule.message");
                        final ConfirmationDialog dialog = new ConfirmationDialog(title, message);
                        dialog.addWindowPaneListener(new PopupDialogListener() {
                            @Override
                            public void onOK() {
                                if (save()) {
                                    if (close) {
                                        close(OK_ID);
                                    } else {
                                        getState();
                                    }
                                }
                            }
                        });
                        dialog.show();
                    }
                }
            }
        }
        return result;
    }

    /**
     * Determines if double booking is allowed.
     *
     * @param schedule the appointment schedule
     * @return {@code true} if double booking is allowed, otherwise {@code false}
     */
    private boolean allowDoubleBooking(Entity schedule) {
        IMObjectBean bean = new IMObjectBean(schedule);
        return bean.getBoolean("allowDoubleBooking");
    }

    /**
     * Determines if the appointment can be saved without checking for overlaps.
     *
     * @return {@code true} if the appointment can be saved
     */
    private boolean canSave() {
        Act appointment = getAppointment();
        return !appointment.isNew() && !timeSeriesModified();
    }

    /**
     * Caches the appointment start and end times.
     */
    private void getState() {
        Act appointment = getAppointment();
        startTime = appointment.getActivityStartTime();
        endTime = appointment.getActivityEndTime();
        AppointmentSeries series = getEditor().getSeries();
        expression = series.getExpression();
        condition = series.getCondition();
    }

    /**
     * Determines if the appointment times or series have been modified since the act was saved.
     *
     * @return {@code true} if the appointment times have been modified,
     *         otherwise {@code false}
     */
    private boolean timeSeriesModified() {
        AppointmentSeries series = getEditor().getSeries();
        Act act = getAppointment();
        return DateRules.compareTo(startTime, act.getActivityStartTime()) != 0
               || DateRules.compareTo(endTime, act.getActivityEndTime()) != 0
               || !ObjectUtils.equals(expression, series.getExpression())
               || !ObjectUtils.equals(condition, series.getCondition());
    }

    /**
     * Returns the appointment.
     *
     * @return the appointment
     */
    private Act getAppointment() {
        return (Act) getEditor().getObject();
    }

}
