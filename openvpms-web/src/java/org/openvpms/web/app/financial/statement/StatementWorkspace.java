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

package org.openvpms.web.app.financial.statement;

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
        super("financial", "statement");
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

        buttons.addButton("processAll", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onProcessAll();
            }
        });
        buttons.addButton("process", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onProcess();
            }
        });
        buttons.addButton("report", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onReport();
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
     * Invoked when the 'process all' button is pressed.
     */
    private void onProcessAll() {
        String title = Messages.get("financial.statements.run.title");
        String message = Messages.get("financial.statements.run.message");
        final ConfirmationDialog dialog
                = new ConfirmationDialog(title, message);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
                    doProcessAll();
                }
            }
        });
        dialog.show();
    }

    /**
     * Processes all customers matching the criteria.
     */
    private void doProcessAll() {
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
    private void onProcess() {
        try {
            ObjectSet selected = browser.getSelected();
            if (selected != null) {
                IMObjectReference ref = (IMObjectReference) selected.get(
                        CustomerBalanceSummaryQuery.CUSTOMER_REFERENCE);
                if (ref != null) {
                    GlobalContext context = GlobalContext.getInstance();
                    StatementGenerator generator
                            = new StatementGenerator(ref, context);
                    generateStatements(generator);
                }
            }
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
                type = Messages.get("financial.statements.print.overdue");
            } else {
                type = Messages.get("financial.statements.print.outstanding");
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
