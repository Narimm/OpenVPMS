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

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.practice.Location;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeSortConstraint;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.component.system.common.query.ObjectSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ShortNameConstraint;

import java.util.Date;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.gte;
import static org.openvpms.component.system.common.query.Constraints.idEq;
import static org.openvpms.component.system.common.query.Constraints.isNull;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.notExists;
import static org.openvpms.component.system.common.query.Constraints.shortName;
import static org.openvpms.component.system.common.query.Constraints.sort;
import static org.openvpms.component.system.common.query.Constraints.subQuery;

/**
 * A factory for creating queries operating on <em>act.patientReminderItem*</em> archetypes.
 * <p>
 * The queries return {@link ObjectSet}s containing:
 * <ul>
 * <li>item - an <em>act.patientReminderItem*</em></li>
 * <li>reminder - the associated <em>act.patientReminder</em></li>
 * <li>patient - the patient linked to <em>reminder</em></li>
 * <li>customer - the customer linked to <em>patient</em></li>
 * </ul>
 * These are sorted on:
 * <ul>
 * <li>customer name</li>
 * <li>customer id</li>
 * <li>patient name</li>
 * <li>item startTime</li>
 * <li>item id</li>
 * </ul>
 *
 * @author Tim Anderson
 */
public class ReminderItemQueryFactory {

    /**
     * The archetypes to query.
     */
    private String[] archetypes;

    /**
     * The from date. May be {@code null}
     */
    private Date from;

    /**
     * The to date. May be {@code null}
     */
    private Date to;

    /**
     * The statuses.
     */
    private String[] statuses;

    /**
     * The customer.
     */
    private IMObjectReference customerRef;

    /**
     * The customer's practice location.
     */
    private Location location = Location.ALL;


    /**
     * Constructs a {@link ReminderItemQueryFactory}.
     */
    public ReminderItemQueryFactory() {
        this(ReminderArchetypes.REMINDER_ITEMS);
    }

    /**
     * Constructs a {@link ReminderItemQueryFactory}.
     *
     * @param archetype the archetypes to query. May contain wildcards
     */
    public ReminderItemQueryFactory(String archetype) {
        setArchetype(archetype);
    }

    /**
     * Constructs a {@link ReminderItemQueryFactory}.
     *
     * @param archetype the archetype to query. May contain wildcards
     * @param status    the status
     */
    public ReminderItemQueryFactory(String archetype, String status) {
        setArchetype(archetype);
        setStatus(status);
    }

    /**
     * Constructs a {@link ReminderItemQueryFactory}
     *
     * @param archetype the archetype to query. May contain wildcards
     * @param statuses  the statuses. May be {@code null}
     * @param from      the from date range. If non-null, all reminder items with a {@code startTime} less than that
     *                  specified will be excluded.
     * @param to        the to date range. If non-null, all reminder items with a {@code startTime} greater than or
     *                  equal to that specified will be excluded.
     */
    public ReminderItemQueryFactory(String archetype, String[] statuses, Date from, Date to) {
        this(new String[]{archetype}, statuses, from, to);
    }

    /**
     * Constructs a {@link ReminderItemQueryFactory}
     *
     * @param archetypes the archetypes to query. May contain wildcards
     * @param statuses   the statuses. May be {@code null}
     * @param from       the from date range. If non-null, all reminder items with a {@code startTime} less than that
     *                   specified will be excluded.
     * @param to         the to date range. If non-null, all reminder items with a {@code startTime} greater than or
     *                   equal to that specified will be excluded.
     */
    public ReminderItemQueryFactory(String[] archetypes, String[] statuses, Date from, Date to) {
        setArchetypes(archetypes);
        setStatuses(statuses);
        setFrom(from);
        setTo(to);
    }

    /**
     * Sets the archetype short name to query.
     *
     * @param archetype the archetype to query. May contain wildcards
     */
    public void setArchetype(String archetype) {
        setArchetypes(new String[]{archetype});
    }

    /**
     * Sets the archetype short name to query.
     *
     * @param archetypes the archetype to query. May contain wildcards
     */
    public void setArchetypes(String[] archetypes) {
        if (!TypeHelper.matches(archetypes, ReminderArchetypes.REMINDER_ITEMS)) {
            throw new IllegalArgumentException("Invalid reminder item names: " + StringUtils.join(archetypes, ','));
        }
        this.archetypes = archetypes;
    }

    /**
     * Returns the archetypes to query.
     *
     * @return the archetypes to query
     */
    public String[] getArchetypes() {
        return archetypes;
    }

    /**
     * Returns the start of the date range.
     *
     * @return the start of the date range. May be {@code null}
     */
    public Date getFrom() {
        return from;
    }

    /**
     * Sets the date range to filter.
     *
     * @param from the start of the date range. If non-null, all reminder items with a {@code startTime} less than that
     *             specified will be excluded.
     */
    public void setFrom(Date from) {
        this.from = from;
    }

    /**
     * Returns the end of the date range.
     *
     * @return the start of the date range. May be {@code null}
     */
    public Date getTo() {
        return to;
    }

    /**
     * Sets the end of the date range.
     *
     * @param to the end of the date range. If non-null, all reminder items with a {@code startTime} greater than or
     *           equal to that specified will be excluded.
     */
    public void setTo(Date to) {
        this.to = to;
    }

    /**
     * Sets the status to include.
     *
     * @param status the status, or {@code null} to include all statuses
     */
    public void setStatus(String status) {
        setStatuses(status != null ? new String[]{status} : null);
    }

    /**
     * Sets the statuses to include.
     *
     * @param statuses the statuses, or {@code null} to include all statuses
     */
    public void setStatuses(String[] statuses) {
        this.statuses = statuses;
    }

    /**
     * Returns the statuses to include.
     *
     * @return the statuses. May be {@code null}
     */
    public String[] getStatuses() {
        return statuses;
    }

    /**
     * Sets the customer to constrain items to.
     *
     * @param customer the customer. May be  {@code null}
     */
    public void setCustomer(Party customer) {
        customerRef = (customer != null) ? customer.getObjectReference() : null;
    }

    /**
     * Sets the location to query.
     * <p>
     * Defaults to {@link Location#ALL}.
     *
     * @param location the location
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Creates a new query.
     *
     * @return a new query
     */
    public ArchetypeQuery createQuery() {
        ArchetypeQuery query = new ArchetypeQuery(shortName("item", archetypes, false));
        query.add(new ObjectSelectConstraint("item"));
        query.add(new ObjectSelectConstraint("reminder"));
        query.add(new ObjectSelectConstraint("patient"));
        query.add(new ObjectSelectConstraint("customer"));
        if (from != null) {
            query.add(gte("startTime", from));
        }
        if (to != null) {
            query.add(Constraints.lt("startTime", to));
        }
        if (statuses != null && statuses.length != 0) {
            query.add(Constraints.in("status", statuses));
        }
        JoinConstraint reminder = join("source", "reminder");
        JoinConstraint patient = join("entity", "patient");
        reminder.add(eq("status", ActStatus.IN_PROGRESS)).add(join("patient", "p").add(patient));
        ShortNameConstraint owner = shortName("owner", PatientArchetypes.PATIENT_OWNER);
        ShortNameConstraint customer = shortName("customer", CustomerArchetypes.PERSON, true);

        owner.add(isNull("activeEndTime")); // only include customers with an open-ended owner relationship

        query.add(join("reminder", "r").add(reminder));
        query.add(owner);
        query.add(customer);

        if (customerRef != null) {
            customer.add(eq("id", customerRef.getId()));
        }
        if (location.getLocation() != null) {
            customer.add(join("practice", "l2").add(eq("target", location.getLocation())));
        } else if (location.isNone()) {
            query.add(notExists(subQuery(CustomerArchetypes.PERSON, "c2").add(
                    join("practice", "l2").add(idEq("customer", "c2")))));
        }

        query.add(idEq("patient", "owner.target"));
        query.add(idEq("customer", "owner.source"));
        query.add(sort("customer", "name"));
        query.add(sort("customer", "id"));
        query.add(new ArchetypeSortConstraint(true));
        query.add(sort("patient", "name"));
        query.add(sort("patient", "id"));
        query.add(sort("item", "startTime"));
        query.add(sort("item", "id"));
        return query;
    }

    /**
     * Copies this instance, for a single archetype.
     *
     * @param archetype the archetype
     * @return a copy of this, with a single archetype populated
     */
    public ReminderItemQueryFactory copy(String archetype) {
        ReminderItemQueryFactory result = new ReminderItemQueryFactory(archetype, statuses, from, to);
        result.customerRef = customerRef;
        result.location = location;
        return result;
    }
}
