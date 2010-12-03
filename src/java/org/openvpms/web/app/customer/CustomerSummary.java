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
 *  $Id: CustomerSummary.java 3487 2009-11-12 01:18:46Z tanderson $
 */

package org.openvpms.web.app.customer;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.layout.GridLayoutData;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.alert.AlertSummary;
import org.openvpms.web.app.alert.Alerts;
import org.openvpms.web.app.customer.note.CustomerAlertQuery;
import org.openvpms.web.app.summary.PartySummary;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;


/**
 * Renders customer summary information.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2009-11-12 12:18:46 +1100 (Thu, 12 Nov 2009) $
 */
public class CustomerSummary extends PartySummary {

    /**
     * The customer rules.
     */
    private final CustomerRules partyRules;

    /**
     * The account rules.
     */
    private CustomerAccountRules accountRules;


    /**
     * Constructs a <tt>CustomerSummary</tt>.
     */
    public CustomerSummary() {
        partyRules = new CustomerRules();
        accountRules = new CustomerAccountRules();
    }

    /**
     * Returns summary information for a party.
     * <p/>
     * The summary includes any alerts.
     *
     * @param party the party
     * @return a summary component
     */
    protected Component createSummary(Party party) {
        Component column = ColumnFactory.create();
        IMObjectReferenceViewer customerName = new IMObjectReferenceViewer(party.getObjectReference(),
                                                                           party.getName(), true);
        customerName.setStyleName("hyperlink-bold");
        column.add(RowFactory.create("Inset.Small",
                                     customerName.getComponent()));
        Label phone = LabelFactory.create();
        phone.setText(partyRules.getHomeTelephone(party));
        column.add(RowFactory.create("Inset.Small", phone));

        Label balanceTitle = create("customer.account.balance");
        BigDecimal balance = accountRules.getBalance(party);
        Label balanceValue = create(balance);

        Label overdueTitle = create("customer.account.overdue");
        BigDecimal overdue = accountRules.getOverdueBalance(party, new Date());
        Label overdueValue = create(overdue);

        Label currentTitle = create("customer.account.current");
        BigDecimal current = balance.subtract(overdue);
        Label currentValue = create(current);

        Label creditTitle = create("customer.account.credit");
        BigDecimal credit = accountRules.getCreditBalance(party);
        Label creditValue = create(credit);

        Label unbilledTitle = create("customer.account.unbilled");
        BigDecimal unbilled = accountRules.getUnbilledAmount(party);
        Label unbilledValue = create(unbilled);

        Grid grid = GridFactory.create(2, balanceTitle, balanceValue,
                                       overdueTitle, overdueValue,
                                       currentTitle, currentValue,
                                       creditTitle, creditValue,
                                       unbilledTitle, unbilledValue);
        column.add(grid);
        AlertSummary alerts = getAlertSummary(party);
        if (alerts != null) {
            grid.add(create("alerts.title"));
            column.add(ColumnFactory.create("Inset.Small", alerts.getComponent()));
        }
        return ColumnFactory.create("PartySummary", column);
    }

    /**
     * Returns the alerts for a party.
     *
     * @param party the party
     * @return the party's alerts
     */
    protected Collection<Alerts> getAlerts(Party party) {
        Map<String, Alerts> result = queryAlerts(party);
        Set<Lookup> lookups = partyRules.getAlerts(party);
        for (Lookup lookup : lookups) {
            Alerts alerts = result.get(lookup.getCode());
            if (alerts == null) {
                alerts = new Alerts(lookup);
                result.put(lookup.getCode(), alerts);
            }
        }
        return result.values();
    }

    /**
     * Returns outstanding alerts for a party.
     *
     * @param party    the party
     * @param pageSize the no. of alerts to return per page
     * @return the set of outstanding alerts for the party
     */
    protected ResultSet<Act> createAlertsResultSet(Party party, int pageSize) {
        return new CustomerAlertQuery(party).query();
    }

    /**
     * Helper to create a label for the given key.
     *
     * @param key the key
     * @return a new label
     */
    private Label create(String key) {
        return LabelFactory.create(key);
    }

    /**
     * Creates a new label for a numeric value, to be right aligned in a cell.
     *
     * @param value the value
     * @return a new label
     */
    private Label create(BigDecimal value) {
        return LabelFactory.create(value, new GridLayoutData());
    }

}
