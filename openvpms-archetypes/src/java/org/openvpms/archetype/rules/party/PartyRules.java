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

package org.openvpms.archetype.rules.party;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.contact.AddressFormatter;
import org.openvpms.archetype.rules.contact.BasicAddressFormatter;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.LookupHelperException;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.model.entity.EntityIdentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The address formatter.
     */
    private final AddressFormatter addressFormatter;

    /**
     * Constructs a {@link PartyRules}.
     *
     * @param service the archetype service
     * @param lookups the lookup service
     */
    public PartyRules(IArchetypeService service, ILookupService lookups) {
        this(service, lookups, new BasicAddressFormatter(service, lookups));
    }

    /**
     * Constructs a {@link PartyRules}.
     *
     * @param service   the archetype service
     * @param lookups   the lookup service
     * @param formatter the address formatter
     */
    public PartyRules(IArchetypeService service, ILookupService lookups, AddressFormatter formatter) {
        this.service = service;
        this.lookups = lookups;
        this.addressFormatter = formatter;
    }

    /**
     * Returns a specified node for a specified customer.
     *
     * @param party    the party
     * @param nodeName the name of the node
     * @return a node object may be null if no customer or invalid node
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Deprecated
    public Object getCustomerNode(Party party, String nodeName) {
        if (party != null) {
            IMObjectBean bean = new IMObjectBean(party, service);
            NodeDescriptor descriptor = bean.getDescriptor(nodeName);
            if (descriptor != null && descriptor.isLookup()) {
                return lookups.getName(party, nodeName);
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
        return getFullName(party, true);
    }

    /**
     * Returns the formatted full name of a party.
     *
     * @param party        the party
     * @param includeTitle if {@code true} include the person's title
     * @return the formatted full name of the party
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getFullName(Party party, boolean includeTitle) {
        String name = null;
        if (party != null) {
            IMObjectBean bean = new IMObjectBean(party, service);
            if (bean.hasNode("companyName") && (bean.getString("companyName") != null)) {
                name = party.getName();
            } else if (bean.hasNode("title") && bean.hasNode("firstName") && bean.hasNode("lastName")) {
                name = getPersonName(bean, includeTitle);
            } else {
                name = party.getName();
            }
        }
        return (name != null) ? name : "";
    }

    /**
     * Returns a set of default contacts containing an
     * <em>contact.phoneNumber</em> and <em>contact.location</em>.
     *
     * @return a new set of default contacts
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Set<org.openvpms.component.model.party.Contact> getDefaultContacts() {
        Set<org.openvpms.component.model.party.Contact> contacts = new HashSet<>();
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
     * <p>
     * If there are multiple preferred contacts, these are sorted on identifier.
     *
     * @param party the party
     * @return a formatted string of preferred contacts
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getPreferredContacts(Party party) {
        StringBuilder result = new StringBuilder();
        for (org.openvpms.component.model.party.Contact contact : Contacts.sort(party.getContacts())) {
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
            List<IMObject> list = bean.getValues("purposes", IMObject.class);
            if (!list.isEmpty()) {
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
     * @param party      the party. May be {@code null}
     * @param singleLine if {@code true}, return the address as a single line
     * @return a formatted billing address for a party. May be empty if
     * there is no corresponding <em>contact.location</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getBillingAddress(Party party, boolean singleLine) {
        return getAddress(party, ContactArchetypes.BILLING_PURPOSE, singleLine);
    }

    /**
     * Returns a formatted correspondence address for a party.
     *
     * @param party      the party
     * @param singleLine if {@code true}, return the address as a single line
     * @return a formatted correspondence address for a party. May be empty if there is no corresponding
     * <em>contact.location</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getCorrespondenceAddress(Party party, boolean singleLine) {
        return getAddress(party, ContactArchetypes.CORRESPONDENCE_PURPOSE, singleLine);
    }

    /**
     * Returns a formatted correspondence name and address for a party.
     *
     * @param party      the party
     * @param singleLine if {@code true}, return the address as a single line
     * @return a formatted correspondence name and address for a party. May be empty if
     * there is no corresponding <em>contact.location</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getCorrespondenceNameAddress(Party party, boolean singleLine) {
        return getFullName(party) + "\n" + getAddress(party, ContactArchetypes.CORRESPONDENCE_PURPOSE, singleLine);
    }

    /**
     * Returns an address for a party.
     * <p>
     * If it cannot find the specified purpose, it uses the preferred location contact or
     * any location contact if there is no preferred.
     *
     * @param party   the party
     * @param purpose the contact purpose
     * @return the contact, or {@code null} if none is found
     */
    public Contact getAddressContact(Party party, String purpose) {
        return getContact(party, ContactArchetypes.LOCATION, purpose);
    }

    /**
     * Returns a formatted <em>contact.location</em> address with the specified purpose for a party.
     * <br/>
     * If it cannot find the specified purpose, it uses the preferred location contact or
     * any location contact if there is no preferred.
     *
     * @param party      the party
     * @param purpose    the contact purpose of the address
     * @param singleLine if {@code true}, return the address as a single line
     * @return a formatted address. May be empty if there is no corresponding <em>contact.location</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getAddress(Party party, String purpose, boolean singleLine) {
        return formatAddress(getAddressContact(party, purpose), singleLine);
    }

    /**
     * Returns the preferred telephone contact for a party.
     *
     * @param party the party. May be {@code null}
     * @return the preferred contact, or {@code null} if there is no corresponding <em>contact.phoneNumber</em> contact
     */
    public Contact getTelephoneContact(Party party) {
        return getContact(party, ContactArchetypes.PHONE, false, ContactArchetypes.FAX_PURPOSE);
    }

    /**
     * Returns the telephone contact for a party.
     * <p>
     * This will return a phone contact with the specified purpose, or any phone contact if there is none.
     *
     * @param party   the party. May be {@code null}
     * @param purpose the contact purpose
     * @return the preferred contact, or {@code null} if there is no corresponding <em>contact.phoneNumber</em> contact
     */
    public Contact getTelephoneContact(Party party, String purpose) {
        return getTelephoneContact(party, false, purpose);
    }

    /**
     * Returns the telephone contact for a party.
     *
     * @param party   the party. May be {@code null}
     * @param exact   if {@code true}, the contact must have the specified purpose
     * @param purpose the contact purpose
     * @return the preferred contact, or {@code null} if there is no corresponding <em>contact.phoneNumber</em> contact
     */
    public Contact getTelephoneContact(Party party, boolean exact, String purpose) {
        return getContact(party, ContactArchetypes.PHONE, exact, ContactArchetypes.FAX_PURPOSE, purpose);
    }

    /**
     * Returns a formatted preferred telephone number for a party.
     *
     * @param party the party. May be {@code null}
     * @return a formatted telephone number for the party. May be empty if there is no corresponding
     * <em>contact.phoneNumber</em> contact
     */
    public String getTelephone(Party party) {
        return getTelephone(party, false);
    }

    /**
     * Returns a formatted preferred telephone number for a party.
     *
     * @param party    the party. May be {@code null}
     * @param withName if {@code true} includes the name, if it is not the default value for the contact
     * @return a formatted telephone number for the party. May be empty if there is no corresponding
     * <em>contact.phoneNumber</em> contact
     */
    public String getTelephone(Party party, boolean withName) {
        Contact contact = getTelephoneContact(party);
        return (contact != null) ? formatPhone(contact, withName) : "";
    }

    /**
     * Returns a formatted home telephone number for a party.
     * <p>
     * This will return a phone contact with HOME purpose, or any phone contact if there is none.
     *
     * @param party the party
     * @return a formatted home telephone number for the party. May be empty if there is no corresponding
     * <em>contact.phoneNumber</em> contact
     */
    public String getHomeTelephone(Party party) {
        Contact contact = getTelephoneContact(party, ContactArchetypes.HOME_PURPOSE);
        return (contact != null) ? formatPhone(contact, false) : "";
    }

    /**
     * Returns a formatted mobile telephone number for a party.
     *
     * @param party the party
     * @return a formatted mobile telephone number for the party. May be empty if there is no corresponding
     * <em>contact.phoneNumber</em> contact
     */
    public String getMobileTelephone(Party party) {
        Contact contact = getTelephoneContact(party, true, ContactArchetypes.MOBILE_PURPOSE);
        return (contact != null) ? formatPhone(contact, false) : "";
    }

    /**
     * Returns a formatted work telephone number for a party.
     *
     * @param party the party
     * @return a formatted telephone number for the party. May be empty if
     * there is no corresponding <em>contact.phoneNumber</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getWorkTelephone(Party party) {
        Contact contact = getTelephoneContact(party, true, ContactArchetypes.WORK_PURPOSE);
        return (contact != null) ? formatPhone(contact, false) : "";
    }

    /**
     * Returns a telephone number for a party that has sms enabled.
     *
     * @param party the party
     * @return a formatted telephone number for the party. May be empty if
     * there is no corresponding <em>contact.phoneNumber</em> contact with sms set {@code true}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getSMSTelephone(Party party) {
        Contact contact = getSMSContact(party);
        return (contact != null) ? formatPhone(contact, false) : "";
    }

    /**
     * Returns an SMS contact for a party.
     *
     * @param party the party
     * @return a <em>contact.phoneNumber</em> contact with sms set {@code true}, or {@code} null if none exists
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Contact getSMSContact(Party party) {
        return (party != null) ? getContact(party, new SMSMatcher(service)) : null;
    }

    /**
     * Returns a formatted fax number for a party.
     *
     * @param party the party
     * @return a formatted fax number for a party. May be empty if
     * there is no corresponding <em>contact.phoneNumber</em> contact
     * with a purpose as FAX
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getFaxNumber(Party party) {
        Contact contact = getContact(party, ContactArchetypes.PHONE, true, null, ContactArchetypes.FAX_PURPOSE);
        return (contact != null) ? formatPhone(contact, false) : "";
    }

    /**
     * Returns a formatted email address for a party.
     *
     * @param party the party
     * @return a formatted email address for a party. May be empty if
     * there is no corresponding <em>contact.email</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getEmailAddress(Party party) {
        Contact contact = getEmailContact(party);
        if (contact != null) {
            IMObjectBean bean = new IMObjectBean(contact, service);
            return bean.getString("emailAddress");
        }
        return "";
    }

    /**
     * Returns the preferred email contact for a party.
     *
     * @param party the party. May be {@code null}
     * @return the preferred contact, or {@code null} if there is no corresponding <em>contact.email</em> contact
     */
    public Contact getEmailContact(Party party) {
        return getContact(party, ContactArchetypes.EMAIL, null);
    }

    /**
     * Returns the website URL for a party.
     *
     * @param party the party
     * @return the website URL of the party. May be empty if there is no corresponding <em>contact.website</em> contact
     * @throws ArchetypeServiceException for any archetype service error
     */
    public String getWebsite(Party party) {
        Contact contact = getContact(party, ContactArchetypes.WEBSITE, null);
        if (contact != null) {
            IMObjectBean bean = new IMObjectBean(contact, service);
            return bean.getString("url");
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
     * Returns a contact for the specified party, contact type and purpose.
     * If cannot find one with matching purpose returns last preferred contact.
     * If cannot find with matching purpose and preferred returns last found.
     *
     * @param party   the party. May be {@code null}
     * @param type    the contact archetype shortname
     * @param purpose the contact purpose. May be {@code null}
     * @return the corresponding contact, or {@code null}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Contact getContact(Party party, String type, String purpose) {
        Contact result = null;
        if (party != null) {
            if (purpose != null) {
                result = getContact(party, type, false, null, purpose);
            } else {
                result = getContact(party, type, false, null);
            }
        }
        return result;
    }

    /**
     * Returns the practice.
     *
     * @return the practice. May be {@code null}
     */
    public Party getPractice() {
        return PracticeRules.getPractice(service);
    }

    /**
     * Returns the practice address.
     *
     * @param singleLine if {@code true}, return the address as a single line string, otherwise as a multi-line string
     * @return the practice address string
     */
    public String getPracticeAddress(boolean singleLine) {
        return formatAddress(getContact(getPractice(), ContactArchetypes.LOCATION, null), singleLine);
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
     * Returns the practice fax number
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

    /**
     * Looks for the contact that best matches the criteria.
     *
     * @param party     the party. May be {@code null}
     * @param type      the contact type
     * @param exact     if {@code true}, the contact must have the specified purpose
     * @param exclusion if present will exclude contacts with this purpose. May be {@code null}
     * @param purposes  the purposes to match, if any
     * @return the matching contact or {@code null}
     */
    public Contact getContact(Party party, String type, boolean exact, String exclusion, String... purposes) {
        Contact contact = null;
        if (party != null) {
            PurposeMatcher matcher = new PurposeMatcher(type, exact, service, purposes);
            matcher.setExclusion(exclusion);
            contact = getContact(party, matcher);
        }
        return contact;
    }

    /**
     * Formats an address.
     *
     * @param contact    the location contact. May be {@code null}
     * @param singleLine if {@code true}, return the address as a single line
     * @return the address, or an empty string if contact is not supplied, or cannot be formatted
     */
    public String formatAddress(Contact contact, boolean singleLine) {
        String result = null;
        if (contact != null) {
            result = addressFormatter.format(contact, singleLine);
        }
        if (result == null) {
            result = "";
        }
        return result;
    }

    /**
     * Returns a formatted telephone number from a <em>contact.phoneNumber</em>.
     *
     * @param contact  the contact
     * @param withName if {@code true} includes the name, if it is not the default value for the contact
     * @return a formatted telephone number
     */
    public String formatPhone(Contact contact, boolean withName) {
        IMObjectBean bean = new IMObjectBean(contact, service);
        String areaCode = bean.getString("areaCode");
        String phone = bean.getString("telephoneNumber", "");
        if (withName) {
            String name = contact.getName();
            if (!StringUtils.isEmpty(name) && bean.hasNode("name") && !bean.isDefaultValue("name")) {
                phone += " (" + name + ")";
            }
        }

        if (StringUtils.isEmpty(areaCode)) {
            return phone;
        } else {
            return "(" + areaCode + ") " + phone;
        }
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getArchetypeService() {
        return service;
    }

    /**
     * Returns the lookup service.
     *
     * @return the lookup service
     */
    protected ILookupService getLookupService() {
        return lookups;
    }

    /**
     * Returns a formatted name for a bean with title, firstName, and lastName nodes.
     *
     * @param bean         the bean
     * @param includeTitle if {@code true} include the person's title
     * @return a formatted name
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if the title lookup is incorrect
     */
    private String getPersonName(IMObjectBean bean, boolean includeTitle) {
        StringBuilder result = new StringBuilder();
        String title = (includeTitle) ? lookups.getName(bean.getObject(), "title") : null;
        String firstName = bean.getString("firstName", "");
        String lastName = bean.getString("lastName", "");
        if (title != null) {
            result.append(title).append(" ");
        }
        result.append(firstName).append(" ").append(lastName);
        return result.toString();
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
        List<String> values = new ArrayList<>();

        for (IMObject object : objects) {
            IMObjectBean bean = new IMObjectBean(object, service);
            if (bean.hasNode(node)) {
                String value = bean.getString(node, "");
                values.add(value);
            }
        }
        if (values.size() > 1) {
            Collections.sort(values);
        }
        for (String value : values) {
            if (result.length() != 0) {
                result.append(", ");
            }
            result.append(value);
        }
        return result.toString();
    }

    /**
     * Looks for a party contact that matches the criteria.
     *
     * @param party   the party
     * @param matcher the contact matcher
     * @return the matching contact or {@code null}
     */
    private Contact getContact(Party party, ContactMatcher matcher) {
        return Contacts.find(Contacts.sort(party.getContacts()), matcher);
    }

}
