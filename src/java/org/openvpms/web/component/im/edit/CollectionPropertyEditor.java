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
import org.openvpms.web.component.edit.Saveable;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Modifiable;

import java.util.Collection;
import java.util.List;


/**
 * An editor for a {@link CollectionProperty}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface CollectionPropertyEditor extends Modifiable, Saveable {

    /**
     * Returns the collection property.
     *
     * @return the property
     */
    CollectionProperty getProperty();

    /**
     * Returns the range of archetypes that the collection may contain.
     * Any wildcards are expanded.
     *
     * @return the range of archetypes
     */
    String[] getArchetypeRange();

    /**
     * Adds an object to the collection, if it doesn't exist.
     *
     * @param object the object to add
     * @return <tt>true</tt> if the object was added, otherwise <tt>false</tt>
     */
    boolean add(IMObject object);

    /**
     * Removes an object from the collection.
     *
     * @param object the object to remove
     * @return <tt>true</tt> if the object was removed
     */
    boolean remove(IMObject object);

    /**
     * Associates an object in the collection with an editor. The editor
     * will be responsible for saving/removing it.
     *
     * @param object the object
     * @param editor the editor
     */
    void setEditor(IMObject object, IMObjectEditor editor);

    /**
     * Returns the editor associated with an object in the collection.
     *
     * @param object the object
     * @return the associated editor, or <tt>null</tt> if none is found
     */
    IMObjectEditor getEditor(IMObject object);

    /**
     * Returns the editors.
     * <p/>
     * There may be fewer editors than there are objects in the collection,
     * as objects may not have an associated editor.
     *
     * @return the editors
     */
    Collection<IMObjectEditor> getEditors();

    /**
     * Returns the objects in the collection.
     *
     * @return the objects in the collection
     */
    List<IMObject> getObjects();

    /**
     * Returns the minimum cardinality.
     *
     * @return the minimum cardinality
     */
    int getMinCardinality();

    /**
     * Returns the maximum cardinality.
     *
     * @return the maximum cardinality, or <tt>-1</tt> if it is unbounded
     */
    int getMaxCardinality();
}
