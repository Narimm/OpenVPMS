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

package org.openvpms.web.app.workflow.consult;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.QueryIterator;
import org.openvpms.web.component.app.ContextException;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.workflow.CreateIMObjectTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskListener;


/**
 * Task to create an <em>act.patientClinicalEvent</em> for a customer,
 * if an IN_PROGRESS one doesn't already exist.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class PatientClinicalEventTask extends CreateIMObjectTask {

    /**
     * Constructs a new <code>PatientClinicalEventTask</code>
     */
    public PatientClinicalEventTask() {
        super(ConsultWorkflow.EVENT_SHORTNAME);
    }

    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or
     * failure.
     *
     * @param context the task context
     * @throws ContextException          if there is no patient
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public void start(final TaskContext context) {
        Party patient = context.getPatient();
        if (patient == null) {
            throw new ContextException(ContextException.ErrorCode.NoPatient);
        }

        ArchetypeQuery query = new ArchetypeQuery(getShortNames(), false,
                                                  true);
        query.setMaxResults(1);

        query.add(new ParticipantConstraint("patient", "participation.patient",
                                            patient.getObjectReference()));
        query.add(new NodeConstraint("status", ActStatus.IN_PROGRESS));
        query.add(new NodeSortConstraint("startTime", false));

        QueryIterator<Act> iterator = new IMObjectQueryIterator<Act>(query);
        if (!iterator.hasNext()) {
            super.start(context);
        } else {
            Act event = iterator.next();
            context.addObject(event);
            notifyCompleted();
        }
    }
}
