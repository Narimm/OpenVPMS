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

/**
 * HL7 archetypes.
 *
 * @author Tim Anderson
 */
public class HL7Archetypes {

    /**
     * Patient event service archetype short name.
     */
    public static String PATIENT_EVENT_SERVICE = "entity.HL7ServicePatientEvent";

    /**
     * Pharmacy archetype short name.
     */
    public static String PHARMACY = "entity.HL7ServicePharmacy";

    /**
     * Pharmacy group archetype short name.
     */
    public static String PHARMACY_GROUP = "entity.HL7ServicePharmacyGroup";

    /**
     * HL7 MLLP sender archetype short name.
     */
    public static String MLLP_SENDER = "entity.HL7SenderMLLP";

    /**
     * HL7 MLLP receiver archetype short name.
     */
    public static String MLLP_RECEIVER = "entity.HL7ReceiverMLLP";

    public static String SENDERS = "entity.HL7Sender*";

    public static String RECEIVERS = "entity.HL7Receiver*";

    public static String SERVICES = "entity.HL7Service*";

}
