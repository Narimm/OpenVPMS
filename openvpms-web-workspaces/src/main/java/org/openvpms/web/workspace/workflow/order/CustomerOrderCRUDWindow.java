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
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.order.OrderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.print.BasicPrinterListener;
import org.openvpms.web.component.workspace.ResultSetCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.order.OrderCharger;

/**
 * CRUD window for customer orders.
 *
 * @author Tim Anderson
 */
public class CustomerOrderCRUDWindow extends ResultSetCRUDWindow<FinancialAct> {

    /**
     * Invoice button identifier.
     */
    private static final String INVOICE_ID = "button.invoice";

    /**
     * Constructs a {@link CustomerOrderCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create instances of
     * @param query      the query. May be {@code null}
     * @param set        the result set. May be {@code null}
     * @param context    the context
     * @param help       the help context
     */
    public CustomerOrderCRUDWindow(Archetypes<FinancialAct> archetypes, Query<FinancialAct> query,
                                   ResultSet<FinancialAct> set, Context context, HelpContext help) {
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
        buttons.add(INVOICE_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onInvoice();
            }
        });
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
        OrderActions actions = getActions();
        FinancialAct order = getObject();
        buttons.setEnabled(VIEW_ID, enable);
        buttons.setEnabled(EDIT_ID, enable && actions.canEdit(order));
        buttons.setEnabled(DELETE_ID, enable && actions.canDelete(order));
        buttons.setEnabled(INVOICE_ID, enable && actions.canInvoice(order));
        buttons.setEnabled(PRINT_ID, enable);
    }

    /**
     * Invoked when the 'invoice' button is pressed.
     */
    protected void onInvoice() {
        final FinancialAct act = IMObjectHelper.reload(getObject()); // make sure we have the latest version
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
    protected void charge(final FinancialAct act) {
        ActBean bean = new ActBean(act);
        Party customer = (Party) bean.getNodeParticipant("customer");
        if (customer != null) {
            OrderCharger charger = new OrderCharger(customer, ServiceHelper.getBean(OrderRules.class),
                                                    getContext(), getHelpContext().subtopic("order"));
            charger.charge(act, new OrderCharger.CompletionListener() {
                @Override
                public void completed() {
                    onRefresh(act);
                }
            });
        }
    }

    /**
     * Creates a new printer.
     *
     * @param object the object to print
     * @return an instance of {@link InteractiveIMPrinter}.
     * @throws OpenVPMSException for any error
     */
    @Override
    protected IMPrinter<FinancialAct> createPrinter(final FinancialAct object) {
        InteractiveIMPrinter<FinancialAct> printer = (InteractiveIMPrinter<FinancialAct>) super.createPrinter(object);
        printer.setListener(new BasicPrinterListener() {
            public void printed(String printer) {
                if (getActions().setPrinted(object)) {
                    onSaved(object, false);
                }
            }
        });
        return printer;
    }


    private static class OrderActions extends ActActions<FinancialAct> {

        public static final OrderActions INSTANCE = new OrderActions();

        public boolean canInvoice(Act order) {
            return ActStatus.IN_PROGRESS.equals(order.getStatus());
        }
    }

}
