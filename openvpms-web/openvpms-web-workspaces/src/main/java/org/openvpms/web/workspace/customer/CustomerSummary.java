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

package org.openvpms.web.workspace.customer;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.GridLayoutData;
import nextapp.echo2.app.layout.RowLayoutData;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.AccountType;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.prefs.PreferenceArchetypes;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextApplicationInstance;
import org.openvpms.web.component.app.ContextHelper;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.contact.ContactHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.query.ResultSetIterator;
import org.openvpms.web.component.im.sms.SMSDialog;
import org.openvpms.web.component.im.sms.SMSHelper;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.mail.MailDialog;
import org.openvpms.web.component.mail.MailDialogFactory;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.alert.Alert;
import org.openvpms.web.workspace.alert.AlertSummary;
import org.openvpms.web.workspace.customer.communication.CommunicationArchetypes;
import org.openvpms.web.workspace.customer.communication.CustomerAlertQuery;
import org.openvpms.web.workspace.summary.PartySummary;

import java.math.BigDecimal;
import java.util.ArrayList;
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
     * Constructs a {@link CustomerSummary}.
     *
     * @param context     the context
     * @param help        the help context
     * @param preferences the preferences
     */
    public CustomerSummary(Context context, HelpContext help, Preferences preferences) {
        super(context, help.topic("customer/summary"), preferences);
        partyRules = ServiceHelper.getBean(CustomerRules.class);
        accountRules = ServiceHelper.getBean(CustomerAccountRules.class);
    }

    /**
     * Returns summary information for a party.
     * <p>
     * The summary includes any alerts.
     *
     * @param party the party
     * @return a summary component
     */
    @Override
    protected Component createSummary(Party party) {
        Component column = ColumnFactory.create();
        Component customerName = getCustomerName(party);
        column.add(ColumnFactory.create(Styles.SMALL_INSET, customerName));
        Component customerId = getCustomerId(party);
        column.add(ColumnFactory.create(Styles.SMALL_INSET, customerId));
        Component phone = getCustomerPhone(party);
        column.add(ColumnFactory.create(Styles.SMALL_INSET, phone));

        Contact email = ContactHelper.getPreferredEmail(party);
        column.add(ColumnFactory.create(Styles.SMALL_INSET, getEmail(email)));
        final Context context = getContext();
        if (getPreferences().getBoolean(PreferenceArchetypes.SUMMARY, "showCustomerAccount", true)) {
            Label balanceTitle = create("customer.account.balance");
            BigDecimal balance = accountRules.getBalance(party);
            Label balanceValue = create(balance);

            Label overdueTitle = create("customer.account.overdue");
            BigDecimal overdue = accountRules.getOverdueBalance(party, new Date());
            Label overdueValue = create(overdue);

            Label currentTitle = create("customer.account.current");
            BigDecimal current = balance.subtract(overdue);
            Label currentValue = create(current);

            Label unbilledTitle = create("customer.account.unbilled");
            BigDecimal unbilled = accountRules.getUnbilledAmount(party);
            Label unbilledValue = create(unbilled);

            Label effectiveTitle = create("customer.account.effective");
            BigDecimal effective = balance.add(unbilled);
            Label effectiveValue = create(effective);

            Grid grid = GridFactory.create(2, balanceTitle, balanceValue,
                                           overdueTitle, overdueValue,
                                           currentTitle, currentValue,
                                           unbilledTitle, unbilledValue,
                                           effectiveTitle, effectiveValue);
            column.add(grid);
        }
        AlertSummary alerts = getAlertSummary(party);
        if (alerts != null) {
            column.add(ColumnFactory.create(Styles.SMALL_INSET, alerts.getComponent()));
        }
        Column result = ColumnFactory.create("PartySummary", column);
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
                result.add(RowFactory.create(Styles.SMALL_INSET, button));
            }
        }

        return result;
    }

    /**
     * Returns a component representing the customer name.
     *
     * @param customer the customer
     * @return the customer name component
     */
    protected Component getCustomerName(Party customer) {
        IMObjectReferenceViewer viewer = new IMObjectReferenceViewer(customer.getObjectReference(),
                                                                     customer.getName(), true, getContext());
        viewer.setStyleName("hyperlink-bold");
        return viewer.getComponent();
    }

    /**
     * Returns a component representing the customer identifier.
     *
     * @param customer the customer
     * @return the customer identifier component
     */
    protected Component getCustomerId(final Party customer) {
        Component customerId = createLabel("customer.id", customer.getId());
        Button communication = ButtonFactory.create(null, "button.communication", new ActionListener() {
            public void onAction(ActionEvent event) {
                ContextApplicationInstance instance = ContextApplicationInstance.getInstance();
                ContextHelper.setCustomer(instance.getContext(), customer);
                instance.switchTo(CommunicationArchetypes.ACTS);
            }
        });
        Row right = RowFactory.create(communication);

        RowLayoutData rightLayout = new RowLayoutData();
        rightLayout.setAlignment(Alignment.ALIGN_RIGHT);
        rightLayout.setWidth(Styles.FULL_WIDTH);
        right.setLayoutData(rightLayout);

        return RowFactory.create(Styles.WIDE_CELL_SPACING, customerId, right);
    }

    /**
     * Returns a component representing the customer phone contact.
     *
     * @param customer the customer
     * @return the customer identifier component
     */
    protected Label getCustomerPhone(Party customer) {
        Label phone = LabelFactory.create();
        phone.setText(partyRules.getTelephone(customer, true));
        return phone;
    }

    /**
     * Returns the alerts for a party.
     *
     * @param party the party
     * @return the party's alerts
     */
    @Override
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
     * Returns the alerts for a party.
     *
     * @param party the party
     * @return the party's alerts
     */
    protected List<Alert> queryAlerts(Party party) {
        List<Alert> result = new ArrayList<>();
        ResultSet<Act> set = createAlertsResultSet(party, 20);
        ResultSetIterator<Act> iterator = new ResultSetIterator<>(set);
        while (iterator.hasNext()) {
            Act act = iterator.next();
            Lookup lookup = ServiceHelper.getLookupService().getLookup(act, "alertType");
            if (lookup != null) {
                result.add(new Alert(lookup, act));
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
     * <p/>
     * If the customer has no email address, displays 'No email', but still allows emails to be sent.
     *
     * @param email the preferred email. May be {@code null}
     * @return a new button to launch the dialog
     */
    private Component getEmail(final Contact email) {
        Button mail = ButtonFactory.create(null, "hyperlink", new ActionListener() {
            public void onAction(ActionEvent event) {
                Context context = getContext();
                HelpContext help = getHelpContext().topic("customer/email");
                MailContext mailContext = new CustomerMailContext(context, help);
                MailDialogFactory factory = ServiceHelper.getBean(MailDialogFactory.class);
                MailDialog dialog = factory.create(mailContext, email, new DefaultLayoutContext(context, help));
                dialog.show();
            }
        });
        String address = (email != null) ? ContactHelper.getEmail(email) : Messages.get("customer.email.none");
        mail.setText(address);
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
