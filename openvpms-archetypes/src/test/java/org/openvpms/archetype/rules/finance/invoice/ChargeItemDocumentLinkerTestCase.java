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

package org.openvpms.archetype.rules.finance.invoice;

import org.apache.commons.lang.ObjectUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientHistoryChanges;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link ChargeItemDocumentLinker} class.
 *
 * @author Tim Anderson
 */
public class ChargeItemDocumentLinkerTestCase extends ArchetypeServiceTest {

    /**
     * Tests linking of charge item documents to a charge item.
     */
    @Test
    public void testSimpleLink() {
        Party patient = TestHelper.createPatient();
        Entity template1 = createDocumentTemplate(PatientArchetypes.DOCUMENT_FORM);
        Entity template2 = createDocumentTemplate(PatientArchetypes.DOCUMENT_LETTER);
        Product product1 = createProduct(template1);
        User author = TestHelper.createUser();
        User clinician = TestHelper.createClinician();
        Party location = TestHelper.createLocation();
        PatientHistoryChanges changes = new PatientHistoryChanges(author, location, getArchetypeService());
        Product product2 = createProduct(template2);
        Product product3 = createProduct(template1, template2);
        Product product4 = createProduct();
        FinancialAct item = createItem(patient, product1, author, clinician);
        save(item);
        ChargeItemDocumentLinker linker = new ChargeItemDocumentLinker(item, getArchetypeService());
        linker.link();

        ActBean bean = new ActBean(item);
        List<Act> documents = bean.getNodeActs("documents");
        assertEquals(1, documents.size());
        Act document1 = documents.get(0);
        assertTrue(TypeHelper.isA(document1, PatientArchetypes.DOCUMENT_FORM));
        checkDocument(document1, patient, product1, template1, author, clinician, item);

        bean.setParticipant(ProductArchetypes.PRODUCT_PARTICIPATION, product2);
        linker.link();
        documents = bean.getNodeActs("documents");
        assertEquals(1, documents.size());
        Act document2 = documents.get(0);
        assertTrue(TypeHelper.isA(document2, PatientArchetypes.DOCUMENT_LETTER));
        checkDocument(document2, patient, product2, template2, author, clinician, item);

        // now test a product with 2 documents
        bean.setParticipant(ProductArchetypes.PRODUCT_PARTICIPATION, product3);
        linker.prepare(changes);
        saveInTxn(changes);
        documents = bean.getNodeActs("documents");
        assertEquals(2, documents.size());
        Act doc3template1 = getDocument(documents, template1);
        Act doc3template2 = getDocument(documents, template2);
        assertTrue(TypeHelper.isA(doc3template1, PatientArchetypes.DOCUMENT_FORM));
        assertTrue(TypeHelper.isA(doc3template2, PatientArchetypes.DOCUMENT_LETTER));
        checkDocument(doc3template1, patient, product3, template1, author, clinician, item);
        checkDocument(doc3template2, patient, product3, template2, author, clinician, item);

        // now test a product with no documents
        bean.setParticipant(ProductArchetypes.PRODUCT_PARTICIPATION, product4);
        linker.link();
        documents = bean.getNodeActs("documents");
        assertEquals(0, documents.size());
    }

    /**
     * Verifies that the document acts are recreated if the charge item product, patient, author, or clinician changes.
     */
    @Test
    public void testRecreateDocumentActsForDifferentParticipant() {
        User author1 = TestHelper.createUser();
        User author2 = TestHelper.createUser();
        User clinician1 = TestHelper.createClinician();
        User clinician2 = TestHelper.createClinician();
        Party patient1 = TestHelper.createPatient();
        Party patient2 = TestHelper.createPatient();
        Entity template1 = createDocumentTemplate(PatientArchetypes.DOCUMENT_FORM);
        Entity template2 = createDocumentTemplate(PatientArchetypes.DOCUMENT_LETTER);
        Product product1 = createProduct(template1);
        Product product2 = createProduct(template2);

        /// create an item, and link a single document to it
        FinancialAct item = createItem(patient1, product1, author1, clinician1);
        ChargeItemDocumentLinker linker = new ChargeItemDocumentLinker(item, getArchetypeService());
        linker.link();

        // verify the document matches that expected
        ActBean bean = new ActBean(item);
        List<Act> documents = bean.getNodeActs("documents");
        assertEquals(1, documents.size());
        Act document1 = documents.get(0);
        checkDocument(document1, patient1, product1, template1, author1, clinician1, item);

        // perform the link again, and verify that act is the same
        linker.link();
        documents = bean.getNodeActs("documents");
        assertEquals(1, documents.size());
        Act same = documents.get(0);
        assertEquals(document1, same);

        // now change the product, and verify a new document has been created
        bean.setParticipant(ProductArchetypes.PRODUCT_PARTICIPATION, product2);
        linker.link();
        documents = bean.getNodeActs("documents");
        assertEquals(1, documents.size());
        Act different1 = documents.get(0);
        assertFalse(document1.equals(different1));
        assertNull(get(document1));  // should have been deleted
        checkDocument(different1, patient1, product2, template2, author1, clinician1, item);

        // now change the patient, and verify a new document has been created
        bean.setParticipant(PatientArchetypes.PATIENT_PARTICIPATION, patient2);
        linker.link();
        documents = bean.getNodeActs("documents");
        assertEquals(1, documents.size());
        Act different2 = documents.get(0);
        assertFalse(different1.equals(different2));
        assertNull(get(different1));  // should have been deleted
        checkDocument(different2, patient2, product2, template2, author1, clinician1, item);

        // now change the author, and verify a new document has been created
        bean.setParticipant(UserArchetypes.AUTHOR_PARTICIPATION, author2);
        linker.link();
        documents = bean.getNodeActs("documents");
        assertEquals(1, documents.size());
        Act different3 = documents.get(0);
        assertFalse(different2.equals(different3));
        assertNull(get(different2));  // should have been deleted
        checkDocument(different3, patient2, product2, template2, author2, clinician1, item);

        // now change the clinician, and verify a new document has been created
        bean.setParticipant(UserArchetypes.CLINICIAN_PARTICIPATION, clinician2);
        linker.link();
        documents = bean.getNodeActs("documents");
        assertEquals(1, documents.size());
        Act different4 = documents.get(0);
        assertFalse(different3.equals(different4));
        assertNull(get(different3));  // should have been deleted
        checkDocument(different4, patient2, product2, template2, author2, clinician2, item);
    }

    /**
     * Verifies that the document acts that are recreated if the clinician changes, are correctly removed when they
     * are linked to an event.
     */
    @Test
    public void testRecreateDocumentsLinkedToEvent() {
        Party patient = TestHelper.createPatient();
        Entity template1 = createDocumentTemplate(PatientArchetypes.DOCUMENT_FORM);
        Entity template2 = createDocumentTemplate(PatientArchetypes.DOCUMENT_LETTER);
        Product product = createProduct(template1, template2);
        User author = TestHelper.createUser();
        User clinician1 = TestHelper.createClinician();
        User clinician2 = TestHelper.createClinician();

        // create an invoice item with a product that is linked to two documents
        FinancialAct item = createItem(patient, product, author, clinician1);
        ActBean bean = new ActBean(item);
        bean.save();

        // link the product documents to the item
        ChargeItemDocumentLinker linker = new ChargeItemDocumentLinker(item, getArchetypeService());
        linker.link();

        List<Act> documents = bean.getNodeActs("documents");
        assertEquals(2, documents.size());

        // link the documents to an event
        MedicalRecordRules rules = new MedicalRecordRules(getArchetypeService());
        Act event = rules.getEventForAddition(patient, new Date(), null);
        for (Act document : documents) {
            rules.linkMedicalRecords(event, document);
        }

        // verify the documents have been linked
        event = get(event);
        assertEquals(2, event.getSourceActRelationships().size());

        // change the invoice item's clinician and re-link the documents
        bean.setParticipant(UserArchetypes.CLINICIAN_PARTICIPATION, clinician2);
        linker.link();

        // the event shouldn't have any relationships to documents any more
        event = get(event);
        assertEquals(0, event.getSourceActRelationships().size());
    }

    /**
     * Helper to create an invoice item.
     *
     * @param patient   the patient
     * @param product   the product
     * @param author    the author
     * @param clinician the clinician
     * @return a new invoice item
     */
    private FinancialAct createItem(Party patient, Product product, User author, User clinician) {
        FinancialAct item = FinancialTestHelper.createChargeItem(CustomerAccountArchetypes.INVOICE_ITEM, patient,
                                                                 product, BigDecimal.ONE);
        ActBean bean = new ActBean(item);
        bean.addNodeParticipation("author", author);
        bean.addNodeParticipation("clinician", clinician);
        return item;
    }

    /**
     * Verifies a document act matches that expected.
     *
     * @param document  the document act
     * @param patient   the expected patient
     * @param product   the expected product
     * @param template  the expected template
     * @param author    the expected author
     * @param clinician the expected clinician
     * @param item      the expected item
     */
    private void checkDocument(Act document, Party patient, Product product, Entity template, User author,
                               User clinician, FinancialAct item) {
        // verify the start time is the same as the invoice item start time
        assertEquals(0, DateRules.compareTo(item.getActivityStartTime(), document.getActivityStartTime(), true));

        // check participants
        ActBean docBean = new ActBean(document);
        assertEquals(patient, docBean.getNodeParticipant("patient"));
        assertEquals(template, docBean.getNodeParticipant("documentTemplate"));
        assertEquals(product, docBean.getNodeParticipant("product"));

        assertEquals(author, docBean.getNodeParticipant("author"));
        assertEquals(clinician, docBean.getNodeParticipant("clinician"));
    }

    /**
     * Saves the changes in a transaction.
     *
     * @param changes the changes
     */
    private void saveInTxn(final PatientHistoryChanges changes) {
        PlatformTransactionManager mgr = (PlatformTransactionManager) applicationContext.getBean("txnManager");
        TransactionTemplate template = new TransactionTemplate(mgr);
        template.execute(new TransactionCallback<Object>() {
            public Object doInTransaction(TransactionStatus status) {
                changes.save();
                return null;
            }
        });
    }


    /**
     * Returns the document with the associated template, failing if one isn't found.
     *
     * @param documents the documents
     * @param template  the template
     * @return the corresponding document
     */
    private Act getDocument(List<Act> documents, Entity template) {
        IMObjectReference templateRef = template.getObjectReference();
        for (Act document : documents) {
            ActBean bean = new ActBean(document);
            if (ObjectUtils.equals(templateRef, bean.getNodeParticipantRef("documentTemplate"))) {
                return document;
            }
        }
        Assert.fail("Template not found");
        return null;
    }

    /**
     * Creates and saves a new product.
     *
     * @param templates the document templates
     * @return a new product
     */
    private Product createProduct(Entity... templates) {
        Product product = (Product) create(ProductArchetypes.MEDICATION);
        EntityBean bean = new EntityBean(product);
        bean.setValue("name", "XProduct");
        for (Entity template : templates) {
            bean.addRelationship("entityRelationship.productDocument", template);
        }
        List<IMObject> objects = new ArrayList<IMObject>();
        objects.add(product);
        objects.addAll(Arrays.asList(templates));
        save(objects);
        return product;
    }

    /**
     * Creates and saves a new document template.
     *
     * @param archetype the document archetype
     * @return a new document template
     */
    private Entity createDocumentTemplate(String archetype) {
        Entity template = (Entity) create(DocumentArchetypes.DOCUMENT_TEMPLATE);
        EntityBean bean = new EntityBean(template);
        bean.setValue("name", "XDocumentTemplate");
        bean.setValue("archetype", archetype);
        bean.save();

        return template;
    }
}
