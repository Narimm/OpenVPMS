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
 */

package org.openvpms.web.app.reporting.statement;

import echopointng.layout.TableLayoutDataEx;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.archetype.rules.finance.account.CustomerBalanceSummaryQuery;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.util.DateHelper;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.text.NumberFormatter;
import org.openvpms.web.resource.i18n.Messages;

import java.math.BigDecimal;
import java.util.Date;


/**
 * Customer balance summary table model.
 *
 * @author Tim Anderson
 */
public class CustomerBalanceSummaryTableModel
    extends AbstractIMTableModel<ObjectSet> {

    /**
     * The context.
     */
    private final Context context;

    /**
     * The customer name index.
     */
    private static final int CUSTOMER_INDEX = 0;

    /**
     * The balance index.
     */
    private static final int BALANCE_INDEX = 1;

    /**
     * The overdue balance index.
     */
    private static final int OVERDUE_BALANCE_INDEX = 2;

    /**
     * The credit balance index.
     */
    private static final int CREDIT_BALANCE_INDEX = 3;

    /**
     * The unbilled amount index.
     */
    private static final int UNBILLED_AMOUNT_INDEX = 4;

    /**
     * The last payment date index.
     */
    private static final int LAST_PAYMENT_DATE_INDEX = 5;

    /**
     * The last payment amount index.
     */
    private static final int LAST_PAYMENT_AMOUNT_INDEX = 6;

    /**
     * The last invoice date index.
     */
    private static final int LAST_INVOICE_DATE_INDEX = 7;

    /**
     * The columns. A 2 dimensional array of the the ObjectSet key and
     * the corresponding column display name.
     */
    private String[][] columns = {
        {CustomerBalanceSummaryQuery.CUSTOMER_REFERENCE,
            Messages.get("customerbalancetablemodel.customer")},
        {CustomerBalanceSummaryQuery.BALANCE,
            Messages.get("customerbalancetablemodel.balance")},
        {CustomerBalanceSummaryQuery.OVERDUE_BALANCE,
            Messages.get("customerbalancetablemodel.overdueBalance")},
        {CustomerBalanceSummaryQuery.CREDIT_BALANCE, Messages.get(
            "customerbalancetablemodel.creditBalance")},
        {CustomerBalanceSummaryQuery.UNBILLED_AMOUNT, Messages.get(
            "customerbalancetablemodel.unbilledAmount")},
        {CustomerBalanceSummaryQuery.LAST_PAYMENT_DATE, Messages.get(
            "customerbalancetablemodel.lastPaymentDate")},
        {CustomerBalanceSummaryQuery.LAST_PAYMENT_AMOUNT, Messages.get(
            "customerbalancetablemodel.lastPaymentAmount")},
        {CustomerBalanceSummaryQuery.LAST_INVOICE_DATE, Messages.get(
            "customerbalancetablemodel.lastInvoiceDate")}};


    /**
     * Constructs a {@code CustomerBalanceSummaryTableModel}.
     *
     * @param context the context
     */
    public CustomerBalanceSummaryTableModel(Context context) {
        this.context = context;
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        for (int i = 0; i < columns.length; ++i) {
            model.addColumn(new TableColumn(i));
        }
        setTableColumnModel(model);
    }

    /**
     * Returns the name of the specified column number.
     *
     * @param column the column index (0-based)
     * @return the column name
     */
    @Override
    public String getColumnName(int column) {
        return columns[column][1];
    }

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if <code>true</code> sort in ascending order; otherwise
     *                  sort in <code>descending</code> order
     * @return {@code null}
     */
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        return null;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param set    the object set
     * @param column the column
     * @param row    the row
     * @return the value at the given coordinate.
     */
    protected Object getValue(ObjectSet set, TableColumn column, int row) {
        Object result = null;
        int index = column.getModelIndex();
        Object value = set.get(columns[index][0]);
        switch (index) {
            case CUSTOMER_INDEX:
                String name = set.getString(
                    CustomerBalanceSummaryQuery.CUSTOMER_NAME);
                result = getViewer((IMObjectReference) value, name);
                break;
            case BALANCE_INDEX:
                result = getAmount((BigDecimal) value);
                break;
            case OVERDUE_BALANCE_INDEX:
                result = getAmount((BigDecimal) value);
                break;
            case CREDIT_BALANCE_INDEX:
                result = getAmount((BigDecimal) value);
                break;
            case UNBILLED_AMOUNT_INDEX:
                result = getAmount((BigDecimal) value);
                break;
            case LAST_PAYMENT_DATE_INDEX:
                result = getDate((Date) value);
                break;
            case LAST_PAYMENT_AMOUNT_INDEX:
                result = getAmount((BigDecimal) value);
                break;
            case LAST_INVOICE_DATE_INDEX:
                result = getDate((Date) value);
                break;
        }
        return result;
    }

    /**
     * Helper to return a component to display a party.
     *
     * @param party the party reference
     * @param name  the party name
     * @return a component to display the party
     */
    private Component getViewer(IMObjectReference party, String name) {
        IMObjectReferenceViewer viewer = new IMObjectReferenceViewer(party, name, true, context);
        return viewer.getComponent();
    }

    /**
     * Helper to return a component to display a date.
     *
     * @param date the date. May be <code>null</code>
     * @return a component to display the date
     */
    private Component getDate(Date date) {
        Label label = LabelFactory.create();
        if (date != null) {
            label.setText(DateHelper.formatDate(date, false));
        }
        return label;
    }

    /**
     * Helper to return a component to display a right justified amount in
     * a table cell.
     *
     * @param amount the amount. May be <code>null</code>
     * @return a component to display the amount
     */
    private Component getAmount(BigDecimal amount) {
        Label label = LabelFactory.create();
        if (amount != null) {
            String text = NumberFormatter.formatCurrency(amount);
            label.setText(text);
            TableLayoutData layout = new TableLayoutDataEx();
            Alignment right = new Alignment(Alignment.RIGHT,
                                            Alignment.DEFAULT);
            layout.setAlignment(right);
            label.setLayoutData(layout);
        }
        return label;
    }

}
