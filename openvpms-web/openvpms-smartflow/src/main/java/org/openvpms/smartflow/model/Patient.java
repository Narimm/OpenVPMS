package org.openvpms.smartflow.model;

import java.util.Date;

/**
 * Smart Flow Sheet patient.
 *
 * @author Tim Anderson
 */
public class Patient {

    /**
     * Describes the type of the object transferred with the SFS events. Should be assigned patient value. Optional.
     */
    private String objectType = "patient";

    /**
     * EMR internal ID of the patient. Optional.
     */
    private String patientId;

    /**
     * The name of the patient. Required.
     */
    private String name;

    /**
     * Patient`s birthday. Time format: YYYY-MM-DDThh:mm:ss.sssTZD (e.g. 1997-07-16T19:20:30.000+00:00). Optional.
     */
    private Date birthday;

    /**
     * Patient’s sex type. There is a set of standard predefined strings that specify the sex type of a
     * patient: M, F, MN, FS. Required.
     */
    private String sex;

    /**
     * Patient’s species. Optional.
     */
    private String species;

    /**
     * Patient’s color. Optional.
     */
    private String color;

    /**
     * Patient’s breed. Optional.
     */
    private String breed;

    /**
     * The value of the critical notes that will be shown on a flowsheet. Optional.
     */
    private String criticalNotes;

    /**
     * The value of the custom field that will be shown on a flowsheet. Optional.
     */
    private String customField;

    /**
     * The path to the patient`s image file. Optional.
     */
    private String imagePath;

    /**
     * The patient owner. Required when creating new hospitalization. Optional if used to update existing
     * hospitalization.
     */
    private Client owner;

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getCriticalNotes() {
        return criticalNotes;
    }

    public void setCriticalNotes(String criticalNotes) {
        this.criticalNotes = criticalNotes;
    }

    public String getCustomField() {
        return customField;
    }

    public void setCustomField(String customField) {
        this.customField = customField;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Client getOwner() {
        return owner;
    }

    public void setOwner(Client owner) {
        this.owner = owner;
    }
}
