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

import org.apache.commons.io.FileUtils;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentHelper;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Tests the {@link DocumentLoader} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentLoaderTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service.
     */
    private IArchetypeService service;


    /**
     * Tests the {@link DocumentLoader} using an {@link NameLoader}.
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
        query.add(new NodeConstraint("docReference", RelationalOp.IsNULL));
        List<DocumentAct> expected = new ArrayList<DocumentAct>();
        Iterator<DocumentAct> iter = new IMObjectQueryIterator<DocumentAct>(
                query);
        while (iter.hasNext()) {
            DocumentAct act = iter.next();
            ActBean bean = new ActBean(act);
            if (bean.getString("fileName") != null) {
                expected.add(act);
            }
        }
        assertTrue(expected.size() >= count);

        NameLoader nameLoader = new NameLoader(new File("./"),
                                               shortName, service,
                                               new TestFactory());
        LoggingLoaderListener listener
                = new LoggingLoaderListener(DocumentLoader.log);
        nameLoader.setListener(listener);
        DocumentLoader loader = new DocumentLoader(nameLoader);
        loader.setFailOnError(false);
        loader.load();

        // verify there a no acts left to load documents for.
        iter = new IMObjectQueryIterator<DocumentAct>(query);
        assertFalse(iter.hasNext());
        assertEquals(expected.size(), listener.getLoaded());
        assertEquals(expected.size(), listener.getProcessed());
        assertEquals(0, listener.getErrors());
    }

    /**
     * Tests the {@link DocumentLoader} using an {@link IdLoader}.
     */
    @SuppressWarnings("unchecked")
    public void testIdLoader() throws Exception {
        final int count = 10;
        final String patientAct = "act.patientDocumentAttachment";
        final String customerAct = "act.customerDocumentAttachment";
        File source = new File("target/sdocs" + System.currentTimeMillis());
        File target = new File("target/tdocs" + System.currentTimeMillis());
        assertTrue(source.mkdir());
        assertTrue(target.mkdir());

        DocumentAct[] acts = new DocumentAct[count];
        for (int i = 0; i < count; ++i) {
            acts[i] = createPatientDocAct(patientAct);
        }

        Set<String> expectedNames = new HashSet<String>();
        for (DocumentAct act : acts) {
            File file = createFile(act, source, null);
            expectedNames.add(file.getName());
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

        IdLoader idLoader = new IdLoader(source, service,
                                         new DefaultDocumentFactory(), true);
        LoaderListener listener = new LoggingLoaderListener(DocumentLoader.log,
                                                            target);
        idLoader.setListener(listener);
        DocumentLoader loader = new DocumentLoader(idLoader);
        loader.setFailOnError(false);
        loader.load();

        int expectedErrors = 5;
        assertEquals(count, listener.getLoaded());
        assertEquals(expectedErrors, listener.getErrors());
        assertEquals(count + expectedErrors, listener.getProcessed());
        assertEquals(4, listener.getMissingAct());
        assertEquals(1, listener.getAlreadyLoaded());

        Set<File> files = new HashSet<File>(
                FileUtils.listFiles(source, null, true));
        assertEquals(expectedErrors, files.size());
        assertTrue(files.contains(noActFile));
        assertTrue(files.contains(invalidF1));
        assertTrue(files.contains(invalidF2));
        assertTrue(files.contains(invalidF3));

        files = new HashSet<File>(FileUtils.listFiles(target, null, true));
        assertEquals(count, files.size());
        for (File file : files) {
            assertTrue(expectedNames.contains(file.getName()));
        }

        // verify the acts have associated documents
        for (DocumentAct act : acts) {
            act = (DocumentAct) service.get(act.getObjectReference());
            assertNotNull(act);
            assertNotNull(act.getDocument());
            assertNotNull(service.get(act.getDocument()));
        }
    }

    private File createFile(DocumentAct act, File source, String prefix)
            throws IOException {
        StringBuffer buff = new StringBuffer();
        if (prefix != null) {
            buff.append(prefix);
        }
        buff.append(act.getId());
        buff.append(".gif");
        File file = new File(source, buff.toString());
        FileUtils.touch(file);
        return file;
    }

    /**
     * Returns the location of the spring config files.
     *
     * @return an array of config locations
     */
    protected String[] getConfigLocations() {
        return new String[]{"applicationContext.xml"};
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        service = (IArchetypeService) applicationContext.getBean(
                "archetypeService");
    }

    /**
     * Creates a new document act.
     *
     * @param shortName the document act short name
     * @return a new document act
     */
    private DocumentAct createPatientDocAct(String shortName) {
        return createPatientDocAct(shortName, null);
    }

    /**
     * Creates a new patient document act.
     *
     * @param shortName the document act short name
     * @param fileName  the file name. May be <tt>null</tt>
     * @return a new document act
     */
    private DocumentAct createPatientDocAct(String shortName, String fileName) {
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
     * Creates a new customer document act.
     *
     * @param shortName the document act short name
     */
    private DocumentAct createCustomerDocAct(String shortName) {
        Party customer = (Party) service.create("party.customerperson");
        IMObjectBean bean = new IMObjectBean(customer);
        bean.setValue("firstName", "Foo");
        bean.setValue("lastName", "ZBar");
        bean.save();
        DocumentAct act = (DocumentAct) service.create(shortName);
        assertNotNull(act);
        ActBean actBean = new ActBean(act);
        actBean.addParticipation("participation.customer", customer);
        actBean.save();
        return act;

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
