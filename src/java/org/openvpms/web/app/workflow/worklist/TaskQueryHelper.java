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

package org.openvpms.web.app.workflow.worklist;

import org.apache.commons.lang.time.DateUtils;
import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.AndConstraint;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.util.DateHelper;

import java.util.Date;


/**
 * Helper for task queries.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class TaskQueryHelper {

    /**
     * Helper to create a date range constraint for a particular date.
     *
     * @param date the date
     * @return a new constraint
     */
    public static IConstraint createDateRangeConstraint(Date date) {
        Date from = DateHelper.getDayMonthYear(date);
        long end = from.getTime() + DateUtils.MILLIS_PER_DAY - DateUtils.MILLIS_PER_SECOND;
        Date to = new Date(end);
        return createDateRangeConstraint(from, to);
    }

    /**
     * Helper to create a constraint of the form:
     * <code>act.startTime <= to && (act.endTime >= from || act.endTime == null)
     * </code>
     *
     * @param from the from date
     * @param to   the to date
     * @return a new constraint
     */
    public static IConstraint createDateRangeConstraint(Date from, Date to) {
        AndConstraint and = new AndConstraint();
        and.add(new NodeConstraint("startTime", RelationalOp.LTE, to));
        OrConstraint or = new OrConstraint();
        or.add(new NodeConstraint("endTime", RelationalOp.GTE, from));
        or.add(new NodeConstraint("endTime", RelationalOp.IsNULL));
        and.add(or);
        return and;
    }

    /**
     * Determines there are too many outstanding tasks for a worklist associated
     * with an act.
     *
     * @param act the act. Any instance of <em>act.customerTask</em> with a
     *            non-null start date.
     * @return <code>true</code> if there are too many outstanding tasks;
     *         otherwise <code>false</code>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static boolean tooManyTasks(Act act) {
        boolean result = false;
        ActBean actBean = new ActBean(act);
        Date startTime = act.getActivityStartTime();
        Party workList = (Party) actBean.getParticipant(
                "participation.worklist");
        if (startTime != null && workList != null) {
            IMObjectBean bean = new IMObjectBean(workList);
            int maxSlots = bean.getInt("maxSlots");
            IArchetypeService service
                    = ArchetypeServiceHelper.getArchetypeService();
            ArchetypeQuery query
                    = new ArchetypeQuery("act.customerTask", false, true);
            query.add(new ParticipantConstraint("worklist",
                                                "participation.worklist",
                                                workList));
            query.add(new NodeConstraint("id", RelationalOp.NE, act.getId()));
            query.add(new NodeConstraint("status", RelationalOp.NE,
                                         FinancialActStatus.CANCELLED));
            query.add(createDateRangeConstraint(startTime));
            query.setFirstResult(0);
            query.setMaxResults(1);
            query.setCountResults(true);
            IPage<IMObject> page = service.get(query);
            int totalResults = page.getTotalResults();
            if (totalResults >= maxSlots) {
                result = true;
            }
        }
        return result;
    }
}
