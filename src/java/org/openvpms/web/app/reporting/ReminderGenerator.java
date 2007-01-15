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

package org.openvpms.web.app.reporting;

import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.archetype.rules.patient.reminder.AbstractReminderProcessor;
import org.openvpms.archetype.rules.patient.reminder.ReminderQuery;
import org.openvpms.archetype.rules.patient.reminder.ReminderQueryIterator;
import org.openvpms.archetype.rules.patient.reminder.Statistics;
import static org.openvpms.archetype.rules.patient.reminder.Statistics.Type.SKIPPED;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.report.TemplateHelper;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.print.IMObjectPrinterFactory;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.IMPrinterListener;
import org.openvpms.web.component.im.print.InteractiveIMObjectPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.ArrayList;
import java.util.List;


/**
 * Reminder generator.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class ReminderGenerator extends AbstractReminderProcessor {

    /**
     * The query iterator.
     */
    private ReminderQueryIterator iterator;

    /**
     * Indicates if processing should suspend, so the client can be updated.
     */
    private boolean suspend;

    /**
     * Reminders that need to be listed to phone.
     */
    private List<Act> phoneReminders = new ArrayList<Act>();


    /**
     * Constructs a new <code>ReminderGenerator</code>.
     *
     * @param query the reminder query
     */
    public ReminderGenerator(ReminderQuery query) {
        iterator = new ReminderQueryIterator(query);
    }

    /**
     * Generate reminders.
     */
    public void generate() {
        suspend = false;
        while (!suspend && iterator.hasNext()) {
            Act reminder = iterator.next();
            process(reminder);
        }
        if (!suspend) {
            // generation completed.
            // If there are any phone reminders, print them.
            if (!phoneReminders.isEmpty()) {
                printPhoneReminders();
            }
            // log statistics.
            SummaryDialog summary = new SummaryDialog(getStatistics());
            summary.show();
        }
    }

    /**
     * Phone a reminder.
     *
     * @param reminder         the reminder
     * @param reminderType     the reminder type
     * @param contact          the reminder contact
     * @param documentTemplate the document template
     */
    @Override
    protected void phone(Act reminder, Entity reminderType, Contact contact,
                         Entity documentTemplate) {
        super.phone(reminder, reminderType, contact, documentTemplate);
        phoneReminders.add(reminder);
    }

    /**
     * Print a reminder.
     *
     * @param reminder         the reminder
     * @param reminderType     the reminder type
     * @param contact          the reminder contact
     * @param documentTemplate the document template
     */
    @Override
    protected void print(final Act reminder, final Entity reminderType,
                         final Contact contact, final Entity documentTemplate) {
        DocumentAct act = TemplateHelper.getDocumentAct(
                documentTemplate,
                ArchetypeServiceHelper.getArchetypeService());
        if (act != null) {
            IMPrinter<DocumentAct> printer
                    = IMObjectPrinterFactory.create(act);
            InteractiveIMPrinter<DocumentAct> iPrinter
                    = new InteractiveIMObjectPrinter<DocumentAct>(printer);
            iPrinter.setListener(new IMPrinterListener() {
                public void printed() {
                    ReminderGenerator.super.print(
                            reminder, reminderType, contact, documentTemplate);
                    generate();
                }

                public void cancelled() {
                }

                public void failed(Throwable cause) {
                    ErrorHelper.show(cause, new WindowPaneListener() {
                        public void windowPaneClosing(
                                WindowPaneEvent event) {
                        }
                    });
                }
            });
            iPrinter.print();
            suspend = true;

        } else {
            getStatistics().increment(reminderType, SKIPPED);
        }
    }

    /**
     * Prints a report of all phone reminders.
     */
    private void printPhoneReminders() {
    }

    /**
     * Displays summary statistics in a popup window.
     *
     * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
     * @version $LastChangedDate: 2006-04-11 04:09:07Z $
     */
    private static class SummaryDialog extends PopupDialog {

        /**
         * Construct a new <code>SummaryDialog</code>.
         *
         * @param stats the statistics to display
         */
        public SummaryDialog(Statistics stats) {
            super(Messages.get("reporting.reminder.summary.title"), OK);
            setModal(true);
            Grid grid = new Grid(2);
            for (Statistics.Type type : Statistics.Type.values()) {
                Label label = LabelFactory.create();
                label.setText(type.toString());
                Label value = LabelFactory.create();
                value.setText(Integer.toString(stats.getCount(type)));
                grid.add(label);
                grid.add(value);
            }
            getLayout().add(ColumnFactory.create("Inset", grid));
        }
    }
}
