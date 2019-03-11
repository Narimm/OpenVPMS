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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
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
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceFunctions;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.jxpath.ObjectFunctions;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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
     * The customer rules.
     */
    @Autowired
    private CustomerRules rules;

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

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        customer = TestHelper.createCustomer("Foo", "Bar", false, true);
        IMObjectBean bean = getBean(customer);
        bean.setValue("title", TestHelper.getLookup("lookup.personTitle", "MR").getCode());
        patient = TestHelper.createPatient(customer);
        User author = TestHelper.createUser();
        item = EstimateTestHelper.createEstimateItem(patient, TestHelper.createProduct(), author, BigDecimal.TEN);
        estimate = EstimateTestHelper.createEstimate(customer, author, item);
    }

    /**
     * Tests the {@link PartyFunctions#getPartyFullName(Object)} and
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

        // null handling
        assertEquals("", context1.getValue("party:getPartyFullName(null)"));
    }

    /**
     * Tests the {@link PartyFunctions#getPatientOwner(Object)} and
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

        // null handling
        assertNull(context1.getValue("party:getPatientOwner(null)"));
    }

    /**
     * Tests the {@link PartyFunctions#getTelephone(Object)} and {@link PartyFunctions#getTelephone(ExpressionContext)}
     * methods.
     */
    @Test
    public void testGetTelephone() {
        JXPathContext context1 = createContext(customer);
        JXPathContext context2 = createContext(estimate);
        JXPathContext context3 = createContext(item);
        assertEquals("", context1.getValue("party:getTelephone(.)"));
        assertEquals("", context2.getValue("party:getTelephone(.)"));
        assertEquals("", context3.getValue("party:getTelephone(.)"));

        // ExpressionContext form
        assertEquals("", context1.getValue("party:getTelephone()"));
        assertEquals("", context2.getValue("party:getTelephone()"));
        assertEquals("", context3.getValue("party:getTelephone()"));

        customer.addContact(createPhone("12345", false, "HOME"));
        customer.addContact(createPhone("45678", true, null));  // preferred
        save(customer);
        assertEquals("(03) 45678", context1.getValue("party:getTelephone(.)"));
        assertEquals("(03) 45678", context2.getValue("party:getTelephone(.)"));
        assertEquals("(03) 45678", context3.getValue("party:getTelephone(.)"));

        // ExpressionContext form
        assertEquals("(03) 45678", context1.getValue("party:getTelephone()"));
        assertEquals("(03) 45678", context2.getValue("party:getTelephone()"));
        assertEquals("(03) 45678", context3.getValue("party:getTelephone()"));

        // null handling
        assertEquals("", context1.getValue("party:getTelephone(null)"));
    }

    /**
     * Tests the {@link PartyFunctions#getHomeTelephone(Object)}
     * and {@link PartyFunctions#getHomeTelephone(ExpressionContext)} methods.
     */
    @Test
    public void testGetHomeTelephone() {
        JXPathContext context1 = createContext(customer);
        JXPathContext context2 = createContext(estimate);
        JXPathContext context3 = createContext(item);
        assertEquals("", context1.getValue("party:getHomeTelephone(.)"));
        assertEquals("", context2.getValue("party:getHomeTelephone(.)"));
        assertEquals("", context3.getValue("party:getHomeTelephone(.)"));

        // ExpressionContext form
        assertEquals("", context1.getValue("party:getHomeTelephone()"));
        assertEquals("", context2.getValue("party:getHomeTelephone()"));
        assertEquals("", context3.getValue("party:getHomeTelephone()"));

        Contact home = createPhone("12345", true, "HOME");
        customer.addContact(home);
        save(customer);
        assertEquals("(03) 12345", context1.getValue("party:getHomeTelephone(.)"));
        assertEquals("(03) 12345", context2.getValue("party:getHomeTelephone(.)"));
        assertEquals("(03) 12345", context3.getValue("party:getHomeTelephone(.)"));

        // ExpressionContext form
        assertEquals("(03) 12345", context1.getValue("party:getHomeTelephone()"));
        assertEquals("(03) 12345", context2.getValue("party:getHomeTelephone()"));
        assertEquals("(03) 12345", context3.getValue("party:getHomeTelephone()"));

        // remove the home contact
        customer.removeContact(home);
        save(customer);
        assertEquals("", context1.getValue("party:getHomeTelephone(.)"));
        assertEquals("", context2.getValue("party:getHomeTelephone(.)"));
        assertEquals("", context3.getValue("party:getHomeTelephone(.)"));

        // ExpressionContext form
        assertEquals("", context1.getValue("party:getHomeTelephone()"));
        assertEquals("", context2.getValue("party:getHomeTelephone()"));
        assertEquals("", context3.getValue("party:getHomeTelephone()"));

        // add a work contact, and verify it is returned. See OVPMS-718
        customer.addContact(createPhone("56789", true, "WORK"));
        save(customer);

        assertEquals("(03) 56789", context1.getValue("party:getHomeTelephone(.)"));
        assertEquals("(03) 56789", context2.getValue("party:getHomeTelephone(.)"));
        assertEquals("(03) 56789", context3.getValue("party:getHomeTelephone(.)"));

        // ExpressionContext form
        assertEquals("(03) 56789", context1.getValue("party:getHomeTelephone()"));
        assertEquals("(03) 56789", context2.getValue("party:getHomeTelephone()"));
        assertEquals("(03) 56789", context3.getValue("party:getHomeTelephone()"));

        // null handling
        assertEquals("", context1.getValue("party:getHomeTelephone(null)"));
    }

    /**
     * Tests the {@link PartyFunctions#getWorkTelephone(Object)} and
     * {@link PartyFunctions#getWorkTelephone(ExpressionContext)} methods.
     */
    @Test
    public void testGetWorkTelephone() {
        JXPathContext context1 = createContext(customer);
        JXPathContext context2 = createContext(estimate);
        JXPathContext context3 = createContext(item);

        assertEquals("", context1.getValue("party:getWorkTelephone(.)"));
        assertEquals("", context2.getValue("party:getWorkTelephone(.)"));
        assertEquals("", context3.getValue("party:getWorkTelephone(.)"));

        // ExpressionContext form
        assertEquals("", context1.getValue("party:getWorkTelephone()"));
        assertEquals("", context2.getValue("party:getWorkTelephone()"));
        assertEquals("", context3.getValue("party:getWorkTelephone()"));

        customer.addContact(createPhone("56789", true, "WORK"));
        save(customer);
        assertEquals("(03) 56789", context1.getValue("party:getWorkTelephone(.)"));
        assertEquals("(03) 56789", context2.getValue("party:getWorkTelephone(.)"));
        assertEquals("(03) 56789", context3.getValue("party:getWorkTelephone(.)"));

        // ExpressionContext form
        assertEquals("(03) 56789", context1.getValue("party:getWorkTelephone()"));
        assertEquals("(03) 56789", context2.getValue("party:getWorkTelephone()"));
        assertEquals("(03) 56789", context3.getValue("party:getWorkTelephone()"));

        // null handling
        assertEquals("", context1.getValue("party:getWorkTelephone(null)"));
    }

    /**
     * Tests the {@link PartyFunctions#getMobileTelephone(Object)} and
     * {@link PartyFunctions#getMobileTelephone(ExpressionContext)} methods.
     */
    @Test
    public void testGetMobileTelephone() {
        JXPathContext context1 = createContext(customer);
        JXPathContext context2 = createContext(estimate);
        JXPathContext context3 = createContext(item);

        assertEquals("", context1.getValue("party:getMobileTelephone(.)"));
        assertEquals("", context2.getValue("party:getMobileTelephone(.)"));
        assertEquals("", context3.getValue("party:getMobileTelephone(.)"));

        // ExpressionContext form
        assertEquals("", context1.getValue("party:getMobileTelephone()"));
        assertEquals("", context2.getValue("party:getMobileTelephone()"));
        assertEquals("", context3.getValue("party:getMobileTelephone()"));

        customer.addContact(createPhone("56789", true, "MOBILE"));
        save(customer);
        assertEquals("(03) 56789", context1.getValue("party:getMobileTelephone(.)"));
        assertEquals("(03) 56789", context2.getValue("party:getMobileTelephone(.)"));
        assertEquals("(03) 56789", context3.getValue("party:getMobileTelephone(.)"));

        // ExpressionContext form
        assertEquals("(03) 56789", context1.getValue("party:getMobileTelephone()"));
        assertEquals("(03) 56789", context2.getValue("party:getMobileTelephone()"));
        assertEquals("(03) 56789", context3.getValue("party:getMobileTelephone()"));

        // null handling
        assertEquals("", context1.getValue("party:getMobileTelephone(null)"));
    }

    /**
     * Tests the {@link PartyFunctions#getFaxNumber(Object)} and
     * {@link PartyFunctions#getFaxNumber(ExpressionContext)} methods.
     */
    @Test
    public void testGetFaxNumber() {
        JXPathContext context1 = createContext(customer);
        JXPathContext context2 = createContext(estimate);
        JXPathContext context3 = createContext(item);

        assertEquals("", context1.getValue("party:getFaxNumber(.)"));
        assertEquals("", context2.getValue("party:getFaxNumber(.)"));
        assertEquals("", context3.getValue("party:getFaxNumber(.)"));

        // ExpressionContext form
        assertEquals("", context1.getValue("party:getFaxNumber()"));
        assertEquals("", context2.getValue("party:getFaxNumber()"));
        assertEquals("", context3.getValue("party:getFaxNumber()"));

        customer.addContact(createPhone("56789", true, "FAX"));
        save(customer);
        assertEquals("(03) 56789", context1.getValue("party:getFaxNumber(.)"));
        assertEquals("(03) 56789", context2.getValue("party:getFaxNumber(.)"));
        assertEquals("(03) 56789", context3.getValue("party:getFaxNumber(.)"));

        // ExpressionContext form
        assertEquals("(03) 56789", context1.getValue("party:getFaxNumber()"));
        assertEquals("(03) 56789", context2.getValue("party:getFaxNumber()"));
        assertEquals("(03) 56789", context3.getValue("party:getFaxNumber()"));

        // null handling
        assertEquals("", context1.getValue("party:getFaxNumber(null)"));
    }

    /**
     * Tests the {@link PartyFunctions#getEmailAddress(Object)} and
     * {@link PartyFunctions#getEmailAddress(ExpressionContext)} methods.
     */
    @Test
    public void testGetEmailAddress() {
        JXPathContext context1 = createContext(customer);
        JXPathContext context2 = createContext(estimate);
        JXPathContext context3 = createContext(item);

        assertEquals("", context1.getValue("party:getEmailAddress(.)"));
        assertEquals("", context2.getValue("party:getEmailAddress(.)"));
        assertEquals("", context3.getValue("party:getEmailAddress(.)"));

        // ExpressionContext form
        assertEquals("", context1.getValue("party:getEmailAddress()"));
        assertEquals("", context2.getValue("party:getEmailAddress()"));
        assertEquals("", context3.getValue("party:getEmailAddress()"));

        customer.addContact(TestHelper.createEmailContact("foo@bar.com"));
        save(customer);
        assertEquals("foo@bar.com", context1.getValue("party:getEmailAddress(.)"));
        assertEquals("foo@bar.com", context2.getValue("party:getEmailAddress(.)"));
        assertEquals("foo@bar.com", context3.getValue("party:getEmailAddress(.)"));

        // ExpressionContext form
        assertEquals("foo@bar.com", context1.getValue("party:getEmailAddress()"));
        assertEquals("foo@bar.com", context2.getValue("party:getEmailAddress()"));
        assertEquals("foo@bar.com", context3.getValue("party:getEmailAddress()"));

        // null handling
        assertEquals("", context1.getValue("party:getEmailAddress(null)"));
    }

    /**
     * Tests the {@link PartyFunctions#getWebsite(Object)} method.
     */
    @Test
    public void testGetWebsite() {
        Party customer = TestHelper.createCustomer(false);
        JXPathContext context = createContext(customer);

        assertEquals("", context.getValue("party:getWebsite(.)"));

        Contact contact = (Contact) create(ContactArchetypes.WEBSITE);
        IMObjectBean bean = getBean(contact);
        bean.setValue("url", "http://wwww.openvpms.org");
        customer.addContact(contact);

        assertEquals("http://wwww.openvpms.org", context.getValue("party:getWebsite(.)"));

        // null handling
        assertEquals("", context.getValue("party:getWebsite(null)"));
    }

    /**
     * Tests the {@link PartyFunctions#getContactPurposes(ExpressionContext)} method.
     */
    @Test
    public void testGetContactPurposes() {
        Contact contact = TestHelper.createPhoneContact(null, "123456789");

        JXPathContext context = createContext(contact);
        assertEquals("", context.getValue("party:getContactPurposes()"));

        contact.addClassification(TestHelper.getLookup(ContactArchetypes.PURPOSE, "HOME"));
        contact.addClassification(TestHelper.getLookup(ContactArchetypes.PURPOSE, "WORK"));

        assertEquals("(Home, Work)", context.getValue("party:getContactPurposes()"));
    }

    /**
     * Tests the {@link PartyFunctions#getBillingAddress(ExpressionContext)},
     * {@link PartyFunctions#getBillingAddress(Object)} and {@link PartyFunctions#getBillingAddress(Object, boolean)}
     * methods.
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

        // single argument form
        assertEquals("123 4th Avenue\nSawtell Nsw 2452", context1.getValue("party:getBillingAddress(.)"));
        assertEquals("123 4th Avenue\nSawtell Nsw 2452", context2.getValue("party:getBillingAddress(.)"));
        assertEquals("123 4th Avenue\nSawtell Nsw 2452", context3.getValue("party:getBillingAddress(.)"));

        // multiple argument form
        assertEquals("123 4th Avenue, Sawtell Nsw 2452", context1.getValue("party:getBillingAddress(., true())"));
        assertEquals("123 4th Avenue\nSawtell Nsw 2452", context1.getValue("party:getBillingAddress(., false())"));
        assertEquals("123 4th Avenue, Sawtell Nsw 2452", context2.getValue("party:getBillingAddress(., true())"));
        assertEquals("123 4th Avenue\nSawtell Nsw 2452", context2.getValue("party:getBillingAddress(., false())"));
        assertEquals("123 4th Avenue, Sawtell Nsw 2452", context3.getValue("party:getBillingAddress(., true())"));
        assertEquals("123 4th Avenue\nSawtell Nsw 2452", context3.getValue("party:getBillingAddress(., false())"));

        // ExpressionContext form
        assertEquals("123 4th Avenue\nSawtell Nsw 2452", context1.getValue("party:getBillingAddress()"));
        assertEquals("123 4th Avenue\nSawtell Nsw 2452", context2.getValue("party:getBillingAddress()"));
        assertEquals("123 4th Avenue\nSawtell Nsw 2452", context3.getValue("party:getBillingAddress()"));

        // test nulls
        assertEquals("", context1.getValue("party:getBillingAddress(null)"));
    }

    /**
     * Tests the {@link PartyFunctions#getCorrespondenceAddress(Object)},
     * {@link PartyFunctions#getCorrespondenceAddress(Object, boolean)} and
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

        // null handling
        assertEquals("", context1.getValue("party:getCorrespondenceAddress(null)"));
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
     * Tests the {@link PartyFunctions#identities(Object)} and {@link PartyFunctions#identities(ExpressionContext)}
     * methods.
     */
    @Test
    public void testIdentities() {
        Act act = (Act) create("act.customerEstimationItem");
        Party party = TestHelper.createPatient();

        JXPathContext context1 = createContext(party);
        JXPathContext context2 = createContext(act);

        assertEquals("", context1.getValue("party:identities(.)"));
        assertEquals("", context2.getValue("party:identities(.)"));

        // ExpressionContext form
        assertEquals("", context1.getValue("party:identities()"));
        assertEquals("", context2.getValue("party:identities()"));

        // now set up a tag
        party.addIdentity(createPetTag("1234567"));
        save(party);

        IMObjectBean bean = getBean(act);
        bean.setTarget("patient", party);

        String expected = "Pet Tag: 1234567";
        assertEquals(expected, context1.getValue("party:identities(.)"));
        assertEquals(expected, context2.getValue("party:identities(.)"));

        // ExpressionContext form
        assertEquals(expected, context1.getValue("party:identities()"));
        assertEquals(expected, context2.getValue("party:identities()"));

        // null handling
        assertEquals("", context1.getValue("party:identities(null)"));
    }

    /**
     * Tests the {@link PartyFunctions#getPatientMicrochip(Object)} and
     * {@link PartyFunctions#getPatientMicrochip(ExpressionContext)} methods.
     */
    @Test
    public void testGetPatientMicrochip() {
        Party patient = TestHelper.createPatient(false);
        JXPathContext ctx = createContext(patient);

        assertEquals("", ctx.getValue("party:getPatientMicrochip(.)"));
        assertEquals("", ctx.getValue("party:getPatientMicrochip()")); // ExpressionContext form

        EntityIdentity microchip = (EntityIdentity) create("entityIdentity.microchip");
        IMObjectBean tagBean = getBean(microchip);
        tagBean.setValue("microchip", "1234567");
        patient.addIdentity(microchip);

        assertEquals("1234567", ctx.getValue("party:getPatientMicrochip(.)"));
        assertEquals("1234567", ctx.getValue("party:getPatientMicrochip()")); // ExpressionContext form

        // null handling
        assertEquals("", ctx.getValue("party:getPatientMicrochip(null)"));
    }

    /**
     * Tests the {@link PartyFunctions#getPatientMicrochips(Object)} method.
     */
    @Test
    public void testGetPatientMicrochips() {
        Party patient = TestHelper.createPatient(false);
        JXPathContext ctx = createContext(patient);

        assertEquals("", ctx.getValue("party:getPatientMicrochips(.)"));

        EntityIdentity microchip1 = (EntityIdentity) create("entityIdentity.microchip");
        IMObjectBean tagBean = getBean(microchip1);
        tagBean.setValue("microchip", "123");
        patient.addIdentity(microchip1);
        save(patient);

        assertEquals("123", ctx.getValue("party:getPatientMicrochips(.)"));

        EntityIdentity microchip2 = (EntityIdentity) create("entityIdentity.microchip");
        tagBean = getBean(microchip2);
        tagBean.setValue("microchip", "456");
        patient.addIdentity(microchip2);
        save(patient);

        assertEquals("456, 123", ctx.getValue("party:getPatientMicrochips(.)"));

        // null handling
        assertEquals("", ctx.getValue("party:getPatientMicrochips(null)"));
    }

    /**
     * Tests the {@link PartyFunctions#getWeight(Object)}, {@link PartyFunctions#getWeight(Object, String)},
     * and {@link PartyFunctions#getWeight(ExpressionContext)} methods.
     */
    @Test
    public void testGetWeight() {
        Party patient = TestHelper.createPatient();

        Act visit = (Act) create(PatientArchetypes.CLINICAL_EVENT);
        IMObjectBean bean = getBean(visit);
        bean.setTarget("patient", patient);

        JXPathContext context1 = createContext(patient);
        JXPathContext context2 = createContext(visit);

        assertEquals(ZERO, context1.getValue("party:getWeight(.)"));
        assertEquals(ZERO, context2.getValue("party:getWeight(.)"));

        Act weight1 = PatientTestHelper.createWeight(patient, ONE, WeightUnits.KILOGRAMS);
        checkEquals(ONE, (BigDecimal) context1.getValue("party:getWeight(.)"));
        checkEquals(ONE, (BigDecimal) context1.getValue("party:getWeight(., 'KILOGRAMS')"));
        checkEquals(ONE_THOUSAND, (BigDecimal) context1.getValue("party:getWeight(., 'GRAMS')"));
        assertEquals(new BigDecimal("2.20462262"), context1.getValue("party:getWeight(., 'POUNDS')"));

        checkEquals(ONE, (BigDecimal) context2.getValue("party:getWeight(.)"));
        checkEquals(ONE, (BigDecimal) context2.getValue("party:getWeight(., 'KILOGRAMS')"));
        checkEquals(ONE_THOUSAND, (BigDecimal) context2.getValue("party:getWeight(., 'GRAMS')"));
        assertEquals(new BigDecimal("2.20462262"), context2.getValue("party:getWeight(., 'POUNDS')"));

        // ExpressionContext form
        checkEquals(ONE, (BigDecimal) context1.getValue("party:getWeight()"));
        checkEquals(ONE, (BigDecimal) context2.getValue("party:getWeight()"));

        // null handling
        checkEquals(ZERO, (BigDecimal) context1.getValue("party:getWeight(null)"));
        checkEquals(ZERO, (BigDecimal) context2.getValue("party:getWeight(null)"));

        checkEquals(ZERO, (BigDecimal) context1.getValue("party:getWeight(null, 'KILOGRAMS')"));
        checkEquals(ZERO, (BigDecimal) context2.getValue("party:getWeight(null, 'KILOGRAMS')"));

        remove(weight1);
        Act weight2 = PatientTestHelper.createWeight(patient, ONE_THOUSAND, WeightUnits.GRAMS);
        checkEquals(ONE, (BigDecimal) context1.getValue("party:getWeight(.)"));
        checkEquals(ONE, (BigDecimal) context1.getValue("party:getWeight(., 'KILOGRAMS')"));
        checkEquals(ONE_THOUSAND, (BigDecimal) context1.getValue("party:getWeight(., 'GRAMS')"));
        assertEquals(new BigDecimal("2.20462262"), context1.getValue("party:getWeight(., 'POUNDS')"));

        checkEquals(ONE, (BigDecimal) context2.getValue("party:getWeight(.)"));
        checkEquals(ONE, (BigDecimal) context2.getValue("party:getWeight(., 'KILOGRAMS')"));
        checkEquals(ONE_THOUSAND, (BigDecimal) context2.getValue("party:getWeight(., 'GRAMS')"));
        assertEquals(new BigDecimal("2.20462262"), context2.getValue("party:getWeight(., 'POUNDS')"));

        remove(weight2);

        PatientTestHelper.createWeight(patient, ONE, WeightUnits.POUNDS);
        checkEquals(ONE_POUND_IN_KILOS, (BigDecimal) context1.getValue("party:getWeight(.)"));
        checkEquals(ONE_POUND_IN_KILOS, (BigDecimal) context1.getValue("party:getWeight(., 'KILOGRAMS')"));
        checkEquals(ONE_POUND_IN_GRAMS, (BigDecimal) context1.getValue("party:getWeight(., 'GRAMS')"));
        assertEquals(ONE, context1.getValue("party:getWeight(., 'POUNDS')"));

        checkEquals(ONE_POUND_IN_KILOS, (BigDecimal) context2.getValue("party:getWeight(.)"));
        checkEquals(ONE_POUND_IN_KILOS, (BigDecimal) context2.getValue("party:getWeight(., 'KILOGRAMS')"));
        checkEquals(ONE_POUND_IN_GRAMS, (BigDecimal) context2.getValue("party:getWeight(., 'GRAMS')"));
        assertEquals(ONE, context2.getValue("party:getWeight(., 'POUNDS')"));
    }

    /**
     * Tests the {@link PartyFunctions#getPatientWeight(Object)} and
     * {@link PartyFunctions#getPatientWeight(ExpressionContext)} methods.
     */
    @Test
    public void testGetPatientWeight() {
        Party patient = TestHelper.createPatient();

        Act visit = (Act) create(PatientArchetypes.CLINICAL_EVENT);
        IMObjectBean bean = getBean(visit);
        bean.setTarget("patient", patient);

        JXPathContext context1 = createContext(patient);
        JXPathContext context2 = createContext(visit);

        assertEquals("", context1.getValue("party:getPatientWeight(.)"));
        assertEquals("", context2.getValue("party:getPatientWeight(.)"));

        PatientTestHelper.createWeight(patient, ONE, WeightUnits.KILOGRAMS);
        assertEquals("1 Kilograms", context1.getValue("party:getPatientWeight(.)"));
        assertEquals("1 Kilograms", context2.getValue("party:getPatientWeight(.)"));

        // ExpressionContext form
        assertEquals("1 Kilograms", context1.getValue("party:getPatientWeight()"));
        assertEquals("1 Kilograms", context2.getValue("party:getPatientWeight()"));

        // null handling
        assertEquals("", context1.getValue("party:getPatientWeight(null)"));
    }

    /**
     * Tests the {@link PartyFunctions#getPatientMicrochip(Object)}, {@link PartyFunctions#getMicrochip(Object)}
     * {@link PartyFunctions#getPatientMicrochip(ExpressionContext)}, and
     * {@link PartyFunctions#getMicrochip(ExpressionContext)} methods.
     */
    @Test
    public void tesGetPatientMicrochip() {
        Party patient = TestHelper.createPatient(false);
        Act act = (Act) create("act.customerEstimationItem");

        JXPathContext context1 = createContext(patient);
        JXPathContext context2 = createContext(act);

        assertEquals("", context1.getValue("party:getPatientMicrochip(.)")); // by Party
        assertNull(context1.getValue("party:getMicrochip(.)"));

        assertEquals("", context2.getValue("party:getPatientMicrochip(.)")); // by Act
        assertNull(context2.getValue("party:getMicrochip(.)"));

        // ExpressionContext form
        assertEquals("", context1.getValue("party:getPatientMicrochip()"));
        assertEquals("", context2.getValue("party:getPatientMicrochip()"));
        assertNull(context1.getValue("party:getMicrochip()"));
        assertNull(context2.getValue("party:getMicrochip()"));

        EntityIdentity microchip = (EntityIdentity) create("entityIdentity.microchip");
        IMObjectBean tagBean = getBean(microchip);
        tagBean.setValue("microchip", "1234567");
        patient.addIdentity(microchip);
        save(patient);

        IMObjectBean bean = getBean(act);
        bean.setTarget("patient", patient);

        // by Party
        assertEquals("1234567", context1.getValue("party:getPatientMicrochip(.)"));
        assertEquals(microchip, context1.getValue("party:getMicrochip(.)"));
        assertEquals("1234567", context1.getValue("openvpms:get(party:getMicrochip(.), 'microchip')"));

        // by Act
        assertEquals("1234567", context2.getValue("party:getPatientMicrochip(.)"));
        assertEquals(microchip, context2.getValue("party:getMicrochip(.)"));
        assertEquals("1234567", context2.getValue("openvpms:get(party:getMicrochip(.), 'microchip')"));

        // ExpressionContext
        assertEquals("1234567", context1.getValue("party:getPatientMicrochip()"));
        assertEquals(microchip, context1.getValue("party:getMicrochip()"));

        // null handling
        assertEquals("", context1.getValue("party:getPatientMicrochip(null)"));
        assertNull(context1.getValue("party:getMicrochip(null)"));
    }

    /**
     * Tests the {@link PartyFunctions#getPatientReferralVet(Object)} and
     * {@link PartyFunctions#getPatientReferralVet(ExpressionContext)} methods when invoked with either a
     * patient or an act referencing a patient.
     */
    @Test
    public void testGetPatientReferralVet() {
        Party patient = TestHelper.createPatient();
        Party vet = TestHelper.createSupplierVet();
        Act act = (Act) create("act.customerEstimationItem");

        JXPathContext context1 = createContext(patient);
        JXPathContext context2 = createContext(act);

        assertNull(context1.getValue("party:getPatientReferralVet()"));  // getPatientReferralVet(ExpressionContext)
        assertNull(context1.getValue("party:getPatientReferralVet(.)")); // getPatientReferralVet(Object)

        assertNull(context2.getValue("party:getPatientReferralVet()"));  // getPatientReferralVet(ExpressionContext)
        assertNull(context2.getValue("party:getPatientReferralVet(.)")); // getPatientReferralVet(Object)

        IMObjectBean bean = getBean(patient);
        bean.addTarget("referrals", PatientArchetypes.REFERRED_FROM, vet);
        save(patient, vet);

        IMObjectBean actBean = getBean(act);
        actBean.setTarget("patient", patient);

        assertEquals(vet, context1.getValue("party:getPatientReferralVet()"));
        assertEquals(vet, context1.getValue("party:getPatientReferralVet(.)"));

        assertEquals(vet, context2.getValue("party:getPatientReferralVet()"));
        assertEquals(vet, context2.getValue("party:getPatientReferralVet(.)"));

        // null handling
        assertNull(context1.getValue("party:getPatientReferralVet(null)"));
    }

    /**
     * Tests the {@link PartyFunctions#getPatientReferralVetPractice(Object)} and
     * {@link PartyFunctions#getPatientReferralVetPractice(ExpressionContext)} methods.
     */
    @Test
    public void testGetPatientReferralVetPractice() {
        Party patient = TestHelper.createPatient();
        Party vet = TestHelper.createSupplierVet();
        Party practice = TestHelper.createSupplierVetPractice();
        Act act = (Act) create("act.customerEstimationItem");
        JXPathContext context1 = createContext(patient);
        JXPathContext context2 = createContext(act);

        assertNull(context1.getValue("party:getPatientReferralVetPractice()"));   // ExpressionContext method
        assertNull(context1.getValue("party:getPatientReferralVetPractice(.)"));  // Object method

        assertNull(context2.getValue("party:getPatientReferralVetPractice()"));   // ExpressionContext method
        assertNull(context2.getValue("party:getPatientReferralVetPractice(.)"));  // Object method

        // create relationships between the patient, vet, and vet practice
        IMObjectBean bean = getBean(patient);
        bean.addTarget("referrals", PatientArchetypes.REFERRED_FROM, vet);
        IMObjectBean practiceBean = getBean(practice);
        practiceBean.addTarget("veterinarians", vet);
        save(patient, vet, practice);

        IMObjectBean actBean = getBean(act);
        actBean.setTarget("patient", patient);

        assertEquals(practice, context1.getValue("party:getPatientReferralVetPractice()"));
        assertEquals(practice, context1.getValue("party:getPatientReferralVetPractice(.)"));

        assertEquals(practice, context2.getValue("party:getPatientReferralVetPractice()"));
        assertEquals(practice, context2.getValue("party:getPatientReferralVetPractice(.)"));

        // null handling
        assertNull(context1.getValue("party:getPatientReferralVetPractice(null)"));
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

        // null handling
        checkEquals(BigDecimal.ZERO, (BigDecimal) ctx1.getValue("party:getAccountBalance(null)"));
    }

    /**
     * Tests the {@link PartyFunctions#getPatientRabiesTag(ExpressionContext)}
     * and {@link PartyFunctions#getPatientRabiesTag(Object)} methods.
     */
    @Test
    public void testGetPatientRabiesTag() {
        Party patient = TestHelper.createPatient();
        Act visit = (Act) create(PatientArchetypes.CLINICAL_EVENT);
        IMObjectBean bean = getBean(visit);
        bean.setTarget("patient", patient);

        JXPathContext context1 = createContext(patient);
        JXPathContext context2 = createContext(visit);
        assertEquals("", context1.getValue("party:getPatientRabiesTag(.)"));
        assertEquals("", context2.getValue("party:getPatientRabiesTag(.)"));

        // ExpressionContext form
        assertEquals("", context1.getValue("party:getPatientRabiesTag()"));
        assertEquals("", context2.getValue("party:getPatientRabiesTag()"));

        EntityIdentity tag = (EntityIdentity) create("entityIdentity.rabiesTag");
        String identity = "1234567890";
        tag.setIdentity(identity);
        patient.addIdentity(tag);
        getArchetypeService().save(patient, false);  // need to disabled validation as rabies tags are not enabled

        assertEquals(identity, context1.getValue("party:getPatientRabiesTag(.)"));
        assertEquals(identity, context2.getValue("party:getPatientRabiesTag(.)"));

        // ExpressionContext form
        assertEquals(identity, context1.getValue("party:getPatientRabiesTag()"));
        assertEquals(identity, context2.getValue("party:getPatientRabiesTag()"));

        // null handling
        assertEquals("", context2.getValue("party:getPatientRabiesTag(null)"));
    }

    /**
     * Tests the {@link PartyFunctions#getLetterheadContacts(Object)} method.
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
        IMObjectBean locationBean = getBean(location1);
        locationBean.setTarget("letterhead", letterhead);
        save(location1, letterhead);
        assertEquals(location1, context.getValue("party:getLetterheadContacts($location)"));

        bean.addTarget("contacts", location2);
        bean.save();
        assertEquals(location2, context.getValue("party:getLetterheadContacts($location)"));

        // null handling
        assertNull(context.getValue("party:getLetterheadContacts(null)"));
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
        IMObjectBean bean = getBean(work);
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
     * Tests the {@link PartyFunctions#setPatientInactive(Party)}. method.
     */
    @Test
    public void testSetInactive() {
        assertTrue(patient.isActive());
        JXPathContext context = createContext(patient);
        context.getValue("party:setPatientInactive(.)");
        Party object = get(patient);
        assertFalse(object.isActive());
    }

    /**
     * Tests the {@link PartyFunctions#setPatientDeceased(Party)} method.
     */
    @Test
    public void testSetDeceased() {
        JXPathContext context = createContext(patient);
        context.getValue("party:setPatientDeceased(.)");
        Party object = get(patient);
        assertFalse(object.isActive());
        IMObjectBean bean = getBean(object);
        assertTrue(bean.getBoolean("deceased"));
        assertNotNull(bean.getDate("deceasedDate"));
    }

    /**
     * Tests the {@link PartyFunctions#setPatientDesexed(Party)} method.
     */
    @Test
    public void testSetDesexed() {
        JXPathContext context = createContext(patient);
        context.getValue("party:setPatientDesexed(.)");
        Party object = get(patient);
        IMObjectBean bean = getBean(object);
        assertTrue(bean.getBoolean("desexed"));
    }

    /**
     * Tests the {@link PartyFunctions#getBpayId(Object)} and {@link PartyFunctions#getBpayId(ExpressionContext)}
     * methods.
     */
    @Test
    public void testGetBpayId() {
        JXPathContext context = createContext(customer);
        String expected = rules.getBpayId(customer);

        assertEquals(expected, context.getValue("party:getBpayId(.)"));
        assertEquals(expected, context.getValue("party:getBpayId()"));  // ExpressionContext form

        // null handling
        assertNull(expected, context.getValue("party:getBpayId(null)"));
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
        IMObjectBean bean = getBean(contact);
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
        IMObjectBean tagBean = getBean(result);
        tagBean.setValue("petTag", tag);
        return result;
    }

    /**
     * Creates a new JXPathContext, with the party functions registered.
     *
     * @param object the context object
     * @return a new JXPathContext
     */
    private JXPathContext createContext(org.openvpms.component.model.object.IMObject object) {
        // use the non-rules based archetype service, as that is what is used at deployment
        IArchetypeService service = applicationContext.getBean("defaultArchetypeService", IArchetypeService.class);
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
