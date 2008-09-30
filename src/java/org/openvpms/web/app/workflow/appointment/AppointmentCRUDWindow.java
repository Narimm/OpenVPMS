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

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.app.workflow.checkin.CheckInWorkflow;
import org.openvpms.web.app.workflow.scheduling.ScheduleCRUDWindow;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ProtectedListener;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.TaskListener;
import org.openvpms.web.resource.util.Messages;

import java.util.Date;


/**
 * Appointment CRUD window.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentCRUDWindow extends ScheduleCRUDWindow {

    /**
     * The appointment start time.
     */
    private Date startTime;

    /**
     * The check-in button.
     */
    private Button checkIn;

    /**
     * Check-in button identifier.
     */
    private static final String CHECKIN_ID = "checkin";


    /**
     * Constructs a new <tt>AppointmentCRUDWindow</tt>.
     */
    public AppointmentCRUDWindow() {
        super(Archetypes.create(
                "act.customerAppointment", Act.class,
                Messages.get("workflow.scheduling.createtype")));
    }

    /**
     * Creates and edits a new object.
     */
    @Override
    public void create() {
        if (GlobalContext.getInstance().getSchedule() != null) {
            super.create();
        }
    }

    /**
     * Sets the start time for new appointments.
     *
     * @param startTime the start time. If <tt>null</tt>, specifies to use the
     *                  default start time
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Edits an object.
     *
     * @param editor the object editor
     */
    @Override
    protected void edit(IMObjectEditor editor) {
        if (startTime != null && editor.getObject().isNew()
                && editor instanceof AppointmentActEditor) {
            ((AppointmentActEditor) editor).setStartTime(startTime);
        }
        super.edit(editor);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        if (checkIn == null) {
            checkIn = ButtonFactory.create(CHECKIN_ID, new ProtectedListener() {
                protected void onAction(ActionEvent event) {
                    onCheckIn();
                }
            });
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
        super.enableButtons(buttons, enable);
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
            } else if (canCheckoutOrConsult(act)) {
                buttons.add(consult);
                buttons.add(checkOut);
            }
        }
        buttons.add(getOverTheCounterButton());
    }

    /**
     * Creates a layout context for editing an object.
     *
     * @return a new layout context.
     */
    @Override
    protected LayoutContext createLayoutContext() {
        LayoutContext context = super.createLayoutContext();

        // create a local context - don't want customer and patient changes
        // to propagate to the global context, and don't want to pick up
        // the current clinician
        Context global = GlobalContext.getInstance();
        LocalContext local = new LocalContext(null);
        local.setSchedule(global.getSchedule());
        local.setScheduleDate(global.getScheduleDate());
        local.setCustomer(global.getCustomer());
        local.setPatient(global.getPatient());
        context.setContext(local);
        return context;
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
     * Determines if a checkout or consult can be performed on an act.
     *
     * @param act the act
     */
    protected boolean canCheckoutOrConsult(Act act) {
        String status = act.getStatus();
        return AppointmentStatus.CHECKED_IN.equals(status)
                || AppointmentStatus.IN_PROGRESS.equals(status)
                || AppointmentStatus.COMPLETED.equals(status)
                || AppointmentStatus.BILLED.equals(status);
    }

    /**
     * Invoked when the 'check-in' button is pressed.
     */
    private void onCheckIn() {
        Act act = IMObjectHelper.reload(getObject());
        // make sure the act is still available and PENDING prior to beginning
        // workflow
        if (act != null && AppointmentStatus.PENDING.equals(act.getStatus())) {
            CheckInWorkflow workflow = new CheckInWorkflow(act);
            workflow.addTaskListener(new TaskListener() {
                public void taskEvent(TaskEvent event) {
                    onRefresh(getObject());
                }
            });
            workflow.start();
        } else {
            onRefresh(getObject());
        }
    }

}
