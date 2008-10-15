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

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.layout.GridLayoutData;
import org.openvpms.archetype.rules.finance.account.AccountType;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.ParticipationConstraint;
import static org.openvpms.component.system.common.query.ParticipationConstraint.Field.ActShortName;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.im.view.TableComponentFactory;
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
            IMObjectReferenceViewer customerName
                    = new IMObjectReferenceViewer(customer.getObjectReference(),
                                                  customer.getName(), true);
            customerName.setStyleName("hyperlink-bold");
            result.add(RowFactory.create("Inset.Small",
                                         customerName.getComponent()));

            Label alertTitle = LabelFactory.create("customer.alerts");
            Component alert;
            final AccountType type = getAccountType(customer);
            final List<Act> notes = getAlertNotes(customer);
            if ((type != null && type.showAlert()) || !notes.isEmpty()) {
                alert = ButtonFactory.create(
                        null, "alert", new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        new AlertDialog(type, notes);
                    }
                });
                alert = RowFactory.create(alert);
            } else {
                alert = LabelFactory.create("customer.noalerts");
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
     * Creates a new label for a numeric value, to be right aligned in a cell.
     *
     * @param value the value
     * @return a new label
     */
    private static Label create(BigDecimal value) {
        return LabelFactory.create(value, new GridLayoutData());
    }

    /**
     * Returns any <em>act.customerNotes</em> that are alerts for the customer.
     *
     * @param customer the customer
     * @return a list of notes. May be empty.
     */
    private static List<Act> getAlertNotes(Party customer) {
        List<Act> notes = new ArrayList<Act>();
        ArchetypeQuery query = new ArchetypeQuery("act.customerNote", false,
                                                  true);
        ParticipantConstraint participant
                = new ParticipantConstraint("customer",
                                            "participation.customer",
                                            customer);
        participant.add(new ParticipationConstraint(ActShortName,
                                                    "act.customerNote"));
        query.add(participant);
        IMObjectQueryIterator<Act> iter = new IMObjectQueryIterator<Act>(query);
        while (iter.hasNext()) {
            Act note = iter.next();
            IMObjectBean bean = new IMObjectBean(note);
            if (bean.getBoolean("alert")) {
                notes.add(note);
            }
        }
        return notes;
    }

    /**
     * Returns the account type of a customer.
     *
     * @param customer the customer
     * @return the account type, or <tt>null</tt> if none is found
     */
    private static AccountType getAccountType(Party customer) {
        CustomerRules rules = new CustomerRules();
        Lookup lookup = rules.getAccountType(customer);
        return (lookup != null) ? new AccountType(lookup) : null;
    }

    /**
     * Displays customer alerts.
     *
     * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
     * @version $LastChangedDate$
     */
    private static class AlertDialog extends PopupDialog {

        /**
         * Construct a new <tt>AlertDialog</tt>.
         */
        public AlertDialog(AccountType type, List<Act> notes) {
            super(Messages.get("customer.alert.title"),
                  "CustomerSummary.AlertDialog", OK);
            setModal(true);
            Column column = ColumnFactory.create("WideCellSpacing");
            if (type != null && type.showAlert()) {
                String msg = Messages.get("customer.alert.accounttype",
                                          type.getName());
                Label label = LabelFactory.create();
                label.setText(msg);
                column.add(label);
            }
            if (!notes.isEmpty()) {
                Column noteCol = ColumnFactory.create("CellSpacing");
                noteCol.add(LabelFactory.create("customer.alert.notes"));
                IMObjectListResultSet<Act> acts
                        = new IMObjectListResultSet<Act>(notes, 20);
                IMObjectTableModel<Act> model
                        = IMObjectTableModelFactory.create(
                        new String[]{"act.customerNote"},
                        createLayoutContext());
                PagedIMTable<Act> table = new PagedIMTable<Act>(model, acts);
                noteCol.add(table);
                column.add(noteCol);
            }
            getLayout().add(ColumnFactory.create("Inset", column));
            show();
        }

        /**
         * Helper to create a layout context where hyperlinks are disabled.
         *
         * @return a new layout context
         */
        private LayoutContext createLayoutContext() {
            LayoutContext context = new DefaultLayoutContext();
            context.setEdit(true); // disable hyerlinks
            TableComponentFactory factory = new TableComponentFactory(context);
            context.setComponentFactory(factory);
            return context;
        }
    }

}
