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

import java.util.Date;

/**
 * .
 *
 * @author Tim Anderson
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Treatment {

    /**
     * Describes the type of the object transferred with the SFS events (e.g. treatment.record_entered). Should be assigned treatment value.
     */
    private String objectType;

    /**
     * Inventory item external id (which was provided with inventory upload). If “Null” then there is no inventory item for this treatment found in SFS.
     */
    private String inventoryId;

    /**
     * Name of the treatment parameter as it is shown on the flowsheet.
     */
    private String name;

    /**
     * External id (which was provided with hospitalization creation).
     */
    private String hospitalizationId;

    /**
     * A unique internal identifier of the treatment item (which corresponds to the treatment at particular hour on a flowsheet
     */
    private String treatmentGuid;

    /**
     * Treatment time (UTC time that corresponds to an hour on a flowsheet). Time format: YYYY-MM-DDThh:mm:ss.sssTZD (e.g. 1997-07-16T19:20:30.000+00:00)
     */
    private Date time;

    /**
     * This field describes what have happened to the medical record. Can be one of the following: 1. changed, 2. added, 3. removed, 4. not_changed.
     */
    private String status;

    /**
     * Quantity. In case if the medication has been given to a patient this value will be equal to the volume field (described below). Otherwise it will be 1.
     */
    private double qty;

    /**
     * The volume of the medication that has been given to a patient. In case of non-medication treatment this value will not be provided.
     */
    private double volume;

    /**
     * The units of the medication volume (ml, tab, etc). For non-medication items this value will not be provided.
     */
    private String units;

    /**
     * The string value that was entered during treatment execution.
     */
    private String value;

    /**
     * The path to the media file that has been attached to the treatment.
     */
    private String mediaPath;

    /**
     * The content type of media file (e.g. image/jpg, video/mp4, etc…)
     */
    private String mediaContentType;

    /**
     * The name of the doctor on duty. This value will be provided only in case if the name of the doctor is specified on a correspondent flowsheet.
     */
    private String doctorName;

    /**
     * The medic object that corresponds to the doctor on duty. This value will be provided only in case if associated medic has been imported from EMR.
     */
    private Medic doctor;

    /**
     * The type of the treatment, should be one of following types: 0 - Flowsheet treatment, 1 - Anesthetic treatment, 2 - Treatment task.
     */
    private int type;

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(String inventoryId) {
        this.inventoryId = inventoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHospitalizationId() {
        return hospitalizationId;
    }

    public void setHospitalizationId(String hospitalizationId) {
        this.hospitalizationId = hospitalizationId;
    }

    public String getTreatmentGuid() {
        return treatmentGuid;
    }

    public void setTreatmentGuid(String treatmentGuid) {
        this.treatmentGuid = treatmentGuid;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getQty() {
        return qty;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMediaPath() {
        return mediaPath;
    }

    public void setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath;
    }

    public String getMediaContentType() {
        return mediaContentType;
    }

    public void setMediaContentType(String mediaContentType) {
        this.mediaContentType = mediaContentType;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public Medic getDoctor() {
        return doctor;
    }

    public void setDoctor(Medic doctor) {
        this.doctor = doctor;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
