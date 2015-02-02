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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.mail;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.resource.i18n.Messages;

/**
 * Abstract implementation of {@link AddressFormatter}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractAddressFormatter implements AddressFormatter {

    /**
     * The name node default value.
     */
    private final String defaultValue;

    /**
     * Constructs an {@link AbstractAddressFormatter}.
     */
    public AbstractAddressFormatter() {
        ArchetypeDescriptor archetypeDescriptor = DescriptorHelper.getArchetypeDescriptor(ContactArchetypes.EMAIL);
        String value = null;
        if (archetypeDescriptor != null) {
            NodeDescriptor descriptor = archetypeDescriptor.getNodeDescriptor("name");
            if (descriptor != null) {
                value = descriptor.getDefaultValue();
                if (value != null) {
                    // defaultValue is an xpath expression. Rather than evaluating it, just support the simple case of
                    // a quoted string.
                    value = StringUtils.strip(value, "'");
                }
                if (StringUtils.isEmpty(value)) {
                    value = null;
                }
            }
        }
        defaultValue = value;
    }

    /**
     * Returns the email address of a contact.
     *
     * @param contact the email contact. May be {@code null}
     * @return the the contact's email address, or {@code null} if none is found
     */
    public String getAddress(Contact contact) {
        if (contact != null) {
            IMObjectBean bean = new IMObjectBean(contact);
            return bean.getString("emailAddress");
        }
        return null;
    }

    /**
     * Returns the name of a contact. If the contact name has been customised from its default, this will be returned,
     * otherwise the name of the associated party will be returned.
     *
     * @param contact the contact
     * @return the name. May be {@code null}
     */
    public String getName(Contact contact) {
        Party party = contact.getParty();
        return (party != null) ? party.getName() : null;
    }

    /**
     * Returns the qualified name.
     * <p/>
     * This includes the party name and contact name, if a contact name is specified.
     *
     * @param contact the email contact
     * @return the contact name. May be {@code null}
     */
    @Override
    public String getQualifiedName(Contact contact) {
        String name = contact.getName();
        String partyName = getName(contact);
        if (StringUtils.isEmpty(name) || StringUtils.equals(defaultValue, name)) {
            name = partyName;
        } else {
            name = (partyName != null) ? Messages.format("mail.qualifiedname", partyName, name) : name;
        }
        return name;
    }

    /**
     * Returns the contact name and address.
     *
     * @return the contact name and address. May {@code null}
     */
    @Override
    public String getNameAddress(Contact contact) {
        String name = getName(contact);
        return getNameAddress(contact, name);
    }

    /**
     * Returns the qualified name and address.
     * <p/>
     * This includes the party name and contact name, if a contact name is specified.
     *
     * @param contact the email contact
     * @return the qualified name and address
     */
    @Override
    public String getQualifiedNameAddress(Contact contact) {
        String name = getQualifiedName(contact);
        return getNameAddress(contact, name);
    }

    /**
     * Returns the type of a contact.
     *
     * @param contact the contact
     * @return the type of the contact. May be {@code null}
     */
    @Override
    public String getType(Contact contact) {
        return (contact.getParty() != null) ? DescriptorHelper.getDisplayName(contact.getParty()) : null;
    }

    /**
     * Returns the contact name and address.
     *
     * @param contact the contact
     * @param name    the contact name
     * @return the contact name and address
     */
    private String getNameAddress(Contact contact, String name) {
        String result;
        String address = getAddress(contact);
        if (name != null) {
            if (address != null) {
                result = Messages.format("mail.contact", name, address);
            } else {
                result = name;
            }
        } else {
            result = address;
        }
        return result;
    }

}
