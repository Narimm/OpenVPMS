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

package org.openvpms.web.app.workflow.worklist;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.workflow.TaskStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.app.workflow.LocalClinicianContext;
import org.openvpms.web.app.workflow.scheduling.ScheduleCRUDWindow;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.workflow.DefaultTaskListener;
import org.openvpms.web.component.workflow.TaskEvent;


/**
 * Task CRUD window.
 *
 * @author Tim Anderson
 */
public class TaskCRUDWindow extends ScheduleCRUDWindow {

    /**
     * Transfer button identifier.
     */
    private static final String TRANSFER_ID = "transfer";


    /**
     * Constructs a {@code TaskCRUDWindow}.
     *
     * @param context the context
     * @param help    the help context
     */
    public TaskCRUDWindow(Context context, HelpContext help) {
        super(Archetypes.create("act.customerTask", Act.class), context, help);
    }

    /**
     * Creates and edits a new object.
     */
    @Override
    public void create() {
        if (getContext().getWorkList() != null) {
            super.create();
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

        Button transfer = ButtonFactory.create(TRANSFER_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onTransfer();
            }
        });

        buttons.add(createConsultButton());
        buttons.add(createCheckOutButton());
        buttons.add(transfer);
        buttons.add(createOverTheCounterButton());
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
        if (enable) {
            Act act = getObject();
            enable = canCheckoutOrConsult(act);
        }
        buttons.setEnabled(CONSULT_ID, enable);
        buttons.setEnabled(CHECKOUT_ID, enable);
        buttons.setEnabled(TRANSFER_ID, enable);
    }

    /**
     * Creates a layout context for editing an object.
     *
     * @param help the help context
     * @return a new layout context
     */
    @Override
    protected LayoutContext createLayoutContext(HelpContext help) {
        // create a local context - don't want don't want to pick up the current clinician
        Context local = new LocalClinicianContext(getContext());
        return new DefaultLayoutContext(true, local, help);
    }

    /**
     * Determines if a consulation or checkout can be performed on an act.
     *
     * @param act the act
     * @return {@code true} if consultation can be performed
     */
    protected boolean canCheckoutOrConsult(Act act) {
        String status = act.getStatus();
        return (TaskStatus.PENDING.equals(status)
                || TaskStatus.IN_PROGRESS.equals(status)
                || TaskStatus.BILLED.equals(status));
    }

    /**
     * Transfers the selected task to a different worklist.
     */
    private void onTransfer() {
        final Act act = IMObjectHelper.reload(getObject());
        // make sure the act is still available
        if (act != null) {
            TransferWorkflow transfer = new TransferWorkflow(act, getContext(), getHelpContext());
            transfer.addTaskListener(new DefaultTaskListener() {
                public void taskEvent(TaskEvent event) {
                    onRefresh(act);
                }
            });
            transfer.start();
        }
    }

}
