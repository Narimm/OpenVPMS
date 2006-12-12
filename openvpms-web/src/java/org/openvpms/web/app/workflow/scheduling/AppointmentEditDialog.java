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

import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.resource.util.Messages;


/**
 * Edit dialog for appointment acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentEditDialog extends EditDialog {

    /**
     * Construct a new <code>AppointmentEditDialog</code>.
     *
     * @param editor the editor
     */
    public AppointmentEditDialog(IMObjectEditor editor) {
        super(editor);
    }

    /**
     * Save the current object.
     */
    @Override
    protected void onApply() {
        if (!checkForOverlappingAppointment(false)) {
            super.onApply();
        }
    }

    /**
     * Save the current object, and close the editor.
     */
    @Override
    protected void onOK() {
        if (!checkForOverlappingAppointment(true)) {
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
     * @return <code>true</code> if there are overlapping appointments, otherwise
     *         <code>false</code>
     */
    private boolean checkForOverlappingAppointment(final boolean close) {
        final IMObjectEditor editor = getEditor();
        IMObject object = editor.getObject();
        boolean overlap = false;
        if (editor.isValid()) {
            Act act = (Act) object;
            ActBean appointment = new ActBean(act);
            overlap = AppointmentRules.hasOverlappingAppointments(act);
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
                    dialog.addWindowPaneListener(new WindowPaneListener() {
                        public void windowPaneClosing(WindowPaneEvent e) {
                            if (ConfirmationDialog.OK_ID.equals(
                                    dialog.getAction())) {
                                if (editor.save() && close) {
                                    close();
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
     * @return <code>true</code> if double booking is allowed, otherwise
     *         <code>false</code>
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


}
