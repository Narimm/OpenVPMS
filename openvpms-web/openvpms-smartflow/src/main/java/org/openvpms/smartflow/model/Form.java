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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.smartflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Smart Flow Sheet anaesthetic treatment.
 *
 * @author Tim Anderson
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Form {

    /**
     * Describes the type of the object transferred with the SFS API. Should be form.
     */
    private String objectType = "form";

    /**
     * A unique internal identifier of the form.
     */
    private String formGuid;

    /**
     * EMR internal ID of the hospitalization.
     */
    private String hospitalizationId;

    /**
     * A unique identifier of the form type. This value is specified on the Settings/Forms web page in the Internal
     * Name attribute of the form.
     */
    private String name;

    /**
     * Determines if the form has been deleted by the user.
     */
    private boolean deleted;

    /**
     * Determines if the form has been finalized by the user.
     * If the patient is created from the form, then this field will return true.
     */
    private boolean finalized;

    /**
     * Optional. The path to the form pdf report file that has been generated during form finalization.
     */
    private String reportPath;

    /**
     * The  title of the form (e.g. “Admission form”).
     */
    private String title;


    /**
     * Returns the object type.
     *
     * @return the object type
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * Returns the form identifier.
     *
     * @return the form identifier
     */
    public String getFormGuid() {
        return formGuid;
    }

    /**
     * Sets the form identifier.
     *
     * @param formGuid the form identifier
     */
    public void setFormGuid(String formGuid) {
        this.formGuid = formGuid;
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
     * Returns the internal name for the form. This is a unique identifier.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the unique internal name for the form.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Determines if the form is deleted.
     *
     * @return {@code true} if the form is deleted
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Determines if the form is deleted.
     *
     * @param deleted if {@code true} the form is deleted
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * Determines if the form has been finalized by the user.
     *
     * @return {@code true} if the form has been finalized
     */
    public boolean isFinalized() {
        return finalized;
    }

    /**
     * Determines if the form has been finalized by the user.
     *
     * @param finalized if {@code true} the form has been finalized
     */
    public void setFinalized(boolean finalized) {
        this.finalized = finalized;
    }

    /**
     * Returns the path to the anesthetic sheet report file that has been generated during finalization of the
     * anesthetic sheet.
     *
     * @return the report path
     */
    public String getReportPath() {
        return reportPath;
    }

    /**
     * Sets the path to the anesthetic sheet report file that has been generated during finalization of the
     * anesthetic sheet.
     *
     * @param reportPath the report path
     */
    public void setReportPath(String reportPath) {
        this.reportPath = reportPath;
    }

    /**
     * Returns the form title.
     *
     * @return the form title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the form title.
     *
     * @param title the form title
     */
    public void setTitle(String title) {
        this.title = title;
    }

}
