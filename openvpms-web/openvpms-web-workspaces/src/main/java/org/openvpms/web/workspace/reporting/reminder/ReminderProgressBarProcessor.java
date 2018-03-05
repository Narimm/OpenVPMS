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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.reminder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.Reminders;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.exception.OpenVPMSException;
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
     * Determines if the iterator has been initialised.
     */
    private boolean initialised = false;

    /**
     * Determines if reminders are being reprocessed.
     */
    private boolean resend = false;

    /**
     * The statistics.
     */
    private Statistics statistics;

    /**
     * The current reminders being processed.
     */
    private Reminders currentReminders;

    /**
     * Determines if the current reminders are in error.
     */
    private boolean currentError = false;

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
     * @param items     the reminder item source
     * @param processor the processor
     * @param title     the progress bar title for display purposes
     */
    public ReminderProgressBarProcessor(ReminderItemSource items, PatientReminderProcessor processor, String title) {
        super(title);
        String[] shortNames = items.getArchetypes();
        if (shortNames.length != 1 || !TypeHelper.matches(shortNames[0], processor.getArchetype())) {
            throw new IllegalStateException("This may only query " + processor.getArchetype());
        }

        this.items = items;
        this.processor = processor;
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
     * <p>
     * If set:
     * <ul>
     * <li>due dates are ignored</li>
     * <li>the reminder last sent date is not updated</li>
     * </ul>
     * <p>
     * Defaults to {@code false}.
     *
     * @param resend if {@code true} reminders are being resent
     */
    public void setResend(boolean resend) {
        this.resend = resend;
    }

    /**
     * Registers the statistics.
     *
     * @param statistics the statistics
     */
    @Override
    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    /**
     * Determines if there are more reminders available on completion of processing.
     *
     * @return {@code true} if there are more reminders available
     */
    @Override
    public boolean hasMoreReminders() {
        return getIterator().hasNext();
    }

    /**
     * Processes the batch.
     */
    @Override
    public void process() {
        if (!initialised) {
            int count = items.count();
            setItems(items.query(), count);
            initialised = true;
        }
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
        currentReminders = reminders;
        currentState = null;
        currentError = false;
        try {
            currentState = processor.prepare(reminders.getReminders(), reminders.getGroupBy(), new Date(), resend);
            if (!currentState.getReminders().isEmpty()) {
                process(currentState);
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
     * Processes reminders.
     *
     * @param reminders the reminders
     */
    protected void process(PatientReminders reminders) {
        processor.process(reminders);
    }

    /**
     * Returns the reminder processor.
     *
     * @return the reminder processor
     */
    protected PatientReminderProcessor getProcessor() {
        return processor;
    }

    /**
     * Invoked when processing a batch of reminders is completed.
     */
    protected void processCompleted() {
        if (currentState != null) {
            if (!currentError) {
                // only complete the reminders if processError() hasn't been invoked on them
                processor.complete(currentState);
                if (statistics != null) {
                    processor.addStatistics(currentState, statistics);
                }
            }
            super.processCompleted(currentReminders);
        } else {
            log.error("ReminderProgressBarProcess.processCompleted() invoked with no current reminders");
        }
    }

    /**
     * Invoked if an error occurs processing the batch.
     * <p>
     * This:
     * <ul>
     * <li>updates the error node of each reminder if they aren't being resent</li>
     * <li>updates statistics</li>
     * <li>notifies any listeners of the error</li>
     * <li>delegates to the parent {@link #processCompleted(Object)} to continue processing if this is asynchronous</li>
     * </ul>
     *
     * @param exception the cause
     */
    protected void processError(Throwable exception) {
        if (currentState != null) {
            currentError = true;
            processor.failed(currentState, exception);
            statistics.addErrors(currentState.getReminders().size());
            statistics.addErrors(currentState.getErrors().size());
            notifyError(exception);
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
     * <p>
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
