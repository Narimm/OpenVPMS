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

package org.openvpms.archetype.rules.patient.reminder;

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.SequenceComparator;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Specifies how a reminder should be processed.
 * <p>
 * This is a wrapper around an <em>entity.reminderCount</em>.
 *
 * @author Tim Anderson
 * @see ReminderType
 * @see ReminderRule
 */
public class ReminderCount {

    /**
     * The reminder count.
     */
    private final int count;

    /**
     * The overdue interval.
     */
    private final int interval;

    /**
     * The overdue units.
     */
    private DateUnits units;

    /**
     * The rules.
     */
    private final List<ReminderRule> rules = new ArrayList<>();

    /**
     * The entity wrapper.
     */
    private final IMObjectBean bean;

    /**
     * The document template to use.
     */
    private DocumentTemplate template;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs a {@link ReminderCount}.
     *
     * @param object  the <em>entity.reminderCount</em>
     * @param service the archetype service
     */
    public ReminderCount(IMObject object, IArchetypeService service) {
        bean = new IMObjectBean(object, service);
        this.service = service;
        count = bean.getInt("count");
        interval = bean.getInt("interval");
        units = DateUnits.fromString(bean.getString("units"), DateUnits.DAYS);
    }

    /**
     * Returns the reminder count.
     * <p>
     * For historical reasons, these start at 0
     *
     * @return the reminder count
     */
    public int getCount() {
        return count;
    }

    /**
     * Returns the overdue interval.
     *
     * @return the overdue interval
     */
    public int getInterval() {
        return interval;
    }

    /**
     * Returns the overdue units.
     *
     * @return the overdue units
     */
    public DateUnits getUnits() {
        return units;
    }

    /**
     * Calculates the next due date based on the overdue interval and units.
     *
     * @param dueDate the current due date
     * @return the next due date
     */
    public Date getNextDueDate(Date dueDate) {
        return DateRules.getDate(dueDate, interval, units);
    }

    /**
     * Returns the rules for this reminder count.
     *
     * @return the rules
     */
    public List<ReminderRule> getRules() {
        if (rules.isEmpty()) {
            for (IMObject rule : bean.getNodeTargetObjects("rules", SequenceComparator.INSTANCE)) {
                rules.add(new ReminderRule(rule, service));
            }
        }
        return rules;
    }

    /**
     * Returns the document template.
     *
     * @return the document template. May be {@code null}
     */
    public DocumentTemplate getTemplate() {
        if (template == null) {
            IMObject object = bean.getNodeTargetObject("template");
            if (object != null) {
                template = new DocumentTemplate((Entity) object, service);
            }
        }
        return template;
    }

}
