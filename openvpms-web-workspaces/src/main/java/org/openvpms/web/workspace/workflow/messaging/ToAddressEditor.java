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

package org.openvpms.web.workspace.workflow.messaging;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.edit.PropertyEditor;
import org.openvpms.web.component.im.edit.act.ParticipationHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.select.AbstractIMObjectSelectorListener;
import org.openvpms.web.component.im.select.MultiIMObjectSelector;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.AbstractModifiable;
import org.openvpms.web.component.property.ErrorListener;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.resource.i18n.Messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Editor for the "to" participation of <em>act.userMessage</em>.
 * <p/>
 * This supports the selection of both users and user groups; user groups are expanded to their constituent users
 *
 * @author Tim Anderson
 */
class ToAddressEditor extends AbstractModifiable implements PropertyEditor {

    /**
     * The user/group selector.
     */
    private final MultiIMObjectSelector<Entity> selector;

    /**
     * The selector component state.
     */
    private final ComponentState state;

    /**
     * The participation. This is only populated in order for the parent act to validate successfully.
     */
    private Participation participation;

    /**
     * The archetypes to query.
     */
    private static final String[] SHORT_NAMES = {UserArchetypes.USER, UserArchetypes.GROUP};

    /**
     * Constructs a {@code ToAddressEditor}.
     *
     * @param act      the <em>act.userMessage</em>.
     * @param property the "to" participation property
     * @param context  the layout context
     */
    public ToAddressEditor(Act act, Property property, LayoutContext context) {
        participation = ParticipationHelper.getParticipation(property);
        if (participation != null) {
            participation.setAct(act.getObjectReference());
        }
        selector = new MultiIMObjectSelector<Entity>(property.getDisplayName(), context, SHORT_NAMES);
        selector.setListener(new AbstractIMObjectSelectorListener<Entity>() {
            public void selected(Entity object) {
                onSelected(object);
            }
        });
        selector.getTextField().setWidth(new Extent(100, Extent.PERCENT));
        state = new ComponentState(selector.getComponent(), property, selector.getFocusGroup());
    }

    /**
     * Returns the property being edited.
     *
     * @return the property being edited
     */
    public Property getProperty() {
        return state.getProperty();
    }

    /**
     * Returns the selected users.
     * <p/>
     * Any groups have been expanded to their constituent users
     *
     * @return the selected users
     */
    public Set<User> getTo() {
        return MessageHelper.getUsers(selector.getObjects());
    }

    /**
     * Sets the 'to' user.
     *
     * @param to the 'to' user. May be {@code null}
     */
    public void setTo(User to) {
        List<Entity> users = new ArrayList<Entity>();
        if (to != null) {
            users.add(to);
        }
        selector.setObjects(users);
    }

    /**
     * Returns the edit component.
     *
     * @return the edit component
     */
    public Component getComponent() {
        return selector.getComponent();
    }

    /**
     * Returns the edit component state
     *
     * @return the component state
     */
    public ComponentState getComponentState() {
        return state;
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return selector.getFocusGroup();
    }

    /**
     * Disposes of the editor.
     * <br/>
     * Once disposed, the behaviour of invoking any method is undefined.
     */
    public void dispose() {
    }

    /**
     * Determines if the object has been modified.
     *
     * @return {@code true}if the object has been modified
     */
    public boolean isModified() {
        return true;
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        // no-op
    }

    /**
     * Adds a listener to be notified when this changes.
     *
     * @param listener the listener to add
     */
    public void addModifiableListener(ModifiableListener listener) {
        // no-op
    }

    /**
     * Adds a listener to be notified when this changes, specifying the order of the listener.
     *
     * @param listener the listener to add
     * @param index    the index to add the listener at. The 0-index listener is notified first
     */
    public void addModifiableListener(ModifiableListener listener, int index) {
        // no-op
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
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
     * @return {@code null}
     */
    @Override
    public ErrorListener getErrorListener() {
        return null;
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if at least one user has been selected
     */
    @Override
    protected boolean doValidation(Validator validator) {
        boolean valid = false;
        if (selector.isValid() && !getTo().isEmpty()) {
            valid = true;
        } else {
            Property property = state.getProperty();
            String message;
            String notFound = selector.getFirstNotFound();
            if (notFound != null) {
                message = Messages.format("workflow.message.invaliduserorgroup", notFound);
            } else {
                message = Messages.format("property.error.required", property.getDisplayName());
            }
            validator.add(property, new ValidatorError(property, message));
        }
        return valid;
    }

    /**
     * Invoked when an entity is selected.
     * <p/>
     * This updates the participation with a user reference, if one is available.
     *
     * @param entity the entity
     */
    private void onSelected(Entity entity) {
        if (participation != null) {
            IMObjectReference ref = null;
            if (TypeHelper.isA(entity, UserArchetypes.USER)) {
                ref = entity.getObjectReference();
                participation.setEntity(entity.getObjectReference());
            } else if (TypeHelper.isA(entity, UserArchetypes.GROUP)) {
                EntityBean bean = new EntityBean(entity);
                User user = (User) bean.getNodeTargetEntity("users");
                if (user != null) {
                    ref = user.getObjectReference();
                }
            }
            participation.setEntity(ref);
        }
    }

}
