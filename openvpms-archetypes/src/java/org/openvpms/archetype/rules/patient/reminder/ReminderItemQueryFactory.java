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
import org.openvpms.archetype.rules.patient.PatientArchetypes;
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
import static org.openvpms.component.system.common.query.Constraints.isNull;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.shortName;
import static org.openvpms.component.system.common.query.Constraints.sort;

/**
 * A factory for creating queries operating on <em>act.patientReminderItem*</em> archetypes.
 * <p/>
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
     * The short names to query.
     */
    private String[] shortNames;

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
     * Constructs a {@link ReminderItemQueryFactory}.
     */
    public ReminderItemQueryFactory() {
        this(ReminderArchetypes.REMINDER_ITEMS);
    }

    /**
     * Constructs a {@link ReminderItemQueryFactory}.
     *
     * @param shortName the archetype short name to query. May contain wildcards
     */
    public ReminderItemQueryFactory(String shortName) {
        setShortName(shortName);
    }

    /**
     * Constructs a {@link ReminderItemQueryFactory}.
     *
     * @param shortName the archetype short name to query. May contain wildcards
     * @param status    the status
     */
    public ReminderItemQueryFactory(String shortName, String status) {
        setShortName(shortName);
        setStatus(status);
    }

    /**
     * Constructs a {@link ReminderItemQueryFactory}
     *
     * @param shortName the archetype short name to query. May contain wildcards
     * @param statuses  the statuses. May be {@code null}
     * @param from      the from date range. If non-null, all reminder items with a {@code startTime} less than that
     *                  specified will be excluded.
     * @param to        the to date range. If non-null, all reminder items with a {@code startTime} greater than or
     *                  equal to that specified will be excluded.
     */
    public ReminderItemQueryFactory(String shortName, String[] statuses, Date from, Date to) {
        setShortName(shortName);
        setStatuses(statuses);
        setFrom(from);
        setTo(to);
    }

    /**
     * Sets the archetype short name to query.
     *
     * @param shortName the archetype short name to query. May contain wildcards
     */
    public void setShortName(String shortName) {
        setShortNames(new String[]{shortName});
    }

    /**
     * Sets the archetype short name to query.
     *
     * @param shortNames the archetype short names to query. May contain wildcards
     */
    public void setShortNames(String[] shortNames) {
        if (!TypeHelper.matches(shortNames, ReminderArchetypes.REMINDER_ITEMS)) {
            throw new IllegalArgumentException("Invalid reminder item names: " + StringUtils.join(shortNames, ','));
        }
        this.shortNames = shortNames;
    }

    /**
     * Returns the archetype short names to query.
     *
     * @return the archetype short names to query
     */
    public String[] getShortNames() {
        return shortNames;
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
     * Creates a new query.
     *
     * @return a new query
     */
    public ArchetypeQuery createQuery() {
        ArchetypeQuery query = new ArchetypeQuery(shortName("item", shortNames, false));
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
        JoinConstraint customer = join("source", "customer");
        reminder.add(eq("status", ActStatus.IN_PROGRESS)).add(join("patient", "p").add(patient));
        ShortNameConstraint owner = shortName("owner", PatientArchetypes.PATIENT_OWNER);
        owner.add(isNull("activeEndTime")); // only include customers with an open-ended owner relationship
        patient.add(join("customers", owner).add(customer));
        query.add(join("reminder", "r").add(reminder));
        query.add(sort("customer", "name"));
        query.add(sort("customer", "id"));
        query.add(sort("patient", "name"));
        query.add(sort("item", "startTime"));
        query.add(sort("item", "id"));
        query.add(new ArchetypeSortConstraint(true));
        return query;
    }

}
