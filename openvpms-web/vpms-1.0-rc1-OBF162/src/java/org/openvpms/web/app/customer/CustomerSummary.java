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
import nextapp.echo2.app.layout.GridLayoutData;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.NumberFormatter;
import org.openvpms.web.component.util.RowFactory;

import java.math.BigDecimal;
import java.util.Date;


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
    public static Component getSummary(Party customer) {
        Component result = null;
        if (customer != null) {
            result = ColumnFactory.create();
            Label customerName = LabelFactory.create(null, "Customer.Name");
            customerName.setText(customer.getName());
            result.add(
                    RowFactory.create("Patient.Deceased.Inset", customerName));

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

            Grid grid = GridFactory.create(2, balanceTitle, balanceValue,
                                           overdueTitle, overdueValue,
                                           currentTitle, currentValue,
                                           creditTitle, creditValue,
                                           unbilledTitle, unbilledValue);
            result.add(grid);
        }
        return result;
    }

    private static Label create(String key) {
        return LabelFactory.create(key);
    }

    private static Label create(BigDecimal value) {
        Label label = LabelFactory.create();
        label.setText(NumberFormatter.format(value));
        GridLayoutData layout = new GridLayoutData();
        layout.setAlignment(new Alignment(Alignment.RIGHT, Alignment.DEFAULT));
        label.setLayoutData(layout);
        return label;
    }

}
