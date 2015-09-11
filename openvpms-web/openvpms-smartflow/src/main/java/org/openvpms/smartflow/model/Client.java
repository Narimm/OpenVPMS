package org.openvpms.smartflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Smart Flow Sheet patient owner.
 *
 * @author Tim Anderson
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Client {

    /**
     * Describes the type of the object transferred with the SFS events. Should be assigned client value. Optional.
     */
    private String objectType = "client";

    /**
     * EMR internal ID of the client. Optional.
     */
    private String ownerId;

    /**
     * Pet owner’s last name. Optional.
     */
    private String nameLast;

    /**
     * Pet owner’s first name. Optional.
     */
    private String nameFirst;

    /**
     * Pet owner’s home phone. Optional.
     */
    private String homePhone;

    /**
     * Optional. Pet owner’s work phone.
     */
    private String workPhone;

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
     * @param objectType the object type. Should be {@code "client"}
     */
    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    /**
     * Returns the owner identifier.
     *
     * @return the owner identifier
     */
    public String getOwnerId() {
        return ownerId;
    }

    /**
     * Sets the owner identifier.
     *
     * @param ownerId the owner identifier
     */
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * Returns the pet owner's last name.
     *
     * @return the pet owner’s last name. May be {@code null}
     */
    public String getNameLast() {
        return nameLast;
    }

    /**
     * Sets the pet owner's last name.
     *
     * @param nameLast the pet owner’s last name. May be {@code null}
     */
    public void setNameLast(String nameLast) {
        this.nameLast = nameLast;
    }

    /**
     * Returns the pet owner's first name.
     *
     * @return the pet owner’s first name. May be {@code null}
     */
    public String getNameFirst() {
        return nameFirst;
    }

    /**
     * Sets the pet owner's first name.
     *
     * @param nameFirst the pet owner’s first name. May be {@code null}
     */
    public void setNameFirst(String nameFirst) {
        this.nameFirst = nameFirst;
    }

    /**
     * Returns the pet owner’s home phone.
     *
     * @return the pet owner’s home phone. May be {@code null}
     */
    public String getHomePhone() {
        return homePhone;
    }

    /**
     * Sets the pet owner’s home phone.
     *
     * @param homePhone the pet owner’s home phone. May be {@code null}
     */
    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }

    /**
     * Returns the pet owner’s work phone.
     *
     * @return the pet owner’s work phone. May be {@code null}
     */
    public String getWorkPhone() {
        return workPhone;
    }

    /**
     * Sets the pet owner’s work phone.
     *
     * @param workPhone the pet owner’s work phone. May be {@code null}
     */
    public void setWorkPhone(String workPhone) {
        this.workPhone = workPhone;
    }
}
