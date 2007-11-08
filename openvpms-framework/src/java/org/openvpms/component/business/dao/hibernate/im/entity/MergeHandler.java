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


/**
 * Handles merging of {@link IMObject} object graphs using
 * <tt>Session.merge()</tt>, propagating the identifiers and versions of
 * the merged objects to their originals after commit.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
interface MergeHandler {

    /**
     * Merges an object.
     * <p>
     * This makes any transient children persistent prior to invoking
     * <tt>Session.merge()</tt> on the supplied object.
     *
     * @param object the object to merge
     * @param session the session to use
     * @return the result of <tt>Session.merge(object)</tt>
     */
    IMObject merge(IMObject object, Session session);

    /**
     * Updates the target object with the identifier and version of the source,
     * including any direct children.
     *
     * @param target the object to update
     * @param source the object to update from
     */
    void update(IMObject target, IMObject source);
}
