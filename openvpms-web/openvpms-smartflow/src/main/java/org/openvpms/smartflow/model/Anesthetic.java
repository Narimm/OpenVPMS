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
 * Smart Flow Sheet anaesthetic treatment.
 *
 * @author Tim Anderson
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Anesthetic {

    /**
     * Describes the type of the object transferred with the SFS API. Should be anesthetic.
     */
    private String objectType = "anesthetic";

    /**
     * EMR internal ID of the hospitalization.
     */
    private String hospitalizationId;

    /**
     * A unique internal identifier of the surgery. This field will be transferred with the SFS events.
     */
    private String surgeryGuid;

    /**
     * Optional. Anesthesia start time. Time format: YYYY-MM-DDThh:mm:ss.sssTZD (e.g. 1997-07-16T19:20:30.000+00:00).
     */
    private Date dateStarted;

    /**
     * Optional. Anesthesia end time. Time format: YYYY-MM-DDThh:mm:ss.sssTZD (e.g. 1997-07-16T19:20:30.000+00:00)
     */
    private Date dateEnded;

    /**
     * Optional. The path to the anesthetic sheet report file that has been generated during finalization of the
     * anesthetic sheet.
     */
    private String reportPath;

    /**
     * The path to the anesthetic records report file that has been generated during finalization of the anesthetic
     * sheet.
     */
    private String recordsReportPath;

    /**
     * Optional. The medic object that corresponds to the doctor assigned to the anesthetic sheet.
     */
    private Medic surgeon;

    /**
     * Optional. The medic object that corresponds to the anesthetist assigned to the anesthetic sheet.
     */
    private Medic anesthetist;

    /**
     * Optional. The medic object that corresponds to the assistant assigned to the anesthetic sheet.
     */
    private Medic assistant;

    /**
     * Returns the object type.
     *
     * @return the object type
     */
    public String getObjectType() {
        return objectType;
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
     * Returns the unique internal identifier of the surgery. This field will be transferred with the SFS events.
     *
     * @return the unique internal identifier of the surgery
     */
    public String getSurgeryGuid() {
        return surgeryGuid;
    }

    /**
     * Sets the unique internal identifier of the surgery. This field will be transferred with the SFS events.
     *
     * @param surgeryGuid the unique internal identifier of the surgery
     */
    public void setSurgeryGuid(String surgeryGuid) {
        this.surgeryGuid = surgeryGuid;
    }

    /**
     * Returns the anesthesia start time.
     *
     * @return the start time. May be {@code null}
     */
    public Date getDateStarted() {
        return dateStarted;
    }

    /**
     * Sets the anesthesia start time.
     *
     * @param dateStarted the start time. May be {@code null}
     */
    public void setDateStarted(Date dateStarted) {
        this.dateStarted = dateStarted;
    }

    /**
     * Returns the anesthesia end time.
     *
     * @return the anesthesia end time. May be {@code null}
     */
    public Date getDateEnded() {
        return dateEnded;
    }

    /**
     * Sets the anesthesia end time.
     *
     * @param dateEnded the end time. May be {@code null}
     */
    public void setDateEnded(Date dateEnded) {
        this.dateEnded = dateEnded;
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
     * Returns the path to the anesthetic records report file that has been generated during finalization of the
     * anesthetic sheet.
     *
     * @return the records report path
     */
    public String getRecordsReportPath() {
        return recordsReportPath;
    }

    /**
     * Sets the path to the anesthetic records report file that has been generated during finalization of the
     * anesthetic sheet.
     *
     * @param recordsReportPath the records report path
     */
    public void setRecordsReportPath(String recordsReportPath) {
        this.recordsReportPath = recordsReportPath;
    }

    /**
     * Returns the medic object that corresponds to the doctor assigned to the anesthetic sheet.
     *
     * @return the surgeon. May be {@code null}
     */
    public Medic getSurgeon() {
        return surgeon;
    }

    /**
     * Sets the surgeon.
     *
     * @param surgeon the surgeon. May be {@code null}
     */
    public void setSurgeon(Medic surgeon) {
        this.surgeon = surgeon;
    }

    /**
     * Returns the medic object that corresponds to the anesthetist assigned to the anesthetic sheet.
     *
     * @return the anesthetist. May be {@code null}
     */
    public Medic getAnesthetist() {
        return anesthetist;
    }

    /**
     * Sets the medic object that corresponds to the anesthetist assigned to the anesthetic sheet.
     *
     * @param anesthetist the anesthetist. May be {@code null}
     */
    public void setAnesthetist(Medic anesthetist) {
        this.anesthetist = anesthetist;
    }

    /**
     * Returns the medic object that corresponds to the assistant assigned to the anesthetic sheet.
     *
     * @return the assistant. May be {@code null}
     */
    public Medic getAssistant() {
        return assistant;
    }

    /**
     * Sets the medic object that corresponds to the assistant assigned to the anesthetic sheet.
     *
     * @param assistant the assistant. May be {@code null}
     */
    public void setAssistant(Medic assistant) {
        this.assistant = assistant;
    }
}
