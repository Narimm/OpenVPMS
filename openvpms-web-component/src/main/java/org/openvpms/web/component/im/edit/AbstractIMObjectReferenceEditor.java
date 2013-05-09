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

package org.openvpms.web.component.im.edit;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.edit.AbstractPropertyEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.component.im.select.IMObjectSelectorListener;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.util.IMObjectCreatorListener;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Abstract implementation of the {@link IMObjectReferenceEditor} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractIMObjectReferenceEditor<T extends IMObject>
    extends AbstractPropertyEditor implements IMObjectReferenceEditor<T> {

    /**
     * The parent object. May be {@code null}
     */
    private final IMObject parent;

    /**
     * The selector.
     */
    private IMObjectSelector<T> selector;

    /**
     * Determines if the selector listener is currently being invoked
     * to avoid redundant updates.
     */
    private boolean inListener;

    /**
     * Listener for modifications to the property outside of this editor,
     * to refresh the UI.
     */
    private final ModifiableListener propertyListener;

    /**
     * The layout context.
     */
    private final LayoutContext context;


    /**
     * Constructs an {@code AbstractIMObjectReferenceEditor}.
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
        super(property);
        this.parent = parent;
        this.context = new DefaultLayoutContext(context, context.getHelpContext().subtopic("select"));

        selector = new IMObjectSelector<T>(property, allowCreate, this.context) {
            @Override
            protected Query<T> createQuery(String name) {
                return AbstractIMObjectReferenceEditor.this.createQuery(name);
            }
        };
        selector.setListener(new IMObjectSelectorListener<T>() {
            public void selected(T object) {
                inListener = true;
                try {
                    onSelected(object);
                } finally {
                    inListener = false;
                }
            }

            public void selected(T object, Browser<T> browser) {
                inListener = true;
                try {
                    onSelected(object, browser);
                } finally {
                    inListener = false;
                }
            }

            public void create() {
                onCreate();
            }
        });

        // For OVPMS-967, don't allow focus traversal
        selector.getSelect().setFocusTraversalParticipant(false);

        updateSelector();

        propertyListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onUpdate();
            }
        };
        addModifiableListener(propertyListener);
    }

    /**
     * Disposes of the editor.
     * <br/>
     * Once disposed, the behaviour of invoking any method is undefined.
     */
    public void dispose() {
        super.dispose();
        removeModifiableListener(propertyListener);
    }

    /**
     * Sets the value of the reference to the supplied object.
     *
     * @param object the object. May  be {@code null}
     */
    public boolean setObject(T object) {
        if (!inListener) {
            selector.setObject(object);
        }
        return updateProperty(object);
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        return selector.getComponent();
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
     * Determines if the reference is null.
     * This treats an entered but incorrect name as being non-null.
     *
     * @return {@code true}  if the reference is null; otherwise
     *         {@code false}
     */
    public boolean isNull() {
        boolean result = false;
        if (getProperty().getValue() == null && StringUtils.isEmpty(selector.getText())) {
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
        selector.setAllowCreate(create);
    }

    /**
     * Determines if objects may be created.
     *
     * @return {@code true} if objects may be created
     */
    public boolean allowCreate() {
        return selector.allowCreate();
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    protected boolean doValidation(Validator validator) {
        boolean result = false;
        if (!selector.inSelect()) {
            // only raise validation errors if a dialog is not displayed
            if (!selector.isValid()) {
                String message = Messages.get("imobject.invalidreference", selector.getText());
                validator.add(this, new ValidatorError(getProperty(), message));
            } else {
                result = super.doValidation(validator) && isValidReference(validator);
            }
        }
        return result;
    }

    /**
     * Invoked when an object is selected.
     * <p/>
     * This implementation simply invokes {@link #setObject}.
     *
     * @param object the selected object. May be {@code null}
     */
    protected void onSelected(T object) {
        setObject(object);
    }

    /**
     * Invoked when an object is selected from a brwoser.
     * <p/>
     * This implementation delegates to {@link #onSelected(IMObject)}.
     *
     * @param object  the selected object. May be {@code null}
     * @param browser the browser
     */
    protected void onSelected(T object, Browser<T> browser) {
        onSelected(object);
    }

    /**
     * Invoked when the underlying property updates.
     * <p/>
     * This implementation is a no-op.
     *
     * @param object the updated object. May be {@code null}
     */
    protected void onUpdated(T object) {
    }

    /**
     * Invoked to create a new object.
     */
    protected void onCreate() {
        IMObjectCreatorListener listener = new IMObjectCreatorListener() {
            public void created(IMObject object) {
                onCreated(object);
            }

            public void cancelled() {
                getFocusGroup().setFocus(); // restore focus
            }
        };

        IMObjectCreator.create(getProperty().getDisplayName(),
                               getProperty().getArchetypeRange(),
                               listener, getLayoutContext().getHelpContext());
    }

    /**
     * Invoked when an object is created. Pops up an editor to edit it.
     *
     * @param object the object to edit
     */
    protected void onCreated(IMObject object) {
        Context context = new LocalContext(this.context.getContext());
        context.setCurrent(object);
        HelpContext help = this.context.getHelpContext().topic(object, "edit");
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
     * Creates a query to select objects.
     *
     * @param name the name to filter on. May be {@code null}
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    protected Query<T> createQuery(String name) {
        String[] shortNames = getProperty().getArchetypeRange();
        Query<T> query = QueryFactory.create(shortNames, context.getContext());
        query.setValue(name);
        return query;
    }

    /**
     * Returns the context.
     *
     * @return the context
     */
    protected Context getContext() {
        return context.getContext();
    }

    /**
     * Returns the layout context.
     *
     * @return the layout context
     */
    protected LayoutContext getLayoutContext() {
        return context;
    }

    /**
     * Invoked when the property updates. Updates the selector and invokes
     * {@link #onUpdated}.
     */
    private void onUpdate() {
        resetValid();
        T object = updateSelector();
        onUpdated(object);
    }

    /**
     * Updates the selector from the property.
     *
     * @return the current object, or {@code null} if there is none
     */
    private T updateSelector() {
        T object = getObject();
        selector.setObject(object);
        return object;
    }

    /**
     * Returns the object corresponding to the reference.
     *
     * @return the object, or {@code null} if the reference is {@code null} or the object no
     *         longer exists
     */
    @SuppressWarnings("unchecked")
    private T getObject() {
        Property property = getProperty();
        IMObjectReference reference = (IMObjectReference) property.getValue();
        T object = null;
        if (reference != null) {
            object = (T) IMObjectHelper.getObject(reference, property.getArchetypeRange(), context.getContext());
        }
        return object;
    }

    /**
     * Updates the underlying property, notifying any registered listeners.
     *
     * @param object the object. May be {@code null}
     * @return {@code true} if the value was set, {@code false} if it cannot be set due to error, or is the same as
     *         the existing value
     */
    private boolean updateProperty(IMObject object) {
        boolean modified = false;
        removeModifiableListener(propertyListener);
        try {
            Property property = getProperty();
            if (object != null) {
                modified = property.setValue(object.getObjectReference());
            } else {
                modified = property.setValue(null);
            }
            if (modified) {
                resetValid();
            }
        } finally {
            addModifiableListener(propertyListener);
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
                String message = Messages.get("imobject.invalidreference", displayName);
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
