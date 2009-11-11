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

import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.ContextException;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;


/**
 * Queries the most recent <em>act.patientClinicalEvent</em> for the context patient,
 * creating one if it doesn't exist.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class GetClinicalEventTask extends SynchronousTask {

    /**
     * The event short name.
     */
    public static final String EVENT_SHORTNAME = "act.patientClinicalEvent";


    /**
     * Executes the task.
     *
     * @throws org.openvpms.component.system.common.exception.OpenVPMSException for any error
     */
    public void execute(TaskContext context) {
        Party patient = context.getPatient();
        if (patient == null) {
            throw new ContextException(ContextException.ErrorCode.NoPatient);
        }
        Entity clinician = context.getClinician();
        MedicalRecordRules rules = new MedicalRecordRules();
        Act event = rules.getEventForAddition(patient, new Date(), clinician);
        if (event.isNew()) {
            ServiceHelper.getArchetypeService().save(event);
        }
        context.addObject(event);
    }
}
