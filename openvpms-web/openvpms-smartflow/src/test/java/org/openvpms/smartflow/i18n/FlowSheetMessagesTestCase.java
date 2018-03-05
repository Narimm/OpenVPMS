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

import org.junit.Before;
import org.junit.Test;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.i18n.Message;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link FlowSheetMessages} class.
 *
 * @author Tim Anderson
 */
public class FlowSheetMessagesTestCase {

    /**
     * Test patient.
     */
    private Party patient;

    /**
     * Test location.
     */
    private Party location;

    /**
     * Sets up the test.
     */
    @Before
    public void setUp() {
        patient = new Party();
        patient.setName("Fido");
        patient.setId(10);

        location = new Party();
        location.setName("Main Clinic");
        location.setId(15);
    }

    /**
     * Tests error messages.
     */
    @Test
    public void testErrorMessages() {
        check(FlowSheetMessages.failedToGetHospitalization(patient),
              "SFS-0100: Failed to get hospitalization for Fido");
        check(FlowSheetMessages.failedToCreateFlowSheet(patient, "foo"),
              "SFS-0101: Failed to create Flow Sheet for Fido\n\nfoo");
        check(FlowSheetMessages.failedToDownloadPDF(patient, "foo.pdf"),
              "SFS-0102: Failed to download foo.pdf PDF for Fido");
        check(FlowSheetMessages.notAuthorised(), "SFS-0103: Failed to connect to Smart Flow Sheet.\n" +
                                                 "\n" +
                                                 "Check that the Smart Flow Sheet Clinic API Key is correct.");
        check(FlowSheetMessages.cannotConnectUsingSSL("https://foo.com"),
              "SFS-0104: Failed to connect to Smart Flow Sheet.\n" +
              "\n" +
              "Ensure that the required SSL certificates have been installed.");
        check(FlowSheetMessages.failedToGetTemplates(), "SFS-0105: Failed to retrieve treatment templates.\n" +
                                                        "\n" +
                                                        "Check that Smart Flow Sheet is configured correctly.");
        check(FlowSheetMessages.failedToGetDepartments(), "SFS-0106: Failed to retrieve departments.\n" +
                                                          "\n" +
                                                          "Check that Smart Flow Sheet is configured correctly.");
        check(FlowSheetMessages.failedToGetInventory(), "SFS-0107: Failed to retrieve inventory items.\n" +
                                                        "\n" +
                                                        "Check that Smart Flow Sheet is configured correctly.");
        check(FlowSheetMessages.failedToUpdateInventory(), "SFS-0108: Failed to update inventory items.\n" +
                                                           "\n" +
                                                           "Check that Smart Flow Sheet is configured correctly.");
        check(FlowSheetMessages.failedToRemoveInventoryItem("1", "foo"),
              "SFS-0109: Failed to remove inventory item 'foo' (1)");
        check(FlowSheetMessages.failedToGetMedics(), "SFS-0110: Failed to retrieve medics.\n" +
                                                     "\n" +
                                                     "Check that Smart Flow Sheet is configured correctly.");
        check(FlowSheetMessages.failedToUpdateMedics(), "SFS-0111: Failed to update medics.\n" +
                                                        "\n" +
                                                        "Check that Smart Flow Sheet is configured correctly.");
        check(FlowSheetMessages.failedToRemoveMedic("2", "foo"), "SFS-0112: Failed to remove medic 'foo' (2)");
        check(FlowSheetMessages.failedToGetServiceBusConfig(),
              "SFS-0113: Failed to retrieve Azure Service Bus configuration from Smart Flow Sheet.\n" +
              "\n" +
              "Check that Smart Flow Sheet is configured correctly.");
        check(FlowSheetMessages.failedToDeserializeMessage("1", "foo", "bar"),
              "SFS-0114: Failed to get message from Azure Service Bus with MessageID=1, ContentType=foo: bar");
        check(FlowSheetMessages.notConfigured(location),
              "SFS-0115: Smart Flow Sheet is not configured at Main Clinic.");
        check(FlowSheetMessages.failedToDischargePatient(patient, "foo"),
              "SFS-0116: Failed to discharge Fido from Smart Flow Sheet: foo");
        check(FlowSheetMessages.failedToGetAnaesthetics(patient),
              "SFS-0117: Failed to get anaesthetics for Fido");
        check(FlowSheetMessages.accessToDocumentDenied("foo", "bar"),
              "SFS-0118: Failed to download foo\n\nbar");
        check(FlowSheetMessages.unsupportedTimeZone("foo"),
              "SFS-0119: Smart Flow Sheet does not support the system time zone: foo");
    }

    /**
     * Tests informational messages.
     */
    @Test
    public void testInfoMessages() {
        assertEquals(FlowSheetMessages.cannotDeleteFinalisedNote(),
                     "This note was deleted in Smart Flow Sheet but cannot be removed as it is locked");
    }

    /**
     * Verifies a message matches that expected.
     *
     * @param message  the message
     * @param expected the expected message
     */
    private void check(Message message, String expected) {
        assertEquals(expected, message.toString());
    }
}
