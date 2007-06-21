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

package org.openvpms.report;

/**
 * Report parameter type.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ParameterType {

    private final String name;

    private final Class type;

    private final String description;

    private final boolean system;

    public ParameterType(String name, Class type, String description) {
        this(name, type, description, false);
    }

    public ParameterType(String name, Class type, String description,
                         boolean system) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.system = system;
    }

    public String getName() {
        return name;
    }

    public Class getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSystem() {
        return system;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj
     *         argument; <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ParameterType) {
            return name.equals(((ParameterType) obj).name);
        }
        return false;
    }

}
