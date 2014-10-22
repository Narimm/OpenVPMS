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

package org.openvpms.hl7.util;

/**
 * HL7 archetypes.
 *
 * @author Tim Anderson
 */
public class HL7Archetypes {

    /**
     * The HL7 service archetype short names.
     */
    public static final String SERVICES = "entity.HL7Service*";

    /**
     * Patient event service archetype short name.
     */
    public static final String PATIENT_EVENT_SERVICE = "entity.HL7ServicePatientEvent";

    /**
     * Pharmacy archetype short name.
     */
    public static final String PHARMACY = "entity.HL7ServicePharmacy";

    /**
     * Pharmacy group archetype short name.
     */
    public static final String PHARMACY_GROUP = "entity.HL7ServicePharmacyGroup";

    /**
     * HL7 MLLP sender archetype short name.
     */
    public static final String MLLP_SENDER = "entity.HL7SenderMLLP";

    /**
     * HL7 MLLP receiver archetype short name.
     */
    public static final String MLLP_RECEIVER = "entity.HL7ReceiverMLLP";

    /**
     * The send connection archetype short names.
     */
    public static final String SENDERS = "entity.HL7Sender*";

    /**
     * The receive connection archetype short names.
     */
    public static final String RECEIVERS = "entity.HL7Receiver*";

    /**
     * The connection archetype short names.
     */
    public static final String[] CONNECTIONS = {SENDERS, RECEIVERS};

    /**
     * HL7 message archetype short name.
     */
    public static final String MESSAGE = "act.HL7Message";
}
