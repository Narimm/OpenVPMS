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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.checkin;

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.doc.DocumentGenerator;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.retry.Retryer;
import org.openvpms.web.component.workflow.PrintActTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskListener;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.PatientMedicalRecordLinker;

/**
 * A print task that generates letters prior to print, and links the document to the context's act.patientClinicalEvent,
 * if one is present.
 *
 * @author Tim Anderson
 */
public class PrintPatientActTask extends PrintActTask {

    /**
     * Constructs a {@link PrintPatientActTask}.
     *
     * @param act       the act to print
     * @param context   the mail context. May be {@code null}
     * @param printMode the print mode
     */
    public PrintPatientActTask(Act act, MailContext context, PrintMode printMode) {
        super(act, context, printMode);
        setRequired(false);
    }

    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or failure.
     *
     * @param context the task context
     */
    @Override
    public void start(final TaskContext context) {
        final DocumentAct object = (DocumentAct) getObject(context);
        if (TypeHelper.isA(object, PatientArchetypes.DOCUMENT_LETTER)) {
            // need to generate the document before printing it.
            DocumentGenerator.Listener listener = new DocumentGenerator.AbstractListener() {
                @Override
                public void generated(Document document) {
                    print(object, context);
                }

                @Override
                public void cancelled() {
                    notifyCancelled();
                }

                @Override
                public void skipped() {
                    notifySkipped();
                }

                @Override
                public void error() {
                    notifySkipped();
                }
            };
            DocumentGenerator generator = new DocumentGenerator(object, context, context.getHelpContext(),
                                                                listener);
            generator.generate(true, false, true);
        } else {
            super.start(context);
        }
    }

    /**
     * Invoked when the object is successfully printed.
     * <p/>
     * This implementation links the document to the patient visit, if one is present.
     *
     * @param object  the printed object
     * @param context the task context
     */
    @Override
    protected void onPrinted(IMObject object, TaskContext context) {
        if (setPrintStatus(object)) {
            ServiceHelper.getArchetypeService().save(object);
        }

        Act document = (Act) object;
        Act event = getEvent(document, context);
        if (event != null) {
            PatientMedicalRecordLinker linker = new PatientMedicalRecordLinker(event, document);
            if (Retryer.run(linker)) {
                context.setObject(PatientArchetypes.CLINICAL_EVENT, linker.getEvent());
                notifyCompleted();
            } else {
                notifyCancelled();
            }
        }
    }

    /**
     * Returns the patient clinical event.
     *
     * @param document the document
     * @param context  the task context
     * @return the patient clinical event from the context, or {@code null} if none is present
     */
    protected Act getEvent(Act document, TaskContext context) {
        return (Act) context.getObject(PatientArchetypes.CLINICAL_EVENT);
    }
}
