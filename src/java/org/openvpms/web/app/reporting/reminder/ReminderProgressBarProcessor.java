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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.reporting.reminder;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.web.component.processor.ProgressBarProcessor;
import org.openvpms.web.component.util.ErrorHelper;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Abstract implementation of {@link ProgressBarProcessor} for reminders.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
abstract class ReminderProgressBarProcessor extends ProgressBarProcessor<List<ReminderEvent>>
        implements ReminderBatchProcessor {

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
    private Set<IMObjectReference> completed = new HashSet<IMObjectReference>();


    /**
     * Creates a new <tt>ReminderProgressBarProcessor</tt>.
     *
     * @param items      the reminder items
     * @param statistics the statistics
     * @param title      the progress bar title for display purposes
     */
    public ReminderProgressBarProcessor(List<List<ReminderEvent>> items, Statistics statistics, String title) {
        super(items, count(items), title);
        this.statistics = statistics;
        rules = new ReminderRules();
    }

    /**
     * Determines if reminders should be updated on completion.
     * <p/>
     * If set, the <tt>reminderCount</tt> is incremented the <tt>lastSent</tt> timestamp set on completed reminders.
     *
     * @param update if <tt>true</tt> update reminders on completion
     */
    public void setUpdateOnCompletion(boolean update) {
        this.update = update;
    }

    /**
     * Increments the count of processed reminders.
     *
     * @param events the reminder events
     */
    @Override
    protected void incProcessed(List<ReminderEvent> events) {
        super.incProcessed(events.size());
    }

    /**
     * Invoked when processing of reminder events is complete.
     * <p/>
     * This updates the reminders and statistics.
     *
     * @param events the reminder events
     */
    @Override
    protected void processCompleted(List<ReminderEvent> events) {
        if (update) {
            updateReminders(events);
        }
        updateStatistics(events);
        super.processCompleted(events);
    }

    /**
     * Skips a set of reminders.
     * <p/>
     * This doesn't update the reminders and their statistics
     *
     * @param events the reminder events
     */
    protected void skip(List<ReminderEvent> events) {
        super.processCompleted(events);
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
     * Updates each reminder that isn't cancelled.
     * <p/>
     * This sets the <em>lastSent</em> node to the current time, and increments the <em>reminderCount</em>.
     *
     * @param events the reminder event
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void updateReminders(List<ReminderEvent> events) {
        Date date = new Date();
        for (ReminderEvent event : events) {
            Act act = event.getReminder();
            if (!completed.contains(act.getObjectReference())) {
                if (!ActStatus.CANCELLED.equals(act.getStatus())) {
                    try {
                        rules.updateReminder(act, date);
                        completed.add(act.getObjectReference());
                    } catch (Throwable exception) {
                        ErrorHelper.show(exception);
                    }
                }
            }
        }
    }

    /**
     * Updates statistics for a set of reminders.
     *
     * @param events the reminder events
     */
    private void updateStatistics(List<ReminderEvent> events) {
        for (ReminderEvent event : events) {
            statistics.increment(event.getReminderType().getEntity(), event.getAction());
        }
    }

    /**
     * Counts reminders.
     *
     * @param events the reminder events
     * @return the reminder count
     */
    private static int count(List<List<ReminderEvent>> events) {
        int result = 0;
        for (List<ReminderEvent> list : events) {
            result += list.size();
        }
        return result;
    }
}
