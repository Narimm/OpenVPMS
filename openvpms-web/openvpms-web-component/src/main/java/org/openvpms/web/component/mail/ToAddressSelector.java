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
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;

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
     * The localisation key.
     */
    private final String key;

    /**
     * Constructs a {@link ToAddressSelector}.
     *
     * @param contacts  the available contacts
     * @param formatter the address formatter
     * @param context   the layout context
     * @param key       localisation key
     */
    public ToAddressSelector(List<Contact> contacts, AddressFormatter formatter, LayoutContext context, String key) {
        super(contacts, formatter);
        selector = new MultiContactSelector(formatter, context);
        setField(selector.getTextField());
        this.key = key;
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
        String[] result = null;
        if (selector.isValid()) {
            List<Contact> contacts = selector.getObjects();
            result = (!contacts.isEmpty()) ? new String[contacts.size()] : null;
            if (result != null) {
                AddressFormatter formatter = getFormatter();
                for (int i = 0; i < contacts.size(); ++i) {
                    result[i] = formatter.getNameAddress(contacts.get(i), true);
                }
            }
        }
        return result;
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        boolean valid = selector.isValid();
        if (!valid) {
            String name = selector.getFirstNotFound();
            if (name != null) {
                validator.add(this, new ValidatorError(Messages.format("mail.notfound", Messages.get(key), name)));
            }
        }
        return valid;
    }
}
