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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.AndConstraint;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
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
public class TaskQuery extends WorkflowQuery {

    /**
     * Construct a new <code>AppointmentQuery</code>.
     *
     * @param schedule the schedule
     */
    public TaskQuery(Party schedule) {
        super(schedule, "worklist", "participation.worklist",
              new String[]{"act.customerTask"}, new String[0]);
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
        Date from = getStartFrom();
        Date to = getStartTo();

        OrConstraint or = new OrConstraint();
        IConstraint overlapStart = createOverlapConstraint(from);
        IConstraint overlapEnd = createOverlapConstraint(to);
        or.add(overlapStart);
        or.add(overlapEnd);

        return new ActResultSet(participants, getArchetypes(), or,
                                getStatuses(), excludeStatuses(),
                                getConstraints(), ArchetypeQuery.ALL_RESULTS,
                                sort);
    }

    /**
     * Helper to create a constraint of the form:
     * <code>act.startTime < time && act.endTime > time || act.endTime == null
     * </code>
     *
     * @param time the time
     * @return a new constraint
     */
    private static IConstraint createOverlapConstraint(Date time) {
        AndConstraint and = new AndConstraint();
        and.add(new NodeConstraint("startTime", RelationalOp.LTE, time));
        OrConstraint or = new OrConstraint();
        or.add(new NodeConstraint("endTime", RelationalOp.GTE, time));
        or.add(new NodeConstraint("endTime", RelationalOp.IsNULL));
        and.add(or);
        return and;
    }

}
