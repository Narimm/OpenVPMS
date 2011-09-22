/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.app.sms.sms;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.TextArea;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.StringUtils;
import org.openvpms.sms.Connection;
import org.openvpms.sms.ConnectionFactory;
import org.openvpms.sms.SMSException;
import org.openvpms.web.component.echo.TextField;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.ModifiableListeners;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.component.util.TextComponentFactory;
import org.openvpms.web.system.ServiceHelper;


/**
 * An editor for SMS messages.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class SMSEditor implements Modifiable {

    /**
     * The phone number, if the number may be edited.
     */
    private TextField phone;

    /**
     * The phone selector, if a list of phone numbers are provided.
     */
    private SelectField phoneSelector;

    /**
     * The text message.
     */
    private TextArea message;

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
     * Constructs an <tt>SMSEditor</tt>.
     */
    public SMSEditor() {
        this(null);
    }

    /**
     * Constructs an <tt>SMSEditor</tt>.
     *
     * @param numbers the available numbers. If <tt>null</null> or empty, the phone number will be editable, otherwise
     *                it must be selected from a dropdown. The first no. is the default
     */
    public SMSEditor(String[] numbers) {
        if (numbers == null || numbers.length == 0) {
            phone = TextComponentFactory.create(12);
            phone.addActionListener(new ActionListener() {
                public void onAction(ActionEvent event) {
                    onModified();
                }
            });
        } else {
            phoneSelector = SelectFieldFactory.create(numbers);
            phoneSelector.addActionListener(new ActionListener() {
                public void onAction(ActionEvent event) {
                    onModified();
                }
            });
        }
        message = TextComponentFactory.createTextArea(40, 15);
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
     * Sets the phone number to send to.
     *
     * @param phone the phone number
     */
    public void setPhone(String phone) {
        if (this.phone != null) {
            this.phone.setText(phone);
        } else {
            phoneSelector.setSelectedItem(phone);
        }
    }

    /**
     * Returns the phone number.
     *
     * @return the phone number. May be <tt>null</tt>
     */
    public String getPhone() {
        String result = null;
        if (phone != null) {
            result = phone.getText();
        } else if (phoneSelector.getSelectedItem() != null) {
            result = phoneSelector.getSelectedItem().toString();
        }
        return result;
    }

    /**
     * Sets the message to send.
     *
     * @param message the message
     */
    public void setMessage(String message) {
        this.message.setText(message);
    }

    /**
     * Returns the message to send.
     *
     * @return the message to send
     */
    public String getMessage() {
        return message.getText();
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
     * @return <tt>true</tt> if the object has been modified
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
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        listeners.removeListener(listener);
    }

    /**
     * Determines if the object is valid.
     *
     * @return <tt>true</tt> if the object is valid; otherwise <tt>false</tt>
     */
    public boolean isValid() {
        return new Validator().validate(this);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the object and its descendents are valid otherwise <tt>false</tt>
     */
    public boolean validate(Validator validator) {
        return !StringUtils.isEmpty(getPhone()) && !StringUtils.isEmpty(getMessage());
    }

    /**
     * Invoked when the phone or message updates. Refreshes the display and notifies listeners.
     */
    private void onModified() {
        modified = true;
        listeners.notifyListeners(this);
    }
}
