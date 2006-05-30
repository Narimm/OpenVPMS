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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.patient.mr;


/**
 * Patient record short names.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface PatientRecordTypes {

    /**
     * Clinical episode act short name.
     */
    String CLINICAL_EPISODE = "act.patientClinicalEpisode";

    /**
     * Clinical event act short name.
     */
    String CLINICAL_EVENT = "act.patientClinicalEvent";

    /**
     * Clinical problem act short name.
     */
    String CLINICAL_PROBLEM = "act.patientClinicalProblem";

    /**
     * Clinical episode event act relationship short name.
     */
    String RELATIONSHIP_CLINICAL_EPISODE_EVENT
            = "actRelationship.patientClinicalEpisodeEvent";

    /**
     * Clinical event item act relationship short name,
     */
    String RELATIONSHIP_CLINICAL_EVENT_ITEM
            = "actRelationship.patientClinicalEventItem";

}
