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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.insurance.internal.claim;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.claim.Condition;
import org.openvpms.insurance.claim.Invoice;
import org.openvpms.insurance.claim.Item;
import org.openvpms.insurance.claim.Note;
import org.openvpms.insurance.policy.Policy;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.openvpms.archetype.rules.finance.account.FinancialTestHelper.createChargesInvoice;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createAddendum;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createEvent;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createNote;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createPatient;
import static org.openvpms.archetype.test.TestHelper.createActIdentity;
import static org.openvpms.archetype.test.TestHelper.createEmailContact;
import static org.openvpms.archetype.test.TestHelper.createLocationContact;
import static org.openvpms.archetype.test.TestHelper.createPhoneContact;
import static org.openvpms.archetype.test.TestHelper.getDate;
import static org.openvpms.archetype.test.TestHelper.getDatetime;
import static org.openvpms.insurance.InsuranceTestHelper.createClaim;
import static org.openvpms.insurance.InsuranceTestHelper.createClaimItem;
import static org.openvpms.insurance.InsuranceTestHelper.createInsurer;
import static org.openvpms.insurance.InsuranceTestHelper.createPolicy;
import static org.openvpms.insurance.InsuranceTestHelper.createPolicyType;

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

    private Party customer;

    private Party patient;

    private User clinician;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        // set up the customer
        Contact address = createLocationContact("12 Broadwater Avenue", "CAPE_WOOLAMAI", "VIC", "3925");
        Contact home = createPhoneContact("03", "9123456", false, false, ContactArchetypes.HOME_PURPOSE);
        Contact work = createPhoneContact("03", "9123456", false, false, ContactArchetypes.WORK_PURPOSE);
        Contact mobile = createPhoneContact(null, "04987654321", true);
        Contact email = createEmailContact("foo@test.com");
        customer = TestHelper.createCustomer("J", "Bloggs", address, home, work, mobile, email);

        // clinician
        clinician = TestHelper.createClinician();

        // set up the patient
        Date dateOfBirth = DateRules.getDate(DateRules.getToday(), -1, DateUnits.YEARS);
        patient = createPatient("Fido", "CANINE", "PUG", "MALE", dateOfBirth, "123454321", "BLACK", customer);
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
        initDiagnosis("VENOM_328", "Abcess", "328");
    }

    /**
     * Tests the {@link ClaimImpl} methods.
     */
    @Test
    public void testClaim() {
        Party insurer = createInsurer(TestHelper.randomName("ZInsurer-"));
        Entity policyType = createPolicyType(insurer);
        Act policyAct = createPolicy(customer, patient, insurer, policyType,
                                     createActIdentity("actIdentity.insurancePolicy", "POL123456"));

        Date treatFrom1 = getDate("2017-09-27");
        Date treatTo1 = getDate("2017-09-29");
        Product product1 = TestHelper.createProduct();
        Product product2 = TestHelper.createProduct();
        Product product3 = TestHelper.createProduct();
        Date itemDate1 = getDatetime("2017-09-27 10:00:00");
        BigDecimal discount1 = new BigDecimal("0.10");
        BigDecimal tax1 = new BigDecimal("0.08");
        BigDecimal total1 = new BigDecimal("0.90");
        FinancialAct invoiceItem1 = createInvoiceItem(itemDate1, product1, ONE, ONE, discount1, tax1);
        Date itemDate2 = getDatetime("2017-09-27 11:00:00");
        BigDecimal discount2 = BigDecimal.ZERO;
        BigDecimal tax2 = new BigDecimal("0.91");
        BigDecimal total2 = BigDecimal.TEN;
        FinancialAct invoiceItem2 = createInvoiceItem(itemDate2, product2, ONE, TEN, discount2, tax2);
        List<FinancialAct> invoice1Acts = createInvoice(getDate("2017-09-27"), invoiceItem1, invoiceItem2);
        save(invoice1Acts);

        Date itemDate3 = getDatetime("2017-09-28 15:00:00");
        BigDecimal discount3 = BigDecimal.ZERO;
        BigDecimal tax3 = new BigDecimal("0.91");
        BigDecimal total3 = BigDecimal.TEN;
        FinancialAct invoiceItem3 = createInvoiceItem(itemDate3, product3, new BigDecimal("2"), new BigDecimal("5"),
                                                      discount3, tax3);
        List<FinancialAct> invoice2Acts = createInvoice(getDate("2017-09-28"), invoiceItem3);
        save(invoice2Acts);

        Act item1Act = createClaimItem("VENOM_328", treatFrom1, treatTo1, clinician, invoiceItem1, invoiceItem2,
                                       invoiceItem3);
        Act claimAct = createClaim(policyAct, clinician, item1Act);
        claimAct.addIdentity(createActIdentity("actIdentity.insuranceClaim", "CLM987654"));
        save(policyAct, claimAct, item1Act);

        Claim claim = new ClaimImpl(claimAct, getArchetypeService(), customerRules, patientRules);
        assertEquals(claimAct.getId(), claim.getId());
        assertEquals("CLM987654", claim.getClaimId());
        assertEquals(Claim.Status.PENDING, claim.getStatus());
        Policy policy = claim.getPolicy();
        assertNotNull(policy);
        assertEquals("POL123456", policy.getPolicyId());

        assertEquals(1, claim.getConditions().size());
        Condition condition1 = claim.getConditions().get(0);
        checkCondition(condition1, treatFrom1, treatTo1, "VENOM_328");
        assertEquals(2, condition1.getInvoices().size());

        // check the first invoice
        Invoice invoice1 = condition1.getInvoices().get(0);
        checkInvoice(invoice1, invoice1Acts.get(0).getId(), discount1.add(discount2), tax1.add(tax2),
                     total1.add(total2));

        List<Item> items1 = invoice1.getItems();
        assertEquals(2, items1.size());
        checkItem(items1.get(0), invoiceItem1.getId(), itemDate1, product1, discount1, tax1, total1);
        checkItem(items1.get(1), invoiceItem2.getId(), itemDate2, product2, discount2, tax2, total2);

        // check the second invoice
        Invoice invoice2 = condition1.getInvoices().get(1);
        checkInvoice(invoice2, invoice2Acts.get(0).getId(), discount3, tax3, total3);

        List<Item> items2 = invoice2.getItems();
        assertEquals(1, items2.size());
        checkItem(items2.get(0), invoiceItem3.getId(), itemDate3, product3, discount3, tax3, total3);

        // check the history
        List<Note> history = claim.getClinicalHistory();
        assertEquals(3, history.size());
        checkNote(history.get(0), getDate("2015-05-01"), clinician, "Note 1", 0);
        checkNote(history.get(1), getDate("2015-05-02"), clinician, "Note 2", 0);
        Note note3 = checkNote(history.get(2), getDate("2015-07-01"), clinician, "Note 3", 2);
        assertEquals(2, note3.getNotes().size());
        checkNote(note3.getNotes().get(0), getDate("2015-07-03"), clinician, "Note 3 addendum 1", 0);
        checkNote(note3.getNotes().get(1), getDate("2015-07-04"), clinician, "Note 3 addendum 2", 0);
    }

    /**
     * Verifies a condition matches that expected.
     *
     * @param condition   the condition to check
     * @param treatedFrom the expected treated-from date
     * @param treatedTo   the expected treated-to date
     * @param diagnosis   the expected diagnosis code
     */
    private void checkCondition(Condition condition, Date treatedFrom, Date treatedTo, String diagnosis) {
        assertEquals(treatedFrom, condition.getTreatedFrom());
        assertEquals(treatedTo, condition.getTreatedTo());
        Lookup lookup = condition.getDiagnosis();
        assertNotNull(lookup);
        assertEquals(diagnosis, lookup.getCode());
    }

    private Note checkNote(Note note, Date date, User clinician, String text, int notes) {
        assertEquals(date, note.getDate());
        assertEquals(clinician, note.getClinician());
        assertEquals(text, note.getText());
        assertEquals(notes, note.getNotes().size());
        return note;
    }

    /**
     * Verifies an invoice matches that expected.
     *
     * @param invoice  the invoice to check
     * @param id       the expected id
     * @param discount the expected discount
     * @param tax      the expected tax
     * @param total    the expected total
     */
    private void checkInvoice(Invoice invoice, long id, BigDecimal discount, BigDecimal tax, BigDecimal total) {
        assertEquals(id, invoice.getId());
        checkEquals(discount, invoice.getDiscount());
        checkEquals(tax, invoice.getTotalTax());
        checkEquals(total, invoice.getTotal());
    }

    /**
     * Verifies an invoice item matches that expected.
     *
     * @param item     the invoice item to check
     * @param id       the expected id
     * @param date     the expected date
     * @param product  the expected product
     * @param discount the expected discount
     * @param tax      the expected tax
     * @param total    the expected total
     */
    private void checkItem(Item item, long id, Date date, Product product, BigDecimal discount, BigDecimal tax,
                           BigDecimal total) {
        assertEquals(id, item.getId());
        assertEquals(date, item.getDate());
        assertEquals(product, item.getProduct());
        checkEquals(discount, item.getDiscount());
        checkEquals(tax, item.getTotalTax());
        checkEquals(total, item.getTotal());
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

    /**
     * Initialises a VeNom diagnosis lookup.
     *
     * @param code         the lookup code
     * @param name         the lookup name
     * @param dictionaryId the VeNom dictionary identifier for the code
     */
    private void initDiagnosis(String code, String name, String dictionaryId) {
        Lookup diagnosis = TestHelper.getLookup("lookup.diagnosisVeNom", code, false);
        IMObjectBean bean = new IMObjectBean(diagnosis);
        bean.setValue("name", name);
        bean.setValue("dataDictionaryId", dictionaryId);
        bean.save();
    }
}

