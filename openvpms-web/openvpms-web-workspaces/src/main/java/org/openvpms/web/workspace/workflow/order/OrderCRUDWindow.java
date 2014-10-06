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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.order;

import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.finance.order.OrderArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.workspace.ResultSetCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditDialog;
import org.openvpms.web.workspace.customer.order.PharmacyOrderCharger;

/**
 * CRUD window for customer orders.
 *
 * @author Tim Anderson
 */
public class OrderCRUDWindow extends ResultSetCRUDWindow<Act> {

    /**
     * Post button identifier.
     */
    private static final String POST_ID = "button.post";

    /**
     * Invoice button identifier.
     */
    private static final String INVOICE_ID = "button.invoice";

    /**
     * Constructs a {@link ResultSetCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create instances of
     * @param query      the query. May be {@code null}
     * @param set        the result set. May be {@code null}
     * @param context    the context
     * @param help       the help context
     */
    public OrderCRUDWindow(Archetypes<Act> archetypes, Query<Act> query, ResultSet<Act> set, Context context,
                           HelpContext help) {
        super(archetypes, OrderActions.INSTANCE, query, set, context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(POST_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onPost();
            }
        });
        buttons.add(INVOICE_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onInvoice();
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
        OrderActions actions = getActions();
        Act order = getObject();
        buttons.setEnabled(VIEW_ID, enable);
        buttons.setEnabled(EDIT_ID, enable && actions.canEdit(order));
        buttons.setEnabled(DELETE_ID, enable && actions.canDelete(order));
        buttons.setEnabled(POST_ID, enable && actions.canPost(order));
        buttons.setEnabled(INVOICE_ID, enable && actions.canInvoice(order));
    }

    /**
     * Invoked when the 'post' button is pressed.
     */
    protected void onPost() {
        final Act act = IMObjectHelper.reload(getObject()); // make sure we have the latest version
        if (act != null) {
            OrderActions actions = getActions();
            if (actions.canPost(act)) {
                actions.post(act);
            }
        } else {
            ErrorDialog.show(Messages.format("imobject.noexist", getArchetypes().getDisplayName()));
        }
    }

    /**
     * Invoked when the 'invoice' button is pressed.
     */
    protected void onInvoice() {
        final Act act = IMObjectHelper.reload(getObject()); // make sure we have the latest version
        if (act != null) {
            if (getActions().canInvoice(act)) {
                charge(act);
            }
        } else {
            ErrorDialog.show(Messages.format("imobject.noexist", getArchetypes().getDisplayName()));
        }
    }

    /**
     * Determines the actions that may be performed on the selected object.
     *
     * @return the actions
     */
    @Override
    protected OrderActions getActions() {
        return (OrderActions) super.getActions();
    }

    /**
     * Charges out an order or return to the customer.
     *
     * @param act the order/return
     */
    protected void charge(final Act act) {
        final FinancialAct charge = getCharge(act);
        if (charge != null) {
            String displayName = DescriptorHelper.getDisplayName(charge);
            String title = Messages.format("customer.order.existingcharge.title", displayName);
            String message = Messages.format("customer.order.existingcharge.message", displayName,
                                             DescriptorHelper.getDisplayName(act));
            ConfirmationDialog dialog = new ConfirmationDialog(title, message);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    charge(act, charge);
                }
            });
            dialog.show();
        } else {
            charge(act, charge);
        }
    }

    /**
     * Charges an order or return.
     *
     * @param act    the order/return
     * @param charge the charge to add items to. If {@code null}, one will be created
     */
    private void charge(final Act act, FinancialAct charge) {
        PharmacyOrderCharger charger = new PharmacyOrderCharger(act);
        if (charger.isValid()) {
            String topic = TypeHelper.isA(act, OrderArchetypes.ORDERS) ?
                           CustomerAccountArchetypes.INVOICE : CustomerAccountArchetypes.CREDIT;
            HelpContext edit = getHelpContext().topic(topic + "/edit");
            CustomerChargeActEditDialog editor = charger.charge(charge,
                                                                new DefaultLayoutContext(true, getContext(), edit));
            editor.addWindowPaneListener(new WindowPaneListener() {
                public void onClose(WindowPaneEvent event) {
                    onRefresh(act);
                }
            });
        } else {
            InformationDialog.show(Messages.format("customer.order.invalid", DescriptorHelper.getDisplayName(act)));
        }
    }

    /**
     * Returns the most recent IN_PROGRESS or COMPLETED invoice to add order items to.
     *
     * @param act the order
     * @return the charge, or {@code null} if none exists
     */
    private FinancialAct getCharge(Act act) {
        FinancialAct result = null;
        ActBean bean = new ActBean(act);
        Party customer = (Party) bean.getNodeParticipant("customer");
        if (customer != null) {
            CustomerAccountRules rules = ServiceHelper.getBean(CustomerAccountRules.class);
            if (TypeHelper.isA(act, OrderArchetypes.ORDERS)) {
                result = rules.getInvoice(customer);
            } else {
                result = rules.getCredit(customer);
            }
        }
        return result;
    }

    private static class OrderActions extends ActActions<Act> {

        public static final OrderActions INSTANCE = new OrderActions();

        public boolean canInvoice(Act order) {
            return ActStatus.IN_PROGRESS.equals(order.getStatus());
        }
    }
}
