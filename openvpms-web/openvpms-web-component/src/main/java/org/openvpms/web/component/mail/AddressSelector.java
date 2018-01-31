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

package org.openvpms.web.component.mail;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.ListBox;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.DocumentEvent;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.property.AbstractModifiable;
import org.openvpms.web.component.property.ErrorListener;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.ModifiableListeners;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.DocumentListener;
import org.openvpms.web.echo.factory.ListBoxFactory;
import org.openvpms.web.echo.popup.DropDown;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;

import static org.openvpms.web.echo.style.Styles.FULL_WIDTH;

/**
 * Email address selector.
 *
 * @author Tim Anderson
 */
public abstract class AddressSelector extends AbstractModifiable {

    /**
     * The address formatter.
     */
    private final AddressFormatter formatter;

    /**
     * The listeners.
     */
    private final ModifiableListeners listeners = new ModifiableListeners();

    /**
     * The address field.
     */
    private TextField field;

    /**
     * The drop-down, if there are contacts.
     */
    private DropDown dropDown;

    /**
     * The drop-down list, if there are contacts.
     */
    private ListBox listBox;

    /**
     * Determines if the field has been modified.
     */
    private boolean modified = false;

    /**
     * Constructs an {@link AddressSelector}.
     * <p>
     * The {@link #setField(TextField)} method must be invoked post construction.
     *
     * @param contacts  the contacts
     * @param formatter the address formatter
     */
    protected AddressSelector(List<Contact> contacts, AddressFormatter formatter) {
        this(contacts, formatter, null);
    }

    /**
     * Constructs an {@link AddressSelector}.
     *
     * @param contacts  the contacts
     * @param formatter the address formatter
     * @param field     the field
     */
    public AddressSelector(List<Contact> contacts, AddressFormatter formatter, TextField field) {
        this.formatter = formatter;
        setField(field);
        if (!contacts.isEmpty()) {
            listBox = ListBoxFactory.create(contacts);
            // don't default the selection as per OVPMS-1295
            listBox.getSelectionModel().clearSelection();
            listBox.setWidth(FULL_WIDTH);
            listBox.setCellRenderer(new EmailCellRenderer(formatter));

            dropDown = new DropDown();
            dropDown.setWidth(FULL_WIDTH);
            if (field != null) {
                dropDown.setTarget(field);
            }
            dropDown.setPopUpAlwaysOnTop(true);
            dropDown.setFocusOnExpand(true);
            dropDown.setFocusComponent(listBox);
            dropDown.setPopUp(listBox);
            listBox.addActionListener(new ActionListener() {
                public void onAction(ActionEvent event) {
                    setSelected((Contact) listBox.getSelectedValue());
                    dropDown.setExpanded(false);
                }
            });
        }
    }

    /**
     * Sets the selected contact.
     *
     * @param contact the contact. May be {@code null}
     */
    public void setSelected(Contact contact) {
        resetValid();
        if (listBox != null) {
            int size = listBox.getModel().size();
            boolean found = false;
            if (contact != null) {
                for (int i = 0; i < size; ++i) {
                    if (ObjectUtils.equals(contact, listBox.getModel().get(i))) {
                        listBox.setSelectedIndex(i);
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                listBox.getSelectionModel().clearSelection();
            }
        }
    }

    /**
     * Returns the selected contact.
     *
     * @return the selected contact. May be {@code null}
     */
    public abstract Contact getSelected();

    /**
     * Returns the address.
     *
     * @return the address. May be {@code null}
     */
    public String getAddress() {
        Contact selected = getSelected();
        return (selected != null) ? getFormatter().getNameAddress(selected, true) : null;
    }

    /**
     * Returns the text field.
     *
     * @return the text field
     */
    public TextField getField() {
        return field;
    }

    /**
     * Returns the address formatter.
     *
     * @return the address formatter
     */
    public AddressFormatter getFormatter() {
        return formatter;
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        return dropDown != null ? dropDown : field;
    }

    /**
     * Determines if the object has been modified.
     *
     * @return {@code true} if the object has been modified
     */
    @Override
    public boolean isModified() {
        return modified;
    }

    /**
     * Clears the modified status of the object.
     */
    @Override
    public void clearModified() {
        modified = false;
    }

    /**
     * Adds a listener to be notified when this changes.
     * <p>
     * Listeners will be notified in the order they were registered.
     *
     * @param listener the listener to add
     */
    @Override
    public void addModifiableListener(ModifiableListener listener) {
        listeners.addListener(listener);
    }

    /**
     * Adds a listener to be notified when this changes, specifying the order of the listener.
     *
     * @param listener the listener to add
     * @param index    the index to add the listener at. The 0-index listener is notified first
     */
    @Override
    public void addModifiableListener(ModifiableListener listener, int index) {
        listeners.addListener(listener, index);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    @Override
    public void removeModifiableListener(ModifiableListener listener) {
        listeners.removeListener(listener);
    }

    /**
     * Sets a listener to be notified of errors.
     *
     * @param listener the listener to register. May be {@code null}
     */
    @Override
    public void setErrorListener(ErrorListener listener) {
    }

    /**
     * Returns the listener to be notified of errors.
     *
     * @return the listener. May be {@code null}
     */
    @Override
    public ErrorListener getErrorListener() {
        return null;
    }

    /**
     * Helper to return a contact with matching address.
     *
     * @param contacts the contacts
     * @param address  the email address
     * @return the corresponding contact, or {@code null} if none is found
     */
    public static Contact getContact(List<Contact> contacts, String address) {
        EmailAddress emailAddress = EmailAddress.parse(address);
        if (emailAddress != null) {
            address = emailAddress.getAddress();
            for (Contact contact : contacts) {
                IMObjectBean bean = new IMObjectBean(contact);
                if (StringUtils.equalsIgnoreCase(address, bean.getString("emailAddress"))) {
                    return contact;
                }
            }
        }
        return null;
    }

    /**
     * Creates an email contact, if the address is valid.
     *
     * @param address the address
     * @return a new contact, or {@code null} if it is invalid
     */
    public static Contact createContact(String address) {
        EmailAddress parsed = EmailAddress.parse(address);
        return (parsed != null) ? createContact(parsed) : null;
    }

    /**
     * Creates an email contact.
     *
     * @param address the address
     * @return a new contact
     */
    public static Contact createContact(EmailAddress address) {
        Contact contact = (Contact) ServiceHelper.getArchetypeService().create(ContactArchetypes.EMAIL);
        String name = address.getName();
        if (name != null) {
            contact.setName(name);
        }
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("emailAddress", address.getAddress());
        return contact;
    }

    /**
     * Sets the field.
     *
     * @param field the field
     */
    protected void setField(TextField field) {
        this.field = field;
        if (field != null) {
            field.setWidth(FULL_WIDTH);
            if (dropDown != null) {
                dropDown.setTarget(field);
            }
            field.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void onUpdate(DocumentEvent event) {
                    onModified();
                }
            });
        }
    }

    /**
     * Invoked when this is modified.
     * Notifies registered listeners.
     */
    protected void onModified() {
        modified = true;
        listeners.notifyListeners(this);
    }

}
