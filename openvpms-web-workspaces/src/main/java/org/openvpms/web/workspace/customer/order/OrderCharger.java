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

package org.openvpms.web.workspace.customer.order;

import org.openvpms.archetype.rules.finance.order.OrderArchetypes;
import org.openvpms.archetype.rules.finance.order.OrderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.charge.AbstractCustomerChargeActEditor;

import java.util.ArrayList;
import java.util.List;

/**
 * Charges orders.
 *
 * @author Tim Anderson
 */
public class OrderCharger {

    /**
     * The customer to query orders for.
     */
    private final Party customer;

    /**
     * The patient to query orders for.
     */
    private final Party patient;

    /**
     * The customer order rules.
     */
    private final OrderRules rules;

    /**
     * The current context.
     */
    private final Context context;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * The orders that have been charged, but not saved.
     */
    private List<Act> charged = new ArrayList<Act>();

    public OrderCharger(Party customer, OrderRules rules, Context context, HelpContext help) {
        this(customer, null, rules, context, help);
    }

    public OrderCharger(Party customer, Party patient, OrderRules rules, Context context, HelpContext help) {
        this.customer = customer;
        this.patient = patient;
        this.rules = rules;
        this.context = context;
        this.help = help;
    }

    public boolean hasOrders() {
        return rules.hasOrders(customer, patient);
    }

    /**
     * Saves any charged orders.
     *
     * @return {@code true} if the orders were successfully saved
     */
    public boolean save() {
        return charged.isEmpty() || SaveHelper.save(charged);
    }

    public Party getCustomer() {
        return customer;
    }

    public void clear() {
        charged.clear();
    }

    public void charge(final AbstractCustomerChargeActEditor editor) {
        PendingOrderQuery query = new PendingOrderQuery(customer, null, charged);
        ResultSet<Act> set = query.query();
        if (!set.hasNext()) {
            if (charged.isEmpty()) {
                InformationDialog.show(Messages.format("customer.order.none", customer.getName()));
            } else {
                InformationDialog.show(Messages.format("customer.order.unsaved", customer.getName()));
            }
        } else {
            final PendingOrderBrowser browser = new PendingOrderBrowser(query, new DefaultLayoutContext(context, help));
            browser.query();
            PendingOrderDialog dialog = new PendingOrderDialog(Messages.get("customer.order.invoice.title"),
                                                               browser, help);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    List<Act> orders = browser.getOrders();
                    charge(orders, editor);
                }
            });
            dialog.show();
        }
    }

    private void charge(List<Act> orders, AbstractCustomerChargeActEditor editor) {
        int invalid = 0;
        for (Act order : orders) {
            if (TypeHelper.isA(order, OrderArchetypes.PHARMACY_ORDER)) {
                PharmacyOrderCharger charger = new PharmacyOrderCharger(order, rules);
                if (charger.isValid()) {
                    if (patient != null && charger.canCharge(patient)) {
                        charger.charge(editor);
                        charged.add(order);
                    } else {
                        ++invalid;
                    }
                } else {
                    ++invalid;
                }
            }
        }
        if (invalid != 0) {
            InformationDialog.show(Messages.format("customer.order.incomplete", customer.getName(), invalid));
        }
    }
}
