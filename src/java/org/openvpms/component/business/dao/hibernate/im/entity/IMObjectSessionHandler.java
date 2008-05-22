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

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.hibernate.Session;
import org.openvpms.component.business.domain.im.common.IMObject;

import java.util.Set;


/**
 * Handles hibernate <tt>Session</tt> operations on {@link IMObject}s.
 * <p/>
 * In particular, it handles saving detached instances of {@link IMObject}
 * object graphs, using <tt>Session.merge()</tt>, propagating the identifiers
 * and versions of the merged objects to their originals after commit.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
interface IMObjectSessionHandler {

    /**
     * Saves an object.
     * <p/>
     * This makes any unsaved children persistent prior to invoking
     * <tt>Session.merge()</tt> on the supplied object.
     * <p/>
     * The <tt>newObjects</tt> argument is used to collect unsaved instances.
     * If the transaction rolls back, any identifiers assigned to these
     * must be reset, as hibernate doesn't do it automatically.
     *
     * @param object     the object to save
     * @param session    the session to use
     * @param newObjects used to collect new objects encountered during save
     * @return the result of <tt>Session.merge(object)</tt>
     */
    IMObject save(IMObject object, Session session, Set<IMObject> newObjects);

    /**
     * Updates the target object with the identifier and version of the source,
     * including any direct children.
     *
     * @param target the object to update
     * @param source the object to update from
     */
    void updateIds(IMObject target, IMObject source);

    /**
     * Deletes an object.
     *
     * @param object  the object to delete
     * @param session
     */
    void delete(IMObject object, Session session);
}
