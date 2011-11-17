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

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.GridLayoutData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.finance.account.AccountType;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.app.alert.Alert;
import org.openvpms.web.app.alert.AlertSummary;
import org.openvpms.web.app.customer.note.CustomerAlertQuery;
import org.openvpms.web.app.sms.SMSDialog;
import org.openvpms.web.app.summary.PartySummary;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.mail.MailDialog;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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
     * The context.
     */
    private Context context;


    /**
     * Constructs a <tt>CustomerSummary</tt>.
     *
     * @param context the context
     */
    public CustomerSummary(Context context) {
        partyRules = new CustomerRules();
        accountRules = new CustomerAccountRules();
        this.context = context;
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

        final List<Contact> mailto = getEmailContacts(party);
        if (!mailto.isEmpty()) {
            column.add(RowFactory.create("Inset.Small", getEmail(mailto)));
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
        final String[] mobiles = getPhonesForSMS(party);
        Button sms = null;
        if (mobiles.length != 0) {
            Context local = new LocalContext(context);
            local.setCustomer(party);
            sms = ButtonFactory.create("button.sms.send", new ActionListener() {
                public void onAction(ActionEvent event) {
                    SMSDialog dialog = new SMSDialog(mobiles, context);
                    dialog.show();
                }
            });
        }
        if (sms != null) {
            Row row = RowFactory.create("CellSpacing");
            row.add(sms);
            result.add(RowFactory.create("Inset.Small", row));
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
        return new CustomerAlertQuery(party).query();
    }

    /**
     * Returns phone numbers that are flagged for SMS messaging.
     * <p/>
     * The preferred no.s are at the head of the list
     *
     * @param party the party
     * @return a list of phone numbers
     */
    private String[] getPhonesForSMS(Party party) {
        List<Contact> contacts = getContacts(party, new SMSPredicate(), SMSPredicate.TELEPHONE_NUMBER);
        List<String> result = new ArrayList<String>();
        for (Contact contact : contacts) {
            IMObjectBean bean = new IMObjectBean(contact);
            result.add(bean.getString(SMSPredicate.TELEPHONE_NUMBER));
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Returns a button to launch an {@link MailDialog} for a list of email contacts.
     *
     * @param contacts the email contacts
     * @return a new button to launch the dialog
     */
    private Component getEmail(final List<Contact> contacts) {
        Button mail = ButtonFactory.create(null, "hyperlink", new ActionListener() {
            public void onAction(ActionEvent event) {
                MailDialog dialog = new MailDialog(Messages.get("mail.write"), context.getPractice(), contacts);
                dialog.show();
            }
        });
        IMObjectBean bean = new IMObjectBean(contacts.get(0));
        mail.setText(bean.getString("emailAddress"));
        return mail;
    }

    /**
     * Returns email contacts for a party.
     *
     * @param party the party
     * @return the email contacts
     */
    private List<Contact> getEmailContacts(Party party) {
        return getContacts(party, new EmailPredicate(), "emailAddress");
    }

    /**
     * Returns contacts for the specified party that match the predicate.
     *
     * @param party     the party
     * @param predicate the predicate
     * @param sortNode  the node to sort on
     * @return the matching contacts
     */
    private List<Contact> getContacts(Party party, Predicate predicate, String sortNode) {
        List<Contact> result = new ArrayList<Contact>();
        CollectionUtils.select(party.getContacts(), predicate, result);
        if (result.size() > 1) {
            SortConstraint[] sort = {new NodeSortConstraint("preferred", true), new NodeSortConstraint(sortNode, true)};
            IMObjectSorter.sort(result, sort);
        }
        return result;
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

    private static class SMSPredicate implements Predicate {

        /**
         * The telephone number node.
         */
        private static final String TELEPHONE_NUMBER = "telephoneNumber";

        /**
         * Use the specified parameter to perform a test that returns true or false.
         *
         * @param object the object to evaluate, should not be changed
         * @return true or false
         */
        public boolean evaluate(Object object) {
            boolean result = false;
            Contact contact = (Contact) object;
            if (TypeHelper.isA(contact, ContactArchetypes.PHONE)) {
                IMObjectBean bean = new IMObjectBean(contact);
                if (bean.getBoolean("sms")) {
                    String phone = bean.getString(TELEPHONE_NUMBER);
                    if (!StringUtils.isEmpty(phone)) {
                        result = true;
                    }
                }
            }
            return result;
        }
    }

    private static class EmailPredicate implements Predicate {

        /**
         * The email address node.
         */
        private static final String EMAIL_ADDRESS = "emailAddress";

        /**
         * Use the specified parameter to perform a test that returns true or false.
         *
         * @param object the object to evaluate, should not be changed
         * @return true or false
         */
        public boolean evaluate(Object object) {
            boolean result = false;
            Contact contact = (Contact) object;
            if (TypeHelper.isA(contact, ContactArchetypes.EMAIL)) {
                IMObjectBean bean = new IMObjectBean(contact);
                if (!StringUtils.isEmpty(bean.getString(EMAIL_ADDRESS))) {
                    result = true;
                }
            }
            return result;
        }

    }

}
