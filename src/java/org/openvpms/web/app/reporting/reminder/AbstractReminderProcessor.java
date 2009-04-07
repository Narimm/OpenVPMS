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

import org.openvpms.archetype.component.processor.Processor;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.ArrayList;
import java.util.List;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractReminderProcessor implements Processor<List<ReminderEvent>> {

    private final Entity groupTemplate;

    public AbstractReminderProcessor(Entity groupTemplate) {
        this.groupTemplate = groupTemplate;
    }

    /**
     * Processes a list of reminder events.
     * <p/>
     * This implementation delegates to {@link #process(List, String, Entity)}.
     *
     * @param events the reminder events
     */
    public void process(List<ReminderEvent> events) {
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
     *
     * @param events           the events
     * @param shortName        the report archetype short name, used to select the document template if none specified
     * @param documentTemplate the document template to use. May be <tt>null</tt>
     */
    protected abstract void process(List<ReminderEvent> events, String shortName, Entity documentTemplate);

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

}
