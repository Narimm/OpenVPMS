/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.party;

import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.Collection;
import java.util.Set;


/**
 * Tests the {@link PartyRules} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PartyRulesTestCase extends ArchetypeServiceTest {

    /**
     * The rules.
     */
    private PartyRules rules;


    /**
     * Tests the {@link PartyRules#getFullName(Party)} method.
     */
    public void testGetFullName() {
        Party customer = (Party) create("party.customerperson");
        IMObjectBean bean = new IMObjectBean(customer);
        Lookup mr = TestHelper.getClassification("lookup.personTitle", "MR");
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
    public void testDefaultContacts() {
        Set<Contact> contacts = rules.getDefaultContacts();
        assertNotNull(contacts);
        assertEquals(2, contacts.size());

        // expect a contact.location and a contact.phoneNumber
        assertNotNull(getContact(contacts, "contact.location"));
        assertNotNull(getContact(contacts, "contact.phoneNumber"));
    }

    /**
     * Tests the {@link PartyRules#getPreferredContacts(Party)} method.
     */
    public void testGetPreferredContacts() {
        Party party = (Party) create("party.customerperson");
        assertEquals(2, party.getContacts().size());
        Contact location = getContact(party, "contact.location");
        Contact phone = getContact(party, "contact.phoneNumber");
        IMObjectBean locationBean = new IMObjectBean(location);
        IMObjectBean phoneBean = new IMObjectBean(phone);
        locationBean.setValue("preferred", false);
        phoneBean.setValue("preferred", false);

        // expect no preferred contacts to result in empty string
        assertEquals("", rules.getPreferredContacts(party));

        // now make the location a preferred contact
        locationBean.setValue("preferred", true);
        locationBean.setValue("address", "1 Foo St");
        locationBean.setValue("suburb", "Bar");
        locationBean.setValue("postcode", "3071");
        location.addClassification(createContactPurpose("HOME"));
        final String address = "1 Foo St Bar 3071 (Home)";
        assertEquals(address, rules.getPreferredContacts(party));

        // and the phone number as well
        phoneBean.setValue("preferred", true);
        phoneBean.setValue("areaCode", "03");
        phoneBean.setValue("telephoneNumber", "1234567");
        phone.addClassification(createContactPurpose("WORK"));
        final String phoneNo = "(03) 1234567 (Work)";

        String contacts = rules.getPreferredContacts(party);
        // order is not guaranteed
        assertTrue(contacts.equals(address + ", " + phoneNo)
                || contacts.equals(phoneNo + ", " + address));
    }

    /**
     * Tests the {@link PartyRules#getContactPurposes(Contact)} method.
     */
    public void testGetContactPurposes() {
        Contact contact = (Contact) create("contact.location");
        contact.addClassification(createContactPurpose("HOME"));

        assertEquals("(Home)", rules.getContactPurposes(contact));

        contact.addClassification(createContactPurpose("WORK"));

        String purposes = rules.getContactPurposes(contact);

        // order not guaranteed
        assertTrue(purposes.equals("(Home, Work)")
                || purposes.equals("(Work, Home)"));
    }

    /**
     * Tests the {@link PartyRules#getBillingAddress(Party)} method.
     */
    public void testGetBillingAddress() {
        Party party = TestHelper.createCustomer("Foo", "Bar", false);
        IMObjectBean customer = new IMObjectBean(party);
        customer.setValue("title", "MR");

        Contact location = getContact(party, "contact.location");
        populateLocation(location, "1 Foo St", null);

        // no location with billing address, uses the first available.
        assertEquals("1 Foo St\nCoburg VIC 3071",
                     rules.getBillingAddress(party));

        // add a billing location
        Contact billing = createLocation("3 Bar St", "BILLING");
        party.addContact(billing);

        // verify the billing address is that just added
        assertEquals("3 Bar St\nCoburg VIC 3071",
                     rules.getBillingAddress(party));

        // remove all the contacts and verify there is no billing address
        for (Contact c: party.getContacts().toArray(new Contact[0])) {
            party.removeContact(c);
        }

        assertEquals("", rules.getBillingAddress(party));
    }

    /**
     * Tests the {@link PartyRules#getCorrespondenceAddress(Party)} method.
     */
    public void testGetCorrespondenceAddress() {
        Party party = TestHelper.createCustomer("Foo", "Bar", false);
        IMObjectBean customer = new IMObjectBean(party);
        customer.setValue("title", "MR");

        Contact location = getContact(party, "contact.location");
        populateLocation(location, "1 Foo St", null);

        // no location with billing address, uses the first available.
        assertEquals("1 Foo St\nCoburg VIC 3071",
                     rules.getCorrespondenceAddress(party));

        // add a correspondence location
        Contact correspondence = createLocation("3 Bar St", "CORRESPONDENCE");
        party.addContact(correspondence);

        // verify the correspondence address is that just added
        assertEquals("3 Bar St\nCoburg VIC 3071",
                     rules.getCorrespondenceAddress(party));

        // remove all the contacts and verify there is no correspondence address
        for (Contact c: party.getContacts().toArray(new Contact[0])) {
            party.removeContact(c);
        }

        assertEquals("", rules.getCorrespondenceAddress(party));
    }

    /**
     * Tests the {@link PartyRules#getIdentities(Party)} method.
     */
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
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        rules = new PartyRules();
    }

    /**
     * Helper to get a contact from party by short name.
     *
     * @param party     the party
     * @param shortName contact short name
     * @return the associated short name or <code>null</code>
     */
    private Contact getContact(Party party, String shortName) {
        return getContact(party.getContacts(), shortName);
    }


    /**
     * Helper to get a contact from a collection by short name.
     *
     * @param contacts  the contacts
     * @param shortName contact short name
     * @return the associated short name or <code>null</code>
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
     * Helper to create a contact purpose.
     *
     * @param purpose the purpose
     */
    private Lookup createContactPurpose(String purpose) {
        Lookup lookup = (Lookup) create("lookup.contactPurpose");
        lookup.setCode(purpose);
        return lookup;
    }

    /**
     * Creates a new <em>contact.location</em>.
     *
     * @param address the address
     * @param purpose the contact purpose. May be <code>null</code>
     * @return a new lcoation contact
     */
    private Contact createLocation(String address, String purpose) {
        Contact contact = (Contact) create("contact.location");
        populateLocation(contact, address, purpose);
        return contact;
    }

    /**
     * Populates an existing <em>contact.location</em>
     *
     * @param contact the contact
     * @param address the address
     * @param purpose the contact purpose. May be <code>null</code>
     */
    private void populateLocation(Contact contact, String address,
                                  String purpose) {
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("address", address);
        bean.setValue("suburb", "Coburg");
        bean.setValue("postcode", "3071");
        bean.setValue("state", "VIC");
        if (purpose != null) {
            contact.addClassification(createContactPurpose(purpose));
        }
    }
}


