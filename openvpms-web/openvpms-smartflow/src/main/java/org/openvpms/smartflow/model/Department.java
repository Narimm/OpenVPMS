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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.smartflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Smart Flow Sheet department.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Department {

    /**
     * Describes the type of the object transferred with the SFS events. Should be assigned department value.
     * Optional.
     */
    private String objectType = "department";

    /**
     * The ID of the department. You should use this value to specify the department when creating a new
     * hospitalization.
     */
    private int departmentId;

    /**
     * The name of the department.
     */
    private String name;

    /**
     * Returns the object type.
     *
     * @return the object type
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * Returns the department identifier.
     *
     * @return the department identifier
     */
    public int getDepartmentId() {
        return departmentId;
    }

    /**
     * Sets the department identifier.
     *
     * @param departmentId department identifier.
     */
    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    /**
     * Returns the department name.
     *
     * @return the department name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the department name.
     *
     * @param name the department name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the department name.
     *
     * @return the department name
     */
    @Override
    public String toString() {
        return name;
    }
}
