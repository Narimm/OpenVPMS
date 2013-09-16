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

package org.openvpms.archetype.rules.product.io;

import org.openvpms.component.business.domain.im.document.Document;

/**
 * Reads product data from a document.
 *
 * @author Tim Anderson
 */
public interface ProductReader {

    /**
     * Reads a document.
     *
     * @param document the document to read
     * @return the read product data
     * @throws ProductIOException for any error
     */
    ProductDataSet read(Document document);
}