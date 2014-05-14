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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.util.IMObjectCreationListener;

import java.util.Collection;

/**
 * An editor for a collection of {@link IMObject}s where the objects themselves can be edited.
 *
 * @author Tim Anderson
 */
public interface EditableIMObjectCollectionEditor extends IMObjectCollectionEditor {

    /**
     * Determines if items can be added and removed.
     *
     * @param readOnly if {@code true} items can't be added and removed
     */
    void setCardinalityReadOnly(boolean readOnly);

    /**
     * Determines if items can be added or removed.
     *
     * @return {@code true} if items can't be added or removed.
     */
    boolean isCardinalityReadOnly();

    /**
     * Sets a listener to be notified when an object is created.
     *
     * @param listener the listener. May be {@code null}
     */
    void setCreationListener(IMObjectCreationListener listener);

    /**
     * Returns the listener to be notified when an object is created.
     *
     * @return the listener, or {@code null} if none is registered
     */
    IMObjectCreationListener getCreationListener();

    /**
     * Creates a new object.
     * <p/>
     * The object is not automatically added to the collection.
     * <p/>
     * If an {@link IMObjectCreationListener} is registered, it will be
     * notified on successful creation of an object.
     *
     * @return a new object, or {@code null} if the object can't be created
     */
    IMObject create();

    /**
     * Refreshes the collection display.
     */
    void refresh();

    /**
     * Returns an editor for an object, creating one if it doesn't exist.
     *
     * @param object the object to edit
     * @return an editor for the object
     */
    IMObjectEditor getEditor(IMObject object);

    /**
     * Returns the current editor.
     *
     * @return the current editor. May be {@code null}
     */
    IMObjectEditor getCurrentEditor();

    /**
     * Returns editors for items in the collection.
     * <p/>
     * These include any editors that have been created for objects in the
     * collection, and the current editor, which may be for an uncommitted object.
     * <p/>
     * If an object hasn't been edited, it may not have a corresponding editor.
     *
     * @return editors for items in the collection and editors for items not yet committed to the collection
     */
    Collection<IMObjectEditor> getEditors();
}
