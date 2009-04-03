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
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.processor.ProgressBarProcessor;

import java.util.ArrayList;
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
abstract class ReminderProgressBarProcessor extends ProgressBarProcessor<List<ReminderEvent>> {

    /**
     * The document template for grouped reminders.
     */
    private final Entity groupTemplate;

    /**
     * The reminder rules.
     */
    private final ReminderRules rules;

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
     * @param items         the reminder items
     * @param groupTemplate reminder group template. May be <tt>null</tt>
     * @param statistics    the statistics
     * @param title         the progress bar title for display purposes
     */
    public ReminderProgressBarProcessor(List<List<ReminderEvent>> items, Entity groupTemplate,
                                        Statistics statistics, String title) {
        super(items, count(items), title);
        this.groupTemplate = groupTemplate;
        this.statistics = statistics;
        rules = new ReminderRules();
    }

    /**
     * Processes a list of reminder events.
     * <p/>
     * This implementation delegates to {@link #process(List, String, Entity)}.
     *
     * @param events the reminder events
     */
    protected void process(List<ReminderEvent> events) {
        ReminderEvent event = events.get(0);
        String shortName;
        Entity documentTemplate;

        if (events.size() > 1) {
            shortName = "GROUPED_REMINDERS";
            documentTemplate = groupTemplate;
        } else {
            shortName = "act.patientReminder";
            documentTemplate = event.getDocumentTemplate();
        }
        process(events, shortName, documentTemplate);
    }

    /**
     * Processes a list of reminder events.
     * <p/>
     * This implementation is a no-op
     *
     * @param events           the events
     * @param shortName        the report archetype short name, used to select the document template if none specified
     * @param documentTemplate the document template to use. May be <tt>null</tt>
     */
    protected void process(List<ReminderEvent> events, String shortName, Entity documentTemplate) {

    }

    @Override
    protected void incProcessed(List<ReminderEvent> events) {
        super.incProcessed(events.size());
    }

    @Override
    protected void processCompleted(List<ReminderEvent> events) {
        updateReminders(events);
        updateStatistics(events);
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
     * Creates object sets for the specified set of reminder events.
     *
     * @param events the events
     * @return a list of object sets corresponding to the events
     */
    protected List<ObjectSet> createObjectSets(List<ReminderEvent> events) {
        List<ObjectSet> result = new ArrayList<ObjectSet>();
        for (ReminderEvent event : events) {
            ObjectSet set = new ObjectSet();
            ActBean bean = new ActBean(event.getReminder());
            set.set("customer", event.getCustomer());
            set.set("reminderType", event.getReminderType().getEntity());
            set.set("patient", bean.getNodeParticipant("patient"));
            set.set("product", bean.getNodeParticipant("product"));
            set.set("clinician", bean.getNodeParticipant("product"));
            set.set("startTime", event.getReminder().getActivityEndTime());
            set.set("endTime", event.getReminder().getActivityEndTime());
            set.set("reminderCount", bean.getInt("reminderCount"));
            set.set("act", event.getReminder());
            result.add(set);
        }
        return result;
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
                    rules.updateReminder(act, date);
                }
            }
            completed.add(act.getObjectReference());
        }
    }

    private void updateStatistics(List<ReminderEvent> events) {
        for (ReminderEvent event : events) {
            statistics.increment(event.getReminderType().getEntity(), event.getAction());
        }
    }

    private static int count(List<List<ReminderEvent>> events) {
        int result = 0;
        for (List<ReminderEvent> list : events) {
            result += list.size();
        }
        return result;
    }
}
