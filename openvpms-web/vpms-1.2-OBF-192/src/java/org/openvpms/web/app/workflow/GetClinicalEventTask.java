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

package org.openvpms.web.app.workflow;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.web.component.app.ContextException;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.workflow.QueryIMObjectTask;
import org.openvpms.web.component.workflow.TaskContext;


/**
 * Queries the most recent in progress <em>act.patientClinicalEvent</em> for
 * the context patient. If one is present, adds it to the context.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class GetClinicalEventTask extends QueryIMObjectTask {

    /**
     * The event short name.
     */
    public static final String EVENT_SHORTNAME = "act.patientClinicalEvent";


    /**
     * Returns the queries to execute.
     *
     * @param context the task context
     * @return the queries
     */
    protected ArchetypeQuery[] getQueries(TaskContext context) {
        Party patient = context.getPatient();
        if (patient == null) {
            throw new ContextException(ContextException.ErrorCode.NoPatient);
        }
        ArchetypeQuery query = new ArchetypeQuery(EVENT_SHORTNAME, false,
                                                  true);
        query.add(new ParticipantConstraint("patient", "participation.patient",
                                            patient.getObjectReference()));
        query.add(new NodeConstraint("status", ActStatus.IN_PROGRESS));
        query.add(new NodeSortConstraint("startTime", false));
        return new ArchetypeQuery[]{query};
    }
}
