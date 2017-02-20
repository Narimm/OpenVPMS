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
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.processor.ProgressBarProcessor;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Abstract implementation of {@link ProgressBarProcessor} for reminders.
 *
 * @author Tim Anderson
 */
abstract class ReminderProgressBarProcessor extends ProgressBarProcessor<List<ObjectSet>>
        implements ReminderBatchProcessor {

    /**
     * The processor to use.
     */
    private final PatientReminderProcessor processor;

    /**
     * The reminder rules.
     */
    private final ReminderRules rules;

    /**
     * Determines if reminders should be updated on completion.
     */
    private boolean update = true;

    /**
     * The statistics.
     */
    private final Statistics statistics;

    /**
     * The set of completed reminder ids, used to avoid updating reminders that are being reprocessed.
     */
    private Map<Long, LastSentCount> lastSentCountMap = new HashMap<>();

    /**
     * The current reminders being processed.
     */
    private List<ObjectSet> currentReminders;

    /**
     * The current reminder state.
     */
    private PatientReminderProcessor.State currentState;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ReminderProgressBarProcessor.class);


    /**
     * Constructs a {@link ReminderProgressBarProcessor}.
     *
     * @param processor  the processor
     * @param statistics the statistics
     * @param title      the progress bar title for display purposes
     */
    public ReminderProgressBarProcessor(PatientReminderProcessor processor, Statistics statistics, String title) {
        super(title);
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
     * Determines if reminders should be updated on completion.
     * <p/>
     * If set, the {@code reminderCount} is incremented the {@code lastSent} timestamp set on completed reminders.
     *
     * @param update if {@code true} update reminders on completion
     */
    public void setUpdateOnCompletion(boolean update) {
        this.update = update;
    }

    /**
     * Processes a set of reminders.
     *
     * @param reminders the reminders to process
     * @throws OpenVPMSException if the events cannot be processed
     */
    protected void process(List<ObjectSet> reminders) {
        this.currentReminders = reminders;
        this.currentState = null;
        if (update) {
            // need to cache the reminderCount and lastSent nodes to allow reprocessing with the original values
            for (ObjectSet event : reminders) {
                Act reminder = (Act) event.get("reminder");
                long id = reminder.getId();
                LastSentCount lastSentCount = lastSentCountMap.get(id);
                if (lastSentCount != null) {
                    // reprocessing a reminder - reset the reminderCount and lastSent nodes
                    if (lastSentCount.isComplete()) {
                        lastSentCount.reset(reminder);
                    }
                } else {
                    lastSentCount = new LastSentCount(reminder);
                    lastSentCountMap.put(id, lastSentCount);
                }
            }
        }

        try {
            currentState = processor.prepare(reminders, new Date());
            processor.process(currentState);
            if (processor.isAsynchronous()) {
                // need to process these reminders asynchronously, so suspend
                setSuspend(true);
            } else {
                processCompleted();
            }
        } catch (Throwable exception) {
            processError(exception);
        }
    }

    protected void initIterator(String shortName, final ReminderItemSource query) {
        String[] shortNames = query.getShortNames();
        if (shortNames.length != 1 || !TypeHelper.matches(shortNames[0], shortName)) {
            throw new IllegalStateException("This may only query " + shortName);
        }

        int count = query.count();
        setItems(query.query(), count);
    }

    /**
     * Invoked when processing a batch of reminders is completed.
     */
    protected void processCompleted() {
        if (currentState != null) {
            if (update) {
                processor.complete(currentState);
            }
            updateStatistics(currentState.getReminders());
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
     * <li>updates the error node of each reminder if {@link #setUpdateOnCompletion(boolean)} is {@code true}</li>
     * <li>updates statistics</li>
     * <li>notifies any listeners of the error</li>
     * <li>delegates to the parent {@link #processCompleted(Object)} to continue processing</li>
     * </ul>
     *
     * @param exception the cause
     */
    protected void processError(Throwable exception) {
        if (currentReminders != null) {
            for (ObjectSet set : currentReminders) {
                if (update) {
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
    protected void incProcessed(List<ObjectSet> reminders) {
        super.incProcessed(reminders.size());
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

    /**
     * Updates statistics for a set of reminders.
     *
     * @param reminders the reminders
     */
    private void updateStatistics(List<ObjectSet> reminders) {
        for (ObjectSet set : reminders) {
            statistics.increment(set);
        }
    }

    private static class LastSentCount {

        private boolean completed;

        private int reminderCount;

        private Date lastSent;

        public LastSentCount(Act reminder) {
            IMObjectBean bean = new IMObjectBean(reminder);
            reminderCount = bean.getInt("reminderCount");
            lastSent = bean.getDate("lastSent");
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        public boolean isComplete() {
            return completed;
        }

        public void reset(Act reminder) {
            IMObjectBean bean = new IMObjectBean(reminder);
            bean.setValue("reminderCount", reminderCount);
            bean.setValue("lastSent", lastSent);
        }

    }
}
