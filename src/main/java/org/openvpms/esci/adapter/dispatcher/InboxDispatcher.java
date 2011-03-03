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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.esci.adapter.dispatcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.util.ESCIAdapterException;
import org.openvpms.esci.exception.DocumentNotFoundException;
import org.openvpms.esci.ubl.common.aggregate.DocumentReferenceType;

import java.util.Iterator;
import java.util.List;


/**
 * Reads documents from an {@link Inbox} and dispatches them to {@link DocumentProcessor}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class InboxDispatcher {

    /**
     * The inbox service.
     */
    private final Inbox inbox;

    /**
     * The document processors.
     */
    private final List<DocumentProcessor> processors;

    /**
     * The document references.
     */
    private Iterator<DocumentReferenceType> references;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(InboxDispatcher.class);


    /**
     * Constructs an <tt>InboxDispatcher</tt>.
     *
     * @param inbox      the inbox to read documents from
     * @param processors the document processors
     */
    public InboxDispatcher(Inbox inbox, List<DocumentProcessor> processors) {
        this.inbox = inbox;
        this.processors = processors;
    }

    /**
     * Determines if there are documents to dispatch.
     *
     * @return <tt>true</tt> if there are more documents to dispatch
     */
    public boolean hasNext() {
        if (references == null || !references.hasNext()) {
            references = inbox.getDocuments().iterator();
        }
        return references.hasNext();
    }

    /**
     * Dispatches the next document, if any.
     * <p/>
     * This fetches the next available document from the <tt>InboxService</tt>, and dispatches it to its corresponding
     * {@link DocumentProcessor}.
     * <p/>
     * If the document is sucessfully processed, it will be acknowledged.
     *
     * @throws ESCIAdapterException if the document cannot be processed, or if there is an error communicating with the
     *                              InboxService.
     */
    public void dispatch() {
        if (hasNext()) {
            DocumentReferenceType reference = references.next();
            Object content = inbox.getDocument(reference);
            if (content != null) {
                Document document = new Document(reference, content);
                DocumentProcessor processor = getProcessor(inbox.getSupplier(), document);
                processor.process(document, inbox.getSupplier(), inbox.getStockLocation(), inbox.getAccountId());
                try {
                    inbox.acknowledge(reference);
                } catch (DocumentNotFoundException exception) {
                    failedToAcknowledgeDocument(reference, exception);
                }
            } else {
                documentNotFound(reference);
            }
        }
    }

    /**
     * Invoked when a document isn't found.
     *
     * @param reference the document reference
     */
    protected void documentNotFound(DocumentReferenceType reference) {
        log.warn(ESCIAdapterMessages.documentNotFound(inbox.getSupplier(), reference));
    }

    /**
     * Invoked when a document is processed, but failed to be acknowledged as it no longer exists.
     *
     * @param reference the document reference
     * @param exception the cause
     */
    protected void failedToAcknowledgeDocument(DocumentReferenceType reference, DocumentNotFoundException exception) {
        log.warn(ESCIAdapterMessages.failedToAcknowledgeDocument(inbox.getSupplier(), reference), exception);
    }

    /**
     * Returns a processor for the supplied document,
     *
     * @param supplier the sender of the document
     * @param document the document
     * @return a processor for the document
     * @throws ESCIAdapterException if no processor can be found
     */
    protected DocumentProcessor getProcessor(Party supplier, Document document) {
        for (DocumentProcessor documentProcessor : processors) {
            if (documentProcessor.canHandle(document)) {
                return documentProcessor;
            }
        }
        throw new ESCIAdapterException(ESCIAdapterMessages.unsupportedDocument(supplier,
                                                                               document.getDocumentReference()));
    }

}
