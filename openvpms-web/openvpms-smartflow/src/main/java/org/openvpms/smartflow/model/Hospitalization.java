package org.openvpms.smartflow.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * Smart Flow Sheet hospitalisation record.
 *
 * @author Tim Anderson
 */
@XmlRootElement
public class Hospitalization {

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
     * Whether to show dnr stripe on a flowsheet or not. Optional.
     */
    private boolean dnr;

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

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getHospitalizationId() {
        return hospitalizationId;
    }

    public void setHospitalizationId(String hospitalizationId) {
        this.hospitalizationId = hospitalizationId;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public String getHospitalizationGuid() {
        return hospitalizationGuid;
    }

    public void setHospitalizationGuid(String hospitalizationGuid) {
        this.hospitalizationGuid = hospitalizationGuid;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getTreatmentTemplateName() {
        return treatmentTemplateName;
    }

    public void setTreatmentTemplateName(String treatmentTemplateName) {
        this.treatmentTemplateName = treatmentTemplateName;
    }

    public String getTemperatureUnits() {
        return temperatureUnits;
    }

    public void setTemperatureUnits(String temperatureUnits) {
        this.temperatureUnits = temperatureUnits;
    }

    public String getWeightUnits() {
        return weightUnits;
    }

    public void setWeightUnits(String weightUnits) {
        this.weightUnits = weightUnits;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getEstimatedDaysOfStay() {
        return estimatedDaysOfStay;
    }

    public void setEstimatedDaysOfStay(int estimatedDaysOfStay) {
        this.estimatedDaysOfStay = estimatedDaysOfStay;
    }

    public String getFileNumber() {
        return fileNumber;
    }

    public void setFileNumber(String fileNumber) {
        this.fileNumber = fileNumber;
    }

    public boolean getCaution() {
        return caution;
    }

    public void setCaution(boolean caution) {
        this.caution = caution;
    }

    public boolean getDnr() {
        return dnr;
    }

    public void setDnr(boolean dnr) {
        this.dnr = dnr;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getMedicId() {
        return medicId;
    }

    public void setMedicId(String medicId) {
        this.medicId = medicId;
    }

    public String[] getDiseases() {
        return diseases;
    }

    public void setDiseases(String[] diseases) {
        this.diseases = diseases;
    }

    public String getCageNumber() {
        return cageNumber;
    }

    public void setCageNumber(String cageNumber) {
        this.cageNumber = cageNumber;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getReportPath() {
        return reportPath;
    }

    public void setReportPath(String reportPath) {
        this.reportPath = reportPath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }
}
