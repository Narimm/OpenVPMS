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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.function.party;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.JXPathContext;
import org.junit.Test;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.jxpath.JXPathHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests the {@link PartyFunctions} class.
 *
 * @author Tim Anderson
 */
public class PartyFunctionsTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link PartyFunctions#getTelephone(Party)} method.
     */
    @Test
    public void testGetTelephone() {
        Party party = TestHelper.createCustomer(false);
        party.getContacts().clear(); // remove all contacts

        JXPathContext ctx = JXPathHelper.newContext(party);
        assertEquals("", ctx.getValue("party:getTelephone(.)"));

        party.addContact(createPhone("12345", false, "HOME"));
        party.addContact(createPhone("45678", true, null));  // preferred
        assertEquals("(03) 45678", ctx.getValue("party:getTelephone(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getTelephone(Act)} method.
     */
    @Test
    public void testActGetTelephone() {
        Act act = (Act) create("act.customerEstimation");
        Party party = TestHelper.createCustomer();
        party.getContacts().clear(); // remove all contacts
        save(party);

        JXPathContext ctx = JXPathHelper.newContext(act);
        assertEquals("", ctx.getValue("party:getTelephone(.)"));

        party.addContact(createPhone("12345", false, "HOME"));
        party.addContact(createPhone("45678", true, null));  // preferred
        save(party);

        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.customer", party);

        assertEquals("(03) 45678", ctx.getValue("party:getTelephone(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getHomeTelephone(Party)} method.
     */
    @Test
    public void testGetHomeTelephone() {
        Party party = TestHelper.createCustomer(false);

        JXPathContext ctx = JXPathHelper.newContext(party);
        assertEquals("", ctx.getValue("party:getHomeTelephone(.)"));

        party.addContact(createPhone("12345", true, "HOME"));
        assertEquals("(03) 12345", ctx.getValue("party:getHomeTelephone(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getHomeTelephone(Act)} method.
     */
    @Test
    public void testActGetHomeTelephone() {
        Act act = (Act) create("act.customerEstimation");
        Party party = TestHelper.createCustomer();

        JXPathContext ctx = JXPathHelper.newContext(act);
        assertEquals("", ctx.getValue("party:getHomeTelephone(.)"));

        party.addContact(createPhone("12345", true, "HOME"));
        save(party);

        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.customer", party);

        assertEquals("(03) 12345", ctx.getValue("party:getHomeTelephone(.)"));
        assertEquals("", ctx.getValue("party:getWorkTelephone(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getWorkTelephone(Party)} method.
     */
    @Test
    public void testGetWorkTelephone() {
        Party party = TestHelper.createCustomer();
        party.getContacts().clear();

        JXPathContext ctx = JXPathHelper.newContext(party);
        assertEquals("", ctx.getValue("party:getWorkTelephone(.)"));

        party.addContact(createPhone("56789", true, "WORK"));
        assertEquals("(03) 56789", ctx.getValue("party:getWorkTelephone(.)"));
        assertEquals("(03) 56789", ctx.getValue("party:getHomeTelephone(.)")); // OVPMS-718
    }

    /**
     * Tests the {@link PartyFunctions#getWorkTelephone(Act)} method.
     */
    @Test
    public void testActGetWorkTelephone() {
        Act act = (Act) create("act.customerEstimation");
        Party party = TestHelper.createCustomer();
        party.getContacts().clear();

        JXPathContext ctx = JXPathHelper.newContext(act);
        assertEquals("", ctx.getValue("party:getWorkTelephone(.)"));

        party.addContact(createPhone("56789", true, "WORK"));
        save(party);

        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.customer", party);

        assertEquals("(03) 56789", ctx.getValue("party:getWorkTelephone(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getPatientMicrochip(Party)} method.
     */
    @Test
    public void testGetPatientMicrochip() {
        Party patient = TestHelper.createPatient(false);
        JXPathContext ctx = JXPathHelper.newContext(patient);

        assertEquals("", ctx.getValue("party:getPatientMicrochip(.)"));

        EntityIdentity microchip = (EntityIdentity) create(
                "entityIdentity.microchip");
        IMObjectBean tagBean = new IMObjectBean(microchip);
        tagBean.setValue("microchip", "1234567");
        patient.addIdentity(microchip);

        assertEquals("1234567", ctx.getValue("party:getPatientMicrochip(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getPatientMicrochip(Party)} method.
     */
    @Test
    public void testActGetPatientMicrochip() {
        Act act = (Act) create("act.customerEstimation");
        Party patient = TestHelper.createPatient(false);

        JXPathContext ctx = JXPathHelper.newContext(act);
        assertEquals("", ctx.getValue("party:getPatientMicrochip(.)"));

        EntityIdentity microchip = (EntityIdentity) create("entityIdentity.microchip");
        IMObjectBean tagBean = new IMObjectBean(microchip);
        tagBean.setValue("microchip", "1234567");
        patient.addIdentity(microchip);
        save(patient);

        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.patient", patient);

        assertEquals("1234567", ctx.getValue("party:getPatientMicrochip(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getPatientReferralVet(Act)} and
     * {@link PartyFunctions#getPatientReferralVet(ExpressionContext)} methods.
     */
    @Test
    public void testActGetPatientReferralVet() {
        Act act = (Act) create("act.customerEstimationItem");
        Party patient = TestHelper.createPatient();
        Party vet = TestHelper.createSupplierVet();
        EntityBean bean = new EntityBean(patient);
        bean.addRelationship(PatientArchetypes.REFERRED_FROM, vet);
        save(patient, vet);

        JXPathContext ctx = JXPathHelper.newContext(act);
        assertNull(ctx.getValue("party:getPatientReferralVet()"));  // invokes getPatientReferralVet(ExpressionContext)
        assertNull(ctx.getValue("party:getPatientReferralVet(.)")); // invokes getPatientReferralVet(Act)

        ActBean actBean = new ActBean(act);
        actBean.addNodeParticipation("patient", patient);

        assertEquals(vet, ctx.getValue("party:getPatientReferralVet()"));
        assertEquals(vet, ctx.getValue("party:getPatientReferralVet(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getPatientReferralVet(Party)} and
     * {@link PartyFunctions#getPatientReferralVet(ExpressionContext)} methods.
     */
    @Test
    public void testGetPatientReferralVet() {
        Party patient = TestHelper.createPatient();
        Party vet = TestHelper.createSupplierVet();

        JXPathContext ctx = JXPathHelper.newContext(patient);

        // verify that if the patient can't be resolved, null is returned
        assertNull(ctx.getValue("party:getPatientReferralVet()"));  // invokes getPatientReferralVet(ExpressionContext)
        assertNull(ctx.getValue("party:getPatientReferralVet(.)")); // invokes getPatientReferralVet(Party)

        EntityBean bean = new EntityBean(patient);
        bean.addRelationship(PatientArchetypes.REFERRED_TO, vet);
        assertEquals(vet, ctx.getValue("party:getPatientReferralVet()"));
        assertEquals(vet, ctx.getValue("party:getPatientReferralVet(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getPatientReferralVetPractice(Act)} and
     * {@link PartyFunctions#getPatientReferralVetPractice(ExpressionContext)} methods.
     */
    @Test
    public void testActGetPatientReferralVetPractice() {
        Act act = (Act) create("act.customerEstimationItem");
        Party patient = TestHelper.createPatient();
        Party vet = TestHelper.createSupplierVet();
        Party practice = TestHelper.createSupplierVetPractice();

        // create relationships between the patient, vet, and vet practice
        EntityBean bean = new EntityBean(patient);
        bean.addRelationship(PatientArchetypes.REFERRED_FROM, vet);
        EntityBean practiceBean = new EntityBean(practice);
        practiceBean.addNodeRelationship("veterinarians", vet);
        save(patient, vet, practice);

        JXPathContext ctx = JXPathHelper.newContext(act);

        // verify that if the patient can't be resolved, null is returned
        assertNull(ctx.getValue("party:getPatientReferralVetPractice()"));
        // invokes getPatientReferralVetPractice(ExpressionContext)

        assertNull(ctx.getValue("party:getPatientReferralVetPractice(.)"));
        // invokes getPatientReferralVetPractice(Act)

        // add the patient tto the act, and verify the practice can be retrieved
        ActBean actBean = new ActBean(act);
        actBean.addNodeParticipation("patient", patient);

        assertEquals(practice, ctx.getValue("party:getPatientReferralVetPractice()"));
        assertEquals(practice, ctx.getValue("party:getPatientReferralVetPractice(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getPatientReferralVetPractice(Party)} and
     * {@link PartyFunctions#getPatientReferralVetPractice(ExpressionContext)} methods.
     */
    @Test
    public void testGetPatientReferralVetPractice() {
        Party patient = TestHelper.createPatient();
        Party vet = TestHelper.createSupplierVet();
        Party practice = TestHelper.createSupplierVetPractice();

        JXPathContext ctx = JXPathHelper.newContext(patient);

        // verify that if the vet can't be resolved, null is returned
        assertNull(ctx.getValue("party:getPatientReferralVetPractice()"));
        // invokes getPatientReferralVetPractice(ExpressionContext)

        assertNull(ctx.getValue("party:getPatientReferralVetPractice(.)"));
        // invokes getPatientReferralVetPractice(Party)

        // create relationships between the patient, vet, and vet practice
        EntityBean bean = new EntityBean(patient);
        bean.addRelationship(PatientArchetypes.REFERRED_TO, vet);
        EntityBean practiceBean = new EntityBean(practice);
        practiceBean.addNodeRelationship("veterinarians", vet);
        save(patient, vet, practice);

        // verify the practice can be retrieved
        assertEquals(practice, ctx.getValue("party:getPatientReferralVetPractice()"));
        assertEquals(practice, ctx.getValue("party:getPatientReferralVetPractice(.)"));
    }

    /**
     * Creates a new <em>contact.phoneNumber</em>.
     *
     * @param number    the phone number
     * @param preferred if {@code true}, marks the contact as the preferred contact
     * @param purpose   the contact purpose. May be {@code null}
     * @return a new phone contact
     */
    private Contact createPhone(String number, boolean preferred, String purpose) {
        Contact contact = (Contact) create(ContactArchetypes.PHONE);
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("areaCode", "03");
        bean.setValue("telephoneNumber", number);
        bean.setValue("preferred", preferred);
        if (purpose != null) {
            Lookup lookup = TestHelper.getLookup("lookup.contactPurpose", purpose);
            contact.addClassification(lookup);
        }
        return contact;
    }

}
