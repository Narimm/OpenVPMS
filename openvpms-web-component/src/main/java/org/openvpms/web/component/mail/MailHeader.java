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

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.GridLayoutData;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.macro.Macros;
import org.openvpms.macro.Variables;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the from address, to, cc and bcc addresses, and mail subject.
 *
 * @author Tim Anderson
 */
class MailHeader {

    /**
     * The selected 'from' contact.
     */
    private Contact selectedFrom;

    /**
     * The from address.
     */
    private SimpleProperty from;

    /**
     * The to address.
     */
    private AddressLine to;

    /**
     * The CC address.
     */
    private AddressLine cc;

    /**
     * The BCC address.
     */
    private AddressLine bcc;

    /**
     * The subject.
     */
    private SimpleProperty subject;

    /**
     * The from-address selector, if multiple addresses are provided.
     */
    private SelectField fromAddressSelector;

    /**
     * The 'from' address formatter.
     */
    private final AddressFormatter fromFormatter;

    /**
     * The header component.
     */
    private Component component;

    /**
     * The focus group.
     */
    private final FocusGroup focus;


    /**
     * Constructs an {@link MailHeader}.
     *
     * @param mailContext the mail context
     * @param preferredTo the preferred to address
     */
    public MailHeader(MailContext mailContext, Contact preferredTo) {
        List<Contact> fromAddresses = mailContext.getFromAddresses();
        this.fromFormatter = mailContext.getFromAddressFormatter();
        focus = new FocusGroup("MailHeader");

        from = MailHelper.createProperty("from", "mail.from", true);
        to = new AddressLine("to", mailContext.getToAddresses(), mailContext.getToAddressFormatter());
        cc = new AddressLine("cc", new ArrayList<Contact>(), mailContext.getToAddressFormatter());
        bcc = new AddressLine("bcc", new ArrayList<Contact>(), mailContext.getToAddressFormatter());

        Component fromAddress;
        if (fromAddresses.size() <= 1) {
            TextField fromText = BoundTextComponentFactory.create(from, 40);
            fromText.setWidth(Styles.FULL_WIDTH);
            fromAddress = fromText;
            fromAddress.setEnabled(false);
            if (fromAddresses.size() == 1) {
                setFrom(fromAddresses.get(0));
            }
        } else {
            fromAddressSelector = createAddressSelector(fromAddresses);
            setFrom((Contact) fromAddressSelector.getSelectedItem());
            fromAddressSelector.addActionListener(new ActionListener() {
                public void onAction(ActionEvent event) {
                    setFrom((Contact) fromAddressSelector.getSelectedItem());
                    // onModified(); TODO
                }
            });
            fromAddress = fromAddressSelector;
            focus.add(fromAddressSelector);
        }

        if (preferredTo != null) {
            setTo(preferredTo);
        }

        Variables variables = mailContext.getVariables();
        Macros macros = ServiceHelper.getMacros();

        subject = MailHelper.createProperty("subject", "mail.subject", true, macros, variables);
        ModifiableListener listener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                // onModified(); TODO
            }
        };
        subject.addModifiableListener(listener);

        TextField subjectText = BoundTextComponentFactory.create(subject, 40);
        subjectText.setWidth(Styles.FULL_WIDTH);

        Grid grid = GridFactory.create(2, createLabel(from), fromAddress,
                                       createLabel(to.getProperty()), to.getComponent(),
                                       createLabel(cc.getProperty()), cc.getComponent(),
                                       createLabel(bcc.getProperty()), bcc.getComponent(),
                                       createLabel(subject), subjectText);
        grid.setColumnWidth(0, new Extent(10, Extent.PERCENT));
        grid.setWidth(Styles.FULL_WIDTH);

        component = ColumnFactory.create(Styles.LARGE_INSET, grid);

        focus.add(to.getField());
        focus.add(cc.getField());
        focus.add(bcc.getField());
        focus.add(subjectText);
    }

    /**
     * Validates the header.
     *
     * @param validator the validator
     * @return {@code true} if the header is valid
     */
    public boolean validate(Validator validator) {
        return from.validate(validator) && validateTo(validator) && validator.validate(subject);
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
     * Returns the header component.
     *
     * @return the header component
     */
    public Component getComponent() {
        return component;
    }

    /**
     * Sets the from address.
     *
     * @param from the from address. May be {@code null}
     */
    public void setFrom(Contact from) {
        this.selectedFrom = from;
        String value = (from != null) ? fromFormatter.format(from) : null;
        this.from.setValue(value);
    }

    /**
     * Returns the from address.
     *
     * @return the from address
     */
    public String getFrom() {
        return fromFormatter.getAddress(selectedFrom);
    }

    /**
     * Sets the 'to' address.
     *
     * @param toAddress the to address. May be {@code null}
     */
    public void setTo(Contact toAddress) {
        to.setAddress(toAddress);
    }

    /**
     * Returns the from name.
     *
     * @return the from name
     */
    public String getFromName() {
        String name = null;
        if (selectedFrom != null && selectedFrom.getParty() != null) {
            name = selectedFrom.getParty().getName();
        }
        return name;
    }

    /**
     * Returns the to address.
     *
     * @return the to address. May be {@code null}
     */
    public String getTo() {
        return to.getAddress();
    }

    /**
     * Returns the Cc address.
     *
     * @return the Cc address. May be {@code null}
     */
    public String getCc() {
        return cc.getAddress();
    }

    /**
     * Returns the Bcc address.
     *
     * @return the Bcc address. May be {@code null}
     */
    public String getBcc() {
        return bcc.getAddress();
    }

    /**
     * Sets the message subject.
     *
     * @param subject the subject
     */
    public void setSubject(String subject) {
        this.subject.setValue(subject);
    }

    /**
     * Returns the message subject.
     *
     * @return the message subject
     */
    public String getSubject() {
        return (String) subject.getValue();
    }

    /**
     * Validates the to, cc, and bcc addresses.
     *
     * @param validator the validator
     * @return {@code true} if the addresses are valid
     */
    private boolean validateTo(Validator validator) {
        boolean valid = to.validate(validator) && cc.validate(validator) && bcc.validate(validator);
        if (valid) {
            if (StringUtils.isEmpty(getTo()) && StringUtils.isEmpty(getCc()) && StringUtils.isEmpty(getBcc())) {
                validator.add(to.getProperty(), new ValidatorError(Messages.get("mail.notoaddress")));
                valid = false;
            }
        }
        return valid;
    }

    /**
     * Creates an address selector for a list of addresses.
     *
     * @param addresses the addresses
     * @return an address editor
     */
    private SelectField createAddressSelector(List<Contact> addresses) {
        SelectField result = SelectFieldFactory.create(addresses);
        result.setCellRenderer(new EmailCellRenderer(fromFormatter));
        result.setWidth(Styles.FULL_WIDTH);
        return result;
    }

    /**
     * Helper to create a label for a property.
     *
     * @param property the property
     * @return a new label
     */
    private Label createLabel(Property property) {
        Label label = LabelFactory.create();
        label.setText(property.getDisplayName());
        GridLayoutData layout = new GridLayoutData();
        layout.setAlignment(Alignment.ALIGN_RIGHT);
        label.setLayoutData(layout);
        return label;
    }

}
