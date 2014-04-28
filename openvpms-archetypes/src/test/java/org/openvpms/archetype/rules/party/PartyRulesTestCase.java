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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.party;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link PartyRules} class.
 *
 * @author Tim Anderson
 */
public class PartyRulesTestCase extends ArchetypeServiceTest {

    /**
     * The rules.
     */
    private PartyRules rules;


    /**
     * Tests the {@link PartyRules#getFullName(Party)} method.
     */
    @Test
    public void testGetFullName() {
        Party customer = (Party) create(CustomerArchetypes.PERSON);
        IMObjectBean bean = new IMObjectBean(customer);
        Lookup mr = TestHelper.getLookup("lookup.personTitle", "MR");
        bean.setValue("title", mr.getCode());
        bean.setValue("firstName", "Foo");
        bean.setValue("lastName", "Bar");
        assertEquals("Mr Foo Bar", rules.getFullName(customer));

        // verify no special formatting for other party types
        Party pet = (Party) create("party.patientpet");
        pet.setName("T Rex");
        assertEquals("T Rex", rules.getFullName(pet));

    }

    /**
     * Tests the {@link PartyRules#getDefaultContacts()} method.
     */
    @Test
    public void testDefaultContacts() {
        Set<Contact> contacts = rules.getDefaultContacts();
        assertNotNull(contacts);
        assertEquals(2, contacts.size());

        // expect a contact.location and a contact.phoneNumber
        assertNotNull(getContact(contacts, ContactArchetypes.LOCATION));
        assertNotNull(getContact(contacts, ContactArchetypes.PHONE));
    }

    /**
     * Tests the {@link PartyRules#getPreferredContacts(Party)} method.
     */
    @Test
    public void testGetPreferredContacts() {
        Party party = (Party) create(CustomerArchetypes.PERSON);
        assertEquals(0, party.getContacts().size());
        party.setContacts(rules.getDefaultContacts());
        assertEquals(2, party.getContacts().size());

        IMObjectBean bean = new IMObjectBean(party);
        bean.setValue("firstName", "ZFoo");
        bean.setValue("lastName", "ZBar");

        Contact location = getContact(party, ContactArchetypes.LOCATION);
        Contact phone = getContact(party, ContactArchetypes.PHONE);
        IMObjectBean locationBean = new IMObjectBean(location);
        IMObjectBean phoneBean = new IMObjectBean(phone);
        locationBean.setValue("preferred", false);
        phoneBean.setValue("preferred", false);

        Lookup state = TestHelper.getLookup("lookup.state", "VIC");
        Lookup suburb = TestHelper.getLookup("lookup.suburb", "BAR", "Bar", state,
                                             "lookupRelationship.stateSuburb");

        // expect no preferred contacts to result in empty string
        assertEquals("", rules.getPreferredContacts(party));

        // now make the location a preferred contact
        locationBean.setValue("preferred", true);
        locationBean.setValue("address", "1 Foo St");
        locationBean.setValue("suburb", suburb.getCode());
        locationBean.setValue("state", state.getCode());
        locationBean.setValue("postcode", "3071");
        location.addClassification(getContactPurpose("HOME"));
        final String address = "1 Foo St Bar 3071 (Home)";
        assertEquals(address, rules.getPreferredContacts(party));

        // and the phone number as well
        phoneBean.setValue("preferred", true);
        phoneBean.setValue("areaCode", "03");
        phoneBean.setValue("telephoneNumber", "1234567");
        phone.addClassification(getContactPurpose("WORK"));
        final String phoneNo = "(03) 1234567 (Work)";

        bean.save();

        String contacts = rules.getPreferredContacts(party);
        if (location.getId() < phone.getId()) { // check sort order
            assertEquals(address + ", " + phoneNo, contacts);
        } else {
            assertEquals(phoneNo + ", " + address, contacts);
        }
    }

    /**
     * Tests the {@link PartyRules#getContactPurposes(Contact)} method.
     */
    @Test
    public void testGetContactPurposes() {
        Contact contact = (Contact) create(ContactArchetypes.LOCATION);
        contact.addClassification(getContactPurpose("HOME"));

        assertEquals("(Home)", rules.getContactPurposes(contact));

        contact.addClassification(getContactPurpose("WORK"));

        String purposes = rules.getContactPurposes(contact);

        // order not guaranteed
        assertTrue(purposes.equals("(Home, Work)")
                   || purposes.equals("(Work, Home)"));
    }

    /**
     * Tests the {@link PartyRules#getBillingAddress(Party)} method.
     */
    @Test
    public void testGetBillingAddress() {
        Party party = TestHelper.createCustomer("Foo", "Bar", false);
        IMObjectBean customer = new IMObjectBean(party);
        customer.setValue("title", "MR");

        Contact location = getContact(party, ContactArchetypes.LOCATION);
        populateLocation(location, "1 Foo St", null);

        // no location with billing address, uses the first available.
        assertEquals("1 Foo St\nCoburg Victoria 3071", rules.getBillingAddress(party));

        // add a billing location
        Contact billing = createLocation("3 Bar St", "BILLING");
        party.addContact(billing);

        // verify the billing address is that just added
        assertEquals("3 Bar St\nCoburg Victoria 3071", rules.getBillingAddress(party));

        // verify nulls aren't displayed if the state doesn't exist
        IMObjectBean locationBean = new IMObjectBean(billing);
        locationBean.setValue("state", "BAD_STATE");
        assertEquals("3 Bar St\n3071", rules.getBillingAddress(party));

        // verify nulls aren't displayed if the suburb doesn't exist
        locationBean.setValue("state", "VIC");
        locationBean.setValue("suburb", "BAD_SUBURB");
        assertEquals("3 Bar St\nVictoria 3071", rules.getBillingAddress(party));


        // remove all the contacts and verify there is no billing address
        Contact[] contacts = party.getContacts().toArray(new Contact[party.getContacts().size()]);
        for (Contact c : contacts) {
            party.removeContact(c);
        }

        assertEquals("", rules.getBillingAddress(party));
    }

    /**
     * Tests the {@link PartyRules#getCorrespondenceAddress(Party)} method.
     */
    @Test
    public void testGetCorrespondenceAddress() {
        Party party = TestHelper.createCustomer("Foo", "Bar", false);
        IMObjectBean customer = new IMObjectBean(party);
        customer.setValue("title", "MR");

        Contact location = getContact(party, ContactArchetypes.LOCATION);
        populateLocation(location, "1 Foo St", null);

        // no location with billing address, uses the first available.
        assertEquals("1 Foo St\nCoburg Victoria 3071",
                     rules.getCorrespondenceAddress(party));

        // add a correspondence location
        Contact correspondence = createLocation("3 Bar St", "CORRESPONDENCE");
        party.addContact(correspondence);

        // verify the correspondence address is that just added
        assertEquals("3 Bar St\nCoburg Victoria 3071",
                     rules.getCorrespondenceAddress(party));

        // remove all the contacts and verify there is no correspondence address
        Contact[] contacts = party.getContacts().toArray(new Contact[party.getContacts().size()]);
        for (Contact c : contacts) {
            party.removeContact(c);
        }

        assertEquals("", rules.getCorrespondenceAddress(party));
    }

    /**
     * Tests the {@link PartyRules#getTelephone(Party)} method.
     */
    @Test
    public void testGetTelephone() {
        Party party = TestHelper.createCustomer(false);
        Contact phone1 = getContact(party, ContactArchetypes.PHONE);
        populatePhone(phone1, "12345", false, null);

        assertEquals("(03) 12345", rules.getTelephone(party));

        Contact phone2 = createPhone("56789", true, null);
        party.addContact(phone2);
        assertEquals("(03) 56789", rules.getTelephone(party));
    }

    /**
     * Tests the {@link PartyRules#getTelephone(Party)} method.
     */
    @Test
    public void testActGetTelephone() {
        Act act = (Act) create("act.customerEstimation");
        assertEquals("", rules.getTelephone(act));

        Party party = TestHelper.createCustomer(false);
        Contact phone1 = getContact(party, ContactArchetypes.PHONE);
        populatePhone(phone1, "12345", false, null);
        Contact phone2 = createPhone("56789", true, null);
        party.addContact(phone2);
        save(party);

        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.customer", party);

        assertEquals("(03) 56789", rules.getTelephone(act));
    }

    /**
     * Tests the {@link PartyRules#getHomeTelephone(Party)} method.
     */
    @Test
    public void testGetHomeTelephone() {
        Party party = TestHelper.createCustomer(false);
        Contact phone1 = getContact(party, ContactArchetypes.PHONE);
        populatePhone(phone1, "12345", false, null);

        assertEquals("(03) 12345", rules.getHomeTelephone(party)); // OVPMS-718
        Lookup purpose = getContactPurpose("HOME");
        phone1.addClassification(purpose);
        assertEquals("(03) 12345", rules.getHomeTelephone(party));

        Contact phone2 = createPhone("56789", true, null);
        party.addContact(phone2);
        assertEquals("(03) 12345", rules.getHomeTelephone(party));

        phone2.addClassification(purpose);
        assertEquals("(03) 56789", rules.getHomeTelephone(party));
    }

    /**
     * Tests the {@link PartyRules#getWorkTelephone(Party)} method.
     */
    @Test
    public void testGetWorkTelephone() {
        Party party = TestHelper.createCustomer(false);
        Contact phone1 = getContact(party, ContactArchetypes.PHONE);
        populatePhone(phone1, "12345", false, null);

        assertEquals("", rules.getWorkTelephone(party));
        Lookup purpose = getContactPurpose("WORK");
        phone1.addClassification(purpose);
        assertEquals("(03) 12345", rules.getWorkTelephone(party));

        Contact phone2 = createPhone("56789", true, null);
        party.addContact(phone2);
        assertEquals("(03) 12345", rules.getWorkTelephone(party));

        phone2.addClassification(purpose);
        assertEquals("(03) 56789", rules.getWorkTelephone(party));
    }

    /**
     * Tests the {@link PartyRules#getHomeTelephone(Act)} method.
     */
    @Test
    public void testActGetHomeTelephone() {
        Act act = (Act) create("act.customerEstimation");
        assertEquals("", rules.getHomeTelephone(act));

        Party party = TestHelper.createCustomer();
        Contact phone = getContact(party, ContactArchetypes.PHONE);
        populatePhone(phone, "12345", false, "HOME");
        save(phone);

        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.customer", party);

        assertEquals("(03) 12345", rules.getHomeTelephone(act));
    }

    /**
     * Tests the {@link PartyRules#getWorkTelephone(Act)} method.
     */
    @Test
    public void testActGetWorkTelephone() {
        Act act = (Act) create("act.customerEstimation");
        assertEquals("", rules.getWorkTelephone(act));

        Party party = TestHelper.createCustomer();
        Contact phone = getContact(party, ContactArchetypes.PHONE);
        populatePhone(phone, "12345", false, "WORK");
        save(phone);

        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.customer", party);

        assertEquals("(03) 12345", rules.getWorkTelephone(act));
    }

    /**
     * Tests the {@link PartyRules#getSMSTelephone(Party)} method.
     */
    @Test
    public void testGetSMSTelephone() {
        Party party = TestHelper.createCustomer(false);

        assertEquals("", rules.getSMSTelephone(party));
        Contact contact1 = createPhone(null, "1234", false, null);

        party.addContact(contact1);
        assertEquals("", rules.getSMSTelephone(party));

        enableSMS(contact1);
        assertEquals("1234", rules.getSMSTelephone(party));

        Contact contact2 = createPhone(null, "5678", true, null);
        party.addContact(contact2);
        assertEquals("1234", rules.getSMSTelephone(party));

        enableSMS(contact2);
        assertEquals("5678", rules.getSMSTelephone(party));
    }

    /**
     * Tests the {@link PartyRules#getFaxNumber(Party)} method.
     */
    @Test
    public void testGetFaxNumber() {
        Party party = TestHelper.createCustomer(false);

        assertEquals("", rules.getFaxNumber(party));

        Contact fax1 = createFax("03", "12345");
        party.addContact(fax1);
        assertEquals("(03) 12345", rules.getFaxNumber(party));

        party.removeContact(fax1);
        Contact fax2 = createFax(null, "12345");
        party.addContact(fax2);
        assertEquals("12345", rules.getFaxNumber(party));
    }

    /**
     * Tests the {@link PartyRules#getIdentities(Party)} method.
     */
    @Test
    public void testGetIdentities() {
        Party pet = TestHelper.createPatient(false);
        EntityIdentity tag = (EntityIdentity) create("entityIdentity.petTag");
        IMObjectBean tagBean = new IMObjectBean(tag);
        tagBean.setValue("petTag", "1234567");
        pet.addIdentity(tag);

        final String tagString = "Pet Tag: 1234567";
        assertEquals(tagString, rules.getIdentities(pet));

        EntityIdentity alias = (EntityIdentity) create("entityIdentity.alias");
        IMObjectBean aliasBean = new IMObjectBean(alias);
        aliasBean.setValue("alias", "Foo");
        pet.addIdentity(alias);

        String identities = rules.getIdentities(pet);
        String expect1 = tagString + ", Alias: Foo";
        String expect2 = "Alias: Foo, " + tagString;

        assertTrue(expect1.equals(identities) || expect2.equals(identities));
    }

    /**
     * Tests the {@link PartyRules#getContact(Party, String, String)} method.
     */
    @Test
    public void testGetContact() {
        Party party = (Party) create(CustomerArchetypes.PERSON);
        IMObjectBean bean = new IMObjectBean(party);
        bean.setValue("firstName", "Foo");
        bean.setValue("lastName", "Bar");

        // add 3 contacts, saving each time to ensure to ensure ordering of contacts by id
        Contact phone1 = createPhone("12345", false, null);
        party.addContact(phone1);
        save(party);

        Contact phone2 = createPhone("45678", false, "HOME");
        party.addContact(phone2);
        save(party);

        Contact phone3 = createPhone("90123", false, "HOME");
        party.addContact(phone3);
        save(party);

        assertEquals(phone1, rules.getContact(party, ContactArchetypes.PHONE, null));
        assertEquals(phone2, rules.getContact(party, ContactArchetypes.PHONE, "HOME"));

        setPreferred(phone3, true);
        assertEquals(phone3, rules.getContact(party, ContactArchetypes.PHONE, "HOME"));

        setPreferred(phone2, true);

        // phone2 should now be returned as its id < phone3
        assertEquals(phone2, rules.getContact(party, ContactArchetypes.PHONE, "HOME"));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        rules = new PartyRules(getArchetypeService());

        Lookup state = TestHelper.getLookup("lookup.state", "VIC", "Victoria", true);
        state.setDefaultLookup(true);
        save(state);
    }

    /**
     * Helper to set the preferred flag of a contact.
     *
     * @param contact   the contact
     * @param preferred if {@code true}, marks the contact as preferred
     */
    private void setPreferred(Contact contact, boolean preferred) {
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("preferred", preferred);
    }

    /**
     * Helper to get a contact from party by short name.
     *
     * @param party     the party
     * @param shortName contact short name
     * @return the associated short name or {@code null}
     */
    private Contact getContact(Party party, String shortName) {
        return getContact(party.getContacts(), shortName);
    }


    /**
     * Helper to get a contact from a collection by short name.
     *
     * @param contacts  the contacts
     * @param shortName contact short name
     * @return the associated short name or {@code null}
     */
    private Contact getContact(Collection<Contact> contacts, String shortName) {
        for (Contact contact : contacts) {
            if (TypeHelper.isA(contact, shortName)) {
                return contact;
            }
        }
        return null;
    }

    /**
     * Gets an <em>lookup.contactPurpose</em> lookup, creating it if it
     * doesn't exist.
     *
     * @param purpose the purpose
     * @return the lookup
     */
    private Lookup getContactPurpose(String purpose) {
        return TestHelper.getLookup("lookup.contactPurpose", purpose);
    }

    /**
     * Creates a new <em>contact.location</em>.
     *
     * @param address the address
     * @param purpose the contact purpose. May be {@code null}
     * @return a new location contact
     */
    private Contact createLocation(String address, String purpose) {
        Contact contact = (Contact) create(ContactArchetypes.LOCATION);
        populateLocation(contact, address, purpose);
        return contact;
    }

    /**
     * Populates a <em>contact.location</em>
     *
     * @param contact the contact
     * @param address the address
     * @param purpose the contact purpose. May be {@code null}
     */
    private void populateLocation(Contact contact, String address,
                                  String purpose) {
        IMObjectBean bean = new IMObjectBean(contact);
        Lookup state = TestHelper.getLookup("lookup.state", "VIC");
        Lookup suburb = TestHelper.getLookup("lookup.suburb", "COBURG", "Coburg", state,
                                             "lookupRelationship.stateSuburb");
        bean.setValue("address", address);
        bean.setValue("suburb", suburb.getCode());
        bean.setValue("postcode", "3071");
        bean.setValue("state", state.getCode());
        if (purpose != null) {
            contact.addClassification(getContactPurpose(purpose));
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
        return createPhone("03", number, preferred, purpose);
    }

    /**
     * Creates a new <em>contact.phoneNumber</em>.
     *
     * @param number    the phone number
     * @param preferred if {@code true}, marks the contact as the preferred contact
     * @param purpose   the contact purpose. May be {@code null}
     * @return a new phone contact
     */
    private Contact createPhone(String areaCode, String number, boolean preferred, String purpose) {
        Contact contact = (Contact) create(ContactArchetypes.PHONE);
        populatePhone(contact, areaCode, number, preferred, purpose);
        return contact;
    }

    /**
     * Enables the SMS flag for a phone contact.
     *
     * @param contact the contact
     */
    private void enableSMS(Contact contact) {
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("sms", true);
    }

    /**
     * Creates a new <em>contact.faxNumber</em>.
     *
     * @param areaCode the area code. May be {@code null}
     * @param number   the fax number
     * @return a new fax contact
     */
    private Contact createFax(String areaCode, String number) {
        Contact contact = (Contact) create(ContactArchetypes.FAX);
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("areaCode", areaCode);
        bean.setValue("faxNumber", number);
        return contact;
    }

    /**
     * Populates a <em>contact.phoneNumber</em>.
     *
     * @param contact   the contact
     * @param number    the phone number
     * @param preferred if {@code true}, marks the contact as the preferred
     *                  contact
     * @param purpose   the contact purpose. May be {@code null}
     */
    private void populatePhone(Contact contact, String number, boolean preferred, String purpose) {
        populatePhone(contact, "03", number, preferred, purpose);
    }

    /**
     * Populates a <em>contact.phoneNumber</em>.
     *
     * @param contact   the contact
     * @param areaCode  the area code. May be {@code null}
     * @param number    the phone number
     * @param preferred if {@code true}, marks the contact as the preferred contact
     * @param purpose   the contact purpose. May be {@code null}
     */
    private void populatePhone(Contact contact, String areaCode, String number, boolean preferred, String purpose) {
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("areaCode", areaCode);
        bean.setValue("telephoneNumber", number);
        bean.setValue("preferred", preferred);
        if (purpose != null) {
            contact.addClassification(getContactPurpose(purpose));
        }
    }
}


