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
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.ModifiableListeners;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;


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
    private final CollectionPropertyEditor _collection;

    /**
     * The parent object.
     */
    private final IMObject _object;


    /**
     * The layout context.
     */
    private final LayoutContext _context;

    /**
     * The component representing this.
     */
    private Component _component;

    /**
     * The current editor.
     */
    private IMObjectEditor _editor;

    /**
     * The event listeners.
     */
    private final ModifiableListeners _listeners = new ModifiableListeners();

    /**
     * Event broadcaster.
     */
    private final ModifiableListener _broadcaster;


    /**
     * Construct a new <code>AbstractIMObjectCollectionEditor</code>.
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
     * Construct a new <code>AbstractIMObjectCollectionEditor</code>.
     *
     * @param editor  the collection property editor
     * @param object  the object being edited
     * @param context the layout context
     */
    protected AbstractIMObjectCollectionEditor(CollectionPropertyEditor editor,
                                               IMObject object,
                                               LayoutContext context) {
        _collection = editor;
        _object = object;
        _context = context;
        _broadcaster = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                _listeners.notifyListeners(modifiable);
            }
        };
        _collection.getProperty().addModifiableListener(_broadcaster);
    }

    /**
     * Returns the property being edited.
     *
     * @return the property being edited
     */
    public Property getProperty() {
        return _collection.getProperty();
    }

    /**
     * Returns the rendered collection.
     *
     * @return the rendered collection
     */
    public Component getComponent() {
        if (_component == null) {
            _component = doLayout(_context);
        }
        return _component;
    }

    /**
     * Returns the object being edited.
     *
     * @return the object being edited
     */
    public IMObject getObject() {
        return _object;
    }

    /**
     * Returns the collection property.
     *
     * @return the collection property
     */
    public CollectionProperty getCollection() {
        return _collection.getProperty();
    }

    /**
     * Determines if the object has been modified.
     *
     * @return <code>true</code> if the object has been modified
     */
    public boolean isModified() {
        boolean modified = _collection.isModified();
        if (!modified && _editor != null) {
            modified = _editor.isModified();
        }
        return modified;
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        if (_editor != null) {
            _editor.clearModified();
        }
        _collection.clearModified();
    }

    /**
     * Add a listener to be notified when this changes.
     *
     * @param listener the listener to add
     */
    public void addModifiableListener(ModifiableListener listener) {
        _listeners.addListener(listener);
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        _listeners.removeListener(listener);
    }

    /**
     * Save any edits.
     *
     * @return <code>true</code> if the save was successful
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
     * @return <code>true</code> if edits have been saved.
     */
    public boolean isSaved() {
        return _collection.isSaved();
    }

    /**
     * Determines if the object is valid.
     *
     * @return <code>true</code> if the object is valid; otherwise
     *         <code>false</code>
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
     * @return <code>true</code> if the object and its descendents are valid
     *         otherwise <code>false</code>
     */
    public boolean validate(Validator validator) {
        boolean valid = true;
        if (_editor != null) {
            valid = addCurrentEdits(validator);
        }
        if (valid) {
            valid = _collection.validate(validator);
        }
        return valid;
    }

    /**
     * Returns the current editor.
     *
     * @return the current editor. May be <code>null</code>
     */
    public IMObjectEditor getCurrentEditor() {
        return _editor;
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
        return _context;
    }

    /**
     * Returns the collection property editor.
     *
     * @return the collection property editor
     */
    protected CollectionPropertyEditor getCollectionPropertyEditor() {
        return _collection;
    }

    /**
     * Sets the current editor.
     *
     * @param editor the editor. May be <code>null</code>
     */
    protected void setCurrentEditor(IMObjectEditor editor) {
        _editor = editor;
    }

    /**
     * Returns an editor for an object, creating one if it doesn't exist.
     *
     * @param object the object to edit
     * @return an editor for the object
     */
    protected IMObjectEditor getEditor(IMObject object) {
        IMObjectEditor editor = _collection.getEditor(object);
        if (editor == null) {
            LayoutContext context = new DefaultLayoutContext(getContext());
            // increase the layout depth for collection items

            editor = createEditor(object, context);
            editor.addModifiableListener(_broadcaster);
            _collection.setEditor(object, editor);
        }
        return editor;
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context
     * @return an editor to edit <code>object</code>
     */
    protected IMObjectEditor createEditor(IMObject object,
                                          LayoutContext context) {
        return IMObjectEditorFactory.create(object, _object, context);
    }

    /**
     * Adds any object being edited to the collection, if it is valid.
     *
     * @param validator the validator
     * @return <code>true</code> if the object is valid,
     *         otherwise <code>false</code>
     */
    protected boolean addCurrentEdits(Validator validator) {
        boolean valid = true;
        if (_editor != null) {
            valid = validator.validate(_editor);
            if (valid) {
                addEdited(_editor);
            }
        }
        return valid;
    }

    /**
     * Adds the object being edited to the collection, if it doesn't exist.
     *
     * @param editor the editor
     * @return <code>true</code> if the object was added, otherwise
     *         <code>false</code>
     */
    protected boolean addEdited(IMObjectEditor editor) {
        IMObject object = editor.getObject();
        return _collection.add(object);
    }

    /**
     * Returnjs the listeners.
     *
     * @return the listeners
     */
    protected ModifiableListeners getListeners() {
        return _listeners;
    }

    /**
     * Saves any current edits.
     *
     * @return <code>true</code> if edits were saved successfully, otherwise
     *         <code>false</code>
     */
    protected boolean doSave() {
        if (_editor != null) {
            addEdited(_editor);
        }
        return _collection.save();
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
