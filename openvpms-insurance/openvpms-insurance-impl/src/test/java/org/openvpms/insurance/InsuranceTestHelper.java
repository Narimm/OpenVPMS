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

package org.openvpms.insurance;

import org.openvpms.archetype.rules.patient.insurance.InsuranceArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActIdentity;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.insurance.claim.Condition;
import org.openvpms.insurance.claim.Invoice;
import org.openvpms.insurance.claim.Item;
import org.openvpms.insurance.claim.Note;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.openvpms.archetype.test.TestHelper.checkEquals;
import static org.openvpms.archetype.test.TestHelper.create;
import static org.openvpms.archetype.test.TestHelper.save;

/**
 * Helper for insurance tests.
 *
 * @author Tim Anderson
 */
public class InsuranceTestHelper {

    /**
     * Creates and saves a new insurer.
     *
     * @param name the insurer name
     * @return a new insurer
     */
    public static Party createInsurer(String name) {
        Party result = (Party) create(SupplierArchetypes.INSURER);
        result.setName(name);
        save(result);
        return result;
    }

    /**
     * Creates a new policy starting today, and expiring in 12 months.
     *
     * @param customer the customer
     * @param patient  the patient
     * @param insurer  the insurer
     * @param identity the policy identity. May be {@code null}
     * @return a new policy
     */
    public static Act createPolicy(Party customer, Party patient, Party insurer, ActIdentity identity) {
        Act policy = (Act) create(InsuranceArchetypes.POLICY);
        ActBean bean = new ActBean(policy);
        Date from = new Date();
        Date to = DateRules.getDate(from, 1, DateUnits.YEARS);
        policy.setActivityStartTime(from);
        policy.setActivityEndTime(to);
        bean.setNodeParticipant("customer", customer);
        bean.setNodeParticipant("patient", patient);
        bean.setNodeParticipant("insurer", insurer);
        if (identity != null) {
            policy.addIdentity(identity);
        }
        return policy;
    }

    /**
     * Creates a claim for a policy.
     *
     * @param policy       the policy
     * @param location     the practice location
     * @param clinician    the clinician
     * @param claimHandler the claim handler
     * @param items        the claim items. A list of <em>act.patientInsuranceClaimItem</em>
     * @return a new claim
     */
    public static FinancialAct createClaim(Act policy, Party location, User clinician, User claimHandler,
                                           FinancialAct... items) {
        FinancialAct claim = (FinancialAct) create(InsuranceArchetypes.CLAIM);
        ActBean bean = new ActBean(claim);
        ActBean policyBean = new ActBean(policy);
        bean.setNodeParticipant("patient", policyBean.getNodeParticipantRef("patient"));
        bean.setNodeParticipant("author", claimHandler);
        bean.setNodeParticipant("clinician", clinician);
        bean.setNodeParticipant("location", location);
        bean.setNodeParticipant("user", claimHandler);
        bean.addNodeRelationship("policy", policy);
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        for (FinancialAct item : items) {
            bean.addNodeRelationship("items", item);
            total = total.add(item.getTotal());
            tax = tax.add(item.getTaxAmount());
        }
        bean.setValue("amount", total);
        bean.setValue("tax", tax);
        return claim;
    }

    /**
     * Creates a new claim item.
     *
     * @param diagnosis    the VeNom diagnosis code
     * @param startTime    the treatment start time
     * @param endTime      the treatment end time
     * @param invoiceItems the invoice items being claimed
     * @return a new claim item
     */
    public static FinancialAct createClaimItem(String diagnosis, Date startTime, Date endTime,
                                               FinancialAct... invoiceItems) {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        FinancialAct item = (FinancialAct) create(InsuranceArchetypes.CLAIM_ITEM);
        ActBean bean = new ActBean(item);
        item.setReason(TestHelper.getLookup("lookup.diagnosisVeNom", diagnosis).getCode());
        item.setActivityStartTime(startTime);
        item.setActivityEndTime(endTime);
        item.setDescription("Condition description");
        for (FinancialAct invoiceItem : invoiceItems) {
            total = total.add(invoiceItem.getTotal());
            tax = tax.add(invoiceItem.getTaxAmount());
            bean.addNodeRelationship("items", invoiceItem);
        }
        bean.setValue("amount", total);
        bean.setValue("tax", tax);
        return item;
    }

    /**
     * Creates an insurance claim attachment.
     *
     * @param document the document to link to
     * @return a new attachment
     */
    public static DocumentAct createAttachment(DocumentAct document) {
        DocumentAct result = (DocumentAct) create(InsuranceArchetypes.ATTACHMENT);
        ActBean bean = new ActBean(result);
        bean.setValue("name", document.getName());
        bean.addNodeRelationship("original", document);
        save(result, document);
        return result;
    }

    /**
     * Creates a VeNom diagnosis lookup.
     *
     * @param code         the lookup code
     * @param name         the lookup name
     * @param dictionaryId the VeNom dictionary identifier for the code
     * @return the lookup
     */
    public static Lookup createDiagnosis(String code, String name, String dictionaryId) {
        Lookup diagnosis = TestHelper.getLookup("lookup.diagnosisVeNom", code, false);
        IMObjectBean bean = new IMObjectBean(diagnosis);
        bean.setValue("name", name);
        bean.setValue("dataDictionaryId", dictionaryId);
        bean.save();
        return diagnosis;
    }

    /**
     * Verifies a condition matches that expected.
     *
     * @param condition   the condition to check
     * @param treatedFrom the expected treated-from date
     * @param treatedTo   the expected treated-to date
     * @param diagnosis   the expected diagnosis code
     */
    public static void checkCondition(Condition condition, Date treatedFrom, Date treatedTo, String diagnosis) {
        assertEquals(treatedFrom, condition.getTreatedFrom());
        assertEquals(treatedTo, condition.getTreatedTo());
        Lookup lookup = condition.getDiagnosis();
        assertNotNull(lookup);
        assertEquals(diagnosis, lookup.getCode());
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
    public static void checkInvoice(Invoice invoice, long id, BigDecimal discount,
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
    public static void checkItem(Item item, long id, Date date, Product product, BigDecimal discount,
                                 BigDecimal discountTax, BigDecimal tax, BigDecimal total) {
        assertEquals(id, item.getId());
        assertEquals(date, item.getDate());
        assertEquals(product, item.getProduct());
        checkEquals(discount, item.getDiscount());
        checkEquals(discountTax, item.getDiscountTax());
        checkEquals(tax, item.getTotalTax());
        checkEquals(total, item.getTotal());
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
    public static Note checkNote(Note note, Date date, User clinician, String text, int notes) {
        assertEquals(date, note.getDate());
        assertEquals(clinician, note.getClinician());
        assertEquals(text, note.getText());
        assertEquals(notes, note.getNotes().size());
        return note;
    }


}
