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
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * Abstract implementation of the {@link Processor} interface for reminder processing.
 *
 * @author Tim Anderson
 */
public abstract class AbstractReminderProcessor implements Processor<List<ReminderEvent>> {

    /**
     * The grouped reminder template.
     */
    private final DocumentTemplate groupTemplate;

    /**
     * The context.
     */
    private final Context context;


    /**
     * Creates a new {@code AbstractReminderProcessor}.
     *
     * @param groupTemplate the grouped reminder template
     * @param context       the context
     */
    public AbstractReminderProcessor(DocumentTemplate groupTemplate, Context context) {
        this.groupTemplate = groupTemplate;
        this.context = context;
    }

    /**
     * Processes a list of reminder events.
     * <p/>
     * This implementation delegates to {@link #process(List, String, DocumentTemplate)}.
     *
     * @param events the reminder events
     */
    public void process(List<ReminderEvent> events) {
        ReminderEvent event = events.get(0);
        String shortName;
        DocumentTemplate template;

        if (events.size() > 1) {
            shortName = "GROUPED_REMINDERS";
            template = groupTemplate;
        } else {
            shortName = ReminderArchetypes.REMINDER;
            template = new DocumentTemplate(event.getDocumentTemplate(), ServiceHelper.getArchetypeService());
        }
        process(events, shortName, template);
    }

    /**
     * Processes a list of reminder events.
     *
     * @param events    the events
     * @param shortName the report archetype short name, used to select the document template if none specified
     * @param template  the document template to use. May be {@code null}
     */
    protected abstract void process(List<ReminderEvent> events, String shortName, DocumentTemplate template);

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
            set.set("clinician", bean.getNodeParticipant("clinician"));
            set.set("startTime", event.getReminder().getActivityStartTime());
            set.set("endTime", event.getReminder().getActivityEndTime());
            set.set("reminderCount", bean.getInt("reminderCount"));
            set.set("act", event.getReminder());
            result.add(set);
        }
        return result;
    }

    /**
     * Returns the context.
     *
     * @return the context
     */
    protected Context getContext() {
        return context;
    }

}
