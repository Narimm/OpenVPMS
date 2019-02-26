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

package org.openvpms.web.workspace.customer.account;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.RadioButton;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.button.ButtonGroup;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.list.DefaultListModel;
import nextapp.echo2.app.list.ListCellRenderer;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountQueryFactory;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.query.DateRange;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.print.InteractivePrinter;
import org.openvpms.web.component.print.PrintDialog;
import org.openvpms.web.echo.button.CheckBox;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.util.List;

/**
 * Prints account statements.
 * <p>
 * This supports printing:
 * <ul>
 * <li>the current statement. This is all acts after the most recent opening balance</li>
 * <li>past statements</li>
 * <li>all acts between two dates</li>
 * </ul>
 *
 * @author Tim Anderson
 */
public class InteractiveStatementPrinter extends InteractivePrinter {

    /**
     * The list of issued statements, or {@code null} if none have been issued
     */
    private SelectField issuedSelector;

    /**
     * Constructs an {@link InteractiveStatementPrinter}.
     *
     * @param printer the printer to delegate to
     * @param context the context
     * @param help    the help context
     */
    public InteractiveStatementPrinter(StatementPrinter printer, Context context, HelpContext help) {
        super(printer, context, help);
    }

    /**
     * Returns the underlying printer.
     *
     * @return the printer
     */
    @Override
    protected StatementPrinter getPrinter() {
        return (StatementPrinter) super.getPrinter();
    }

    /**
     * Creates a new print dialog.
     *
     * @return a new print dialog
     */
    @Override
    protected PrintDialog createDialog() {
        return new StatementPrintDialog(getPrinter(), getTitle(), getContext().getLocation(), getHelpContext());
    }

    /**
     * Returns a title for the print dialog.
     *
     * @return a title for the print dialog
     */
    @Override
    protected String getTitle() {
        String title = super.getTitle();
        if (title != null) {
            return title;
        }
        return Messages.format("imobject.print.title", getDisplayName());
    }

    private class StatementPrintDialog extends PrintDialog {

        private final StatementPrinter printer;

        private final List<FinancialAct> closingBalances;

        private final RadioButton current;

        private final CheckBox complete;

        private final CheckBox fee;

        private final RadioButton issued;

        private final RadioButton range;

        private DateRange dates = new DateRange(false);

        private FocusGroup focusGroup;

        /**
         * Constructs a {@link StatementPrintDialog}.
         *
         * @param printer  the statement printer
         * @param title    the window title
         * @param location the current practice location. May be {@code null}
         * @param help     the help context. May be {@code null}
         */
        public StatementPrintDialog(StatementPrinter printer, String title, Party location, HelpContext help) {
            super(title, true, true, false, location, help);
            this.printer = printer;
            closingBalances = getClosingBalances();
            ButtonGroup group = new ButtonGroup();
            current = ButtonFactory.create("customer.account.statement.current", group);
            current.setSelected(true);
            complete = CheckBoxFactory.create("customer.account.statement.complete");
            fee = CheckBoxFactory.create("customer.account.statement.fee");
            issued = ButtonFactory.create("customer.account.statement.issued", group);
            if (closingBalances.isEmpty()) {
                issued.setEnabled(false);
            }
            range = ButtonFactory.create("customer.account.statement.range", group);
            focusGroup = new FocusGroup("StatementPrintDialog");
            getFocusGroup().add(0, focusGroup); // insert at top
        }

        /**
         * Invoked when the 'OK' button is pressed. This sets the action and closes
         * the window.
         */
        @Override
        protected void onOK() {
            if (setParameters()) {
                super.onOK();
            }
        }

        @Override
        protected void onPreview() {
            if (setParameters()) {
                download();
            }
        }

        @Override
        protected void onMail() {
            if (setParameters()) {
                mail(this);
            }
        }

        /**
         * Lays out the dialog.
         *
         * @param container the container
         */
        @Override
        protected void doLayout(Component container) {
            ComponentGrid grid = new ComponentGrid();

            grid.add(current, complete);
            grid.add(LabelFactory.create(), fee);
            ActionListener currentListener = new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    current.setSelected(true);
                }
            };
            complete.addActionListener(currentListener);
            fee.addActionListener(currentListener);
            focusGroup.add(current);

            if (!closingBalances.isEmpty()) {
                DefaultListModel model = new DefaultListModel(closingBalances.toArray());
                issuedSelector = SelectFieldFactory.create(model);
                issuedSelector.addActionListener(new ActionListener() {
                    @Override
                    public void onAction(ActionEvent event) {
                        issued.setSelected(true);
                    }
                });
                ListCellRenderer renderer = (component, o, i) -> DateFormatter.formatDate(
                        ((FinancialAct) o).getActivityStartTime(), false);
                issuedSelector.setCellRenderer(renderer);
                grid.add(issued, issuedSelector);
                focusGroup.add(issued);
                focusGroup.add(issuedSelector);
            }
            grid.add(range, dates.getComponent());
            focusGroup.add(range);
            focusGroup.add(dates.getFocusGroup());
            container.add(grid.createGrid());
            super.doLayout(container);
        }

        /**
         * Sets the print parameters.
         *
         * @return {@code true} if the parameters were set
         */
        private boolean setParameters() {
            boolean set = true;
            if (current.isSelected()) {
                printer.setPrintCurrent(complete.isSelected(), fee.isSelected());
            } else if (issued.isSelected() && issuedSelector != null && issuedSelector.getSelectedIndex() != -1) {
                FinancialAct closing = closingBalances.get(issuedSelector.getSelectedIndex());
                printer.setPrintStatement(closing);
            } else if (range.isSelected()) {
                printer.setPrintRange(dates.getFrom(), dates.getTo());
            } else {
                set = false;
            }
            return set;
        }

        /**
         * Returns all closing balances for the customer.
         *
         * @return the closing balances, most recent first
         */
        private List<FinancialAct> getClosingBalances() {
            ArchetypeQuery query = CustomerAccountQueryFactory.createQuery(getContext().getCustomer(),
                                                                           CustomerAccountArchetypes.CLOSING_BALANCE);
            query.add(Constraints.sort("startTime", false));
            query.add(Constraints.sort("id", false));
            query.setMaxResults(IArchetypeQuery.ALL_RESULTS);
            return QueryHelper.query(query);
        }
    }

}
