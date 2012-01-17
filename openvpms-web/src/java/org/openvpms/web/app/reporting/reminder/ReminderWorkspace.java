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
import org.openvpms.archetype.component.processor.BatchProcessorListener;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderProcessor;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.reporting.AbstractReportingWorkspace;
import org.openvpms.web.component.app.DefaultContextSwitchListener;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.DefaultIMObjectTableBrowser;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.print.InteractivePrinter;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.GroupBoxFactory;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;


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

        // create a layout context, with hyperlinks enabled
        LayoutContext context = new DefaultLayoutContext();
        TableComponentFactory factory = new TableComponentFactory(context);
        context.setComponentFactory(factory);
        context.setContextSwitchListener(DefaultContextSwitchListener.INSTANCE);

        PatientReminderTableModel model = new PatientReminderTableModel(context);
        browser = new DefaultIMObjectTableBrowser<Act>(query, model);

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
            public void onAction(ActionEvent event) {
                onPrint();
            }
        });
        buttons.add("sendAll", new ActionListener() {
            public void onAction(ActionEvent event) {
                onSendAll();
            }
        });
        buttons.add("report", new ActionListener() {
            public void onAction(ActionEvent event) {
                onReport();
            }
        });
    }

    /**
     * Invoked when the 'Print' button is pressed. Prints the selected reminder.
     */
    private void onPrint() {
        try {
            Act reminder = browser.getSelected();
            if (reminder != null) {
                ReminderProcessor processor = new ReminderProcessor(reminder.getActivityStartTime(),
                        reminder.getActivityEndTime(), reminder.getActivityStartTime(),
                        ServiceHelper.getArchetypeService());
                IMObjectBean bean = new IMObjectBean(reminder);
                int reminderCount = bean.getInt("reminderCount");
                ReminderEvent event = processor.process(reminder, reminderCount);
                if (event.getDocumentTemplate() != null) {
                    DocumentTemplate template = new DocumentTemplate(event.getDocumentTemplate(),
                            ServiceHelper.getArchetypeService());
                    DocumentTemplateLocator locator = new ContextDocumentTemplateLocator(template, reminder,
                            GlobalContext.getInstance());
                    InteractivePrinter printer = new InteractivePrinter(
                            new IMObjectReportPrinter<Act>(reminder, locator));
                    printer.print();
                } else {
                    ErrorHelper.show(Messages.get("reporting.reminder.print.notemplate",
                            event.getReminderType().getName(), reminderCount));
                }
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when the 'Send All' button is pressed. Runs the reminder
     * generator for all reminders.
     */
    private void onSendAll() {
        String title = Messages.get("reporting.reminder.run.title");
        String message = Messages.get("reporting.reminder.run.message");
        final ConfirmationDialog dialog
                = new ConfirmationDialog(title, message);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                generateReminders();
            }

        });
        dialog.show();
    }

    /**
     * Invoked when the 'Report' button is pressed.
     */
    private void onReport() {
        Iterable<Act> objects = query.createReminderQuery().query();
        IMPrinter<Act> printer = new IMObjectReportPrinter<Act>(objects, ReminderArchetypes.REMINDER);
        String title = Messages.get("reporting.reminder.print.title");
        try {
            InteractiveIMPrinter<Act> iPrinter = new InteractiveIMPrinter<Act>(title, printer);
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

