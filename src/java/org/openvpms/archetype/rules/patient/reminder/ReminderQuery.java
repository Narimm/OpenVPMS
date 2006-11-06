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
import org.openvpms.component.system.common.query.IPage;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


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
     * @param firstRow the first row to return
     * @param maxRows  the maximum no. of row to return.
     *                 Use {@link ArchetypeQuery#ALL_ROWS} to specify all rows
     * @return the query result
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IPage<Act> query(int firstRow, int maxRows) {
        Map<String, Object> params = new HashMap<String, Object>();
        String name = "act.patientReminder.IN_PROGRESS";
        if (reminderType != null) {
            name += "+reminderType";
            params.put("linkId", reminderType.getLinkId());
        }
        if (dueFrom != null && dueTo != null) {
            name += "+dueDateRange";
            params.put("dueFrom", dueFrom);
            params.put("dueTo", dueTo);
        }
        if (customerFrom != null && customerTo != null) {
            name += "+customerRange";
            params.put("nameFrom", customerFrom);
            params.put("nameTo", customerTo);
        }
        return query(name, params, firstRow, maxRows);

    }

    /**
     * Executes a query.
     *
     * @param queryName the query name
     * @param params    the query parameters
     * @param firstRow  the first row to return
     * @param maxRows   the maximum no. of row to return
     * @return the query result
     */
    @SuppressWarnings("unchecked")
    private IPage<Act> query(String queryName, Map<String, Object> params,
                             int firstRow, int maxRows) {
        IPage result = service.getByNamedQuery(queryName, params, firstRow,
                                               maxRows);
        return (IPage<Act>) result;
    }

}