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

package org.openvpms.web.app.workflow.worklist;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.archetype.rules.workflow.TaskStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.app.workflow.LocalClinicianContext;
import org.openvpms.web.app.workflow.scheduling.ScheduleCRUDWindow;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.TaskListener;


/**
 * Task CRUD window.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TaskCRUDWindow extends ScheduleCRUDWindow {

    /**
     * The transfer button.
     */
    private Button transfer;

    /**
     * Transfer button identifier.
     */
    private static final String TRANSFER_ID = "transfer";


    /**
     * Creates a new <tt>TaskCRUDWindow</tt>.
     */
    public TaskCRUDWindow() {
        super(Archetypes.create("act.customerTask", Act.class));
    }

    /**
     * Creates and edits a new object.
     */
    @Override
    public void create() {
        if (GlobalContext.getInstance().getWorkList() != null) {
            super.create();
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
        Button transfer = getTransferButton();
        buttons.remove(consult);
        buttons.remove(checkOut);
        buttons.remove(transfer);
        if (enable) {
            Act act = getObject();
            if (canCheckoutOrConsult(act)) {
                buttons.add(consult);
                buttons.add(checkOut);
                buttons.add(transfer);
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

        // create a local context - don't want don't want to pick up the current clinician
        Context local = new LocalClinicianContext(GlobalContext.getInstance());
        context.setContext(local);
        return context;
    }

    /**
     * Determines if a consulation or checkout can be performed on an act.
     *
     * @param act the act
     * @return <tt>true</tt> if consultation can be performed
     */
    protected boolean canCheckoutOrConsult(Act act) {
        String status = act.getStatus();
        return (TaskStatus.PENDING.equals(status)
                || TaskStatus.IN_PROGRESS.equals(status)
                || TaskStatus.BILLED.equals(status));
    }

    /**
     * Returns the 'transfer' button.
     *
     * @return the 'transfer' button
     */
    private Button getTransferButton() {
        if (transfer == null) {
            transfer = ButtonFactory.create(TRANSFER_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onTransfer();
                }
            });
        }
        return transfer;
    }

    /**
     * Transfers the selected task to a different worklist.
     */
    private void onTransfer() {
        final Act act = IMObjectHelper.reload(getObject());
        // make sure the act is still available
        if (act != null) {
            TransferWorkflow transfer = new TransferWorkflow(act);
            transfer.addTaskListener(new TaskListener() {
                public void taskEvent(TaskEvent event) {
                    onRefresh(act);
                }
            });
            transfer.start();
        }
    }

}
