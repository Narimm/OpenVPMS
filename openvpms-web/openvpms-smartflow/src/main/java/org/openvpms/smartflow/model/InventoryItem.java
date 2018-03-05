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

import java.math.BigDecimal;

/**
 * Smart Flow Sheet inventory item.
 *
 * @author Tim Anderson
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryItem {

    /**
     * Describes the type of the object transferred with the SFS events (e.g. inventory.imported). Should be assigned
     * inventoryitem value. Optional.
     */
    private String objectType = "inventoryitem";

    /**
     * The EMR internal ID of the inventory item.
     */
    private String id;

    /**
     * The unique name of the inventory item.
     */
    private String name;

    /**
     * The concentration value for the given medication. This value should not be specified if the inventory item is not
     * a medication. Optional.
     */
    private BigDecimal concentration;

    /**
     * Units that define the amount of drug. There is no limitation on what data will be transferred.
     * Required if the concentration value is specified. Otherwise optional.
     * This value should not be specified if inventory item is not a medication.
     */
    private String concentrationUnits;

    /**
     * Units for the volume. There is no limitation on what data will be transferred.
     * Required if the concentration value is specified. Otherwise optional. This value should not be specified if
     * inventory item is not a medication.
     */
    private String concentrationVolume;

    /**
     * The status of the asynchronous operation for the inventory item. This will be filled in by SFS when sending
     * the inventoryitems.imported event. Should be: 1. less than 0 - error occured; 2. greater or
     * equal 0 - operation succeed.
     * Optional.
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
     * Returns the EMR internal ID of the inventory item.
     *
     * @return the internal ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the EMR internal ID of the inventory item.
     *
     * @param id the internal ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the unique name of the inventory item.
     *
     * @return the inventory item name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the unique name of the inventory item.
     *
     * @param name the inventory item name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the concentration if the item is a medication.
     *
     * @return the concentration. May be {@code null}
     */
    public BigDecimal getConcentration() {
        return concentration;
    }

    /**
     * Sets the concentration if the item is a medication.
     *
     * @param concentration the concentration. May be {@code null}
     */
    public void setConcentration(BigDecimal concentration) {
        this.concentration = concentration;
    }

    /**
     * Returns the units that define the amount of a drug. Required if the concentration is specified.
     *
     * @return the concentration units. May be {@code null}
     */
    public String getConcentrationUnits() {
        return concentrationUnits;
    }

    /**
     * Sets the units that define the amount of a drug. Required if the concentration is specified.
     *
     * @param concentrationUnits the concentration units
     */
    public void setConcentrationUnits(String concentrationUnits) {
        this.concentrationUnits = concentrationUnits;
    }

    /**
     * Returns the units for the volume. Required if the concentration value is specified
     *
     * @return the concentration volume. May be {@code null}
     */
    public String getConcentrationVolume() {
        return concentrationVolume;
    }

    /**
     * Sets the units for the volume. Required if the concentration value is specified
     *
     * @param concentrationVolume the concentration volume. May be {@code null}
     */
    public void setConcentrationVolume(String concentrationVolume) {
        this.concentrationVolume = concentrationVolume;
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
     * Sets the status of the asynchronous operation for the inventory item. This will be filled in by SFS when
     * sending the inventoryitems.imported event. Should be: 1. less than 0 - error occurred; 2. greater or
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

}
