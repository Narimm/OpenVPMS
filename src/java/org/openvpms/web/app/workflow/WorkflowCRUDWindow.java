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

package org.openvpms.web.app.workflow;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.app.subsystem.AbstractCRUDWindow;
import org.openvpms.web.app.subsystem.ShortNames;
import org.openvpms.web.app.workflow.checkout.CheckOutWorkflow;
import org.openvpms.web.app.workflow.consult.ConsultWorkflow;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.TaskListener;
import org.openvpms.web.resource.util.Messages;


/**
 * Workflow CRUD window.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class WorkflowCRUDWindow extends AbstractCRUDWindow {

    /**
     * The consult button.
     */
    private Button consult;

    /**
     * The check-out button.
     */
    private Button checkOut;

    /**
     * Consult button identifier.
     */
    private static final String CONSULT_ID = "consult";

    /**
     * Check-out button identifier.
     */
    private static final String CHECKOUT_ID = "checkout";


    /**
     * Constructs a new <code>WorkflowCRUDWindow</code>.
     *
     * @param type       display name for the types of objects that this may
     *                   create
     * @param shortNames the short names of archetypes that this may create.
     *                   If <code>null</code> subclass must override
     *                   {@link #getShortNames}
     */
    public WorkflowCRUDWindow(String type, ShortNames shortNames) {
        super(type, shortNames);
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
        Button print = getPrintButton();
        if (enable) {
            if (buttons.indexOf(print) == -1) {
                buttons.add(print);
            }
        } else {
            buttons.remove(print);
        }
    }

    /**
     * Invoked when the delete button is pressed.
     */
    @Override
    protected void onDelete() {
        Act act = (Act) getObject();
        if (!ActStatus.COMPLETED.equals(act.getStatus())) {
            super.onDelete();
        } else {
            String name = getArchetypeDescriptor().getDisplayName();
            String status = act.getStatus();
            String title = Messages.get("act.nodelete.title", name);
            String message = Messages.get("act.nodelete.message", name, status);
            ErrorDialog.show(title, message);
        }
    }

    /**
     * Returns the 'consult' button.
     *
     * @return the 'consult' button
     */
    protected Button getConsultButton() {
        if (consult == null) {
            consult = ButtonFactory.create(CONSULT_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onConsult();
                }
            });
        }
        return consult;
    }

    /**
     * Returns the 'check-out' button.
     *
     * @return the 'check-out' button
     */
    protected Button getCheckOutButton() {
        if (checkOut == null) {
            checkOut = ButtonFactory.create(CHECKOUT_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onCheckOut();
                }
            });
        }
        return checkOut;
    }

    /**
     * Invoked when the 'consult' button is pressed.
     */
    private void onConsult() {
        ConsultWorkflow workflow = new ConsultWorkflow((Act) getObject());
        workflow.setTaskListener(new TaskListener() {
            public void taskEvent(TaskEvent event) {
                onRefresh(getObject());
            }
        });
        workflow.start();
    }

    /**
     * Invoked when the 'check-out' button is pressed.
     */
    private void onCheckOut() {
        CheckOutWorkflow workflow = new CheckOutWorkflow((Act) getObject());
        workflow.setTaskListener(new TaskListener() {
            public void taskEvent(TaskEvent event) {
                onRefresh(getObject());
            }
        });
        workflow.start();
    }

}
