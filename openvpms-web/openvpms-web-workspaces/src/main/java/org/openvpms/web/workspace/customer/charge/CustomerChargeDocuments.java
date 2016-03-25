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

package org.openvpms.web.workspace.customer.charge;

import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.print.BatchPrinter;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Helper to print customer charge documents.
 *
 * @author Tim Anderson
 */
public class CustomerChargeDocuments {

    /**
     * The charge editor.
     */
    private final CustomerChargeActEditor editor;

    /**
     * The layout context.
     */
    private final LayoutContext context;

    /**
     * The references of documents to exclude.
     */
    private Set<IMObjectReference> exclude = new HashSet<>();

    /**
     * Constructs a {@link CustomerChargeDocuments}.
     *
     * @param editor  the charge editor
     * @param context the layout context
     */
    public CustomerChargeDocuments(CustomerChargeActEditor editor, LayoutContext context) {
        this.editor = editor;
        this.context = context;
        List<Act> documents = getUnprintedDocuments(); // exclude any existing unprinted documents
        exclude(documents);
    }

    /**
     * Prints new documents.
     *
     * @param listener the listener to notify on completion. May be {@code null}
     * @return {@code true} if there were documents to print
     */
    public boolean printNew(final ActionListener listener) {
        List<Act> documents = getUnprintedDocuments();
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
     * @param listener  the listener to notify on completion. May be {@code null}
     */
    protected void print(final List<Act> documents, final ActionListener listener) {
        LocalContext context = new LocalContext();
        context.setCustomer(editor.getCustomer());
        context.setLocation(editor.getLocation());
        exclude(documents); // exclude these documents from subsequent prints
        BatchPrinter printer = new BatchPrinter<Act>(documents, context, editor.getHelpContext()) {

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

    /**
     * Returns any unprinted documents that are flagged for immediate printing.
     *
     * @return the list of unprinted documents
     */
    protected List<Act> getUnprintedDocuments() {
        List<Act> result = new ArrayList<>();
        ActRelationshipCollectionEditor items = editor.getItems();
        for (Act item : items.getActs()) {
            ActBean bean = new ActBean(item);
            if (bean.hasNode("documents")) {
                for (ActRelationship rel : bean.getValues("documents", ActRelationship.class)) {
                    IMObjectReference target = rel.getTarget();
                    if (target != null && !exclude.contains(target)) {
                        Act document = (Act) context.getCache().get(target);
                        if (document != null && isPrintImmediate(document)) {
                            result.add(document);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Adds documents to exclude from subsequent prints.
     *
     * @param documents the documents to exclude
     */
    private void exclude(List<Act> documents) {
        for (Act document : documents) {
            exclude.add(document.getObjectReference());
        }
    }

    /**
     * Determines if a document should be printed immediately.
     *
     * @param document the document
     * @return {@code true} if the document should be printed immediately
     */
    private boolean isPrintImmediate(Act document) {
        ActBean documentBean = new ActBean(document);
        if (!documentBean.getBoolean("printed") && documentBean.hasNode("documentTemplate")) {
            Entity entity = (Entity) context.getCache().get(documentBean.getNodeParticipantRef("documentTemplate"));
            if (entity != null) {
                DocumentTemplate template = new DocumentTemplate(entity, ServiceHelper.getArchetypeService());
                return (template.getPrintMode() == DocumentTemplate.PrintMode.IMMEDIATE);
            }
        }
        return false;
    }
}
