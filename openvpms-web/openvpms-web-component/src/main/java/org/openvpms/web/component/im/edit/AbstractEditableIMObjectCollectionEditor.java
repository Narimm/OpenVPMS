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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreationListener;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.system.ServiceHelper;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * Abstract implementation of the {@link EditableIMObjectCollectionEditor} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractEditableIMObjectCollectionEditor extends AbstractIMObjectCollectionEditor
        implements EditableIMObjectCollectionEditor {

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
     * The handler to confirm removal of objects.
     */
    private RemoveConfirmationHandler removeConfirmationHandler;

    /**
     * Constructs an {@link AbstractEditableIMObjectCollectionEditor}.
     *
     * @param editor  the collection property
     * @param object  the object being edited
     * @param context the layout context
     */
    protected AbstractEditableIMObjectCollectionEditor(CollectionProperty editor, IMObject object,
                                                       LayoutContext context) {
        this(new DefaultCollectionPropertyEditor(editor), object, context);
    }

    /**
     * Constructs an {@link AbstractEditableIMObjectCollectionEditor}.
     *
     * @param editor  the collection property editor
     * @param object  the object being edited
     * @param context the layout context
     */
    protected AbstractEditableIMObjectCollectionEditor(CollectionPropertyEditor editor, IMObject object,
                                                       LayoutContext context) {
        super(editor, object, context);
    }

    /**
     * Disposes of the editor.
     * <br/>
     * Once disposed the behaviour of invoking any method is undefined.
     */
    @Override
    public void dispose() {
        super.dispose();
        ModifiableListener listener = getModifiableListener();
        for (Editor editor : getEditors()) {
            editor.removeModifiableListener(listener);
            editor.setErrorListener(null);
            editor.dispose();
        }
    }

    /**
     * Determines if items can be added and removed.
     *
     * @param readOnly if {@code true} items can't be added and removed
     */
    @Override
    public void setCardinalityReadOnly(boolean readOnly) {
        cardinalityReadOnly = readOnly;
    }

    /**
     * Determines if items can be added or removed.
     *
     * @return {@code true} if items can't be added or removed.
     */
    @Override
    public boolean isCardinalityReadOnly() {
        return cardinalityReadOnly;
    }

    /**
     * Sets a listener to be notified when an object is created.
     *
     * @param listener the listener. May be {@code null}
     */
    @Override
    public void setCreationListener(IMObjectCreationListener listener) {
        creationListener = listener;
    }

    /**
     * Returns the listener to be notified when an object is created.
     *
     * @return the listener, or {@code null} if none is registered
     */
    @Override
    public IMObjectCreationListener getCreationListener() {
        return creationListener;
    }

    /**
     * Registers a handler to confirm removal of objects.
     *
     * @param handler the handler
     */
    @Override
    public void setRemoveConfirmationHandler(RemoveConfirmationHandler handler) {
        this.removeConfirmationHandler = handler;
    }

    /**
     * Returns the handler to confirm removal of objects.
     *
     * @return the handler. May be {@code null}
     */
    @Override
    public RemoveConfirmationHandler getRemoveConfirmationHandler() {
        return removeConfirmationHandler;
    }

    /**
     * Determines if the object has been modified.
     *
     * @return {@code true} if the object has been modified
     */
    @Override
    public boolean isModified() {
        boolean modified = super.isModified();
        if (!modified && editor != null) {
            modified = editor.isModified();
        }
        return modified;
    }

    /**
     * Clears the modified status of the object.
     */
    @Override
    public void clearModified() {
        super.clearModified();
        if (editor != null) {
            editor.clearModified();
        }
    }

    /**
     * Removes an object from the collection.
     *
     * @param object the object to remove
     */
    @Override
    public void remove(IMObject object) {
        super.remove(object);
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
    @Override
    public IMObjectEditor getEditor(IMObject object) {
        IMObjectEditor editor = getCollectionPropertyEditor().getEditor(object);
        if (editor == null) {
            LayoutContext context = new DefaultLayoutContext(getContext());
            // increase the layout depth for collection items

            editor = createEditor(object, context);
            addEditor(object, editor);
        }
        return editor;
    }

    /**
     * Determines if an editor exists for an object.
     *
     * @param object the object
     * @return {@code true} if an editor exists
     */
    public boolean hasEditor(IMObject object) {
        return getCollectionPropertyEditor().getEditor(object) != null;
    }

    /**
     * Returns the current editor.
     *
     * @return the current editor. May be {@code null}
     */
    @Override
    public IMObjectEditor getCurrentEditor() {
        return editor;
    }

    /**
     * Returns all current editors.
     * <p>
     * These include any editors that have been created for objects in the
     * collection, and the {@link #getCurrentEditor() current editor}, which
     * may be for an uncommitted object.
     *
     * @return all current editors
     */
    @Override
    public Collection<IMObjectEditor> getEditors() {
        Set<IMObjectEditor> editors = new HashSet<>();
        editors.addAll(getCollectionPropertyEditor().getEditors());
        if (getCurrentEditor() != null) {
            editors.add(getCurrentEditor());
        }
        return editors;
    }

    /**
     * Returns the objects in the collection.
     * <p>
     * This includes the object of the current editor, which may be uncommitted.
     *
     * @return the objects
     */
    @Override
    public Collection<IMObject> getCurrentObjects() {
        Set<IMObject> result = new LinkedHashSet<>(getCollectionPropertyEditor().getObjects());
        if (editor != null) {
            result.add(editor.getObject());
        }
        return result;
    }

    /**
     * Returns an editor for the first object in the collection.
     *
     * @param create@return the first object editor, or {@code null} if one wasn't found or {@code create} was {@code false} or an
     *                      editor could not be created
     */
    @Override
    public IMObjectEditor getFirstEditor(boolean create) {
        IMObject object = null;
        IMObjectEditor editor = getCurrentEditor();
        if (editor == null) {
            Collection<IMObject> objects = getCurrentObjects();
            if (!objects.isEmpty()) {
                object = objects.iterator().next();
            } else if (create) {
                object = create();
                if (object != null) {
                    add(object);
                }
            }
            if (object != null) {
                editor = getEditor(object);
            }
        }
        return editor;
    }

    /**
     * Validates the object.
     * <p>
     * This validates the current object being edited, and if valid, the collection.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        boolean result = true;
        if (editor != null) {
            result = addCurrentEdits(validator); // can invoke resetValid()
        }
        if (result) {
            result = super.doValidation(validator);
        }
        return result;
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
     * <p>
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
        return ServiceHelper.getBean(IMObjectEditorFactory.class).create(object, getObject(), context);
    }

    /**
     * Adds a new editor for an object.
     *
     * @param object the object
     * @param editor the editor for the object
     */
    protected void addEditor(IMObject object, IMObjectEditor editor) {
        editor.addModifiableListener(getModifiableListener());
        editor.setErrorListener(getErrorListener());
        getCollectionPropertyEditor().setEditor(object, editor);
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
        boolean added = add(object);
        addEditor(object, editor);
        return added;
    }

    /**
     * Saves any current edits.
     *
     * @throws OpenVPMSException if the save fails
     */
    @Override
    protected void doSave() {
        if (editor != null) {
            addEdited(editor);
        }
        super.doSave();
    }

}
