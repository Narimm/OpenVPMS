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
 * Smart Flow Sheet hospitalisation record.
 *
 * @author Tim Anderson
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Hospitalization {

    /**
     * Status indicating the hospitalization is active.
     */
    public static final String ACTIVE_STATUS = "active";

    /**
     * Describes the type of the object transferred with the SFS events. Should be assigned hospitalization value.
     * Optional.
     */
    private String objectType = "hospitalization";

    /**
     * EMR internal ID of the hospitalization. Required.
     */
    private String hospitalizationId;

    /**
     * Id of the department patient should be created at. If not specified hospitalization will be created in
     * SFS default department. Optional.
     */
    private int departmentId;

    /**
     * A unique internal identifier of the hospitalization. This field will be transferred with the SFS events.
     * Optional.
     */
    private String hospitalizationGuid;

    /**
     * Specifies the date and time of the patient arrival in the hospital.
     * Time format: YYYY-MM-DDThh:mm:ss.sssTZD (e.g. 1997-07-16T19:20:30.000+00:00). Required.
     */
    private Date dateCreated;

    /**
     * The name of the treatment template to be used for created hospitalization. If not specified then the
     * “Default” template will be used. Optional.
     */
    private String treatmentTemplateName;

    /**
     * Units for the temperature. Can be F or C. If not specified then clinic’s default temperature units will
     * be used. Optional.
     */
    private String temperatureUnits;

    /**
     * Units for the weight. Can be kg or lbs. If not specified then clinic’s default weight units will be used.
     * Optional.
     */
    private String weightUnits;

    /**
     * Weight of the patient. Smart Flow Sheet requires weight to be specified for every patient from the moment
     * hospitalization is created. Required.
     */
    private double weight;

    /**
     * This value will be used to create requested number of days of hospitalization. If this value is not specified
     * then by default 2 days will be created. Optional.
     */
    private int estimatedDaysOfStay;

    /**
     * The value of emr file number, that will be shown on a flowsheet. Optional.
     */
    private String fileNumber;

    /**
     * Whether to show caution stripe on a flowsheet or not. Optional.
     */
    private boolean caution;

    /**
     * The name of the doctor on duty. Optional.
     */
    private String doctorName;

    /**
     * Alternatively to specifying the doctorName field, you can provide the id of the medic object that corresponds to
     * the doctor on duty, and has been registered with the appropriate API call. Optional.
     */
    private String medicId;

    /**
     * A collection of diseases. Optional.
     */
    private String[] diseases;

    /**
     * The cage number. This value will be shown on the whiteboard near patient name. Optional
     */
    private String cageNumber;

    /**
     * The RGB hex color code (eg. #439FE0). This value is used to color patient`s info panel on the whiteboard and
     * flowsheets. Optional
     */
    private String color;

    /**
     * The path to the flowsheet report file that has been generated during patient discharge. Optional.
     */
    private String reportPath;

    /**
     * The status of the hospitalization. This field will be transferred with the SFS events. Can be active, deleted or
     * discharged.
     */
    private String status;

    /**
     * The patient. Required when creating new hospitalizations.
     */
    private Patient patient;

    /**
     * Returns the object type.
     *
     * @return the object type
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * Sets the object type.
     *
     * @param objectType the object type. Should be {@code "hospitalization"}
     */
    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    /**
     * Returns the EMR internal ID of the hospitalization.
     * <p/>
     * This corresponds to the <em>act.patientClinicialEvent</em> identifier.
     *
     * @return the EMR internal ID of the hospitalization
     */
    public String getHospitalizationId() {
        return hospitalizationId;
    }

    /**
     * Sets the the EMR internal ID of the hospitalization.
     *
     * @param hospitalizationId the identifier
     */
    public void setHospitalizationId(String hospitalizationId) {
        this.hospitalizationId = hospitalizationId;
    }

    /**
     * Returns the Id of the department patient should be created at. If not specified hospitalization will be created
     * in SFS default department.
     *
     * @return the department id
     */
    public int getDepartmentId() {
        return departmentId;
    }

    /**
     * Sets the Id of the department patient should be created at. If not specified hospitalization will be created
     * in SFS default department.
     *
     * @param departmentId the department id
     */
    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    /**
     * Returns a unique internal identifier of the hospitalization. This field will be transferred with the SFS events.
     *
     * @return the hospitalization GUID. May be {@code null}
     */
    public String getHospitalizationGuid() {
        return hospitalizationGuid;
    }

    /**
     * Sets a unique internal identifier of the hospitalization. This field will be transferred with the SFS events.
     *
     * @param hospitalizationGuid the hospitalization GUID. May be {@code null}
     */
    public void setHospitalizationGuid(String hospitalizationGuid) {
        this.hospitalizationGuid = hospitalizationGuid;
    }

    /**
     * Returns the date and time of the patient arrival in the hospital.
     * <p/>
     * In OpenVPMS this corresponds to the visit start time.
     *
     * @return the arrival date/time
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     * Sets the date and time of the patient arrival in the hospital.
     *
     * @param dateCreated the arrival date/time
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * The name of the treatment template to be used for created hospitalization. If not specified then the
     * “Default” template will be used.
     *
     * @return the treatment template name. May be {@code null}
     */
    public String getTreatmentTemplateName() {
        return treatmentTemplateName;
    }

    /**
     * The name of the treatment template to be used for created hospitalization. If not specified then the
     * “Default” template will be used.
     *
     * @param treatmentTemplateName the treatment template name. May be {@code null}
     */
    public void setTreatmentTemplateName(String treatmentTemplateName) {
        this.treatmentTemplateName = treatmentTemplateName;
    }

    /**
     * Returns the units for the temperature. Can be F or C. If not specified then clinic’s default temperature units
     * will be used.
     *
     * @return the temperature units. May be {@code null}
     */
    public String getTemperatureUnits() {
        return temperatureUnits;
    }

    /**
     * Sets the units for the temperature. Can be F or C. If not specified then clinic’s default temperature units
     * will be used.
     *
     * @param temperatureUnits the temperature units. May be {@code null}
     */
    public void setTemperatureUnits(String temperatureUnits) {
        this.temperatureUnits = temperatureUnits;
    }

    /**
     * Returns the units for the weight. Can be kg or lbs. If not specified then clinic’s default weight units will be
     * used.
     *
     * @return the weight units. May be {@code null}
     */
    public String getWeightUnits() {
        return weightUnits;
    }

    /**
     * Sets the units for the weight. Can be kg or lbs. If not specified then clinic’s default weight units will be used.
     *
     * @param weightUnits the weight units. May be {@code null}
     */
    public void setWeightUnits(String weightUnits) {
        this.weightUnits = weightUnits;
    }

    /**
     * Returns the weight of the patient. Smart Flow Sheet requires weight to be specified for every patient from the
     * moment hospitalization is created.
     *
     * @return the patient weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Returns the weight of the patient. Smart Flow Sheet requires weight to be specified for every patient from the
     * moment hospitalization is created.
     *
     * @param weight the patient weight
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * Returns the estimated days of stay. This value will be used to create requested number of days of
     * hospitalization. If this value is not specified then by default 2 days will be created.
     *
     * @return the estimated days of stay
     */
    public int getEstimatedDaysOfStay() {
        return estimatedDaysOfStay;
    }

    /**
     * Sets the estimated days of stay. This value will be used to create requested number of days of
     * hospitalization. If this value is not specified then by default 2 days will be created.
     *
     * @param estimatedDaysOfStay the estimated days of stay
     */
    public void setEstimatedDaysOfStay(int estimatedDaysOfStay) {
        this.estimatedDaysOfStay = estimatedDaysOfStay;
    }

    /**
     * Returns the value of EMR file number, that will be shown on a flowsheet.
     *
     * @return the file number. May be {@code null}
     */
    public String getFileNumber() {
        return fileNumber;
    }

    /**
     * Sets the value of EMR file number, that will be shown on a flowsheet.
     *
     * @param fileNumber the file number. May be {@code null}
     */
    public void setFileNumber(String fileNumber) {
        this.fileNumber = fileNumber;
    }

    /**
     * Determines whether to show a caution stripe on a flowsheet or not.
     *
     * @return {@code true} if a caution stripe should be displayed
     */
    public boolean getCaution() {
        return caution;
    }

    /**
     * Sets whether to show a caution stripe on a flowsheet or not.
     *
     * @param caution if {@code true}, a caution stripe should be displayed
     */
    public void setCaution(boolean caution) {
        this.caution = caution;
    }

    /**
     * Returns the name of the doctor on duty.
     *
     * @return the doctor name. May be {@code null}
     */
    public String getDoctorName() {
        return doctorName;
    }

    /**
     * Sets the name of the doctor on duty.
     *
     * @param doctorName the doctor name. May be {@code null}
     */
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    /**
     * The id of the medic object that corresponds to the doctor on duty, and has been registered with the
     * appropriate API call.
     *
     * @return the medic id. May be {@code null}
     */
    public String getMedicId() {
        return medicId;
    }

    /**
     * Sets the id of the medic object that corresponds to the doctor on duty, and has been registered with the
     * appropriate API call.
     *
     * @param medicId the medic id. May be {@code null}
     */
    public void setMedicId(String medicId) {
        this.medicId = medicId;
    }

    /**
     * Returns the patient diseases.
     *
     * @return the diseases
     */
    public String[] getDiseases() {
        return diseases;
    }

    /**
     * Sets the patient diseases.
     *
     * @param diseases the patient diseases
     */
    public void setDiseases(String[] diseases) {
        this.diseases = diseases;
    }

    /**
     * Returns the cage number. This value will be shown on the whiteboard near patient name.
     *
     * @return the cage number. May be {@code null}
     */
    public String getCageNumber() {
        return cageNumber;
    }

    /**
     * Sets the cage number. This value will be shown on the whiteboard near patient name.
     *
     * @param cageNumber the cage number. May be {@code null}
     */
    public void setCageNumber(String cageNumber) {
        this.cageNumber = cageNumber;
    }

    /**
     * Returns the RGB hex color code (eg. #439FE0). This value is used to color patient`s info panel on the whiteboard
     * and flowsheets.
     *
     * @return the colour. May be {@code null}
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the RGB hex color code (eg. #439FE0). This value is used to color patient`s info panel on the whiteboard
     * and flowsheets.
     *
     * @param color the colour. May be {@code null}
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Returns the path to the flowsheet report file that has been generated during patient discharge.
     *
     * @return the report path. May be {@code null}
     */
    public String getReportPath() {
        return reportPath;
    }

    /**
     * Sets the path to the flowsheet report file that has been generated during patient discharge.
     *
     * @param reportPath the report path. May be {@code null}
     */
    public void setReportPath(String reportPath) {
        this.reportPath = reportPath;
    }

    /**
     * Returns the status of the hospitalization. This field will be transferred with the SFS events. Can be active,
     * deleted or discharged.
     *
     * @return the status of the hospitalization
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the hospitalization. This field will be transferred with the SFS events. Can be active,
     * deleted or discharged.
     *
     * @param status the status of the hospitalization
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the patient.
     *
     * @return the patient
     */
    public Patient getPatient() {
        return patient;
    }

    /**
     * Sets the patient.
     *
     * @param patient the patient
     */
    public void setPatient(Patient patient) {
        this.patient = patient;
    }
}
