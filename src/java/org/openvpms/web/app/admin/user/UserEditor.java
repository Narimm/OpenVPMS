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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.admin.user;

import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.DocumentEvent;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.ComponentSet;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.component.util.TextComponentFactory;
import org.openvpms.web.component.event.DocumentListener;
import org.openvpms.web.resource.util.Messages;

import java.util.Arrays;
import java.util.List;


/**
 * Editor for <em>security.user</em> instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class UserEditor extends AbstractIMObjectEditor {

    /**
     * The password field.
     */
    private final TextField password;

    /**
     * The confirm password field.
     */
    private final TextField confirm;


    /**
     * Construct a new <code>UserEditor</code>.
     *
     * @param object  the object to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context. May be <code>null</code>.
     */
    public UserEditor(User object, IMObject parent, LayoutContext context) {
        super(object, parent, context);
        Property property = getPassword();
        int width = property.getMaxLength();
        if (width > 20) {
            width = 20;
        }
        String value = (String) property.getValue();
        password = TextComponentFactory.createPassword(width);
        password.setText(value);
        password.getDocument().addDocumentListener(new DocumentListener() {
            public void onUpdate(DocumentEvent event) {
                onPasswordChanged();
            }
        });
        confirm = TextComponentFactory.createPassword(width);
        confirm.setText(value);
        confirm.getDocument().addDocumentListener(new DocumentListener() {
            public void onUpdate(DocumentEvent event) {
            }
        });
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return <code>true</code> if the object and its descendents are valid
     *         otherwise <code>false</code>
     */
    @Override
    public boolean validate(Validator validator) {
        boolean valid = false;
        if (super.validate(validator)) {
            Property property = getPassword();
            if (ObjectUtils.equals(property.getValue(), confirm.getText())) {
                valid = true;
            } else {
                ValidatorError error = new ValidatorError(
                        getObject().getArchetypeId().getShortName(),
                        property.getName(),
                        Messages.get("admin.user.password.mismatch"));
                validator.add(property, Arrays.asList(error));
            }
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
     * Invoked when the password changes.
     */
    private void onPasswordChanged() {
        getPassword().setValue(password.getText());
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
     * Layout strategy that adds a 'confirm password' field after the password.
     */
    private class LayoutStrategy extends UserLayoutStrategy {

        @Override
        protected ComponentSet createComponentSet(
                IMObject object, List<NodeDescriptor> descriptors,
                PropertySet properties, LayoutContext context) {
            ComponentSet result = new ComponentSet();
            for (NodeDescriptor descriptor : descriptors) {
                Property property = properties.get(descriptor);
                String displayName = descriptor.getDisplayName();
                if (descriptor.getName().equals("password")) {
                    ComponentState passwordComp
                            = new ComponentState(password, getPassword());
                    result.add(passwordComp, displayName);
                    ComponentState confirmComp
                            = new ComponentState(confirm, getPassword());
                    result.add(confirmComp, Messages.get(
                            "admin.user.password.confirm", displayName));
                } else {
                    ComponentState component = createComponent(property, object,
                                                               context);
                    result.add(component, displayName);
                }
            }
            return result;
        }

    }
}
