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

package org.openvpms.insurance.internal.claim;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.insurance.InsuranceTestHelper;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.claim.ClaimHandler;
import org.openvpms.insurance.claim.Condition;
import org.openvpms.insurance.claim.Invoice;
import org.openvpms.insurance.claim.Item;
import org.openvpms.insurance.claim.Note;
import org.openvpms.insurance.policy.Animal;
import org.openvpms.insurance.policy.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import static org.openvpms.insurance.InsuranceTestHelper.checkCondition;
import static org.openvpms.insurance.InsuranceTestHelper.checkInvoice;
import static org.openvpms.insurance.InsuranceTestHelper.checkItem;
import static org.openvpms.insurance.InsuranceTestHelper.checkNote;
import static org.openvpms.insurance.InsuranceTestHelper.createClaimItem;
import static org.openvpms.insurance.InsuranceTestHelper.createInsurer;
import static org.openvpms.insurance.InsuranceTestHelper.createPolicy;

/**
 * Tests the {@link ClaimImpl} class.
 *
 * @author Tim Anderson
 */
public class ClaimImplTestCase extends ArchetypeServiceTest {

    /**
     * The customer rules.
     */
    @Autowired
    private CustomerRules customerRules;

    /**
     * The patient rules.
     */
    @Autowired
    private PatientRules patientRules;

    /**
     * The transaction manager.
     */
    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * The document handlers.
     */
    private DocumentHandlers handlers;

    /**
     * The test customer.
     */
    private Party customer;

    /**
     * The test patient.
     */
    private Party patient;

    /**
     * Patient date of birth.
     */
    private Date dateOfBirth;

    /**
     * The test clinician.
     */
    private User clinician;

    /**
     * The practice location.
     */
    private Party location;

    /**
     * Practice location phone.
     */
    private Contact locationPhone;

    /**
     * Practice location email.
     */
    private Contact locationEmail;

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
    public void setUp() {
        // customer
        customer = TestHelper.createCustomer("MS", "J", "Bloggs", "12 Broadwater Avenue", "CAPE_WOOLAMAI", "VIC",
                                             "3925", "9123456", "98765432", "04987654321", "foo@test.com");
        handlers = new DocumentHandlers(getArchetypeService());

        // practice location
        locationPhone = TestHelper.createPhoneContact(null, "5123456", false);
        locationEmail = TestHelper.createEmailContact("vetsrus@test.com");
        location = TestHelper.createLocation();
        location.addContact(locationEmail);
        location.addContact(locationPhone);
        save(location);

        // clinician
        clinician = TestHelper.createClinician();

        // claim handler
        user = TestHelper.createUser("Z", "Smith");

        // patient
        dateOfBirth = DateRules.getDate(DateRules.getToday(), -1, DateUnits.YEARS);
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
        Party insurer = createInsurer(TestHelper.randomName("ZInsurer-"));

        // policy
        policyAct = createPolicy(customer, patient, insurer,
                                 createActIdentity("actIdentity.insurancePolicy", "POL123456"));
        save(policyAct);
    }

    /**
     * Tests the {@link ClaimImpl} methods.
     */
    @Test
    public void testClaim() {
        Date treatFrom1 = getDate("2017-09-27");
        Date treatTo1 = getDate("2017-09-29");

        Act note4 = createNote(treatFrom1, patient, clinician, "Note 4");
        createEvent(treatFrom1, treatTo1, patient, clinician, note4);

        Product product1 = TestHelper.createProduct();
        Product product2 = TestHelper.createProduct();
        Product product3 = TestHelper.createProduct();
        Date itemDate1 = getDatetime("2017-09-27 10:00:00");
        BigDecimal discount1 = new BigDecimal("0.10");
        BigDecimal discountTax1 = new BigDecimal("0.01");
        BigDecimal tax1 = new BigDecimal("0.08");
        BigDecimal total1 = new BigDecimal("0.90");
        FinancialAct invoiceItem1 = createInvoiceItem(itemDate1, product1, ONE, ONE, discount1, tax1);
        Date itemDate2 = getDatetime("2017-09-27 11:00:00");
        BigDecimal discount2 = BigDecimal.ZERO;
        BigDecimal discountTax2 = BigDecimal.ZERO;
        BigDecimal tax2 = new BigDecimal("0.91");
        BigDecimal total2 = BigDecimal.TEN;
        FinancialAct invoiceItem2 = createInvoiceItem(itemDate2, product2, ONE, TEN, discount2, tax2);
        List<FinancialAct> invoice1Acts = createInvoice(getDate("2017-09-27"), invoiceItem1, invoiceItem2);
        save(invoice1Acts);

        Date itemDate3 = getDatetime("2017-09-28 15:00:00");
        BigDecimal discount3 = BigDecimal.ZERO;
        BigDecimal discountTax3 = BigDecimal.ZERO;
        BigDecimal tax3 = new BigDecimal("0.91");
        BigDecimal total3 = BigDecimal.TEN;
        FinancialAct invoiceItem3 = createInvoiceItem(itemDate3, product3, new BigDecimal("2"), new BigDecimal("5"),
                                                      discount3, tax3);
        List<FinancialAct> invoice2Acts = createInvoice(getDate("2017-09-28"), invoiceItem3);
        save(invoice2Acts);

        FinancialAct item1Act = createClaimItem("VENOM_328", treatFrom1, treatTo1, invoiceItem1,
                                                invoiceItem2, invoiceItem3);
        Act claimAct = InsuranceTestHelper.createClaim(policyAct, location, clinician, user, item1Act);
        claimAct.addIdentity(createActIdentity("actIdentity.insuranceClaim", "CLM987654"));
        save(policyAct, claimAct, item1Act);

        Claim claim = createClaim(claimAct);

        // check the claim
        assertEquals(claimAct.getId(), claim.getId());
        assertEquals("CLM987654", claim.getInsurerId());
        assertEquals(Claim.Status.PENDING, claim.getStatus());
        Policy policy = claim.getPolicy();
        assertNotNull(policy);
        assertEquals("POL123456", policy.getInsurerId());

        ClaimHandler handler = claim.getClaimHandler();
        assertNotNull(handler);
        assertEquals(user.getName(), handler.getName());
        assertEquals(locationEmail, handler.getEmail());
        assertEquals(locationPhone, handler.getPhone());

        // check the animal
        Animal animal = claim.getAnimal();
        assertEquals(patient.getId(), animal.getId());
        assertEquals(patient.getName(), animal.getName());
        assertEquals(dateOfBirth, animal.getDateOfBirth());
        assertEquals("Canine", animal.getSpecies());
        assertEquals("Pug", animal.getBreed());
        assertEquals(Animal.Sex.MALE, animal.getSex());
        assertEquals("Black", animal.getColour());
        assertEquals("123454321", animal.getMicrochip());
        assertEquals(new IMObjectBean(patient).getDate("createdDate"), animal.getCreatedDate());

        // check the condition
        assertEquals(1, claim.getConditions().size());
        Condition condition1 = claim.getConditions().get(0);
        checkCondition(condition1, treatFrom1, treatTo1, "VENOM_328");
        assertEquals(2, condition1.getInvoices().size());

        BigDecimal conditionDiscount = discount1.add(discount2).add(discount3);
        BigDecimal conditionDiscountTax = discountTax1.add(discountTax2).add(discountTax3);
        BigDecimal conditionTotal = total1.add(total2).add(total3);
        BigDecimal conditionTax = tax1.add(tax2).add(tax3);

        checkEquals(conditionDiscount, condition1.getDiscount());
        checkEquals(conditionDiscountTax, condition1.getDiscountTax());
        checkEquals(conditionTotal, condition1.getTotal());
        checkEquals(conditionTax, condition1.getTotalTax());

        assertNull(condition1.getEuthanasiaReason());

        // check the consultation notes
        List<Note> notes = condition1.getConsultationNotes();
        assertEquals(1, notes.size());
        checkNote(notes.get(0), treatFrom1, clinician, "Note 4", 0);

        // check the first invoice
        Invoice invoice1 = condition1.getInvoices().get(0);
        checkInvoice(invoice1, invoice1Acts.get(0).getId(), discount1.add(discount2), discountTax1.add(discountTax2),
                     tax1.add(tax2), total1.add(total2));

        List<Item> items1 = invoice1.getItems();
        assertEquals(2, items1.size());
        checkItem(items1.get(0), invoiceItem1.getId(), itemDate1, product1, discount1, discountTax1, tax1, total1);
        checkItem(items1.get(1), invoiceItem2.getId(), itemDate2, product2, discount2, discountTax2, tax2, total2);

        // check the second invoice
        Invoice invoice2 = condition1.getInvoices().get(1);
        checkInvoice(invoice2, invoice2Acts.get(0).getId(), discount3, discountTax3, tax3, total3);

        List<Item> items2 = invoice2.getItems();
        assertEquals(1, items2.size());
        checkItem(items2.get(0), invoiceItem3.getId(), itemDate3, product3, discount3, discountTax3, tax3, total3);

        assertEquals(conditionTotal, claim.getTotal());
        assertEquals(conditionTax, claim.getTotalTax());
        assertEquals(conditionDiscount, claim.getDiscount());
        assertEquals(discountTax1.add(discountTax2).add(discountTax3), claim.getDiscountTax());

        // check the history
        List<Note> history = claim.getClinicalHistory();
        assertEquals(4, history.size());
        checkNote(history.get(0), getDate("2015-05-01"), clinician, "Note 1", 0);
        checkNote(history.get(1), getDate("2015-05-02"), clinician, "Note 2", 0);
        Note note3 = checkNote(history.get(2), getDate("2015-07-01"), clinician, "Note 3", 2);
        checkNote(note3.getNotes().get(0), getDate("2015-07-03"), clinician, "Note 3 addendum 1", 0);
        checkNote(note3.getNotes().get(1), getDate("2015-07-04"), clinician, "Note 3 addendum 2", 0);
        checkNote(history.get(3), treatFrom1, clinician, "Note 4", 0);
    }

    /**
     * Tests the {@link Claim#canCancel()} method.
     */
    @Test
    public void testCancel() {
        Product product1 = TestHelper.createProduct();
        Date itemDate1 = getDatetime("2017-09-27 10:00:00");
        BigDecimal discount1 = new BigDecimal("0.10");
        BigDecimal tax1 = new BigDecimal("0.08");
        FinancialAct invoiceItem1 = createInvoiceItem(itemDate1, product1, ONE, ONE, discount1, tax1);
        List<FinancialAct> invoice1Acts = createInvoice(getDate("2017-09-27"), invoiceItem1);
        save(invoice1Acts);

        FinancialAct item1Act = createClaimItem("VENOM_328", itemDate1, itemDate1, invoiceItem1);
        Act claimAct = InsuranceTestHelper.createClaim(policyAct, location, clinician, user, item1Act);
        save(claimAct, item1Act);

        Claim claim = new ClaimImpl(claimAct, (IArchetypeRuleService) getArchetypeService(), customerRules,
                                    patientRules, handlers, transactionManager);
        assertEquals(Claim.Status.PENDING, claim.getStatus());
        assertTrue(claim.canCancel());

        claim.setStatus(Claim.Status.POSTED);
        assertTrue(claim.canCancel());

        claim.setStatus(Claim.Status.SUBMITTED);
        assertTrue(claim.canCancel());

        claim.setStatus(Claim.Status.ACCEPTED);
        assertTrue(claim.canCancel());

        claim.setStatus(Claim.Status.SETTLED);
        assertFalse(claim.canCancel());

        claim.setStatus(Claim.Status.DECLINED);
        assertFalse(claim.canCancel());

        claim.setStatus(Claim.Status.CANCELLING);
        assertFalse(claim.canCancel());

        claim.setStatus(Claim.Status.CANCELLED);
        assertFalse(claim.canCancel());
    }

    /**
     * Verifies that when an <em>act.patientInsuranceClaim</em> is deleted, related items and their attachments are
     * deleted, but charges and original documents are retained.
     */
    @Test
    public void testDeleteClaim() {
        Product product1 = TestHelper.createProduct();
        Date itemDate1 = getDatetime("2017-09-27 10:00:00");
        BigDecimal discount1 = new BigDecimal("0.10");
        BigDecimal tax1 = new BigDecimal("0.08");
        FinancialAct invoiceItem1 = createInvoiceItem(itemDate1, product1, ONE, ONE, discount1, tax1);
        List<FinancialAct> invoice1Acts = createInvoice(getDate("2017-09-27"), invoiceItem1);
        save(invoice1Acts);

        FinancialAct item1Act = createClaimItem("VENOM_328", itemDate1, itemDate1, invoiceItem1);

        Act claimAct = InsuranceTestHelper.createClaim(policyAct, location, clinician, user, item1Act);
        ActBean bean = new ActBean(claimAct);
        DocumentAct documentAct = PatientTestHelper.createDocumentAttachment(itemDate1, patient);
        DocumentAct attachment = InsuranceTestHelper.createAttachment(documentAct);
        Document content = (Document) create(DocumentArchetypes.DEFAULT_DOCUMENT);
        content.setName(documentAct.getName());
        attachment.setDocument(content.getObjectReference());
        bean.addNodeRelationship("attachments", attachment);
        save(claimAct, item1Act, attachment, content);

        remove(claimAct);
        assertNull(get(claimAct));
        assertNull(get(item1Act));
        assertNull(get(attachment));

        // NOTE: the document content is NOT deleted when the act is deleted.
        // It would be possible to set up .drl rules to handle claim deletion but it would likely interfere with
        // deletion from editors+
        assertNotNull(get(content));

        // verify the original document hasn't been deleted
        assertNotNull(get(documentAct));

        // verify the policy hasn't been deleted
        assertNotNull(get(policyAct));

        // verify the invoice hasn't been deleted
        assertNotNull(get(invoiceItem1));
        assertNotNull(get(invoice1Acts.get(0)));
    }

    /**
     * Creates a claim.
     *
     * @param act the claim act
     * @return a new claim
     */
    private Claim createClaim(Act act) {
        return new ClaimImpl(act, (IArchetypeRuleService) getArchetypeService(), customerRules, patientRules,
                             handlers, transactionManager);
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

