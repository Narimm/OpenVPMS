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
 */

package org.openvpms.web.app.workflow.checkin;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.QueryIterator;
import org.openvpms.web.component.app.ContextException;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.workflow.AddActRelationshipTask;
import org.openvpms.web.component.workflow.ConditionalTask;
import org.openvpms.web.component.workflow.DeleteIMObjectTask;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.NodeConditionTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskProperties;
import org.openvpms.web.component.workflow.Variable;
import org.openvpms.web.component.workflow.WorkflowImpl;

import java.math.BigDecimal;


/**
 * Task to create an <em>act.patientWeight</em> for a patient.
 *
 * @author Tim Anderson
 */
class PatientWeightTask extends WorkflowImpl {

    /**
     * The last patient weight.
     */
    private BigDecimal weight;

    /**
     * The last weight units.
     */
    private String units;

    /**
     * Patient weight short name.
     */
    private static final String PATIENT_WEIGHT = "act.patientWeight";


    /**
     * Constructs a {@code PatientWeightTask}.
     *
     * @param help the help context
     * @throws OpenVPMSException for any error
     */
    public PatientWeightTask(HelpContext help) {
        super(help);
        final String event = "act.patientClinicalEvent";
        TaskProperties properties = new TaskProperties();
        properties.add(new Variable("weight") {
            public Object getValue(TaskContext context) {
                initLastWeight(context);
                return weight;
            }
        });
        properties.add(new Variable("units") {
            public Object getValue(TaskContext context) {
                initLastWeight(context);
                return units;
            }
        });
        EditIMObjectTask editWeightTask = new EditIMObjectTask(PATIENT_WEIGHT, properties, true);
        editWeightTask.setRequired(false);
        editWeightTask.setSkip(true);
        editWeightTask.setDeleteOnCancelOrSkip(true);
        addTask(editWeightTask);

        NodeConditionTask<BigDecimal> weightZero
            = new NodeConditionTask<BigDecimal>(PATIENT_WEIGHT, "weight", false, BigDecimal.ZERO);
        AddActRelationshipTask relationshipTask
            = new AddActRelationshipTask(event, PATIENT_WEIGHT, "actRelationship.patientClinicalEventItem");
        DeleteIMObjectTask deleteWeightTask = new DeleteIMObjectTask(PATIENT_WEIGHT);
        ConditionalTask condition = new ConditionalTask(weightZero, relationshipTask, deleteWeightTask);
        addTask(condition);
        setRequired(false);
        setBreakOnSkip(true);
    }

    /**
     * Initialises the most recent <em>act.patientWeight</em> for the context
     * patient.
     *
     * @param context the task context
     * @throws OpenVPMSException for any error
     */
    private void initLastWeight(TaskContext context) {
        if (weight == null) {
            Act act = queryLastWeight(context);
            if (act != null) {
                ActBean bean = new ActBean(act);
                weight = bean.getBigDecimal("weight");
                units = bean.getString("units");
            } else {
                weight = BigDecimal.ZERO;
                units = "KILOGRAMS";
            }
        }
    }

    /**
     * Queries the most recent <em>act.patientWeight</em>.
     *
     * @param context the task context
     * @return the most recent <em>act.patientWeight</em>, or {@code null}
     *         if none is found
     * @throws OpenVPMSException for any error
     */
    private Act queryLastWeight(TaskContext context) {
        ArchetypeQuery query = new ArchetypeQuery(PATIENT_WEIGHT, false, true);
        query.setFirstResult(0);
        query.setMaxResults(1);

        Party patient = context.getPatient();
        if (patient == null) {
            throw new ContextException(ContextException.ErrorCode.NoPatient);
        }
        CollectionNodeConstraint participations
            = new CollectionNodeConstraint("patient",
                                           "participation.patient",
                                           false, true);
        participations.add(new ObjectRefNodeConstraint(
            "entity", patient.getObjectReference()));

        query.add(participations);
        query.add(new NodeSortConstraint("startTime", false));

        QueryIterator<Act> iterator = new IMObjectQueryIterator<Act>(query);
        return (iterator.hasNext()) ? iterator.next() : null;
    }
}
