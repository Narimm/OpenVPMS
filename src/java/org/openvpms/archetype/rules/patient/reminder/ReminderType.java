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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.patient.reminder;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Wrapper around an <em>entity.reminderType</tt>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReminderType {

    /**
     * The underlying <em>entity.reminderType</em>.
     */
    private final Entity reminderType;

    /**
     * The reminder type's templates.
     */
    private final List<Template> templates;

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
    private final boolean canGroup;

    /**
     * Creates a new <tt>ReminderType</tt>.
     *
     * @param reminderType the <em>entity.reminderType</em>
     */
    public ReminderType(Entity reminderType) {
        this(reminderType, ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>ReminderType</tt>.
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
        templates = new ArrayList<Template>();
        for (EntityRelationship template
                : bean.getValues("templates", EntityRelationship.class)) {
            templates.add(new Template(template, service));
        }
        canGroup = bean.getBoolean("group");
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
     * @return <tt>true</tt> if the remidner type is active, <tt>false</tt> if
     *         it is inactive
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
     * <tt>dueDate + (cancelInterval * cancelUnits) &lt;= date</tt>
     *
     * @param dueDate the due date
     * @param date    the date
     * @return <tt>true</tt> if the reminder needs to be cancelled,
     *         otherwise <tt>false</tt>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public boolean shouldCancel(Date dueDate, Date date) {
        Date cancelDate = getCancelDate(dueDate);
        return (cancelDate.getTime() <= date.getTime());
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
     * Returns the cancel interval's units.
     *
     * @return the cancel interval's units
     */
    public DateUnits getCancelUnits() {
        return cancelUnits;
    }

    /**
     * Returns the template relationship for a particular reminder count.
     *
     * @param reminderCount the reminder count
     * @return the template relationship or <tt>null</tt> if none is found
     */
    public EntityRelationship getTemplateRelationship(int reminderCount) {
        Template template = getTemplate(reminderCount);
        return (template != null) ? template.getRelationship() : null;
    }

    /**
     * Calculates the next due date for a reminder.
     *
     * @param dueDate       the due date
     * @param reminderCount the no. of times a reminder has been sent
     * @return the next due date for the reminder, or <tt>null</tt> if the reminder has no next due date
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Date getNextDueDate(Date dueDate, int reminderCount) {
        Template template = getTemplate(reminderCount);
        if (template != null) {
            return DateRules.getDate(dueDate, template.getInterval(),
                                     template.getUnits());
        }
        return null;
    }

    /**
     * Determines if a reminder is due in the specified date range.
     * <p/>
     * NOTE: any time component of the specified dates is ignored. 
     *
     * @param dueDate       the reminder's due date
     * @param reminderCount the no. of times the reminder has been sent
     * @param from          the 'from' due date. May be <tt>null</tt>
     * @param to            the 'to' due date. May be <tt>null</tt>
     * @return <tt>true</tt> if the reminder is due, otherwise <tt>false</tt>
     */
    public boolean isDue(Date dueDate, int reminderCount, Date from, Date to) {
        Template template = getTemplate(reminderCount);
        Date nextDue;
        if (template == null) {
            nextDue = dueDate;
        } else {
            nextDue = getNextDueDate(dueDate, reminderCount);
        }
        if (from != null) {
            from = DateRules.getDate(from); // remove time component
        }
        if (to != null) {
            to = DateRules.getDate(to); // remove time component
            to = DateRules.getDate(to, 1, DateUnits.DAYS); // add one day to get all reminders prior to this date
        }
        nextDue = DateRules.getDate(nextDue);

        return (from == null || nextDue.getTime() >= from.getTime())
               && (to == null || nextDue.getTime() < to.getTime());
    }

    /**
     * Determines if the reminder can be grouped with other reminders in a single report.
     *
     * @return <tt>true</tt> if the reminder can be grouped, otherwise <tt>false</tt>
     */
    public boolean canGroup() {
        return canGroup;
    }

    /**
     * Returns the template for a reminder count
     *
     * @param reminderCount the reminder count
     * @return the template relationship or <tt>null</tt> if none is found
     */
    private Template getTemplate(int reminderCount) {
        for (Template template : templates) {
            if (template.getReminderCount() == reminderCount) {
                return template;
            }
        }
        return null;
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

    /**
     * Wrapper around an <tt>entityRelationship.reminderTypeTemplate</tt> to
     * improve performance.
     */
    private static class Template {

        /**
         * The <tt>entityRelationship.reminderTypeTemplate</tt>.
         */
        private final EntityRelationship relationship;

        /**
         * The reminder count when this template should be used.
         */
        private final int reminderCount;

        /**
         * The date interval.
         */
        private final int interval;

        /**
         * The date interval units.
         */
        private final DateUnits units;

        /**
         * Creates a new <tt>Template</tt>.
         *
         * @param relationship the relationship
         * @param service      the archetype service
         */
        public Template(EntityRelationship relationship,
                        IArchetypeService service) {
            IMObjectBean templateBean = new IMObjectBean(relationship, service);
            reminderCount = templateBean.getInt("reminderCount");
            interval = templateBean.getInt("interval");
            units = getDateUnits(templateBean, "units", DateUnits.DAYS);
            this.relationship = relationship;
        }

        /**
         * Returns the <tt>entityRelationship.reminderTypeTemplate</tt>.
         *
         * @return the <tt>entityRelationship.reminderTypeTemplate</tt>
         */
        public EntityRelationship getRelationship() {
            return relationship;
        }

        /**
         * Returns the reminder count when this template should be used.
         *
         * @return the reminder count
         */
        public int getReminderCount() {
            return reminderCount;
        }

        /**
         * The date interval.
         *
         * @return the date interval
         */
        public int getInterval() {
            return interval;
        }

        /**
         * The date interval units.
         *
         * @return the date interval units
         */
        public DateUnits getUnits() {
            return units;
        }
    }

}
