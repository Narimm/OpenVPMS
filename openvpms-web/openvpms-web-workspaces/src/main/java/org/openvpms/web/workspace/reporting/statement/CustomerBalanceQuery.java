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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.statement;

import echopointng.DateChooser;
import echopointng.DateField;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.finance.account.CustomerBalanceSummaryQuery;
import org.openvpms.archetype.rules.practice.Location;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.list.AbstractListCellRenderer;
import org.openvpms.web.component.im.list.LookupListCellRenderer;
import org.openvpms.web.component.im.location.LocationSelectField;
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
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Customer balance query.
 *
 * @author Tim Anderson
 */
public class CustomerBalanceQuery extends AbstractArchetypeQuery<ObjectSet> {

    /**
     * The practice.
     */
    private final Party practice;

    /**
     * The balance type list items.
     */
    private final String[] balanceTypeItems;

    /**
     * The account type selector.
     */
    private LookupField accountType;

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
     * The customer location selector.
     */
    private LocationSelectField locationSelector;

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
     * Constructs a {@link CustomerBalanceQuery}.
     *
     * @param practice the practice
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public CustomerBalanceQuery(Party practice) {
        super(new String[]{"party.customer*"}, ObjectSet.class);
        this.practice = practice;
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
     * @return {@code true} if customers with both overdue and non-overdue
     * balances are being queried.
     */
    public boolean queryAllBalances() {
        return balanceType.getSelectedIndex() == ALL_BALANCE_INDEX;
    }

    /**
     * Determines if customers with overdue balances are being queried.
     *
     * @return {@code true} if customers with overdue balances are being
     * queried, {@code false} if customers with outstanding balances are being
     * queried
     */
    public boolean queryOverduebalances() {
        return balanceType.getSelectedIndex() == OVERDUE_INDEX;
    }

    /**
     * Returns all objects matching the criteria.
     *
     * @return all objects matching the criteria
     */
    public List<ObjectSet> getObjects() {
        List<ObjectSet> sets = new ArrayList<>();
        try {
            CustomerBalanceSummaryQuery query;
            int selected = balanceType.getSelectedIndex();
            boolean nonOverdue = selected != OVERDUE_INDEX;
            boolean overdue = selected != NON_OVERDUE_INDEX;
            int from = overdue ? getNumber(periodFrom) : -1;
            int to = overdue ? getNumber(periodTo) : -1;
            boolean credit = excludeCredit.isSelected();
            Location location = locationSelector.getSelected();
            CustomerAccountRules rules = ServiceHelper.getBean(CustomerAccountRules.class);
            IArchetypeService service = ServiceHelper.getArchetypeService();
            ILookupService lookups = ServiceHelper.getLookupService();
            query = new CustomerBalanceSummaryQuery(getDate(), nonOverdue, from, to, credit, getAccountType(),
                                                    getWildcardedText(customerFrom),
                                                    getWildcardedText(customerTo), location, service, lookups, rules);
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
     * Returns the preferred height of the query when rendered.
     *
     * @return the preferred height, or {@code null} if it has no preferred height
     */
    @Override
    public Extent getHeight() {
        return getHeight(3);
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
        accountType = LookupFieldFactory.create(new ArchetypeLookupQuery(CustomerArchetypes.ACCOUNT_TYPE), true);
        accountType.setSelected((Lookup) null);
        accountType.setCellRenderer(LookupListCellRenderer.INSTANCE);

        Label accountTypeLabel = LabelFactory.create(
                "reporting.statements.accountType");

        date = DateFieldFactory.create();
        date.addPropertyChangeListener(event -> {
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
        grid.add(balanceTypeLabel);
        grid.add(balanceType);

        periodFromLabel = LabelFactory.create(
                "reporting.statements.periodFrom");
        periodFrom = TextComponentFactory.create();
        periodFrom.addPropertyChangeListener(event -> {
        });

        periodToLabel = LabelFactory.create("reporting.statements.periodTo");
        periodTo = TextComponentFactory.create();
        periodTo.addPropertyChangeListener(event -> {
        });

        excludeCredit = CheckBoxFactory.create("reporting.statements.excludeCredit", true);

        grid.add(periodFromLabel);
        grid.add(periodFrom);
        grid.add(periodToLabel);
        grid.add(periodTo);
        grid.add(excludeCredit);
        grid.add(LabelFactory.create());


        Label customerFromLabel = LabelFactory.create(
                "reporting.statements.customerFrom");
        customerFrom = TextComponentFactory.create();
        customerFrom.addPropertyChangeListener(event -> {
        });

        Label customerToLabel = LabelFactory.create(
                "reporting.statements.customerTo");
        customerTo = TextComponentFactory.create();
        customerTo.addPropertyChangeListener(event -> {
        });

        grid.add(customerFromLabel);
        grid.add(customerFrom);
        grid.add(customerToLabel);
        grid.add(customerTo);
        grid.add(LabelFactory.create("reporting.customer.location"));
        locationSelector = new LocationSelectField(practice);
        grid.add(locationSelector);

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
        group.add(locationSelector);

        FocusHelper.setFocus(getSearchField());
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be {@code null}
     * @return a new result set
     */
    protected ResultSet<ObjectSet> createResultSet(SortConstraint[] sort) {
        return new ListResultSet<>(getObjects(), getMaxResults());
    }

    /**
     * Returns the selected account type.
     *
     * @return the selected lookup, or {@code null} to indicate all account types
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
         * Constructs a new {@code BalanceTypeListCellRenderer}.
         */
        BalanceTypeListCellRenderer() {
            super(String.class);
        }

        /**
         * Renders an object.
         *
         * @param list   the list component
         * @param object the object to render. May be {@code null}
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
         * @param object the object. May be {@code null}
         * @param index  the object index
         * @return {@code true} if the object represents 'All'.
         */
        protected boolean isAll(Component list, String object, int index) {
            return index == ALL_BALANCE_INDEX;
        }

        /**
         * Determines if an object represents 'None'.
         *
         * @param list   the list component
         * @param object the object. May be {@code null}
         * @param index  the object index
         * @return {@code true} if the object represents 'None'.
         */
        protected boolean isNone(Component list, String object, int index) {
            return false;
        }
    }

}
