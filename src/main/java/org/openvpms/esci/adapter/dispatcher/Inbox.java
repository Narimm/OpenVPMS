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

import org.oasis.ubl.common.aggregate.DocumentReferenceType;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.esci.exception.DocumentNotFoundException;
import org.openvpms.esci.service.InboxService;

import java.util.List;


/**
 * Associates a supplier with an <tt>InboxService</tt>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class Inbox implements InboxService {

    /**
     * The supplier.
     */
    private final Party supplier;

    /**
     * The stock location.
     */
    private final Party stockLocation;

    /**
     * The supplier account identifier. May be <tt>null</tt>.
     */
    private final String accountId;

    /**
     * The inbox service.
     */
    private final InboxService inbox;


    /**
     * Constructs an <tt>Inbox</tt>.
     *
     * @param supplier      the supplier
     * @param stockLocation the stock location
     * @param accountId     the supplier account identifier. May be <tt>null</tt>
     * @param inbox         the inbox service
     */
    public Inbox(Party supplier, Party stockLocation, String accountId, InboxService inbox) {
        this.supplier = supplier;
        this.stockLocation = stockLocation;
        this.accountId = accountId;
        this.inbox = inbox;
    }

    /**
     * Returns the supplier.
     *
     * @return the supplier
     */
    public Party getSupplier() {
        return supplier;
    }

    /**
     * Returns the stock location.
     *
     * @return the stock location
     */
    public Party getStockLocation() {
        return stockLocation;
    }

    /**
     * Returns the supplier account identifier.
     *
     * @return the supplier account identifier. May be <tt>null</tt>
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * Returns a list of document references, in the order that they were received.
     * <p/>
     * Each reference uniquely identifies a single document.
     *
     * @return a list of document references
     */
    public List<DocumentReferenceType> getDocuments() {
        return inbox.getDocuments();
    }

    /**
     * Returns the document with the specified reference.
     *
     * @param reference the document reference
     * @return the corresponding document, or <tt>null</tt> if the document is not found
     */
    public Object getDocument(DocumentReferenceType reference) {
        return inbox.getDocument(reference);
    }

    /**
     * Acknowledges a document.
     * <p/>
     * Once acknowledged, the document will no longer be returned by {@link #getDocuments} nor {@link #getDocument}.
     *
     * @param reference the document reference
     * @throws DocumentNotFoundException if the reference doesn't refer to a valid document
     */
    public void acknowledge(DocumentReferenceType reference) throws DocumentNotFoundException {
        inbox.acknowledge(reference);
    }

}
