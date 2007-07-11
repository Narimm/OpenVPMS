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

package org.openvpms.web.app.customer;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.layout.GridLayoutData;
import org.openvpms.archetype.rules.finance.account.AccountType;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.dialog.InformationDialog;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.NumberFormatter;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * Renders customer summary information.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class CustomerSummary {

    /**
     * Returns summary information for a customer.
     *
     * @param customer the customer. May be <code>null</code>
     * @return a summary component, or <code>null</code> if there is no summary
     */
    public static Component getSummary(final Party customer) {
        Component result = null;
        if (customer != null) {
            result = ColumnFactory.create();
            Label customerName = LabelFactory.create(null, "Customer.Name");
            customerName.setText(customer.getName());
            result.add(RowFactory.create("Inset.Small", customerName));

            Label alertTitle = LabelFactory.create("customer.alerts");
            Component alert;
            if (!hasAlerts(customer)) {
                alert = LabelFactory.create("customer.noalerts");
            } else {
                alert = ButtonFactory.create(
                        null, "alert", new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        onShowAlerts(customer);
                    }
                });
                alert = RowFactory.create(alert);
            }

            CustomerAccountRules rules = new CustomerAccountRules();
            Label balanceTitle = create("customer.account.balance");
            BigDecimal balance = rules.getBalance(customer);
            Label balanceValue = create(balance);

            Label overdueTitle = create("customer.account.overdue");
            BigDecimal overdue = rules.getOverdueBalance(customer, new Date());
            Label overdueValue = create(overdue);

            Label currentTitle = create("customer.account.current");
            BigDecimal current = balance.subtract(overdue);
            Label currentValue = create(current);

            Label creditTitle = create("customer.account.credit");
            BigDecimal credit = rules.getCreditBalance(customer);
            Label creditValue = create(credit);

            Label unbilledTitle = create("customer.account.unbilled");
            BigDecimal unbilled = rules.getUnbilledAmount(customer);
            Label unbilledValue = create(unbilled);

            Grid grid = GridFactory.create(2, alertTitle, alert,
                                           balanceTitle, balanceValue,
                                           overdueTitle, overdueValue,
                                           currentTitle, currentValue,
                                           creditTitle, creditValue,
                                           unbilledTitle, unbilledValue);
            result.add(grid);
        }
        return result;
    }

    /**
     * Helper to create a label for the given key.
     *
     * @param key the key
     * @return a new label
     */
    private static Label create(String key) {
        return LabelFactory.create(key);
    }

    /**
     * Helper to create a label for a numeric value.
     *
     * @param value the value
     * @return a new label
     */
    private static Label create(BigDecimal value) {
        Label label = LabelFactory.create();
        label.setText(NumberFormatter.format(value));
        GridLayoutData layout = new GridLayoutData();
        layout.setAlignment(new Alignment(Alignment.RIGHT, Alignment.DEFAULT));
        label.setLayoutData(layout);
        return label;
    }

    /**
     * Determines if the customer has any alerts.
     *
     * @param customer the customer
     * @return <tt>true</tt> if the customer has any alerts
     */
    private static boolean hasAlerts(Party customer) {
        AccountType type = getAccountType(customer);
        return (type != null) && type.showAlert();
    }

    /**
     * Returns the account type of a customer.
     *
     * @param customer the customer
     * @return the account type, or <tt>null</tt> if none is found
     */
    private static AccountType getAccountType(Party customer) {
        IMObjectBean bean = new IMObjectBean(customer);
        List<Lookup> types = bean.getValues("type", Lookup.class);
        if (!types.isEmpty()) {
            return new AccountType(types.get(0));
        }
        return null;
    }

    /**
     * Invoked to display alerts for a customer.
     *
     * @param customer the customer
     */
    private static void onShowAlerts(final Party customer) {
        AccountType type = getAccountType(customer);
        if (type != null) {
            String msg = Messages.get("customer.accounttype.alert",
                                      type.getName());
            InformationDialog.show(msg);
        }
    }

}
