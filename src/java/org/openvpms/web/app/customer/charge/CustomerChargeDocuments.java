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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.customer.charge;

import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.event.WindowPaneListener;
import org.openvpms.web.component.print.BatchPrinter;
import org.openvpms.web.component.util.ErrorHelper;

import java.util.List;


/**
 * Helper to print customer charge documents.
 *
 * @author Tim Anderson
 */
public class CustomerChargeDocuments {

    /**
     * The charge editor.
     */
    private final AbstractCustomerChargeActEditor editor;

    /**
     * Constructs a {@code CustomerChargeDocuments}.
     *
     * @param editor the charge editor
     */
    public CustomerChargeDocuments(AbstractCustomerChargeActEditor editor) {
        this.editor = editor;
    }

    /**
     * Returns any unprinted documents flagged for IMMEDIATE printing.
     *
     * @return a list of unprinted documents
     */
    public List<Act> getUnprinted() {
        return editor.getUnprintedDocuments();
    }

    /**
     * Returns any unprinted documents flagged for IMMEDIATE printing.
     *
     * @param exclude acts to ignore when determining the unprinted documents
     * @return a list of unprinted documents
     */
    public List<Act> getUnprinted(List<Act> exclude) {
        return editor.getUnprintedDocuments(exclude);
    }

    /**
     * Prints new documents.
     *
     * @param existing the existing documents
     * @param listener the listener to notify on completion. May be <tt>null</tt>
     * @return {@code true} if there were documents to print
     */
    public boolean printNew(List<Act> existing, final ActionListener listener) {
        List<Act> documents = getUnprinted(existing);
        if (!documents.isEmpty()) {
            print(documents, listener);
            return true;
        }
        return false;
    }

    /**
     * Prints documents.
     *
     * @param documents the documents to print
     * @param listener  the listener to notify on completion. May be <tt>null</tt>
     */
    public void print(final List<Act> documents, final ActionListener listener) {
        LocalContext context = new LocalContext();
        context.setCustomer(editor.getCustomer());
        context.setLocation(editor.getLocation());
        BatchPrinter printer = new BatchPrinter<Act>(documents, context) {

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
                if (listener != null) {
                    listener.actionPerformed(new ActionEvent(this, "completed"));
                }
            }
        };
        printer.print();
    }
}
