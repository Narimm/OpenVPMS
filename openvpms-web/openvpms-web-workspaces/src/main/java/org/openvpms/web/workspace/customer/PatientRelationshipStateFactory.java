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

package org.openvpms.web.workspace.customer;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
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
     * @param sourceId     the source entity Id
     * @param sourceName   the source entity name
     * @param sourceDesc   the source entity description
     * @param targetId     the target entity Id
     * @param targetName   the target entity name
     * @param targetDesc   the target entity description
     * @param active       determines the entities are active
     * @return a new <tt>PatientRelationshipState</tt>
     */
    @Override
    public RelationshipState create(IMObjectRelationship relationship,
                                    long sourceId, String sourceName,
                                    String sourceDesc, long targetId,
                                    String targetName, String targetDesc,
                                    boolean active) {
        return new PatientRelationshipState((EntityRelationship) relationship,
                                            sourceId, sourceName,
                                            sourceDesc, targetId, targetName,
                                            targetDesc, active);
    }

    /**
     * Creates a new <tt>PatientRelationshipState</tt>.
     *
     * @param parent       the parent entity
     * @param relationship the relationship
     * @param source       determines if parent is the source or target of the
     *                     relationship
     * @return a new <tt>PatientRelationshipState</tt>
     * @throws ArchetypeServiceException for any archetype service exception
     */
    @Override
    public RelationshipState create(IMObject parent,
                                    IMObjectRelationship relationship,
                                    boolean source) {
        return new PatientRelationshipState((Entity) parent,
                                            (EntityRelationship) relationship,
                                            source);
    }
}
