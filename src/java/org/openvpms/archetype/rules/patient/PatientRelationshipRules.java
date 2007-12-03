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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.patient;

import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Patient relationship rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientRelationshipRules {

    /**
     * Check the relationships for a patient, ensuring they are valid
     * and only one is active for each relationship type.
     */
    public static void checkRelationships(Party patient) {
        Map<String, EntityRelationship> currentActives
                = new HashMap<String, EntityRelationship>();
        EntityRelationship currentActive;

        // Loop through all the patient relationships.
        // . If one is new then assume it is the active and set the
        //   activeEndTime on any others.
        // . If more than 1 is new then set active to youngest
        //   activeStartTime.
        // . If no new relationship and more than one active relationship of
        //   same type then set the one with the youngest activeStartTime as
        //   active.
        for (EntityRelationship rel : patient.getEntityRelationships()) {
            if (rel.getActiveEndTime() == null) {
                String shortname = rel.getArchetypeId().getShortName();
                currentActive = currentActives.get(shortname);
                if (rel.isNew()) {
                    if (currentActive == null) {
                        currentActive = rel;
                    } else if (currentActive.isNew()) {
                        if (after(rel, currentActive)) {
                            deactivate(currentActive);
                            currentActive = rel;
                        } else {
                            deactivate(rel);
                        }
                    } else {
                        deactivate(currentActive);
                        currentActive = rel;
                    }
                } else {
                    if (currentActive == null) {
                        currentActive = rel;
                    } else if (currentActive.isNew()) {
                        deactivate(rel);
                    } else if (after(rel, currentActive)) {
                        deactivate(currentActive);
                        currentActive = rel;
                    } else if (after(currentActive, rel)) {
                        deactivate(rel);
                    }
                }
                currentActives.put(shortname, currentActive);
            }
        }
    }

    /**
     * Deactivates an relationship by setting its active end time.
     *
     * @param relationship the relationship
     */
    private static void deactivate(EntityRelationship relationship) {
        Date end = new Date(System.currentTimeMillis() - 1000);
        relationship.setActiveEndTime(end);
    }

    /**
     * Determines if the active start time of one relationship is after
     * another.
     *
     * @param r1 the first relationship
     * @param r2 the second relationship
     * @return <tt>true</tt> if r1.activeStartTime > r2.activeStartTime
     */
    private static boolean after(EntityRelationship r1,
                                 EntityRelationship r2) {
        return r1.getActiveStartTime().after(
                r2.getActiveStartTime());
    }
}
