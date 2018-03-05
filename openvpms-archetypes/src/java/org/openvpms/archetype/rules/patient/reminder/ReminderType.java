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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * Wrapper around an <em>entity.reminderType}.
 *
 * @author Tim Anderson
 */
public class ReminderType {

    public enum GroupBy {
        CUSTOMER, PATIENT, NONE
    }

    /**
     * The underlying <em>entity.reminderType</em>.
     */
    private final Entity reminderType;

    /**
     * The default interval.
     */
    private final int defaultInterval;

    /**
     * The default interval units.
     */
    private final DateUnits defaultUnits;

    /**
     * The cancel interval.
     */
    private final int cancelInterval;

    /**
     * The cancel interval units.
     */
    private final DateUnits cancelUnits;

    /**
     * Determines if the reminder can be grouped with other reminders in a single report.
     */
    private final GroupBy groupBy;

    /**
     * The reminder counts.
     */
    private final List<ReminderCount> counts = new ArrayList<>();

    /**
     * The reminder groups.
     */
    private final List<Lookup> groups;

    /**
     * Determines if the reminder is interactive.
     */
    private final boolean interactive;


    /**
     * Constructs a {@link ReminderType}.
     *
     * @param reminderType the <em>entity.reminderType</em>
     * @param service      the archetype service
     */
    public ReminderType(Entity reminderType, IArchetypeService service) {
        EntityBean bean = new EntityBean(reminderType, service);
        defaultInterval = bean.getInt("defaultInterval");
        defaultUnits = getDateUnits(bean, "defaultUnits", DateUnits.YEARS);
        cancelInterval = bean.getInt("cancelInterval");
        cancelUnits = getDateUnits(bean, "cancelUnits", DateUnits.YEARS);
        for (IMObject count : bean.getNodeTargetObjects("counts")) {
            counts.add(new ReminderCount(count, service));
        }
        if (!counts.isEmpty()) {
            Collections.sort(counts, new Comparator<ReminderCount>() {
                @Override
                public int compare(ReminderCount o1, ReminderCount o2) {
                    return Integer.compare(o1.getCount(), o2.getCount());
                }
            });
        }
        String group = bean.getString("groupBy");
        if (GroupBy.CUSTOMER.name().equals(group)) {
            groupBy = GroupBy.CUSTOMER;
        } else if (GroupBy.PATIENT.name().equals(group)) {
            groupBy = GroupBy.PATIENT;
        } else {
            groupBy = GroupBy.NONE;
        }
        groups = bean.getValues("groups", Lookup.class);
        interactive = bean.getBoolean("interactive");
        this.reminderType = reminderType;
    }

    /**
     * Returns the underlying <em>entity.reminderType</em>.
     *
     * @return the underlying <em>entity.reminderType</em>
     */
    public Entity getEntity() {
        return reminderType;
    }

    /**
     * Returns the reminder type's name.
     *
     * @return the name
     */
    public String getName() {
        return reminderType.getName();
    }

    /**
     * Determines if the reminder type is active or inactive.
     *
     * @return {@code true} if the reminder type is active, {@code false} if it is inactive
     */
    public boolean isActive() {
        return reminderType.isActive();
    }

    /**
     * Calculates the due date for a reminder.
     *
     * @param date the reminder's start time
     * @return the due date for the reminder
     */
    public Date getDueDate(Date date) {
        return DateRules.getDate(date, defaultInterval, defaultUnits);
    }

    /**
     * Determines when a reminder should be cancelled.
     *
     * @param date the reminder's due date
     * @return the reminder's cancel date
     */
    public Date getCancelDate(Date date) {
        return DateRules.getDate(date, cancelInterval, cancelUnits);
    }

    /**
     * Determines if a reminder needs to be cancelled, based on its due date
     * and the specified date. Reminders should be cancelled if:
     * <p/>
     * {@code dueDate + (cancelInterval * cancelUnits) &lt;= date}
     *
     * @param dueDate the due date
     * @param date    the date
     * @return {@code true} if the reminder needs to be cancelled, otherwise {@code false}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public boolean shouldCancel(Date dueDate, Date date) {
        Date cancelDate = getCancelDate(dueDate);
        return (cancelDate.getTime() <= date.getTime());
    }

    /**
     * Returns the default interval.
     *
     * @return the default interval
     */
    public int getDefaultInterval() {
        return defaultInterval;
    }

    /**
     * Returns the default interval's units.
     *
     * @return the default interval's units
     */
    public DateUnits getDefaultUnits() {
        return defaultUnits;
    }

    /**
     * Returns the cancel interval.
     *
     * @return the cancel interval
     */
    public int getCancelInterval() {
        return cancelInterval;
    }

    /**
     * Returns the cancel interval's units.
     *
     * @return the cancel interval's units
     */
    public DateUnits getCancelUnits() {
        return cancelUnits;
    }

    /**
     * Returns the available reminder counts, sorted on increasing count.
     *
     * @return the available reminder counts
     */
    public List<ReminderCount> getReminderCounts() {
        return counts;
    }

    /**
     * Returns a reminder count.
     *
     * @param count the reminder count
     * @return the corresponding count, or {@code null} if none is found
     */
    public ReminderCount getReminderCount(final int count) {
        return CollectionUtils.find(counts, new Predicate<ReminderCount>() {
            @Override
            public boolean evaluate(ReminderCount object) {
                return object.getCount() == count;
            }
        });
    }

    /**
     * Calculates the next due date for a reminder.
     *
     * @param dueDate       the due date
     * @param reminderCount the no. of times a reminder has been sent
     * @return the next due date for the reminder, or {@code null} if the reminder has no next due date
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Date getNextDueDate(Date dueDate, int reminderCount) {
        ReminderCount count = getReminderCount(reminderCount);
        return count != null ? count.getNextDueDate(dueDate) : null;
    }

    /**
     * Determines if reminders can be grouped with others.
     *
     * @return the grouping policy
     */
    public GroupBy getGroupBy() {
        return groupBy;
    }

    /**
     * Returns the reminder groups.
     *
     * @return the reminder groups
     */
    public List<Lookup> getGroups() {
        return groups;
    }

    /**
     * Determines if the reminder is interactive.
     *
     * @return {@code true} if the reminder is interactive, otherwise {@code false}
     */
    public boolean isInteractive() {
        return interactive;
    }

    /**
     * Helper to return the date units for a particular node, or default units if none are present.
     *
     * @param bean         the bean
     * @param node         the node name
     * @param defaultUnits the default units to use if none are specified
     * @return the date units
     */
    private static DateUnits getDateUnits(IMObjectBean bean, String node, DateUnits defaultUnits) {
        String units = bean.getString(node);
        return (!StringUtils.isEmpty(units)) ? DateUnits.valueOf(units) : defaultUnits;
    }

}
