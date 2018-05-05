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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.scheduling;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.im.edit.IMObjectActions;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.DefaultTaskListener;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.Workflow;
import org.openvpms.web.component.workspace.AbstractCRUDWindow;
import org.openvpms.web.component.workspace.CRUDWindowListener;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.CustomerMailContext;
import org.openvpms.web.workspace.workflow.WorkflowFactory;
import org.openvpms.web.workspace.workflow.checkin.NewFlowSheetTask;
import org.openvpms.web.workspace.workflow.otc.OverTheCounterWorkflow;


/**
 * Schedule event CRUD window.
 *
 * @author Tim Anderson
 */
public abstract class ScheduleCRUDWindow extends AbstractCRUDWindow<Act> {

    /**
     * Consult button identifier.
     */
    protected static final String CONSULT_ID = "button.consult";

    /**
     * Check-out button identifier.
     */
    protected static final String CHECKOUT_ID = "button.checkout";

    /**
     * Over-the-counter button identifier.
     */
    protected static final String OVER_THE_COUNTER_ID = "button.OTC";

    /**
     * New flow sheet button identifier.
     */
    private static final String NEW_FLOW_SHEET_ID = "button.newflowsheet";

    /**
     * The Smart Flow Sheet service factory.
     */
    private final FlowSheetServiceFactory flowSheetServiceFactory;


    /**
     * Constructs a {@link ScheduleCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param actions    determines the operations that may be performed on the selected object. If {@code null},
     *                   actions should be registered via {@link #setActions(IMObjectActions)}
     * @param context    the context
     * @param help       the help context
     */
    public ScheduleCRUDWindow(Archetypes<Act> archetypes, ScheduleActions actions, Context context, HelpContext help) {
        super(archetypes, actions, context, help);
        this.flowSheetServiceFactory = ServiceHelper.getBean(FlowSheetServiceFactory.class);
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
     * Determines the actions that may be performed on the selected object.
     *
     * @return the actions
     */
    @Override
    protected ScheduleActions getActions() {
        return (ScheduleActions) super.getActions();
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
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        super.enableButtons(buttons, enable);
        boolean newFlowSheetEnabled = enable && getActions().canCreateFlowSheet(
                getObject(), getContext().getLocation(), flowSheetServiceFactory);
        buttons.setEnabled(NEW_FLOW_SHEET_ID, newFlowSheetEnabled);
        enablePrintPreview(buttons, enable);
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
     * Invoked if an object may not be deleted.
     *
     * @param act the act
     */
    protected void deleteDisallowed(Act act) {
        String name = getArchetypes().getDisplayName();
        String status = act.getStatus();
        String title = Messages.format("act.nodelete.title", name);
        String message = Messages.format("act.nodelete.message", name, status);
        ErrorDialog.show(title, message);
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

    protected Button createFlowSheetButton() {
        return ButtonFactory.create(NEW_FLOW_SHEET_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onNewFlowSheet();
            }
        });

    }

    /**
     * Invoked when the 'consult' button is pressed.
     */
    private void onConsult() {
        Act act = IMObjectHelper.reload(getObject());
        // make sure the act is still available and has a valid status prior to
        // beginning workflow
        if (act != null && getActions().canCheckoutOrConsult(act)) {
            WorkflowFactory factory = ServiceHelper.getBean(WorkflowFactory.class);
            Workflow workflow = factory.createConsultWorkflow(act, getContext(), getHelpContext());
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
        if (act != null && getActions().canCheckoutOrConsult(act)) {
            WorkflowFactory factory = ServiceHelper.getBean(WorkflowFactory.class);
            Workflow workflow = factory.createCheckOutWorkflow(act, getContext(), getHelpContext());
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

    /**
     * Creates a new Smart Flow sheet for the patient associated with the selected appointment/task.
     */
    private void onNewFlowSheet() {
        final Act object = IMObjectHelper.reload(getObject());
        Party location = getContext().getLocation();
        if (object != null && flowSheetServiceFactory.isSmartFlowSheetEnabled(location)) {
            NewFlowSheetTask task = new NewFlowSheetTask(object, location, flowSheetServiceFactory, getHelpContext());
            task.addTaskListener(new DefaultTaskListener() {
                @Override
                public void taskEvent(TaskEvent event) {
                    onRefresh(object);
                }
            });
            TaskContext context = new DefaultTaskContext(getContext(), getHelpContext());
            task.start(context);
        }
    }

    protected static abstract class ScheduleActions extends ActActions<Act> {

        /**
         * Default constructor.
         */
        protected ScheduleActions() {
        }

        /**
         * Determines if an act can be deleted.
         *
         * @param act the act to check
         * @return {@code true} if the act status isn't {@code POSTED} nor {@code COMPLETED}
         */
        @Override
        public boolean canDelete(Act act) {
            return super.canDelete(act) && !ActStatus.COMPLETED.equals(act.getStatus());
        }

        /**
         * Determines if a consultation or checkout can be performed on an act.
         *
         * @param act the act
         * @return {@code true} if consultation can be performed
         */
        public abstract boolean canCheckoutOrConsult(Act act);

        /**
         * Determines if a flow sheet can be created.
         *
         * @param act      the act
         * @param location the practice location
         * @param factory  the flow sheet service factory
         * @return {@code true} if a flow sheet can be created
         */
        public boolean canCreateFlowSheet(Act act, Party location, FlowSheetServiceFactory factory) {
            if (location != null && TypeHelper.isA(act, ScheduleArchetypes.APPOINTMENT, ScheduleArchetypes.TASK)
                && factory.isSmartFlowSheetEnabled(location)) {
                ActBean bean = new ActBean(act);
                return bean.getNodeParticipantRef("patient") != null;
            }
            return false;
        }

    }

}
