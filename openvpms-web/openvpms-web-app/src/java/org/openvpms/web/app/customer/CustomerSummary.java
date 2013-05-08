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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.customer;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.GridLayoutData;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.AccountType;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.app.alert.Alert;
import org.openvpms.web.app.alert.AlertSummary;
import org.openvpms.web.app.customer.note.CustomerAlertQuery;
import org.openvpms.web.component.im.sms.SMSDialog;
import org.openvpms.web.component.im.sms.SMSHelper;
import org.openvpms.web.app.summary.PartySummary;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.component.im.contact.ContactHelper;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.mail.MailDialog;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * Renders customer summary information.
 *
 * @author Tim Anderson
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
     * Constructs a {@code CustomerSummary}.
     *
     * @param context the context
     * @param help    the help context
     */
    public CustomerSummary(Context context, HelpContext help) {
        super(context, help.topic("customer/summary"));
        IArchetypeService service = ServiceHelper.getArchetypeService();
        partyRules = new CustomerRules(service);
        accountRules = new CustomerAccountRules(service);
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
                                                                           party.getName(), true, getContext());
        customerName.setStyleName("hyperlink-bold");
        column.add(RowFactory.create("Inset.Small",
                                     customerName.getComponent()));
        Label phone = LabelFactory.create();
        phone.setText(partyRules.getTelephone(party));
        column.add(RowFactory.create("Inset.Small", phone));

        Contact email = ContactHelper.getPreferredEmail(party);
        if (email != null) {
            column.add(RowFactory.create("Inset.Small", getEmail(email)));
        }

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
        Column result = ColumnFactory.create("PartySummary", column);
        final Context context = getContext();
        if (SMSHelper.isSMSEnabled(context.getPractice())) {
            final List<Contact> contacts = ContactHelper.getSMSContacts(party);
            if (!contacts.isEmpty()) {
                Context local = new LocalContext(context);
                local.setCustomer(party);
                Button button = ButtonFactory.create("button.sms.send", new ActionListener() {
                    public void onAction(ActionEvent event) {
                        SMSDialog dialog = new SMSDialog(contacts, context, getHelpContext().subtopic("sms"));
                        dialog.show();
                    }
                });
                result.add(RowFactory.create("Inset.Small", button));
            }
        }

        return result;
    }

    /**
     * Returns the alerts for a party.
     *
     * @param party the party
     * @return the party's alerts
     */
    protected List<Alert> getAlerts(Party party) {
        List<Alert> result = queryAlerts(party);
        Lookup accountTypeLookup = partyRules.getAccountType(party);
        if (accountTypeLookup != null) {
            AccountType accountType = new AccountType(accountTypeLookup);
            Lookup alertLookup = accountType.getAlert();
            if (alertLookup != null) {
                result.add(new Alert(alertLookup));
            }
        }
        return result;
    }

    /**
     * Returns outstanding alerts for a party.
     *
     * @param party    the party
     * @param pageSize the no. of alerts to return per page
     * @return the set of outstanding alerts for the party
     */
    protected ResultSet<Act> createAlertsResultSet(Party party, int pageSize) {
        CustomerAlertQuery query = new CustomerAlertQuery(party, true);
        query.setStatus(ActStatus.IN_PROGRESS);
        return query.query();
    }

    /**
     * Returns a button to launch an {@link MailDialog} for a customer.
     *
     * @param email the preferred email
     * @return a new button to launch the dialog
     */
    private Component getEmail(final Contact email) {
        Button mail = ButtonFactory.create(null, "hyperlink", new ActionListener() {
            public void onAction(ActionEvent event) {
                Context context = getContext();
                HelpContext mail = getHelpContext().topic("customer/email");
                MailContext mailContext = new CustomerMailContext(context, mail);
                MailDialog dialog = new MailDialog(mailContext, email, context, mail);
                dialog.show();
            }
        });
        mail.setText(ContactHelper.getEmail(email));
        return mail;
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
