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

package org.openvpms.plugin.service.archetype;

import org.openvpms.component.model.object.IMObject;

/**
 * Listener for {@link IMObject} changes.
 *
 * @author Tim Anderson
 */
public interface IMObjectListener {

    /**
     * The archetypes to receive notifications for.
     *
     * @return the archetypes
     */
    String[] getArchetypes();

    /**
     * Invoked when an object is updated.
     *
     * @param object the object
     */
    void updated(IMObject object);

    /**
     * invoked when an object is removed.
     *
     * @param object the object
     */
    void removed(IMObject object);
}
