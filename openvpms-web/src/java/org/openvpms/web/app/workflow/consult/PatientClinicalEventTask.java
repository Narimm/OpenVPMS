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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.web.component.workflow.CreateIMObjectTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskListener;

import java.util.List;


/**
 * Task to create an <em>act.patientClinicalEvent</em> for a customer,
 * if one doesn't already exist.
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
     */
    @Override
    public void start(final TaskContext context) {
        ArchetypeQuery query = new ArchetypeQuery(getShortNames(), false,
                                                  true);
        query.setFirstResult(0);
        query.setMaxResults(1);

        Party patient = context.getPatient();
        CollectionNodeConstraint participations
                = new CollectionNodeConstraint("patient",
                                               "participation.patient",
                                               false, true);
        participations.add(new ObjectRefNodeConstraint(
                "entity", patient.getObjectReference()));

        query.add(participations);
        OrConstraint or = new OrConstraint();
        or.add(new NodeConstraint("status", ActStatus.IN_PROGRESS));
        or.add(new NodeConstraint("status", ActStatus.COMPLETED));
        query.add(or);

        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        List<IMObject> result = service.get(query).getResults();
        if (result.isEmpty()) {
            super.start(context);
        } else {
            Act event = (Act) result.get(0);
            context.addObject(event);
            notifyCompleted();
        }
    }
}
