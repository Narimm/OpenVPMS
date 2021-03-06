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

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.edit.AlertListener;
import org.openvpms.web.component.im.util.IMObjectCreationListener;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.ErrorListener;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.echo.focus.FocusGroup;

import java.util.Collection;


/**
 * An {@link EditableIMObjectCollectionEditor} that delegates to another.
 *
 * @author Tim Anderson
 */
public abstract class DelegatingCollectionEditor implements EditableIMObjectCollectionEditor {

    /**
     * The editor to delegate to.
     */
    private AbstractEditableIMObjectCollectionEditor editor;


    /**
     * Constructs a {@link DelegatingCollectionEditor}.
     */
    public DelegatingCollectionEditor() {
    }

    /**
     * Constructs a {@link DelegatingCollectionEditor}.
     *
     * @param editor the editor to delegate to
     */
    public DelegatingCollectionEditor(AbstractEditableIMObjectCollectionEditor editor) {
        setEditor(editor);
    }

    /**
     * Returns the collection property.
     *
     * @return the collection property
     */
    public CollectionProperty getCollection() {
        return editor.getCollection();
    }

    /**
     * Returns the parent of the collection.
     *
     * @return the parent object
     */
    public IMObject getObject() {
        return editor.getObject();
    }

    /**
     * Determines if items can be added and removed.
     *
     * @param readOnly if {@code true} items cannot be added or removed
     */
    public void setCardinalityReadOnly(boolean readOnly) {
        editor.setCardinalityReadOnly(readOnly);
    }

    /**
     * Determines if items can be added or removed.
     *
     * @return {@code true} if items can't be added or removed.
     */
    public boolean isCardinalityReadOnly() {
        return editor.isCardinalityReadOnly();
    }

    /**
     * Sets a listener to be notified when an object is created.
     *
     * @param listener the listener. May be {@code null}
     */
    public void setCreationListener(IMObjectCreationListener listener) {
        editor.setCreationListener(listener);
    }

    /**
     * Returns the listener to be notified when an object is created.
     *
     * @return the listener, or {@code null} if none is registered
     */
    @Override
    public IMObjectCreationListener getCreationListener() {
        return editor.getCreationListener();
    }

    /**
     * Registers a handler to confirm removal of objects.
     *
     * @param handler the handler. May be {@code null}
     */
    @Override
    public void setRemoveConfirmationHandler(RemoveConfirmationHandler handler) {
        editor.setRemoveConfirmationHandler(handler);
    }

    /**
     * Returns the handler to confirm removal of objects.
     *
     * @return the handler. May be {@code null}
     */
    @Override
    public RemoveConfirmationHandler getRemoveConfirmationHandler() {
        return editor.getRemoveConfirmationHandler();
    }

    /**
     * Creates a new object.
     * <p>
     * The object is not automatically added to the collection.
     * <p>
     * If an {@link IMObjectCreationListener} is registered, it will be
     * notified on successful creation of an object.
     *
     * @return a new object, or {@code null} if the object can't be created
     */
    @Override
    public IMObject create() {
        return editor.create();
    }

    /**
     * Adds an object to the collection, if it doesn't exist.
     *
     * @param object the object to add
     * @return {@code true} if the object was added, otherwise {@code false}
     */
    @Override
    public boolean add(IMObject object) {
        return editor.add(object);
    }

    /**
     * Removes an object from the collection.
     *
     * @param object the object to remove
     */
    public void remove(IMObject object) {
        editor.remove(object);
    }

    /**
     * Refreshes the collection display.
     */
    public void refresh() {
        editor.refresh();
    }

    /**
     * Returns the property being edited.
     *
     * @return the property being edited
     */
    public Property getProperty() {
        return editor.getProperty();
    }

    /**
     * Returns the edit component.
     *
     * @return the edit component
     */
    public Component getComponent() {
        return editor.getComponent();
    }

    /**
     * Determines if the object has been modified.
     *
     * @return {@code true} if the object has been modified
     */
    public boolean isModified() {
        return editor.isModified();
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        editor.clearModified();
    }

    /**
     * Adds a listener to be notified when this changes.
     *
     * @param listener the listener to add
     */
    public void addModifiableListener(ModifiableListener listener) {
        editor.addModifiableListener(listener);
    }

    /**
     * Adds a listener to be notified when this changes, specifying the order of the listener.
     *
     * @param listener the listener to add
     * @param index    the index to add the listener at. The 0-index listener is notified first
     */
    public void addModifiableListener(ModifiableListener listener, int index) {
        editor.addModifiableListener(listener, index);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        editor.removeModifiableListener(listener);
    }

    /**
     * Sets a listener to be notified of errors.
     *
     * @param listener the listener to register. May be {@code null}
     */
    @Override
    public void setErrorListener(ErrorListener listener) {
        editor.setErrorListener(listener);
    }

    /**
     * Returns the listener to be notified of errors.
     *
     * @return the listener. May be {@code null}
     */
    @Override
    public ErrorListener getErrorListener() {
        return editor.getErrorListener();
    }

    /**
     * Registers a listener to be notified of alerts.
     *
     * @param listener the listener. May be {@code null}
     */
    @Override
    public void setAlertListener(AlertListener listener) {
        editor.setAlertListener(listener);
    }

    /**
     * Returns the listener to be notified of alerts.
     *
     * @return the listener. May be {@code null}
     */
    @Override
    public AlertListener getAlertListener() {
        return editor.getAlertListener();
    }

    /**
     * Determines if the object is valid.
     *
     * @return {@code true} if the object is valid; otherwise {@code false}
     */
    public boolean isValid() {
        return editor.isValid();
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    public boolean validate(Validator validator) {
        return editor.validate(validator);
    }

    /**
     * Resets the cached validity state of the object, to force revalidation to of the object and its descendants.
     */
    public void resetValid() {
        editor.resetValid();
    }

    /**
     * Save any edits.
     *
     * @throws OpenVPMSException if the save fails
     */
    public void save() {
        editor.save();
    }

    /**
     * Determines if any edits have been saved.
     *
     * @return {@code true} if edits have been saved.
     */
    public boolean isSaved() {
        return editor.isSaved();
    }

    /**
     * Returns the current editor.
     *
     * @return the current editor, or {@code null} if there is no current editor
     */
    public IMObjectEditor getCurrentEditor() {
        return editor.getCurrentEditor();
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group, or {@code null} if the editor hasn't been
     * rendered
     */
    public FocusGroup getFocusGroup() {
        return editor.getFocusGroup();
    }

    /**
     * Disposes of the editor.
     * <br/>
     * Once disposed, the behaviour of invoking any method is undefined.
     */
    public void dispose() {
        editor.dispose();
    }

    /**
     * Returns an editor for an object, creating one if it doesn't exist.
     *
     * @param object the object to edit
     * @return an editor for the object
     */
    public IMObjectEditor getEditor(IMObject object) {
        return editor.getEditor(object);
    }

    /**
     * Returns the editor to delegate to.
     *
     * @return the editor to delegate to
     */
    public AbstractIMObjectCollectionEditor getEditor() {
        return editor;
    }

    /**
     * Returns editors for items in the collection.
     * <p>
     * These include any editors that have been created for objects in the
     * collection, and the current editor, which may be for an uncommitted object.
     *
     * @return all current editors
     */
    @Override
    public Collection<IMObjectEditor> getEditors() {
        return editor.getEditors();
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
        return editor.getCurrentObjects();
    }

    /**
     * Returns an editor for the first object in the collection.
     *
     * @param create, if {@code true} create a new instance if the collection is empty
     * @return the first object editor, or {@code null} if one wasn't found or {@code create} was {@code false} or an
     * editor could not be created
     */
    @Override
    public IMObjectEditor getFirstEditor(boolean create) {
        return editor.getFirstEditor(create);
    }

    /**
     * Registers the editor to delegate to.
     *
     * @param editor the editor to delegate to
     */
    protected void setEditor(AbstractEditableIMObjectCollectionEditor editor) {
        this.editor = editor;
    }

}
