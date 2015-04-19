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
 *  Copyright 2007-2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.etl.tools.doc;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Tests the {@link NameLoader} class.
 *
 * @author Tim Anderson
 */
public class NameLoaderTestCase extends AbstractLoaderTest {

    /**
     * Tests the {@link NameLoader}.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testNameLoader() throws IOException {
        File source = new File("target/sdocs" + System.currentTimeMillis());
        File target = new File("target/tdocs" + System.currentTimeMillis());
        assertTrue(source.mkdirs());

        DocumentAct act1 = createPatientDocAct("file1.gif");
        DocumentAct act2 = createPatientDocAct("file2.pdf");
        DocumentAct act3 = createPatientDocAct("file3.html");
        DocumentAct act4 = createPatientDocAct("file4.png");
        DocumentAct act5 = createPatientDocAct("file5.htm");
        DocumentAct act6 = createPatientDocAct("file6.txt");
        DocumentAct act7 = createPatientDocAct("file7.doc");
        DocumentAct act8 = createPatientDocAct("file8.odt");
        List<DocumentAct> expected = Arrays.asList(act1, act2, act3, act4, act5, act6, act7, act8);

        File act1File = createFile(source, act1.getFileName(), null);
        File act2File = createFile(source, act2.getFileName(), null);
        File act3File = createFile(source, act3.getFileName(), null);
        File act4File = createFile(source, act4.getFileName(), null);
        File act5File = createFile(source, act5.getFileName(), null);
        File act6File = createFile(source, act6.getFileName(), null);
        File act7File = createFile(source, act7.getFileName(), null);
        File act8File = createFile(source, act8.getFileName(), null);

        Loader loader = new NameLoader(source, new String[]{PatientArchetypes.DOCUMENT_ATTACHMENT}, service,
                                       new DefaultDocumentFactory());
        LoggingLoaderListener listener = new LoggingLoaderListener(DocumentLoader.log, target);
        load(loader, listener);

        // verify there a no acts left to load documents for.
        ArchetypeQuery query = createQuery();
        Iterator<DocumentAct> iter = new IMObjectQueryIterator<DocumentAct>(query);
        while (iter.hasNext()) {
            ActBean bean = new ActBean(iter.next());
            String fileName = bean.getString("fileName");
            assertNull(fileName);
        }

        checkFiles(target, act1File, act2File, act3File, act4File, act5File, act6File, act7File, act8File);

        assertEquals(expected.size(), listener.getLoaded());
        assertEquals(expected.size(), listener.getProcessed());
        assertEquals(0, listener.getErrors());
        for (DocumentAct e : expected) {
            e = (DocumentAct) service.get(e.getObjectReference());
            assertNotNull(e);
            checkMimeType(e);
        }
    }

    /**
     * Verifies that document templates cannot be loaded to.
     */
    @Test
    public void testLoadDocumentTemplate() throws Exception {
        File source = new File("target/sdocs" + System.currentTimeMillis());
        File target = new File("target/tdocs" + System.currentTimeMillis());
        assertTrue(source.mkdirs());
        assertTrue(target.mkdirs());

        // verify that the loader cannot be constructed to load act.documentTemplate acts
        try {
            new NameLoader(source, new String[]{DocumentArchetypes.DOCUMENT_TEMPLATE_ACT}, service,
                           new DefaultDocumentFactory());
            fail("Expected exception to be thrown");
        } catch (Throwable exception) {
            assertEquals(exception.getMessage(), "Argument 'shortNames' doesn't refer to any valid archetype for "
                                                 + "loading documents to: {act.documentTemplate}");
        }

        // verify that an act.documentTemplate cannot be overwritten by a load
        DocumentAct act = (DocumentAct) service.create(DocumentArchetypes.DOCUMENT_TEMPLATE_ACT);
        File file = createFile(act, source, null, "-Z", "A");

        Entity template = (Entity) service.create(DocumentArchetypes.DOCUMENT_TEMPLATE);
        act.setName("foo");
        act.setDescription("bar");
        template.setName("ZTemplate");
        ActBean bean = new ActBean(act);
        bean.setValue("fileName", file.getName());
        bean.addNodeParticipation("template", template);
        service.save(Arrays.asList(act, template));


        Loader loader = new NameLoader(source, new String[]{PatientArchetypes.DOCUMENT_ATTACHMENT}, service,
                                       new DefaultDocumentFactory());
        LoggingLoaderListener listener = new LoggingLoaderListener(DocumentLoader.log, target);

        load(loader, listener);
        assertEquals(0, listener.getLoaded());
        assertEquals(0, listener.getAlreadyLoaded());
        assertEquals(0, listener.getMissingAct());
        assertEquals(0, listener.getErrors());
    }


    /**
     * Removes all <em>act.patientDocumentAttachment</em> acts with no document, and a non-null file name.
     */
    @Before
    public void setUp() {
        // remove all documents matching the short name with null doc references
        // and non-null filenames
        ArchetypeQuery query = createQuery();
        Iterator<DocumentAct> iter = new IMObjectQueryIterator<DocumentAct>(query);
        while (iter.hasNext()) {
            DocumentAct act = iter.next();
            ActBean bean = new ActBean(act);
            String fileName = bean.getString("fileName");
            if (fileName != null) {
                service.remove(act);
            }
        }

    }

    /**
     * Creates a query for <em>act.patientDocumentAttachment</em> acts with no document.
     *
     * @return a new query.
     */
    private ArchetypeQuery createQuery() {
        ArchetypeQuery query = new ArchetypeQuery(PatientArchetypes.DOCUMENT_ATTACHMENT, false, true);
        query.add(new NodeConstraint("document", RelationalOp.IS_NULL));
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        return query;
    }

}
