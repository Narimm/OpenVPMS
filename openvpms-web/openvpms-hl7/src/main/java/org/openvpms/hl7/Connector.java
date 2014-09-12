package org.openvpms.hl7;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class Connector {

    /**
     * The sending application.
     */
    private String sendingApplication;

    /**
     * The sending facility.
     */
    private String sendingFacility;

    /**
     * The receiving application.
     */
    private String receivingApplication;

    /**
     * The receiving facility.
     */
    private String receivingFacility;


    /**
     * Returns the sending application.
     *
     * @return the sending application
     */
    public String getSendingApplication() {
        return sendingApplication;
    }

    /**
     * Sets the sending application.
     *
     * @param sendingApplication the sending application
     */
    public void setSendingApplication(String sendingApplication) {
        this.sendingApplication = sendingApplication;
    }

    /**
     * Returns the sending facility.
     *
     * @return the sending facility
     */
    public String getSendingFacility() {
        return sendingFacility;
    }

    public void setSendingFacility(String sendingFacility) {
        this.sendingFacility = sendingFacility;
    }

    /**
     * Returns the receiving application.
     *
     * @return the receiving application
     */
    public String getReceivingApplication() {
        return receivingApplication;
    }

    /**
     * Sets the receiving application.
     *
     * @param receivingApplication the receiving application
     */
    public void setReceivingApplication(String receivingApplication) {
        this.receivingApplication = receivingApplication;
    }

    /**
     * Returns the receiving facility.
     *
     * @return the receiving facility
     */
    public String getReceivingFacility() {
        return receivingFacility;
    }

    /**
     * Sets the receiving facility.
     *
     * @param receivingFacility the receiving facility
     */
    public void setReceivingFacility(String receivingFacility) {
        this.receivingFacility = receivingFacility;
    }


}