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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.patient.reminder;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.IdConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.component.system.common.query.ShortNameConstraint;

import java.util.Date;
import java.util.Iterator;


/**
 * Queries <em>act.act.patientReminder</em> acts, returning a limited set of
 * data for performance purposes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReminderQuery {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

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
     * Constructs a new <tt>ReminderQuery</tt>.
     */
    public ReminderQuery() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Constructs  a new <tt>ReminderQuery</tt>.
     */
    public ReminderQuery(IArchetypeService service) {
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
    }

    /**
     * Sets the due date range.
     * If either date is null, indicates to query all due dates.
     *
     * @param from the from due date. May be <tt>null</tt>
     * @param to   the to due date. May be <tt>null</tt>
     */
    public void setDueDateRange(Date from, Date to) {
        this.from = from;
        this.to = to;
    }


    /**
     * Returns an iterator over the reminder acts matching the query
     * criteria.
     * Note that each iteration may throw {@link ArchetypeServiceException}.
     *
     * @return an iterator over the reminder acts
     */
    public Iterable<Act> query() {
        return new Iterable<Act>() {
            public Iterator<Act> iterator() {
                return new IMObjectQueryIterator<Act>(service, createQuery());
            }
        };
    }

    public ArchetypeQuery createQuery() {
        ShortNameConstraint act = new ShortNameConstraint("act",
                                                          "act.patientReminder",
                                                          true);
        ArchetypeQuery query = new ArchetypeQuery(act);
        query.setDistinct(true);

        ShortNameConstraint participation = new ShortNameConstraint(
                "participation", "participation.patient", true);
        ShortNameConstraint owner = new ShortNameConstraint(
                "owner", "entityRelationship.patientOwner", true);
        ShortNameConstraint patient = new ShortNameConstraint(
                "patient", "party.patientpet", true);
        ShortNameConstraint customer = new ShortNameConstraint(
                "customer", "party.customer*", true);
        ShortNameConstraint reminder = new ShortNameConstraint(
                "reminderType", "participation.reminderType", true);

        query.add(new NodeConstraint("status", ReminderStatus.IN_PROGRESS));

        query.add(new CollectionNodeConstraint("patient", participation));
        query.add(new IdConstraint("act", "participation.act"));
        query.add(owner);
        query.add(patient);
        query.add(customer);
        query.add(new IdConstraint("participation.entity", "patient"));
        query.add(new IdConstraint("patient", "owner.target"));
        query.add(new IdConstraint("customer", "owner.source"));
        query.add(new NodeSortConstraint("customer", "name"));
        query.add(new NodeSortConstraint("patient", "name"));
        query.add(new NodeSortConstraint("act", "endTime"));

        if (reminderType != null) {
            ObjectRefNodeConstraint objRef
                    = new ObjectRefNodeConstraint(
                    "entity", reminderType.getObjectReference());
            CollectionNodeConstraint constraint = new CollectionNodeConstraint(
                    "reminderType", reminder);
            constraint.add(objRef);
            query.add(constraint);
        } else {
            query.add(reminder);
            query.add(new IdConstraint("reminderType.act", "act"));
        }
        if (from != null && to != null) {
            query.add(new NodeConstraint("endTime", RelationalOp.BTW,
                                         from, to));
        }
        return query;
    }

    /**
     * Adapts an <tt>Iterable<ObjectSet></tt> returned by the { @link #query} to an
     */
    /**
     *
     private class IterableObjectSetAdapter implements Iterable<Act> {

     private final Iterable<ObjectSet> objects;


     public IterableObjectSetAdapter(Iterable<ObjectSet> objects) {
     this.objects = objects;
     }

     public Iterator<Act> iterator() {
     return new ReminderIteratorAdaptor(objects.iterator());
     }

     private class ReminderIteratorAdaptor implements Iterator<Act> {
     private final Iterator<ObjectSet> iterator;
     private Act next;

     public ReminderIteratorAdaptor(Iterator<ObjectSet> iterator) {
     this.iterator = iterator;
     }

     public boolean hasNext() {
     // scan through the iterator until the end is reached or
     // an act can be retrieved
     while (next == null && iterator.hasNext()) {
     ObjectSet set = iterator.next();
     IMObjectReference ref = (IMObjectReference) set.get(
     ReminderQuery.ACT_REFERENCE);
     if (ref != null) {
     next = (Act) ArchetypeQueryHelper.getByObjectReference(
     service, ref);
     }
     }
     return next != null;
     }

     public Act next() {
     if (next == null) {
     throw new NoSuchElementException();
     }
     return next;
     }

     public void remove() {
     iterator.remove();
     }
     }
     }
     */

}