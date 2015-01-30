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
import nextapp.echo2.app.layout.GridLayoutData;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.macro.Macros;
import org.openvpms.macro.Variables;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.AbstractModifiable;
import org.openvpms.web.component.property.ErrorListener;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;

/**
 * Manages the from address, to, cc and bcc addresses, and mail subject.
 *
 * @author Tim Anderson
 */
class MailHeader extends AbstractModifiable {

    /**
     * The to address.
     */
    private ToAddressSelector to;

    /**
     * The CC address.
     */
    private ToAddressSelector cc;

    /**
     * The BCC address.
     */
    private ToAddressSelector bcc;

    /**
     * The subject.
     */
    private SimpleProperty subject;

    /**
     * The from-address selector.
     */
    private AddressSelector from;

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
     * @param context     the layout context
     */
    public MailHeader(MailContext mailContext, Contact preferredTo, LayoutContext context) {
        List<Contact> fromAddresses = mailContext.getFromAddresses();
        focus = new FocusGroup("MailHeader");

        from = new FromAddressSelector(fromAddresses, mailContext.getFromAddressFormatter());
        if (!fromAddresses.isEmpty()) {
            from.setSelected(fromAddresses.get(0));
            if (fromAddresses.size() > 1) {
                focus.add(from.getComponent());
            }
        }

        List<Contact> contacts = mailContext.getToAddresses();
        to = new ToAddressSelector(contacts, mailContext.getToAddressFormatter(contacts), context);
        cc = new ToAddressSelector(contacts, mailContext.getToAddressFormatter(contacts), context);
        bcc = new ToAddressSelector(contacts, mailContext.getToAddressFormatter(contacts), context);

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

        Grid grid = GridFactory.create(2, createLabel("mail.from"), from.getComponent(),
                                       createLabel("mail.to"), to.getComponent(),
                                       createLabel("mail.cc"), cc.getComponent(),
                                       createLabel("mail.bcc"), bcc.getComponent(),
                                       createLabel("mail.subject"), subjectText);
        grid.setColumnWidth(0, new Extent(10, Extent.PERCENT));
        grid.setWidth(Styles.FULL_WIDTH);

        component = ColumnFactory.create(Styles.LARGE_INSET, grid);

        focus.add(to.getField());
        focus.add(cc.getField());
        focus.add(bcc.getField());
        focus.add(subjectText);
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
        this.from.setSelected(from);
    }

    /**
     * Returns the from address.
     *
     * @return the from address
     */
    public String getFrom() {
        AddressFormatter formatter = from.getFormatter();
        return formatter.getAddress(from.getSelected());
    }

    /**
     * Sets the 'to' address.
     *
     * @param contact the to address. May be {@code null}
     */
    public void setTo(Contact contact) {
        to.setSelected(contact);
    }

    /**
     * Returns the from name.
     *
     * @return the from name
     */
    public String getFromName() {
        String name = null;
        Contact contact = from.getSelected();
        if (contact != null && contact.getParty() != null) {
            name = contact.getParty().getName();
            if (name != null) {
                name = name.replaceAll(",", "");
            }
        }
        return name;
    }

    /**
     * Returns the to addresses.
     *
     * @return the to addresses. May be {@code null}
     */
    public String[] getTo() {
        return to.getAddresses();
    }

    /**
     * Returns the Cc addresses.
     *
     * @return the Cc addresses. May be {@code null}
     */
    public String[] getCc() {
        return cc.getAddresses();
    }

    /**
     * Returns the Bcc addresses.
     *
     * @return the Bcc addresses. May be {@code null}
     */
    public String[] getBcc() {
        return bcc.getAddresses();
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
     * Determines if the object has been modified.
     *
     * @return {@code true} if the object has been modified
     */
    @Override
    public boolean isModified() {
        return false;
    }

    /**
     * Clears the modified status of the object.
     */
    @Override
    public void clearModified() {
        // no-op
    }

    /**
     * Adds a listener to be notified when this changes.
     * <p/>
     * Listeners will be notified in the order they were registered.
     *
     * @param listener the listener to add
     */
    @Override
    public void addModifiableListener(ModifiableListener listener) {
        // no-op
    }

    /**
     * Adds a listener to be notified when this changes, specifying the order of the listener.
     *
     * @param listener the listener to add
     * @param index    the index to add the listener at. The 0-index listener is notified first
     */
    @Override
    public void addModifiableListener(ModifiableListener listener, int index) {
        // no-op
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    @Override
    public void removeModifiableListener(ModifiableListener listener) {
        // no-op
    }

    /**
     * Sets a listener to be notified of errors.
     *
     * @param listener the listener to register. May be {@code null}
     */
    @Override
    public void setErrorListener(ErrorListener listener) {
        // no-op
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
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        boolean valid = from.getSelected() != null;
        if (!valid) {
            validator.add(this, new ValidatorError(Messages.get("mail.nofromaddress")));
        } else {
            valid = validator.validate(subject);
            if (valid) {
                if (getTo() == null && getCc() == null && getBcc() == null) {
                    validator.add(this, new ValidatorError(Messages.get("mail.notoaddress")));
                    valid = false;
                }
            }
        }
        return valid;
    }

    /**
     * Helper to create a right aligned label.
     *
     * @param key the resource bundle key
     * @return a new label
     */
    private Label createLabel(String key) {
        Label label = LabelFactory.create(key);
        GridLayoutData layout = new GridLayoutData();
        layout.setAlignment(Alignment.ALIGN_RIGHT);
        label.setLayoutData(layout);
        return label;
    }

}
