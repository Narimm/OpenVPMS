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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.workflow.worklist;

import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.query.QueryHelper;

import java.util.Date;


/**
 * Helper for task queries.
 *
 * @author Tim Anderson
 */
class TaskQueryHelper {


    /**
     * Determines there are too many outstanding tasks for a worklist associated with an act.
     *
     * @param act the act. An instance of <em>act.customerTask</em> with a non-null start date.
     * @return {@code true} if there are too many outstanding tasks; otherwise {@code false}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static boolean tooManyTasks(Act act) {
        boolean result = false;
        ActBean actBean = new ActBean(act);
        Date startTime = act.getActivityStartTime();
        Party workList = (Party) actBean.getNodeParticipant("worklist");
        if (startTime != null && workList != null) {
            IMObjectBean bean = new IMObjectBean(workList);
            int maxSlots = bean.getInt("maxSlots");
            IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
            ArchetypeQuery query = new ArchetypeQuery(ScheduleArchetypes.TASK, false, true);
            query.add(new ParticipantConstraint("worklist", ScheduleArchetypes.WORKLIST_PARTICIPATION, workList));
            query.add(Constraints.ne("id", act.getId()));
            query.add(Constraints.and(Constraints.ne("status", FinancialActStatus.CANCELLED),
                                      Constraints.ne("status", FinancialActStatus.COMPLETED)));
            query.add(QueryHelper.createDateRangeConstraint(startTime, act.getActivityEndTime()));
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
