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

package org.openvpms.web.app.reporting.statement;

import echopointng.GroupBox;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.archetype.component.processor.BatchProcessorListener;
import org.openvpms.archetype.rules.finance.account.CustomerBalanceSummaryQuery;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.print.ObjectSetReportPrinter;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.subsystem.AbstractWorkspace;
import org.openvpms.web.component.util.ButtonRow;
import org.openvpms.web.component.util.CheckBoxFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.GroupBoxFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.Calendar;
import java.util.Date;


/**
 * Statement workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StatementWorkspace extends AbstractWorkspace {

    /**
     * The query.
     */
    private CustomerBalanceQuery query;

    /**
     * The browser.
     */
    private Browser<ObjectSet> browser;

    /**
     * Determines if this is the first rendering of the workspace.
     */
    private boolean rendered;


    /**
     * Construct a new <code>StatementWorkspace</code>.
     */
    public StatementWorkspace() {
        super("reporting", "statement");
    }

    /**
     * Determines if the workspace supports an archetype.
     *
     * @param shortName the archetype's short name
     * @return <code>true</code> if the workspace can handle the archetype;
     *         otherwise <code>false</code>
     */
    public boolean canHandle(String shortName) {
        return false;
    }

    /**
     * Sets the current object.
     * This is analagous to  {@link #setObject} but performs a safe cast
     * to the required type.
     *
     * @param object the current object
     */
    public void setIMObject(IMObject object) {
        setObject(object);
    }

    /**
     * Sets the object to be viewed/edited by the workspace.
     *
     * @param object the object. May be <code>null</code>
     */
    public void setObject(IMObject object) {
        // no-op. This workspace doesn't work on individual objects
    }

    /**
     * Returns the object to to be viewed/edited by the workspace.
     *
     * @return the the object. May be <oode>null</code>
     */
    public IMObject getObject() {
        return null;
    }

    /**
     * Renders the workspace.
     *
     * @return the component representing the workspace
     */
    @Override
    public Component getComponent() {
        Component component = super.getComponent();
        if (rendered) {
            // make sure the account types are up to date
            query.refreshAccountTypes();
        } else {
            rendered = true;
        }
        return component;
    }

    /**
     * Lays out the component.
     *
     * @return the component
     */
    @Override
    protected Component doLayout() {
        SplitPane root = SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL,
                "StatementWorkspace.Layout");
        Component heading = super.doLayout();
        root.add(heading);
        FocusGroup group = new FocusGroup("StatementWorkspace");
        ButtonRow buttons = new ButtonRow(group, "ControlRow", "default");
        SplitPane content = SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
                "StatementWorkspace.Layout", buttons);
        doLayout(content, group);
        root.add(content);

        buttons.addButton("sendAll", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onSendAll();
            }
        });
        buttons.addButton("print", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onPrint();
            }
        });
        buttons.addButton("report", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onReport();
            }
        });
        buttons.addButton("endPeriod", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onEndPeriod();
            }
        });
        return root;
    }

    /**
     * Lays out the components.
     *
     * @param container the container
     * @param group     the focus group
     */
    private void doLayout(Component container, FocusGroup group) {
        query = new CustomerBalanceQuery();
        query.getComponent();
        query.setDate(getYesterday()); // default statement date to yesterday
        browser = new CustomerBalanceBrowser(query);
        GroupBox box = GroupBoxFactory.create(browser.getComponent());
        container.add(box);
        group.add(browser.getFocusGroup());
    }

    /**
     * Invoked when the 'send all' button is pressed.
     */
    private void onSendAll() {
        if (checkStatementDate("reporting.statements.run.invalidDate")) {
            String title = Messages.get("reporting.statements.run.title");
            String message = Messages.get("reporting.statements.run.message");
            final ConfirmationDialog dialog
                    = new ConfirmationDialog(title, message);
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void windowPaneClosing(WindowPaneEvent event) {
                    if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
                        doSendAll();
                    }
                }
            });
            dialog.show();
        }
    }

    /**
     * Processes all customers matching the criteria.
     */
    private void doSendAll() {
        try {
            GlobalContext context = GlobalContext.getInstance();
            StatementGenerator generator = new StatementGenerator(query,
                                                                  context);
            generateStatements(generator);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when the 'print' button is pressed.
     */
    private void onPrint() {
        if (checkStatementDate("reporting.statements.run.invalidDate")) {
            try {
                ObjectSet selected = browser.getSelected();
                if (selected != null) {
                    IMObjectReference ref = (IMObjectReference) selected.get(
                            CustomerBalanceSummaryQuery.CUSTOMER_REFERENCE);
                    if (ref != null) {
                        GlobalContext context = GlobalContext.getInstance();
                        StatementGenerator generator = new StatementGenerator(
                                ref, query.getDate(), true, context);
                        generateStatements(generator);
                    }
                }
            } catch (OpenVPMSException exception) {
                ErrorHelper.show(exception);
            }
        }
    }

    /**
     * Invoked when the 'end period' button is pressed.
     */
    private void onEndPeriod() {
        if (checkStatementDate("reporting.statements.eop.invalidDate")) {
            String title = Messages.get("reporting.statements.eop.title");
            String message = Messages.get("reporting.statements.eop.message");
            final EndOfPeriodDialog dialog
                    = new EndOfPeriodDialog(title, message);
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void windowPaneClosing(WindowPaneEvent event) {
                    if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
                        doEndPeriod(dialog.postCompletedInvoices());
                    }
                }
            });
            dialog.show();
        }
    }

    /**
     * Runs end period.
     *
     * @param postCompletedInvoices if <tt>true</tt>, post completed invoices
     */
    private void doEndPeriod(boolean postCompletedInvoices) {
        try {
            EndOfPeriodGenerator generator = new EndOfPeriodGenerator(
                    query.getDate(), postCompletedInvoices);
            generator.setListener(new BatchProcessorListener() {
                public void completed() {
                    browser.query();
                }

                public void error(Throwable exception) {
                    ErrorHelper.show(exception);
                }
            });
            generator.process();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Generates statements.
     *
     * @param generator the statement generator
     */
    private void generateStatements(StatementGenerator generator) {
        generator.setListener(new BatchProcessorListener() {
            public void completed() {
                browser.query();
            }

            public void error(Throwable exception) {
                ErrorHelper.show(exception);
            }
        });
        generator.process();
    }

    /**
     * Verfies that the statement date is at least a day prior to the current
     * date.
     *
     * @param errorKey the error message key, if the date is invalid
     * @return <tt>true</tt> if the statement date is less than today
     */
    private boolean checkStatementDate(String errorKey) {
        Date statementDate = query.getDate();
        Date date = getYesterday();
        if (date.compareTo(statementDate) < 0) {
            ErrorDialog.show(Messages.get(errorKey));
            return false;
        }
        return true;
    }

    /**
     * Returns yesterday's date.
     *
     * @return yesterday's date
     */
    private Date getYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }

    /**
     * Invoked when the 'Report' button is pressed.
     */
    private void onReport() {
        try {
            IMPrinter<ObjectSet> printer = new ObjectSetReportPrinter(
                    query.getObjects(), "CUSTOMER_BALANCE");
            String type;
            if (query.queryOverdue()) {
                type = Messages.get("reporting.statements.print.overdue");
            } else {
                type = Messages.get("reporting.statements.print.outstanding");
            }
            String title = Messages.get("imobject.print.title", type);
            InteractiveIMPrinter<ObjectSet> iPrinter
                    = new InteractiveIMPrinter<ObjectSet>(title, printer);
            iPrinter.print();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    private class EndOfPeriodDialog extends ConfirmationDialog {

        /**
         * Determines if completed invoices should be posted.
         */
        private final CheckBox postCompleted;

        /**
         * Constructs a new <code>EndOfPeriodDialog</code>.
         *
         * @param title   the window title
         * @param message the message
         */
        public EndOfPeriodDialog(String title, String message) {
            super(title, message, OK_CANCEL);
            postCompleted = CheckBoxFactory.create(
                    "reporting.statements.eop.postCompleted", true);
        }

        /**
         * Determines if completed invoices should be posted.
         *
         * @return <tt>true</tt> if completed invoices should be posted
         */
        public boolean postCompletedInvoices() {
            return postCompleted.isSelected();
        }

        /**
         * Lays out the component prior to display.
         */
        @Override
        protected void doLayout() {
            Label message = LabelFactory.create();
            message.setText(getMessage());
            Column column = ColumnFactory.create("WideCellSpacing",
                                                 message, postCompleted);
            Row row = RowFactory.create("Inset", column);
            getLayout().add(row);
        }
    }
}
