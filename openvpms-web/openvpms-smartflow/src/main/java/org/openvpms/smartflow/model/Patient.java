package org.openvpms.smartflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

/**
 * Smart Flow Sheet patient.
 *
 * @author Tim Anderson
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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
     * @param objectType the object type. Should be {@code "patient"}
     */
    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    /**
     * Returns the patient identifier.
     *
     * @return the patient identifier
     */
    public String getPatientId() {
        return patientId;
    }

    /**
     * Sets the patient identifier.
     *
     * @param patientId the patient identifier
     */
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    /**
     * Returns the patient name.
     *
     * @return the patient name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the patient name.
     *
     * @param name the patient name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the patient birthday.
     *
     * @return the patient birthday
     */
    public Date getBirthday() {
        return birthday;
    }

    /**
     * Sets the patient birthday.
     *
     * @param birthday the patient birthday
     */
    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    /**
     * Returns the patient sex.
     *
     * @return the patient sex. One of M, F, MN, FS
     */
    public String getSex() {
        return sex;
    }

    /**
     * Sets the patient sex.
     *
     * @param sex the patient sex. One of M, F, MN, FS
     */
    public void setSex(String sex) {
        this.sex = sex;
    }

    /**
     * Returns the patient species.
     *
     * @return the patient species. May be {@code null}
     */
    public String getSpecies() {
        return species;
    }

    /**
     * Sets the patient species.
     *
     * @param species the patient species. May be {@code null}
     */
    public void setSpecies(String species) {
        this.species = species;
    }

    /**
     * Returns the patient’s color.
     *
     * @return the patient’s color. May be {@code null}
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the patient’s color.
     *
     * @param color the patient’s color. May be {@code null}
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Returns the patient breed.
     *
     * @return the patient breed. May be {@code null}
     */
    public String getBreed() {
        return breed;
    }

    /**
     * Sets the patient breed.
     *
     * @param breed the patient breed. May be {@code null}
     */
    public void setBreed(String breed) {
        this.breed = breed;
    }

    /**
     * Returns the critical notes that will be shown on a flowsheet.
     *
     * @return the critical notes. May be {@code null}
     */
    public String getCriticalNotes() {
        return criticalNotes;
    }

    /**
     * Sets he critical notes that will be shown on a flowsheet.
     *
     * @param criticalNotes the critical notes. May be {@code null}
     */
    public void setCriticalNotes(String criticalNotes) {
        this.criticalNotes = criticalNotes;
    }

    /**
     * Returns the value of the custom field that will be shown on a flowsheet.
     *
     * @return the custom field. May be {@code null}
     */
    public String getCustomField() {
        return customField;
    }

    /**
     * Sets the value of the custom field that will be shown on a flowsheet.
     *
     * @param customField the custom field. May be {@code null}
     */
    public void setCustomField(String customField) {
        this.customField = customField;
    }

    /**
     * Return the path to the patient`s image file.
     *
     * @return the image file path. May be {@code null}
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * Sets the path to the patient`s image file.
     *
     * @param imagePath the image file path. May be {@code null}
     */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    /**
     * Returns the patient owner.
     *
     * @return the patient owner.
     */
    public Client getOwner() {
        return owner;
    }

    /**
     * Sets the patient owner.
     *
     * @param owner the owner. Required when creating new hospitalization. Optional if used to update existing
     *              hospitalization.
     */
    public void setOwner(Client owner) {
        this.owner = owner;
    }
}
