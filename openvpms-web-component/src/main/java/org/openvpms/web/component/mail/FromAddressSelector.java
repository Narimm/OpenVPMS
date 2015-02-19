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
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.factory.TextComponentFactory;
import org.openvpms.web.resource.i18n.Messages;

import java.util.List;

/**
 * Selector for the from address.
 *
 * @author Tim Anderson
 */
public class FromAddressSelector extends AddressSelector {

    /**
     * The selected contact.
     */
    private Contact selected;

    /**
     * Constructs an {@link FromAddressSelector}.
     *
     * @param contacts  the available contacts
     * @param formatter the address formatter
     */
    public FromAddressSelector(List<Contact> contacts, AddressFormatter formatter) {
        super(contacts, formatter, TextComponentFactory.create(40));
        getField().setEnabled(false);
    }

    /**
     * Sets the selected contact.
     *
     * @param contact the contact. May be {@code null}
     */
    @Override
    public void setSelected(Contact contact) {
        super.setSelected(contact);
        this.selected = contact;
        getField().setText(getFormatter().getNameAddress(contact));
    }

    /**
     * Returns the selected contact.
     *
     * @return the selected contact. May be {@code null}
     */
    @Override
    public Contact getSelected() {
        return selected;
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        boolean valid = false;
        if (selected != null) {
            valid = true;
        } else {
            validator.add(this, new ValidatorError(Messages.get("mail.nofromaddress")));
        }
        return valid;
    }
}
