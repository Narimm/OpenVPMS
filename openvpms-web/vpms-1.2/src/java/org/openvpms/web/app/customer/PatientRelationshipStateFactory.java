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

package org.openvpms.web.app.customer;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.web.component.im.relationship.RelationshipState;
import org.openvpms.web.component.im.relationship.RelationshipStateFactory;


/**
 * Factory for {@link PatientRelationshipState} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class PatientRelationshipStateFactory extends RelationshipStateFactory {

    /**
     * Creates a new <tt>PatientRelationshipState</tt>.
     *
     * @param relationship the relationship
     * @param sourceUID    the source entity UID
     * @param sourceName   the source entity name
     * @param sourceDesc   the source entity description
     * @param targetUID    the target entity UID
     * @param targetName   the target entity name
     * @param targetDesc   the target entity description
     * @param active       determines the entities are active
     * @return a new <tt>PatientRelationshipState</tt>
     */
    @Override
    public RelationshipState create(EntityRelationship relationship,
                                    long sourceUID, String sourceName,
                                    String sourceDesc, long targetUID,
                                    String targetName, String targetDesc,
                                    boolean active) {
        return new PatientRelationshipState(relationship, sourceUID, sourceName,
                                            sourceDesc, targetUID, targetName,
                                            targetDesc, active);
    }

    /**
     * Creates a new <tt>PatientRelationshipState</tt>.
     *
     * @param entity       the parent entity
     * @param relationship the relationship
     * @param source       determines if entity is the source or target of the
     *                     relationship
     * @return a new <tt>PatientRelationshipState</tt>
     * @throws ArchetypeServiceException for any archetype service exception
     */
    @Override
    public RelationshipState create(Entity entity,
                                    EntityRelationship relationship,
                                    boolean source) {
        return new PatientRelationshipState(entity, relationship, source);
    }
}
