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

package org.openvpms.archetype.rules.doc;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.CRC32;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link DocumentRules} class.
 *
 * @author Tim Anderson
 */
public class DocumentRulesTestCase extends ArchetypeServiceTest {

    /**
     * The document rules.
     */
    private DocumentRules rules;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        rules = new DocumentRules(getArchetypeService());
    }

    /**
     * Tests the {@link DocumentRules#supportsVersions} method.
     */
    @Test
    public void testSupportsVersions() {
        DocumentAct image = (DocumentAct) create(PatientArchetypes.DOCUMENT_IMAGE);
        assertTrue(rules.supportsVersions(image));

        DocumentAct form = (DocumentAct) create(PatientArchetypes.DOCUMENT_FORM);
        assertFalse(rules.supportsVersions(form));
    }

    /**
     * Tests the {@link DocumentRules#addDocument} method.
     */
    @Test
    public void testAddDocument() {
        // create an act.patientClinicalEvent and act.patientDocumentImage and add a relationship between them
        Party patient = TestHelper.createPatient();
        Act event = (Act) create(PatientArchetypes.CLINICAL_EVENT);
        ActBean eventBean = new ActBean(event);
        eventBean.addNodeParticipation("patient", patient);
        eventBean.save();

        DocumentAct act = (DocumentAct) create(PatientArchetypes.DOCUMENT_IMAGE);
        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("patient", patient);

        eventBean.addNodeRelationship("items", act);
        save(act, event);

        // now add a document.
        Document document1 = createDocument();
        List<IMObject> objects = rules.addDocument(act, document1);
        save(objects);

        assertEquals(document1.getObjectReference(), act.getDocument());

        Document document2 = createDocument();
        objects = rules.addDocument(act, document2);
        save(objects);

        assertEquals(document2.getObjectReference(), act.getDocument());

        List<DocumentAct> acts = bean.getNodeActs("versions", DocumentAct.class);
        assertEquals(1, acts.size());
        DocumentAct old = acts.get(0);
        assertEquals(document1.getObjectReference(), old.getDocument());

        // add another document.
        Document document3 = createDocument();
        objects = rules.addDocument(act, document3);
        save(objects);

        // verify the document3 is the latest
        assertEquals(document3.getObjectReference(), act.getDocument());

        // verify document1 and document2 are versioned
        acts = bean.getNodeActs("versions", DocumentAct.class);
        assertEquals(2, acts.size());
        Set<IMObjectReference> docs = new HashSet<>();
        for (DocumentAct version : acts) {
            assertEquals(1, version.getActRelationships().size()); // only one relationship, back to parent
            docs.add(version.getDocument());
        }
        assertTrue(docs.contains(document1.getObjectReference()));
        assertTrue(docs.contains(document2.getObjectReference()));
    }

    /**
     * Verifies that the appropriate version acts are created for <em>act.patientDocumentAttachment</em>,
     * <em>act.patientDocumentImage</em>, <em>act.patientDocumentLetter</em> and <em>act.patientInvestigation</em>
     */
    @Test
    public void testCreatePatientDocumentVersion() {
        checkCreatePatientVersion(PatientArchetypes.DOCUMENT_ATTACHMENT, PatientArchetypes.DOCUMENT_ATTACHMENT_VERSION);
        checkCreatePatientVersion(PatientArchetypes.DOCUMENT_IMAGE, PatientArchetypes.DOCUMENT_IMAGE_VERSION);
        checkCreatePatientVersion(PatientArchetypes.DOCUMENT_LETTER, PatientArchetypes.DOCUMENT_LETTER_VERSION);
        checkCreatePatientVersion(InvestigationArchetypes.PATIENT_INVESTIGATION,
                                  InvestigationArchetypes.PATIENT_INVESTIGATION_VERSION);
    }

    /**
     * Verifies that the appropriate version acts are created for <em>act.customerDocumentAttachment</em> and
     * <em>act.customerDocumentLetter</em>
     */
    @Test
    public void testCreateCustomerDocumentVersion() {
        checkCreateCustomerSupplierVersion(CustomerArchetypes.DOCUMENT_ATTACHMENT,
                                           CustomerArchetypes.DOCUMENT_ATTACHMENT_VERSION);
        checkCreateCustomerSupplierVersion(CustomerArchetypes.DOCUMENT_LETTER,
                                           CustomerArchetypes.DOCUMENT_LETTER_VERSION);
    }

    /**
     * Verifies that the appropriate version acts are created for <em>act.customerDocumentAttachment</em> and
     * <em>act.customerDocumentLetter</em>
     */
    @Test
    public void testCreateSupplierDocumentVersion() {
        checkCreateCustomerSupplierVersion(SupplierArchetypes.DOCUMENT_ATTACHMENT,
                                           SupplierArchetypes.DOCUMENT_ATTACHMENT_VERSION);
        checkCreateCustomerSupplierVersion(SupplierArchetypes.DOCUMENT_LETTER,
                                           SupplierArchetypes.DOCUMENT_LETTER_VERSION);
    }

    /**
     * Tests the {@link DocumentRules#isDuplicate} method.
     */
    @Test
    public void testIsDuplicate() {
        // create an act.patientDocumentImage and link a patient
        Party patient = TestHelper.createPatient();
        DocumentAct act = (DocumentAct) create(PatientArchetypes.DOCUMENT_IMAGE);
        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("patient", patient);

        // now add a document.
        Document document1 = createDocument();
        assertFalse(document1.getDocSize() == 0);
        assertFalse(document1.getChecksum() == 0);

        List<IMObject> objects = rules.addDocument(act, document1);
        save(objects);

        // verify that for the same document, isDuplicate returns true
        assertTrue(rules.isDuplicate(act, document1));

        // now change the checksum to 0 and verify that isDuplicate returns false
        document1.setChecksum(0);
        save(document1);
        assertFalse(rules.isDuplicate(act, document1));

        // verify that for a different document with different content, isDuplicate returns false
        Document document2 = createDocument();
        assertFalse(document2.getDocSize() == 0);
        assertFalse(document2.getChecksum() == 0);
        assertFalse(rules.isDuplicate(act, document2));

        // verify that for a different document with same checksum and length, isDuplicate returns tue
        document1.setChecksum(1);
        save(document1);
        document2.setDocSize(document1.getDocSize());
        document2.setChecksum(document1.getChecksum());
        assertTrue(rules.isDuplicate(act, document2));
    }

    /**
     * Verifies that versioning works for a patient document act.
     *
     * @param actShortName    the act archetype short name
     * @param expectedVersion the act version archetype short name
     */
    private void checkCreatePatientVersion(String actShortName, String expectedVersion) {
        DocumentAct act = (DocumentAct) create(actShortName);
        User clinician = TestHelper.createClinician();
        User author = TestHelper.createClinician();
        Product product = null;
        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("clinician", clinician);
        bean.addNodeParticipation("author", author);
        if (bean.hasNode("product")) {
            product = TestHelper.createProduct();
            bean.addNodeParticipation("product", product);
        }

        DocumentAct version = checkCreateVersion(act, expectedVersion);
        ActBean versionBean = new ActBean(version);
        assertEquals(clinician, versionBean.getNodeParticipant("clinician"));
        assertEquals(author, versionBean.getNodeParticipant("author"));
        if (versionBean.hasNode("product")) {
            assertEquals(product, versionBean.getNodeParticipant("product"));
        }
    }

    /**
     * Verifies that versioning works for a customer or supplier document act.
     *
     * @param actShortName    the act archetype short name
     * @param expectedVersion the act version archetype short name
     */
    private void checkCreateCustomerSupplierVersion(String actShortName, String expectedVersion) {
        DocumentAct act = (DocumentAct) create(actShortName);
        User author = TestHelper.createClinician();
        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("author", author);

        DocumentAct version = checkCreateVersion(act, expectedVersion);
        ActBean versionBean = new ActBean(version);
        assertEquals(author, versionBean.getNodeParticipant("author"));
    }

    /**
     * Verifies that versioning works for an act.
     *
     * @param act             the act
     * @param expectedVersion the act version archetype short name
     * @return the version
     */
    private DocumentAct checkCreateVersion(DocumentAct act, String expectedVersion) {
        act.setPrinted(true);

        ActBean bean = new ActBean(act);
        assertTrue(bean.hasNode("document")); // make sure act has document node

        Document document = createDocument();
        save(document);
        act.setDocument(document.getObjectReference());
        assertNotNull(act);
        DocumentAct version = rules.createVersion(act);
        assertNotNull(version);
        assertEquals(expectedVersion, version.getArchetypeId().getShortName());
        assertEquals(document.getObjectReference(), version.getDocument());
        assertEquals(act.getMimeType(), version.getMimeType());
        assertEquals(act.getFileName(), version.getFileName());
        assertEquals(act.isPrinted(), version.isPrinted());

        ActBean versionBean = new ActBean(version);
        assertTrue(versionBean.hasNode("document"));
        return version;
    }

    /**
     * Helper to create a document.
     *
     * @return a new document
     */
    private Document createDocument() {
        Document document = (Document) create(DocumentArchetypes.DEFAULT_DOCUMENT);
        document.setName("test" + System.currentTimeMillis() + ".gif");
        document.setMimeType("image/gif");
        document.setContents(document.getName().getBytes());
        document.setDocSize(document.getContents().length);
        CRC32 checksum = new CRC32();
        checksum.update(document.getContents());
        document.setChecksum(checksum.getValue());
        return document;
    }

}
