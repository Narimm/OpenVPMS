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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreationListener;
import org.openvpms.web.component.property.AbstractModifiable;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.ErrorListener;
import org.openvpms.web.component.property.ErrorListeners;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.ModifiableListeners;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * Abstract implementation of the {@link IMObjectCollectionEditor} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractIMObjectCollectionEditor extends AbstractModifiable
        implements IMObjectCollectionEditor {

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
     * The current editor.
     */
    private IMObjectEditor editor;

    /**
     * Determines if elements may be added/removed.
     */
    private boolean cardinalityReadOnly = false;

    /**
     * The listener for creation events.
     */
    private IMObjectCreationListener creationListener;

    /**
     * The event listeners.
     */
    private final ModifiableListeners listeners = new ModifiableListeners();

    /**
     * The error listeners.
     */
    private final ErrorListeners errorListeners = new ErrorListeners();

    /**
     * Event broadcaster.
     */
    private final ModifiableListener broadcaster;


    /**
     * Constructs an {@link AbstractIMObjectCollectionEditor}.
     *
     * @param editor  the collection property
     * @param object  the object being edited
     * @param context the layout context
     */
    protected AbstractIMObjectCollectionEditor(CollectionProperty editor,
                                               IMObject object,
                                               LayoutContext context) {
        this(new DefaultCollectionPropertyEditor(editor), object, context);
    }

    /**
     * Constructs an {@link AbstractIMObjectCollectionEditor}.
     *
     * @param editor  the collection property editor
     * @param object  the object being edited
     * @param context the layout context
     */
    protected AbstractIMObjectCollectionEditor(CollectionPropertyEditor editor,
                                               IMObject object,
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
    public void dispose() {
        collection.removeModifiableListener(broadcaster);
        for (Editor editor : getCurrentEditors()) {
            editor.removeModifiableListener(broadcaster);
            editor.dispose();
        }
    }

    /**
     * Determines if items can be added and removed.
     *
     * @param readOnly if {@code true} items can't be added and removed
     */
    public void setCardinalityReadOnly(boolean readOnly) {
        cardinalityReadOnly = readOnly;
    }

    /**
     * Determines if items can be added or removed.
     *
     * @return {@code true} if items can't be added or removed.
     */
    public boolean isCardinalityReadOnly() {
        return cardinalityReadOnly;
    }

    /**
     * Sets a listener to be notified when an object is created.
     *
     * @param listener the listener. May be {@code null}
     */
    public void setCreationListener(IMObjectCreationListener listener) {
        creationListener = listener;
    }

    /**
     * Returns the listener to be notified when an object is created.
     *
     * @return the listener, or {@code null} if none is registered
     */
    public IMObjectCreationListener getCreationListener() {
        return creationListener;
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
     * Returns the object being edited.
     *
     * @return the object being edited
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
        boolean modified = collection.isModified();
        if (!modified && editor != null) {
            modified = editor.isModified();
        }
        return modified;
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        if (editor != null) {
            editor.clearModified();
        }
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
     * Adds a listener to be notified of errors.
     *
     * @param listener the listener to add
     */
    @Override
    public void addErrorListener(ErrorListener listener) {
        errorListeners.addListener(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    @Override
    public void removeErrorListener(ErrorListener listener) {
        errorListeners.removeListener(listener);
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
     * Validates the object.
     * <p/>
     * This validates the current object being edited, and if valid, the collection.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    protected boolean doValidation(Validator validator) {
        boolean result = true;
        if (editor != null) {
            result = addCurrentEdits(validator); // can invoke resetValid()
        }
        if (result) {
            result = collection.validate(validator);
        }
        return result;
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
        if (editor != null && editor.getObject() == object) {
            removeCurrentEditor();
        }
    }

    /**
     * Returns an editor for an object, creating one if it doesn't exist.
     *
     * @param object the object to edit
     * @return an editor for the object
     */
    public IMObjectEditor getEditor(IMObject object) {
        IMObjectEditor editor = collection.getEditor(object);
        if (editor == null) {
            LayoutContext context = new DefaultLayoutContext(getContext());
            // increase the layout depth for collection items

            editor = createEditor(object, context);
            addEditor(object, editor);
        }
        return editor;
    }

    /**
     * Returns the current editor.
     *
     * @return the current editor. May be {@code null}
     */
    public IMObjectEditor getCurrentEditor() {
        return editor;
    }

    /**
     * Returns all current editors.
     * <p/>
     * These include any editors that have been created for objects in the
     * collection, and the {@link #getCurrentEditor() current editor}, which
     * may be for an uncommitted object.
     *
     * @return all current editors
     */
    public Collection<IMObjectEditor> getCurrentEditors() {
        Set<IMObjectEditor> editors = new HashSet<IMObjectEditor>();
        editors.addAll(getCollectionPropertyEditor().getEditors());
        if (getCurrentEditor() != null) {
            editors.add(getCurrentEditor());
        }
        return editors;
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
     * Sets the current editor.
     *
     * @param editor the editor. May be {@code null}
     */
    protected void setCurrentEditor(IMObjectEditor editor) {
        this.editor = editor;
    }

    /**
     * Removes the current editor.
     * <p/>
     * This implementation simply invokes {@code setCurrentEditor(null)}.
     */
    protected void removeCurrentEditor() {
        setCurrentEditor(null);
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context
     * @return an editor to edit {@code object}
     */
    protected IMObjectEditor createEditor(IMObject object, LayoutContext context) {
        IMObjectEditor editor = IMObjectEditorFactory.create(object, this.object, context);
        editor.addModifiableListener(broadcaster);
        return editor;
    }

    /**
     * Adds a new editor for an object.
     *
     * @param object the object
     * @param editor the editor for the object
     */
    protected void addEditor(IMObject object, IMObjectEditor editor) {
        collection.setEditor(object, editor);
    }

    /**
     * Adds any object being edited to the collection, if it is valid.
     *
     * @param validator the validator
     * @return {@code true} if the object is valid, otherwise {@code false}
     */
    protected boolean addCurrentEdits(Validator validator) {
        boolean valid = true;
        if (editor != null) {
            valid = validator.validate(editor);
            if (valid) {
                addEdited(editor);
            }
        }
        return valid;
    }

    /**
     * Adds the object being edited to the collection, if it doesn't exist.
     *
     * @param editor the editor
     * @return {@code true} if the object was added, otherwise {@code false}
     */
    protected boolean addEdited(IMObjectEditor editor) {
        IMObject object = editor.getObject();
        boolean added = collection.add(object);
        addEditor(object, editor);
        return added;
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
        if (editor != null) {
            addEdited(editor);
        }
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
