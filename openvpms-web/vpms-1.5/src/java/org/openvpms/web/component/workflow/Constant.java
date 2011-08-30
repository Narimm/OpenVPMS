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


/**
 * Constant property.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Constant implements TaskProperty {

    /**
     * The property name.
     */
    private final String name;

    /**
     * The property value.
     */
    private final Object value;


    /**
     * Creates a new <code>SimpleProperty</code>.
     *
     * @param name  the property name
     * @param value the property value
     */
    public Constant(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Returns the property name.
     *
     * @return the property name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the property value.
     *
     * @param context the task context
     * @return the property value
     */
    public Object getValue(TaskContext context) {
        return value;
    }
}
