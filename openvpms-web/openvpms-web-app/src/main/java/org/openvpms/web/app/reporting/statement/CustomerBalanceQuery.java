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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.reporting.statement;

import echopointng.DateChooser;
import echopointng.DateField;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.finance.account.CustomerBalanceSummaryQuery;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.list.AbstractListCellRenderer;
import org.openvpms.web.component.im.list.LookupListCellRenderer;
import org.openvpms.web.component.im.lookup.ArchetypeLookupQuery;
import org.openvpms.web.component.im.lookup.LookupField;
import org.openvpms.web.component.im.lookup.LookupFieldFactory;
import org.openvpms.web.component.im.query.AbstractArchetypeQuery;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.util.ComponentHelper;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.DateFieldFactory;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.echo.factory.TextComponentFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.focus.FocusHelper;
import org.openvpms.web.resource.i18n.Messages;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Customer balance query.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerBalanceQuery extends AbstractArchetypeQuery<ObjectSet> {

    /**
     * The account type selector.
     */
    private LookupField accountType;

    /**
     * The balance type list items.
     */
    private final String[] balanceTypeItems;

    /**
     * The balance type selector.
     */
    private SelectField balanceType;

    /**
     * Determines if credit balances should be excluded.
     */
    private CheckBox excludeCredit;

    /**
     * The processing date.
     */
    private DateField date;

    /**
     * The 'overdue period from' days label.
     */
    private Label periodFromLabel;

    /**
     * The 'overdue period from' days.
     */
    private TextField periodFrom;

    /**
     * The 'overdue period to' days label.
     */
    private Label periodToLabel;

    /**
     * The 'overdue period to' days.
     */
    private TextField periodTo;

    /**
     * The 'customer from' field.
     */
    private TextField customerFrom;

    /**
     * The 'customer to' field.
     */
    private TextField customerTo;

    /**
     * Index of the all balances balance type.
     */
    private static final int ALL_BALANCE_INDEX = 0;

    /**
     * Index of the overdue balance type.
     */
    private static final int OVERDUE_INDEX = 1;

    /**
     * Index of the non-overdue balance type.
     */
    private static final int NON_OVERDUE_INDEX = 2;


    /**
     * Constructs a new <tt>CustomerBalanceQuery</tt> .
     *
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public CustomerBalanceQuery() {
        super(new String[]{"party.customer*"}, ObjectSet.class);
        balanceTypeItems = new String[]{
            Messages.get("reporting.statements.balancetype.all"),
            Messages.get("reporting.statements.balancetype.overdue"),
            Messages.get("reporting.statements.balancetype.nonOverdue")
        };
    }

    /**
     * Refreshes the account types.
     */
    public void refreshAccountTypes() {
        if (accountType != null) {
            Lookup selected = accountType.getSelected();
            accountType.refresh();
            accountType.setSelected(selected);
        }
    }

    /**
     * Determines if customers with both overdue and non-overdue balances
     * are being queried.
     *
     * @return <tt>true</tt> if customers with both overdue and non-overdue
     *         balances are being queried.
     */
    public boolean queryAllBalances() {
        return balanceType.getSelectedIndex() == ALL_BALANCE_INDEX;
    }

    /**
     * Determines if customers with overdue balances are being queried.
     *
     * @return <tt>true</tt> if customers with overdue balances are being
     *         queried, <tt>false</tt> if customers with outstanding balances are being
     *         queried
     */
    public boolean queryOverduebalances() {
        return balanceType.getSelectedIndex() == OVERDUE_INDEX;
    }

    /**
     * Lays out the component in a container, and sets focus on the instance
     * name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        Grid grid = GridFactory.create(6);
        accountType = LookupFieldFactory.create(
            new ArchetypeLookupQuery("lookup.customerAccountType"),
            true);
        accountType.setSelected((Lookup) null);
        accountType.setCellRenderer(LookupListCellRenderer.INSTANCE);

        Label accountTypeLabel = LabelFactory.create(
            "reporting.statements.accountType");

        date = DateFieldFactory.create();
        date.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
            }
        });
        Label dateLabel = LabelFactory.create("reporting.statements.date");

        balanceType = SelectFieldFactory.create(balanceTypeItems);
        balanceType.setCellRenderer(new BalanceTypeListCellRenderer());
        balanceType.addActionListener(new ActionListener() {
            public void onAction(ActionEvent e) {
                onBalanceTypeChanged();
            }
        });
        Label balanceTypeLabel = LabelFactory.create(
            "reporting.statements.balancetypes");

        grid.add(accountTypeLabel);
        grid.add(accountType);
        grid.add(dateLabel);
        grid.add(date);
        grid.add(balanceType);
        grid.add(balanceTypeLabel);

        periodFromLabel = LabelFactory.create(
            "reporting.statements.periodFrom");
        periodFrom = TextComponentFactory.create();
        periodFrom.addPropertyChangeListener(
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent event) {
                }
            });

        periodToLabel = LabelFactory.create("reporting.statements.periodTo");
        periodTo = TextComponentFactory.create();
        periodTo.addPropertyChangeListener(
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent event) {
                }
            });

        Label excludeCreditLabel
            = LabelFactory.create("reporting.statements.excludeCredit");
        excludeCredit = CheckBoxFactory.create(true);

        grid.add(periodFromLabel);
        grid.add(periodFrom);
        grid.add(periodToLabel);
        grid.add(periodTo);
        grid.add(excludeCredit);
        grid.add(excludeCreditLabel);


        Label customerFromLabel = LabelFactory.create(
            "reporting.statements.customerFrom");
        customerFrom = TextComponentFactory.create();
        customerFrom.addPropertyChangeListener(
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent event) {
                }
            });

        Label customerToLabel = LabelFactory.create(
            "reporting.statements.customerTo");
        customerTo = TextComponentFactory.create();
        customerTo.addPropertyChangeListener(
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent event) {
                }
            });

        grid.add(customerFromLabel);
        grid.add(customerFrom);
        grid.add(customerToLabel);
        grid.add(customerTo);

        container.add(grid);

        FocusGroup group = getFocusGroup();
        group.add(accountType);
        group.add(date);
        group.add(balanceType);
        group.add(periodFrom);
        group.add(periodTo);
        group.add(excludeCredit);
        group.add(customerFrom);
        group.add(customerTo);

        FocusHelper.setFocus(getSearchField());
    }

    /**
     * Returns all objects matching the criteria.
     *
     * @return all objects matching the criteria
     */
    public List<ObjectSet> getObjects() {
        List<ObjectSet> sets = new ArrayList<ObjectSet>();
        try {
            CustomerBalanceSummaryQuery query;
            int selected = balanceType.getSelectedIndex();
            boolean nonOverdue = selected != OVERDUE_INDEX;
            boolean overdue = selected != NON_OVERDUE_INDEX;
            int from = overdue ? getNumber(periodFrom) : -1;
            int to = overdue ? getNumber(periodTo) : -1;
            boolean credit = excludeCredit.isSelected();
            query = new CustomerBalanceSummaryQuery(
                getDate(), nonOverdue, from, to, credit, getAccountType(),
                getWildcardedText(customerFrom),
                getWildcardedText(customerTo));
            while (query.hasNext()) {
                sets.add(query.next());
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
        return sets;
    }

    /**
     * Sets the statement date.
     *
     * @param date the statement date
     */
    public void setDate(Date date) {
        DateChooser chooser = this.date.getDateChooser();
        Calendar calendar = null;
        if (date != null) {
            calendar = Calendar.getInstance();
            calendar.setTime(date);
        }
        chooser.setSelectedDate(calendar);
    }

    /**
     * Returns the statement date.
     *
     * @return the statement date
     */
    public Date getDate() {
        return date.getSelectedDate().getTime();
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be <code>null</code>
     * @return a new result set
     */
    protected ResultSet<ObjectSet> createResultSet(SortConstraint[] sort) {
        return new ListResultSet<ObjectSet>(getObjects(), getMaxResults());
    }

    /**
     * Returns the selected account type.
     *
     * @return the selected lookup, or <tt>null</tt> to indicate all account
     *         types
     */
    private Lookup getAccountType() {
        return accountType.getSelected();
    }

    /**
     * Returns the numeric value of a text field.
     *
     * @param field the text field
     * @return the numeric value of the field
     */
    private int getNumber(TextField field) {
        int from = 0;
        String fromStr = field.getText();
        if (!StringUtils.isEmpty(fromStr)) {
            try {
                from = Integer.valueOf(fromStr);
            } catch (NumberFormatException ignore) {
                // do nothing
            }
        }
        return from;
    }

    /**
     * Invoked when the balance type changes. Enables/disables the overdue
     * fields.
     */
    private void onBalanceTypeChanged() {
        boolean enabled = balanceType.getSelectedIndex() != NON_OVERDUE_INDEX;
        ComponentHelper.enable(periodFromLabel, enabled);
        ComponentHelper.enable(periodFrom, enabled);
        ComponentHelper.enable(periodToLabel, enabled);
        ComponentHelper.enable(periodTo, enabled);
    }

    /**
     * Cell renderer that renders 'All' in bold.
     */
    class BalanceTypeListCellRenderer extends AbstractListCellRenderer<String> {

        /**
         * Constructs a new <tt>BalanceTypeListCellRenderer</tt>.
         */
        public BalanceTypeListCellRenderer() {
            super(String.class);
        }

        /**
         * Renders an object.
         *
         * @param list   the list component
         * @param object the object to render. May be <tt>null</tt>
         * @param index  the object index
         * @return the rendered object
         */
        protected Object getComponent(Component list, String object,
                                      int index) {
            return balanceTypeItems[index];
        }

        /**
         * Determines if an object represents 'All'.
         *
         * @param list   the list component
         * @param object the object. May be <tt>null</tt>
         * @param index  the object index
         * @return <code>true</code> if the object represents 'All'.
         */
        protected boolean isAll(Component list, String object, int index) {
            return index == ALL_BALANCE_INDEX;
        }

        /**
         * Determines if an object represents 'None'.
         *
         * @param list   the list component
         * @param object the object. May be <tt>null</tt>
         * @param index  the object index
         * @return <code>true</code> if the object represents 'None'.
         */
        protected boolean isNone(Component list, String object, int index) {
            return false;
        }
    }

}
