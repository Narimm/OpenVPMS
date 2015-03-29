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

package org.openvpms.web.workspace.workflow.checkin;

import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.workflow.TaskContext;

/**
 * Check-In workflow helper methods.
 *
 * @author Tim Anderson
 */
class CheckInHelper {

    /**
     * Returns the work list associated with the context task.
     *
     * @param context the context
     * @return the work list, or {@code null} if none is found
     */
    public static Entity getWorkList(TaskContext context) {
        Act task = (Act) context.getObject(ScheduleArchetypes.TASK);
        if (task != null) {
            return new ActBean(task).getNodeParticipant("worklist");
        }
        return null;
    }
}
