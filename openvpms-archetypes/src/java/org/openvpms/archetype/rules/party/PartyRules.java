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

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceFunctions;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.business.service.archetype.helper.LookupHelperException;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * Business rules for <em>party.*</em> instances.
 *
 * @author Tim Anderson
 * @author Tony De Keizer
 */
public class PartyRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Constructs a {@code PartyRules}.
     *
     * @param service the archetype service
     */
    public PartyRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns a specified node for a customer associated with an
     * act via an <em>participation.customer</em> or an
     * <em>participation.patient</em> participation.
     *
     * @param act      the act
     * @param nodeName the name of the node
     * @return a node object may be null if no customer or invalid node
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Object getCustomerNode(Act act, String nodeName) {
        Party party = getCustomer(act);
        if (party == null) {
            party = getOwner(act);
        }
        return getCustomerNode(party, nodeName);
    }

    /**
     * Returns a specified node for a specified customer.
     *
     * @param party    the party
     * @param nodeName the name of the node
     * @return a node object may be null if no customer or invalid node
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Object getCustomerNode(Party party, String nodeName) {
        if (party != null) {
            IMObjectBean bean = new IMObjectBean(party);
            NodeDescriptor descriptor = bean.getDescriptor(nodeName);
            if (descriptor != null && descriptor.isLookup()) {
                return ArchetypeServiceFunctions.lookup(party, nodeName);
            } else {
                return bean.getValue(nodeName);
            }
        }
        return null;
    }

    /**
     * Returns the formatted full name of a party.
     *
     * @param party the party
     * @return the formatted full name of the party
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getFullName(Party party) {
        String name;
        if (TypeHelper.isA(party, "party.customerperson")) {
            IMObjectBean bean = new IMObjectBean(party, service);
            if (bean.hasNode("companyName") && (bean.getString("companyName") != null)) {
                name = party.getName();
            } else {
                name = getPersonName(party);
            }
        } else {
            name = party.getName();
        }
        return (name != null) ? name : "";
    }

    /**
     * Returns the formatted full name of a party.
     *
     * @param act the Act
     * @return the formatted full name of the party
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getFullName(Act act) {
        Party party = getCustomer(act);
        if (party == null) {
            party = getOwner(act);
        }
        return (party != null) ? getFullName(party) : "";
    }

    /**
     * Returns a set of default contacts containing an
     * <em>contact.phoneNumber</em> and <em>contact.location</em>.
     *
     * @return a new set of default contacts
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Set<Contact> getDefaultContacts() {
        Set<Contact> contacts = new HashSet<Contact>();
        Contact phone = (Contact) service.create(ContactArchetypes.PHONE);
        Contact location = (Contact) service.create(ContactArchetypes.LOCATION);
        service.deriveValues(phone);
        service.deriveValues(location);
        contacts.add(phone);
        contacts.add(location);
        return contacts;
    }

    /**
     * Returns a formatted string of preferred contacts for a party.
     * <p/>
     * If there are multiple preferred contacts, these are sorted on identifier.
     *
     * @param party the party
     * @return a formatted string of preferred contacts
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getPreferredContacts(Party party) {
        StringBuilder result = new StringBuilder();
        for (Contact contact : sort(party.getContacts())) {
            IMObjectBean bean = new IMObjectBean(contact, service);
            if (bean.hasNode("preferred")) {
                boolean preferred = bean.getBoolean("preferred");
                if (preferred && bean.hasNode("description")) {
                    String description = bean.getString("description", "");
                    if (result.length() != 0) {
                        result.append(", ");
                    }
                    result.append(description);
                }
            }
        }
        return result.toString();
    }

    /**
     * Returns a formatted string of contact purposes.
     *
     * @param contact the contact
     * @return the formatted contact purposes string. May be empty.
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getContactPurposes(Contact contact) {
        StringBuilder result = new StringBuilder();
        IMObjectBean bean = new IMObjectBean(contact, service);
        if (bean.hasNode("purposes")) {
            List<IMObject> list = bean.getValues("purposes");
            if (!list.isEmpty()) {
                if (result.length() != 0) {
                    result.append(" ");
                }
                result.append("(");
                result.append(getValues(list, "name"));
                result.append(")");
            }
        }
        return result.toString();
    }

    /**
     * Returns a formatted billing address for a party.
     *
     * @param party the party
     * @return a formatted billing address for a party. May be empty if
     *         there is no corresponding <em>contact.location</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getBillingAddress(Party party) {
        return getAddress(party, "BILLING");
    }

    /**
     * Returns a formatted billing address for a customer associated with an
     * act via an <em>participation.customer</em> or an
     * <em>participation.patient</em> participation.
     *
     * @param act the act
     * @return a formatted billing address for a party. May be empty if
     *         the act has no customer party or the party has no corresponding
     *         <em>contact.location</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getBillingAddress(Act act) {
        Party party = getCustomer(act);
        if (party == null) {
            party = getOwner(act);
        }
        return (party != null) ? getBillingAddress(party) : "";
    }

    /**
     * Returns a formatted correspondence address for a party.
     *
     * @param party the party
     * @return a formatted correspondence address for a party. May be empty if
     *         there is no corresponding <em>contact.location</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getCorrespondenceAddress(Party party) {
        return getAddress(party, "CORRESPONDENCE");
    }

    /**
     * Returns a formatted correspondence name and address for a party.
     *
     * @param party the party
     * @return a formatted correspondence name and address for a party. May be empty if
     *         there is no corresponding <em>contact.location</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getCorrespondenceNameAddress(Party party) {
        StringBuilder result = new StringBuilder();
        result.append(getFullName(party));
        result.append("\n");
        result.append(getAddress(party, "CORRESPONDENCE"));

        return result.toString();
    }

    /**
     * Returns a formatted correspondence address for a customer associated with
     * an act via an <em>participation.customer</em> or an
     * <em>participation.patient</em> participation.
     *
     * @param act the act
     * @return a formatted billing address for a party. May be empty if
     *         the act has no customer party or the party has no corresponding
     *         <em>contact.location</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getCorrespondenceAddress(Act act) {
        Party party = getCustomer(act);
        if (party == null) {
            party = getOwner(act);
        }
        return (party != null) ? getCorrespondenceAddress(party) : "";
    }

    /**
     * Returns a formatted correspondence name and address for a customer associated with
     * an act via an <em>participation.customer</em> or an
     * <em>participation.patient</em> participation.
     *
     * @param act the act
     * @return a formatted name and billing address for a party. May be empty if
     *         the act has no customer party or the party has no corresponding
     *         <em>contact.location</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getCorrespondenceNameAddress(Act act) {
        Party party = getCustomer(act);
        if (party == null) {
            party = getOwner(act);
        }
        return (party != null) ? getCorrespondenceNameAddress(party) : "";
    }

    /**
     * Returns a formatted <em>contact.location</em> address with the
     * specified purpose, for a party if one exists.
     *
     * @param party   the party
     * @param purpose the contact purpose of the address
     * @return a formatted address. May be empty if there is no corresponding
     *         <em>contact.location</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getAddress(Party party, String purpose) {
        Contact contact = getContact(party, ContactArchetypes.LOCATION, purpose);
        return (contact != null) ? formatAddress(contact) : "";
    }

    /**
     * Returns a formatted preferred telephone number for a party.
     *
     * @param party the party
     * @return a formatted telephone number for the party. May be empty if there is no corresponding
     *         <em>contact.phoneNumber</em> contact
     */
    public String getTelephone(Party party) {
        Contact contact = getContact(party, ContactArchetypes.PHONE, null, false);
        return (contact != null) ? formatPhone(contact) : "";
    }

    /**
     * Returns a formatted preferred telephone number for a party associated with
     * an act via an <em>participation.customer</em> participation.
     *
     * @param act the act
     * @return a formatted telephone number for the party. May be empty if there is no corresponding
     *         <em>contact.phoneNumber</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getTelephone(Act act) {
        Party party = getCustomer(act);
        if (party == null) {
            party = getOwner(act);
        }
        return (party != null) ? getTelephone(party) : "";
    }

    /**
     * Returns a formatted home telephone number for a party.
     *
     * @param party the party
     * @return a formatted home telephone number for the party. May be empty if
     *         there is no corresponding <em>contact.phoneNumber</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getHomeTelephone(Party party) {
        Contact contact = getContact(party, ContactArchetypes.PHONE, "HOME",
                                     false);
        return (contact != null) ? formatPhone(contact) : "";
    }

    /**
     * Returns a formatted home telephone number for a party associated with
     * an act via an <em>participation.customer</em> participation.
     *
     * @param act the act
     * @return a formatted home telephone number for the party. May be empty if
     *         there is no customer, or corresponding
     *         <em>contact.phoneNumber</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getHomeTelephone(Act act) {
        Party party = getCustomer(act);
        if (party == null) {
            party = getOwner(act);
        }
        return (party != null) ? getHomeTelephone(party) : "";
    }

    /**
     * Returns a formatted mobile telephone number for a party.
     *
     * @param party the party
     * @return a formatted mobile telephone number for the party. May be empty if
     *         there is no corresponding <em>contact.phoneNumber</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getMobileTelephone(Party party) {
        Contact contact = getContact(party, ContactArchetypes.PHONE, "MOBILE",
                                     true);
        return (contact != null) ? formatPhone(contact) : "";
    }

    /**
     * Returns a formatted mobile telephone number for a party associated with
     * an act via an <em>participation.customer</em> participation.
     *
     * @param act the act
     * @return a formatted mobile telephone number for the party. May be empty if
     *         there is no customer, or corresponding
     *         <em>contact.phoneNumber</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getMobileTelephone(Act act) {
        Party party = getCustomer(act);
        if (party == null) {
            party = getOwner(act);
        }
        return (party != null) ? getMobileTelephone(party) : "";
    }

    /**
     * Returns a formatted work telephone number for a party.
     *
     * @param party the party
     * @return a formatted telephone number for the party. May be empty if
     *         there is no corresponding <em>contact.phoneNumber</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getWorkTelephone(Party party) {
        Contact contact = getContact(party, ContactArchetypes.PHONE, "WORK",
                                     true);
        return (contact != null) ? formatPhone(contact) : "";
    }

    /**
     * Returns a formatted work telephone number for a party associated with
     * an act via an <em>participation.customer</em> participation.
     *
     * @param act the act
     * @return a formatted work telephone number for the party. May be empty if
     *         there is no customer, or corresponding
     *         <em>contact.phoneNumber</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getWorkTelephone(Act act) {
        Party party = getCustomer(act);
        if (party == null) {
            party = getOwner(act);
        }
        return (party != null) ? getWorkTelephone(party) : "";
    }

    /**
     * Returns a telephone number for a party that has sms enabled.
     *
     * @param party the party
     * @return a formatted telephone number for the party. May be empty if
     *         there is no corresponding <em>contact.phoneNumber</em> contact with sms set {@code true}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getSMSTelephone(Party party) {
        Contact contact = (party != null) ? getContact(party, new SMSMatcher()) : null;
        return (contact != null) ? formatPhone(contact) : "";
    }

    /**
     * Returns a formatted fax number for a party.
     *
     * @param party the party
     * @return a formatted fax number for a party. May be empty if
     *         there is no corresponding <em>contact.faxNumber</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getFaxNumber(Party party) {
        Contact contact = getContact(party, ContactArchetypes.FAX, null);
        if (contact != null) {
            IMObjectBean bean = new IMObjectBean(contact, service);
            String areaCode = bean.getString("areaCode");
            String faxNumber = bean.getString("faxNumber");
            if (areaCode == null || areaCode.equals("")) {
                return faxNumber;
            } else {
                return "(" + areaCode + ") " + faxNumber;
            }
        }
        return "";
    }

    /**
     * Returns a formatted email address for a party.
     *
     * @param party the party
     * @return a formatted email address for a party. May be empty if
     *         there is no corresponding <em>contact.email</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getEmailAddress(Party party) {
        Contact contact = getContact(party, ContactArchetypes.EMAIL, null);
        if (contact != null) {
            IMObjectBean bean = new IMObjectBean(contact, service);
            return bean.getString("emailAddress");
        }
        return "";
    }

    /**
     * Returns a formatted string of a party's identities.
     *
     * @param party the party
     * @return a formatted string of the party's identities
     */
    public String getIdentities(Party party) {
        StringBuilder result = new StringBuilder();
        for (EntityIdentity identity : party.getIdentities()) {
            IMObjectBean bean = new IMObjectBean(identity, service);
            if (bean.hasNode("name")) {
                String name = bean.getString("name");
                String displayName = bean.getDisplayName();
                if (name != null) {
                    if (result.length() != 0) {
                        result.append(", ");
                    }
                    result.append(displayName);
                    result.append(": ");
                    result.append(name);
                }
            }
        }
        return result.toString();
    }

    /**
     * Returns a formatted name for a <em>party.customerPerson</em>.
     *
     * @param party the party
     * @return a formatted name
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if the title lookup is incorrect
     */
    private String getPersonName(Party party) {

        StringBuilder result = new StringBuilder();
        if (party != null) {
            IMObjectBean bean = new IMObjectBean(party, service);
            NodeDescriptor descriptor = bean.getDescriptor("title");
            String title = LookupHelper.getName(service, descriptor, party);
            String firstName = bean.getString("firstName", "");
            String lastName = bean.getString("lastName", "");
            if (title != null) {
                result.append(title).append(" ");
            }
            result.append(firstName).append(" ").append(lastName);
        }
        return result.toString();
    }

    /**
     * Returns a contact for the specified party, contact type and purpose.
     * If cannot find one with matching purpose returns last preferred contact.
     * If cannot find with matching purpose and preferred returns last found.
     *
     * @param party   the party
     * @param type    the contact archetype shortname
     * @param purpose the contact purpose. May be {@code null}
     * @return the corresponding contact, or {@code null}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Contact getContact(Party party, String type, String purpose) {
        return getContact(party, type, purpose, false);
    }

    /**
     * Returns the customer associated with an act via an
     * <em>participation.customer</em> participation.
     *
     * @param act the act
     * @return the customer, or {@code null} if none is present
     */
    public Party getCustomer(Act act) {
        ActBean bean = new ActBean(act, service);
        return (Party) bean.getParticipant("participation.customer");
    }

    /**
     * Looks for the contact that best matches the criteria.
     *
     * @param party   the party
     * @param type    the contact type
     * @param purpose the contact purpose. May be {@code null}
     * @param exact   if {@code true}, the contact must have the specified purpose
     * @return the matching contact or {@code null}
     */
    private Contact getContact(Party party, String type, String purpose, boolean exact) {
        return (party != null) ? getContact(party, new PurposeMatcher(type, purpose, exact)) : null;
    }

    private Contact getContact(Party party, ContactMatcher matcher) {
        Contact result;
        for (Contact contact : sort(party.getContacts())) {
            if (matcher.matches(contact)) {
                break;
            }
        }
        result = matcher.getMatch();
        return result;
    }

    /**
     * Formats an address from an <em>contact.location</em> contact.
     *
     * @param contact contact
     * @return a formatted address
     * @throws ArchetypeServiceException for any archetype service error
     */
    private String formatAddress(Contact contact) {
        return formatAddress(contact, false);
    }

    /**
     * Formats an address from an <em>contact.location</em> contact.
     *
     * @param contact    contact
     * @param singleLine if {@code true} formats the address on a single line
     * @return a formatted address
     * @throws ArchetypeServiceException for any archetype service error
     */
    private String formatAddress(Contact contact, boolean singleLine) {
        IMObjectBean bean = new IMObjectBean(contact, service);
        StringBuilder result = new StringBuilder();
        if (singleLine) {
            result.append(bean.getString("address", "").replace('\n', ' '));
            result.append(" ");
        } else {
            result.append(bean.getString("address", ""));
            result.append("\n");
        }
        String suburb = ArchetypeServiceFunctions.lookup(contact, "suburb", "");
        if (!StringUtils.isEmpty(suburb)) {
            result.append(suburb);
            result.append(" ");
        }
        String state = ArchetypeServiceFunctions.lookup(contact, "state", "");
        if (!StringUtils.isEmpty(state)) {
            result.append(state);
            result.append(" ");
        }
        result.append(bean.getString("postcode", ""));
        return result.toString();
    }

    /**
     * Returns a formatted telephone number from a <em>contact.phoneNumber</em>.
     *
     * @param contact the contact
     * @return a formatted telephone number
     */
    private String formatPhone(Contact contact) {
        IMObjectBean bean = new IMObjectBean(contact, service);
        String areaCode = bean.getString("areaCode");
        String phone = bean.getString("telephoneNumber", "");
        if (StringUtils.isEmpty(areaCode)) {
            return phone;
        } else {
            return "(" + areaCode + ") " + phone;
        }
    }

    /**
     * Returns the Practice party
     *
     * @return the practice party object
     */
    public Party getPractice() {
        // First get the Practice.  Should only be one but get first if more.
        List<IMObject> rows = ArchetypeQueryHelper.get(
                service, "party", "organisationPractice", null, true,
                0, 1).getResults();
        if (!rows.isEmpty()) {
            return (Party) rows.get(0);
        } else {
            return null;
        }
    }

    /**
     * Returns the Practice address
     *
     * @return the practice address string
     */
    public String getPracticeAddress() {
        return formatAddress(
                getContact(getPractice(), ContactArchetypes.LOCATION, null), true);
    }

    /**
     * Returns the Practice phone number
     *
     * @return the practice phone string
     */
    public String getPracticeTelephone() {
        return getWorkTelephone(getPractice());
    }

    /**
     * Returns the Practice fax number
     *
     * @return the practice fax string
     */
    public String getPracticeFaxNumber() {
        return getFaxNumber(getPractice());
    }

    /**
     * Returns a Bpay Id for the Party.
     * Utilises the party uid and adds a check digit using a Luntz 10 algorithm.
     *
     * @param party the party
     * @return string bpay id
     */
    public String getBpayId(Party party) {
        // this will be a running total
        int sum = 0;
        // Get string value of party uid
        String uid = String.valueOf(party.getId());

        // loop through digits from right to left
        for (int i = 0; i < uid.length(); i++) {

            //set ch to "current" character to be processed
            char ch = uid.charAt(uid.length() - i - 1);

            // our "digit" is calculated using ASCII value - 48
            int digit = (int) ch - 48;

            // weight will be the current digit's contribution to
            // the running total
            int weight;
            if (i % 2 == 0) {

                // for alternating digits starting with the rightmost, we
                // use our formula this is the same as multiplying x 2 and
                // adding digits together for values 0 to 9.  Using the
                // following formula allows us to gracefully calculate a
                // weight for non-numeric "digits" as well (from their
                // ASCII value - 48).
                weight = (2 * digit) - (digit / 5) * 9;
            } else {

                // even-positioned digits just contribute their ascii
                // value minus 48
                weight = digit;

            }

            // keep a running total of weights
            sum += weight;
        }
        // avoid sum less than 10 (if characters below "0" allowed,
        // this could happen)
        sum = (10 - ((Math.abs(sum) + 10) % 10)) % 10;

        return uid + String.valueOf(sum);
    }

    protected IArchetypeService getArchetypeService() {
        return service;
    }

    /**
     * Returns the patient owner for the patient referenced by an act.
     *
     * @param act the act
     * @return the patient owner. May be {@code null}
     */
    private Party getOwner(Act act) {
        return new PatientRules(service, null, null).getOwner(act);
    }

    /**
     * Returns a concatenated list of values for a set of objects.
     *
     * @param objects the objects
     * @param node    the node name
     * @return the stringified value of {@code node} for each object, separated by ", "
     * @throws ArchetypeServiceException for any archetype service error
     */
    private String getValues(List<IMObject> objects, String node) {
        StringBuilder result = new StringBuilder();

        for (IMObject object : objects) {
            IMObjectBean bean = new IMObjectBean(object, service);
            if (bean.hasNode(node)) {
                if (result.length() != 0) {
                    result.append(", ");
                }
                result.append(bean.getString(node));
            }
        }
        return result.toString();
    }

    /**
     * Sorts contacts by id.
     *
     * @param contacts the contacts to sort
     * @return the sorted contacts
     */
    private List<Contact> sort(Set<Contact> contacts) {
        List<Contact> list = new ArrayList<Contact>(contacts);
        if (list.size() > 1) {
            Collections.sort(list, new Comparator<Contact>() {
                @Override
                public int compare(Contact o1, Contact o2) {
                    return o1.getId() < o2.getId() ? -1 : o1.getId() == o2.getId() ? 0 : 1;
                }
            });
        }
        return list;
    }

    /**
     * Helper to find a contact matching some criteria.
     */
    private abstract class ContactMatcher {

        /**
         * The contact archetype short name.
         */
        private final String shortName;

        /**
         * The contacts matching some or all of the criteria, keyed on
         * priority, where the 0 is the highest priority.
         */
        private SortedMap<Integer, Contact> contacts
                = new TreeMap<Integer, Contact>();

        /**
         * Constructs a new {@code ContactMatcher}.
         *
         * @param shortName the contact archetype short name
         */
        public ContactMatcher(String shortName) {
            this.shortName = shortName;
        }

        /**
         * Determines if a contact matches the criteria.
         *
         * @param contact the contact
         * @return {@code true} if the contact is an exact match; otherwise
         *         {@code false}
         */
        public boolean matches(Contact contact) {
            return TypeHelper.isA(contact, shortName);
        }

        /**
         * Returns the contact that best matches the criteria.
         *
         * @return the contact that best matches the criteria, or {@code null}
         */
        public Contact getMatch() {
            Integer best = null;
            if (!contacts.isEmpty()) {
                best = contacts.firstKey();
            }
            return (best != null) ? contacts.get(best) : null;
        }

        /**
         * Registers a contact that matches some/all of the criteria, if none with the same priority is present.
         *
         * @param priority the priority, where {@code 0} is the highest priority.
         * @param contact  the contact
         */
        protected void setMatch(int priority, Contact contact) {
            if (contacts.get(priority) == null) {
                contacts.put(priority, contact);
            }
        }

        /**
         * Determines if a contact has a preferred node with value 'true'.
         *
         * @param contact the contact
         * @return {@code true} if the contact is preferred
         */
        protected boolean isPreferred(Contact contact) {
            IMObjectBean bean = new IMObjectBean(contact, service);
            return bean.hasNode("preferred") && bean.getBoolean("preferred");
        }
    }

    /**
     * Matches contacts on purpose.
     */
    private class PurposeMatcher extends ContactMatcher {

        /**
         * The purpose to match on.
         */
        private final String purpose;

        /**
         * If {@code true} the contact must contain the purpose to be returned
         */
        private final boolean exact;

        /**
         * Constructs a new {@code PurposeMatcher}.
         *
         * @param shortName the contact archetype short name
         * @param purpose   the purpose. May be {@code null}
         * @param exact     if {@code true} the contact must contain the purpose
         *                  in order to be considered a match
         */
        public PurposeMatcher(String shortName, String purpose, boolean exact) {
            super(shortName);
            this.purpose = purpose;
            this.exact = exact;
        }

        @Override
        public boolean matches(Contact contact) {
            boolean best = false;
            if (super.matches(contact)) {
                boolean preferred = isPreferred(contact);
                if (purpose != null) {
                    if (hasContactPurpose(contact, purpose)) {
                        if (preferred) {
                            setMatch(0, contact);
                            best = true;
                        } else {
                            setMatch(1, contact);
                        }
                    } else if (!exact) {
                        if (preferred) {
                            setMatch(2, contact);
                        } else {
                            setMatch(3, contact);
                        }
                    }
                } else {
                    if (preferred) {
                        setMatch(0, contact);
                        best = true;
                    } else {
                        setMatch(1, contact);
                    }
                }
            }
            return best;
        }

        /**
         * Determines if a contact has a particular purpose.
         *
         * @param contact the contact
         * @param purpose the contact purpose
         * @return {@code true} if the contact has the specified purpose,
         *         otherwise {@code false}
         */
        private boolean hasContactPurpose(Contact contact, String purpose) {
            for (Lookup classification : contact.getClassifications()) {
                if (classification.getCode().equals(purpose)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Matches phone contacts if they have sms enabled.
     */
    private class SMSMatcher extends ContactMatcher {

        /**
         * Constructs an {@link SMSMatcher}.
         */
        public SMSMatcher() {
            super(ContactArchetypes.PHONE);
        }

        @Override
        public boolean matches(Contact contact) {
            boolean result = super.matches(contact);
            if (result) {
                IMObjectBean bean = new IMObjectBean(contact, service);
                if (bean.getBoolean("sms")) {
                    if (isPreferred(contact)) {
                        result = true;
                        setMatch(0, contact);
                    } else {
                        result = false;
                        setMatch(1, contact);
                    }
                } else {
                    result = false;
                }
            }
            return result;
        }
    }
}
