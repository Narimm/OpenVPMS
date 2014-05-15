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

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.AbstractModifiable;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.ErrorListener;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.ModifiableListeners;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;


/**
 * Abstract implementation of the {@link IMObjectCollectionEditor} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractIMObjectCollectionEditor extends AbstractModifiable
        implements IMObjectCollectionEditor {

    /**
     * Nodes to sort candidate objects on.
     */
    protected final String[] DEFAULT_SORT_NODES = new String[]{"name", "id"};

    /**
     * The collection.
     */
    private final CollectionPropertyEditor collection;

    /**
     * The parent object.
     */
    private final IMObject object;

    /**
     * The layout context.
     */
    private final LayoutContext context;

    /**
     * The component representing this.
     */
    private Component component;

    /**
     * The event listeners.
     */
    private final ModifiableListeners listeners = new ModifiableListeners();

    /**
     * Event broadcaster.
     */
    private final ModifiableListener broadcaster;

    /**
     * The error listener.
     */
    private ErrorListener errorListener;

    /**
     * Constructs an {@link AbstractIMObjectCollectionEditor}.
     *
     * @param editor  the collection property
     * @param object  the object being edited
     * @param context the layout context
     */
    protected AbstractIMObjectCollectionEditor(CollectionProperty editor, IMObject object, LayoutContext context) {
        this(new DefaultCollectionPropertyEditor(editor), object, context);
    }

    /**
     * Constructs an {@link AbstractIMObjectCollectionEditor}.
     *
     * @param editor  the collection property editor
     * @param object  the object being edited
     * @param context the layout context
     */
    protected AbstractIMObjectCollectionEditor(CollectionPropertyEditor editor, IMObject object,
                                               LayoutContext context) {
        collection = editor;
        this.object = object;
        this.context = context;
        broadcaster = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onModified(modifiable);
            }
        };
        collection.addModifiableListener(broadcaster);
    }

    /**
     * Disposes of the editor.
     * <br/>
     * Once disposed the behaviour of invoking any method is undefined.
     */
    @Override
    public void dispose() {
        collection.removeModifiableListener(broadcaster);
        collection.setErrorListener(null);
    }

    /**
     * Returns the property being edited.
     *
     * @return the property being edited
     */
    public Property getProperty() {
        return collection.getProperty();
    }

    /**
     * Returns the rendered collection.
     *
     * @return the rendered collection
     */
    public Component getComponent() {
        if (component == null) {
            component = doLayout(context);
        }
        return component;
    }

    /**
     * Returns the parent of the collection.
     *
     * @return the parent object
     */
    public IMObject getObject() {
        return object;
    }

    /**
     * Returns the collection property.
     *
     * @return the collection property
     */
    public CollectionProperty getCollection() {
        return collection.getProperty();
    }

    /**
     * Determines if the object has been modified.
     *
     * @return {@code true} if the object has been modified
     */
    public boolean isModified() {
        return collection.isModified();
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        collection.clearModified();
    }

    /**
     * Add a listener to be notified when this changes.
     *
     * @param listener the listener to add
     */
    public void addModifiableListener(ModifiableListener listener) {
        listeners.addListener(listener);
    }

    /**
     * Adds a listener to be notified when this changes, specifying the order of the listener.
     *
     * @param listener the listener to add
     * @param index    the index to add the listener at. The 0-index listener is notified first
     */
    public void addModifiableListener(ModifiableListener listener, int index) {
        listeners.addListener(listener, index);
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        listeners.removeListener(listener);
    }

    /**
     * Sets a listener to be notified of errors.
     *
     * @param listener the listener to register. May be {@code null}
     */
    @Override
    public void setErrorListener(ErrorListener listener) {
        this.errorListener = listener;
    }

    /**
     * Returns the listener to be notified of errors.
     *
     * @return the listener. May be {@code null}
     */
    @Override
    public ErrorListener getErrorListener() {
        return errorListener;
    }

    /**
     * Save any edits.
     *
     * @return {@code true} if the save was successful
     */
    public boolean save() {
        boolean saved;
        if (!isModified()) {
            saved = true;
        } else {
            saved = doSave();
            if (saved) {
                clearModified();
            }
        }
        return saved;
    }

    /**
     * Determines if any edits have been saved.
     *
     * @return {@code true} if edits have been saved.
     */
    public boolean isSaved() {
        return collection.isSaved();
    }

    /**
     * Adds an object to the collection.
     *
     * @param object the object to add
     */
    public void add(IMObject object) {
        collection.add(object);
    }

    /**
     * Removes an object from the collection.
     *
     * @param object the object to remove
     */
    public void remove(IMObject object) {
        collection.remove(object);
    }

    /**
     * Returns the listener to receive update notifications.
     * <p/>
     * This delegates to {@link #onModified(Modifiable)}.
     *
     * @return the listener
     */
    protected ModifiableListener getModifiableListener() {
        return broadcaster;
    }

    /**
     * Validates the object.
     * <p/>
     * This validates the collection.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    protected boolean doValidation(Validator validator) {
        return collection.validate(validator);
    }

    /**
     * Lays out the component.
     *
     * @param context the layout context
     * @return the component
     */
    protected abstract Component doLayout(LayoutContext context);

    /**
     * Returns the layout context.
     *
     * @return the layout context
     */
    protected LayoutContext getContext() {
        return context;
    }

    /**
     * Returns the collection property editor.
     *
     * @return the collection property editor
     */
    protected CollectionPropertyEditor getCollectionPropertyEditor() {
        return collection;
    }

    /**
     * Returns the listeners.
     *
     * @return the listeners
     */
    protected ModifiableListeners getListeners() {
        return listeners;
    }

    /**
     * Invoked when the collection or an editor changes. Resets the cached valid status and notifies registered
     * listeners.
     *
     * @param modifiable the modifiable to pass to the listeners
     */
    protected void onModified(Modifiable modifiable) {
        resetValid(false);
        listeners.notifyListeners(modifiable);
    }

    /**
     * Saves any current edits.
     *
     * @return {@code true} if edits were saved successfully, otherwise {@code false}
     */
    protected boolean doSave() {
        return collection.save();
    }

    /**
     * Helper to return an object given its reference.
     * <p/>
     * This implementation uses the cache associated with the layout context.
     *
     * @param reference the reference. May be {@code null}
     * @return the object corresponding to {@code reference} or {@code null} if none exists
     */
    protected IMObject getObject(IMObjectReference reference) {
        return context.getCache().get(reference);
    }

}
