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

    /**
     * The parameter name.
     */
    private final String name;

    /**
     * The parameter type.
     */
    private final Class type;

    /**
     * The parameter description.
     */
    private final String description;

    /**
     * Determines if the parameter is a system or user property.
     * User properties should be set interactively.
     */
    private final boolean system;

    /**
     * The default value. May be <tt>null</tt>
     */
    private final Object defaultValue;


    /**
     * Constructs a new <tt>ParameterType</tt>.
     *
     * @param name        the parameter name
     * @param type        the parameter type
     * @param description the parameter description. May be <tt>null</tt>
     */
    public ParameterType(String name, Class type, String description) {
        this(name, type, description, null);
    }

    /**
     * Constructs a new <tt>ParameterType</tt>.
     *
     * @param name         the parameter name
     * @param type         the parameter type
     * @param description  the parameter description. May be <tt>null</tt>
     * @param defaultValue the default parameter value. May be <tt>null</tt>
     */
    public ParameterType(String name, Class type, String description, Object defaultValue) {
        this(name, type, description, false, defaultValue);
    }

    /**
     * Constructs a new <tt>ParameterType</tt>.
     *
     * @param name         the parameter name
     * @param type         the parameter type
     * @param description  the parameter description. May be <tt>null</tt>
     * @param system       if <tt>true</tt> denotes a system supplied parameter
     * @param defaultValue the default parameter value. May be <tt>null</tt>
     */
    public ParameterType(String name, Class type, String description, boolean system, Object defaultValue) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.system = system;
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the parameter name.
     *
     * @return the parameter name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the parameter type.
     *
     * @return the parameter type
     */
    public Class getType() {
        return type;
    }

    /**
     * Returns the parameter description.
     *
     * @return the parameter description. May be <tt>null</tt>
     */
    public String getDescription() {
        return description;
    }

    /**
     * Determines if the parameter is a system or user property.
     * System properties are typically populated without user interaction.
     *
     * @return <tt>true</tt> if the parameter is a system parameter;
     *         <tt>false</tt> if it is a user parameter
     */
    public boolean isSystem() {
        return system;
    }

    /**
     * Returns the default value of the parameter.
     *
     * @return the default value. May be <tt>null</tt>
     */
    public Object getDefaultValue() {
        return defaultValue;
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
