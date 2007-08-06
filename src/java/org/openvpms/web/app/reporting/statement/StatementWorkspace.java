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
import nextapp.echo2.app.Component;
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
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.print.ObjectSetReportPrinter;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.subsystem.AbstractWorkspace;
import org.openvpms.web.component.util.ButtonRow;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.GroupBoxFactory;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.resource.util.Messages;


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

        buttons.addButton("printAll", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onPrintAll();
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
        browser = new CustomerBalanceBrowser(query);
        GroupBox box = GroupBoxFactory.create(browser.getComponent());
        container.add(box);
        group.add(browser.getFocusGroup());
    }

    /**
     * Invoked when the 'print all' button is pressed.
     */
    private void onPrintAll() {
        String title = Messages.get("reporting.statements.run.title");
        String message = Messages.get("reporting.statements.run.message");
        final ConfirmationDialog dialog
                = new ConfirmationDialog(title, message);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
                    doPrintAll();
                }
            }
        });
        dialog.show();
    }

    /**
     * Processes all customers matching the criteria.
     */
    private void doPrintAll() {
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
     * Invoked when the 'process' button is pressed.
     */
    private void onPrint() {
        try {
            ObjectSet selected = browser.getSelected();
            if (selected != null) {
                IMObjectReference ref = (IMObjectReference) selected.get(
                        CustomerBalanceSummaryQuery.CUSTOMER_REFERENCE);
                if (ref != null) {
                    GlobalContext context = GlobalContext.getInstance();
                    StatementGenerator generator = new StatementGenerator(
                            ref, query.getDate(), context);
                    generateStatements(generator);
                }
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when the 'end period' button is pressed.
     */
    private void onEndPeriod() {
        String title = Messages.get("reporting.statements.eop.title");
        String message = Messages.get("reporting.statements.eop.message");
        final ConfirmationDialog dialog
                = new ConfirmationDialog(title, message);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
                    doEndPeriod();
                }
            }
        });
        dialog.show();
    }

    /**
     * Runs end period.
     */
    private void doEndPeriod() {
        try {
            EndOfPeriodGenerator generator = new EndOfPeriodGenerator(
                    query.getDate());
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

}
