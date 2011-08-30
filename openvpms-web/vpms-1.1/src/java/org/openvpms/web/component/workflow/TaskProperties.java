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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Task properties.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TaskProperties {

    /**
     * The properties.
     */
    private Map<String, TaskProperty> properties
            = new HashMap<String, TaskProperty>();

    /**
     * Adds a property.
     *
     * @param property the property to add
     */
    public void add(TaskProperty property) {
        properties.put(property.getName(), property);
    }

    /**
     * Adds a constant property.
     *
     * @param name  the property name
     * @param value the property value
     */
    public void add(String name, Object value) {
        add(new Constant(name, value));
    }

    /**
     * Returns the named property.
     *
     * @param name the property name
     * @return the property corresponding to <code>name</code> or
     *         <code>null</code> if it doesn't exist
     */
    public TaskProperty get(String name) {
        return properties.get(name);
    }

    /**
     * Returns the properties.
     *
     * @return the properties
     */
    public Collection<TaskProperty> getProperties() {
        return properties.values();
    }
}
