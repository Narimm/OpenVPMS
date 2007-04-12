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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.load;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;


/**
 * Resolves objects.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface ObjectHandler {

    /**
     * Indicates start of a load.
     */
    void begin();

    /**
     * Commits any unsaved objects.
     *
     * @throws LoaderException           for any loader exception
     * @throws ArchetypeServiceException for any archetyype service error
     */
    void commit();

    /**
     * Indicates end of a load.
     *
     * @throws LoaderException           for any loader exception
     * @throws ArchetypeServiceException for any archetyype service error
     */
    void end();

    /**
     * Rolls back any unsaved objects.
     */
    void rollback();

    /**
     * Dispose of the handler.
     */
    void close();

    /**
     * Adds a new object. The object is considered incomplete until
     * {@link #commit()} is invoked.
     *
     * @param legacyId the source legacy identifier
     * @param object   the object
     * @param index    the object's  collection index, or <tt>-1</tt> if it
     *                 doesn't belong to a collection
     */
    void add(String legacyId, IMObject object, int index);

    /**
     * Invoked when an error occurs.
     *
     * @param legacyId  the identifier of the row that triggered the error
     * @param exception the exception
     */
    void error(String legacyId, Throwable exception);

    /**
     * Gets an object, given a string reference.
     *
     * @param reference the reference
     * @return the object corresponding to <tt>reference</tt>
     * @throws LoaderException           for any loader exception
     * @throws ArchetypeServiceException for any archetyype service error
     */
    IMObject getObject(String reference);

    /**
     * Gets an object reference, given a string reference.
     *
     * @param reference the reference
     * @return the object reference corresponding to <tt>reference</tt>
     * @throws LoaderException           for any loader exception
     * @throws ArchetypeServiceException for any archetyype service error
     */
    IMObjectReference getReference(String reference);

    /**
     * Sets a listener to be notified of errors.
     *
     * @param listener the listener. May be <tt>null</tt>
     */
    void setErrorListener(ErrorListener listener);
}
