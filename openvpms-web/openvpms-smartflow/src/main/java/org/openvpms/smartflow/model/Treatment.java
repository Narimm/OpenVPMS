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
import java.util.Date;

/**
 * Smart Flow Sheet treatment.
 *
 * @author Tim Anderson
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Treatment {

    /**
     * Treatment added status.
     */
    public static final String ADDED_STATUS = "added";

    /**
     * Treatment changed status.
     */
    public static final String CHANGED_STATUS = "changed";

    /**
     * Treatment removed status.
     */
    public static final String REMOVED_STATUS = "removed";

    /**
     * Describes the type of the object transferred with the SFS events (e.g. treatment.record_entered). Should be
     * assigned treatment value.
     */
    private String objectType = "treatment";

    /**
     * Inventory item external id (which was provided with inventory upload). If “Null” then there is no inventory
     * item for this treatment found in SFS.
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
     * A unique internal identifier of the treatment item (which corresponds to the treatment at particular hour on a
     * flowsheet.
     */
    private String treatmentGuid;

    /**
     * Treatment time (UTC time that corresponds to an hour on a flowsheet). Time format: YYYY-MM-DDThh:mm:ss.sssTZD
     * (e.g. 1997-07-16T19:20:30.000+00:00)
     */
    private Date time;

    /**
     * This field describes what have happened to the medical record. Can be one of the following: 1. changed, 2. added,
     * 3. removed, 4. not_changed.
     */
    private String status;

    /**
     * Quantity. In case if the medication has been given to a patient this value will be equal to the volume field
     * (described below). Otherwise it will be 1.
     */
    private BigDecimal qty;

    /**
     * The volume of the medication that has been given to a patient. In case of non-medication treatment this value
     * will not be provided.
     */
    private BigDecimal volume;

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
     * The name of the doctor on duty. This value will be provided only in case if the name of the doctor is specified
     * on a correspondent flowsheet.
     */
    private String doctorName;

    /**
     * The medic object that corresponds to the doctor on duty. This value will be provided only in case if associated
     * medic has been imported from EMR.
     */
    private Medic doctor;

    /**
     * The type of the treatment, should be one of following types: 0 - Flowsheet treatment, 1 - Anesthetic treatment,
     * 2 - Treatment task.
     */
    private int type;

    /**
     * Determines if the treatment should be included in the billing.
     */
    private boolean billed;

    /**
     * Returns the object type.
     *
     * @return the object type
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * Returns the inventory item external id (which was provided with inventory upload).
     *
     * @return the inventory item external id
     */
    public String getInventoryId() {
        return inventoryId;
    }

    /**
     * Sets the inventory item external id.
     *
     * @param inventoryId the inventory item external id
     */
    public void setInventoryId(String inventoryId) {
        this.inventoryId = inventoryId;
    }

    /**
     * Returns the name of the treatment parameter as it is shown on the flowsheet.
     *
     * @return the name of the treatment parameter
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the treatment parameter.
     *
     * @param name the name of the treatment parameter
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the external hospitalization id (which was provided with hospitalization creation).
     *
     * @return the external hospitalization id
     */
    public String getHospitalizationId() {
        return hospitalizationId;
    }

    /**
     * Sets the external hospitalization id.
     *
     * @param hospitalizationId the external hospitalization id
     */
    public void setHospitalizationId(String hospitalizationId) {
        this.hospitalizationId = hospitalizationId;
    }

    /**
     * Returns a unique internal identifier of the treatment item (which corresponds to the treatment at particular hour
     * on a flowsheet.
     *
     * @return the treatment identifier
     */
    public String getTreatmentGuid() {
        return treatmentGuid;
    }

    /**
     * Sets the internal identifier of the treatment item.
     *
     * @param treatmentGuid the treatment identifier
     */
    public void setTreatmentGuid(String treatmentGuid) {
        this.treatmentGuid = treatmentGuid;
    }

    /**
     * Returns the treatment time.
     *
     * @return the treatment time
     */
    public Date getTime() {
        return time;
    }

    /**
     * Sets the treatment time.
     *
     * @param time the treatment time
     */
    public void setTime(Date time) {
        this.time = time;
    }

    /**
     * Returns the record status.
     *
     * @return the record status. One of added, changed, removed, not_changed
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the record status.
     *
     * @param status the status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the quantity. If a medication has been given to a patient this value will be equal to the volume field
     * otherwise it will be 1.
     *
     * @return the quantity
     */
    public BigDecimal getQty() {
        return qty;
    }

    /**
     * Sets the quantity.
     *
     * @param qty the quantity
     */
    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    /**
     * Returns the volume of the medication that has been given to a patient. In case of non-medication treatment this
     * value will not be provided.
     *
     * @return the volume. May be {@code null}
     */
    public BigDecimal getVolume() {
        return volume;
    }

    /**
     * Sets the volume.
     *
     * @param volume the volume. May be {@code null}
     */
    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    /**
     * Returns the units of the medication volume (ml, tab, etc). For non-medication items this value will not be
     * provided.
     *
     * @return the units. May be {@code null}
     */
    public String getUnits() {
        return units;
    }

    /**
     * Sets the units.  .
     *
     * @param units the units. May be {@code null}
     */
    public void setUnits(String units) {
        this.units = units;
    }

    /**
     * Returns the string value that was entered during treatment execution.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value that was entered during treatment execution.
     *
     * @param value the value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns the path to the media file that has been attached to the treatment.
     *
     * @return the path. May be {@code null}
     */
    public String getMediaPath() {
        return mediaPath;
    }

    /**
     * Sets the path to the media file that has been attached to the treatment.
     *
     * @param mediaPath the path. May be {@code null}
     */
    public void setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath;
    }

    /**
     * Returns the content type of the media file (e.g. image/jpg, video/mp4, etc…)
     *
     * @return the content type. May be {@code null}
     */
    public String getMediaContentType() {
        return mediaContentType;
    }

    /**
     * Sets the content type of the the media file.
     *
     * @param mediaContentType the content type. May be {@code null}
     */
    public void setMediaContentType(String mediaContentType) {
        this.mediaContentType = mediaContentType;
    }

    /**
     * Returns the name of the doctor on duty.
     *
     * @return the name of the doctor. May be {@code null}
     */
    public String getDoctorName() {
        return doctorName;
    }

    /**
     * Sets the name of the doctor on duty.
     *
     * @param doctorName the name of the doctor. May be {@code null}
     */
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    /**
     * Returns the doctor on duty.
     *
     * @return the doctor. May be {@code null}
     */
    public Medic getDoctor() {
        return doctor;
    }

    /**
     * Sets the doctor.
     *
     * @param doctor the doctor. May be {@code null}
     */
    public void setDoctor(Medic doctor) {
        this.doctor = doctor;
    }

    /**
     * Returns the type of the treatment, should be one of following types: 0 - Flowsheet treatment,
     * 1 - Anesthetic treatment, 2 - Treatment task.
     *
     * @return the type of treatment
     */
    public int getType() {
        return type;
    }

    /**
     * Sets the type of the treatment.
     *
     * @param type the type
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Determines if the treatment should be billed.
     *
     * @return {@code true }if the treatment should be billed
     */
    public boolean getBilled() {
        return billed;
    }

    /**
     * Determines if the treatment should be billed.
     *
     * @param billed if {@code true}, the treatment should be billed
     */
    public void setBilled(boolean billed) {
        this.billed = billed;
    }
}
