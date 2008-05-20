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

package org.openvpms.web.component.im.edit;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreationListener;
import org.openvpms.web.component.property.CollectionProperty;
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
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractIMObjectCollectionEditor
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
     * Event broadcaster.
     */
    private final ModifiableListener broadcaster;


    /**
     * Construct a new <tt>AbstractIMObjectCollectionEditor</tt>.
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
     * Construct a new <tt>AbstractIMObjectCollectionEditor</tt>.
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
                listeners.notifyListeners(modifiable);
            }
        };
        collection.addModifiableListener(broadcaster);
    }

    /**
     * Determines if items can be added and removed.
     *
     * @param readOnly if <tt>true</tt> items can't be added and removed
     */
    public void setCardinalityReadOnly(boolean readOnly) {
        cardinalityReadOnly = readOnly;
    }

    /**
     * Determines if items can be added or removed.
     *
     * @return <tt>true</tt> if items can't be added or removed.
     */
    public boolean isCardinalityReadOnly() {
        return cardinalityReadOnly;
    }

    /**
     * Sets a listener to be notified when an object is created.
     *
     * @param listener the listener. May be <tt>null</tt>
     */
    public void setCreationListener(IMObjectCreationListener listener) {
        creationListener = listener;
    }

    /**
     * Returns the listener to be notified when an object is created.
     *
     * @return the listener, or <tt>null</tt> if none is registered
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
     * @return <tt>true</tt> if the object has been modified
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
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        listeners.removeListener(listener);
    }

    /**
     * Save any edits.
     *
     * @return <tt>true</tt> if the save was successful
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
     * @return <tt>true</tt> if edits have been saved.
     */
    public boolean isSaved() {
        return collection.isSaved();
    }

    /**
     * Determines if the object is valid.
     *
     * @return <tt>true</tt> if the object is valid; otherwise
     *         <tt>false</tt>
     */
    public boolean isValid() {
        Validator validator = new Validator();
        return validator.validate(this);
    }

    /**
     * Validates the object.
     * This validates the current object being edited, and if valid, the
     * collection.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the object and its descendents are valid
     *         otherwise <tt>false</tt>
     */
    public boolean validate(Validator validator) {
        boolean valid = true;
        if (editor != null) {
            valid = addCurrentEdits(validator);
        }
        if (valid) {
            valid = collection.validate(validator);
        }
        return valid;
    }

    /**
     * Returns the current editor.
     *
     * @return the current editor. May be <tt>null</tt>
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
     * @param editor the editor. May be <tt>null</tt>
     */
    protected void setCurrentEditor(IMObjectEditor editor) {
        this.editor = editor;
    }

    /**
     * Returns an editor for an object, creating one if it doesn't exist.
     *
     * @param object the object to edit
     * @return an editor for the object
     */
    protected IMObjectEditor getEditor(IMObject object) {
        IMObjectEditor editor = collection.getEditor(object);
        if (editor == null) {
            LayoutContext context = new DefaultLayoutContext(getContext());
            // increase the layout depth for collection items

            editor = createEditor(object, context);
            editor.addModifiableListener(broadcaster);
            collection.setEditor(object, editor);
        }
        return editor;
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context
     * @return an editor to edit <tt>object</tt>
     */
    protected IMObjectEditor createEditor(IMObject object,
                                          LayoutContext context) {
        return IMObjectEditorFactory.create(object, this.object, context);
    }

    /**
     * Adds any object being edited to the collection, if it is valid.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the object is valid,
     *         otherwise <tt>false</tt>
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
     * @return <tt>true</tt> if the object was added, otherwise <tt>false</tt>
     */
    protected boolean addEdited(IMObjectEditor editor) {
        IMObject object = editor.getObject();
        return collection.add(object);
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
     * Saves any current edits.
     *
     * @return <tt>true</tt> if edits were saved successfully, otherwise
     *         <tt>false</tt>
     */
    protected boolean doSave() {
        if (editor != null) {
            addEdited(editor);
        }
        return collection.save();
    }

    /**
     * Invoked when a editor changes.
     *
     * @param modifiable the modifiable
     */
    protected void onEditorChanged(Modifiable modifiable) {
        // no-op
    }

}
