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

package org.openvpms.web.component.im.sms;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.list.DefaultListModel;
import nextapp.echo2.app.list.ListModel;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.macro.Macros;
import org.openvpms.macro.Variables;
import org.openvpms.sms.Connection;
import org.openvpms.sms.ConnectionFactory;
import org.openvpms.sms.SMSException;
import org.openvpms.web.component.bound.BoundSelectFieldFactory;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.edit.Editors;
import org.openvpms.web.component.property.AbstractModifiable;
import org.openvpms.web.component.property.ErrorListener;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.ModifiableListeners;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.StringPropertyTransformer;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.CountedTextArea;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.Messages;
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
    private SimpleProperty messageProperty;

    /**
     * The phone property.
     */
    private SimpleProperty phoneProperty;

    /**
     * Focus group.
     */
    private FocusGroup focus;

    /**
     * Maximum SMS length.
     */
    private static final int MAX_LENGTH = 160;

    /**
     * Used to track property modification, and perform validation.
     */
    private Editors editors;


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
        phoneProperty = new SimpleProperty("phone", null, String.class, Messages.get("sms.phone"));
        phoneProperty.setRequired(true);
        if (length <= 1) {
            phone = BoundTextComponentFactory.create(phoneProperty, 20);
            if (length == 1) {
                phoneProperty.setValue(formatPhone(contacts.get(0)));
                phone.setEnabled(false);
            }
        } else {
            phoneSelector = BoundSelectFieldFactory.create(phoneProperty, formatPhones(contacts));
            phoneSelector.setSelectedIndex(0);
        }

        messageProperty = new SimpleProperty("message", null, String.class, Messages.get("sms.message"));
        messageProperty.setRequired(true);
        messageProperty.setMaxLength(MAX_LENGTH);
        Macros macros = ServiceHelper.getMacros();
        messageProperty.setTransformer(new StringPropertyTransformer(messageProperty, false, macros, null, variables));

        message = new BoundCountedTextArea(messageProperty, 40, 15);
        message.setStyleName(Styles.DEFAULT);
        focus = new FocusGroup("SMSEditor");
        if (phone != null) {
            focus.add(phone);
        } else {
            focus.add(phoneSelector);
        }
        focus.add(message);
        focus.setDefault(message);

        PropertySet properties = new PropertySet(phoneProperty, messageProperty);
        editors = new Editors(properties, new ModifiableListeners());
        editors.addModifiableListener(new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                resetValid();
            }
        });
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
        String result = phoneProperty.getString();
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
        messageProperty.setValue(message);
    }

    /**
     * Returns the message to send.
     *
     * @return the message to send
     */
    public String getMessage() {
        return messageProperty.getString();
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
        return editors.isModified();
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        editors.clearModified();
    }

    /**
     * Adds a listener to be notified when this changes.
     *
     * @param listener the listener to add
     */
    public void addModifiableListener(ModifiableListener listener) {
        editors.addModifiableListener(listener);
    }

    /**
     * Adds a listener to be notified when this changes, specifying the order of the listener.
     *
     * @param listener the listener to add
     * @param index    the index to add the listener at. The 0-index listener is notified first
     */
    public void addModifiableListener(ModifiableListener listener, int index) {
        editors.addModifiableListener(listener, index);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        editors.removeModifiableListener(listener);
    }

    /**
     * Sets a listener to be notified of errors.
     *
     * @param listener the listener to register. May be {@code null}
     */
    @Override
    public void setErrorListener(ErrorListener listener) {
        editors.setErrorListener(listener);
    }

    /**
     * Returns the listener to be notified of errors.
     *
     * @return the listener. May be {@code null}
     */
    @Override
    public ErrorListener getErrorListener() {
        return editors.getErrorListener();
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    protected boolean doValidation(Validator validator) {
        boolean valid = editors.validate(validator);
        if (valid && StringUtils.trimToEmpty(getMessage()).isEmpty()) {
            validator.add(messageProperty, new ValidatorError(
                    messageProperty, Messages.format("property.error.required", messageProperty.getDisplayName())));
            valid = false;
        }
        return valid;
    }

    /**
     * Resets the cached validity state of the object, to force revalidation of the object and its descendants.
     */
    @Override
    public void resetValid() {
        super.resetValid();
        editors.resetValid();
    }

    /**
     * Formats phone numbers that are flagged for SMS messaging.
     * <p/>
     * The preferred no.s are at the head of the list
     *
     * @param contacts the SMS contacts
     * @return a list of phone numbers
     */
    private ListModel formatPhones(List<Contact> contacts) {
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
        return new DefaultListModel(phones.toArray(new String[phones.size()]));
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
            phone = Messages.format("phone.withAreaCode", areaCode, phone);
        } else {
            phone = Messages.format("phone.noAreaCode", phone);
        }
        return phone;
    }

}
