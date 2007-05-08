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

package org.openvpms.web.app.workflow.worklist;

import org.openvpms.archetype.rules.workflow.TaskStatus;
import static org.openvpms.archetype.rules.workflow.WorkflowStatus.StatusRange;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.app.workflow.WorkflowQuery;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.query.ResultSet;

import java.util.Date;


/**
 * Queries <em>act.customerTask</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TaskQuery extends WorkflowQuery<Act> {

    /**
     * Construct a new <code>CustomerAppointmentQuery</code>.
     *
     * @param schedule the schedule
     */
    public TaskQuery(Party schedule) {
        super(schedule, "worklist", "participation.worklist",
              new String[]{"act.customerTask"}, new String[0]);
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return the query result set. May be <code>null</code>
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public ResultSet<Act> query(SortConstraint[] sort) {
        if (getEntityId() != null && getClinician() != INVALID_CLINICIAN) {
            return createResultSet(sort);
        }
        return null;
    }

    /**
     * Returns the act statuses to query.
     *
     * @return the act statuses to query
     */
    @Override
    protected String[] getStatuses() {
        StatusRange range = getStatusRange();
        if (range == StatusRange.COMPLETE) {
            return TaskStatus.COMPLETE;
        } else if (range == StatusRange.INCOMPLETE) {
            return TaskStatus.INCOMPLETE;
        }
        return new String[0];
    }

    /**
     * Creates a new result set.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return a new result set
     */
    @Override
    protected ResultSet<Act> createResultSet(SortConstraint[] sort) {
        ParticipantConstraint[] participants = getParticipantConstraints();
        Date from = getFrom();
        Date to = getTo();

        IConstraint range = TaskQueryHelper.createDateRangeConstraint(from, to);
        return new ActResultSet<Act>(getArchetypeConstraint(), participants,
                                     range, getStatuses(), excludeStatuses(),
                                     getConstraints(),
                                     ArchetypeQuery.ALL_RESULTS, sort);
    }

}
