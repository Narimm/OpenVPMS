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
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.web.component.app.ContextException;
import org.openvpms.web.component.im.doc.DocumentGenerator;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.AbstractBrowserListener;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.retry.Retryer;
import org.openvpms.web.component.workflow.PrintActTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskListener;
import org.openvpms.web.component.workflow.Tasks;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.CustomerMailContext;
import org.openvpms.web.workspace.patient.PatientMedicalRecordLinker;

import java.util.List;

/**
 * Task to optionally print <em>act.patientDocumentForm</em> and <em>act.patientDocumentLetter</em> for a patient.
 *
 * @author Tim Anderson
 */
class PrintPatientDocumentsTask extends Tasks {

    /**
     * The browser dialog.
     */
    private BrowserDialog<Entity> dialog;


    /**
     * Constructs a {@link PrintPatientDocumentsTask}.
     */
    public PrintPatientDocumentsTask(HelpContext help) {
        super(help);
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
        Entity schedule = context.getSchedule();
        Entity worklist = CheckInHelper.getWorkList(context);
        if (hasTemplates(schedule) || hasTemplates(worklist)) {
            Party patient = context.getPatient();
            if (patient == null) {
                throw new ContextException(ContextException.ErrorCode.NoPatient);
            }

            String title = Messages.get("workflow.print.title");
            Query<Entity> query;
            query = new ScheduleDocumentTemplateQuery(schedule, worklist);
            final PatientDocumentTemplateBrowser browser = new PatientDocumentTemplateBrowser(
                    query, new DefaultLayoutContext(context, context.getHelpContext()));
            dialog = new BrowserDialog<Entity>(title, PopupDialog.OK_SKIP_CANCEL, browser, context.getHelpContext());
            dialog.getButtons().setEnabled(PopupDialog.OK_ID, false);
            browser.addBrowserListener(new AbstractBrowserListener<Entity>() {
                @Override
                public void selected(Entity object) {
                    dialog.getButtons().setEnabled(PopupDialog.OK_ID, browser.hasSelections());
                }
            });
            dialog.setCloseOnSelection(false);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                protected void onAction(PopupDialog dialog) {
                    try {
                        super.onAction(dialog);
                    } finally {
                        PrintPatientDocumentsTask.this.dialog = null;
                    }
                }

                @Override
                public void onOK() {
                    print(browser.getSelectedList(), context);
                }

                @Override
                public void onSkip() {
                    notifySkipped();
                }

                @Override
                public void onCancel() {
                    notifyCancelled();
                }

                /**
                 * Invoked when an unknown button is selected.
                 *
                 * @param action the dialog action
                 */
                @Override
                public void onAction(String action) {
                    notifyCancelled();
                }

            });
            dialog.show();
        } else {
            // no templates associated with the schedule or worklist
            notifyCompleted();
        }
    }

    /**
     * Returns the browser dialog.
     *
     * @return the browser dialog, or {@code null} if none is being displayed
     */
    public BrowserDialog<Entity> getBrowserDialog() {
        return dialog;
    }

    private void print(List<Entity> templates, TaskContext context) {
        CustomerMailContext mailContext = new CustomerMailContext(context, context.getHelpContext());
        for (Entity template : templates) {
            IMObjectBean templateBean = new IMObjectBean(template);
            Act document = (Act) ServiceHelper.getArchetypeService().create(templateBean.getString("archetype"));
            ActBean bean = new ActBean(document);
            bean.addNodeParticipation("patient", context.getPatient());
            bean.addNodeParticipation("documentTemplate", template);
            addTask(new PrintPatientActTask(document, mailContext));
        }
        // now start the workflow to print the documents
        super.start(context);
    }

    /**
     * Determines if a schedule/work list has any active templates linked to it.
     *
     * @param schedule the schedule/work list
     * @return {@code true} if there are any active templates, otherwise {@code false}
     */
    private boolean hasTemplates(Entity schedule) {
        if (schedule == null || ScheduleDocumentTemplateQuery.useAllTemplates(schedule)) {
            return true;
        }
        ArchetypeQuery query = new ArchetypeQuery(schedule.getObjectReference());
        query.add(new NodeSelectConstraint("id"));
        query.add(Constraints.join("templates").add(Constraints.join("target").add(Constraints.eq("active", true))));
        query.setMaxResults(1);
        ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(query);
        return iterator.hasNext();
    }

    private static class PrintPatientActTask extends PrintActTask {

        /**
         * Constructs a {@link PrintPatientActTask}.
         *
         * @param act     the act to print
         * @param context the mail context. May be {@code null}
         */
        public PrintPatientActTask(Act act, MailContext context) {
            super(act, context, false);
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
         * This implementation links the document to the patient visit.
         *
         * @param object  the printed object
         * @param context the task context
         */
        @Override
        protected void onPrinted(final IMObject object, final TaskContext context) {
            if (setPrintStatus(object)) {
                ServiceHelper.getArchetypeService().save(object);
            }

            Act event = (Act) context.getObject(PatientArchetypes.CLINICAL_EVENT);
            PatientMedicalRecordLinker linker = new PatientMedicalRecordLinker(event, (Act) object);
            if (Retryer.run(linker)) {
                context.setObject(PatientArchetypes.CLINICAL_EVENT, linker.getEvent());
                notifyCompleted();
            } else {
                notifyCancelled();
            }
        }
    }


}
