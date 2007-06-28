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

package org.openvpms.web.app.financial.statement;

import echopointng.DateField;
import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.finance.account.CustomerBalanceSummaryQuery;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.list.LookupListCellRenderer;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.component.im.query.AbstractQuery;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.FastLookupHelper;
import org.openvpms.web.component.util.CheckBoxFactory;
import org.openvpms.web.component.util.ComponentHelper;
import org.openvpms.web.component.util.DateFieldFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.component.util.TextComponentFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Customer balance query.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerBalanceQuery extends AbstractQuery<ObjectSet> {

    /**
     * The account type selector.
     */
    private SelectField accountType;

    /**
     * Determines if only customers with overdue balances should be queried.
     */
    private CheckBox overdue;

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
     * Constructs a new <tt>CustomerBalanceQuery</tt> .
     *
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public CustomerBalanceQuery() {
        super(new String[]{"party.customer*"});
    }

    /**
     * Refreshes the account types.
     */
    public void refreshAccountTypes() {
        if (accountType != null) {
            String selected = (String) accountType.getSelectedItem();
            LookupListModel model = createAccountTypeModel();
            accountType.setModel(model);
            int index = model.indexOf(selected);
            if (index != -1) {
                accountType.setSelectedIndex(index);
            }
        }
    }

    /**
     * Determines if customers with overdue balances are being queried.
     *
     * @return <tt>true</tt> if customers with overdue balances are being
     *         queried, <tt>false</tt> if customers with outstanding balances are being
     *         queried
     */
    public boolean queryOverdue() {
        return overdue.isSelected();
    }

    /**
     * Lays out the component in a container, and sets focus on the instance
     * name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        LookupListModel model = createAccountTypeModel();

        accountType = SelectFieldFactory.create(model);
        accountType.setCellRenderer(new LookupListCellRenderer());

        Row accountTypeRow = createRow(
                "CellSpacing",
                LabelFactory.create("financial.statements.accountType"),
                accountType);

        date = DateFieldFactory.create();
        date.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
            }
        });
        Row statementDateRow = createRow(
                "CellSpacing",
                LabelFactory.create("financial.statements.date"),
                date);

        overdue = CheckBoxFactory.create("financial.statements.overdue", true);
        overdue.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOverdueChanged();
            }
        });

        periodFromLabel = LabelFactory.create(
                "financial.statements.periodFrom");
        periodFrom = TextComponentFactory.create();
        periodFrom.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                    }
                });

        periodToLabel = LabelFactory.create("financial.statements.periodTo");
        periodTo = TextComponentFactory.create();
        periodTo.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                    }
                });
        Row periodRange = createRow("CellSpacing",
                                    periodFromLabel, periodFrom,
                                    periodToLabel, periodTo);
        Row overdueRow = createRow("ControlRow", accountTypeRow, overdue,
                                   periodRange);

        container.add(accountTypeRow);
        container.add(statementDateRow);
        container.add(overdueRow);

        FocusGroup group = getFocusGroup();
        group.add(accountType);
        group.add(date);
        group.add(overdue);
        group.add(periodFrom);
        group.add(periodTo);

        ApplicationInstance.getActive().setFocusedComponent(getInstanceName());
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
            if (overdue.isSelected()) {
                query = new CustomerBalanceSummaryQuery(getDate(),
                                                        getNumber(periodFrom),
                                                        getNumber(periodTo),
                                                        getAccountType());
            } else {
                query = new CustomerBalanceSummaryQuery(getDate(),
                                                        getAccountType());
            }
            while (query.hasNext()) {
                sets.add(query.next());
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
        return sets;
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
        int index = accountType.getSelectedIndex();
        if (index != -1) {
            LookupListModel model = (LookupListModel) accountType.getModel();
            Lookup lookup = model.getLookup(index);
            return (LookupListModel.ALL == lookup) ? null : lookup;
        }
        return null;
    }

    /**
     * Returns the statement date.
     *
     * @return the statement date
     */
    private Date getDate() {
        return date.getSelectedDate().getTime();
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
     * Invoked when the overdue check box changes.
     */
    private void onOverdueChanged() {
        boolean enabled = overdue.isSelected();
        ComponentHelper.enable(periodFromLabel, enabled);
        ComponentHelper.enable(periodFrom, enabled);
        ComponentHelper.enable(periodToLabel, enabled);
        ComponentHelper.enable(periodTo, enabled);
    }

    /**
     * Creates the lookup list model of account types.
     *
     * @return a new lookup list model
     */
    private LookupListModel createAccountTypeModel() {
        List<Lookup> lookups = FastLookupHelper.getLookups(
                "lookup.customerAccountType");
        return new LookupListModel(lookups, true);
    }

    /**
     * Helper to create a row containing a set of components.
     *
     * @param style      the style name
     * @param components the components
     * @return a row containing the components
     */
    private Row createRow(String style, Component ... components) {
        return RowFactory.create(style, components);
    }

}
