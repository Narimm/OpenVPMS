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

import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.layout.LayoutContext;

import java.util.List;

/**
 * Selector for to addresses.
 *
 * @author Tim Anderson
 */
class ToAddressSelector extends AddressSelector {

    /**
     * The contact selector.
     */
    private MultiContactSelector selector;

    /**
     * Constructs a {@link ToAddressSelector}.
     *
     * @param contacts  the available contacts
     * @param formatter the address formatter
     * @param context   the layout context
     */
    public ToAddressSelector(List<Contact> contacts, AddressFormatter formatter, LayoutContext context) {
        super(contacts, formatter);
        selector = new MultiContactSelector(formatter, context);
        setField(selector.getTextField());
    }

    /**
     * Sets the selected contact.
     *
     * @param contact the contact. May be {@code null}
     */
    @Override
    public void setSelected(Contact contact) {
        super.setSelected(contact);
        selector.setObject(contact);
    }

    /**
     * Returns the selected contact.
     *
     * @return the selected contact. May be {@code null}
     */
    @Override
    public Contact getSelected() {
        List<Contact> objects = selector.getObjects();
        return !objects.isEmpty() ? objects.get(0) : null;
    }

    /**
     * Returns the addresses.
     *
     * @return the addresses
     */
    public String[] getAddresses() {
        List<Contact> contacts = selector.getObjects();
        String[] result = (!contacts.isEmpty()) ? new String[contacts.size()] : null;
        if (result != null) {
            for (int i = 0; i < contacts.size(); ++i) {
                IMObjectBean bean = new IMObjectBean(contacts.get(i));
                result[i] = bean.getString("emailAddress");
            }
        }
        return result;
    }

    /**
     * Determines if the selector is valid.
     *
     * @return {@code true} if the selector is valid
     */
    public boolean isValid() {
        return selector.isValid();
    }
}
