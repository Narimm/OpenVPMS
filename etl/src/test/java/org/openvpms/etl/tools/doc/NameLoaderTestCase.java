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

import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Tests the {@link NameLoader} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class NameLoaderTestCase extends AbstractLoaderTest {

    /**
     * Tests the {@link NameLoader}.
     */
    public void testNameLoader() {
        final int count = 10;
        final String shortName = "act.patientDocumentAttachment";
        for (int i = 0; i < count; ++i) {
            createPatientDocAct(shortName, "file" + i);
        }

        // find all documents matching the short name with null doc references
        // and non-null filenames
        ArchetypeQuery query = new ArchetypeQuery(shortName, false, true);
        query.add(new NodeConstraint("document", RelationalOp.IsNULL));
        List<DocumentAct> expected = new ArrayList<DocumentAct>();
        Iterator<DocumentAct> iter = new IMObjectQueryIterator<DocumentAct>(query);
        while (iter.hasNext()) {
            DocumentAct act = iter.next();
            ActBean bean = new ActBean(act);
            if (bean.getString("fileName") != null) {
                expected.add(act);
            }
        }
        assertTrue(expected.size() >= count);

        Loader loader = new NameLoader(new File("./"), shortName, service, new TestFactory());
        LoggingLoaderListener listener = new LoggingLoaderListener(DocumentLoader.log);
        load(loader, listener);

        // verify there a no acts left to load documents for.
        iter = new IMObjectQueryIterator<DocumentAct>(query);
        assertFalse(iter.hasNext());
        assertEquals(expected.size(), listener.getLoaded());
        assertEquals(expected.size(), listener.getProcessed());
        assertEquals(0, listener.getErrors());
    }

    /**
     * Test document factory that simply creates a new document for a
     * document act.
     */
    private class TestFactory implements DocumentFactory {

        public Document create(
                File file, String mimeType) {
            Document doc = (Document) service.create("document.other");
            doc.setName(file.getName());
            return doc;
        }
    }

}
