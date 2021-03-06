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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.PropertyEditor;
import org.openvpms.web.component.edit.Saveable;
import org.openvpms.web.component.property.CollectionProperty;


/**
 * Editor for a collection of {@link IMObject}s.
 *
 * @author Tim Anderson
 */
public interface IMObjectCollectionEditor extends PropertyEditor, Saveable {

    /**
     * Returns the collection property.
     *
     * @return the collection property
     */
    CollectionProperty getCollection();

    /**
     * Returns the parent of the collection.
     *
     * @return the parent object
     */
    IMObject getObject();

    /**
     * Adds an object to the collection, if it doesn't exist.
     *
     * @param object the object to add
     * @return {@code true} if the object was added, otherwise {@code false}
     */
    boolean add(IMObject object);

    /**
     * Removes an object from the collection.
     *
     * @param object the object to remove
     */
    void remove(IMObject object);

}
