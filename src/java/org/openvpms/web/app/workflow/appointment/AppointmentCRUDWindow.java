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

import echopointng.KeyStrokes;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.app.workflow.LocalClinicianContext;
import org.openvpms.web.app.workflow.checkin.CheckInWorkflow;
import org.openvpms.web.app.workflow.scheduling.ScheduleCRUDWindow;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ButtonFactory;
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
     * The browser.
     */
    private final AppointmentBrowser browser;

    /**
     * Check-in button identifier.
     */
    private static final String CHECKIN_ID = "checkin";


    /**
     * Constructs a new <tt>AppointmentCRUDWindow</tt>.
     *
     * @param browser the browser
     */
    public AppointmentCRUDWindow(AppointmentBrowser browser) {
        super(Archetypes.create("act.customerAppointment", Act.class, Messages.get("workflow.scheduling.createtype")));
        this.browser = browser;
    }

    /**
     * Creates and edits a new object.
     */
    @Override
    public void create() {
        if (browser.getSelectedSchedule() != null && browser.getSelectedTime() != null) {
            super.create();
        }
    }

    /**
     * /**
     * Edits an object.
     *
     * @param editor the object editor
     * @return the edit dialog
     */
    @Override
    protected EditDialog edit(IMObjectEditor editor) {
        Date startTime = browser.getSelectedTime();
        if (startTime != null && editor.getObject().isNew()
            && editor instanceof AppointmentActEditor) {
            ((AppointmentActEditor) editor).setStartTime(startTime);
        }
        return super.edit(editor);
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
        boolean checkInEnabled = false;
        boolean checkoutConsultEnabled = false;
        if (enable) {
            Act act = getObject();
            String status = act.getStatus();
            if (AppointmentStatus.PENDING.equals(status)) {
                checkInEnabled = true;
                checkoutConsultEnabled = false;
            } else if (canCheckoutOrConsult(act)) {
                checkInEnabled = false;
                checkoutConsultEnabled = true;
            }
        }
        buttons.setEnabled(CHECKIN_ID, checkInEnabled);
        buttons.setEnabled(CONSULT_ID, checkoutConsultEnabled);
        buttons.setEnabled(CHECKOUT_ID, checkoutConsultEnabled);
    }

    /**
     * Creates a layout context for editing an object.
     *
     * @return a new layout context.
     */
    @Override
    protected LayoutContext createLayoutContext() {
        LayoutContext context = super.createLayoutContext();

        // create a local context - don't want to pick up the current clinician
        Context local = new LocalClinicianContext(GlobalContext.getInstance());
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

    /**
     * Invoked to cut an appointment.
     */
    private void onCut() {
        PropertySet selected = browser.getSelected();
        browser.setCut(selected);
    }

    /**
     * Invoked to paste an appointment.
     */
    private void onPaste() {
        Act appointment = browser.getAct(browser.getCut());
        Entity schedule = browser.getSelectedSchedule();
        Date startTime = browser.getSelectedTime();
        if (appointment != null && schedule != null && startTime != null
            && AppointmentStatus.PENDING.equals(appointment.getStatus())) {
            browser.setCut(null);
            AppointmentActEditor editor = new AppointmentActEditor(appointment, null, new DefaultLayoutContext());
            editor.setSchedule(schedule);
            editor.setStartTime(startTime); // will recalc end time
            EditDialog dialog = edit(editor);
            dialog.save(true); // checks for overlapping appointments
        }
    }

}
