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

package org.openvpms.web.component.im.contact;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.system.ServiceHelper;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * Editor for <em>contact.email</em> contacts.
 *
 * @author Tim Anderson
 */
public class EmailEditor extends AbstractIMObjectEditor {

    /**
     * The default value for the name node.
     */
    private final String defaultEmailName;

    /**
     * Constructs an {@link EmailEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public EmailEditor(Contact object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);

        defaultEmailName = ContactHelper.getDefaultEmailName();
        if (object.isNew() && parent instanceof Party) {
            PartyRules rules = ServiceHelper.getBean(CustomerRules.class);
            String fullName = rules.getFullName((Party) parent, false);
            if (!StringUtils.isEmpty(fullName)) {
                object.setName(fullName);
            }
        }
    }

    /**
     * Returns the personal name.
     *
     * @return the personal name. May be {@code null}
     */
    public String getName() {
        return getProperty("name").getString();
    }

    /**
     * Returns the email address.
     *
     * @return the email address. May be {@code null}
     */
    public String getEmailAddress() {
        return getProperty("emailAddress").getString();
    }


    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        return super.doValidation(validator) && validateInternetAddress(validator);
    }

    /**
     * Validates the email address and personal name (if any) using {@link InternetAddress}.
     *
     * @param validator the validator
     * @return {@code true} if they are valid, otherwise {@code false}
     */
    private boolean validateInternetAddress(Validator validator) {
        boolean valid = false;
        String name = getName();
        if (StringUtils.equals(name, defaultEmailName)) {
            name = null;
        }
        String email = ContactHelper.getEmail(getEmailAddress(), name);
        try {
            new InternetAddress(email, true);
            valid = true;
        } catch (AddressException exception) {
            // if a personal name is specified, it is most likely the cause of the exception (the email address
            // has its own validation within the archetype), so use its property for the validation error
            Property property = (name != null) ? getProperty("name") : getProperty("emailAddress");
            validator.add(property, new ValidatorError(property, exception.getMessage()));
        }
        return valid;
    }
}
