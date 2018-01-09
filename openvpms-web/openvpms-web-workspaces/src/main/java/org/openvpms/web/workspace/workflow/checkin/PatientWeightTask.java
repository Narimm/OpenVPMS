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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.checkin;

import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.math.Weight;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.app.ContextException;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.retry.Retryer;
import org.openvpms.web.component.workflow.ConditionalTask;
import org.openvpms.web.component.workflow.DeleteIMObjectTask;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.NodeConditionTask;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskProperties;
import org.openvpms.web.component.workflow.Tasks;
import org.openvpms.web.component.workflow.Variable;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.VetoListener;
import org.openvpms.web.echo.event.Vetoable;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.PatientMedicalRecordLinker;

import java.math.BigDecimal;


/**
 * Task to create an <em>act.patientWeight</em> for a patient, if either the schedule or work list have an
 * "inputWeight" set to true, or a work list has "createFlowSheet" set.
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
     * Determines if the patient weight should be entered based on the context schedule and work list.
     */
    private final boolean useContext;

    /**
     * Patient weight short name.
     */
    private static final String PATIENT_WEIGHT = "act.patientWeight";

    /**
     * Constructs a {@link PatientWeightTask} that determines if the patient weight should be entered based
     * on the context schedule and work list.
     *
     * @param help the help context
     * @throws OpenVPMSException for any error
     */
    public PatientWeightTask(HelpContext help) {
        this(null, true, help);
    }

    /**
     * Constructs a {@link PatientWeightTask} that determines if the patient weight should be entered based
     * on the context schedule and work list.
     *
     * @param weight the current weight, or {@link Weight#ZERO} to indicate no current weight
     * @param help   the help context
     * @throws OpenVPMSException for any error
     */
    public PatientWeightTask(Weight weight, HelpContext help) {
        this(weight, false, help);
    }

    private PatientWeightTask(Weight weight, boolean useContext, HelpContext help) {
        super(help);
        if (weight != null) {
            this.weight = weight.getWeight();
            this.units = weight.getUnits().toString();
        }
        this.useContext = useContext;
        setRequired(false);
        setBreakOnSkip(true);
    }

    /**
     * Initialise any tasks.
     *
     * @param context the task context
     */
    @Override
    protected void initialise(TaskContext context) {
        boolean inputWeight;
        final boolean createFlowSheet;
        if (useContext) {
            Entity schedule = context.getSchedule();
            Entity worklist = CheckInHelper.getWorkList(context);
            createFlowSheet = createFlowSheet(worklist);
            inputWeight = createFlowSheet || inputWeight(schedule) || inputWeight(worklist);
        } else {
            createFlowSheet = true;
            inputWeight = true;
        }
        if (inputWeight) {
            initLastWeight(context);
            TaskProperties properties = new TaskProperties();
            properties.add(new Variable("weight") {
                public Object getValue(TaskContext context) {
                    return weight;
                }
            });
            properties.add(new Variable("units") {
                public Object getValue(TaskContext context) {
                    return units;
                }
            });
            EditIMObjectTask editWeightTask = new EditWeightTask(properties, createFlowSheet);
            addTask(editWeightTask);

            NodeConditionTask<BigDecimal> weightZero = new NodeConditionTask<>(PATIENT_WEIGHT, "weight", false,
                                                                               BigDecimal.ZERO);
            DeleteIMObjectTask deleteWeightTask = new DeleteIMObjectTask(PATIENT_WEIGHT);
            ConditionalTask condition = new ConditionalTask(weightZero, new WeightLinkerTask(), deleteWeightTask);
            addTask(condition);
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

    private boolean createFlowSheet(Entity workList) {
        return workList != null && new IMObjectBean(workList).getString("createFlowSheet") != null;
    }

    /**
     * Initialises the most recent <em>act.patientWeight</em> for the context patient.
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
                IArchetypeRuleService service = ServiceHelper.getArchetypeService();
                act = (Act) service.create(PATIENT_WEIGHT);
                ActBean bean = new ActBean(act);
                Object value = bean.getDefaultValue("weight");
                units = (value != null) ? value.toString() : WeightUnits.KILOGRAMS.toString();
            }
        }
    }

    /**
     * Queries the most recent <em>act.patientWeight</em>.
     *
     * @param context the task context
     * @return the most recent <em>act.patientWeight</em>, or {@code null}
     * if none is found
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

    private class EditWeightTask extends EditIMObjectTask {
        private final boolean createFlowSheet;

        public EditWeightTask(TaskProperties properties, boolean createFlowSheet) {
            super(PatientWeightTask.PATIENT_WEIGHT, properties, true);
            setRequired(false);
            setSkip(true);
            setDeleteOnCancelOrSkip(true);
            this.createFlowSheet = createFlowSheet;
        }

        /**
         * Creates a new edit dialog.
         *
         * @param editor  the editor
         * @param skip    if {@code true}, editing may be skipped
         * @param context the help context
         * @return a new edit dialog
         */
        @Override
        protected EditDialog createEditDialog(IMObjectEditor editor, boolean skip, TaskContext context) {
            EditDialog dialog = super.createEditDialog(editor, skip, context);
            if (createFlowSheet) {
                dialog.setSkipListener(new VetoListener() {
                    @Override
                    public void onVeto(Vetoable action) {
                        confirmSkip(action);
                    }
                });
            }
            return dialog;
        }

        private void confirmSkip(final Vetoable action) {
            String title = Messages.get("workflow.checkin.skipweight.title");
            String message;
            if (!MathRules.isZero(weight)) {
                message = Messages.get("workflow.checkin.skipweight.previous");
            } else {
                message = Messages.get("workflow.checkin.skipweight.none");
            }
            ConfirmationDialog dialog = new ConfirmationDialog(title, message, PopupDialog.YES_NO);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onYes() {
                    action.veto(false);
                }

                @Override
                public void onNo() {
                    action.veto(true);
                }
            });
            dialog.show();
        }
    }
}
