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

package org.openvpms.web.component.im.edit;

import nextapp.echo2.app.event.WindowPaneEvent;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.util.IMObjectCreatorListener;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Abstract implementation of the {@link IMObjectReferenceEditor} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractIMObjectReferenceEditor<T extends IMObject>
        extends AbstractSelectorPropertyEditor<T> implements IMObjectReferenceEditor<T> {

    /**
     * The parent object. May be {@code null}
     */
    private final IMObject parent;


    /**
     * Constructs an {@link AbstractIMObjectReferenceEditor}.
     *
     * @param property the reference property
     * @param parent   the parent object. May be {@code null}
     * @param context  the layout context
     */
    public AbstractIMObjectReferenceEditor(Property property, IMObject parent, LayoutContext context) {
        this(property, parent, context, false);
    }

    /**
     * Constructs an {@code AbstractIMObjectReferenceEditor}.
     *
     * @param property    the reference property
     * @param parent      the parent object. May be {@code null}
     * @param context     the layout context
     * @param allowCreate determines if objects may be created
     */
    public AbstractIMObjectReferenceEditor(Property property, IMObject parent, LayoutContext context,
                                           boolean allowCreate) {
        super(property, context, allowCreate);
        this.parent = parent;
        updateSelector();
    }

    /**
     * Determines if the reference is null.
     * This treats an entered but incorrect name as being non-null.
     *
     * @return {@code true} if the reference is null; otherwise {@code false}
     */
    public boolean isNull() {
        boolean result = false;
        if (getProperty().getValue() == null && StringUtils.isEmpty(getSelector().getText())) {
            result = true;
        }
        return result;
    }

    /**
     * Determines if objects may be created.
     *
     * @param create if {@code true}, objects may be created
     */
    public void setAllowCreate(boolean create) {
        getSelector().setAllowCreate(create);
    }

    /**
     * Determines if objects may be created.
     *
     * @return {@code true} if objects may be created
     */
    public boolean allowCreate() {
        return getSelector().allowCreate();
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    protected boolean doValidation(Validator validator) {
        boolean result = false;
        IMObjectSelector<T> selector = getSelector();
        if (!selector.inSelect()) {
            // only raise validation errors if a dialog is not displayed
            if (!selector.isValid()) {
                String message = Messages.format("imobject.invalidreference", selector.getText());
                validator.add(this, new ValidatorError(getProperty(), message));
            } else {
                result = super.doValidation(validator) && isValidReference(validator);
            }
        }
        return result;
    }

    /**
     * Invoked to create a new object.
     */
    @Override
    protected void onCreate() {
        IMObjectCreatorListener listener = new IMObjectCreatorListener() {
            public void created(IMObject object) {
                onCreated(object);
            }

            public void cancelled() {
                getFocusGroup().setFocus(); // restore focus
            }
        };

        IMObjectCreator.create(getProperty().getDisplayName(), getProperty().getArchetypeRange(),
                               listener, getLayoutContext().getHelpContext());
    }

    /**
     * Invoked when an object is created. Pops up an editor to edit it.
     *
     * @param object the object to edit
     */
    protected void onCreated(IMObject object) {
        Context context = new LocalContext(getContext());
        context.setCurrent(object);
        HelpContext help = getLayoutContext().getHelpContext().topic(object, "edit");
        LayoutContext layoutContext = new DefaultLayoutContext(true, context, help);
        final IMObjectEditor editor = IMObjectEditorFactory.create(object, parent, layoutContext);
        final EditDialog dialog = EditDialogFactory.create(editor, context);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void onClose(WindowPaneEvent event) {
                getFocusGroup().setFocus(); // restore focus
                onEditCompleted(editor);
            }
        });

        dialog.show();
    }

    /**
     * Invoked when the editor is closed.
     *
     * @param editor the editor
     */
    @SuppressWarnings("unchecked")
    protected void onEditCompleted(IMObjectEditor editor) {
        if (!editor.isCancelled() && !editor.isDeleted()) {
            setObject((T) editor.getObject());
        }
    }

    /**
     * Returns the context.
     *
     * @return the context
     */
    protected Context getContext() {
        return getLayoutContext().getContext();
    }

    /**
     * Returns the object corresponding to the reference.
     *
     * @return the object, or {@code null} if the reference is {@code null} or the object no longer exists
     */
    @SuppressWarnings("unchecked")
    @Override
    protected T getValue() {
        Property property = getProperty();
        IMObjectReference reference = (IMObjectReference) property.getValue();
        T object = null;
        if (reference != null) {
            object = (T) IMObjectHelper.getObject(reference, property.getArchetypeRange(), getContext());
        }
        return object;
    }

    /**
     * Updates the underlying property with the specified value.
     *
     * @param property the property
     * @param value    the value to update with. May be {@code null}
     * @return {@code true} if the property was modified
     */
    @Override
    protected boolean updateProperty(Property property, T value) {
        boolean modified;
        if (value != null) {
            modified = property.setValue(value.getObjectReference());
        } else {
            modified = property.setValue(null);
        }
        return modified;
    }

    /**
     * Determines if the reference is valid.
     *
     * @param validator the validator
     * @return {@code true} if the reference is valid, otherwise {@code false}
     */
    protected boolean isValidReference(Validator validator) {
        IMObjectReference reference = (IMObjectReference) getProperty().getValue();
        boolean result = true;
        if (reference != null && !reference.isNew()) {
            if (!isValidReference(reference)) {
                result = false;
                ArchetypeId archetypeId = reference.getArchetypeId();
                String displayName = DescriptorHelper.getDisplayName(archetypeId.getShortName());
                String message = Messages.format("imobject.invalidreference", displayName);
                validator.add(this, new ValidatorError(getProperty(), message));
            }
        }
        return result;
    }

    /**
     * Determines if a reference is valid.
     * <p/>
     * This implementation determines if the query returned by {#link #createQuery} selects the reference.
     *
     * @param reference the reference to check
     * @return {@code true} if the query selects the reference
     */
    protected boolean isValidReference(IMObjectReference reference) {
        Query<T> query = createQuery(null);
        return query.selects(reference);
    }

}
