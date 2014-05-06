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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.patient.reminder;

import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.practice.Location;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.IdConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.gte;
import static org.openvpms.component.system.common.query.Constraints.idEq;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.notExists;
import static org.openvpms.component.system.common.query.Constraints.shortName;
import static org.openvpms.component.system.common.query.Constraints.sort;
import static org.openvpms.component.system.common.query.Constraints.subQuery;


/**
 * Queries <em>act.patientReminder</em> acts.
 * The acts are sorted on customer name, patient name and endTime.
 *
 * @author Tim Anderson
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
     * The customer.
     */
    private Party customer;

    /**
     * The location.
     */
    private Location location = Location.ALL;


    /**
     * Constructs a {@link ReminderQuery}.
     *
     * @param service the archetype service
     */
    public ReminderQuery(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Sets the reminder type.
     *
     * @param reminderType an <em>entity.reminderType</em>. If {@code null} indicates to query all reminder types
     */
    public void setReminderType(Entity reminderType) {
        this.reminderType = reminderType;
    }

    /**
     * Sets the 'from' date.
     * <p/>
     * This excludes all reminders with a due date prior to the specified date.
     * <p/>
     * Any time component is ignored.
     *
     * @param from the from date. If {@code null} don't set a lower bound for due dates
     */
    public void setFrom(Date from) {
        this.from = from;
    }

    /**
     * Sets the 'to' date.
     * <p/>
     * This filters excludes reminders with a due date after the specified date.
     * <p/>
     * Any time component is ignored.
     *
     * @param to the to date. If {@code null} don't set an upper bound for due
     *           dates
     */
    public void setTo(Date to) {
        this.to = to;
    }

    /**
     * Sets the customer.
     *
     * @param customer the customer. May be {@code null}
     */
    public void setCustomer(Party customer) {
        this.customer = customer;
    }

    /**
     * Sets the location to query.
     * <p/>
     * Defaults to {@link Location#ALL}.
     *
     * @param location the location
     */
    public void setLocation(Location location) {
        this.location = location;
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
     * Executes the query.
     *
     * @return a list of the reminder acts matching the query criteria
     */
    public List<Act> execute() {
        List<Act> result = new ArrayList<Act>();
        for (Act act : query()) {
            result.add(act);
        }
        return result;
    }

    /**
     * Returns a new {@link ArchetypeQuery} matching the constraints.
     *
     * @return a new query
     */
    public ArchetypeQuery createQuery() {
        ShortNameConstraint act = shortName("act", ReminderArchetypes.REMINDER, true);
        ArchetypeQuery query = new ArchetypeQuery(act);
        query.setMaxResults(1000);
        query.setDistinct(true);

        query.add(eq("status", ReminderStatus.IN_PROGRESS));

        query.add(join("patient", shortName("participation", PatientArchetypes.PATIENT_PARTICIPATION, true)));
        query.add(new IdConstraint("act", "participation.act"));
        query.add(shortName("owner", PatientArchetypes.PATIENT_OWNER, false));
        query.add(shortName("patient", PatientArchetypes.PATIENT, true));
        ShortNameConstraint cust = shortName("customer", CustomerArchetypes.PERSON, true);
        if (customer != null) {
            cust.add(eq("id", customer.getId()));
        }
        if (location.getLocation() != null) {
            cust.add(join("practice", "l2").add(eq("target", location.getLocation())));
        }
        query.add(cust);

        if (location.isNone()) {
            query.add(notExists(subQuery(CustomerArchetypes.PERSON, "c2").add(
                    join("practice", "l2").add(idEq("customer", "c2")))));
        }

        query.add(new IdConstraint("participation.entity", "patient"));
        query.add(new IdConstraint("patient", "owner.target"));
        query.add(new IdConstraint("customer", "owner.source"));
        query.add(sort("customer", "name"));
        query.add(sort("patient", "name"));
        query.add(sort("act", "endTime"));

        ShortNameConstraint reminder = shortName("reminderType", ReminderArchetypes.REMINDER_TYPE_PARTICIPATION, true);
        if (reminderType != null) {
            query.add(join("reminderType", reminder).add(eq("entity", reminderType)));
        } else {
            query.add(reminder);
            query.add(new IdConstraint("reminderType.act", "act"));
        }
        if (from != null) {
            query.add(gte("endTime", DateRules.getDate(from)));
        }
        if (to != null) {
            // remove any time component and add 1 day
            Date tempTo = DateRules.getDate(to);
            tempTo = DateRules.getDate(tempTo, 1, DateUnits.DAYS);
            query.add(Constraints.lt("endTime", tempTo));
        }
        return query;
    }


}