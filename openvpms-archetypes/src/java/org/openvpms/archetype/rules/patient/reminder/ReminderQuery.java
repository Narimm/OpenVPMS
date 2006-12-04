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
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.IdConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.component.system.common.query.ShortNameConstraint;

import java.util.Date;


/**
 * Queries <em>act.patientReminders</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReminderQuery {

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * The reminder type.
     */
    private Entity reminderType;

    /**
     * The start due date.
     */
    private Date dueFrom;

    /**
     * The end due date.
     */
    private Date dueTo;

    /**
     * The start customer name.
     */
    private String customerFrom;

    /**
     * The end customer name.
     */
    private String customerTo;


    /**
     * Constructs a new <code>ReminderQuery</code>.
     */
    public ReminderQuery() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Constructs a new <code>ReminderQuery</code>.
     *
     * @param service the archetype service
     */
    public ReminderQuery(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Sets the reminder type.
     *
     * @param reminderType the reminder type. If <code>null</code> indicates to
     *                     query all reminder types.
     */
    public void setReminderType(Entity reminderType) {
        this.reminderType = reminderType;
    }

    /**
     * Sets the due date range.
     * If either date is null, indicates to query all due dates.
     *
     * @param from the from date. May be <code>null</code>
     * @param to   the to date. May be <code>null</code>
     */
    public void setDueDateRange(Date from, Date to) {
        dueFrom = from;
        dueTo = to;
    }

    /**
     * Sets the customer range.
     * If either name is null, indicates to query all customers.
     *
     * @param from the from customer name
     * @param to   the to customer name
     */
    public void setCustomerRange(String from, String to) {
        customerFrom = from;
        customerTo = to;
    }

    /**
     * Executes the query.
     *
     * @param firstResult the first row to return
     * @param maxResults  the maximum no. of results to return.
     *                    Use {@link ArchetypeQuery#ALL_RESULTS} to specify all
     *                    results
     * @return the query result
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IPage<Act> query(int firstResult, int maxResults) {
        ShortNameConstraint act = new ShortNameConstraint("act",
                                                          "act.patientReminder",
                                                          true);
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

        ArchetypeQuery query = new ArchetypeQuery(act);
        query.setFirstResult(firstResult);
        query.setMaxResults(maxResults);
        query.setCountResults(true);
        query.setDistinct(true);

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

        if (reminderType != null) {
            query.add(
                    new CollectionNodeConstraint("reminderType", reminder).add(
                            new ObjectRefNodeConstraint("entity",
                                                        reminderType.getObjectReference())));
        }
        if (dueFrom != null && dueTo != null) {
            query.add(new NodeConstraint("endTime", RelationalOp.BTW,
                                         dueFrom, dueTo));
        }
        if (customerFrom != null && customerTo != null) {
            query.add(new NodeConstraint("customer.name", RelationalOp.BTW,
                                         customerFrom, customerTo));
        }
        return query(query);
    }

    /**
     * Executes a query.
     *
     * @param query the query
     * @return the query result
     */
    @SuppressWarnings("unchecked")
    private IPage<Act> query(ArchetypeQuery query) {
        IPage result;
        result = service.get(query);
        return result;
    }

}