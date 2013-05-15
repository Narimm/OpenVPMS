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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.ContextException;
import org.openvpms.web.component.im.doc.DocumentTemplateQuery;
import org.openvpms.web.component.workflow.CreateIMObjectTask;
import org.openvpms.web.component.workflow.PrintIMObjectTask;
import org.openvpms.web.component.workflow.SelectIMObjectTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskProperties;
import org.openvpms.web.component.workflow.UpdateIMObjectTask;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.workspace.customer.CustomerMailContext;

import static org.openvpms.web.component.app.ContextException.ErrorCode.NoObject;
import static org.openvpms.web.component.app.ContextException.ErrorCode.NoPatient;

/**
 * Task to optionally print an <em>act.patientDocumentForm</em> for a patient.
 *
 * @author Tim Anderson
 */
class PrintDocumentFormTask extends WorkflowImpl {

    /**
     * Patient document form act short name.
     */
    private static final String DOCUMENT_FORM = "act.patientDocumentForm";

    /**
     * Document template entity short name.
     */
    private static final String DOCUMENT_TEMPLATE = "entity.documentTemplate";

    /**
     * Constructs a {@code PatientDocumentFormTask}.
     *
     * @param context the context
     * @throws OpenVPMSException for any error
     */
    public PrintDocumentFormTask(TaskContext context) {
        super(context.getHelpContext().topic("template"));
        // create a query for all entity.documentTemplate instances with
        // archetype node='act.patientDocumentForm'.
        DocumentTemplateQuery query = new DocumentTemplateQuery();
        query.setArchetype(DOCUMENT_FORM);
        SelectIMObjectTask<Entity> docTask = new SelectIMObjectTask<Entity>(query, getHelpContext());
        docTask.setRequired(false);

        // task to create an an act.patientDocumentForm associating the
        // context patient and selected template to it
        CreateIMObjectTask createTask = new CreateIMObjectTask(DOCUMENT_FORM) {
            @Override
            protected void created(IMObject object, TaskContext context) {
                Party patient = context.getPatient();
                if (patient == null) {
                    throw new ContextException(NoPatient);
                }
                Entity template = (Entity) context.getObject(DOCUMENT_TEMPLATE);
                if (template == null) {
                    throw new ContextException(NoObject, DOCUMENT_TEMPLATE);
                }
                ActBean bean = new ActBean((Act) object);
                bean.addParticipation("participation.patient", patient);
                bean.addParticipation("participation.documentTemplate",
                                      template);
                super.created(object, context);
            }
        };

        // task to print the act.patientDocumentForm. May be skipped if
        // printing interactively
        CustomerMailContext mailContext = new CustomerMailContext(context, context.getHelpContext());
        PrintIMObjectTask printTask = new PrintIMObjectTask(DOCUMENT_FORM, mailContext, false);
        printTask.setRequired(false);

        // task to save the act.patientDocumentForm, setting its 'printed'
        // flag, if the document is printed.
        TaskProperties saveProperties = new TaskProperties();
        saveProperties.add("printed", true);
        UpdateIMObjectTask saveTask
            = new UpdateIMObjectTask(DOCUMENT_FORM, saveProperties, true);
        addTask(docTask);
        addTask(createTask);
        addTask(printTask);
        addTask(saveTask);
        setRequired(false);
        setBreakOnSkip(true);
    }

}
