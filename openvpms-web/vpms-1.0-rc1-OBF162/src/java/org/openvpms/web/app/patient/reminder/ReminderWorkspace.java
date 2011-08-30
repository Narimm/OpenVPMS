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

package org.openvpms.web.app.patient.reminder;

import echopointng.GroupBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.archetype.component.processor.BatchProcessorListener;
import org.openvpms.archetype.rules.patient.reminder.DueReminderQuery;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.subsystem.AbstractWorkspace;
import org.openvpms.web.component.util.ButtonRow;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.GroupBoxFactory;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Reminder generation workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReminderWorkspace extends AbstractWorkspace {

    /**
     * The query.
     */
    private PatientReminderQuery query;

    /**
     * The browser.
     */
    private Browser<Act> browser;


    /**
     * Construct a new <code>ReminderWorkspace</code>.
     */
    public ReminderWorkspace() {
        super("patient", "reminder");
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
     * Lays out the component.
     *
     * @return the component
     */
    @Override
    protected Component doLayout() {
        SplitPane root = SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL,
                "ReminderWorkspace.Layout");
        Component heading = super.doLayout();
        root.add(heading);
        FocusGroup group = new FocusGroup("ReminderWorkspace");
        ButtonRow buttons = new ButtonRow(group, "ControlRow",
                                          ButtonRow.BUTTON_STYLE);
        buttons.addButton("process", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onProcess();
            }
        });
        buttons.addButton("processAll", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onProcessAll();
            }
        });
        buttons.addButton("print", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onPrint();
            }
        });
        SplitPane content = SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
                "ReminderWorkspace.Layout", buttons);
        doLayout(content, group);
        root.add(content);
        return root;
    }

    /**
     * Lays out the components.
     *
     * @param container the container
     * @param group     the focus group
     */
    private void doLayout(Component container, FocusGroup group) {
        query = new PatientReminderQuery();
        browser = new PatientReminderBrowser(query);
        GroupBox box = GroupBoxFactory.create(browser.getComponent());
        container.add(box);
        group.add(browser.getFocusGroup());
    }

    /**
     * Invoked when the 'Process' button is pressed.
     */
    private void onProcess() {
        try {
            Act selected = browser.getSelected();
            if (selected != null) {
                GlobalContext context = GlobalContext.getInstance();
                DueReminderQuery q = query.createReminderQuery();
                ReminderGenerator generator
                        = new ReminderGenerator(selected, q.getFrom(),
                                                q.getTo(), context);
                generateReminders(generator);
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when the 'Print' button is pressed.
     */
    private void onPrint() {
        Iterable<Act> objects = query.createReminderQuery().query();
        IMPrinter<Act> printer
                = new IMObjectReportPrinter<Act>(objects,
                                                 "act.patientReminder");
        String title = Messages.get("patient.reminder.print.title");
        try {
            InteractiveIMPrinter<Act> iPrinter
                    = new InteractiveIMPrinter<Act>(title, printer);
            iPrinter.print();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when the 'Process All' button is pressed.
     */
    private void onProcessAll() {
        String title = Messages.get("patient.reminder.run.title");
        String message = Messages.get("patient.reminder.run.message");
        final ConfirmationDialog dialog
                = new ConfirmationDialog(title, message);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
                    generateReminders();
                }
            }

        });
        dialog.show();
    }

    /**
     * Generate the reminders.
     */
    private void generateReminders() {
        try {
            GlobalContext context = GlobalContext.getInstance();
            ReminderGenerator generator
                    = new ReminderGenerator(query.createReminderQuery(),
                                            context);
            generateReminders(generator);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Generates reminders using the specified generator.
     * Updates the browser on completion.
     *
     * @param generator the generator
     */
    private void generateReminders(ReminderGenerator generator) {
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

}

