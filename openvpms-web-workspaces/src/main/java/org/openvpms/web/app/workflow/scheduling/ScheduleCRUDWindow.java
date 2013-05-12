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
*/

package org.openvpms.web.app.workflow.scheduling;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.app.customer.CustomerMailContext;
import org.openvpms.web.app.workflow.checkout.CheckOutWorkflow;
import org.openvpms.web.app.workflow.consult.ConsultWorkflow;
import org.openvpms.web.app.workflow.otc.OverTheCounterWorkflow;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.DefaultIMObjectActions;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.subsystem.AbstractCRUDWindow;
import org.openvpms.web.component.subsystem.CRUDWindowListener;
import org.openvpms.web.component.workflow.DefaultTaskListener;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.Workflow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Schedule event CRUD window.
 *
 * @author Tim Anderson
 */
public abstract class ScheduleCRUDWindow extends AbstractCRUDWindow<Act> {

    /**
     * Consult button identifier.
     */
    protected static final String CONSULT_ID = "consult";

    /**
     * Check-out button identifier.
     */
    protected static final String CHECKOUT_ID = "checkout";

    /**
     * Over-the-counter button identifier.
     */
    protected static final String OVER_THE_COUNTER_ID = "OTC";


    /**
     * Constructs a {@code ScheduleCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param context    the context
     * @param help       the help context
     */
    public ScheduleCRUDWindow(Archetypes<Act> archetypes, Context context, HelpContext help) {
        super(archetypes, DefaultIMObjectActions.<Act>getInstance(), context, help);
    }

    /**
     * Deletes the current object.
     */
    @Override
    public void delete() {
        Act act = getObject();
        if (!ActStatus.COMPLETED.equals(act.getStatus())) {
            super.delete();
        } else {
            String name = getArchetypeDescriptor().getDisplayName();
            String status = act.getStatus();
            String title = Messages.get("act.nodelete.title", name);
            String message = Messages.get("act.nodelete.message", name, status);
            ErrorDialog.show(title, message);
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
        buttons.add(createPrintButton());
    }

    /**
     * Returns the mail context.
     *
     * @return the mail context. May be {@code null}
     */
    @Override
    public MailContext getMailContext() {
        MailContext context = null;
        if (getObject() != null) {
            context = CustomerMailContext.create(getObject(), getContext(), getHelpContext());
        }
        if (context == null) {
            context = super.getMailContext();
        }
        return context;
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
        buttons.setEnabled(PRINT_ID, enable);
    }

    /**
     * Invoked when the object needs to be refreshed.
     * <p/>
     * This implementation updates the object and notifies any registered listener.
     *
     * @param object the object
     */
    @Override
    protected void onRefresh(Act object) {
        Act refreshed = IMObjectHelper.reload(object); // may be null
        setObject(refreshed);
        CRUDWindowListener<Act> listener = getListener();
        if (listener != null) {
            listener.refresh(object); // won't be null
        }
    }

    /**
     * Helper to create a new button with id {@link #CONSULT_ID} linked to {@link #onConsult()}.
     *
     * @return a new button
     */
    protected Button createConsultButton() {
        return ButtonFactory.create(CONSULT_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onConsult();
            }
        });
    }

    /**
     * Helper to create a new button with id {@link #CHECKOUT_ID} linked to {@link #onCheckOut()}.
     *
     * @return a new button
     */
    protected Button createCheckOutButton() {
        return ButtonFactory.create(CHECKOUT_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onCheckOut();
            }
        });
    }

    /**
     * Helper to create a new button with id {@link #OVER_THE_COUNTER_ID} linked to {@link #onOverTheCounter()}.
     *
     * @return a new button
     */
    protected Button createOverTheCounterButton() {
        return ButtonFactory.create(OVER_THE_COUNTER_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onOverTheCounter();
            }
        });
    }

    /**
     * Determines if a consulation or checkout can be performed on an act.
     *
     * @param act the act
     * @return {@code true} if consultation can be performed
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
            ConsultWorkflow workflow = new ConsultWorkflow(act, getContext(), getHelpContext());
            workflow.addTaskListener(new DefaultTaskListener() {
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
            CheckOutWorkflow workflow = new CheckOutWorkflow(act, getContext(), getHelpContext());
            workflow.addTaskListener(new DefaultTaskListener() {
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
        Workflow workflow = new OverTheCounterWorkflow(getContext(), getHelpContext());
        workflow.start();
    }

}
