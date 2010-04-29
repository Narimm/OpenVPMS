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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.report.openoffice;

import org.junit.After;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentHelper;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.report.ArchetypeServiceTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;


/**
 * Base class for tests requiring OpenOffice.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractOpenOfficeTest extends ArchetypeServiceTest {

    /**
     * The document handlers.
     */
    @Autowired
    private DocumentHandlers handlers;


    /**
     * Tears down the test case.
     */
    @After
    public void tearDown() {
        OOBootstrapService service = (OOBootstrapService) applicationContext.getBean("OOSocketBootstrapService");
        service.stop();
    }

    /**
     * Returns the connection to OpenOffice.
     *
     * @return the connection
     */
    protected OOConnection getConnection() {
        OOConnectionPool pool = OpenOfficeHelper.getConnectionPool();
        return pool.getConnection();
    }

    /**
     * Helper to load a document from a file.
     *
     * @param path     the file path
     * @param mimeType the mime type
     * @return a new document
     */
    protected Document getDocument(String path, String mimeType) {
        File file = new File(path);
        return DocumentHelper.create(file, mimeType, handlers);
    }

    /**
     * Returns the document handlers.
     *
     * @return the document handlers
     */
    protected DocumentHandlers getHandlers() {
        return handlers;
    }

}
