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

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.ContextException;
import org.openvpms.web.component.retry.Retryer;
import org.openvpms.web.component.workflow.ConditionalTask;
import org.openvpms.web.component.workflow.DeleteIMObjectTask;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.NodeConditionTask;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskListener;
import org.openvpms.web.component.workflow.TaskProperties;
import org.openvpms.web.component.workflow.Tasks;
import org.openvpms.web.component.workflow.Variable;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.PatientMedicalRecordLinker;

import java.math.BigDecimal;


/**
 * Task to create an <em>act.patientWeight</em> for a patient, if either the schedule or work list have a "inputWeight"
 * set to true.
 *
 * @author Tim Anderson
 */
class PatientWeightTask extends Tasks {

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
        setRequired(false);
        setBreakOnSkip(true);
    }

    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or failure.
     *
     * @param context the task context
     */
    @Override
    public void start(TaskContext context) {
        Entity schedule = context.getSchedule();
        Entity worklist = CheckInHelper.getWorkList(context);
        if (inputWeight(schedule) || inputWeight(worklist)) {
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
            DeleteIMObjectTask deleteWeightTask = new DeleteIMObjectTask(PATIENT_WEIGHT);
            ConditionalTask condition = new ConditionalTask(weightZero, new WeightLinkerTask(), deleteWeightTask);
            addTask(condition);
            super.start(context);
        } else {
            notifySkipped();
        }
    }

    /**
     * Determines if a schedule should prompt to input the patient weight.
     *
     * @param schedule the schedule. An <em>party.organisationSchedule</em> or <em>party.organisationWorkList</em>.
     *                 May be {@code null}
     * @return {@code true} if the patient weight should be input
     */
    private boolean inputWeight(Entity schedule) {
        return schedule != null && new IMObjectBean(schedule).getBoolean("inputWeight", true);
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
        PatientRules rules = ServiceHelper.getBean(PatientRules.class);
        Party patient = context.getPatient();
        if (patient == null) {
            throw new ContextException(ContextException.ErrorCode.NoPatient);
        }
        return rules.getWeightAct(patient);
    }

    private static class WeightLinkerTask extends SynchronousTask {
        /**
         * Executes the task.
         *
         * @throws OpenVPMSException for any error
         */
        @Override
        public void execute(TaskContext context) {
            Act event = (Act) context.getObject(PatientArchetypes.CLINICAL_EVENT);
            Act weight = (Act) context.getObject(PatientArchetypes.PATIENT_WEIGHT);
            PatientMedicalRecordLinker linker = new PatientMedicalRecordLinker(event, weight);
            if (Retryer.run(linker)) {
                context.setObject(PatientArchetypes.CLINICAL_EVENT, event);
                notifyCompleted();
            } else {
                notifyCancelled();
            }
        }
    }
}
