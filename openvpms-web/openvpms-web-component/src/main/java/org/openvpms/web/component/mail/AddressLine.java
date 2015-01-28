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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.ListBox;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ListBoxFactory;
import org.openvpms.web.echo.popup.DropDown;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;

import static org.openvpms.web.echo.style.Styles.FULL_WIDTH;

/**
 * Email address line.
 *
 * @author Tim Anderson
 */
class AddressLine {

    /**
     * The address contacts.
     */
    private final List<Contact> contacts;

    /**
     * The address property.
     */
    private SimpleProperty property;

    /**
     * The property listener.
     */
    private ModifiableListener listener;

    /**
     * The address formatter.
     */
    private final AddressFormatter formatter;

    /**
     * The selected contact.
     */
    private Contact selectedContact;

    /**
     * The text field.
     */
    private final TextField field;

    /**
     * The address line component.
     */
    private Component component;

    /**
     * Constructs an {@link AddressLine}.
     *
     * @param name      the address property name
     * @param contacts  the available contacts
     * @param formatter the address formatter
     */
    public AddressLine(String name, List<Contact> contacts, AddressFormatter formatter) {
        this.contacts = contacts;
        this.formatter = formatter;

        this.property = MailHelper.createProperty(name, "mail." + name, true);
        this.property.setRequired(false);

        listener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onAddressChanged();
            }
        };
        property.addModifiableListener(listener);

        field = BoundTextComponentFactory.create(property, 40);
        field.setWidth(FULL_WIDTH);
        component = field;
        if (!contacts.isEmpty()) {
            final ListBox toAddressSelector = ListBoxFactory.create(contacts);
            if (contacts.size() > 1) {
                // don't default the selection as per OVPMS-1295
                toAddressSelector.getSelectionModel().clearSelection();
            } else {
                setAddress(contacts.get(0));
            }

            toAddressSelector.setWidth(FULL_WIDTH);
            toAddressSelector.setCellRenderer(new EmailCellRenderer(formatter));

            final DropDown toAddressDropDown = new DropDown();
            toAddressDropDown.setWidth(FULL_WIDTH);
            component = toAddressDropDown;
            toAddressDropDown.setTarget(field);
            toAddressDropDown.setPopUpAlwaysOnTop(true);
            toAddressDropDown.setFocusOnExpand(true);
            toAddressDropDown.setPopUp(toAddressSelector);
            toAddressSelector.addActionListener(new ActionListener() {
                public void onAction(ActionEvent event) {
                    setAddress((Contact) toAddressSelector.getSelectedValue());
                    toAddressDropDown.setExpanded(false);
                    // onModified(); TODO
                }
            });
        }
    }

    /**
     * Returns the address property.
     *
     * @return the address property
     */
    public Property getProperty() {
        return property;
    }

    /**
     * Validates the address line.
     *
     * @param validator the validator
     * @return {@code true} if the address line is valid
     */
    public boolean validate(Validator validator) {
        return validator.validate(property);
    }

    /**
     * Sets the address.
     *
     * @param contact the address contact
     */
    public void setAddress(Contact contact) {
        selectedContact = contact;
        property.removeModifiableListener(listener);
        try {
            String value = contact != null ? formatter.format(contact) : null;
            property.setValue(value);
        } finally {
            property.addModifiableListener(listener);
        }
    }

    /**
     * Returns the address.
     *
     * @return the address
     */
    public String getAddress() {
        return formatter.getAddress(selectedContact);
    }

    /**
     * Returns the address field.
     *
     * @return the address field
     */
    public TextField getField() {
        return field;
    }

    /**
     * Returns the address component.
     *
     * @return the component
     */
    public Component getComponent() {
        return component;
    }

    /**
     * Invoked when the 'to' address changes.
     */
    private void onAddressChanged() {
        String text = (String) property.getValue();
        Contact result = null;
        if (!StringUtils.isEmpty(text)) {
            int start = text.indexOf('<');
            int end = text.indexOf('>');
            if (start != -1 && end != -1) {
                text = text.substring(start + 1, end);
            }
            for (Contact contact : contacts) {
                String email = formatter.getAddress(contact);
                if (StringUtils.equals(text, email)) {
                    result = contact;
                    break;
                }
            }
            if (result == null) {
                //  contact not in the list, so create a temporary one to hold the email address
                result = (Contact) ServiceHelper.getArchetypeService().create(ContactArchetypes.EMAIL);
                IMObjectBean bean = new IMObjectBean(result);
                bean.setValue("emailAddress", text);
            }
        }
        selectedContact = result;
    }

}
