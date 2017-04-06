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
public class Medic {

    /**
     * Describes the type of the object transferred with the SFS events. Should be assigned medic value.
     * Optional.
     */
    private String objectType = "medic";

    /**
     * The EMR internal ID of the medic.
     */
    private String medicId;

    /**
     * The unique name of the medic for a particular medicType.
     */
    private String name;

    /**
     * The type of the medic imported in SFS. Should always be assigned the doctor value.
     */
    private String medicType = "doctor";

    /**
     * Returns the object type.
     *
     * @return the object type
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * Returns the medic identifier.
     *
     * @return the medic identifier
     */
    public String getMedicId() {
        return medicId;
    }

    /**
     * Sets the medic identifier.
     *
     * @param medicId the medic identifier.
     */
    public void setMedicId(String medicId) {
        this.medicId = medicId;
    }

    /**
     * Returns the medic name.
     *
     * @return the medic name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the medic name.
     *
     * @param name the medic name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the medic type.
     *
     * @return the medic type
     */
    public String getMedicType() {
        return medicType;
    }

    /**
     * Sets the medic type.
     *
     * @param medicType the medic type
     */
    public void setMedicType(String medicType) {
        this.medicType = medicType;
    }

    /**
     * Returns the medic name.
     *
     * @return the medic name
     */
    @Override
    public String toString() {
        return name;
    }
}
