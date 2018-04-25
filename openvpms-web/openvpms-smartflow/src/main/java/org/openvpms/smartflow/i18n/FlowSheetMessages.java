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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.smartflow.i18n;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.i18n.Message;
import org.openvpms.component.i18n.Messages;

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
        return messages.create(100, patient.getName());
    }

    /**
     * Creates a message indicating that a flow sheet couldn't be created for a patient.
     *
     * @param patient the patient
     * @param message the cause of the failure
     * @return a new message
     */
    public static Message failedToCreateFlowSheet(Party patient, String message) {
        return messages.create(101, patient.getName(), message);
    }

    /**
     * Creates a message indicating that a PDF couldn't be downloaded for a patient.
     *
     * @param patient the patient
     * @param name    the pdf name
     * @return a new message
     */
    public static Message failedToDownloadPDF(Party patient, String name) {
        return messages.create(102, patient.getName(), name);
    }

    /**
     * Creates a message indicating that an operation has failed through lack of authorisation.
     *
     * @return a new message
     */
    public static Message notAuthorised() {
        return messages.create(103);
    }

    /**
     * Creates a message indicating that SSL handing shaking has failed.
     * <p>
     * This typically indicates that the Start Com certification authority has not been imported into cacerts.
     *
     * @param url the url being connected to
     * @return a new message
     */
    public static Message cannotConnectUsingSSL(String url) {
        return messages.create(104, url);
    }

    /**
     * Creates a message when the treatment templates can't be returned.
     *
     * @return a new message
     */
    public static Message failedToGetTemplates() {
        return messages.create(105);
    }

    /**
     * Creates a message when the departments can't be returned.
     *
     * @return a new message
     */
    public static Message failedToGetDepartments() {
        return messages.create(106);
    }

    /**
     * Creates a message when the inventory items can't be returned.
     *
     * @return a new message
     */
    public static Message failedToGetInventory() {
        return messages.create(107);
    }

    /**
     * Creates a message when the inventory items can't be updated.
     *
     * @return a new message
     */
    public static Message failedToUpdateInventory() {
        return messages.create(108);
    }

    /**
     * Creates a message when an inventory item can't be removed.
     *
     * @return a new message
     */
    public static Message failedToRemoveInventoryItem(String id, String name) {
        return messages.create(109, id, name);
    }

    /**
     * Creates a message when the medics can't be returned.
     *
     * @return a new message
     */
    public static Message failedToGetMedics() {
        return messages.create(110);
    }

    /**
     * Creates a message when the medics can't be updated.
     *
     * @return a new message
     */
    public static Message failedToUpdateMedics() {
        return messages.create(111);
    }

    /**
     * Creates a message when a medic can't be removed.
     *
     * @param medicId the medic identifier
     * @param name    the medic name
     * @return a new message
     */
    public static Message failedToRemoveMedic(String medicId, String name) {
        return messages.create(112, medicId, name);
    }

    /**
     * Creates a message when the Azure Service Bus configuration can't be retrieved.
     *
     * @return a new message
     */
    public static Message failedToGetServiceBusConfig() {
        return messages.create(113);
    }

    /**
     * Creates a message indicating that a Azure Service Bus message couldn't be deserialized
     *
     * @param messageId   the message identifier
     * @param contentType the content type
     * @param reason      the reason
     * @return a new message
     */
    public static Message failedToDeserializeMessage(String messageId, String contentType, String reason) {
        return messages.create(114, messageId, contentType, reason);
    }

    /**
     * Creates a message indicating that Smart Flow Sheet is not configured at a practice location.
     *
     * @param location the practice location
     * @return a new message
     */
    public static Message notConfigured(Party location) {
        return messages.create(115, location.getName());
    }

    /**
     * Creates a message when a patient cannot be discharged.
     *
     * @param patient the patient
     * @param reason  the reason
     * @return a new message
     */
    public static Message failedToDischargePatient(Party patient, String reason) {
        return messages.create(116, patient.getName(), reason);
    }

    /**
     * Creates a message indicating that anaesthetics couldn't be retrieved for a patient.
     *
     * @param patient the patient
     * @return a new message
     */
    public static Message failedToGetAnaesthetics(Party patient) {
        return messages.create(117, patient.getName());
    }

    /**
     * Creates a message indicating that access to documents have been denied by documents being turned off on the
     * SFS Settings / Documents management page.
     *
     * @param name    the document name
     * @param message the SFS error message
     * @return a new message
     */
    public static Message accessToDocumentDenied(String name, String message) {
        return messages.create(118, name, message);
    }

    /**
     * Creates a message indicating the Smart Flow Sheet does not support the specified time zone.
     *
     * @param id the time zone identifier
     * @return a new message
     */
    public static Message unsupportedTimeZone(String id) {
        return messages.create(119, id);
    }

    /**
     * Creates a message indicating that forms couldn't be retrieved for a patient.
     *
     * @param patient the patient
     * @return a new message
     */
    public static Message failedToGetForms(Party patient) {
        return messages.create(120, patient.getName());
    }

    /**
     * Returns a message indicating that a clinical note cannot be deleted as it has been locked.
     *
     * @return the message
     */
    public static String cannotDeleteFinalisedNote() {
        return messages.create(300).getMessage();
    }

    /**
     * Returns the file name to use for Smart Flow Flow Sheet report attachments.
     *
     * @return the file name, minus any extension
     */
    public static String reportFileName(String name) {
        return messages.create(1000, name).getMessage();
    }

    /**
     * Returns the name to use for Flow Sheet report attachments.
     *
     * @return the name, minus any extension
     */
    public static String flowSheetReportName() {
        return messages.create(1001).getMessage();
    }

    /**
     * Returns the name to use for Medical Records report attachments.
     *
     * @return the name, minus any extension
     */
    public static String medicalRecordsReportName() {
        return messages.create(1002).getMessage();
    }

    /**
     * Returns the name to use for Billing report attachments.
     *
     * @return the name, minus any extension
     */
    public static String billingReportName() {
        return messages.create(1003).getMessage();
    }

    /**
     * Returns the name to use for Notes report attachments.
     *
     * @return the name, minus any extension
     */
    public static String notesReportName() {
        return messages.create(1004).getMessage();
    }

    /**
     * Returns the name to use for Anaesthetic report attachments.
     *
     * @return the name, minus any extension
     */
    public static String anaestheticReportName() {
        return messages.create(1005).getMessage();
    }

    /**
     * Returns the name to use for Anaesthetic Records report attachments.
     *
     * @return the name, minus any extension
     */
    public static String anaestheticRecordsReportName() {
        return messages.create(1006).getMessage();
    }
}
