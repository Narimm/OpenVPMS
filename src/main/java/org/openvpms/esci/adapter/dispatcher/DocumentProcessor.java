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

import org.openvpms.component.business.domain.im.party.Party;


/**
 * Processes document.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public interface DocumentProcessor {

    /**
     * Determines if this processor can handle the supplied document.
     *
     * @param document the document
     * @return <tt>true</tt> if the processor can handle the document, otherwise <tt>false</tt>
     */
    boolean canHandle(Document document);

    /**
     * Processes a document.
     *
     * @param document      the document to process
     * @param supplier      the supplier submitting the document
     * @param stockLocation the stock location
     * @param accountId     the supplier account identifier  @throws ESCIAdapterException for any error
     */
    void process(Document document, Party supplier, Party stockLocation, String accountId);
}


