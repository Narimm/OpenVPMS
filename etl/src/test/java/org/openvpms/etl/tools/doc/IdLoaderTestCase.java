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
        File source = new File("target/sdocs" + System.currentTimeMillis());
        File target = new File("target/tdocs" + System.currentTimeMillis());
        assertTrue(source.mkdirs());
        assertTrue(target.mkdirs());

        DocumentAct act1 = createPatientDocAct();
        DocumentAct act2 = createPatientDocAct();
        DocumentAct act3 = createPatientDocAct();
        DocumentAct act4 = createPatientDocAct();

        // create files with varying file names for each act
        File act1File = createFile(act1, source, null);
        File act2File = createFile(act2, source, "V");
        File act3File = createFile(act3, source, null, "-12345");
        File act4File = createFile(act4, source, "P", "-123456");

        IdLoader idLoader = new IdLoader(source, service, new DefaultDocumentFactory(), true, false);
        LoaderListener listener = new LoggingLoaderListener(DocumentLoader.log, target);
        load(idLoader, listener);

        assertEquals(4, listener.getLoaded());
        assertEquals(0, listener.getErrors());
        assertEquals(4, listener.getProcessed());
        assertEquals(0, listener.getMissingAct());
        assertEquals(0, listener.getAlreadyLoaded());

        Set<File> files = getFiles(source);
        assertEquals(0, files.size());

        checkFiles(target, act1File, act2File, act3File, act4File);

        // verify the acts have associated documents
        checkAct(act1);
        checkAct(act2);
        checkAct(act3);
        checkAct(act4);
    }

    /**
     * Verifies behaviour when an act is missing.
     *
     * @throws Exception for any error
     */
    public void testMissingAct() throws Exception {
        File source = new File("target/sdocs" + System.currentTimeMillis());
        File target = new File("target/tdocs" + System.currentTimeMillis());
        assertTrue(source.mkdirs());
        assertTrue(target.mkdirs());

        // create files with no corresponding acts.
        File noAct1 = new File(source, "0000.gif");
        FileUtils.touch(noAct1);

        File noAct2 = new File(source, "C0001.gif");
        FileUtils.touch(noAct2);

        File noAct3 = new File(source, "0000-12345.gif");
        FileUtils.touch(noAct3);

        IdLoader idLoader = new IdLoader(source, service, new DefaultDocumentFactory(), true, false);
        LoaderListener listener = new LoggingLoaderListener(DocumentLoader.log, target);
        load(idLoader, listener);

        int expectedErrors = 3;
        assertEquals(0, listener.getLoaded());
        assertEquals(expectedErrors, listener.getErrors());
        assertEquals(expectedErrors, listener.getProcessed());
        assertEquals(expectedErrors, listener.getMissingAct());
        assertEquals(0, listener.getAlreadyLoaded());

        checkFiles(source, noAct1, noAct2, noAct3);
        checkFiles(target);
    }

    /**
     * Tests the loader.
     *
     * @throws Exception for any error
     */
    public void testSkipProcessed() throws Exception {
        File source = new File("target/sdocs" + System.currentTimeMillis());
        File target = new File("target/tdocs" + System.currentTimeMillis());
        assertTrue(source.mkdirs());
        assertTrue(target.mkdirs());

        // create a file and associate it with the act. The loader will skip it
        DocumentAct preLoaded = createPatientDocAct();
        File preloadedFile = createFile(preLoaded, source, null);
        Document doc = DocumentHelper.create(preloadedFile, "image/gif", new DocumentHandlers());
        preLoaded.setDocument(doc.getObjectReference());
        service.save(Arrays.asList(doc, preLoaded));

        IdLoader idLoader = new IdLoader(source, service, new DefaultDocumentFactory(), true, false);
        LoaderListener listener = new LoggingLoaderListener(DocumentLoader.log, target);
        load(idLoader, listener);

        assertEquals(0, listener.getLoaded());
        assertEquals(1, listener.getErrors());
        assertEquals(1, listener.getProcessed());
        assertEquals(0, listener.getMissingAct());
        assertEquals(1, listener.getAlreadyLoaded());

        checkFiles(source, preloadedFile);
        checkFiles(target);
    }

    /**
     * Verifies that the multiple files for the same act are ordered on timestamp so that the most recent file
     * is processed last.
     *
     * @throws Exception for any error
     */
    public void testTimestampOrdering() throws Exception {
        final int count = 3;
        File source = new File("target/sdocs" + System.currentTimeMillis());
        File target = new File("target/tdocs" + System.currentTimeMillis());
        assertTrue(source.mkdirs());
        assertTrue(target.mkdirs());

        DocumentAct act = createPatientDocAct();

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
        checkFiles(target, first);
        checkFiles(source, second, third); // not moved as overwrite = false

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
        checkFiles(target, first, second, third);

        // verify the act has a document, and it is the third instance
        act = (DocumentAct) service.get(act.getObjectReference());
        assertNotNull(act);
        assertNotNull(act.getDocument());
        Document doc = (Document) service.get(act.getDocument());
        assertNotNull(doc);
        assertEquals(third.getName(), doc.getName());
    }


}