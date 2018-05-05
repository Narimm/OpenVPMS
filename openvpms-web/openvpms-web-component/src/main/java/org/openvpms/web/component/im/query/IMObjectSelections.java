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

package org.openvpms.web.component.im.query;

import org.openvpms.component.model.object.IMObject;
import org.openvpms.component.model.object.Reference;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tracks selected {@link IMObject} instances in a {@link MultiSelectTableBrowser}.
 */
public class IMObjectSelections<T extends IMObject> implements MultiSelectTableBrowser.SelectionTracker<T> {

    /**
     * The map of selected objects.
     */
    private Map<Reference, T> objects = new LinkedHashMap<>();

    /**
     * Determines if an object is selected.
     *
     * @param object the object
     * @return {@code true} if the object is selected
     */
    public boolean isSelected(T object) {
        return objects.containsKey(object.getObjectReference());
    }

    /**
     * Marks an object as selected.
     *
     * @param object   the object
     * @param selected if {@code true}, select the object, otherwise deselect it
     */
    public void setSelected(T object, boolean selected) {
        Reference ref = object.getObjectReference();
        if (selected) {
            objects.put(ref, object);
        } else {
            objects.remove(ref);
        }
    }

    /**
     * Returns the selected objects.
     * <p>
     * This implementation returns the objects in order of selection.
     *
     * @return the selected objects
     */
    @Override
    public Collection<T> getSelected() {
        return objects.values();
    }

    /**
     * Determines if an object can be selected.
     *
     * @param object the object
     * @return {@code true}
     */
    @Override
    public boolean canSelect(T object) {
        return true;
    }

    /**
     * Clears all selections.
     */
    public void clear() {
        objects.clear();
    }
}
