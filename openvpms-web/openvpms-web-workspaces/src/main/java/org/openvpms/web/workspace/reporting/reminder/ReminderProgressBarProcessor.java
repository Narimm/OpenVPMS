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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.reminder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.Reminders;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.processor.ProgressBarProcessor;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;


/**
 * Abstract implementation of {@link ProgressBarProcessor} for reminders.
 *
 * @author Tim Anderson
 */
abstract class ReminderProgressBarProcessor extends ProgressBarProcessor<Reminders>
        implements ReminderBatchProcessor {

    /**
     * The reminder item source.
     */
    private final ReminderItemSource items;

    /**
     * The processor to use.
     */
    private final PatientReminderProcessor processor;

    /**
     * The reminder rules.
     */
    private final ReminderRules rules;

    /**
     * Determines if reminders are being reprocessed.
     */
    private boolean resend = false;

    /**
     * The statistics.
     */
    private final Statistics statistics;

    /**
     * The current reminders being processed.
     */
    private Reminders currentReminders;

    /**
     * The current reminder state.
     */
    private PatientReminders currentState;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ReminderProgressBarProcessor.class);


    /**
     * Constructs a {@link ReminderProgressBarProcessor}.
     *
     * @param items      the reminder item source
     * @param processor  the processor
     * @param statistics the statistics
     * @param title      the progress bar title for display purposes
     */
    public ReminderProgressBarProcessor(ReminderItemSource items, PatientReminderProcessor processor,
                                        Statistics statistics, String title) {
        super(title);
        String[] shortNames = items.getArchetypes();
        if (shortNames.length != 1 || !TypeHelper.matches(shortNames[0], processor.getArchetype())) {
            throw new IllegalStateException("This may only query " + processor.getArchetype());
        }

        this.items = items;
        this.processor = processor;
        this.statistics = statistics;
        rules = ServiceHelper.getBean(ReminderRules.class);
    }

    /**
     * Returns the reminder item archetype that this processes.
     *
     * @return the reminder item archetype
     */
    @Override
    public String getArchetype() {
        return processor.getArchetype();
    }

    /**
     * Indicates if reminders are being resent.
     * <p/>
     * If set:
     * <ul>
     * <li>due dates are ignored</li>
     * <li>the reminder last sent date is not updated</li>
     * </ul>
     * <p/>
     * Defaults to {@code false}.
     *
     * @param resend if {@code true} reminders are being resent
     */
    public void setResend(boolean resend) {
        this.resend = resend;
    }

    /**
     * Processes the batch.
     */
    @Override
    public void process() {
        int count = items.count();
        setItems(items.query(), count);
        super.process();
    }

    /**
     * Processes a set of reminders.
     *
     * @param reminders the reminders to process
     * @throws OpenVPMSException if the events cannot be processed
     */
    @Override
    protected void process(Reminders reminders) {
        this.currentReminders = reminders;
        this.currentState = null;
        try {
            currentState = processor.prepare(reminders.getReminders(), reminders.getGroupBy(), new Date(), resend);
            if (!currentState.getReminders().isEmpty()) {
                processor.process(currentState);
                if (processor.isAsynchronous()) {
                    // need to process these reminders asynchronously, so suspend
                    setSuspend(true);
                } else {
                    processCompleted();
                }
            } else {
                processCompleted();
            }
        } catch (Throwable exception) {
            processError(exception);
        }
    }

    /**
     * Invoked when processing a batch of reminders is completed.
     */
    protected void processCompleted() {
        if (currentState != null) {
            processor.complete(currentState);
            processor.addStatistics(currentState, statistics);
            super.processCompleted(currentReminders);
        } else {
            log.error("ReminderProgressBarProcess.processCompleted() invoked with no current reminders");
        }
    }

    /**
     * Invoked if an error occurs processing the batch.
     * <p/>
     * This:
     * <ul>
     * <li>updates the error node of each reminder if {@link #setResend(boolean)} is {@code true}</li>
     * <li>updates statistics</li>
     * <li>notifies any listeners of the error</li>
     * <li>delegates to the parent {@link #processCompleted(Object)} to continue processing</li>
     * </ul>
     *
     * @param exception the cause
     */
    protected void processError(Throwable exception) {
        if (currentReminders != null) {
            for (ObjectSet set : currentReminders.getReminders()) {
                if (resend) {
                    ReminderHelper.setError((Act) set.get("item"), exception);
                }
                statistics.incErrors();
            }
            notifyError(exception);
            super.processCompleted(currentReminders);
        } else {
            log.error("ReminderProgressBarProcess.processError() invoked with no current reminders", exception);
        }
    }

    /**
     * Increments the count of processed reminders.
     *
     * @param reminders the reminders
     */
    @Override
    protected void incProcessed(Reminders reminders) {
        super.incProcessed(reminders.getReminders().size());
    }

    /**
     * Skips a set of reminders.
     * <p/>
     * This doesn't update the reminders and their statistics.
     */
    protected void skip() {
        if (currentReminders != null) {
            processCompleted(currentReminders);
        } else {
            log.error("ReminderProgressBarProcess.skip() invoked with no current reminders");
        }
    }

    /**
     * Returns the reminder rules.
     *
     * @return the reminder rules
     */
    protected ReminderRules getRules() {
        return rules;
    }

}
