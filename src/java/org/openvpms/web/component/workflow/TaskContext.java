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

package org.openvpms.web.component.workflow;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.Context;


/**
 * Workflow task context.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface TaskContext {

    /**
     * Adds an object to the context.
     *
     * @param object the object to add
     */
    void addObject(IMObject object);

    /**
     * Adds an object to the context.
     *
     * @param object the object to add
     * @param global if <code>true</code> update the global {@link Context}
     *               as well
     */
    void addObject(IMObject object, boolean global);

    /**
     * Retrieves the object with the specified short name from the context.
     *
     * @param shortName the short name
     * @return the object corresponding to <code>shortName</code>;
     *         otherwise <code>null</code>
     */
    IMObject getObject(String shortName);

    /**
     * Retrieves the object with the specified short name from the context.
     *
     * @param shortName the short name
     * @param global    if <code>true</code> and the object is not found in the
     *                  local context, looks in the global context
     * @return the object corresponding to <code>shortName</code>;
     *         otherwise <code>null</code>
     */
    IMObject getObject(String shortName, boolean global);

}
