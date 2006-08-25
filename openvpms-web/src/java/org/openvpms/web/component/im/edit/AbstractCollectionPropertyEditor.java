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

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.edit.Validator;
import org.openvpms.web.spring.ServiceHelper;

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
    private final CollectionProperty _property;

    /**
     * The set of edited objects.
     */
    private final Set<IMObject> _edited = new HashSet<IMObject>();

    /**
     * The editors. Where present, these will be responsible for saving/removing
     * the associated object.
     */
    private Map<IMObject, IMObjectEditor> _editors
            = new HashMap<IMObject, IMObjectEditor>();


    /**
     * Indicates if any object has been saved.
     */
    private boolean _saved;


    /**
     * Construct a new <code>AbstractCollectionPropertyEditor</code>.
     *
     * @param property the collection property
     */
    public AbstractCollectionPropertyEditor(CollectionProperty property) {
        _property = property;
    }

    /**
     * Returns the collection property.
     *
     * @return the property
     */
    public CollectionProperty getProperty() {
        return _property;
    }

    /**
     * Returns the range of archetypes that the collection may contain.
     * Any wildcards are expanded.
     *
     * @return the range of archetypes
     */
    public String[] getArchetypeRange() {
        NodeDescriptor descriptor = _property.getDescriptor();
        return DescriptorHelper.getShortNames(descriptor);
    }

    /**
     * Adds an object to the collection, if it doesn't exist.
     *
     * @param object the object to add
     * @return <code>true</code> if the object was added, otherwise
     *         <code>false</code>
     */
    public boolean add(IMObject object) {
        boolean added = false;
        if (!_property.getValues().contains(object)) {
            _property.add(object);
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
     * @param editor the editor. Use <code>null</code> to remove an association
     */
    public void setEditor(IMObject object, IMObjectEditor editor) {
        if (editor == null) {
            _editors.remove(object);
        } else {
            _editors.put(object, editor);
        }
    }

    /**
     * Returns the editor associated with an object in the collection.
     *
     * @param object the object
     * @return the associated editor, or <code>null</code> if none is found
     */
    public IMObjectEditor getEditor(IMObject object) {
        return _editors.get(object);
    }

    /**
     * Removes an object from the collection.
     * This removes any associated editor.
     *
     * @param object the object to remove
     */
    public void remove(IMObject object) {
        _property.remove(object);
        removeEdited(object);
    }

    /**
     * Determines if the collection has been modified.
     *
     * @return <code>true</code> if the collection has been modified
     */
    public boolean isModified() {
        boolean modified = _property.isModified() || !_edited.isEmpty();
        if (!modified) {
            for (IMObjectEditor editor : _editors.values()) {
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
        _property.clearModified();
        for (IMObjectEditor editor : _editors.values()) {
            editor.clearModified();
        }
    }

    /**
     * Determines if the collection is valid.
     *
     * @return <code>true</code> if the collection is valid; otherwise
     *         <code>false</code>
     */
    public boolean isValid() {
        Validator validator = new Validator();
        validator.validate(_property);
        return validator.isValid();
    }

    /**
     * Validates the object.
     *
     * @param validator thhe validator
     */
    public boolean validate(Validator validator) {
        boolean valid = validator.validate(_property);
        IArchetypeService service = ServiceHelper.getArchetypeService();
        for (IMObject object : getObjects()) {
            IMObjectEditor editor = getEditor(object);
            if (editor != null) {
                if (!validator.validate(editor)) {
                    valid = false;
                }
            } else {
                List<ValidationError> errors
                        = ValidationHelper.validate(object, service);
                if (errors != null) {
                    validator.add(_property, errors);
                    valid = false;
                }
            }
        }
        return valid;
    }

    /**
     * Saves any edits.
     *
     * @return <code>true</code> if the save was successful
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
     * @return <code>true</code> if edits have been saved.
     */
    public boolean isSaved() {
        return _saved;
    }

    /**
     * Returns the objects in the collection.
     *
     * @return the objects in the collection
     */
    public List<IMObject> getObjects() {
        List<IMObject> objects = Collections.emptyList();
        Collection values = _property.getValues();
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
        return _property.getMinCardinality();
    }

    /**
     * Returns the maximum cardinality.
     *
     * @return the maximum cardinality, or <code>-1</code> if it is unbounded
     */
    public int getMaxCardinality() {
        return _property.getMaxCardinality();
    }

    /**
     * Saves the collection.
     *
     * @return <code>true</code> if the save was successful
     */
    protected boolean doSave() {
        boolean result = false;
        if (!_edited.isEmpty() || !_editors.isEmpty()) {
            for (IMObjectEditor editor : _editors.values()) {
                result = editor.save();
                if (result) {
                    _edited.remove(editor.getObject());
                    _saved = true;
                } else {
                    break;
                }
            }

            // now save objects with no associated editor
            IArchetypeService service
                    = ArchetypeServiceHelper.getArchetypeService();
            IMObject[] edited = _edited.toArray(new IMObject[0]);
            for (IMObject object : edited) {
                result = SaveHelper.save(object, service);
                if (result) {
                    _edited.remove(object);
                    _saved = true;
                } else {
                    break;
                }
            }
        } else {
            result = true;
        }
        return result;
    }

    /**
     * Sets the saved state.
     *
     * @param saved if <code>true</code> indicates that this has been saved
     */
    protected void setSaved(boolean saved) {
        _saved = saved;
    }

    /**
     * Adds an object to the set of objects to save when the collection is
     * saved.
     *
     * @param object the edited object
     */
    protected void addEdited(IMObject object) {
        _edited.add(object);
    }

    /**
     * Removes an object from the the set of objects to save.
     * This removes any associated editor.
     *
     * @param object the object to remove
     */
    protected void removeEdited(IMObject object) {
        _edited.remove(object);
        _editors.remove(object);
    }
}
