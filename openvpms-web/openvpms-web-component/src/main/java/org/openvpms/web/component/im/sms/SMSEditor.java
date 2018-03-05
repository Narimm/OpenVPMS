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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.sms;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.util.Variables;
import org.openvpms.macro.Macros;
import org.openvpms.sms.SMSException;
import org.openvpms.sms.util.SMSLengthCalculator;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.bound.BoundSelectFieldFactory;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.edit.Editors;
import org.openvpms.web.component.im.list.PairListModel;
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
import org.openvpms.web.component.service.SMSService;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.SMSTextArea;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * An editor for SMS messages.
 *
 * @author Tim Anderson
 */
public class SMSEditor extends AbstractModifiable {

    /**
     * The context.
     */
    private final Context context;

    /**
     * The phone number, if 0 or 1 no. are provided.
     */
    private TextField phone;

    /**
     * The phone selector, if multiple phone numbers are provided.
     */
    private SelectField phoneSelector;

    /**
     * The selected contact.
     */
    private Contact selected;

    /**
     * The text message.
     */
    private final SMSTextArea message;

    /**
     * The text property. Used to support macro expansion.
     */
    private final SimpleProperty messageProperty;

    /**
     * The phone property.
     */
    private final SimpleProperty phoneProperty;

    /**
     * Focus group.
     */
    private final FocusGroup focus;

    /**
     * Used to track property modification, and perform validation.
     */
    private final Editors editors;

    /**
     * The maximum number of parts in multi-part SMS messages.
     */
    private int maxParts;

    /**
     * Ad hoc SMS reason code.
     */
    private static final String AD_HOC_SMS = "AD_HOC_SMS";

    /**
     * Constructs an {@link SMSEditor}.
     *
     * @param context the context
     */
    public SMSEditor(Context context) {
        this(Collections.<Contact>emptyList(), null, context);
    }

    /**
     * Constructs an {@link SMSEditor}.
     * <p>
     * If no phone numbers are supplied, the phone number will be editable, otherwise it will be read-only.
     * If there are multiple phone numbers, they will be displayed in a dropdown, with the first no. as the default
     *
     * @param contacts  the available mobile contacts. May be {@code null}
     * @param variables the variables for macro expansion. May be {@code null}
     * @param context   the context
     */
    public SMSEditor(List<Contact> contacts, Variables variables, Context context) {
        this.context = context;
        int length = (contacts == null) ? 0 : contacts.size();
        phoneProperty = new SimpleProperty("phone", null, String.class, Messages.get("sms.phone"));
        phoneProperty.setRequired(true);
        if (length <= 1) {
            phone = BoundTextComponentFactory.create(phoneProperty, 20);
            if (length == 1) {
                onSelected(contacts.get(0));
                phoneProperty.setValue(formatPhone(selected));
                phone.setEnabled(false);
            }
        } else {
            final PairListModel model = formatPhones(contacts);
            phoneSelector = BoundSelectFieldFactory.create(phoneProperty, model);
            phoneSelector.addActionListener(new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    int index = phoneSelector.getSelectedIndex();
                    if (index >= 0 && index < model.size()) {
                        onSelected((Contact) model.getValue(index));
                    } else {
                        onSelected(null);
                    }
                }
            });
            phoneSelector.setSelectedIndex(0);
        }

        messageProperty = new SimpleProperty("message", null, String.class, Messages.get("sms.message"));
        messageProperty.setRequired(true);
        messageProperty.setMaxLength(-1); // don't limit length as it is determined by maxParts and encoding
        Macros macros = ServiceHelper.getMacros();
        messageProperty.setTransformer(new StringPropertyTransformer(messageProperty, false, macros, null, variables));

        message = new BoundSMSTextArea(messageProperty, 40, 15);
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

        setMaxParts(ServiceHelper.getSMSConnectionFactory().getMaxParts());
    }

    /**
     * Sends an SMS.
     *
     * @throws SMSException if the SMS can't be sent
     */
    public void send() {
        String phone = getPhone();
        String message = getMessage();
        SMSService service = ServiceHelper.getBean(SMSService.class);
        Party customer = context.getCustomer();
        String subject = Messages.get("sms.log.adhoc.subject");
        if (customer != null) {
            service.send(phone, message, customer, context.getPatient(), selected, subject, AD_HOC_SMS,
                         context.getLocation());
        } else {
            Party party = (selected != null) ? selected.getParty() : null;
            service.send(phone, message, party, selected, subject, AD_HOC_SMS, context.getLocation());
        }
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
            result = result.replaceAll("[\\s\\-()]", "").replaceAll("[^\\d\\+].*", "");
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
     * Sets the maximum number of parts of the message.
     *
     * @param maxParts the maximum length
     */
    public void setMaxParts(int maxParts) {
        if (maxParts <= 0) {
            maxParts = 1;
        }
        this.maxParts = maxParts;
        message.setMaxParts(maxParts);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    public boolean validate(Validator validator) {
        boolean valid = super.validate(validator);
        if (valid) {
            String message = getMessage();
            int parts = SMSLengthCalculator.getParts(message);
            if (parts > maxParts) {
                valid = false;
                validator.add(this, new ValidatorError(Messages.format("sms.toolong", parts, maxParts)));
            }
        }
        return valid;
    }

    /**
     * Returns the selected contact.
     *
     * @return the selected contact. May be {@code null}
     */
    public Contact getContact() {
        return selected;
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
     * <p>
     * The preferred no.s are at the head of the list
     *
     * @param contacts the SMS contacts
     * @return a list of phone numbers
     */
    private PairListModel formatPhones(List<Contact> contacts) {
        List<PairListModel.Pair> phones = new ArrayList<>();
        PairListModel.Pair preferred = null;
        for (Contact contact : contacts) {
            String phone = formatPhone(contact);
            PairListModel.Pair pair = new PairListModel.Pair(phone, contact);
            IMObjectBean bean = new IMObjectBean(contact);
            if (bean.getBoolean("preferred")) {
                preferred = pair;
            }
            phones.add(pair);
        }
        Collections.sort(phones, new Comparator<PairListModel.Pair>() {
            @Override
            @SuppressWarnings("unchecked")
            public int compare(PairListModel.Pair o1, PairListModel.Pair o2) {
                return ComparatorUtils.nullLowComparator(null).compare(o1.getKey(), o2.getKey());
            }
        });
        if (preferred != null && !phones.get(0).equals(preferred)) {
            phones.remove(preferred);
            phones.add(0, preferred);
        }
        return new PairListModel(phones);
    }

    /**
     * Formats a mobile phone number. Adds the name afterward if it is not the default value.
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
        String name = contact.getName();
        if (!StringUtils.isEmpty(name) && bean.hasNode("name") && !bean.isDefaultValue("name")) {
            phone += " (" + name + ")";
        }
        return phone;
    }

    private void onSelected(Contact selected) {
        this.selected = selected;
    }
}
