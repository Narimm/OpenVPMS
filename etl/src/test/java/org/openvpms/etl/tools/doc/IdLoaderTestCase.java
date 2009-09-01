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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.tools.doc;

import org.apache.commons.io.FileUtils;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentHelper;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.document.Document;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


/**
 * Tests the {@link IdLoader} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IdLoaderTestCase extends AbstractLoaderTest {


    /**
     * Tests the loader.
     *
     * @throws Exception for any error
     */
    public void testLoad() throws Exception {
        final int count = 10;
        final String patientAct = "act.patientDocumentAttachment";
        final String customerAct = "act.customerDocumentAttachment";
        File source = new File("target/sdocs" + System.currentTimeMillis());
        File target = new File("target/tdocs" + System.currentTimeMillis());
        assertTrue(source.mkdirs());
        assertTrue(target.mkdirs());

        DocumentAct[] acts = new DocumentAct[count];
        String[] expectedNames = new String[count];
        for (int i = 0; i < count; ++i) {
            DocumentAct act = createPatientDocAct(patientAct);
            File file = createFile(act, source, null);
            acts[i] = act;
            expectedNames[i] = file.getName();
        }

        // create a file with no corresponding act.
        File noActFile = new File(source, "0000.gif");
        FileUtils.touch(noActFile);

        // create a file with an incorrect prefix for the id.
        DocumentAct invalid1 = createPatientDocAct(patientAct);

        // C indicates customer act
        File invalidF1 = createFile(invalid1, source, "C");

        DocumentAct invalid2 = createCustomerDocAct(customerAct);
        // P indicates patient act
        File invalidF2 = createFile(invalid2, source, "P");

        DocumentAct invalid3 = createCustomerDocAct(customerAct);
        // V indicates patient act
        File invalidF3 = createFile(invalid3, source, "V");

        // create a file and associate it with the act. The loader will skip
        // it
        DocumentAct preLoaded = createPatientDocAct(patientAct);
        File preloadedFile = createFile(preLoaded, source, null);
        Document doc = DocumentHelper.create(preloadedFile, "image/gif",
                                             new DocumentHandlers());
        preLoaded.setDocument(doc.getObjectReference());
        service.save(Arrays.asList(doc, preLoaded));

        IdLoader idLoader = new IdLoader(source, service, new DefaultDocumentFactory(), true, false);
        LoaderListener listener = new LoggingLoaderListener(DocumentLoader.log, target);
        load(idLoader, listener);

        int expectedErrors = 5;
        assertEquals(count, listener.getLoaded());
        assertEquals(expectedErrors, listener.getErrors());
        assertEquals(count + expectedErrors, listener.getProcessed());
        assertEquals(4, listener.getMissingAct());
        assertEquals(1, listener.getAlreadyLoaded());

        Set<File> files = getFiles(source);
        assertEquals(expectedErrors, files.size());
        assertTrue(files.contains(noActFile));
        assertTrue(files.contains(invalidF1));
        assertTrue(files.contains(invalidF2));
        assertTrue(files.contains(invalidF3));

        checkFiles(target, expectedNames);

        // verify the acts have associated documents
        for (DocumentAct act : acts) {
            act = (DocumentAct) service.get(act.getObjectReference());
            assertNotNull(act);
            assertNotNull(act.getDocument());
            assertNotNull(service.get(act.getDocument()));
        }
    }

    /**
     * Verifies that the multiple files for the same act are ordered on timestamp so that the most recent file
     * is processed last.
     *
     * @throws Exception for any error
     */
    public void testTimestampOrdering() throws Exception {
        final int count = 3;
        final String patientAct = "act.patientDocumentAttachment";
        File source = new File("target/sdocs" + System.currentTimeMillis());
        File target = new File("target/tdocs" + System.currentTimeMillis());
        assertTrue(source.mkdirs());
        assertTrue(target.mkdirs());

        DocumentAct act = createPatientDocAct(patientAct);

        // generate 3 files for the same act, and assign last modification timestamps to ensure that they are
        // processed in the correct order
        File first = createFile(act, source, null, "-Z");
        File second = createFile(act, source, null, "-A");
        File third = createFile(act, source, null, "-X");
        assertTrue(second.setLastModified(first.lastModified() + 1000));
        assertTrue(third.setLastModified(second.lastModified() + 1000));

        // for the first load, set overwrite = false. Only first should be loaded.
        boolean overwrite = false;
        Loader loader1 = new IdLoader(source, service, new DefaultDocumentFactory(), true, overwrite);
        LoaderListener listener1 = new LoggingLoaderListener(DocumentLoader.log, target);
        load(loader1, listener1);

        assertEquals(1, listener1.getLoaded());
        assertEquals(2, listener1.getErrors()); // errors as overwrite = false
        assertEquals(count, listener1.getProcessed());
        assertEquals(0, listener1.getMissingAct());
        assertEquals(2, listener1.getAlreadyLoaded());

        // verify first is moved, but second and third remain
        checkFiles(target, first.getName());
        checkFiles(source, second.getName(), third.getName()); // not moved as overwrite = false

        // now re-run with overwrite = true. The second and third file should now be loaded
        overwrite = true;
        Loader loader2 = new IdLoader(source, service, new DefaultDocumentFactory(), true, overwrite);
        LoaderListener listener2 = new LoggingLoaderListener(DocumentLoader.log, target);
        load(loader2, listener2);

        assertEquals(2, listener2.getLoaded());
        assertEquals(0, listener2.getErrors());
        assertEquals(2, listener2.getProcessed());
        assertEquals(0, listener2.getMissingAct());
        assertEquals(0, listener2.getAlreadyLoaded());

        checkFiles(source);
        checkFiles(target, first.getName(), second.getName(), third.getName());

        // verify the act has a document, and it is the third instance
        act = (DocumentAct) service.get(act.getObjectReference());
        assertNotNull(act);
        assertNotNull(act.getDocument());
        Document doc = (Document) service.get(act.getDocument());
        assertNotNull(doc);
        assertEquals(third.getName(), doc.getName());
    }

    /**
     * Verifies that a directory contains the expected files.
     *
     * @param dir   the directory
     * @param names the expected file names
     */
    private void checkFiles(File dir, String... names) {
        Set<File> files = getFiles(dir);
        assertEquals(names.length, files.size());
        List<String> list = Arrays.asList(names);
        for (File file : files) {
            assertTrue(list.contains(file.getName()));
        }
    }

}