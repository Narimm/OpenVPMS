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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7;

import org.openvpms.component.business.domain.im.common.IMObjectReference;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public abstract class Connector {

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

    /**
     * Returns the connector reference.
     *
     * @return the connector reference
     */
    public abstract IMObjectReference getReference();


}