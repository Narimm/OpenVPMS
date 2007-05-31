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

import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.patient.reminder.DefaultReminderProcessor;
import org.openvpms.archetype.rules.patient.reminder.DueReminderQuery;
import org.openvpms.archetype.rules.patient.reminder.ReminderCancelProcessor;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderProcessorException;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderStatisticsListener;
import org.openvpms.archetype.rules.patient.reminder.Statistics;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.workflow.AbstractTask;
import org.openvpms.web.component.workflow.PrintIMObjectsTask;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.TaskListener;
import org.openvpms.web.component.workflow.Tasks;
import org.openvpms.web.component.workflow.Workflow;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;


/**
 * Reminder generator.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class ReminderGenerator {

    public interface CompletionListener {

        void completed();
    }

    /**
     * The iterator over the reminders.
     */
    private Iterator<Act> iterator;

    /**
     * Indicates if processing should suspend, so the client can be updated.
     */
    private boolean suspend;

    /**
     * The processor.
     */
    private DefaultReminderProcessor processor;

    /**
     * Phone listener.
     */
    private ReminderPhoneListener phone;

    /**
     * Statistics listener.
     */
    private ReminderStatisticsListener stats;

    /**
     * Invoked when generation is complete.
     */
    private CompletionListener listener;


    /**
     * Constructs a new <tt>ReminderGenerator</tt> for a single reminder.
     *
     * @param reminder the reminder
     * @param from     only process reminder if its next due date &gt;= from
     * @param to       only process reminder if its next due date &lt;= to
     * @param context  the context
     * @throws ReminderProcessorException for any error
     */
    public ReminderGenerator(Act reminder, Date from, Date to,
                             Context context) {
        iterator = Arrays.asList(reminder).iterator();
        init(from, to, context);
    }

    /**
     * Constructs a new <tt>ReminderGenerator</tt> for reminders returned by a
     * query.
     *
     * @param query the query
     * @throws ReminderProcessorException for any error
     */
    public ReminderGenerator(DueReminderQuery query, Context context) {
        List<Act> reminders = new ArrayList<Act>();
        for (Act reminder : query.query()) {
            reminders.add(reminder);
        }
        iterator = reminders.iterator();
        init(query.getFrom(), query.getTo(), context);
    }

    /**
     * Generate reminders.
     */
    public void generate() {
        try {
            setSuspend(false);
            while (!isSuspended() && iterator.hasNext()) {
                Act reminder = iterator.next();
                processor.process(reminder);
            }
            if (!isSuspended()) {
                // generation completed.
                onGenerationComplete();
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
            notifyCompletionListener();
        }
    }

    /**
     * Sets the listener to invoke when generation is complete.
     *
     * @param listener the listener. May be <tt>null</tt>
     */
    public void setListener(CompletionListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the suspend state.
     *
     * @param suspend if <tt>true</tt> suspend generation
     */
    public void setSuspend(boolean suspend) {
        this.suspend = suspend;
    }

    /**
     * Determines if generation has been suspended.
     *
     * @return <tt>true</tt> if generation has been suspended
     */
    public boolean isSuspended() {
        return suspend;
    }

    /**
     * Initialises this.
     *
     * @param context the context
     * @throws ReminderProcessorException for any error
     */
    private void init(Date from, Date to, Context context) {
        processor = new DefaultReminderProcessor(from, to);
        phone = new ReminderPhoneListener();
        stats = new ReminderStatisticsListener();
        processor.addListener(stats);

        Party practice = context.getPractice();
        if (practice == null) {
            throw new ReminderProcessorException(
                    ReminderProcessorException.ErrorCode.InvalidConfiguration,
                    "Context has no practice");
        }
        ReminderRules rules = new ReminderRules();
        Contact email = rules.getEmailContact(practice.getContacts());
        if (email == null) {
            throw new ReminderProcessorException(
                    ReminderProcessorException.ErrorCode.InvalidConfiguration,
                    "Practice " + practice.getName()
                            + " has no email contact for reminders");
        }
        IMObjectBean bean = new IMObjectBean(email);
        String address = bean.getString("emailAddress");
        String name = practice.getName();
        if (StringUtils.isEmpty(address)) {
            throw new ReminderProcessorException(
                    ReminderProcessorException.ErrorCode.InvalidConfiguration,
                    "Practice " + practice.getName()
                            + " email contact address is empty");
        }

        ReminderCancelProcessor canceller = new ReminderCancelProcessor();
        ReminderEmailProcessor emailer
                = new ReminderEmailProcessor(ServiceHelper.getMailSender(),
                                             address, name);
        ReminderPrintProcessor printer = new ReminderPrintProcessor(this);

        processor.addListener(ReminderEvent.Action.CANCEL, canceller);
        processor.addListener(ReminderEvent.Action.EMAIL, emailer);
        processor.addListener(ReminderEvent.Action.PHONE, phone);
        processor.addListener(ReminderEvent.Action.PRINT, printer);
    }

    /**
     * Notifies any registered listener that generation has completed.
     */
    private void notifyCompletionListener() {
        if (listener != null) {
            listener.completed();
        }
    }

    /**
     * Invoked when generation is complete.
     * Prints any phone reminders and displays statistics.
     */
    private void onGenerationComplete() {
        Workflow workflow = new WorkflowImpl();
        Tasks printTasks = new Tasks();

        PrintIMObjectsTask<Act> printTask = new PrintIMObjectsTask<Act>(
                phone.getPhoneReminders(), "act.patientReminder");
        printTask.setRequired(false);
        printTasks.addTask(printTask);
        printTasks.addTask(new SynchronousTask() {
            public void execute(TaskContext context) {
                phone.updateAll();
            }
        });
        workflow.addTask(printTasks);
        workflow.addTask(new AbstractTask() {
            public void start(TaskContext context) {
                SummaryDialog summary = new SummaryDialog(
                        stats.getStatistics());
                summary.show();
                summary.addWindowPaneListener(new WindowPaneListener() {
                    public void windowPaneClosing(WindowPaneEvent e) {
                        notifyCompleted();
                    }
                });
            }
        });
        workflow.addTaskListener(new TaskListener() {
            public void taskEvent(TaskEvent event) {
                notifyCompletionListener();
            }
        });
        workflow.start();
    }

    /**
     * Displays summary statistics in a popup window.
     *
     * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
     * @version $LastChangedDate: 2006-04-11 04:09:07Z $
     */
    private static class SummaryDialog extends PopupDialog {

        /**
         * Constructs a new <tt>SummaryDialog</tt>.
         *
         * @param stats the statistics to display
         */
        public SummaryDialog(Statistics stats) {
            super(Messages.get("patient.reminder.summary.title"), OK);
            setModal(true);
            EnumSet<ReminderEvent.Action> actions = EnumSet.range(
                    ReminderEvent.Action.EMAIL, ReminderEvent.Action.PRINT);

            Grid grid = GridFactory.create(2);
            add(grid, ReminderEvent.Action.CANCEL, stats);

            for (Entity reminderType : stats.getReminderTypes()) {
                String text = reminderType.getName();
                add(grid, text, stats.getCount(reminderType, actions));
            }

            for (ReminderEvent.Action action : actions) {
                add(grid, action, stats);
            }
            getLayout().add(ColumnFactory.create("Inset", grid));
        }

        /**
         * Adds a summary line item to a grid.
         *
         * @param grid   the grid
         * @param action the reminder action
         * @param stats  the reminder statistics
         */
        private void add(Grid grid, ReminderEvent.Action action,
                         Statistics stats) {
            String text = Messages.get("patient.reminder.summary."
                    + action.name());
            add(grid, text, stats.getCount(action));
        }

        /**
         * Adds a summary line item to a grid.
         *
         * @param grid  the grid
         * @param text  the item text
         * @param count the statistics count
         */
        private void add(Grid grid, String text, int count) {
            Label label = LabelFactory.create();
            label.setText(text);
            Label value = LabelFactory.create();
            value.setText(Integer.toString(count));
            grid.add(label);
            grid.add(value);
        }
    }
}
