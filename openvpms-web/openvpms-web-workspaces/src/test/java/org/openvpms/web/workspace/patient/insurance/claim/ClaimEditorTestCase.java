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

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.insurance.InsuranceTestHelper;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.finance.account.FinancialTestHelper.createChargesInvoice;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createAddendum;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createEvent;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createNote;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createPatient;
import static org.openvpms.archetype.test.TestHelper.createActIdentity;
import static org.openvpms.archetype.test.TestHelper.getDate;
import static org.openvpms.archetype.test.TestHelper.getDatetime;

/**
 * Tests the {@link ClaimEditor}.
 *
 * @author Tim Anderson
 */
public class ClaimEditorTestCase extends AbstractAppTest {

    /**
     * The transaction manager.
     */
    @Autowired
    PlatformTransactionManager transactionManager;

    /**
     * The test customer.
     */
    private Party customer;

    /**
     * The test patient.
     */
    private Party patient;

    /**
     * The test clinician.
     */
    private User clinician;

    /**
     * The practice location.
     */
    private Party location;

    /**
     * The claim handler.
     */
    private User user;

    /**
     * The policy.
     */
    private Act policyAct;

    /**
     * Sets up the test case.
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();
        // customer
        customer = TestHelper.createCustomer("MS", "J", "Bloggs", "12 Broadwater Avenue", "CAPE_WOOLAMAI", "VIC",
                                             "3925", "9123456", "98765432", "04987654321", "foo@test.com");

        // practice location
        location = TestHelper.createLocation("5123456", "vetsrus@test.com", false);

        // clinician
        clinician = TestHelper.createClinician();

        // claim handler
        user = TestHelper.createUser("Z", "Smith");

        // patient
        Date dateOfBirth = DateRules.getDate(DateRules.getToday(), -1, DateUnits.YEARS);
        patient = createPatient("Fido", "CANINE", "PUG", "MALE", dateOfBirth, "123454321", "BLACK", customer);

        // patient history
        Act note1 = createNote(getDate("2015-05-01"), patient, clinician, "Note 1");
        Act note2 = createNote(getDate("2015-05-02"), patient, clinician, "Note 2");
        createEvent(getDate("2015-05-01"), patient, note1, note2);
        Act note3 = createNote(getDate("2015-07-01"), patient, clinician, "Note 3");
        Act addendum1 = createAddendum(getDate("2015-07-03"), patient, clinician, "Note 3 addendum 1");
        Act addendum2 = createAddendum(getDate("2015-07-04"), patient, clinician, "Note 3 addendum 2");
        PatientTestHelper.addAddendum(note3, addendum1);
        PatientTestHelper.addAddendum(note3, addendum2);
        createEvent(getDate("2015-07-01"), patient, note3);

        // diagnosis codes
        InsuranceTestHelper.createDiagnosis("VENOM_328", "Abcess", "328");

        // insurer
        Party insurer = InsuranceTestHelper.createInsurer(TestHelper.randomName("ZInsurer-"));

        // policy
        policyAct = InsuranceTestHelper.createPolicy(customer, patient, insurer,
                                                     createActIdentity("actIdentity.insurancePolicy", "POL123456"));
        save(policyAct);
    }

    /**
     * Tests the {@link ClaimEditor#delete()} method.
     */
    @Test
    public void testDelete() {
        Product product1 = TestHelper.createProduct();
        Date itemDate1 = getDatetime("2017-09-27 10:00:00");
        BigDecimal discount1 = new BigDecimal("0.10");
        BigDecimal tax1 = new BigDecimal("0.08");
        FinancialAct invoiceItem1 = createInvoiceItem(itemDate1, product1, ONE, ONE, discount1, tax1);
        List<FinancialAct> invoice1Acts = createInvoice(getDate("2017-09-27"), invoiceItem1);
        save(invoice1Acts);

        FinancialAct item1Act = InsuranceTestHelper.createClaimItem("VENOM_328", itemDate1, itemDate1, invoiceItem1);

        FinancialAct claimAct = InsuranceTestHelper.createClaim(policyAct, location, clinician, user, item1Act);
        ActBean bean = new ActBean(claimAct);

        // add some attachments
        DocumentAct documentAct1 = PatientTestHelper.createDocumentAttachment(itemDate1, patient);
        DocumentAct attachment1 = InsuranceTestHelper.createAttachment(documentAct1);
        Document content1 = (Document) create(DocumentArchetypes.DEFAULT_DOCUMENT);
        content1.setName(documentAct1.getName());
        attachment1.setDocument(content1.getObjectReference());
        bean.addNodeRelationship("attachments", attachment1);

        DocumentAct documentAct2 = PatientTestHelper.createDocumentAttachment(itemDate1, patient);
        DocumentAct attachment2 = InsuranceTestHelper.createAttachment(documentAct2);
        Document content2 = (Document) create(DocumentArchetypes.DEFAULT_DOCUMENT);
        content2.setName(documentAct2.getName());
        attachment2.setDocument(content2.getObjectReference());
        bean.addNodeRelationship("attachments", attachment2);

        save(claimAct, item1Act, attachment1, content1, attachment2, content2);

        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                ClaimEditor editor = createEditor(claimAct);
                editor.delete();
            }
        });

        assertNull(get(claimAct));
        assertNull(get(item1Act));
        assertNull(get(attachment1));
        assertNull(get(content1));
        assertNull(get(claimAct));
        assertNull(get(attachment2));
        assertNull(get(content2));

        // verify the original documents haven't been deleted
        assertNotNull(get(documentAct1));
        assertNotNull(get(documentAct2));

        // verify the policy hasn't been deleted
        assertNotNull(get(policyAct));

        // verify the invoice hasn't been deleted
        assertNotNull(get(invoiceItem1));
        assertNotNull(get(invoice1Acts.get(0)));
    }

    /**
     * Verifies that when a claim item is deleted, no associated invoice items are deleted.
     */
    @Test
    public void testDeleteClaimItem() {
        Date date1 = getDatetime("2017-09-27 10:00:00");
        FinancialAct invoiceItem1 = createInvoiceItem(date1);
        List<FinancialAct> invoice1Acts = createInvoice(date1, invoiceItem1);
        FinancialAct invoice1 = invoice1Acts.get(0);
        save(invoice1Acts);

        Date date2 = getDatetime("2017-10-27 15:00:00");
        FinancialAct invoiceItem2 = createInvoiceItem(date2);
        List<FinancialAct> invoice2Acts = createInvoice(date2, invoiceItem2);
        FinancialAct invoice2 = invoice2Acts.get(0);
        save(invoice2Acts);

        FinancialAct item1Act = InsuranceTestHelper.createClaimItem("VENOM_328", date1, date1, invoiceItem1);
        FinancialAct item2Act = InsuranceTestHelper.createClaimItem("VENOM_328", date2, date2, invoiceItem2);

        FinancialAct claimAct = InsuranceTestHelper.createClaim(policyAct, location, clinician, user, item1Act,
                                                                item2Act);
        save(claimAct, item1Act, item2Act, invoiceItem1, invoiceItem2);

        ClaimEditor editor = createEditor(claimAct);

        // verify the tax and amount are correct
        checkClaim(claimAct, new BigDecimal("1.64"), BigDecimal.valueOf(18));

        // remove the second item
        editor.getItems().remove(item2Act);

        // verify the tax and amount have updated
        checkClaim(claimAct, new BigDecimal("0.82"), BigDecimal.valueOf(9));

        // save the editor
        assertTrue(SaveHelper.save(editor));

        // verify the claim item has been deleted
        assertNull(get(item2Act));

        // verify the invoices haven't been deleted
        assertNotNull(get(invoice1));
        assertNotNull(get(invoiceItem1));
        assertNotNull(get(invoice2));
        assertNotNull(get(invoiceItem2));

        // verify the other claim item hasn't been deleted
        assertNotNull(item1Act);
    }

    /**
     * Verifies that when an attachment is deleted, the associated document is also deleted.
     */
    @Test
    public void testDeleteAttachment() {
        Date itemDate1 = getDatetime("2017-09-27 10:00:00");
        FinancialAct invoiceItem1 = createInvoiceItem(itemDate1);
        List<FinancialAct> invoice1Acts = createInvoice(getDate("2017-09-27"), invoiceItem1);
        save(invoice1Acts);

        FinancialAct item1Act = InsuranceTestHelper.createClaimItem("VENOM_328", itemDate1, itemDate1, invoiceItem1);

        FinancialAct claimAct = InsuranceTestHelper.createClaim(policyAct, location, clinician, user, item1Act);
        ActBean bean = new ActBean(claimAct);
        DocumentAct documentAct = PatientTestHelper.createDocumentAttachment(itemDate1, patient);
        Document content = (Document) create(DocumentArchetypes.DEFAULT_DOCUMENT);
        content.setName(documentAct.getName());
        documentAct.setDocument(content.getObjectReference());
        save(documentAct, content);

        DocumentAct attachment = InsuranceTestHelper.createAttachment(documentAct);
        Document copy = (Document) create(DocumentArchetypes.DEFAULT_DOCUMENT);
        copy.setName(documentAct.getName());
        attachment.setDocument(copy.getObjectReference());
        bean.addNodeRelationship("attachments", attachment);
        save(claimAct, item1Act, documentAct, content, attachment, copy);

        ClaimEditor editor = createEditor(claimAct);

        editor.getAttachments().remove(attachment);
        assertTrue(SaveHelper.save(editor));

        assertNull(get(attachment));
        assertNull(get(copy));

        // verify the original document is untouched
        assertNotNull(get(documentAct));
        assertNotNull(get(content));
    }

    /**
     * Creates a new editor.
     *
     * @param claim the claim to edit
     * @return a new editor
     */
    private ClaimEditor createEditor(FinancialAct claim) {
        LayoutContext layout = new DefaultLayoutContext(true, new LocalContext(), new HelpContext("foo", null));
        ClaimEditor editor = new ClaimEditor(claim, null, layout);
        editor.getComponent();
        return editor;
    }

    /**
     * Verifies a claim matches that expected.
     *
     * @param claim the claim
     * @param tax   the expected tax amount
     * @param total the expected total
     */
    private void checkClaim(FinancialAct claim, BigDecimal tax, BigDecimal total) {
        checkEquals(tax, claim.getTaxAmount());
        checkEquals(total, claim.getTotal());
    }

    /**
     * Creates an invoice item, with quantity=1, price=10, discount=1, tax=0.82, total=9
     *
     * @return the new invoice item
     */
    private FinancialAct createInvoiceItem(Date date) {
        BigDecimal discount = BigDecimal.ONE;
        BigDecimal tax = new BigDecimal("0.82");
        return createInvoiceItem(date, TestHelper.createProduct(), ONE, BigDecimal.TEN, discount, tax);
    }

    /**
     * Creates an invoice item.
     *
     * @param date     the date
     * @param product  the product
     * @param quantity the quantity
     * @param price    the unit price
     * @param discount the discount
     * @param tax      the tax
     * @return the new invoice item
     */
    private FinancialAct createInvoiceItem(Date date, Product product, BigDecimal quantity, BigDecimal price,
                                           BigDecimal discount, BigDecimal tax) {
        return FinancialTestHelper.createInvoiceItem(date, patient, clinician, product, quantity, ZERO, price,
                                                     discount, tax);
    }

    /**
     * Creates and saves a POSTED invoice.
     *
     * @param date  the invoice date
     * @param items the invoice items
     * @return the invoice acs
     */
    private List<FinancialAct> createInvoice(Date date, FinancialAct... items) {
        List<FinancialAct> invoice = createChargesInvoice(customer, clinician, ActStatus.POSTED, items);
        invoice.get(0).setActivityStartTime(date);
        save(invoice);
        return invoice;
    }
}
