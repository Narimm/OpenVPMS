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

package org.openvpms.web.app.reporting.reminder;

import echopointng.GroupBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.archetype.component.processor.BatchProcessorListener;
import org.openvpms.archetype.rules.patient.reminder.DueReminderQuery;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.reporting.AbstractReportingWorkspace;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.GroupBoxFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Reminder generation workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReminderWorkspace extends AbstractReportingWorkspace<Act> {

    /**
     * The query.
     */
    private PatientReminderQuery query;

    /**
     * The browser.
     */
    private Browser<Act> browser;


    /**
     * Construct a new <tt>ReminderWorkspace</tt>.
     */
    public ReminderWorkspace() {
        super("reporting", "reminder", Act.class);
    }

    /**
     * Lays out the components.
     *
     * @param container the container
     * @param group     the focus group
     */
    protected void doLayout(Component container, FocusGroup group) {
        query = new PatientReminderQuery();
        browser = new PatientReminderBrowser(query);
        GroupBox box = GroupBoxFactory.create(browser.getComponent());
        container.add(box);
        group.add(browser.getFocusGroup());
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button set
     */
    protected void layoutButtons(ButtonSet buttons) {
        buttons.add("print", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onPrint();
            }
        });
        buttons.add("printAll", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onPrintAll();
            }
        });
        buttons.add("report", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onReport();
            }
        });
    }

    /**
     * Invoked when the 'Print' button is pressed. Runs the reminder generator
     * for the selected reminder.
     */
    private void onPrint() {
        try {
            Act selected = browser.getSelected();
            if (selected != null) {
                DueReminderQuery q = query.createReminderQuery();
                ReminderGenerator generator
                        = new ReminderGenerator(selected, q.getFrom(),
                                                q.getTo());
                generateReminders(generator);
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when the 'Print All' button is pressed. Runs the reminder
     * generator for all reminders.
     */
    private void onPrintAll() {
        String title = Messages.get("reporting.reminder.run.title");
        String message = Messages.get("reporting.reminder.run.message");
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
     * Invoked when the 'Print' button is pressed.
     */
    private void onReport() {
        Iterable<Act> objects = query.createReminderQuery().query();
        IMPrinter<Act> printer
                = new IMObjectReportPrinter<Act>(objects,
                                                 "act.patientReminder");
        String title = Messages.get("reporting.reminder.print.title");
        try {
            InteractiveIMPrinter<Act> iPrinter
                    = new InteractiveIMPrinter<Act>(title, printer);
            iPrinter.print();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
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

