package org.openvpms.smartflow.model;

/**
 * Smart Flow Sheet patient owner.
 *
 * @author Tim Anderson
 */
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

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getNameLast() {
        return nameLast;
    }

    public void setNameLast(String nameLast) {
        this.nameLast = nameLast;
    }

    public String getNameFirst() {
        return nameFirst;
    }

    public void setNameFirst(String nameFirst) {
        this.nameFirst = nameFirst;
    }

    public String getHomePhone() {
        return homePhone;
    }

    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }

    public String getWorkPhone() {
        return workPhone;
    }

    public void setWorkPhone(String workPhone) {
        this.workPhone = workPhone;
    }
}
