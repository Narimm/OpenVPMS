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
     * The status of the asynchronous operation for the medic object. This will be filled in by SFS when sending the
     * medics.imported event. Should be: 1. less than 0 - error occured; 2. greater or equal 0 - operation succeed.
     */
    private Integer asyncOperationStatus;

    /**
     * May contain the error message in case the asyncOperationStatus field represents the error (less than 0).
     */
    private String asyncOperationMessage;

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
     * Returns the status of the asynchronous operation for the inventory item. This will be filled in by SFS when
     * sending the inventoryitems.imported event. Should be: 1. less than 0 - error occrured; 2. greater or
     * equal 0 - operation succeed.
     *
     * @return the status. May be {@code null}
     */
    public Integer getAsyncOperationStatus() {
        return asyncOperationStatus;
    }

    /**
     * Sets the status of the asynchronous operation for the medic. This will be filled in by SFS when
     * sending the medics.imported event. Should be: 1. less than 0 - error occurred; 2. greater or
     * equal 0 - operation succeed.
     *
     * @param asyncOperationStatus the status. May be {@code null}
     */
    public void setAsyncOperationStatus(Integer asyncOperationStatus) {
        this.asyncOperationStatus = asyncOperationStatus;
    }

    /**
     * Returns the error message in case the asyncOperationStatus field represents an error (less than 0).
     *
     * @return the error message. May be {@code null}
     */
    public String getAsyncOperationMessage() {
        return asyncOperationMessage;
    }

    /**
     * Sets the error message in case the asyncOperationStatus field represents an error (less than 0).
     *
     * @param asyncOperationMessage the error message. May be {@code null}
     */
    public void setAsyncOperationMessage(String asyncOperationMessage) {
        this.asyncOperationMessage = asyncOperationMessage;
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
