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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.customer.charge;

import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.event.WindowPaneListener;
import org.openvpms.web.component.im.edit.act.ActEditDialog;
import org.openvpms.web.component.print.BatchPrinter;
import org.openvpms.web.component.util.ErrorHelper;

import java.util.List;


/**
 * An edit dialog for {@link CustomerChargeActEditor} editors.
 * <p/>
 * This performs printing of unprinted documents that have their <em>interactive</em> flag set to <tt>true</tt>
 * when <em>Apply</em> or <em>OK</em> is pressed.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerChargeActEditDialog extends ActEditDialog {

    /**
     * Completed button identifier.
     */
    private static final String COMPLETED_ID = "completed";

    /**
     * In Progress button identifier.
     */
    private static final String IN_PROGRESS_ID = "inprogress";


    /**
     * Constructs a <tt>CustomerChargeActEditDialog</tt>.
     *
     * @param editor the editor
     */
    public CustomerChargeActEditDialog(CustomerChargeActEditor editor) {
        super(editor);
        addButton(COMPLETED_ID, false);
        addButton(IN_PROGRESS_ID, false);
        setDefaultCloseAction(CANCEL_ID);
    }

    /**
     * Saves the current object.
     * <p/>
     * Any documents added as part of the save that have a template with an IMMEDIATE print mode will be printed.
     */
    @Override
    protected void onOK() {
        List<Act> existing = getUnprintedDocuments();
        if (save()) {
            List<Act> docs = getUnprintedDocuments(existing);
            if (!docs.isEmpty()) {
                printDocuments(docs, true);
            } else {
                close(OK_ID);
            }
        }
    }

    /**
     * Saves the current object.
     * <p/>
     * Any documents added as part of the save that have a template with an IMMEDIATE print mode will be printed.
     */
    @Override
    protected void onApply() {
        List<Act> existing = getUnprintedDocuments();
        if (save()) {
            List<Act> docs = getUnprintedDocuments(existing);  // only select documents added during save
            if (!docs.isEmpty()) {
                printDocuments(docs, false);
            }
        }
    }

    /**
     * Invoked when a button is pressed. This delegates to the appropriate
     * on*() method for the button if it is known, else sets the action to
     * the button identifier and closes the window.
     *
     * @param button the button identifier
     */
    @Override
    protected void onButton(String button) {
        if (IN_PROGRESS_ID.equals(button)) {
            onInProgress();
        } else if (COMPLETED_ID.equals(button)) {
            onCompleted();
        } else {
            super.onButton(button);
        }
    }

    /**
     * Invoked when the 'In Progress' button is pressed.
     * <p/>
     * If the act hasn't been POSTED, then this sets the status to IN_PROGRESS, and attempts to save and close the
     * dialog.
     */
    private void onInProgress() {
        if (!isPosted()) {
            CustomerChargeActEditor editor = (CustomerChargeActEditor) getEditor();
            editor.setStatus(ActStatus.IN_PROGRESS);
            onOK();
        }
    }

    /**
     * Invoked when the 'Completed' button is pressed.
     * <p/>
     * If the act hasn't been POSTED, then this sets the status to COMPLETED, and attempts to save and close the
     * dialog.
     */
    private void onCompleted() {
        if (!isPosted()) {
            CustomerChargeActEditor editor = (CustomerChargeActEditor) getEditor();
            editor.setStatus(ActStatus.COMPLETED);
            onOK();
        }
    }

    /**
     * Returns any unprinted documents flagged for IMMEDIATE printing.
     *
     * @return a list of unprinted documents
     */
    private List<Act> getUnprintedDocuments() {
        CustomerChargeActEditor editor = (CustomerChargeActEditor) getEditor();
        return editor.getUnprintedDocuments();
    }

    /**
     * Returns any unprinted documents flagged for IMMEDIATE printing.
     *
     * @param exclude acts to ignore when determining the unprinted documents
     * @return a list of unprinted documents
     */
    private List<Act> getUnprintedDocuments(List<Act> exclude) {
        CustomerChargeActEditor editor = (CustomerChargeActEditor) getEditor();
        return editor.getUnprintedDocuments(exclude);
    }

    /**
     * Prints any unprinted documents flagged as <em>interactive</em>, associated with the charge.
     *
     * @param documents the documents to print
     * @param close     if <tt>true</tt>, close the edit dialog on completion
     */
    private void printDocuments(List<Act> documents, final boolean close) {
        BatchPrinter printer = new BatchPrinter<Act>(documents) {

            public void failed(Throwable cause) {
                ErrorHelper.show(cause, new WindowPaneListener() {
                    public void onClose(WindowPaneEvent event) {
                        print(); // print the next document
                    }
                });
            }

            /**
             * Invoked when printing completes. Closes the edit dialog if required.
             */
            @Override
            protected void completed() {
                if (close) {
                    close(OK_ID);
                }
            }
        };
        printer.print();
    }

}