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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.tools.doc;

import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.component.business.domain.im.document.Document;

import java.io.File;


/**
 * Factory for {@link Document} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
interface DocumentFactory {

    /**
     * Creates a document from the supplied file.
     *
     * @param file the file
     * @return a new document
     * @throws DocumentException for any error
     */
    Document create(File file);
}
