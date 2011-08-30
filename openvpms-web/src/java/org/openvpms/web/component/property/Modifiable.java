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
 *  $Id:Modifiable.java 2147 2007-06-21 04:16:11Z tanderson $
 */

package org.openvpms.web.component.property;


/**
 * Interface to track the modified status of an object.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2007-06-21 04:16:11Z $
 */
public interface Modifiable {

    /**
     * Determines if the object has been modified.
     *
     * @return <tt>true</tt> if the object has been modified
     */
    boolean isModified();

    /**
     * Clears the modified status of the object.
     */
    void clearModified();

    /**
     * Adds a listener to be notified when this changes.
     *
     * @param listener the listener to add
     */
    void addModifiableListener(ModifiableListener listener);

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    void removeModifiableListener(ModifiableListener listener);

    /**
     * Determines if the object is valid.
     *
     * @return <tt>true</tt> if the object is valid; otherwise <tt>false</tt>
     */
    boolean isValid();

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the object and its descendants are valid otherwise <tt>false</tt>
     */
    boolean validate(Validator validator);

    /**
     * Resets the cached validity state of the object, to force revalidation of the object and its descendants.
     */
    void resetValid();

}
