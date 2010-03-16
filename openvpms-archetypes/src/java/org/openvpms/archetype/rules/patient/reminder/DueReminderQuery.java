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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * Query for due reminders.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DueReminderQuery {

    /**
     * The reminder type.
     */
    private Entity reminderType;

    /**
     * The 'from' due date.
     */
    private Date from;

    /**
     * The 'to' due date.
     */
    private Date to;

    /**
     * The 'cancel' date.
     */
    private Date cancel;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * A cache of reminder types.
     */
    private final ReminderTypeCache reminderTypes;


    /**
     * Constructs a new <tt>DueReminderQuery</tt>.
     */
    public DueReminderQuery() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Constructs a new <tt>DueReminderQuery</tt>.
     *
     * @param service the archetype service
     */
    public DueReminderQuery(IArchetypeService service) {
        reminderTypes = new ReminderTypeCache(service);
        this.service = service;
    }

    /**
     * Sets the reminder type.
     *
     * @param reminderType an <em>entity.reminderType</em>. If <tt>null</tt>
     *                     indicates to query all reminder types
     */
    public void setReminderType(Entity reminderType) {
        this.reminderType = reminderType;
        if (reminderType != null) {
            reminderTypes.add(reminderType);
        }
    }

    /**
     * Sets the 'from' date.
     * This excludes all reminders with a due date prior to the specified
     * date.
     *
     * @param from the from date. If <tt>null</tt> don't set a lower bound for
     *             due dates
     */
    public void setFrom(Date from) {
        this.from = from;
    }

    /**
     * Returns the 'from' date.
     *
     * @return the 'from' date. May be <tt>null</tt>
     */
    public Date getFrom() {
        return from;
    }

    /**
     * Sets the 'to' date.
     * This filters excludes reminders with a due date after the specified
     * date.
     *
     * @param to the to date. If <tt>null</tt> don't set an upper bound for due
     *           dates
     */
    public void setTo(Date to) {
        this.to = to;
    }

    /**
     * Returns the 'to' date.
     *
     * @return the 'to' date. May be <tt>null</tt>
     */
    public Date getTo() {
        return to;
    }

    /**
     * Sets the cancel date. Reminder's whose next due date is prior to this
     * are returned for cancelling. If <tt>null</tt>, no reminder will be
     * returned for cancelling.
     *
     * @param cancel the cancel date. May be <tt>null</tt>
     */
    public void setCancelDate(Date cancel) {
        this.cancel = cancel;
    }

    /**
     * Returns an iterator over the reminder acts matching the query
     * criteria.
     * Note that each iteration may throw {@link ArchetypeServiceException}.
     * Also note that the behaviour is undefined if any of the attributes
     * are modified while an iteration is in progress.
     *
     * @return an iterator over the reminder acts
     */
    public Iterable<Act> query() {
        return new Iterable<Act>() {
            public Iterator<Act> iterator() {
                ReminderQuery query = new ReminderQuery(service);
                query.setTo(to);
                query.setReminderType(reminderType);
                return new DueIterator(query.query().iterator());
            }
        };
    }

    /**
     * Iterator over a collection of reminders that only returns those
     * that are due.
     */
    class DueIterator implements Iterator<Act> {

        /**
         * The iterator to delegate to.
         */
        private final Iterator<Act> iterator;

        /**
         * The next reminder to return.
         */
        private Act next;

        /**
         * @param iterator the reminder iterator
         */
        public DueIterator(Iterator<Act> iterator) {
            this.iterator = iterator;
        }

        /**
         * Returns <tt>true</tt> if the iteration has more elements.
         *
         * @return <tt>true</tt> if the iterator has more elements
         */
        public boolean hasNext() {
            if (next == null) {
                next = getNext();
            }
            return (next != null);
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration.
         * @throws NoSuchElementException iteration has no more elements.
         */
        public Act next() {
            if (next == null) {
                next = getNext();
                if (next == null) {
                    throw new NoSuchElementException();
                }
            }
            Act result = next;
            next = null;
            return result;
        }

        /**
         * Not supported.
         *
         * @throws UnsupportedOperationException if invoked
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /**
         * Returns the next available act.
         *
         * @return the next available act, or <tt>null</tt>
         * @throws ArchetypeServiceException for any archetype service error
         */
        private Act getNext() {
            Act result = null;
            while (iterator.hasNext()) {
                Act reminder = iterator.next();
                ActBean bean = new ActBean(reminder, service);
                IMObjectReference ref = bean.getParticipantRef(ReminderArchetypes.REMINDER_TYPE_PARTICIPATION);
                ReminderType reminderType = reminderTypes.get(ref);
                if (reminderType != null) {
                    if (shouldCancel(reminder, reminderType)
                            || isDue(bean, reminderType)) {
                        result = reminder;
                        break;
                    }
                }
            }
            return result;
        }

        /**
         * Determines if a reminder should be cancelled.
         *
         * @param reminder     the reminder
         * @param reminderType the reminder type
         * @return <tt>true</tt> if the reminder should be cancelled
         */
        private boolean shouldCancel(Act reminder, ReminderType reminderType) {
            return cancel != null && reminderType.shouldCancel(reminder.getActivityEndTime(), cancel);
        }

        /**
         * Determines if a reminder is due, relative to the 'from' and 'to'
         * dates.
         *
         * @param reminder     the reminder
         * @param reminderType the reminder type
         * @return <tt>true</tt> if the reminder is due
         */
        private boolean isDue(ActBean reminder, ReminderType reminderType) {
            int reminderCount = reminder.getInt("reminderCount");
            return reminderType.isDue(reminder.getAct().getActivityEndTime(),
                                      reminderCount, from, to);
        }
    }

}
