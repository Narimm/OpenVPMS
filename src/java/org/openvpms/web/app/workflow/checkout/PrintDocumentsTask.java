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

package org.openvpms.web.app.workflow.checkout;

import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.IMPrinterFactory;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.print.PrinterListener;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workflow.AbstractTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskListener;
import org.openvpms.web.component.event.WindowPaneListener;
import org.openvpms.web.resource.util.Messages;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * Task to allow the user to selectively print any unprinted documents
 * from a particular time.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class PrintDocumentsTask extends AbstractTask {

    /**
     * The time to select unprinted documents from.
     */
    private final Date startTime;

    /**
     * The charge acts to print.
     */
    private static final String[] CHARGES = {"act.customerAccountCharges*"};

    /**
     * The printable patient documents.
     */
    private static final String[] DOCUMENTS
            = {"act.patientDocumentLetter", "act.patientDocumentForm"};


    /**
     * Constructs a new <tt>PrintDocumentsTask</tt>.
     *
     * @param startTime the act start time.
     */
    public PrintDocumentsTask(Date startTime) {
        this.startTime = startTime;
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
        List<IMObject> unprinted = new ArrayList<IMObject>();
        unprinted.addAll(getCustomerActs(context));
        unprinted.addAll(getPatientActs(context));
        if (unprinted.isEmpty()) {
            notifyCompleted();
        } else {
            String title = Messages.get("workflow.checkout.print.title");
            String[] buttons = isRequired()
                    ? PopupDialog.OK_CANCEL : PopupDialog.OK_SKIP_CANCEL;
            final BatchPrintDialog dialog = new BatchPrintDialog(title, buttons,
                                                                 unprinted);
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void onClose(WindowPaneEvent event) {
                    String action = dialog.getAction();
                    if (BatchPrintDialog.OK_ID.equals(action)) {
                        print(dialog.getSelected(), context);
                    } else if (BatchPrintDialog.SKIP_ID.equals(action)) {
                        notifySkipped();
                    } else {
                        notifyCancelled();
                    }
                }
            });
            dialog.show();
        }
    }

    /**
     * Prints a list of objects.
     *
     * @param objects the objects to print
     * @param context the task context
     */
    private void print(List<IMObject> objects, TaskContext context) {
        BatchPrinter printer = new BatchPrinter(objects, context);
        printer.print();
    }

    /**
     * Returns a list of unprinted customer charges.
     *
     * @param context the task context
     * @return a list of unprinted customer charges
     */
    private List<IMObject> getCustomerActs(TaskContext context) {
        Party customer = context.getCustomer();
        String node = "customer";
        String participation = "participation.customer";
        return getUnprintedActs(CHARGES, customer, node, participation);
    }

    /**
     * Returns a list of unprinted patient documents.
     *
     * @param context the task context
     * @return a list of unprinted patient documents
     */
    private List<IMObject> getPatientActs(TaskContext context) {
        Party patient = context.getPatient();
        String node = "patient";
        String participation = "participation.patient";
        return getUnprintedActs(DOCUMENTS, patient, node, participation);
    }

    /**
     * Returns a list of unprinted acts for a party.
     *
     * @param shortNames    the act short names to query. May include wildcards
     * @param party         the party to query
     * @param node          the participation node to query
     * @param participation the participation short name to query
     * @return the unprinted acts
     */
    private List<IMObject> getUnprintedActs(String[] shortNames, Party party,
                                            String node, String participation) {
        ArchetypeQuery query = new ArchetypeQuery(shortNames, false, true);
        query.setFirstResult(0);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);

        CollectionNodeConstraint participations
                = new CollectionNodeConstraint(node, participation,
                                               false, true);
        participations.add(new ObjectRefNodeConstraint(
                "entity", party.getObjectReference()));

        query.add(participations);
        query.add(new NodeConstraint("startTime", RelationalOp.GTE, startTime));
        query.add(new NodeConstraint("printed", false));

        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        return service.get(query).getResults();

    }

    /**
     * Batch printer. Note that printing currently occurs interactively
     * due to limitations in downloading multiple pdf files to the client
     * browser.
     */
    class BatchPrinter implements PrinterListener {

        /**
         * Iterator over the objects to  print.
         */
        private Iterator<IMObject> iterator;

        /**
         * The object being printed.
         */
        private IMObject object;

        /**
         * The task context.
         */
        private final TaskContext context;


        /**
         * Constructs a new <code>BatchPrinter</code>.
         *
         * @param objects the objects to print
         * @param context the task context
         */
        public BatchPrinter(List<IMObject> objects, TaskContext context) {
            iterator = objects.iterator();
            this.context = context;
        }

        /**
         * Initiates printing of the objects.
         */
        public void print() {
            if (iterator.hasNext()) {
                object = iterator.next();
                try {
                    IMPrinter<IMObject> printer
                            = IMPrinterFactory.create(object);
                    InteractiveIMPrinter<IMObject> iPrinter
                            = new InteractiveIMPrinter<IMObject>(printer);
                    iPrinter.setInteractive(false);
                    iPrinter.setListener(this);
                    iPrinter.print();
                } catch (OpenVPMSException exception) {
                    failed(exception);
                }
            } else {
                notifyCompleted();
            }
        }

        /**
         * Invoked when an object has been successfully printed.
         *
         * @param printer the printer that was used. May be <tt>null</tt>
         */
        public void printed(String printer) {
            boolean next = false;
            try {
                // update the print flag, if it exists
                IMObjectBean bean = new IMObjectBean(object);
                if (bean.hasNode("printed")) {
                    bean.setValue("printed", true);
                    bean.save();
                }
                next = true;
            } catch (OpenVPMSException exception) {
                failed(exception);
            }
            if (next) {
                print(); // print the next available object
            }
        }

        /**
         * Invoked when a print is cancelled. This restarts the task.
         */
        public void cancelled() {
            start(context);
        }

        /**
         * Notifies that the print was skipped. This restarts the task.
         */
        public void skipped() {
            start(context);
        }

        /**
         * Invoked when an object fails to print. This restarts the task.
         *
         * @param cause the reason for the failure
         */
        public void failed(Throwable cause) {
            ErrorHelper.show(cause, new WindowPaneListener() {
                public void onClose(WindowPaneEvent event) {
                    start(context);
                }
            });
        }
    }
}
