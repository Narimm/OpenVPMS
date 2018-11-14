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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.insurance.claim;

import org.junit.Test;
import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.insurance.InsuranceTestHelper;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.doc.DocumentTestHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.openvpms.archetype.test.TestHelper.createActIdentity;

/**
 * Tests the {@link AttachmentCollectionEditor}.
 *
 * @author Tim Anderson
 */
public class AttachmentCollectionEditorTestCase extends AbstractAppTest {

    /**
     * Verifies that the {@link AttachmentCollectionEditor#deleteGeneratedDocuments()} method only deletes documents
     * that have been generated from a template.
     */
    @Test
    public void testDeleteGeneratedDocuments() {
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient(customer);
        Party insurer = InsuranceTestHelper.createInsurer("Foo");
        Party location = TestHelper.createLocation();
        User clinician = TestHelper.createClinician();
        Act policy = InsuranceTestHelper.createPolicy(customer, patient, insurer,
                                                      createActIdentity("actIdentity.insurancePolicy", "POL123456"));
        save(policy);
        FinancialAct item = InsuranceTestHelper.createClaimItem("VENOM_328", new Date(), new Date());
        FinancialAct claim = InsuranceTestHelper.createClaim(policy, location, clinician, clinician, item);
        save(claim, item);

        PropertySet set = new PropertySet(claim);
        LayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));

        FinancialAct invoice = createInvoice(customer, patient);

        AttachmentCollectionEditor editor = new AttachmentCollectionEditor((CollectionProperty) set.get("attachments"),
                                                                           claim, context);
        editor.getComponent();

        DocumentAct patientAttachment = PatientTestHelper.createDocumentAttachment(new Date(), patient);
        DocumentAct patientForm = PatientTestHelper.createDocumentForm(patient);
        DocumentAct patientImage = PatientTestHelper.createDocumentImage(new Date(), patient);
        DocumentAct patientLetter = PatientTestHelper.createDocumentLetter(new Date(), patient);
        DocumentAct patientInvestigation = PatientTestHelper.createInvestigation(
                patient, ProductTestHelper.createInvestigationType());
        DocumentAct customerAttachment = createCustomerDocument(CustomerArchetypes.DOCUMENT_ATTACHMENT, customer);
        DocumentAct customerForm = createCustomerDocument(CustomerArchetypes.DOCUMENT_FORM, customer);
        DocumentAct customerLetter = createCustomerDocument(CustomerArchetypes.DOCUMENT_LETTER, customer);

        DocumentAct historyAttachment = editor.createHistory();
        editor.add(historyAttachment);
        DocumentAct invoiceAttachment = checkAddInvoice(editor, invoice);
        DocumentAct claimAttachment = checkAddDocument(editor, patientAttachment);
        DocumentAct formAttachment = checkAddDocument(editor, patientForm);
        DocumentAct letterAttachment = editor.addDocument(patientLetter);
        DocumentAct imageAttachment = editor.addDocument(patientImage);
        DocumentAct investigationAttachment = editor.addDocument(patientInvestigation);
        DocumentAct customerClaimAttachment = checkAddDocument(editor, customerAttachment);
        DocumentAct customerFormAttachment = checkAddDocument(editor, customerForm);
        DocumentAct customerLetterAttachment = checkAddDocument(editor, customerLetter);

        editor.save();

        // simulate generation
        setDocument(historyAttachment);
        setDocument(invoiceAttachment);
        setDocument(claimAttachment);
        setDocument(formAttachment);
        setDocument(letterAttachment);
        setDocument(imageAttachment);
        setDocument(investigationAttachment);
        setDocument(customerClaimAttachment);
        setDocument(customerFormAttachment);
        setDocument(customerLetterAttachment);

        editor.deleteGeneratedDocuments();

        assertNull(historyAttachment.getDocument());
        assertNull(invoiceAttachment.getDocument());
        assertNotNull(claimAttachment.getDocument());
        assertNull(formAttachment.getDocument());
        assertNotNull(letterAttachment.getDocument());
        assertNotNull(imageAttachment.getDocument());
        assertNotNull(investigationAttachment.getDocument());
        assertNotNull(customerClaimAttachment.getDocument());
        assertNull(customerFormAttachment.getDocument());
        assertNotNull(customerLetterAttachment.getDocument());
    }

    /**
     * Sets dummy content on a document.
     *
     * @param attachment the document attachment
     */
    private void setDocument(DocumentAct attachment) {
        Document document = DocumentTestHelper.createDocument("/blank.jrxml");
        attachment.setDocument(document.getObjectReference());
        save(document, attachment);
    }

    /**
     * Adds an invoice.
     *
     * @param editor  the editor
     * @param invoice the invoice
     * @return the invoice attachment
     */
    private DocumentAct checkAddInvoice(AttachmentCollectionEditor editor, FinancialAct invoice) {
        int size = editor.getCurrentActs().size();
        DocumentAct attachment = editor.addInvoice(invoice);
        assertEquals(size + 1, editor.getCurrentActs().size());
        assertEquals(attachment, editor.addInvoice(invoice));  // should not be re-added
        assertEquals(size + 1, editor.getCurrentActs().size());
        return attachment;
    }

    /**
     * Adds an document act.
     *
     * @param editor the editor
     * @param act    the document act
     * @return the document attachment
     */
    private DocumentAct checkAddDocument(AttachmentCollectionEditor editor, DocumentAct act) {
        int size = editor.getCurrentActs().size();
        DocumentAct attachment = editor.addDocument(act);
        assertEquals(size + 1, editor.getCurrentActs().size());
        assertEquals(attachment, editor.addDocument(act));  // should not be re-added
        assertEquals(size + 1, editor.getCurrentActs().size());
        return attachment;
    }

    /**
     * Creates an invoice.
     *
     * @param customer the customer
     * @param patient  the patient
     * @return a new invoice
     */
    private FinancialAct createInvoice(Party customer, Party patient) {
        List<FinancialAct> invoice = FinancialTestHelper.createChargesInvoice(BigDecimal.TEN, customer, patient,
                                                                              TestHelper.createProduct(),
                                                                              FinancialActStatus.POSTED);
        save(invoice);
        return invoice.get(0);
    }

    /**
     * Creates a customer document.
     *
     * @param archetype the document archetype
     * @param customer  the customer
     * @return a new document
     */
    private DocumentAct createCustomerDocument(String archetype, Party customer) {
        DocumentAct act = (DocumentAct) create(archetype);
        IMObjectBean bean = new IMObjectBean(act);
        bean.setTarget("customer", customer);
        bean.save();
        return act;
    }
}
