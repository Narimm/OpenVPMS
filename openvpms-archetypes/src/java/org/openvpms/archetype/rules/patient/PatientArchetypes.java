/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.patient;


/**
 * Patient archetypes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientArchetypes {

    /**
     * Patient archetype.
     */
    public static final String PATIENT = "party.patientpet";

    /**
     * Patient medication act short name.
     */
    public static final String PATIENT_MEDICATION = "act.patientMedication";
    
    /**
     * Patient participation archetype.
     */
    public static final String PATIENT_PARTICIPATION = "participation.patient";

    /**
     * Patient owner relationship short name.
     */
    public static final String PATIENT_OWNER
            = "entityRelationship.patientOwner";

    /**
     * Patient weight act short name.
     */
    public static final String PATIENT_WEIGHT = "act.patientWeight";

}
