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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.order.OrderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActEditDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.order.OrderCharger;

import java.util.List;


/**
 * An edit dialog for {@link CustomerChargeActEditor} editors.
 * <p/>
 * This performs printing of unprinted documents that have their <em>interactive</em> flag set to {@code true}
 * when <em>Apply</em> or <em>OK</em> is pressed.
 *
 * @author Tim Anderson
 */
public class CustomerChargeActEditDialog extends ActEditDialog {

    /**
     * The message and editor container.
     */
    private Column container;

    /**
     * Manages charging orders and returns.
     */
    private final OrderChargeManager manager;

    /**
     * Determines if customer orders are automatically charged.
     */
    private final boolean autoChargeOrders;

    /**
     * Completed button identifier.
     */
    private static final String COMPLETED_ID = "button.completed";

    /**
     * In Progress button identifier.
     */
    private static final String IN_PROGRESS_ID = "button.inprogress";

    /**
     * Invoice orders button identifier.
     */
    private static final String INVOICE_ORDERS_ID = "button.invoiceOrders";


    /**
     * Constructs a {@link CustomerChargeActEditDialog}.
     *
     * @param editor  the editor
     * @param context the context
     */
    public CustomerChargeActEditDialog(CustomerChargeActEditor editor, Context context) {
        this(editor, null, context, true);
    }

    /**
     * Constructs a {@link CustomerChargeActEditDialog}.
     *
     * @param editor           the editor
     * @param charger          the order charger. May be {@code null}
     * @param context          the context
     * @param autoChargeOrders if {@code true}, automatically charge customer orders if they are complete
     */
    public CustomerChargeActEditDialog(CustomerChargeActEditor editor, OrderCharger charger, Context context,
                                       boolean autoChargeOrders) {
        super(editor, context);
        addButton(COMPLETED_ID);
        addButton(IN_PROGRESS_ID);
        addButton(INVOICE_ORDERS_ID);
        setDefaultCloseAction(CANCEL_ID);
        OrderRules rules = ServiceHelper.getBean(OrderRules.class);
        if (charger == null) {
            HelpContext help = editor.getHelpContext().subtopic("order");
            charger = new OrderCharger(getContext().getCustomer(), rules, context, help);
        }
        this.autoChargeOrders = autoChargeOrders;
        manager = new OrderChargeManager(charger, getEditorContainer());
    }

    /**
     * Show the window.
     */
    @Override
    public void show() {
        super.show();
        if (autoChargeOrders) {
            manager.charge(getEditor());
        } else {
            manager.check();
        }
    }

    /**
     * Returns the editor.
     *
     * @return the editor, or {@code null} if none has been set
     */
    @Override
    public CustomerChargeActEditor getEditor() {
        return (CustomerChargeActEditor) super.getEditor();
    }

    /**
     * Saves the current object.
     * <p/>
     * Any documents added as part of the save that have a template with an IMMEDIATE print mode will be printed.
     */
    @Override
    protected void onOK() {
        CustomerChargeDocuments docs = new CustomerChargeDocuments(getEditor(), getHelpContext());
        List<Act> existing = docs.getUnprinted();
        if (save()) {
            ActionListener printListener = new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    close(OK_ID);
                }
            };
            if (!docs.printNew(existing, printListener)) {
                // nothing to print, so close now
                close(OK_ID);
            }
        } else {
            manager.check();
        }
    }

    /**
     * Saves the current object.
     * <p/>
     * Any documents added as part of the save that have a template with an IMMEDIATE print mode will be printed.
     */
    @Override
    protected void onApply() {
        CustomerChargeDocuments docs = new CustomerChargeDocuments(getEditor(), getHelpContext());
        List<Act> existing = docs.getUnprinted();
        if (save()) {
            docs.printNew(existing, null);
        }
        manager.check();
    }

    /**
     * Saves the current object.
     *
     * @return {@code true} if the object was saved
     */
    @Override
    protected boolean doSave() {
        boolean result = super.doSave();
        if (result) {
            manager.clear();
        }
        return result;
    }

    /**
     * Invoked when the editor is saved, to allow subclasses to participate in the save transaction.
     *
     * @param editor the editor
     * @return {@code true} if the save was successful
     */
    @Override
    protected boolean saved(IMObjectEditor editor) {
        return manager.save();
    }

    /**
     * Invoked when a button is pressed. This delegates to the appropriate
     * on*() method for the button if it is known, else sets the action to
     * the button identifier and closes the window.
     *
     * @param button the button identifier
     */
    @Override
    protected void onButton(String button) {
        if (IN_PROGRESS_ID.equals(button)) {
            onInProgress();
        } else if (COMPLETED_ID.equals(button)) {
            onCompleted();
        } else if (INVOICE_ORDERS_ID.equals(button)) {
            chargeOrders();
        } else {
            super.onButton(button);
        }
    }

    /**
     * Sets the component.
     *
     * @param component the component
     * @param group     the focus group
     * @param context   the help context
     */
    @Override
    protected void setComponent(Component component, FocusGroup group, HelpContext context) {
        Column container = getEditorContainer();
        container.add(component);
        super.setComponent(container, group, context);
    }

    /**
     * Removes the component.
     *
     * @param component the component
     * @param group     the focus group
     */
    @Override
    protected void removeComponent(Component component, FocusGroup group) {
        Column container = getEditorContainer();
        container.removeAll();
        super.removeComponent(container, group);
    }

    /**
     * Invoked when the 'In Progress' button is pressed.
     * <p/>
     * If the act hasn't been POSTED, then this sets the status to IN_PROGRESS, and attempts to save and close the
     * dialog.
     */
    private void onInProgress() {
        if (!isPosted()) {
            CustomerChargeActEditor editor = getEditor();
            editor.setStatus(ActStatus.IN_PROGRESS);
            onOK();
        }
    }

    /**
     * Invoked when the 'Completed' button is pressed.
     * <p/>
     * If the act hasn't been POSTED, then this sets the status to COMPLETED, and attempts to save and close the
     * dialog.
     */
    private void onCompleted() {
        if (!isPosted()) {
            CustomerChargeActEditor editor = getEditor();
            editor.setStatus(ActStatus.COMPLETED);
            onOK();
        }
    }

    /**
     * Charges orders.
     */
    private void chargeOrders() {
        if (!isPosted()) {
            manager.chargeSelected(getEditor());
        }
    }

    /**
     * Returns the message and editor container.
     *
     * @return the message and editor container
     */
    private Column getEditorContainer() {
        if (container == null) {
            container = new Column();
        }
        return container;
    }

}