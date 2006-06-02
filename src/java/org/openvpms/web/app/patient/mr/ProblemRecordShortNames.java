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

import static org.openvpms.web.app.patient.mr.PatientRecordTypes.*;
import org.openvpms.web.component.im.edit.act.ActHelper;
import org.openvpms.web.component.util.CollectionHelper;

import org.openvpms.archetype.util.TypeHelper;
import org.openvpms.component.business.domain.im.common.Act;


/**
 * Determines shortnames of acts that may be created in 'problem view'.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ProblemRecordShortNames implements RecordShortNames {

    /**
     * The act.
     */
    private Act _act;

    /**
     * Problem event item short names.
     */
    private String[] _problemEventItems;

    /**
     * Construct a new <code>VisitRecordShortNames</code>.
     */
    public ProblemRecordShortNames() {
        _problemEventItems = ActHelper.getTargetShortNames(
                RELATIONSHIP_CLINICAL_PROBLEM_ITEM);
    }


    /**
     * Sets the act to determine short names from.
     *
     * @param act the act. May be <code>null</code>
     */
    public void setAct(Act act) {
        _act = act;
    }

    /**
     * Returns the archetype short names.
     *
     * @return the archetype short names
     */
    public String[] getShortNames() {
        boolean isProblemItem = false;
        boolean isProblem = false;
        boolean isEvent = false;
        boolean isEpisode = false;
        String[] result = {};
        if (TypeHelper.isA(_act, _problemEventItems)) {
            isProblemItem = true;
        } else if (TypeHelper.isA(_act, CLINICAL_PROBLEM)) {
            isProblem = true;
        } else if (TypeHelper.isA(_act, CLINICAL_EVENT)) {
            isEvent = true;
        } else if (TypeHelper.isA(_act, CLINICAL_EPISODE)) {
            isEpisode = true;
        }
        if (isEpisode) {
            // check to see if there is a child event to add problems to
            Act event = ActHelper.getActOrChild(_act, CLINICAL_EVENT);
            if (event != null) {
                isEvent = true;
            }
        }
        if (isEvent) {
            // check to see if there is a child problem to add problem items to
            Act problem = ActHelper.getActOrChild(_act, CLINICAL_PROBLEM);
            if (problem != null) {
                isProblem = true;
            }
        }
        if (isEvent || isProblem || isProblemItem) {
            result = new String[]{CLINICAL_PROBLEM};
            if (isProblem || isProblemItem) {
                result = CollectionHelper.concat(result, _problemEventItems);
            }
        }
        return result;
    }

}
