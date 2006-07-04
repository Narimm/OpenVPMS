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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.AbstractIMObjectCopyHandler;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;


/**
 * Patient medical record rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MedicalRecordRules {

    /**
     * Adds a new <em>act.patientClinicalEvent</em>
     * to the New<em>act.patientClinicalEpisode</em> act passed.
     *
     * @param service the archetype service
     * @param act     the Episode act
     */
    public static void addNewEpisodeEvent(IArchetypeService service, Act act) {
        // Copy the Episode 
        IMObjectCopier copier = new IMObjectCopier(new MedicalEventCopyHandler());
        Act event = (Act) copier.copy(act);
        service.save(event);

        // Create a act relationship between the Episode and Event
        ActRelationship rel = (ActRelationship)service.create("actRelationship.patientClinicalEpisodeEvent");
        rel.setSource(act.getObjectReference());
        rel.setTarget(event.getObjectReference());
        act.addSourceActRelationship(rel);
        service.save(act);       
    }

    private static class MedicalEventCopyHandler extends AbstractIMObjectCopyHandler {

        /**
         * Map of invoice types to their corresponding credit types.
         */
        private static final String[][] TYPE_MAP = {
                {"act.patientClinicalEpisode", "act.patientClinicalEvent"}
        };

        /**
         * Determines how {@link IMObjectCopier} should treat an object.
         *
         * @param object  the source object
         * @param service the archetype service
         * @return <code>object</code> if the object shouldn't be copied,
         *         <code>null</code> if it should be replaced with
         *         <code>null</code>, or a new instance if the object should be
         *         copied
         */
        public IMObject getObject(IMObject object, IArchetypeService service) {
            IMObject result;
            if (object instanceof Act || object instanceof ActRelationship
                    || object instanceof Participation) {
                String shortName = object.getArchetypeId().getShortName();
                for (String[] map : TYPE_MAP) {
                    String episodeType = map[0];
                    String eventType = map[1];
                    if (episodeType.equals(shortName)) {
                        shortName = eventType;
                        break;
                    }
                }
                result = service.create(shortName);
                if (result == null) {
                    throw new ArchetypeServiceException(
                            ArchetypeServiceException.ErrorCode.FailedToCreateArchetype,
                            new String[]{shortName});
                }
            } else {
                result = object;
            }
            return result;
        }
    }

    /**
     * Recursively deletes children of <em>act.patientClinicalEpisode</em>
     * and <em>act.patientClinicalEvent</em> acts.
     *
     * @param service the archetype service
     * @param act     the deleted act
     */
    public static void deleteChildRecords(IArchetypeService service, Act act) {
        String[] shortNames = {"act.patientClinicalEpisode",
                               "act.patientClinicalEvent"};
        if (TypeHelper.isA(act, shortNames)) {
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
