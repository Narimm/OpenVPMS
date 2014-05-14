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

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;

import java.util.List;

/**
 * An {@link IMObjectCollectionEditor} for collections with a maximum cardinality of {@code 1} that
 * provides an {@link IMObjectSelector} to select the collection object.
 *
 * @author Tim Anderson
 */
public class SelectorIMObjectCollectionEditor extends AbstractSelectorPropertyEditor<IMObject>
        implements IMObjectCollectionEditor {

    /**
     * The collection property editor.
     */
    private CollectionPropertyEditor editor;

    /**
     * The parent object.
     */
    private IMObject object;

    /**
     * Constructs an {@link SelectorIMObjectCollectionEditor}.
     *
     * @param property the property
     * @param context  the layout context
     */
    public SelectorIMObjectCollectionEditor(CollectionProperty property, IMObject object, LayoutContext context) {
        super(property, context);
        this.object = object;
        editor = new DefaultCollectionPropertyEditor(property);
        updateSelector();
    }

    /**
     * Save any edits.
     *
     * @return {@code true} if the save was successful
     */
    @Override
    public boolean save() {
        boolean saved = editor.save();
        if (saved) {
            clearModified();
        }
        return saved;
    }

    /**
     * Returns the object corresponding to the property.
     *
     * @return the object. May be {@code null}
     */
    @Override
    protected IMObject getValue() {
        List<IMObject> objects = editor.getObjects();
        return !objects.isEmpty() ? objects.get(0) : null;
    }

    /**
     * Updates the underlying property with the specified value.
     *
     * @param property the property
     * @param value    the value to update with. May be {@code null}
     * @return {@code true} if the property was modified
     */
    @Override
    protected boolean updateProperty(Property property, IMObject value) {
        boolean result = false;
        List<IMObject> objects = editor.getObjects();
        if (value == null) {
            for (IMObject object : objects) {
                result |= editor.remove(object);
            }
        } else if ((objects.size() == 1 && !ObjectUtils.equals(objects.get(0), value) || objects.size() > 1)) {
            for (IMObject object : objects) {
                editor.remove(object);
            }
            editor.add(value);
            result = true;
        }
        return result;
    }

    /**
     * Returns the collection property.
     *
     * @return the collection property
     */
    @Override
    public CollectionProperty getCollection() {
        return editor.getProperty();
    }

    /**
     * Returns the parent of the collection.
     *
     * @return the parent object
     */
    @Override
    public IMObject getObject() {
        return object;
    }

    /**
     * Adds an object to the collection.
     *
     * @param object the object to add
     */
    @Override
    public void add(IMObject object) {
        editor.add(object);
    }

    /**
     * Removes an object from the collection.
     *
     * @param object the object to remove
     */
    @Override
    public void remove(IMObject object) {
        editor.remove(object);
    }

    /**
     * Determines if any edits have been saved.
     *
     * @return {@code true} if edits have been saved.
     */
    @Override
    public boolean isSaved() {
        return editor.isSaved();
    }
}
