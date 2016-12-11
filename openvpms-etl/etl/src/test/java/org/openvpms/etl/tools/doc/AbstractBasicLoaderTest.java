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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.etl.tools.doc;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests common to {@link IdLoader} and {@link NameLoader}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractBasicLoaderTest extends AbstractLoaderTest {

    /**
     * Verifies that when a file cannot be loaded, it can be moved to an error directory.
     *
     * @throws Exception for any error
     */
    @Test
    public void testMoveToErrorDir() throws Exception {
        File source = folder.newFolder("sdocs");
        File target = folder.newFolder("tdocs");
        File error = folder.newFolder("edocs");

        // create a file with no corresponding act.
        File file = new File(source, System.nanoTime() + ".gif");
        FileUtils.touch(file);

        LoaderListener listener = new LoggingLoaderListener(DocumentLoader.log);
        load(source, target, error, false, false, listener);

        assertEquals(0, listener.getLoaded());
        assertEquals(1, listener.getErrors());
        assertEquals(1, listener.getProcessed());
        assertEquals(1, listener.getMissingAct());
        assertEquals(0, listener.getAlreadyLoaded());

        checkFiles(source);
        checkFiles(target);
        checkFiles(error, file);
    }

    /**
     * Verifies that when a file cannot be moved to the target, the file is moved to the error directory,
     * but the file is still loaded.
     *
     * @throws Exception for any error
     */
    @Test
    public void testFailToMoveToTarget() throws Exception {
        File source = folder.newFolder("sdocs");
        File target = new File(folder.getRoot(), "tdocs"); // don't create the target
        File error = folder.newFolder("edocs");

        DocumentAct act = createPatientDocAct();
        File file = createSourceFile(act, source);

        LoaderListener listener = new LoggingLoaderListener(DocumentLoader.log);
        load(source, target, error, false, false, listener);

        checkFiles(source);
        checkFiles(error, file); // file should now be in the error directory

        assertEquals(0, listener.getLoaded());
        assertEquals(1, listener.getProcessed());
        assertEquals(1, listener.getErrors());

        // reload the act, and verify the document is attached
        act = get(act);
        checkAct(act, file.getName());
    }

    /**
     * Verifies that when a file cannot be moved to the error directory, it remains in the source.
     *
     * @throws Exception for any error
     */
    @Test
    public void testFailToMoveToError() throws Exception {
        File source = folder.newFolder("sdocs");
        File target = folder.newFolder("tdocs");
        File error = new File(folder.getRoot(), "edocs"); // don't create the dir

        // create a file with no corresponding act.
        File file = new File(source, System.nanoTime() + ".gif");
        FileUtils.touch(file);

        LoaderListener listener = new LoggingLoaderListener(DocumentLoader.log);
        load(source, target, error, false, false, listener);

        assertEquals(0, listener.getLoaded());
        assertEquals(1, listener.getErrors());
        assertEquals(1, listener.getProcessed());
        assertEquals(0, listener.getMissingAct()); // this is not called, as the file can't be moved
        assertEquals(0, listener.getAlreadyLoaded());

        checkFiles(source, file);
        checkFiles(target);
    }

    /**
     * Verifies that when renaming is not in use, and there is a duplicate file in the target directory, the new file is
     * moved to the error directory but the file is still loaded.
     *
     * @throws Exception for any error
     */
    @Test
    public void testDuplicateFileInTargetNoRename() throws Exception {
        File source = folder.newFolder("sdocs");
        File target = folder.newFolder("tdocs");
        File error = folder.newFolder("edocs");

        DocumentAct act = createPatientDocAct();
        File file = createSourceFile(act, source);
        File fileWithSameName = createFile(target, file.getName(), "some random content");

        LoaderListener listener = new LoggingLoaderListener(DocumentLoader.log);
        load(source, target, error, false, false, listener);

        checkFiles(source);
        checkFiles(target, fileWithSameName);
        checkFiles(error, file); // file should now be in the error directory

        assertEquals(0, listener.getLoaded());
        assertEquals(1, listener.getProcessed());
        assertEquals(1, listener.getErrors());

        // reload the act, and verify the document is associated
        act = get(act);
        checkAct(act, file.getName());
    }

    /**
     * Verifies that when renaming is in use, and there is a duplicate file in the target directory, the new file is
     * moved and renamed.
     *
     * @throws Exception for any error
     */
    @Test
    public void testDuplicateFileInTargetWithRename() throws Exception {
        File source = folder.newFolder("sdocs");
        File target = folder.newFolder("tdocs");
        File error = folder.newFolder("edocs");

        DocumentAct act = createPatientDocAct();
        File file = createSourceFile(act, source);
        File fileWithSameName = createFile(target, file.getName(), "random content");

        LoaderListener listener = new LoggingLoaderListener(DocumentLoader.log);
        load(source, target, error, false, true, listener); // rename

        checkFiles(source);
        checkFiles(error);

        Set<File> files = getFiles(target);
        assertEquals(2, files.size());
        assertTrue(files.remove(fileWithSameName));
        File renamed = files.iterator().next();
        checkFiles(target, fileWithSameName, renamed);


        assertEquals(1, listener.getLoaded());
        assertEquals(1, listener.getProcessed());
        assertEquals(0, listener.getErrors());

        // reload the act, and verify a document is associated with it
        act = get(act);
        assertNotNull(act.getDocument());
    }

    /**
     * Verifies that when renaming is not in use and there is a duplicate file in the error directory, the new file is
     * not moved.
     *
     * @throws Exception for any error
     */
    @Test
    public void testDuplicateFileInErrorNoRename() throws Exception {
        File source = folder.newFolder("sdocs");
        File target = folder.newFolder("tdocs");
        File error = folder.newFolder("edocs");

        File file = createFile(source, "" + System.nanoTime() + ".gif", "some random content");
        File fileWithSameName = createFile(error, file.getName(), "some more random content");

        LoaderListener listener = new LoggingLoaderListener(DocumentLoader.log);
        load(source, target, error, false, false, listener);

        checkFiles(source, file);
        checkFiles(target);
        checkFiles(error, fileWithSameName);

        assertEquals(0, listener.getLoaded());
        assertEquals(1, listener.getProcessed());
        assertEquals(1, listener.getErrors());
    }

    /**
     * Verifies that when renaming is in use, and there is a duplicate file in the error directory, the new file is
     * moved and renamed.
     *
     * @throws Exception for any error
     */
    @Test
    public void testDuplicateFileInErrorWithRename() throws Exception {
        File source = folder.newFolder("sdocs");
        File target = folder.newFolder("tdocs");
        File error = folder.newFolder("edocs");

        File file = createFile(source, "" + System.nanoTime() + ".gif", "some random content");
        File fileWithSameName = createFile(error, file.getName(), "some more random content");

        LoaderListener listener = new LoggingLoaderListener(DocumentLoader.log);
        load(source, target, error, false, true, listener);

        checkFiles(source);
        checkFiles(target);

        Set<File> files = getFiles(error);
        assertEquals(2, files.size());
        assertTrue(files.remove(fileWithSameName));
        File renamed = files.iterator().next();
        checkFiles(error, fileWithSameName, renamed);

        assertEquals(0, listener.getLoaded());
        assertEquals(1, listener.getProcessed());
        assertEquals(1, listener.getErrors());
    }

    /**
     * Helper to load files.
     *
     * @param source    the source directory to load from
     * @param target    the target directory to move processed files to
     * @param error     the error directory. May be {@code null}
     * @param overwrite if {@code true} overwrite existing documents
     * @param rename    if {@code true}, rename duplicates
     * @param listener  the listener
     */
    protected void load(File source, File target, File error, boolean overwrite, boolean rename,
                        LoaderListener listener) {
        load(source, null, target, error, overwrite, rename, listener);
    }

    /**
     * Helper to load files.
     *
     * @param source    the source directory to load from
     * @param shortName the document archetype(s) that may be loaded to. May be {@code null}, or contain wildcards
     * @param target    the target directory to move processed files to
     * @param error     the error directory. May be {@code null}
     * @param overwrite if {@code true} overwrite existing documents
     * @param rename    if {@code true}, rename duplicates
     * @param listener  the listener
     */
    protected void load(File source, String shortName, File target, File error, boolean overwrite,
                        boolean rename, LoaderListener listener) {
        String[] shortNames = shortName != null ? new String[]{shortName} : null;
        FileStrategy strategy = new FileStrategy(target, error, rename);
        DefaultLoadContext context = new DefaultLoadContext(strategy, listener);
        Loader loader = createLoader(source, shortNames, getArchetypeService(),
                                     new DefaultDocumentFactory(getArchetypeService()),
                                     transactionManager, overwrite, context);
        load(loader, listener);
    }

    /**
     * Creates a file for a document act in the specified directory according to the loader naming conventions,
     * and updates the act if required.
     *
     * @param act the act
     * @param dir the directory
     * @return the new file
     * @throws IOException for any I/O error
     */
    protected abstract File createSourceFile(DocumentAct act, File dir) throws IOException;

    /**
     * Creates a loader.
     *
     * @param source             the source directory to load from
     * @param shortNames         the document archetype(s) that may be loaded to. May be {@code null}
     * @param service            the archetype service
     * @param factory            the document factory
     * @param transactionManager the transaction manager
     * @param overwrite          if {@code true} overwrite existing documents
     * @param context            the load context
     * @return a new loader
     */
    protected abstract Loader createLoader(File source, String[] shortNames, IArchetypeService service,
                                           DocumentFactory factory, PlatformTransactionManager transactionManager,
                                           boolean overwrite, LoadContext context);

    /**
     * Verifies the document versions associated with the version node of document act.
     *
     * @param act      the document act
     * @param versions the expected document versions
     */
    protected void checkVersions(DocumentAct act, Document... versions) {
        act = get(act);
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

    /**
     * Loads documents.
     *
     * @param loader the loader
     */
    protected void load(Loader loader, LoaderListener listener) {
        DocumentLoader docLoader = new DocumentLoader(loader, listener);
        docLoader.setFailOnError(false);
        docLoader.load();
    }

}
