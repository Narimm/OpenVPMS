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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.workflow;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.Date;


/**
 * Queries <em>act.customerTask</em> acts, returning a limited set of data for
 * display purposes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class TaskQuery extends ScheduleEventQuery {

    /**
     * Creates a new <tt>TaskQuery</tt>.
     *
     * @param workList the work list
     * @param from     the 'from' start time
     * @param to       the 'to' start time
     */
    public TaskQuery(Party workList, Date from, Date to) {
        super(workList, from, to);
    }

    /**
     * Creates a new <tt>TaskQuery</tt>.
     *
     * @param workList the schedule
     * @param from     the 'from' start time
     * @param to       the 'to' start time
     * @param service  the archetype service
     */
    public TaskQuery(Party workList, Date from, Date to,
                     IArchetypeService service) {
        super(workList, from, to, service);
    }

    /**
     * Returns the name of the named query to execute.
     *
     * @return the name of the named query
     */
    protected String getQueryName() {
        return "act.customerTask";
    }

    /**
     * Returns the archetype short name of the schedule type.
     *
     * @return the short name of the schedule type
     */
    protected String getScheduleType() {
        return "entity.taskType";
    }
}
