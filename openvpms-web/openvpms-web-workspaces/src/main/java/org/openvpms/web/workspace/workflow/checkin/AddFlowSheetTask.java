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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.checkin;

import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.Tasks;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;

/**
 * A task that creates a Flow Sheet if the selected work list supports it, and no Flow Sheet exists for the visit.
 * <p/>
 * The visit must be present in the task context and be persistent, or the patient must have a visit, for a Flow Sheet
 * to be created.
 *
 * @author Tim Anderson
 */
public class AddFlowSheetTask extends Tasks {

    /**
     * The task.
     */
    private Act task;

    /**
     * The FlowSheet service factory.
     */
    private final FlowSheetServiceFactory factory;

    /**
     * Constructs a {@link AddFlowSheetTask}.
     *
     * @param factory the FlowSheet service factory
     * @param help    the help context
     */
    public AddFlowSheetTask(FlowSheetServiceFactory factory, HelpContext help) {
        this(null, factory, help);
    }

    /**
     * Constructs a {@link AddFlowSheetTask}.
     *
     * @param task    the task. If {@code null}, it will be selected from the context
     * @param factory the FlowSheet Service factory
     * @param help    the help context
     */
    public AddFlowSheetTask(Act task, FlowSheetServiceFactory factory, HelpContext help) {
        super(help);
        this.task = task;
        this.factory = factory;
    }

    /**
     * Initialise any tasks.
     *
     * @param context the task context
     */
    @Override
    protected void initialise(TaskContext context) {
        if (task == null) {
            task = context.getTask();
        }
        if (task != null) {
            ActBean bean = new ActBean(task);
            Party workList = (Party) bean.getNodeParticipant("worklist");
            if (workList != null && createFlowSheet(workList)) {
                MedicalRecordRules rules = ServiceHelper.getBean(MedicalRecordRules.class);
                Act visit = (Act) context.getObject(PatientArchetypes.CLINICAL_EVENT);
                if (visit == null) {
                    visit = rules.getEventForAddition(context.getPatient().getObjectReference(),
                                                      task.getActivityStartTime(), null);
                }
                if (visit != null && !visit.isNew()) {
                    addTask(new NewFlowSheetTask(task, visit, true, context.getLocation(), factory,
                                                 context.getHelpContext()));
                }
            }
        }
    }

    /**
     * Determines if a FlowSheet should be created for a work list
     *
     * @param workList the work list
     * @return {@code true} if a FlowSheet should be created
     */
    private boolean createFlowSheet(Entity workList) {
        IMObjectBean bean = new IMObjectBean(workList);
        return bean.getString("createFlowSheet") != null;
    }
}
