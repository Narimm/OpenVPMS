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

package org.openvpms.web.app.workflow.scheduling;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.workflow.WorkflowQuery;


/**
 * Queries <em>act.customerAppointment</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentQuery extends WorkflowQuery {

    /**
     * Construct a new <code>AppointmentQuery</code>.
     *
     * @param schedule the schedule
     */
    public AppointmentQuery(Party schedule) {
        super(schedule, "schedule", "participation.schedule",
              new String[]{"act.customerAppointment"}, new String[0]);
    }

}
