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

import java.util.Date;
import java.util.Iterator;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
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


/**
 * Queries <em>act.patientReminder</em> acts.
 * The acts are sorted on customer name, patient name and endTime.
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
     *
     * @param service the archetype service
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
                return new IMObjectQueryIterator<Act>(service, createQuery());
            }
        };
    }

    /**
     * Returns a new {@link ArchetypeQuery} matching the constraints.
     *
     * @return a new query
     */
    public ArchetypeQuery createQuery() {
        ShortNameConstraint act = new ShortNameConstraint("act", ReminderArchetypes.REMINDER, true);
        ArchetypeQuery query = new ArchetypeQuery(act);
        query.setMaxResults(1000);
        query.setDistinct(true);

        ShortNameConstraint participation = new ShortNameConstraint(
                "participation", "participation.patient", true);
        ShortNameConstraint owner = new ShortNameConstraint(
                "owner", "entityRelationship.patientOwner", false);
        ShortNameConstraint patient = new ShortNameConstraint(
                "patient", "party.patientpet", true);
        ShortNameConstraint customer = new ShortNameConstraint(
                "customer", "party.customer*", true);
        ShortNameConstraint reminder = new ShortNameConstraint(
                "reminderType", ReminderArchetypes.REMINDER_TYPE_PARTICIPATION, true);

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
        if (from != null) {
            query.add(new NodeConstraint("endTime", RelationalOp.GTE, DateRules.getDate(from)));
        }
        if (to != null) {
        	Date tempTo = DateRules.getDate(to); // truncat eto date to date only
        	tempTo = DateRules.getDate(tempTo, 1, DateUnits.DAYS);  //Add one day 
            query.add(new NodeConstraint("endTime", RelationalOp.LT, tempTo));
        }
        return query;
    }


}