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

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.app.subsystem.ShortNames;
import org.openvpms.web.app.workflow.WorkflowCRUDWindow;
import org.openvpms.web.app.workflow.checkin.CheckInWorkflow;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.TaskListener;


/**
 * Appointment CRUD window.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentCRUDWindow extends WorkflowCRUDWindow {

    /**
     * The check-in button.
     */
    private Button checkIn;

    /**
     * Check-in button identifier.
     */
    private static final String CHECKIN_ID = "checkin";


    /**
     * Constructs a new <code>AppointmentCRUDWindow</code>.
     *
     * @param type       display name for the types of objects that this may
     *                   create
     * @param shortNames the short names of archetypes that this may create.
     *                   If <code>null</code> subclass must override
     *                   {@link #getShortNames}
     */
    public AppointmentCRUDWindow(String type, ShortNames shortNames) {
        super(type, shortNames);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(Row buttons) {
        super.layoutButtons(buttons);
        if (checkIn == null) {
            checkIn = ButtonFactory.create(CHECKIN_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onCheckIn();
                }
            });
        }
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param enable determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(boolean enable) {
        super.enableButtons(enable);
        Row buttons = getButtons();
        Button consult = getConsultButton();
        Button checkOut = getCheckOutButton();
        buttons.remove(checkIn);
        buttons.remove(consult);
        buttons.remove(checkOut);
        if (enable) {
            Act act = getObject();
            String status = act.getStatus();
            if (AppointmentStatus.PENDING.equals(status)) {
                buttons.add(checkIn);
            } else if (AppointmentStatus.CHECKED_IN.equals(status)) {
                buttons.add(consult);
                buttons.add(checkOut);
            }
        }
    }

    /**
     * Creates a new edit dialog.
     *
     * @param editor the editor
     */
    @Override
    protected EditDialog createEditDialog(IMObjectEditor editor) {
        return new AppointmentEditDialog(editor);
    }

    /**
     * Invoked when the 'check-in' button is pressed.
     */
    private void onCheckIn() {
        CheckInWorkflow workflow = new CheckInWorkflow(getObject());
        workflow.setTaskListener(new TaskListener() {
            public void taskEvent(TaskEvent event) {
                onRefresh(getObject());
            }
        });
        workflow.start();
    }

}
