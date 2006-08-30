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

package org.openvpms.archetype.rules.patient;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;


/**
 * Patient medical record rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MedicalRecordRules {

    /**
     * Recursively deletes children of <em>act.patientClinicalEvent</em> acts.
     *
     * @param service the archetype service
     * @param act     the deleted act
     */
    public static void deleteChildRecords(IArchetypeService service, Act act) {
        if (TypeHelper.isA(act, "act.patientClinicalEvent")) {
            for (ActRelationship relationship :
                    act.getSourceActRelationships()) {
                Act child = get(service, relationship.getTarget());
                if (child != null) {
                    delete(service, child);
                }
            }
        }
    }

    /**
     * Recursively deletes an act heirarchy, from the top down.
     *
     * @param service the archetype service
     * @param act     the act to delete
     */
    private static void delete(IArchetypeService service, Act act) {
        service.remove(act);
        for (ActRelationship relationship :
                act.getSourceActRelationships()) {
            Act child = get(service, relationship.getTarget());
            if (child != null) {
                delete(service, child);
            }
        }
    }

    /**
     * Returns an act given its reference.
     *
     * @param service the archetype service
     * @param ref     a reference to the object
     * @return the object corresponding to <code>ref</code> or null if none
     *         is found
     */
    private static Act get(IArchetypeService service, IMObjectReference ref) {
        return (Act) ArchetypeQueryHelper.getByObjectReference(service, ref);
    }

}
