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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.sms;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.util.Variables;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.sms.Connection;
import org.openvpms.sms.ConnectionFactory;
import org.openvpms.sms.SMSException;
import org.openvpms.web.component.echo.CountedTextArea;
import org.openvpms.web.component.echo.TextField;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.property.AbstractModifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.ModifiableListeners;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.StringPropertyTransformer;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.component.util.TextComponentFactory;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.resource.util.Styles;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * An editor for SMS messages.
 *
 * @author Tim Anderson
 */
public class SMSEditor extends AbstractModifiable {

    /**
     * The phone number, if 0 or 1 no. are provided.
     */
    private TextField phone;

    /**
     * The phone selector, if multiple phone numbers are provided.
     */
    private SelectField phoneSelector;

    /**
     * The text message.
     */
    private CountedTextArea message;

    /**
     * The text property. Used to support macro expansion.
     */
    private SimpleProperty property;

    /**
     * Determines if this has been modified.
     */
    private boolean modified;

    /**
     * The listeners.
     */
    private ModifiableListeners listeners = new ModifiableListeners();

    /**
     * Focus group.
     */
    private FocusGroup focus;

    /**
     * Maximum SMS length.
     */
    private static final int MAX_LENGTH = 160;


    /**
     * Constructs an {@code SMSEditor}.
     */
    public SMSEditor() {
        this(Collections.<Contact>emptyList(), null);
    }

    /**
     * Constructs an {@code SMSEditor}.
     * <p/>
     * If no phone numbers are supplied, the phone number will be editable, otherwise it will be read-only.
     * If there are multiple phone numbers, they will be displayed in a dropdown, with the first no. as the default
     *
     * @param contacts  the available mobile contacts. May be {@code null}
     * @param variables the variables for macro expansion. May be {@code null}
     */
    public SMSEditor(List<Contact> contacts, Variables variables) {
        int length = (contacts == null) ? 0 : contacts.size();
        if (length <= 1) {
            phone = TextComponentFactory.create(20);
            phone.addActionListener(new ActionListener() {
                public void onAction(ActionEvent event) {
                    onModified();
                }
            });
            if (length == 1) {
                phone.setText(formatPhone(contacts.get(0)));
                phone.setEnabled(false);
            }
        } else {
            phoneSelector = SelectFieldFactory.create(formatPhones(contacts));
            phoneSelector.addActionListener(new ActionListener() {
                public void onAction(ActionEvent event) {
                    onModified();
                }
            });
        }
        property = new SimpleProperty("property", String.class);
        property.setVariables(variables);
        property.setMaxLength(MAX_LENGTH);
        property.setTransformer(new StringPropertyTransformer(property, new Object(), false));

        message = new BoundCountedTextArea(property, 40, 15);
        message.setStyleName(Styles.DEFAULT);
        message.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onModified();
            }
        });
        focus = new FocusGroup("SMSEditor");
        if (phone != null) {
            focus.add(phone);
        } else {
            focus.add(phoneSelector);
        }
        focus.add(message);
        focus.setDefault(message);
    }

    /**
     * Sends an SMS.
     *
     * @throws SMSException if the SMS can't be sent
     */
    public void send() {
        ConnectionFactory factory = ServiceHelper.getSMSConnectionFactory();
        Connection connection = factory.createConnection();
        connection.send(getPhone(), getMessage());
    }

    /**
     * Returns the phone number.
     *
     * @return the phone number. May be {@code null}
     */
    public String getPhone() {
        String result = null;
        if (phone != null) {
            result = phone.getText();
        } else if (phoneSelector.getSelectedItem() != null) {
            result = phoneSelector.getSelectedItem().toString();
        }
        if (result != null) {
            // strip any spaces, hyphens, and brackets, and any characters after the last digit.
            result = result.replaceAll("[\\s\\-()]", "").replaceAll("[^\\d].*", "");
        }
        return result;
    }

    /**
     * Sets the message to send.
     *
     * @param message the message
     */
    public void setMessage(String message) {
        property.setValue(message);
    }

    /**
     * Returns the message to send.
     *
     * @return the message to send
     */
    public String getMessage() {
        Object result = property.getValue();
        return (result != null) ? result.toString() : null;
    }

    /**
     * Returns the editor component.
     *
     * @return the component
     */
    public Component getComponent() {
        return GridFactory.create(2, LabelFactory.create("sms.phone"), (phone != null) ? phone : phoneSelector,
                                  LabelFactory.create("sms.message"), message);
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return focus;
    }

    /**
     * Determines if the object has been modified.
     *
     * @return {@code true} if the object has been modified
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        modified = false;
    }

    /**
     * Adds a listener to be notified when this changes.
     *
     * @param listener the listener to add
     */
    public void addModifiableListener(ModifiableListener listener) {
        listeners.addListener(listener);
    }

    /**
     * Adds a listener to be notified when this changes, specifying the order of the listener.
     *
     * @param listener the listener to add
     * @param index    the index to add the listener at. The 0-index listener is notified first
     */
    public void addModifiableListener(ModifiableListener listener, int index) {
        listeners.addListener(listener, index);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        listeners.removeListener(listener);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    protected boolean doValidation(Validator validator) {
        return !StringUtils.isEmpty(getPhone()) && !StringUtils.isEmpty(getMessage());
    }

    /**
     * Formats phone numbers that are flagged for SMS messaging.
     * <p/>
     * The preferred no.s are at the head of the list
     *
     * @param contacts the SMS contacts
     * @return a list of phone numbers
     */
    private String[] formatPhones(List<Contact> contacts) {
        List<String> phones = new ArrayList<String>();
        String preferred = null;
        for (Contact contact : contacts) {
            String phone = formatPhone(contact);
            IMObjectBean bean = new IMObjectBean(contact);
            if (bean.getBoolean("preferred")) {
                preferred = phone;
            }
            phones.add(phone);
        }
        Collections.sort(phones);
        if (preferred != null && !phones.get(0).equals(preferred)) {
            phones.remove(preferred);
            phones.add(0, preferred);
        }
        return phones.toArray(new String[phones.size()]);
    }

    /**
     * Formats a mobile phone number.
     *
     * @param contact the phone contact
     * @return a formatted number, including an area code, if specified
     */
    private String formatPhone(Contact contact) {
        IMObjectBean bean = new IMObjectBean(contact);
        String areaCode = bean.getString("areaCode");
        String phone = bean.getString("telephoneNumber");
        if (!StringUtils.isEmpty(areaCode)) {
            phone = Messages.get("phone.withAreaCode", areaCode, phone);
        } else {
            phone = Messages.get("phone.noAreaCode", phone);
        }
        return phone;
    }

    /**
     * Invoked when the phone or message updates. Refreshes the display and notifies listeners.
     */
    private void onModified() {
        modified = true;
        listeners.notifyListeners(this);
    }
}
