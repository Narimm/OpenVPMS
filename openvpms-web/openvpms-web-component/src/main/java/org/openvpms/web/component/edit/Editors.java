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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.edit;

import org.openvpms.web.component.property.AbstractModifiable;
import org.openvpms.web.component.property.ErrorListener;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.ModifiableListeners;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.Validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Collection of {@link Editor} instances.
 *
 * @author Tim Anderson
 */
public class Editors extends AbstractModifiable {

    /**
     * Caches the modified status.
     */
    private boolean modified;

    /**
     * The event listeners.
     */
    private ModifiableListeners listeners;

    /**
     * The listener for property and editor modifications.
     */
    private ModifiableListener listener;

    /**
     * The listener for errors.
     */
    private ErrorListener errorListener;

    /**
     * Listener to handle alerts. May be {@code null}
     */
    private AlertListener alertListener;

    /**
     * The properties.
     */
    private final PropertySet properties;

    /**
     * The set of editors.
     */
    private final Set<Editor> editors = new HashSet<>();

    /**
     * The set of editors associated with properties, keyed on property name.
     */
    private final Map<String, Editor> propertyEditors = new HashMap<>();

    /**
     * Used to determine if the editors have changed while validation is in progress. If so, validation needs
     * to be redone.
     */
    private int modCount = 0;

    /**
     * Constructs an {@link Editors}.
     *
     * @param properties the properties being edited
     * @param listeners  the listeners
     */
    public Editors(PropertySet properties, ModifiableListeners listeners) {
        this.properties = properties;
        this.listeners = listeners;
        listener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onModified(modifiable);
            }
        };

        for (Property property : properties.getProperties()) {
            // initially register the listener with each property. If an editor for a property is registered,
            // the listener will be moved to the editor, to avoid redundant notifications.
            property.addModifiableListener(listener);
            property.setErrorListener(errorListener);
        }
    }

    /**
     * Adds an editor.
     *
     * @param editor the editor to add
     */
    public void add(Editor editor) {
        if (editor instanceof PropertyEditor) {
            PropertyEditor p = (PropertyEditor) editor;
            add(p, p.getProperty());
        } else {
            addEditor(editor);
        }
    }

    /**
     * Adds an editor, associating it with a property.
     *
     * @param editor   the editor to add
     * @param property the property
     */
    public void add(Editor editor, Property property) {
        addEditor(editor);
        propertyEditors.put(property.getName(), editor);
    }

    /**
     * Returns a property editor, given its name.
     *
     * @param name the property name
     * @return the property editor associated with {@code name}, or {@code null} if none exists
     */
    public Editor getEditor(String name) {
        return propertyEditors.get(name);
    }

    /**
     * Removes an editor.
     *
     * @param editor the editor to remove
     */
    public void remove(Editor editor) {
        modCount++;
        resetValid(false);
        editor.removeModifiableListener(listener);
        editor.getErrorListener();
        if (editor instanceof PropertyEditor) {
            PropertyEditor p = (PropertyEditor) editor;
            String name = p.getProperty().getName();
            Property property = properties.get(name);
            if (property != null) {
                // if the property is registered, move the listener to property
                property.addModifiableListener(listener);
                property.setErrorListener(errorListener);
            }
            propertyEditors.remove(name);
        }
        editors.remove(editor);
    }

    /**
     * Returns the editors.
     *
     * @return the editors
     */
    public Set<Editor> getEditors() {
        return editors;
    }

    /**
     * Returns all {@link Saveable} objects that have been modified.
     *
     * @return a list of modified save-able objects.
     */
    public List<Saveable> getModifiedSaveable() {
        List<Saveable> result = new ArrayList<>();
        for (Modifiable modifiable : editors) {
            if ((modifiable instanceof Saveable) && modifiable.isModified()) {
                result.add((Saveable) modifiable);
            }
        }
        return result;
    }

    /**
     * Returns all {@link Cancellable} editors.
     *
     * @return a list of all Cancellable editors.
     */
    public List<Cancellable> getCancellable() {
        List<Cancellable> result = new ArrayList<>();
        for (Modifiable modifiable : editors) {
            if (modifiable instanceof Cancellable) {
                result.add((Cancellable) modifiable);
            }
        }
        return result;
    }

    /**
     * Returns all {@link Deletable} editors.
     *
     * @return a list of all Deletable editors.
     */
    public List<Deletable> getDeletable() {
        List<Deletable> result = new ArrayList<>();
        for (Modifiable modifiable : editors) {
            if (modifiable instanceof Deletable) {
                result.add((Deletable) modifiable);
            }
        }
        return result;
    }

    /**
     * Determines if the object has been modified.
     *
     * @return {@code true} if the object has been modified
     */
    public boolean isModified() {
        if (!modified) {
            for (Modifiable modifiable : editors) {
                if (modifiable.isModified()) {
                    modified = true;
                    break;
                }
            }
            if (!modified) {
                modified = properties.isModified();
            }
        }
        return modified;
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        modified = false;
        for (Modifiable modifiable : editors) {
            modifiable.clearModified();
        }
        properties.clearModified();
    }

    /**
     * Adds a listener to be notified when this changes.
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
     * Removes a listener.
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
     * Registers a listener to be notified of alerts.
     *
     * @param listener the listener. May be {@code null}
     */
    public void setAlertListener(AlertListener listener) {
        this.alertListener = listener;
        for (Editor editor : editors) {
            editor.setAlertListener(listener);
        }
    }

    /**
     * Returns the listener to be notified of alerts.
     *
     * @return the listener. May be {@code null}
     */
    public AlertListener getAlertListener() {
        return alertListener;
    }

    /**
     * Disposes of the editors.
     */
    public void dispose() {
        modCount++;
        for (Editor editor : editors) {
            editor.dispose();
        }
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        boolean result = true;
        int count = modCount;
        for (Modifiable modifiable : editors) {
            if (!validator.validate(modifiable) || count != modCount) {
                result = false;
                break;
            }
        }
        if (result) {
            // validate each property not associated with an editor
            for (Property property : properties.getProperties()) {
                String name = property.getName();
                if (getEditor(name) == null) {
                    if (!validator.validate(property)) {
                        result = false;
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Resets the cached validity state of the object.
     *
     * @param descendants if {@code true} reset the validity state of any descendants as well.
     */
    @Override
    protected void resetValid(boolean descendants) {
        super.resetValid(descendants);
        if (descendants) {
            for (Modifiable modifiable : editors) {
                modifiable.resetValid();
            }

            // reset state of properties not associated with an editor
            for (Property property : properties.getProperties()) {
                String name = property.getName();
                if (getEditor(name) == null) {
                    property.resetValid();
                }
            }
        }
    }

    /**
     * Invoked when a {@link Modifiable} changes. Resets the cached valid status and forwards the event to any
     * registered listener.
     *
     * @param modified the changed instance
     */
    protected void onModified(Modifiable modified) {
        resetValid(false);
        listeners.notifyListeners(modified);
    }

    /**
     * Adds an editor.
     *
     * @param editor the editor to add
     */
    private void addEditor(Editor editor) {
        modCount++;
        resetValid(false);
        if (editor instanceof PropertyEditor) {
            PropertyEditor p = (PropertyEditor) editor;
            String name = p.getProperty().getName();
            Property property = properties.get(name);
            if (property != null) {
                // if the property is registered, remove the listener from the property
                property.removeModifiableListener(listener);
            }
        }
        editors.add(editor);
        editor.addModifiableListener(listener);
        editor.setAlertListener(alertListener);
    }

}
