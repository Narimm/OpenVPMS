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
 *  $Id: Editors.java 2034 2007-05-04 07:03:38Z tanderson $
 */

package org.openvpms.web.component.edit;

import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.ModifiableListeners;
import org.openvpms.web.component.property.Property;
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
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-05-04 07:03:38Z $
 */
public class Editors implements Modifiable {

    /**
     * Caches the modified status.
     */
    private boolean modified;

    /**
     * The event listeners.
     */
    private ModifiableListeners listeners = new ModifiableListeners();

    /**
     * The set of editors.
     */
    private final Set<Editor> editors = new HashSet<Editor>();

    /**
     * The set of editors associated with properties, keyed on property name.
     */
    private final Map<String, Editor> propertyEditors
            = new HashMap<String, Editor>();


    /**
     * Construct a new <code>Editors</code>.
     */
    public Editors() {
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
     * @return the property editor associated with <code>name</code>, or
     *         <code>null</code> if none exists
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
        if (editor instanceof PropertyEditor) {
            PropertyEditor p = (PropertyEditor) editor;
            propertyEditors.remove(p.getProperty().getName());
        }
        editors.remove(editor);
    }

    /**
     * Removes all the editors.
     */
    public void removeAll() {
        for (Editor editor : editors.toArray(new Editor[0])) {
            remove(editor);
        }
    }

    /**
     * Returns all saveable objects that have been modified.
     *
     * @return a list of modified saveable objects.
     */
    public List<Saveable> getModifiedSaveable() {
        List<Saveable> result = new ArrayList<Saveable>();
        for (Modifiable modifiable : editors) {
            if ((modifiable instanceof Saveable)
                    && modifiable.isModified()) {
                result.add((Saveable) modifiable);
            }
        }
        return result;
    }

    /**
     * Returns all {@link Cancellable} editers.
     *
     * @return a list of all Cancellable editors.
     */
    public List<Cancellable> getCancellable() {
        List<Cancellable> result = new ArrayList<Cancellable>();
        for (Modifiable modifiable : editors) {
            if (modifiable instanceof Cancellable) {
                result.add((Cancellable) modifiable);
            }
        }
        return result;
    }

    /**
     * Returns all {@link Deletable} editers.
     *
     * @return a list of all Deletable editors.
     */
    public List<Deletable> getDeletable() {
        List<Deletable> result = new ArrayList<Deletable>();
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
     * @return <code>true</code> if the object has been modified
     */
    public boolean isModified() {
        if (!modified) {
            for (Modifiable modifiable : editors) {
                if (modifiable.isModified()) {
                    modified = true;
                    return modified;
                }
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
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        listeners.removeListener(listener);
    }

    /**
     * Determines if the object is valid.
     *
     * @return <code>true</code> if the object is valid; otherwise
     *         <code>false</code>
     */
    public boolean isValid() {
        Validator validator = new Validator();
        return validate(validator);
    }

    /**
     * Validates the object.
     *
     * @param validator thhe validator
     */
    public boolean validate(Validator validator) {
        boolean valid = true;
        for (Modifiable modifiable : editors) {
            if (!validator.validate(modifiable)) {
                valid = false;
            }
        }
        return valid;
    }

    /**
     * Returns <code>true</code> if no editors are registered.
     *
     * @return <code>true</code> if no editors are registered, otherwise
     *         <code>false</code>
     */
    public boolean isEmpty() {
        return editors.isEmpty();
    }

    /**
     * Invoked when a {@link Modifiable} changes. Forwards the event to any
     * registered listener.
     *
     * @param modified the changed instance
     */
    protected void notifyListeners(Modifiable modified) {
        listeners.notifyListeners(modified);
    }

    /**
     * Adds an editor.
     *
     * @param editor the editor to add
     */
    private void addEditor(Editor editor) {
        editors.add(editor);
        editor.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                notifyListeners(modifiable);
            }
        });
    }

}
