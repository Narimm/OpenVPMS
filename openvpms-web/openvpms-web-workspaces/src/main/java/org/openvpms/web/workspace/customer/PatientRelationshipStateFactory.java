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

package org.openvpms.web.workspace.customer;

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.im.relationship.RelationshipState;
import org.openvpms.web.component.im.relationship.RelationshipStateFactory;


/**
 * Factory for {@link PatientRelationshipState} instances.
 *
 * @author Tim Anderson
 */
class PatientRelationshipStateFactory extends RelationshipStateFactory {

    /**
     * Creates a new {@link PatientRelationshipState}.
     *
     * @param parent       the parent object
     * @param relationship the relationship
     * @param sourceId     the source entity Id
     * @param sourceName   the source entity name
     * @param sourceDesc   the source entity description
     * @param targetId     the target entity Id
     * @param targetName   the target entity name
     * @param targetDesc   the target entity description
     * @param active       determines the entities are active
     * @param set          the set
     * @return a new {@link PatientRelationshipState}
     */
    @Override
    public RelationshipState create(IMObject parent, IMObjectRelationship relationship,
                                    long sourceId, String sourceName,
                                    String sourceDesc, long targetId,
                                    String targetName, String targetDesc,
                                    boolean active, ObjectSet set) {
        boolean deceased;
        if (TypeHelper.isA(parent, PatientArchetypes.PATIENT)) {
            IMObjectBean bean = new IMObjectBean(parent);
            deceased = bean.getBoolean("deceased");
        } else {
            deceased = set.exists("patient.deceased") && set.getBoolean("patient.deceased");
        }
        return new PatientRelationshipState((EntityRelationship) relationship, sourceId, sourceName, sourceDesc,
                                            targetId, targetName, targetDesc, active, deceased);
    }

    /**
     * Creates a new {@link PatientRelationshipState}.
     *
     * @param parent       the parent entity
     * @param relationship the relationship
     * @param source       determines if parent is the source or target of the
     *                     relationship
     * @return a new {@link PatientRelationshipState}
     * @throws ArchetypeServiceException for any archetype service exception
     */
    @Override
    public RelationshipState create(IMObject parent,
                                    IMObjectRelationship relationship,
                                    boolean source) {
        return new PatientRelationshipState((Entity) parent, (EntityRelationship) relationship, source);
    }
}
