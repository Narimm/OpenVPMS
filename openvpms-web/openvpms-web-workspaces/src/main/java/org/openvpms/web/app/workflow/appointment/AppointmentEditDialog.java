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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.workflow.appointment;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.sql.Timestamp;
import java.util.Date;


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
     * Constructs a {@code AppointmentEditDialog}.
     *
     * @param editor  the editor
     * @param context the context
     */
    public AppointmentEditDialog(IMObjectEditor editor, Context context) {
        super(editor, context);
        getAppointmentTimes();
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
                getAppointmentTimes();
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
     * If so, and double scheduling is allowed, a confirmation dialog is shown
     * prompting to save or continue editing. If double scheduling is not
     * allowed, an error dialog is shown and no save is performed.
     *
     * @param close determines if the dialog should close if the user OKs
     *              overlapping appointments
     * @return {@code true} if there are overlapping appointments, otherwise
     *         {@code false}
     */
    private boolean checkForOverlappingAppointment(final boolean close) {
        final IMObjectEditor editor = getEditor();
        IMObject object = editor.getObject();
        boolean overlap = false;
        if (editor.isValid()) {
            Act act = (Act) object;
            ActBean appointment = new ActBean(act);
            AppointmentRules rules = new AppointmentRules();
            overlap = rules.hasOverlappingAppointments(
                act, ServiceHelper.getAppointmentService());
            if (overlap) {
                if (!allowDoubleBooking(appointment)) {
                    String title = Messages.get(
                        "workflow.scheduling.nodoubleschedule.title");
                    String message = Messages.get(
                        "workflow.scheduling.nodoubleschedule.message");
                    ErrorDialog.show(title, message);
                } else {
                    String title = Messages.get(
                        "workflow.scheduling.doubleschedule.title");
                    String message = Messages.get(
                        "workflow.scheduling.doubleschedule.message");
                    final ConfirmationDialog dialog = new ConfirmationDialog(
                        title, message);
                    dialog.addWindowPaneListener(new PopupDialogListener() {
                        @Override
                        public void onOK() {
                            if (save()) {
                                if (close) {
                                    close(OK_ID);
                                } else {
                                    getAppointmentTimes();
                                }
                            }
                        }
                    });
                    dialog.show();
                }
            }
        }
        return overlap;
    }

    /**
     * Determines if double booking is allowed.
     *
     * @param appointment the appointment
     * @return {@code true} if double booking is allowed, otherwise
     *         {@code false}
     */
    private boolean allowDoubleBooking(ActBean appointment) {
        boolean result;
        IMObject schedule = appointment.getParticipant(
            "participation.schedule");
        if (schedule != null) {
            IMObjectBean bean = new IMObjectBean(schedule);
            result = bean.getBoolean("allowDoubleBooking");
        } else {
            result = false;
        }
        return result;
    }

    /**
     * Determines if the appointment can be saved without checking for overlaps.
     *
     * @return <tt>true</tt> if the appointment can be saved
     */
    private boolean canSave() {
        Act appointment = getAppointment();
        return !appointment.isNew() && !timesModified();
    }

    /**
     * Caches the appointment start and end times.
     */
    private void getAppointmentTimes() {
        Act appointment = getAppointment();
        startTime = appointment.getActivityStartTime();
        endTime = appointment.getActivityEndTime();
    }

    /**
     * Determines if the appointment times have been modified since the
     * act was saved.
     *
     * @return {@code true} if the appointment times have been modified,
     *         otherwise {@code false}
     */
    private boolean timesModified() {
        Act act = getAppointment();
        return !ObjectUtils.equals(getDate(startTime),
                                   getDate(act.getActivityStartTime()))
               || !ObjectUtils.equals(getDate(endTime),
                                      getDate(act.getActivityEndTime()));
    }

    /**
     * Returns the appointment.
     *
     * @return the appointment
     */
    private Act getAppointment() {
        return (Act) getEditor().getObject();
    }

    /**
     * Helper to convert a Timestamp to a Date so comparisons work correctly.
     *
     * @param date the date. May be an instance/subclass of {@code Date} or
     *             null
     * @return the date, or {@code null}
     */
    private Date getDate(Date date) {
        return (date instanceof Timestamp) ? new Date(date.getTime()) : date;
    }
}
