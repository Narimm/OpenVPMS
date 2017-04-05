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

package org.openvpms.smartflow.i18n;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.i18n.Message;
import org.openvpms.component.system.common.i18n.Messages;

/**
 * Messages reported by the Smart Flow Sheet interface.
 *
 * @author Tim Anderson
 */
public class FlowSheetMessages {

    /**
     * The messages.
     */
    private static Messages messages = new Messages("SFS", FlowSheetMessages.class.getName());

    /**
     * Creates a message indicating that a hospitalization couuldn't be retrieved for a patient.
     *
     * @param patient the patient
     * @return a new message
     */
    public static Message failedToGetHospitalization(Party patient) {
        return messages.getMessage(100, patient.getName());

    }

    /**
     * Creates a message indicating that a flow sheet couldn't be created for a patient.
     *
     * @param patient the patient
     * @return a new message
     */
    public static Message failedToCreateFlowSheet(Party patient) {
        return messages.getMessage(101, patient.getName());
    }

    /**
     * Creates a message when the departments can't be returned.
     *
     * @return a new message
     */
    public static Message failedToGetDepartments() {
        return messages.getMessage(106);
    }

    /**
     * Creates a message when the treatment templates can't be returned.
     *
     * @return a new message
     */
    public static Message failedToGetTemplates() {
        return messages.getMessage(105);
    }

    /**
     * Creates a message when the inventory items can't be returned.
     *
     * @return a new message
     */
    public static Message failedToGetInventory() {
        return messages.getMessage(107);
    }

    /**
     * Creates a message when the inventory items can't be updated.
     *
     * @return a new message
     */
    public static Message failedToUpdateInventory() {
        return messages.getMessage(108);
    }

    /**
     * Creates a message when an inventory items can't be removed.
     *
     * @return a new message
     */
    public static Message failedToRemoveInventoryItem(String id, String name) {
        return messages.getMessage(109, id, name);
    }

    /**
     * Creates a message indicating that a PDF couldn't be downloaded for a patient.
     *
     * @param patient the patient
     * @param name    the pdf name
     * @return a new message
     */
    public static Message failedToDownloadPDF(Party patient, String name) {
        return messages.getMessage(102, patient.getName(), name);
    }

    /**
     * Creates a message indicating that an operation has failed through lack of authorisation.
     *
     * @return a new message
     */
    public static Message notAuthorised() {
        return messages.getMessage(103);
    }

    /**
     * Creates a message indicating that SSL handing shaking has failed.
     * <p/>
     * This typically indicates that the Start Com certification authority has not been imported into cacerts.
     *
     * @param url the url being connected to
     * @return a new message
     */
    public static Message cannotConnectUsingSSL(String url) {
        return messages.getMessage(104, url);
    }

}
