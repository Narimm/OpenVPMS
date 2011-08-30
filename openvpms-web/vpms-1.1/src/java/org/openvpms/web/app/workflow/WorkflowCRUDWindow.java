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
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.app.subsystem.AbstractCRUDWindow;
import org.openvpms.web.app.subsystem.ShortNames;
import org.openvpms.web.app.workflow.checkout.CheckOutWorkflow;
import org.openvpms.web.app.workflow.consult.ConsultWorkflow;
import org.openvpms.web.app.workflow.otc.OverTheCounterWorkflow;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ProtectedListener;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.TaskListener;
import org.openvpms.web.component.workflow.Workflow;
import org.openvpms.web.resource.util.Messages;


/**
 * Workflow CRUD window.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class WorkflowCRUDWindow extends AbstractCRUDWindow<Act> {

    /**
     * The consult button.
     */
    private Button consult;

    /**
     * The check-out button.
     */
    private Button checkOut;

    /**
     * The over-the-counter button.
     */
    private Button overTheCounter;

    /**
     * Consult button identifier.
     */
    private static final String CONSULT_ID = "consult";

    /**
     * Check-out button identifier.
     */
    private static final String CHECKOUT_ID = "checkout";

    /**
     * Over-the-counter button identifier.
     */
    private static final String OVER_THE_COUNTER_ID = "OTC";


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
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        super.enableButtons(buttons, enable);
        Button print = getPrintButton();
        if (enable) {
            if (!buttons.contains(print)) {
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
        Act act = getObject();
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
            consult = ButtonFactory.create(CONSULT_ID, new ProtectedListener() {
                protected void onAction(ActionEvent event) {
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
            checkOut = ButtonFactory.create(
                    CHECKOUT_ID, new ProtectedListener() {
                protected void onAction(ActionEvent event) {
                    onCheckOut();
                }
            });
        }
        return checkOut;
    }

    /**
     * Returns the 'over-the-counter' button.
     *
     * @return the 'over-the-counter' button
     */
    protected Button getOverTheCounterButton() {
        if (overTheCounter == null) {
            overTheCounter = ButtonFactory.create(
                    OVER_THE_COUNTER_ID, new ProtectedListener() {
                protected void onAction(ActionEvent event) {
                    onOverTheCounter();
                }
            });
        }
        return overTheCounter;
    }

    /**
     * Determines if a consulation or checkout can be performed on an act.
     *
     * @param act the act
     * @return <tt>true</tt> if consultation can be performed
     */
    protected abstract boolean canCheckoutOrConsult(Act act);

    /**
     * Invoked when the 'consult' button is pressed.
     */
    private void onConsult() {
        Act act = IMObjectHelper.reload(getObject());
        // make sure the act is still available and has a valid status prior to
        // beginning workflow
        if (act != null && canCheckoutOrConsult(act)) {
            ConsultWorkflow workflow = new ConsultWorkflow(act);
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
     * Invoked when the 'check-out' button is pressed.
     */
    private void onCheckOut() {
        Act act = IMObjectHelper.reload(getObject());
        // make sure the act is still available and has a valid status prior
        // to beginning workflow
        if (act != null && canCheckoutOrConsult(act)) {
            CheckOutWorkflow workflow = new CheckOutWorkflow(act);
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
     * Invoked when the 'over-the-counter' button is pressed.
     */
    private void onOverTheCounter() {
        Workflow workflow = new OverTheCounterWorkflow();
        workflow.start();
    }

}
