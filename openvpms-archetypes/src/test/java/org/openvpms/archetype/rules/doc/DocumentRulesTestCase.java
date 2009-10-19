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
package org.openvpms.archetype.rules.doc;

import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.util.List;
import java.util.HashSet;
import java.util.Set;


/**
 * Tests the {@link DocumentRules} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentRulesTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link DocumentRules#addDocument} method.
     */
    public void testAddDocument() {
        // create an act.patientClinicalEvent and act.patientDocumentImage and add a relationship between them
        Party patient = TestHelper.createPatient();
        Act event = (Act) create("act.patientClinicalEvent");
        ActBean eventBean = new ActBean(event);
        eventBean.addParticipation("participation.patient", patient);
        eventBean.save();

        DocumentAct act = (DocumentAct) create("act.patientDocumentImage");
        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.patient", patient);

        eventBean.addNodeRelationship("items", act);
        save(act, event);

        // now add a document.
        DocumentRules rules = new DocumentRules();
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
        Set<IMObjectReference> docs = new HashSet<IMObjectReference>();
        for (DocumentAct version :acts) {
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
    public void testCreatePatientDocumentVersion() {
        checkCreatePatientVersion("act.patientDocumentAttachment", "act.patientDocumentAttachmentVersion");
        checkCreatePatientVersion("act.patientDocumentImage", "act.patientDocumentImageVersion");
        checkCreatePatientVersion("act.patientDocumentLetter", "act.patientDocumentLetterVersion");
        checkCreatePatientVersion("act.patientInvestigation", "act.patientInvestigationVersion");
    }

    /**
     * Verifies that the appropriate version acts are created for <em>act.customerDocumentAttachment</em> and
     * <em>act.customerDocumentLetter</em>
     */
    public void testCreateCustomerDocumentVersion() {
        checkCreateCustomerSupplierVersion("act.customerDocumentAttachment", "act.customerDocumentAttachmentVersion");
        checkCreateCustomerSupplierVersion("act.customerDocumentLetter", "act.customerDocumentLetterVersion");
    }

    /**
     * Verifies that the appropriate version acts are created for <em>act.customerDocumentAttachment</em> and
     * <em>act.customerDocumentLetter</em>
     */
    public void testCreateSupplierDocumentVersion() {
        checkCreateCustomerSupplierVersion("act.supplierDocumentAttachment", "act.supplierDocumentAttachmentVersion");
        checkCreateCustomerSupplierVersion("act.supplierDocumentLetter", "act.supplierDocumentLetterVersion");
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
        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("clinician", clinician);
        bean.addNodeParticipation("author", author);

        DocumentAct version = checkCreateVersion(act, expectedVersion);
        ActBean versionBean = new ActBean(version);
        assertEquals(clinician, versionBean.getNodeParticipant("clinician"));
        assertEquals(author, versionBean.getNodeParticipant("author"));
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
     * @param act the act
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
        DocumentRules rules = new DocumentRules();
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
        Document document = (Document) create("document.other");
        document.setName("test" + System.currentTimeMillis() + ".gif");
        document.setMimeType("image/gif");
        return document;
    }

}
