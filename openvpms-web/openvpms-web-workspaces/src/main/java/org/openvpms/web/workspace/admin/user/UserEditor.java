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

package org.openvpms.web.workspace.admin.user;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.ComponentSet;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;


/**
 * Editor for <em>security.user</em> instances.
 *
 * @author Tim Anderson
 */
public class UserEditor extends AbstractIMObjectEditor {

    /**
     * The confirm password property.
     */
    private final Property confirm;

    /**
     * The user rules.
     */
    private final UserRules rules;


    /**
     * Constructs an {@link UserEditor}.
     *
     * @param object  the object to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context. May be {@code null}.
     */
    public UserEditor(User object, IMObject parent, LayoutContext context) {
        super(object, parent, context);
        rules = ServiceHelper.getBean(UserRules.class);
        Property property = getPassword();
        String value = (String) property.getValue();
        confirm = new SimpleProperty("confirm", value, String.class);
    }

    /**
     * Sets the user's login name.
     *
     * @param name the user's login name
     */
    public void setUsername(String name) {
        getProperty("username").setValue(name);
    }

    /**
     * Sets the user's password.
     *
     * @param password the user's password
     */
    public void setPassword(String password) {
        getProperty("password").setValue(password);
        confirm.setValue(password);
    }

    /**
     * Sets the user's name.
     *
     * @param name the user's name
     */
    public void setName(String name) {
        getProperty("name").setValue(name);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the object and its descendants are valid otherwise <tt>false</tt>
     */
    @Override
    protected boolean doValidation(Validator validator) {
        return super.doValidation(validator) && validateUniqueUserName(validator) && validatePassword(validator);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new LayoutStrategy();
    }

    /**
     * Validates that the username is unique.
     *
     * @param validator the validator
     * @return {@code true} if the username is unique
     */
    private boolean validateUniqueUserName(Validator validator) {
        boolean valid = true;
        Property username = getProperty("username");
        String name = username.getString();
        if (name != null && rules.exists(name, (User) getObject())) {
            String message = Messages.format("admin.user.duplicate", name);
            ValidatorError error = new ValidatorError(getObject().getArchetypeId().getShortName(),
                                                      username.getName(), message);
            validator.add(username, error);
            valid = false;
        }
        return valid;
    }

    /**
     * Verifies that the passwords are the same.
     *
     * @param validator the validator
     * @return {@code true} if the passwords are the same
     */
    private boolean validatePassword(Validator validator) {
        boolean valid = false;
        Property password = getPassword();
        if (ObjectUtils.equals(password.getValue(), confirm.getValue())) {
            valid = true;
        } else {
            ValidatorError error = new ValidatorError(getObject().getArchetypeId().getShortName(), password.getName(),
                                                      Messages.get("admin.user.password.mismatch"));
            validator.add(password, error);
        }
        return valid;
    }

    /**
     * Returns the password property.
     *
     * @return the password property
     */
    private Property getPassword() {
        return getProperty("password");
    }

    /**
     * Layout strategy that adds a 'confirm password' field after the password.
     */
    private class LayoutStrategy extends UserLayoutStrategy {

        /**
         * Creates a component for a property.
         *
         * @param property the property
         * @param parent   the parent object
         * @param context  the layout context
         * @return a component to display <tt>property</tt>
         */
        @Override
        protected ComponentState createComponent(Property property, IMObject parent, LayoutContext context) {
            if (property.getName().equals("password")) {
                return new ComponentState(BoundTextComponentFactory.createPassword(property), property);
            }
            return super.createComponent(property, parent, context);
        }

        /**
         * Creates a set of components to be rendered from the supplied descriptors.
         *
         * @param object     the parent object
         * @param properties the properties
         * @param context    the layout context
         * @return the components
         */
        @Override
        protected ComponentSet createComponentSet(IMObject object, List<Property> properties,
                                                  LayoutContext context) {
            ComponentSet set = super.createComponentSet(object, properties, context);

            int index = set.indexOf("password");
            if (index != -1) {
                ComponentState passwordField = set.getComponents().get(index);
                ComponentState confirmField = new ComponentState(BoundTextComponentFactory.createPassword(confirm));
                String label = Messages.format("admin.user.password.confirm", passwordField.getDisplayName());
                confirmField.setDisplayName(label);
                set.add(index + 1, confirmField);
            }
            return set;
        }

    }
}
