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
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Base class for {@link Loader} test cases.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
@ContextConfiguration("/applicationContext.xml")
public class AbstractLoaderTest extends AbstractJUnit4SpringContextTests {

    /**
     * The archetype service.
     */
    @Autowired
    protected IArchetypeService service;

    /**
     * The transaction manager.
     */
    @Autowired
    protected PlatformTransactionManager transactionManager;


    /**
     * Returns the file in a directory.
     *
     * @param dir the directory
     * @return the files in the directory
     */
    @SuppressWarnings("unchecked")
    protected Set<File> getFiles(File dir) {
        return new HashSet<File>(FileUtils.listFiles(dir, null, true));
    }

    /**
     * Creates a dummy <em>.gif</em> file for a document act.
     *
     * @param act the act
     * @param dir the parent directory
     * @return a new file
     * @throws java.io.IOException for any I/O error
     */
    protected File createFile(DocumentAct act, File dir) throws IOException {
        return createFile(act, dir, null);
    }

    /**
     * Creates a dummy <em>.gif</em> file for a document act.
     *
     * @param act    the act
     * @param dir    the parent directory
     * @param prefix the file name prefix. May be <tt>null</tt>
     * @return a new file
     * @throws java.io.IOException for any I/O error
     */
    protected File createFile(DocumentAct act, File dir, String prefix) throws IOException {
        return createFile(act, dir, prefix, null);
    }

    /**
     * Creates a dummy <em>.gif</em> file for a document act.
     *
     * @param act    the act
     * @param dir    the parent directory
     * @param prefix the file name prefix. May be <tt>null</tt>
     * @param suffix the file name suffix (pre extension). May be <tt>null</tt>
     * @return a new file
     * @throws java.io.IOException for any I/O error
     */
    protected File createFile(DocumentAct act, File dir, String prefix, String suffix) throws IOException {
        return createFile(act, dir, prefix, suffix, null);
    }

    /**
     * Creates a dummy <em>.gif</em> file for a document act.
     *
     * @param act     the act
     * @param dir     the parent directory
     * @param prefix  the file name prefix. May be <tt>null</tt>
     * @param suffix  the file name suffix (pre extension). May be <tt>null</tt>
     * @param content the file content. May be <tt>null</tt>
     * @return a new file
     * @throws java.io.IOException for any I/O error
     */
    protected File createFile(DocumentAct act, File dir, String prefix, String suffix, String content)
            throws IOException {
        return createFile(act, dir, prefix, suffix, content, ".gif");
    }

    /**
     * Creates a dummy file for a document act.
     *
     * @param act       the act
     * @param dir       the parent directory
     * @param prefix    the file name prefix. May be <tt>null</tt>
     * @param suffix    the file name suffix (pre extension). May be <tt>null</tt>
     * @param content   the file content. May be <tt>null</tt>
     * @param extension the file name extension
     * @return a new file
     * @throws java.io.IOException for any I/O error
     */
    protected File createFile(DocumentAct act, File dir, String prefix, String suffix, String content, String extension)
            throws IOException {
        StringBuilder buff = new StringBuilder();
        if (prefix != null) {
            buff.append(prefix);
        }
        buff.append(act.getId());
        if (suffix != null) {
            buff.append(suffix);
        }
        buff.append(extension);
        return createFile(dir, buff.toString(), content);
    }

    /**
     * Creates a test file.
     *
     * @param dir     the parent directory
     * @param name    the file name
     * @param content the file content. May be <tt>null</tt>
     * @return a new file
     * @throws IOException for any I/O error
     */
    protected File createFile(File dir, String name, String content) throws IOException {
        File file = new File(dir, name);
        if (content != null) {
            PrintStream stream = new PrintStream(new FileOutputStream(file));
            stream.print(content);
            stream.close();
        } else {
            FileUtils.touch(file);
        }
        return file;
    }

    /**
     * Creates a new <em>act.patientDocumentAttachment</em>.
     *
     * @return a new <em>act.patientDocumentAttachment</em>
     */
    protected DocumentAct createPatientDocAct() {
        return createPatientDocAct(null);
    }

    /**
     * Creates a new <em>act.patientDocumentAttachment</em>.
     *
     * @param fileName the file name. May be <tt>null</tt>
     * @return a new <em>act.patientDocumentAttachment</em>
     */
    protected DocumentAct createPatientDocAct(String fileName) {
        return createPatientDocAct("act.patientDocumentAttachment", fileName);
    }

    /**
     * Creates a new document act.
     *
     * @param shortName the archetype short name
     * @param fileName  the file name. May be <tt>null</tt>
     * @return a new <em>act.patientDocumentAttachment</em>
     */
    protected DocumentAct createPatientDocAct(String shortName, String fileName) {
        Party patient = (Party) service.create("party.patientpet");
        patient.setName("ZTestPet-" + System.currentTimeMillis());
        IMObjectBean bean = new IMObjectBean(patient);
        bean.setValue("species", "CANINE");
        bean.save();
        DocumentAct act = (DocumentAct) service.create(shortName);
        assertNotNull(act);
        act.setFileName(fileName);
        ActBean actBean = new ActBean(act);
        actBean.addParticipation("participation.patient", patient);
        actBean.save();
        return act;
    }

    /**
     * Loads documents.
     *
     * @param loader   the loader
     * @param listener the load listener to notify
     */
    protected void load(Loader loader, LoaderListener listener) {
        loader.setListener(listener);
        DocumentLoader docLoader = new DocumentLoader(loader);
        docLoader.setFailOnError(false);
        docLoader.load();
    }

    /**
     * Verify an act exists and has a document.
     *
     * @param act the act to check
     */
    protected void checkAct(DocumentAct act) {
        checkAct(act, act.getFileName());
    }

    /**
     * Verify an act exists and has a document, expected file name and mime type.
     *
     * @param act  the act to check
     * @param name the expected file name
     * @return the document
     */
    protected Document checkAct(DocumentAct act, String name) {
        act = (DocumentAct) service.get(act.getObjectReference());
        assertNotNull(act);
        assertNotNull(act.getDocument());
        Document doc = (Document) service.get(act.getDocument());
        assertNotNull(doc);
        assertEquals(name, act.getFileName());
        assertEquals(name, doc.getName());
        checkMimeType(act);
        return doc;
    }

    /**
     * Verifies a document act has no document.
     *
     * @param act the act to check
     */
    protected void checkNoDocument(DocumentAct act) {
        act = (DocumentAct) service.get(act.getObjectReference());
        assertNotNull(act);
        assertNull(act.getDocument());
    }

    /**
     * Verifies that the mime type matches that expected.
     *
     * @param act the act to check
     */
    protected void checkMimeType(DocumentAct act) {
        checkMimeType(act.getFileName(), act.getMimeType());
        IMObjectReference docRef = act.getDocument();
        if (docRef != null) {
            Document doc = (Document) service.get(docRef);
            assertNotNull(doc);
            checkMimeType(doc.getName(), doc.getMimeType());
        }
    }

    /**
     * Verifies that a document file name corresponds to the mime type.
     *
     * @param fileName the file name
     * @param mimeType the mime type
     */
    protected void checkMimeType(String fileName, String mimeType) {
        if (fileName.endsWith(".gif")) {
            assertEquals("image/gif", mimeType);
        } else if (fileName.endsWith(".png")) {
            assertEquals("image/png", mimeType);
        } else if (fileName.endsWith(".pdf")) {
            assertEquals("application/pdf", mimeType);
        } else if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
            assertEquals("text/html", mimeType);
        } else if (fileName.endsWith(".txt")) {
            assertEquals("text/plain", mimeType);
        } else if (fileName.endsWith(".doc")) {
            assertEquals("application/msword", mimeType);
        } else if (fileName.endsWith(".odt")) {
            assertEquals("application/vnd.oasis.opendocument.text", mimeType);
        } else {
            fail("Cannot determine if valid mime type for fileName: " + fileName);
        }
    }

    /**
     * Verifies that a directory contains the expected files.
     *
     * @param dir      the directory
     * @param expected the expected files
     */
    protected void checkFiles(File dir, File... expected) {
        String[] names = new String[expected.length];
        for (int i = 0; i < expected.length; ++i) {
            names[i] = expected[i].getName();
        }
        Set<File> files = getFiles(dir);
        assertEquals(names.length, files.size());
        List<String> list = Arrays.asList(names);
        for (File file : files) {
            assertTrue(list.contains(file.getName()));
        }
    }

}
