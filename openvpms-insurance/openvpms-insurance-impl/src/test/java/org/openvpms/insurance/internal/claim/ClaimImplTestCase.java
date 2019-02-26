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

import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.insurance.InsuranceTestHelper;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.user.User;
import org.openvpms.domain.patient.Patient;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.claim.ClaimHandler;
import org.openvpms.insurance.claim.Condition;
import org.openvpms.insurance.claim.Invoice;
import org.openvpms.insurance.claim.Item;
import org.openvpms.insurance.claim.Note;
import org.openvpms.insurance.policy.Policy;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.insurance.InsuranceTestHelper.createClaimItem;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createAddendum;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createEvent;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createNote;
import static org.openvpms.archetype.test.TestHelper.createActIdentity;
import static org.openvpms.archetype.test.TestHelper.getDate;
import static org.openvpms.archetype.test.TestHelper.getDatetime;

/**
 * Tests the {@link ClaimImpl} class.
 *
 * @author Tim Anderson
 */
public class ClaimImplTestCase extends AbstractClaimTest {

    /**
     * Tests the {@link ClaimImpl} methods.
     */
    @Test
    public void testClaim() {
        Date treatFrom1 = getDate("2017-09-27");
        Date treatTo1 = getDate("2017-09-29");

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

        FinancialAct item1Act = (FinancialAct) createClaimItem("VENOM_328", treatFrom1, treatTo1, invoiceItem1,
                                                               invoiceItem2, invoiceItem3);
        Act claimAct = (Act) InsuranceTestHelper.createClaim(policyAct, location, clinician, user, false, item1Act);
        claimAct.addIdentity(createActIdentity("actIdentity.insuranceClaim", "CLM987654"));
        save(policyAct, claimAct, item1Act);

        Claim claim = createClaim(claimAct);

        // check the claim
        assertEquals(claimAct.getId(), claim.getId());
        assertEquals("CLM987654", claim.getInsurerId());
        assertEquals(Claim.Status.PENDING, claim.getStatus());
        Policy policy = claim.getPolicy();
        assertNotNull(policy);
        assertEquals("POL123456", policy.getPolicyNumber());

        ClaimHandler handler = claim.getClaimHandler();
        assertNotNull(handler);
        assertEquals(user.getName(), handler.getName());
        assertEquals(locationEmail, handler.getEmail());
        assertEquals(locationPhone, handler.getPhone());

        // check the animal
        Patient animal = claim.getAnimal();
        assertEquals(patient.getId(), animal.getId());
        assertEquals(patient.getName(), animal.getName());
        assertEquals(dateOfBirth, DateRules.toDate(animal.getDateOfBirth()));
        assertEquals("Canine", animal.getSpeciesName());
        assertEquals("Pug", animal.getBreedName());
        assertEquals(Patient.Sex.MALE, animal.getSex());
        assertEquals("Black", animal.getColourName());
        assertEquals("123454321", animal.getMicrochip().getIdentity());
        assertEquals(new IMObjectBean(patient).getDate("createdDate"), DateRules.toDate(animal.getCreated()));

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
        Note n3 = checkNote(history.get(2), getDate("2015-07-01"), clinician, "Note 3", 2);
        checkNote(n3.getNotes().get(0), getDate("2015-07-03"), clinician, "Note 3 addendum 1", 0);
        checkNote(n3.getNotes().get(1), getDate("2015-07-04"), clinician, "Note 3 addendum 2", 0);
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

        FinancialAct item1Act = (FinancialAct) createClaimItem("VENOM_328", itemDate1, itemDate1, invoiceItem1);
        Act claimAct = (Act) InsuranceTestHelper.createClaim(policyAct, location, clinician, user, false, item1Act);
        save(claimAct, item1Act);

        Claim claim = createClaim(claimAct);
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

        claimAct.setStatus(Claim.Status.DECLINED.toString());
        assertFalse(claim.canCancel());

        claimAct.setStatus(Claim.Status.CANCELLING.toString());
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

        FinancialAct item1Act = (FinancialAct) createClaimItem("VENOM_328", itemDate1, itemDate1, invoiceItem1);

        Act claimAct = (Act) InsuranceTestHelper.createClaim(policyAct, location, clinician, user, false, item1Act);
        ActBean bean = new ActBean(claimAct);
        DocumentAct documentAct = PatientTestHelper.createDocumentAttachment(itemDate1, patient);
        DocumentAct attachment = (DocumentAct) InsuranceTestHelper.createAttachment(documentAct);
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
     * Verifies the claim policy can be changed.
     */
    @Test
    public void testSetPolicy() {
        FinancialAct item1Act = (FinancialAct) createClaimItem("VENOM_328", new Date(), new Date());
        Act claimAct1 = (Act) InsuranceTestHelper.createClaim(policyAct, location, clinician, user, false, item1Act);
        save(policyAct, claimAct1, item1Act);

        Party insurer2 = (Party) InsuranceTestHelper.createInsurer(TestHelper.randomName("ZInsurer-"));

        Claim claim1 = createClaim(claimAct1);
        Policy existing = claim1.getPolicy();
        Policy update1 = claim1.setPolicy(insurer1, "POL123456");
        assertEquals(existing, update1);                 // policy should not change
        checkPolicy(update1, insurer1, "POL123456");

        Policy update2 = claim1.setPolicy(insurer2, "POL123456");
        assertNotEquals(update1, update2);               // policy should be changed
        assertEquals(existing.getId(), update2.getId()); // but should be same underlying act
        checkPolicy(update2, insurer2, "POL123456");

        Policy update3 = claim1.setPolicy(insurer2, "POL987654");
        assertNotEquals(update2, update3);               // policy should be changed
        assertEquals(existing.getId(), update3.getId()); // but should be same underlying act
        checkPolicy(update3, insurer2, "POL987654");

        // associate the policy with another claim.
        policyAct = get(policyAct);
        FinancialAct item2Act = (FinancialAct) createClaimItem("VENOM_328", new Date(), new Date());
        Act claimAct2 = (Act) InsuranceTestHelper.createClaim(policyAct, location, clinician, user, false, item2Act);
        save(policyAct, claimAct2, item2Act);

        claim1 = createClaim(claimAct1);
        Policy update4 = claim1.setPolicy(insurer2, "POL1111111");
        assertNotEquals(update3, update4);              // policy should be changed
        assertEquals(existing.getId(), update3.getId()); // as should the underlying act
        assertNotEquals(update4.getId(), policyAct.getId());
        checkPolicy(update4, insurer2, "POL1111111");
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
        assertEquals(treatedFrom, DateRules.toDate(condition.getTreatedFrom()));
        assertEquals(treatedTo, DateRules.toDate(condition.getTreatedTo()));
        Lookup lookup = condition.getDiagnosis();
        assertNotNull(lookup);
        assertEquals(diagnosis, lookup.getCode());
    }

    /**
     * Verifies a note matches that expected.
     *
     * @param note      the note to check
     * @param date      the expected date
     * @param clinician the expected clinician
     * @param text      the expected tex
     * @param notes     the expected no. of addenda
     * @return the note
     */
    private Note checkNote(Note note, Date date, User clinician, String text, int notes) {
        assertEquals(date, DateRules.toDate(note.getDate()));
        assertEquals(clinician, note.getClinician());
        assertEquals(text, note.getText());
        assertEquals(notes, note.getNotes().size());
        return note;
    }

    /**
     * Verifies an invoice matches that expected.
     *
     * @param invoice     the invoice to check
     * @param id          the expected id
     * @param discount    the expected discount
     * @param discountTax the expected discount tax
     * @param tax         the expected tax
     * @param total       the expected total
     */
    private void checkInvoice(Invoice invoice, long id, BigDecimal discount,
                              BigDecimal discountTax, BigDecimal tax, BigDecimal total) {
        assertEquals(id, invoice.getId());
        checkEquals(discount, invoice.getDiscount());
        checkEquals(discountTax, invoice.getDiscountTax());
        checkEquals(tax, invoice.getTotalTax());
        checkEquals(total, invoice.getTotal());
    }

    /**
     * Verifies an invoice item matches that expected.
     *
     * @param item        the invoice item to check
     * @param id          the expected id
     * @param date        the expected date
     * @param product     the expected product
     * @param discount    the expected discount
     * @param discountTax the expected discount tax
     * @param tax         the expected tax
     * @param total       the expected total
     */
    private void checkItem(Item item, long id, Date date, org.openvpms.component.model.product.Product product, BigDecimal discount,
                           BigDecimal discountTax, BigDecimal tax, BigDecimal total) {
        assertEquals(id, item.getId());
        assertEquals(date, DateRules.toDate(item.getDate()));
        assertEquals(product, item.getProduct());
        checkEquals(discount, item.getDiscount());
        checkEquals(discountTax, item.getDiscountTax());
        checkEquals(tax, item.getTotalTax());
        checkEquals(total, item.getTotal());
    }

}

