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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.property;


/**
 * Interface to track the modified status of an object.
 *
 * @author Tim Anderson
 */
public interface Modifiable {

    /**
     * Determines if the object has been modified.
     *
     * @return {@code true} if the object has been modified
     */
    boolean isModified();

    /**
     * Clears the modified status of the object.
     */
    void clearModified();

    /**
     * Adds a listener to be notified when this changes.
     * <p/>
     * Listeners will be notified in the order they were registered.
     *
     * @param listener the listener to add
     */
    void addModifiableListener(ModifiableListener listener);

    /**
     * Adds a listener to be notified when this changes, specifying the order of the listener.
     *
     * @param listener the listener to add
     * @param index    the index to add the listener at. The 0-index listener is notified first
     */
    void addModifiableListener(ModifiableListener listener, int index);

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    void removeModifiableListener(ModifiableListener listener);

    /**
     * Adds a listener to be notified of errors.
     *
     * @param listener the listener to add
     */
    void addErrorListener(ErrorListener listener);

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    void removeErrorListener(ErrorListener listener);

    /**
     * Determines if the object is valid.
     *
     * @return {@code true} if the object is valid; otherwise {@code false}
     */
    boolean isValid();

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    boolean validate(Validator validator);

    /**
     * Resets the cached validity state of the object, to force revalidation of the object and its descendants.
     */
    void resetValid();

}
