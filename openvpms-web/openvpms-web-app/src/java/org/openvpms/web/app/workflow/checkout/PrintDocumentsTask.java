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

package org.openvpms.web.app.workflow.checkout;

import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.web.app.customer.CustomerMailContext;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.event.WindowPaneListener;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.Reporter;
import org.openvpms.web.component.im.report.ReporterFactory;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.mail.MailDialog;
import org.openvpms.web.component.mail.MailEditor;
import org.openvpms.web.component.print.BatchPrintDialog;
import org.openvpms.web.component.print.BatchPrinter;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workflow.AbstractTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskListener;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Task to allow the user to selectively print any unprinted documents
 * from a particular time.
 *
 * @author Tim Anderson
 */
class PrintDocumentsTask extends AbstractTask {

    /**
     * The time to select unprinted documents from.
     */
    private final Date startTime;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * The print dialog.
     */
    private BatchPrintDialog dialog;

    /**
     * The charge acts to print.
     */
    private static final String[] CHARGES = {"act.customerAccountCharges*"};

    /**
     * The printable patient documents.
     */
    private static final String[] DOCUMENTS = {"act.patientDocumentLetter", "act.patientDocumentForm"};

    /**
     * The mail button identifier.
     */
    private static final String MAIL_ID = "mail";


    /**
     * Constructs a {@code PrintDocumentsTask}.
     *
     * @param startTime the act start time.
     * @param help      the help context
     */
    public PrintDocumentsTask(Date startTime, HelpContext help) {
        this.startTime = startTime;
        this.help = help;
    }

    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or
     * failure.
     *
     * @param context the task context
     */
    public void start(final TaskContext context) {
        Map<IMObject, Boolean> unprinted = new LinkedHashMap<IMObject, Boolean>();
        unprinted.putAll(getCustomerActs(context));
        unprinted.putAll(getPatientActs(context));
        if (unprinted.isEmpty()) {
            notifyCompleted();
        } else {
            String title = Messages.get("workflow.checkout.print.title");
            String[] buttons = isRequired() ? PopupDialog.OK_CANCEL : PopupDialog.OK_SKIP_CANCEL;
            dialog = new BatchPrintDialog(title, buttons, unprinted, help);
            dialog.getButtons().add(MAIL_ID, new ActionListener() {
                public void onAction(ActionEvent event) {
                    onMail(context);
                }
            });
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void onClose(WindowPaneEvent event) {
                    try {
                        String action = dialog.getAction();
                        if (BatchPrintDialog.OK_ID.equals(action)) {
                            print(dialog.getSelected(), context);
                        } else if (BatchPrintDialog.SKIP_ID.equals(action)) {
                            notifySkipped();
                        } else {
                            notifyCancelled();
                        }
                    } finally {
                        dialog = null;
                    }
                }
            });
            dialog.show();
        }
    }

    /**
     * Returns the print dialog.
     *
     * @return the print dialog, or {@code null} if none is being displayed
     */
    public BatchPrintDialog getPrintDialog() {
        return dialog;
    }

    /**
     * Prints a list of objects.
     *
     * @param objects the objects to print
     * @param context the task context
     */
    private void print(List<IMObject> objects, TaskContext context) {
        Printer printer = new Printer(objects, context);
        printer.print();
    }

    /**
     * Returns a map of unprinted customer charges.
     *
     * @param context the task context
     * @return a map of unprinted customer charges
     */
    private Map<IMObject, Boolean> getCustomerActs(TaskContext context) {
        Party customer = context.getCustomer();
        String node = "customer";
        String participation = "participation.customer";
        return getUnprintedActs(CHARGES, customer, node, participation);
    }

    /**
     * Returns a map of unprinted patient documents.
     *
     * @param context the task context
     * @return a map of unprinted patient documents
     */
    private Map<IMObject, Boolean> getPatientActs(TaskContext context) {
        Party patient = context.getPatient();
        String node = "patient";
        String participation = "participation.patient";
        return getUnprintedActs(DOCUMENTS, patient, node, participation);
    }

    /**
     * Returns a map of unprinted acts for a party.
     * <p/>
     * The corresponding boolean flag if {@code true} indicates if the act should be selected for printing.
     * If {@code false}, it indicates that the act should be displayed, but not selected.
     *
     * @param shortNames    the act short names to query. May include wildcards
     * @param party         the party to query
     * @param node          the participation node to query
     * @param participation the participation short name to query
     * @return the unprinted acts
     */
    private Map<IMObject, Boolean> getUnprintedActs(String[] shortNames, Party party, String node,
                                                    String participation) {
        Map<IMObject, Boolean> result = new LinkedHashMap<IMObject, Boolean>();
        ArchetypeQuery query = new ArchetypeQuery(shortNames, false, true);
        query.setFirstResult(0);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);

        CollectionNodeConstraint participations
            = new CollectionNodeConstraint(node, participation,
                                           false, true);
        participations.add(new ObjectRefNodeConstraint("entity", party.getObjectReference()));

        query.add(participations);
        query.add(new NodeConstraint("startTime", RelationalOp.GTE, startTime));
        query.add(new NodeConstraint("printed", false));

        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        for (IMObject object : service.get(query).getResults()) {
            DocumentTemplate.PrintMode mode = getPrintMode((Act) object);
            // select all acts with CHECK_OUT print mode, or with no print mode specified
            boolean select = (mode == null || mode == DocumentTemplate.PrintMode.CHECK_OUT);
            result.put(object, select);
        }
        return result;
    }

    private void onMail(TaskContext context) {
        List<IMObject> list = dialog.getSelected();
        if (!list.isEmpty()) {
            HelpContext email = context.getHelpContext().subtopic("email");
            MailContext mailContext = new CustomerMailContext(context, email);
            MailDialog dialog = new MailDialog(mailContext, context, email);
            MailEditor editor = dialog.getMailEditor();
            for (IMObject object : list) {
                ContextDocumentTemplateLocator locator = new ContextDocumentTemplateLocator(object, context);
                Reporter<IMObject> reporter = ReporterFactory.create(object, locator, Reporter.class);
                Document document = reporter.getDocument(Reporter.DEFAULT_MIME_TYPE, true);
                editor.addAttachment(document);
            }
            dialog.show();
        }
    }

    /**
     * Returns the print mode of the supplied act.
     *
     * @param act the act
     * @return the print mode. May be {@code null}
     */
    private DocumentTemplate.PrintMode getPrintMode(Act act) {
        DocumentTemplate.PrintMode result = null;
        ActBean bean = new ActBean(act);
        if (bean.hasNode("documentTemplate")) {
            Entity entity = bean.getNodeParticipant("documentTemplate");
            if (entity != null) {
                DocumentTemplate template = new DocumentTemplate(entity, ServiceHelper.getArchetypeService());
                result = template.getPrintMode();
            }
        }
        return result;
    }

    /**
     * Batch printer.
     */
    class Printer extends BatchPrinter<IMObject> {

        /**
         * Constructs a {@code Printer}.
         *
         * @param objects the objects to print
         * @param context the task context
         */
        public Printer(List<IMObject> objects, TaskContext context) {
            super(objects, context, context.getHelpContext());
        }

        /**
         * Invoked when a print is cancelled. This restarts the task.
         */
        public void cancelled() {
            start(getContext());
        }

        /**
         * Notifies that the print was skipped. This restarts the task.
         */
        public void skipped() {
            start(getContext());
        }

        /**
         * Invoked when an object fails to print. This restarts the task.
         *
         * @param cause the reason for the failure
         */
        public void failed(Throwable cause) {
            ErrorHelper.show(cause, new WindowPaneListener() {
                public void onClose(WindowPaneEvent event) {
                    start(getContext());
                }
            });
        }

        /**
         * Invoked when printing completes.
         */
        @Override
        protected void completed() {
            notifyCompleted();
        }

        /**
         * Creates a new interactive printer.
         * <p/>
         * This implementation disables the default interactive behaviour - a dialog will only be popped up if
         * there is no physical printer specified.
         *
         * @param printer the printer to delegate to
         * @return a new interactive printer
         */
        @Override
        protected InteractiveIMPrinter<IMObject> createInteractivePrinter(IMPrinter<IMObject> printer) {
            InteractiveIMPrinter<IMObject> result = super.createInteractivePrinter(printer);
            result.setInteractive(false);
            return result;
        }

        /**
         * Returns the context.
         *
         * @return the context
         */
        @Override
        protected TaskContext getContext() {
            return (TaskContext) super.getContext();
        }
    }
}
