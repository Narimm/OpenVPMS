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

package org.openvpms.archetype.function.party;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.JXPathContext;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.contact.BasicAddressFormatter;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.finance.estimate.EstimateTestHelper;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceFunctions;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.jxpath.ObjectFunctions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.openvpms.archetype.rules.math.MathRules.ONE_POUND_IN_GRAMS;
import static org.openvpms.archetype.rules.math.MathRules.ONE_POUND_IN_KILOS;
import static org.openvpms.archetype.rules.math.MathRules.ONE_THOUSAND;
import static org.openvpms.archetype.rules.util.DateUnits.HOURS;
import static org.openvpms.archetype.rules.util.DateUnits.MONTHS;
import static org.openvpms.archetype.rules.util.DateUnits.YEARS;

/**
 * Tests the {@link PartyFunctions} class.
 *
 * @author Tim Anderson
 */
public class PartyFunctionsTestCase extends ArchetypeServiceTest {

    /**
     * Test customer.
     */
    private Party customer;

    /**
     * Test patient.
     */
    private Party patient;

    /**
     * Estimate linked to the customer.
     */
    private Act estimate;

    /**
     * Item linked to the patient.
     */
    private Act item;

    @Before
    public void setUp() {
        customer = TestHelper.createCustomer("Foo", "Bar", false, true);
        IMObjectBean bean = new IMObjectBean(customer);
        bean.setValue("title", TestHelper.getLookup("lookup.personTitle", "MR").getCode());
        patient = TestHelper.createPatient(customer);
        User author = TestHelper.createUser();
        item = EstimateTestHelper.createEstimateItem(patient, TestHelper.createProduct(), author, BigDecimal.TEN);
        estimate = EstimateTestHelper.createEstimate(customer, author, item);
    }

    /**
     * Tests the {@link PartyFunctions#getPartyFullName(Party)}, {@link PartyFunctions#getPartyFullName(Act)} and
     * {@link PartyFunctions#getPartyFullName(ExpressionContext)} methods.
     */
    @Test
    public void testGetPartyFullName() {
        JXPathContext context1 = createContext(customer);
        JXPathContext context2 = createContext(estimate); // verifies the customer is accessed from the Act
        JXPathContext context3 = createContext(item);     // verifies the customer is accessed from the patient owner

        // test the ExpressionContext form
        assertEquals("Mr Foo Bar", context1.getValue("party:getPartyFullName()"));
        assertEquals("Mr Foo Bar", context2.getValue("party:getPartyFullName()"));
        assertEquals("Mr Foo Bar", context3.getValue("party:getPartyFullName()"));

        // test the Party version
        assertEquals("Mr Foo Bar", context1.getValue("party:getPartyFullName(.)"));

        // test the Act version
        assertEquals("Mr Foo Bar", context2.getValue("party:getPartyFullName(.)"));
        assertEquals("Mr Foo Bar", context3.getValue("party:getPartyFullName(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getPatientOwner(Party)}, {@link PartyFunctions#getPatientOwner(Act)} and
     * {@link PartyFunctions#getPatientOwner(ExpressionContext)} methods.
     */
    @Test
    public void testGetPatientOwner() {
        JXPathContext context1 = createContext(patient);
        JXPathContext context2 = createContext(item); // verifies the patient is accessed from the Act

        // test the ExpressionContext form
        assertEquals(customer, context1.getValue("party:getPatientOwner()"));
        assertEquals(customer, context2.getValue("party:getPatientOwner()"));

        // test the Party version
        assertEquals(customer, context1.getValue("party:getPatientOwner(.)"));

        // test the Act version
        assertEquals(customer, context2.getValue("party:getPatientOwner(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getTelephone(Party)} and {@link PartyFunctions#getTelephone(Act)} methods.
     */
    @Test
    public void testGetTelephone() {
        JXPathContext context1 = createContext(customer);
        JXPathContext context2 = createContext(estimate);
        JXPathContext context3 = createContext(item);
        assertEquals("", context1.getValue("party:getTelephone(.)"));
        assertEquals("", context2.getValue("party:getTelephone(.)"));
        assertEquals("", context3.getValue("party:getTelephone(.)"));

        customer.addContact(createPhone("12345", false, "HOME"));
        customer.addContact(createPhone("45678", true, null));  // preferred
        save(customer);
        assertEquals("(03) 45678", context1.getValue("party:getTelephone(.)"));
        assertEquals("(03) 45678", context2.getValue("party:getTelephone(.)"));
        assertEquals("(03) 45678", context3.getValue("party:getTelephone(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getHomeTelephone(Party)} and {@link PartyFunctions#getHomeTelephone(Act)}
     * methods.
     */
    @Test
    public void testGetHomeTelephone() {
        JXPathContext context1 = createContext(customer);
        JXPathContext context2 = createContext(estimate);
        JXPathContext context3 = createContext(item);
        assertEquals("", context1.getValue("party:getHomeTelephone(.)"));
        assertEquals("", context2.getValue("party:getHomeTelephone(.)"));
        assertEquals("", context3.getValue("party:getHomeTelephone(.)"));

        Contact home = createPhone("12345", true, "HOME");
        customer.addContact(home);
        save(customer);
        assertEquals("(03) 12345", context1.getValue("party:getHomeTelephone(.)"));
        assertEquals("(03) 12345", context2.getValue("party:getHomeTelephone(.)"));
        assertEquals("(03) 12345", context3.getValue("party:getHomeTelephone(.)"));

        // remove the home contact
        customer.removeContact(home);
        save(customer);
        assertEquals("", context1.getValue("party:getHomeTelephone(.)"));
        assertEquals("", context2.getValue("party:getHomeTelephone(.)"));
        assertEquals("", context3.getValue("party:getHomeTelephone(.)"));

        // add a work contact, and verify it is returned. See OVPMS-718
        customer.addContact(createPhone("56789", true, "WORK"));
        save(customer);

        assertEquals("(03) 56789", context1.getValue("party:getHomeTelephone(.)"));
        assertEquals("(03) 56789", context2.getValue("party:getHomeTelephone(.)"));
        assertEquals("(03) 56789", context3.getValue("party:getHomeTelephone(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getWorkTelephone(Party)} and {@link PartyFunctions#getWorkTelephone(Act)}
     * methods.
     */
    @Test
    public void testGetWorkTelephone() {
        JXPathContext context1 = createContext(customer);
        JXPathContext context2 = createContext(estimate);
        JXPathContext context3 = createContext(item);

        assertEquals("", context1.getValue("party:getWorkTelephone(.)"));
        assertEquals("", context2.getValue("party:getWorkTelephone(.)"));
        assertEquals("", context3.getValue("party:getWorkTelephone(.)"));

        customer.addContact(createPhone("56789", true, "WORK"));
        save(customer);
        assertEquals("(03) 56789", context1.getValue("party:getWorkTelephone(.)"));
        assertEquals("(03) 56789", context2.getValue("party:getWorkTelephone(.)"));
        assertEquals("(03) 56789", context3.getValue("party:getWorkTelephone(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getBillingAddress(Party)},
     * {@link PartyFunctions#getBillingAddress(Act, boolean)},  {@link PartyFunctions#getBillingAddress(Act)},
     * {@link PartyFunctions#getBillingAddress(Act, boolean)} and
     * {@link PartyFunctions#getBillingAddress(ExpressionContext)} methods.
     */
    @Test
    public void testGetBillingAddress() {
        JXPathContext context1 = createContext(customer);
        JXPathContext context2 = createContext(estimate);
        JXPathContext context3 = createContext(item);

        assertEquals("", context1.getValue("party:getBillingAddress(.)"));  // party
        assertEquals("", context2.getValue("party:getBillingAddress(.)"));  // act -> customer
        assertEquals("", context3.getValue("party:getBillingAddress(.)"));  // act -> patient -> patient owner

        // test the ExpressionContext form
        assertEquals("", context1.getValue("party:getBillingAddress()"));
        assertEquals("", context2.getValue("party:getBillingAddress()"));
        assertEquals("", context3.getValue("party:getBillingAddress()"));

        Contact home = TestHelper.createLocationContact("123 4th Avenue", "SAWTELL", "NSW", "2452");
        home.addClassification(TestHelper.getLookup("lookup.contactPurpose", "BILLING"));
        home.addClassification(TestHelper.getLookup("lookup.contactPurpose", "HOME"));
        Contact work = TestHelper.createLocationContact("456 Main Rd", "SAWTELL", "NSW", "2452");
        work.addClassification(TestHelper.getLookup("lookup.contactPurpose", "WORK"));
        customer.addContact(home);
        customer.addContact(work);
        save(customer);

        assertEquals("123 4th Avenue, Sawtell Nsw 2452", context1.getValue("party:getBillingAddress(., true())"));
        assertEquals("123 4th Avenue\nSawtell Nsw 2452", context1.getValue("party:getBillingAddress(., false())"));
        assertEquals("123 4th Avenue, Sawtell Nsw 2452", context2.getValue("party:getBillingAddress(., true())"));
        assertEquals("123 4th Avenue\nSawtell Nsw 2452", context2.getValue("party:getBillingAddress(., false())"));
        assertEquals("123 4th Avenue, Sawtell Nsw 2452", context3.getValue("party:getBillingAddress(., true())"));
        assertEquals("123 4th Avenue\nSawtell Nsw 2452", context3.getValue("party:getBillingAddress(., false())"));

        // test the ExpressionContext form
        assertEquals("123 4th Avenue\nSawtell Nsw 2452", context1.getValue("party:getBillingAddress()"));
        assertEquals("123 4th Avenue\nSawtell Nsw 2452", context2.getValue("party:getBillingAddress()"));
        assertEquals("123 4th Avenue\nSawtell Nsw 2452", context3.getValue("party:getBillingAddress()"));
    }

    /**
     * Tests the {@link PartyFunctions#getCorrespondenceAddress(Party)},
     * {@link PartyFunctions#getCorrespondenceAddress(Act, boolean)},
     * {@link PartyFunctions#getCorrespondenceAddress(Act)},
     * {@link PartyFunctions#getCorrespondenceAddress(Act, boolean)} and
     * {@link PartyFunctions#getCorrespondenceAddress(ExpressionContext)} methods.
     */
    @Test
    public void testGetCorrespondenceAddress() {
        JXPathContext context1 = createContext(customer);
        JXPathContext context2 = createContext(estimate);
        JXPathContext context3 = createContext(item);

        assertEquals("", context1.getValue("party:getCorrespondenceAddress(.)"));  // party
        assertEquals("", context2.getValue("party:getCorrespondenceAddress(.)"));  // act -> customer
        assertEquals("", context3.getValue("party:getCorrespondenceAddress(.)"));  // act -> patient -> patient owner

        // test the ExpressionContext form
        assertEquals("", context1.getValue("party:getCorrespondenceAddress()"));
        assertEquals("", context2.getValue("party:getCorrespondenceAddress()"));
        assertEquals("", context3.getValue("party:getCorrespondenceAddress()"));

        Contact home = TestHelper.createLocationContact("123 4th Avenue", "SAWTELL", "NSW", "2452");
        home.addClassification(TestHelper.getLookup("lookup.contactPurpose", "CORRESPONDENCE"));
        home.addClassification(TestHelper.getLookup("lookup.contactPurpose", "HOME"));
        Contact work = TestHelper.createLocationContact("456 Main Rd", "SAWTELL", "NSW", "2452");
        work.addClassification(TestHelper.getLookup("lookup.contactPurpose", "WORK"));
        customer.addContact(home);
        customer.addContact(work);
        save(customer);

        assertEquals("123 4th Avenue, Sawtell Nsw 2452",
                     context1.getValue("party:getCorrespondenceAddress(., true())"));
        assertEquals("123 4th Avenue\nSawtell Nsw 2452",
                     context1.getValue("party:getCorrespondenceAddress(., false())"));
        assertEquals("123 4th Avenue, Sawtell Nsw 2452",
                     context2.getValue("party:getCorrespondenceAddress(., true())"));
        assertEquals("123 4th Avenue\nSawtell Nsw 2452",
                     context2.getValue("party:getCorrespondenceAddress(., false())"));
        assertEquals("123 4th Avenue, Sawtell Nsw 2452",
                     context3.getValue("party:getCorrespondenceAddress(., true())"));
        assertEquals("123 4th Avenue\nSawtell Nsw 2452",
                     context3.getValue("party:getCorrespondenceAddress(., false())"));

        // test the ExpressionContext form
        assertEquals("123 4th Avenue\nSawtell Nsw 2452", context1.getValue("party:getCorrespondenceAddress()"));
        assertEquals("123 4th Avenue\nSawtell Nsw 2452", context2.getValue("party:getCorrespondenceAddress()"));
        assertEquals("123 4th Avenue\nSawtell Nsw 2452", context3.getValue("party:getCorrespondenceAddress()"));
    }

    /**
     * Tests the {@link PartyFunctions#getPracticeAddress()} and {@link PartyFunctions#getPracticeAddress(boolean)}
     * methods.
     */
    @Test
    public void testGetPracticeAddress() {
        JXPathContext context = createContext(new IMObject());
        Party practice = TestHelper.getPractice();
        practice.getContacts().clear();
        practice.addContact(TestHelper.createLocationContact("123 Main Rd", "ELTHAM", "VIC", "3095"));
        save(practice);

        assertEquals("123 Main Rd, Eltham Vic 3095", context.getValue("party:getPracticeAddress()"));
        assertEquals("123 Main Rd, Eltham Vic 3095", context.getValue("party:getPracticeAddress(true())"));
        assertEquals("123 Main Rd\nEltham Vic 3095", context.getValue("party:getPracticeAddress(false())"));
    }

    /**
     * Tests the {@link PartyFunctions#identities(ExpressionContext)} method.
     */
    @Test
    public void testIdentities() {
        Party party = TestHelper.createPatient(false);

        JXPathContext ctx = createContext(party);
        assertEquals("", ctx.getValue("party:identities()"));

        String tag = "1234567";
        party.addIdentity(createPetTag(tag));
        assertEquals("Pet Tag: " + tag, ctx.getValue("party:identities()"));
    }

    /**
     * Tests the {@link PartyFunctions#identities(Party)} method.
     */
    @Test
    public void testIdentitiesForParty() {
        Act act = (Act) create("act.customerEstimationItem");
        Party party = TestHelper.createPatient();

        JXPathContext ctx = createContext(act);

        assertEquals("", ctx.getValue("party:identities(openvpms:get(., 'patient.entity'))"));
        String tag = "1234567";
        party.addIdentity(createPetTag(tag));
        save(party);

        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("patient", party);

        assertEquals("Pet Tag: " + tag, ctx.getValue("party:identities(openvpms:get(., 'patient.entity'))"));
    }


    /**
     * Tests the {@link PartyFunctions#getPatientMicrochip(Party)} method.
     */
    @Test
    public void testGetPatientMicrochip() {
        Party patient = TestHelper.createPatient(false);
        JXPathContext ctx = createContext(patient);

        assertEquals("", ctx.getValue("party:getPatientMicrochip(.)"));

        EntityIdentity microchip = (EntityIdentity) create(
                "entityIdentity.microchip");
        IMObjectBean tagBean = new IMObjectBean(microchip);
        tagBean.setValue("microchip", "1234567");
        patient.addIdentity(microchip);

        assertEquals("1234567", ctx.getValue("party:getPatientMicrochip(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getPatientMicrochips(Party)} method.
     */
    @Test
    public void testGetPatientMicrochips() {
        Party patient = TestHelper.createPatient(false);
        JXPathContext ctx = createContext(patient);

        assertEquals("", ctx.getValue("party:getPatientMicrochips(.)"));

        EntityIdentity microchip1 = (EntityIdentity) create("entityIdentity.microchip");
        IMObjectBean tagBean = new IMObjectBean(microchip1);
        tagBean.setValue("microchip", "123");
        patient.addIdentity(microchip1);
        save(patient);

        assertEquals("123", ctx.getValue("party:getPatientMicrochips(.)"));

        EntityIdentity microchip2 = (EntityIdentity) create("entityIdentity.microchip");
        tagBean = new IMObjectBean(microchip2);
        tagBean.setValue("microchip", "456");
        patient.addIdentity(microchip2);
        save(patient);

        assertEquals("456, 123", ctx.getValue("party:getPatientMicrochips(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getWeight(Party)} and {@link PartyFunctions#getWeight(Party, String)} methods.
     */
    @Test
    public void testGetWeight() {
        Party patient = TestHelper.createPatient();
        JXPathContext ctx = createContext(patient);
        assertEquals(ZERO, ctx.getValue("party:getWeight(.)"));

        Act weight1 = PatientTestHelper.createWeight(patient, ONE, WeightUnits.KILOGRAMS);
        checkEquals(ONE, (BigDecimal) ctx.getValue("party:getWeight(.)"));
        checkEquals(ONE, (BigDecimal) ctx.getValue("party:getWeight(., 'KILOGRAMS')"));
        checkEquals(ONE_THOUSAND, (BigDecimal) ctx.getValue("party:getWeight(., 'GRAMS')"));
        assertEquals(new BigDecimal("2.20462262"), ctx.getValue("party:getWeight(., 'POUNDS')"));

        remove(weight1);
        Act weight2 = PatientTestHelper.createWeight(patient, ONE_THOUSAND, WeightUnits.GRAMS);
        checkEquals(ONE, (BigDecimal) ctx.getValue("party:getWeight(.)"));
        checkEquals(ONE, (BigDecimal) ctx.getValue("party:getWeight(., 'KILOGRAMS')"));
        checkEquals(ONE_THOUSAND, (BigDecimal) ctx.getValue("party:getWeight(., 'GRAMS')"));
        assertEquals(new BigDecimal("2.20462262"), ctx.getValue("party:getWeight(., 'POUNDS')"));

        remove(weight2);

        PatientTestHelper.createWeight(patient, ONE, WeightUnits.POUNDS);
        checkEquals(ONE_POUND_IN_KILOS, (BigDecimal) ctx.getValue("party:getWeight(.)"));
        checkEquals(ONE_POUND_IN_KILOS, (BigDecimal) ctx.getValue("party:getWeight(., 'KILOGRAMS')"));
        checkEquals(ONE_POUND_IN_GRAMS, (BigDecimal) ctx.getValue("party:getWeight(., 'GRAMS')"));
        assertEquals(ONE, ctx.getValue("party:getWeight(., 'POUNDS')"));
    }

    /**
     * Tests the {@link PartyFunctions#getWeight(Act)} and {@link PartyFunctions#getWeight(Act, String)} methods.
     */
    @Test
    public void testActGetWeight() {
        Party patient = TestHelper.createPatient();
        Act visit = (Act) create(PatientArchetypes.CLINICAL_EVENT);
        ActBean bean = new ActBean(visit);
        bean.addNodeParticipation("patient", patient);

        JXPathContext ctx = createContext(visit);
        assertEquals(ZERO, ctx.getValue("party:getWeight(.)"));

        Act weight1 = PatientTestHelper.createWeight(patient, ONE, WeightUnits.KILOGRAMS);
        checkEquals(ONE, (BigDecimal) ctx.getValue("party:getWeight(.)"));
        checkEquals(ONE, (BigDecimal) ctx.getValue("party:getWeight(., 'KILOGRAMS')"));
        checkEquals(ONE_THOUSAND, (BigDecimal) ctx.getValue("party:getWeight(., 'GRAMS')"));
        assertEquals(new BigDecimal("2.20462262"), ctx.getValue("party:getWeight(., 'POUNDS')"));

        remove(weight1);
        Act weight2 = PatientTestHelper.createWeight(patient, ONE_THOUSAND, WeightUnits.GRAMS);
        checkEquals(ONE, (BigDecimal) ctx.getValue("party:getWeight(.)"));
        checkEquals(ONE, (BigDecimal) ctx.getValue("party:getWeight(., 'KILOGRAMS')"));
        checkEquals(ONE_THOUSAND, (BigDecimal) ctx.getValue("party:getWeight(., 'GRAMS')"));
        assertEquals(new BigDecimal("2.20462262"), ctx.getValue("party:getWeight(., 'POUNDS')"));

        remove(weight2);

        PatientTestHelper.createWeight(patient, ONE, WeightUnits.POUNDS);
        checkEquals(ONE_POUND_IN_KILOS, (BigDecimal) ctx.getValue("party:getWeight(.)"));
        checkEquals(ONE_POUND_IN_KILOS, (BigDecimal) ctx.getValue("party:getWeight(., 'KILOGRAMS')"));
        checkEquals(ONE_POUND_IN_GRAMS, (BigDecimal) ctx.getValue("party:getWeight(., 'GRAMS')"));
        assertEquals(ONE, ctx.getValue("party:getWeight(., 'POUNDS')"));
    }

    /**
     * Tests the {@link PartyFunctions#getPatientMicrochip(Act)} and {@link PartyFunctions#getMicrochip(Act)}
     * methods.
     */
    @Test
    public void testActGetPatientMicrochip() {
        Act act = (Act) create("act.customerEstimation");
        Party patient = TestHelper.createPatient(false);

        JXPathContext ctx = createContext(act);
        assertEquals("", ctx.getValue("party:getPatientMicrochip(.)"));
        assertNull(ctx.getValue("party:getMicrochip(.)"));

        EntityIdentity microchip = (EntityIdentity) create("entityIdentity.microchip");
        IMObjectBean tagBean = new IMObjectBean(microchip);
        tagBean.setValue("microchip", "1234567");
        patient.addIdentity(microchip);
        save(patient);

        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.patient", patient);

        assertEquals("1234567", ctx.getValue("party:getPatientMicrochip(.)"));
        assertEquals(microchip, ctx.getValue("party:getMicrochip(.)"));
        assertEquals("1234567", ctx.getValue("openvpms:get(party:getMicrochip(.), 'microchip')"));
    }

    /**
     * Tests the {@link PartyFunctions#getPatientReferralVet(Act)} and
     * {@link PartyFunctions#getPatientReferralVet(ExpressionContext)} methods.
     */
    @Test
    public void testActGetPatientReferralVet() {
        Party patient = TestHelper.createPatient();
        Party vet = TestHelper.createSupplierVet();
        EntityBean bean = new EntityBean(patient);
        bean.addRelationship(PatientArchetypes.REFERRED_FROM, vet);
        save(patient, vet);
        Act act = (Act) create("act.customerEstimationItem");

        JXPathContext ctx = createContext(act);
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

        JXPathContext ctx = createContext(patient);

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
        Party patient = TestHelper.createPatient();
        Party vet = TestHelper.createSupplierVet();
        Party practice = TestHelper.createSupplierVetPractice();

        // create relationships between the patient, vet, and vet practice
        EntityBean bean = new EntityBean(patient);
        bean.addRelationship(PatientArchetypes.REFERRED_FROM, vet);
        EntityBean practiceBean = new EntityBean(practice);
        practiceBean.addNodeRelationship("veterinarians", vet);
        save(patient, vet, practice);

        Act act = (Act) create("act.customerEstimationItem");
        JXPathContext ctx = createContext(act);

        // verify that if the patient can't be resolved, null is returned
        assertNull(ctx.getValue("party:getPatientReferralVetPractice()"));
        // invokes getPatientReferralVetPractice(ExpressionContext)

        assertNull(ctx.getValue("party:getPatientReferralVetPractice(.)"));
        // invokes getPatientReferralVetPractice(Act)

        // add the patient to the act, and verify the practice can be retrieved
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

        JXPathContext ctx = createContext(patient);

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
     * Tests the {@link PartyFunctions#getAccountBalance} methods.
     */
    @Test
    public void testGetAccountBalance() {
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient(customer);
        BigDecimal total = BigDecimal.valueOf(100);

        JXPathContext ctx1 = createContext(customer);
        checkEquals(BigDecimal.ZERO, (BigDecimal) ctx1.getValue("party:getAccountBalance(.)"));

        List<FinancialAct> invoice = FinancialTestHelper.createChargesInvoice(
                total, customer, patient, TestHelper.createProduct(), ActStatus.POSTED);
        save(invoice);

        checkEquals(total, (BigDecimal) ctx1.getValue("party:getAccountBalance(.)"));

        JXPathContext ctx2 = createContext(invoice.get(1));
        checkEquals(total, (BigDecimal) ctx2.getValue("party:getAccountBalance(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getPatientRabiesTag(Party) and {@link PartyFunctions#getPatientRabiesTag(Act)}}
     * methods.
     */
    @Test
    public void testGetPatientRabiesTag() {
        Party patient = TestHelper.createPatient();
        Act visit = (Act) create(PatientArchetypes.CLINICAL_EVENT);
        ActBean bean = new ActBean(visit);
        bean.addNodeParticipation("patient", patient);

        JXPathContext patientCtx = createContext(patient);
        JXPathContext visitCtx = createContext(visit);
        assertEquals("", patientCtx.getValue("party:getPatientRabiesTag(.)"));
        assertEquals("", visitCtx.getValue("party:getPatientRabiesTag(.)"));

        EntityIdentity tag = (EntityIdentity) create("entityIdentity.rabiesTag");
        String identity = "1234567890";
        tag.setIdentity(identity);
        patient.addIdentity(tag);
        getArchetypeService().save(patient, false);  // need to disabled validation as rabies tags are not enabled

        assertEquals(identity, patientCtx.getValue("party:getPatientRabiesTag(.)"));
        assertEquals(identity, visitCtx.getValue("party:getPatientRabiesTag(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getWebsite(Party)} method.
     */
    @Test
    public void testGetWebsite() {
        Party customer = TestHelper.createCustomer(false);
        JXPathContext context = createContext(customer);

        assertEquals("", context.getValue("party:getWebsite(.)"));

        Contact contact = (Contact) create(ContactArchetypes.WEBSITE);
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("url", "http://wwww.openvpms.org");
        customer.addContact(contact);

        assertEquals("http://wwww.openvpms.org", context.getValue("party:getWebsite(.)"));
    }

    /**
     * Tests the {@link PartyFunctions#getLetterheadContacts(Party)} method.
     */
    @Test
    public void testGetLetterheadContacts() {
        Party location1 = TestHelper.createLocation();
        Party location2 = TestHelper.createLocation();

        JXPathContext context = createContext(new IMObject());
        context.getVariables().declareVariable("location", null);
        assertNull(context.getValue("party:getLetterheadContacts($location)"));

        context.getVariables().declareVariable("location", location1);
        assertEquals(location1, context.getValue("party:getLetterheadContacts($location)"));

        Entity letterhead = (Entity) create("entity.letterhead");
        EntityBean bean = new EntityBean(letterhead);
        bean.setValue("name", "Z Test Letterhead");
        bean.setValue("logoFile", "logo.png");
        EntityBean locationBean = new EntityBean(location1);
        locationBean.addNodeTarget("letterhead", letterhead);
        save(location1, letterhead);
        assertEquals(location1, context.getValue("party:getLetterheadContacts($location)"));

        bean.addNodeTarget("contacts", location2);
        bean.save();
        assertEquals(location2, context.getValue("party:getLetterheadContacts($location)"));
    }

    /**
     * Tests the {@link PartyFunctions#getAppointments(Party, int, String)} method.
     */
    @Test
    public void testGetAppointments() {
        Party customer1 = TestHelper.createCustomer();
        Party patient1 = TestHelper.createPatient(customer1);
        Party customer2 = TestHelper.createCustomer();
        Party patient2 = TestHelper.createPatient();
        Party location = TestHelper.createLocation();
        Entity schedule = ScheduleTestHelper.createSchedule(location);
        Date now = new Date();
        Act act1a = createAppointment(schedule, customer1, patient1, DateRules.getDate(now, -1, HOURS));
        Act act1b = createAppointment(schedule, customer1, patient1, DateRules.getDate(now, 6, MONTHS));
        Act act1c = createAppointment(schedule, customer1, null, DateRules.getDate(now, 9, MONTHS));
        Act act1d = createAppointment(schedule, customer1, patient1, DateRules.getDate(now, 2, YEARS));
        Act act2a = createAppointment(schedule, customer2, patient2, DateRules.getDate(now, -1, YEARS));
        Act act2b = createAppointment(schedule, customer2, patient2, DateRules.getDate(now, 1, MONTHS));
        Act act2c = createAppointment(schedule, customer2, patient2, DateRules.getDate(now, 6, MONTHS));
        act2b.setStatus(AppointmentStatus.CANCELLED);
        save(act1a, act1b, act1c, act1d, act2a, act2b, act2c);

        checkAppointments(customer1, "party:getAppointments(., 1, 'YEARS')", act1b, act1c);
        checkAppointments(patient1, "party:getAppointments(., 3, 'YEARS')", act1b, act1d);
        checkAppointments(customer2, "party:getAppointments(., 1, 'YEARS')", act2c);
    }

    /**
     * Tests the {@link PartyFunctions#getAddress(Party, String)}
     * and {@link PartyFunctions#getAddress(Party, String, boolean)} methods.
     */
    @Test
    public void testGetAddress() {
        Party customer = (Party) create(CustomerArchetypes.PERSON);
        Lookup billing = TestHelper.getLookup("lookup.contactPurpose", "BILLING");
        Lookup shipping = TestHelper.getLookup("lookup.contactPurpose", "SHIPPING");
        Contact home = TestHelper.createLocationContact("123 Main Rd", "KONGWAK", "VIC", "3058");
        home.addClassification(billing);
        Contact work = TestHelper.createLocationContact("456 Smith St", "WONTHAGGI", "VIC", "3058");
        work.addClassification(shipping);
        IMObjectBean bean = new IMObjectBean(work);
        bean.setValue("preferred", false);
        customer.addContact(home);
        customer.addContact(work);

        JXPathContext context = createContext(customer);
        assertEquals("123 Main Rd\nKongwak Vic 3058", context.getValue("party:getAddress(., 'BILLING')"));
        assertEquals("123 Main Rd\nKongwak Vic 3058", context.getValue("party:getAddress(., 'BILLING', false())"));
        assertEquals("123 Main Rd, Kongwak Vic 3058", context.getValue("party:getAddress(., 'BILLING', true())"));
        assertEquals("456 Smith St\nWonthaggi Vic 3058", context.getValue("party:getAddress(., 'SHIPPING')"));
        assertEquals("123 Main Rd\nKongwak Vic 3058", context.getValue("party:getAddress(., 'NO_SUCH_PURPOSE')"));
    }

    /**
     * Helper to create a pending 15 minute appointment.
     *
     * @param schedule  the schedule
     * @param customer  the customer
     * @param patient   the patient
     * @param startTime the appointment start time
     * @return a new appointment
     */
    private Act createAppointment(Entity schedule, Party customer, Party patient, Date startTime) {
        return ScheduleTestHelper.createAppointment(startTime, schedule, customer, patient, AppointmentStatus.PENDING);
    }

    /**
     * Verifies that the results of an party:getAppointments(...) call match that expected.
     *
     * @param party      the context party
     * @param expression the expression to invoke
     * @param expected   the expected results
     */
    @SuppressWarnings("unchecked")
    private void checkAppointments(Party party, String expression, Act... expected) {
        JXPathContext context = createContext(party);
        List<Act> result = new ArrayList<>();
        CollectionUtils.addAll(result, (Iterable<Act>) context.getValue(expression));
        assertEquals(expected.length, result.size());
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result.get(i));
        }
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
            Lookup lookup = TestHelper.getLookup(ContactArchetypes.PURPOSE, purpose);
            contact.addClassification(lookup);
        }
        return contact;
    }


    /**
     * Helper to create a pet tag.
     *
     * @param tag the tag value
     * @return a new pet tag
     */
    private EntityIdentity createPetTag(String tag) {
        EntityIdentity result = (EntityIdentity) create("entityIdentity.petTag");
        IMObjectBean tagBean = new IMObjectBean(result);
        tagBean.setValue("petTag", tag);
        return result;
    }

    /**
     * Creates a new JXPathContext, with the party functions registered.
     *
     * @param object the context object
     * @return a new JXPathContext
     */
    private JXPathContext createContext(IMObject object) {
        IArchetypeService service = getArchetypeService();
        ILookupService lookups = getLookupService();
        ArchetypeServiceFunctions functions = new ArchetypeServiceFunctions(service, lookups);
        PartyFunctions partyFunctions = new PartyFunctions(service, lookups, new PatientRules(null, service, lookups),
                                                           new BasicAddressFormatter(service, lookups));
        FunctionLibrary library = new FunctionLibrary();
        library.addFunctions(new ObjectFunctions(functions, "openvpms"));
        library.addFunctions(new ObjectFunctions(partyFunctions, "party"));
        return JXPathHelper.newContext(object, library);
    }

}
