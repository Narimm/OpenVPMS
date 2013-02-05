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
 *  Copyright 2009-2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.etl.tools.doc;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentHelper;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link IdLoader} class.
 *
 * @author Tim Anderson
 */
public class IdLoaderTestCase extends AbstractLoaderTest {

    /**
     * Tests the loader.
     *
     * @throws Exception for any error
     */
    @Test
    public void testLoad() throws Exception {
        File source = new File("target/sdocs" + System.currentTimeMillis());
        File target = new File("target/tdocs" + System.currentTimeMillis());
        assertTrue(source.mkdirs());
        assertTrue(target.mkdirs());

        DocumentAct act1 = createPatientDocAct("file1.gif");
        DocumentAct act2 = createPatientDocAct("file2.pdf");
        DocumentAct act3 = createPatientDocAct("file3.html");
        DocumentAct act4 = createPatientDocAct("file4.png");
        DocumentAct act5 = createPatientDocAct("file5.hml");

        File act1File = createFile(act1, source, null, null, ".gif");
        File act2File = createFile(act2, source, "V", null, ".pdf");
        File act3File = createFile(act3, source, null, "-12345", null, ".html");
        File act4File = createFile(act4, source, "P", "-123456", null, ".png");
        File act5File = createFile(act5, source, "P", "-123457", null, ".htm");

        LoaderListener listener = load(source, target, false);
        assertEquals(5, listener.getLoaded());
        assertEquals(0, listener.getErrors());
        assertEquals(5, listener.getProcessed());
        assertEquals(0, listener.getMissingAct());
        assertEquals(0, listener.getAlreadyLoaded());

        Set<File> files = getFiles(source);
        assertEquals(0, files.size());

        checkFiles(target, act1File, act2File, act3File, act4File, act5File);

        // verify the acts have associated documents
        checkAct(act1, act1File.getName());
        checkAct(act2, act2File.getName());
        checkAct(act3, act3File.getName());
        checkAct(act4, act4File.getName());
        checkAct(act5, act5File.getName());
    }

    /**
     * Verifies behaviour when an act is missing.
     *
     * @throws Exception for any error
     */
    @Test
    public void testMissingAct() throws Exception {
        File source = new File("target/sdocs" + System.currentTimeMillis());
        File target = new File("target/tdocs" + System.currentTimeMillis());
        assertTrue(source.mkdirs());
        assertTrue(target.mkdirs());

        // create files with no corresponding acts.
        File noAct1 = new File(source, "0000.gif");
        FileUtils.touch(noAct1);

        File noAct2 = new File(source, "C987654321.gif");
        FileUtils.touch(noAct2);

        File noAct3 = new File(source, "0000-12345.gif");
        FileUtils.touch(noAct3);

        LoaderListener listener = load(source, target, false);

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
    @Test
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

        LoaderListener listener = load(source, target, false);
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
    @Test
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
        LoaderListener listener1 = load(source, target, overwrite);

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
        LoaderListener listener2 = load(source, target, overwrite);

        assertEquals(2, listener2.getLoaded());
        assertEquals(0, listener2.getErrors());
        assertEquals(2, listener2.getProcessed());
        assertEquals(0, listener2.getMissingAct());
        assertEquals(0, listener2.getAlreadyLoaded());

        checkFiles(source);
        checkFiles(target, first, second, third);

        // verify the act has a document, and it is the third instance
        act = (DocumentAct) service.get(act.getObjectReference());
        checkAct(act, third.getName());

        // now check the first and second versions are present.
        ActBean bean = new ActBean(act);
        List<DocumentAct> versions = bean.getNodeActs("versions", DocumentAct.class);
        assertEquals(2, versions.size());

        DocumentAct firstVersion = getVersion(versions, first.getName());
        assertNotNull(firstVersion);

        DocumentAct secondVersion = getVersion(versions, second.getName());
        assertNotNull(secondVersion);

        // verify each version as an associated document
        checkAct(firstVersion, first.getName());
        checkAct(secondVersion, second.getName());
    }

    /**
     * Verifies that duplicate documents are only loaded if they have a different file name to the original
     * content.
     *
     * @throws Exception for any error
     */
    @Test
    public void testDuplicates() throws Exception {
        File source = new File("target/sdocs" + System.currentTimeMillis());
        File target = new File("target/tdocs" + System.currentTimeMillis());
        assertTrue(source.mkdirs());
        assertTrue(target.mkdirs());

        DocumentAct act = createPatientDocAct();

        int alreadyLoaded = 0;

        // create a file and load it
        File first = createFile(act, source, null, "-Z", "A");
        LoaderListener listener1 = load(source, target, true);
        assertEquals(1, listener1.getLoaded());
        assertEquals(0, listener1.getErrors());
        assertEquals(1, listener1.getProcessed());
        assertEquals(0, listener1.getMissingAct());
        assertEquals(alreadyLoaded, listener1.getAlreadyLoaded());

        // verify the act has a document, and it is the first instance
        Document firstDoc = checkAct(act, first.getName());

        // create a file that duplicates the first in name and content
        File duplicateFirst = createFile(source, first.getName(), "A");
        alreadyLoaded++;
        LoaderListener listenerDup = load(source, target, true);
        assertEquals(0, listenerDup.getLoaded());
        assertEquals(1, listenerDup.getErrors());
        assertEquals(1, listenerDup.getProcessed());
        assertEquals(0, listenerDup.getMissingAct());
        assertEquals(alreadyLoaded, listenerDup.getAlreadyLoaded());

        File second = createFile(act, source, null, "-A", "B");
        LoaderListener listener2 = load(source, target, true);
        assertEquals(1, listener2.getLoaded());
        assertEquals(1, listener2.getErrors());
        assertEquals(2, listener2.getProcessed());
        assertEquals(0, listener2.getMissingAct());
        assertEquals(alreadyLoaded, listener2.getAlreadyLoaded());

        // verify the act has a document, and it is the second instance
        Document secondDoc = checkAct(act, second.getName());
        checkVersions(act, firstDoc);

        // remove the duplicate file as it can now be loaded except will trigger a failure when it is moved
        // from the source to the target directory (file exists).
        // TODO - how should this be handled. Overwrite or rename?
        assertTrue(duplicateFirst.delete());

        // create a third file that duplicates the content of the first. This should be loaded
        File third = createFile(act, source, null, "-X", "A");
        assertTrue(third.setLastModified(second.lastModified() + 1000));
        LoaderListener listener3 = load(source, target, true);
        assertEquals(1, listener3.getLoaded());
        assertEquals(0, listener3.getErrors());
        assertEquals(1, listener3.getProcessed());
        assertEquals(0, listener3.getMissingAct());
        assertEquals(0, listener3.getAlreadyLoaded());

        // verify the act has a document, and it is the third instance, and only the second version remains
        // i.e that the first version has been removed
        checkAct(act, third.getName());
        checkVersions(act, secondDoc);
    }

    /**
     * Verifies that documents are only loaded if they are of the correct type.
     *
     * @throws Exception for any error
     */
    @Test
    public void testLoadByType() throws Exception {
        File source = new File("target/sdocs" + System.currentTimeMillis());
        File target = new File("target/tdocs" + System.currentTimeMillis());
        assertTrue(source.mkdirs());
        assertTrue(target.mkdirs());

        DocumentAct act1 = createPatientDocAct("file1.gif");
        DocumentAct act2 = createPatientDocAct("file2.pdf");
        DocumentAct act3 = createPatientDocAct("file3.html");

        // create some document image acts. These should not be overwritten by the loader
        DocumentAct act4 = createPatientDocAct(PatientArchetypes.DOCUMENT_IMAGE, "file4.png");
        DocumentAct act5 = createPatientDocAct(PatientArchetypes.DOCUMENT_IMAGE, "file5.gif");

        File act1File = createFile(act1, source, null, null, ".gif");
        File act2File = createFile(act2, source, "V", null, ".pdf");
        File act3File = createFile(act3, source, null, "-12345", null, ".html");
        File act4File = createFile(act4, source, null, null, ".png");
        File act5File = createFile(act5, source,  null, null, ".gif");

        // load files that have corresponding document attachment acts. The document image acts should not be uppdated
        LoaderListener listener = load(source, PatientArchetypes.DOCUMENT_ATTACHMENT, target, false);
        assertEquals(3, listener.getLoaded());
        assertEquals(2, listener.getErrors());
        assertEquals(5, listener.getProcessed());
        assertEquals(2, listener.getMissingAct());
        assertEquals(0, listener.getAlreadyLoaded());

        Set<File> files = getFiles(source);
        assertEquals(2, files.size());
        checkFiles(source, act4File, act5File);

        checkFiles(target, act1File, act2File, act3File);

        // verify the acts have associated documents
        checkAct(act1, act1File.getName());
        checkAct(act2, act2File.getName());
        checkAct(act3, act3File.getName());
        checkNoDocument(act4);
        checkNoDocument(act5);
    }

    /**
     * Returns the document act version with the matching file name.
     *
     * @param versions the document act versions
     * @param fileName the file name
     * @return the corresponding document act version, or <tt>null</tt> if none is found
     */
    private DocumentAct getVersion(List<DocumentAct> versions, String fileName) {
        for (DocumentAct version : versions) {
            if (fileName.equals(version.getFileName())) {
                return version;
            }
        }
        return null;
    }

    /**
     * Helper to load files.
     *
     * @param source    the source directory to load from
     * @param target    the target directory to move processed files to
     * @param overwrite if <tt>true</tt> overwrite existing documents
     * @return the loader listener
     */
    private LoaderListener load(File source, File target, boolean overwrite) {
        return load(source, null, target, overwrite);
    }

    /**
     * Helper to load files.
     *
     * @param source    the source directory to load from
     * @param shortName the document archetype(s) that may be loaded to. May be <tt>null</tt>, or contain wildcards
     * @param target    the target directory to move processed files to
     * @param overwrite if <tt>true</tt> overwrite existing documents
     * @return the loader listener
     */
    private LoaderListener load(File source, String shortName, File target, boolean overwrite) {
        Loader loader = new IdLoader(source, shortName != null ? new String[]{shortName} : null, service,
                                     new DefaultDocumentFactory(), transactionManager, true, overwrite);
        LoaderListener listener = new LoggingLoaderListener(DocumentLoader.log, target);
        load(loader, listener);
        return listener;
    }

    /**
     * Verifies the document versions associated with the version node of document act.
     *
     * @param act      the document act
     * @param versions the expected document versions
     */
    private void checkVersions(DocumentAct act, Document... versions) {
        act = (DocumentAct) service.get(act.getObjectReference());
        ActBean bean = new ActBean(act);
        List<DocumentAct> acts = bean.getNodeActs("versions", DocumentAct.class);
        assertEquals(versions.length, acts.size());
        for (DocumentAct childAct : acts) {
            checkAct(childAct);
            boolean found = false;
            for (Document version : versions) {
                if (childAct.getDocument().equals(version.getObjectReference())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
    }

}