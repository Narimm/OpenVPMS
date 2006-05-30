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
import org.openvpms.web.component.im.util.IMObjectHelper;

import org.openvpms.component.business.domain.im.common.Act;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
     * Clinical event item short names.
     */
    private String[] _clinicalEventItems;

    /**
     * Construct a new <code>VisitRecordShortNames</code>.
     */
    public ProblemRecordShortNames() {
        _clinicalEventItems = ActHelper.getTargetShortNames(
                RELATIONSHIP_CLINICAL_EVENT_ITEM);
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
        boolean isEventItem = false;
        boolean isEvent = false;
        boolean isEpisode = false;
        List<String> names = new ArrayList<String>();
        if (IMObjectHelper.isA(_act, _clinicalEventItems)) {
            isEventItem = true;
        } else if (IMObjectHelper.isA(_act, CLINICAL_EVENT)) {
            isEvent = true;
        } else if (IMObjectHelper.isA(_act, CLINICAL_EPISODE)) {
            isEpisode = true;
        }
        if (isEpisode) {
            // check to see if there is a child event to add items to
            Act event = ActHelper.getActOrChild(_act, CLINICAL_EVENT);
            if (event != null) {
                isEvent = true;
            }
        }
        if (isEvent || isEventItem) {
            names.addAll(Arrays.asList(_clinicalEventItems));
        }
        return names.toArray(new String[0]);
    }

}
