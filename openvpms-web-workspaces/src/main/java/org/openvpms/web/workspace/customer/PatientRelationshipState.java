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
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.web.component.im.relationship.PeriodRelationshipState;


/**
 * Patient entity relationship state.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class PatientRelationshipState extends PeriodRelationshipState {

    /**
     * Determines if the patient is deceased.
     */
    private boolean deceased;


    /**
     * Creates a new <tt>PatientRelationshipState</tt>.
     *
     * @param relationship      the relationship
     * @param sourceId          the source entity id
     * @param sourceName        the source entity name
     * @param sourceDescription the source entity description
     * @param targetId          the target entity id
     * @param targetName        the target entity name
     * @param targetDescription the target entity description
     * @param active            determines the entities are active
     */
    public PatientRelationshipState(EntityRelationship relationship,
                                    long sourceId, String sourceName,
                                    String sourceDescription,
                                    long targetId, String targetName,
                                    String targetDescription,
                                    boolean active) {
        super(relationship, sourceId, sourceName, sourceDescription, targetId,
              targetName, targetDescription, active);
    }

    /**
     * Creates a new <tt>PatientRelationshipState</tt>.
     *
     * @param entity       the parent entity
     * @param relationship the relationship
     * @param source       determines if entity is the source or target of the
     *                     relationship
     * @throws ArchetypeServiceException for any archetype service exception
     */
    public PatientRelationshipState(Entity entity,
                                    EntityRelationship relationship,
                                    boolean source) {
        super(entity, relationship, source);
    }

    /**
     * Determines if the patient is deceased.
     *
     * @return <tt>true</tt> if the patient is deceased
     */
    public boolean isDeceased() {
        return deceased;
    }

    /**
     * Sets the deceased state of the patient.
     *
     * @param deceased if <tt>true</tt> indicates the patient is deceased
     */
    public void setDeceased(boolean deceased) {
        this.deceased = deceased;
    }

    /**
     * Determines if the relationship is active.
     * It is active if:
     * <ul>
     * <li>the underlying {@link EntityRelationship} is active
     * <li>the underlying entities are active
     * <li>{@link EntityRelationship#getActiveEndTime} is null or greater than
     * the current time
     * <li>the patient isn't deceased
     * </ul>
     *
     * @return <tt>true</tt> if this is active; otherwise <tt>false</tt>
     */
    @Override
    public boolean isActive() {
        return !deceased && super.isActive();
    }
}
