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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.etl.tools.doc;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentHelper;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.springframework.transaction.PlatformTransactionManager;

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
public class NameLoaderTestCase extends AbstractBasicLoaderTest {

    /**
     * Tests the {@link NameLoader}.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testLoad() throws IOException {
        File source = folder.newFolder("sdocs");
        File target = folder.newFolder("tdocs");

        DocumentAct act1 = createPatientDocAct("file1-" + System.nanoTime() + ".gif");
        DocumentAct act2 = createPatientDocAct("file2-" + System.nanoTime() + ".pdf");
        DocumentAct act3 = createPatientDocAct("file3-" + System.nanoTime() + ".html");
        DocumentAct act4 = createPatientDocAct("file4-" + System.nanoTime() + ".png");
        DocumentAct act5 = createPatientDocAct("file5-" + System.nanoTime() + ".htm");
        DocumentAct act6 = createPatientDocAct("file6-" + System.nanoTime() + ".txt");
        DocumentAct act7 = createPatientDocAct("file7-" + System.nanoTime() + ".doc");
        DocumentAct act8 = createPatientDocAct("file8-" + System.nanoTime() + ".odt");
        List<DocumentAct> expected = Arrays.asList(act1, act2, act3, act4, act5, act6, act7, act8);

        File act1File = createFile(source, act1.getFileName(), null);
        File act2File = createFile(source, act2.getFileName(), null);
        File act3File = createFile(source, act3.getFileName(), null);
        File act4File = createFile(source, act4.getFileName(), null);
        File act5File = createFile(source, act5.getFileName(), null);
        File act6File = createFile(source, act6.getFileName(), null);
        File act7File = createFile(source, act7.getFileName(), null);
        File act8File = createFile(source, act8.getFileName(), null);

        LoaderListener listener = load(source, target, false);

        // verify there a no acts left to load documents for.
        ArchetypeQuery query = createQuery();
        Iterator<DocumentAct> iter = new IMObjectQueryIterator<>(query);
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
            e = get(e);
            assertNotNull(e);
            checkMimeType(e);
        }
    }

    /**
     * Verifies behaviour when an act is missing.
     *
     * @throws Exception for any error
     */
    @Test
    public void testMissingAct() throws Exception {
        File source = folder.newFolder("sdocs");
        File target = folder.newFolder("tdocs");

        // create a file with no corresponding act.
        File file = new File(source, System.nanoTime() + ".gif");
        FileUtils.touch(file);

        LoaderListener listener = load(source, target, false);

        assertEquals(0, listener.getLoaded());
        assertEquals(1, listener.getErrors());
        assertEquals(1, listener.getProcessed());
        assertEquals(1, listener.getMissingAct());
        assertEquals(0, listener.getAlreadyLoaded());

        checkFiles(source, file);
        checkFiles(target);
    }

    /**
     * Verifies that when overwrite is {@code false}, files are skipped if they are already loaded.
     * Note that for the {@link NameLoader}, such documents are flagged as having missing acts, to avoid a full table
     * scan.
     * <p/>
     * This is because there is no index on the fileName column.
     *
     * @throws Exception for any error
     */
    @Test
    public void testSkipProcessed() throws Exception {
        File source = folder.newFolder("sdocs");
        File target = folder.newFolder("tdocs");

        // create a file and associate it with the act. The loader will skip it
        DocumentAct preLoaded = createPatientDocAct();
        File preloadedFile = createFile(preLoaded, source, null);
        Document doc = DocumentHelper.create(preloadedFile, "image/gif", new DocumentHandlers());
        preLoaded.setDocument(doc.getObjectReference());
        preLoaded.setFileName(doc.getName());
        save(doc, preLoaded);

        LoaderListener listener = load(source, target, false);
        assertEquals(0, listener.getLoaded());
        assertEquals(1, listener.getErrors());
        assertEquals(1, listener.getProcessed());
        assertEquals(1, listener.getMissingAct());
        assertEquals(0, listener.getAlreadyLoaded());

        checkFiles(source, preloadedFile);
        checkFiles(target);
    }

    /**
     * Verifies that document templates cannot be loaded to.
     * <p/>
     * Note that the documents are flagged as having missing acts, to avoid a full table scan.
     * <p/>
     * This is because there is no index on the fileName column.
     */
    @Test
    public void testLoadDocumentTemplate() throws Exception {
        File source = folder.newFolder("sdocs");
        File target = folder.newFolder("tdocs");

        // verify that the loader cannot be constructed to load act.documentTemplate acts
        try {
            new NameLoader(source, new String[]{DocumentArchetypes.DOCUMENT_TEMPLATE_ACT}, getArchetypeService(),
                           new DefaultDocumentFactory(), transactionManager, false, false);
            fail("Expected exception to be thrown");
        } catch (Throwable exception) {
            assertEquals(exception.getMessage(), "Argument 'shortNames' doesn't refer to any valid archetype for "
                                                 + "loading documents to: {act.documentTemplate}");
        }

        // verify that an act.documentTemplate cannot be overwritten by a load
        DocumentAct act = (DocumentAct) create(DocumentArchetypes.DOCUMENT_TEMPLATE_ACT);
        File file = createFile(act, source, null, "-Z", "A");

        Entity template = (Entity) create(DocumentArchetypes.DOCUMENT_TEMPLATE);
        act.setName("foo");
        act.setDescription("bar");
        template.setName("ZTemplate");
        ActBean bean = new ActBean(act);
        bean.setValue("fileName", file.getName());
        bean.addNodeParticipation("template", template);
        save(act, template);

        LoaderListener listener = load(source, target, true);
        assertEquals(0, listener.getLoaded());
        assertEquals(0, listener.getAlreadyLoaded());
        assertEquals(1, listener.getMissingAct());
        assertEquals(1, listener.getErrors());
    }

    /**
     * Verifies that duplicate documents are only loaded if they have a different file name to the original
     * content.
     *
     * @throws Exception for any error
     */
    @Test
    public void testDuplicates() throws Exception {
        File source = folder.newFolder("sdocs");
        File target = folder.newFolder("tdocs");

        File first = createFile(source, System.nanoTime() + ".gif", "A");
        DocumentAct act = createPatientDocAct(first.getName());

        // create a file and load it
        LoaderListener listener1 = load(source, target, true);
        assertEquals(1, listener1.getLoaded());
        assertEquals(0, listener1.getErrors());
        assertEquals(1, listener1.getProcessed());
        assertEquals(0, listener1.getMissingAct());
        assertEquals(0, listener1.getAlreadyLoaded());

        // delete from the target so another can be loaded with the same name
        assertTrue(new File(target, first.getName()).delete());

        // verify the act has a document, and it is the first instance
        Document firstDoc = checkAct(act, first.getName());

        // create a file that duplicates the first in name and content
        createFile(source, first.getName(), "A");
        LoaderListener listenerDup = load(source, target, true);
        assertEquals(0, listenerDup.getLoaded());
        assertEquals(1, listenerDup.getErrors());
        assertEquals(1, listenerDup.getProcessed());
        assertEquals(0, listenerDup.getMissingAct());
        assertEquals(1, listenerDup.getAlreadyLoaded());

        File second = createFile(source, first.getName(), "B");
        LoaderListener listener2 = load(source, target, true);
        assertEquals(1, listener2.getLoaded());
        assertEquals(0, listener2.getErrors());
        assertEquals(1, listener2.getProcessed());
        assertEquals(0, listener2.getMissingAct());
        assertEquals(0, listener2.getAlreadyLoaded());

        // verify the act has a document, and it is the second instance
        Document secondDoc = checkAct(act, second.getName());
        checkVersions(act, firstDoc);

        // create a third file that duplicates the content of the first. This won't be loaded
        // as it has the same name.
        File third = createFile(source, first.getName(), "A");
        assertTrue(third.setLastModified(second.lastModified() + 1000));
        LoaderListener listener3 = load(source, target, true);
        assertEquals(0, listener3.getLoaded());
        assertEquals(1, listener3.getErrors());
        assertEquals(1, listener3.getProcessed());
        assertEquals(0, listener3.getMissingAct());
        assertEquals(1, listener3.getAlreadyLoaded());

        act = get(act);
        checkAct(act, second.getName());
        assertEquals(act.getDocument(), secondDoc.getObjectReference());

        // there should a single version and reference the first document,
        checkVersions(act, firstDoc);
    }

    /**
     * Creates a loader.
     *
     * @param source             the source directory to load from
     * @param shortNames         the document archetype(s) that may be loaded to. May be {@code null}
     * @param service            the archetype service
     * @param factory            the document factory
     * @param transactionManager the transaction manager
     * @param overwrite          if {@code true} overwrite existing documents
     * @return a new loader
     */
    @Override
    protected Loader createLoader(File source, String[] shortNames, IArchetypeService service, DocumentFactory factory,
                                  PlatformTransactionManager transactionManager, boolean overwrite) {
        return new NameLoader(source, shortNames, service, factory, transactionManager, false, overwrite);
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
