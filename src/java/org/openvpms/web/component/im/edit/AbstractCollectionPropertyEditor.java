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

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
package org.openvpms.web.component.im.edit;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Abstract implementation of the {@link CollectionPropertyEditor} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractCollectionPropertyEditor
        implements CollectionPropertyEditor {

    /**
     * The property being edited.
     */
    private final CollectionProperty property;

    /**
     * The set of edited objects.
     */
    private final Set<IMObject> edited = new HashSet<IMObject>();

    /**
     * The editors. Where present, these will be responsible for saving/removing
     * the associated object.
     */
    private Map<IMObject, IMObjectEditor> editors
            = new HashMap<IMObject, IMObjectEditor>();


    /**
     * Indicates if any object has been saved.
     */
    private boolean saved;


    /**
     * Construct a new <tt>AbstractCollectionPropertyEditor</tt>.
     *
     * @param property the collection property
     */
    public AbstractCollectionPropertyEditor(CollectionProperty property) {
        this.property = property;
    }

    /**
     * Returns the collection property.
     *
     * @return the property
     */
    public CollectionProperty getProperty() {
        return property;
    }

    /**
     * Returns the range of archetypes that the collection may contain.
     * Any wildcards are expanded.
     *
     * @return the range of archetypes
     */
    public String[] getArchetypeRange() {
        return property.getArchetypeRange();
    }

    /**
     * Adds an object to the collection, if it doesn't exist.
     *
     * @param object the object to add
     * @return <tt>true</tt> if the object was added, otherwise <tt>false</tt>
     */
    public boolean add(IMObject object) {
        boolean added = false;
        if (!property.getValues().contains(object)) {
            property.add(object);
            added = true;
        }
        addEdited(object);
        return added;
    }

    /**
     * Associates an object in the collection with an editor. The editor
     * will be responsible for saving/removing it.
     *
     * @param object the object
     * @param editor the editor. Use <tt>null</tt> to remove an association
     */
    public void setEditor(IMObject object, IMObjectEditor editor) {
        if (editor == null) {
            editors.remove(object);
        } else {
            editors.put(object, editor);
        }
    }

    /**
     * Returns the editor associated with an object in the collection.
     *
     * @param object the object
     * @return the associated editor, or <tt>null</tt> if none is found
     */
    public IMObjectEditor getEditor(IMObject object) {
        return editors.get(object);
    }

    /**
     * Returns the editors.
     * <p/>
     * There may be fewer editors than there are objects in the collection,
     * as objects may not have an associated editor.
     *
     * @return the editors
     */
    public Collection<IMObjectEditor> getEditors() {
        return editors.values();
    }

    /**
     * Removes an object from the collection.
     * This removes any associated editor.
     *
     * @param object the object to remove
     * @return <tt>true</tt> if the object was removed
     */
    public boolean remove(IMObject object) {
        boolean removed = property.getValues().contains(object);
        removed |= removeEdited(object);
        property.remove(object); // will notify listeners, so invoke last
        return removed;
    }

    /**
     * Determines if the collection has been modified.
     *
     * @return <tt>true</tt> if the collection has been modified
     */
    public boolean isModified() {
        boolean modified = property.isModified() || !edited.isEmpty();
        if (!modified) {
            for (IMObjectEditor editor : editors.values()) {
                if (editor.isModified()) {
                    modified = true;
                    break;
                }
            }
        }
        return modified;
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        property.clearModified();
        for (IMObjectEditor editor : editors.values()) {
            editor.clearModified();
        }
    }

    /**
     * Determines if the collection is valid.
     *
     * @return <tt>true</tt> if the collection is valid; otherwise
     *         <tt>false</tt>
     */
    public boolean isValid() {
        Validator validator = new Validator();
        return validator.validate(property);
    }

    /**
     * Validates the object.
     *
     * @param validator thhe validator
     */
    public boolean validate(Validator validator) {
        boolean valid = validator.validate(property);
        IArchetypeService service = ServiceHelper.getArchetypeService();
        for (IMObject object : getObjects()) {
            IMObjectEditor editor = getEditor(object);
            if (editor != null) {
                if (!validator.validate(editor)) {
                    valid = false;
                }
            } else {
                List<ValidatorError> errors
                        = ValidationHelper.validate(object, service);
                if (errors != null) {
                    validator.add(property, errors);
                    valid = false;
                }
            }
        }
        return valid;
    }

    /**
     * Adds a listener to be notified when this changes.
     *
     * @param listener the listener to add
     */
    public void addModifiableListener(ModifiableListener listener) {
        property.addModifiableListener(listener);
    }

    /**
     * Adds a listener to be notified when this changes, specifying the order of the listener.
     *
     * @param listener the listener to add
     * @param index    the index to add the listener at. The 0-index listener is notified first
     */
    public void addModifiableListener(ModifiableListener listener, int index) {
        property.addModifiableListener(listener, index);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        property.removeModifiableListener(listener);
    }

    /**
     * Saves any edits.
     *
     * @return <tt>true</tt> if the save was successful
     */
    public boolean save() {
        boolean saved = doSave();
        if (saved) {
            clearModified();
        }
        return saved;
    }

    /**
     * Determines if any edits have been saved.
     *
     * @return <tt>true</tt> if edits have been saved.
     */
    public boolean isSaved() {
        return saved;
    }

    /**
     * Returns the objects in the collection.
     *
     * @return the objects in the collection
     */
    public List<IMObject> getObjects() {
        List<IMObject> objects = Collections.emptyList();
        Collection values = property.getValues();
        int size = values.size();
        if (size != 0) {
            objects = new ArrayList<IMObject>();
            for (Object value : values) {
                objects.add((IMObject) value);
            }
        }
        return objects;
    }

    /**
     * Returns the minimum cardinality.
     *
     * @return the minimum cardinality
     */
    public int getMinCardinality() {
        return property.getMinCardinality();
    }

    /**
     * Returns the maximum cardinality.
     *
     * @return the maximum cardinality, or <tt>-1</tt> if it is unbounded
     */
    public int getMaxCardinality() {
        return property.getMaxCardinality();
    }

    /**
     * Saves the collection.
     *
     * @return <tt>true</tt> if the save was successful
     */
    protected boolean doSave() {
        saved = false;
        if (!edited.isEmpty() || !editors.isEmpty()) {
            for (IMObjectEditor editor : editors.values()) {
                boolean result = editor.save();
                if (result) {
                    edited.remove(editor.getObject());
                    saved = true;
                } else {
                    return false;
                }
            }

            // now save objects with no associated editor
            IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
            IMObject[] edited = this.edited.toArray(new IMObject[this.edited.size()]);
            for (IMObject object : edited) {
                boolean result = SaveHelper.save(object, service);
                if (result) {
                    this.edited.remove(object);
                    saved = true;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Sets the saved state.
     *
     * @param saved if <tt>true</tt> indicates that this has been saved
     */
    protected void setSaved(boolean saved) {
        this.saved = saved;
    }

    /**
     * Adds an object to the set of objects to save when the collection is
     * saved.
     *
     * @param object the edited object
     */
    protected void addEdited(IMObject object) {
        edited.add(object);
    }

    /**
     * Removes an object from the the set of objects to save.
     * This removes any associated editor.
     *
     * @param object the object to remove
     * @return <tt>true</tt> if the the object was being edited
     */
    protected boolean removeEdited(IMObject object) {
        editors.remove(object);
        return edited.remove(object);
    }
}
