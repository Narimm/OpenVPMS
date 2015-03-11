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
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.relationship.PeriodRelationshipState;


/**
 * Patient entity relationship state.
 *
 * @author Tim Anderson
 */
public class PatientRelationshipState extends PeriodRelationshipState {

    /**
     * Determines if the patient is deceased.
     */
    private boolean deceased;

    /**
     * Constructs a {@link PatientRelationshipState}.
     *
     * @param relationship      the relationship
     * @param sourceId          the source entity id
     * @param sourceName        the source entity name
     * @param sourceDescription the source entity description
     * @param targetId          the target entity id
     * @param targetName        the target entity name
     * @param targetDescription the target entity description
     * @param active            determines the entities are active
     * @param deceased          if {@code true}, indicates that the patient is deceased
     */
    public PatientRelationshipState(EntityRelationship relationship,
                                    long sourceId, String sourceName,
                                    String sourceDescription,
                                    long targetId, String targetName,
                                    String targetDescription,
                                    boolean active,
                                    boolean deceased) {
        super(relationship, sourceId, sourceName, sourceDescription, targetId, targetName, targetDescription, active);
        setDeceased(deceased);
    }

    /**
     * Creates a new {@code PatientRelationshipState}.
     *
     * @param entity       the parent entity
     * @param relationship the relationship
     * @param source       determines if entity is the source or target of the relationship
     * @throws ArchetypeServiceException for any archetype service exception
     */
    public PatientRelationshipState(Entity entity,
                                    EntityRelationship relationship,
                                    boolean source) {
        super(entity, relationship, source);
        if (TypeHelper.isA(entity, PatientArchetypes.PATIENT)) {
            IMObjectBean bean = new IMObjectBean(entity);
            setDeceased(bean.getBoolean("deceased"));
        }
    }

    /**
     * Determines if the patient is deceased.
     *
     * @return {@code true} if the patient is deceased
     */
    public boolean isDeceased() {
        return deceased;
    }

    /**
     * Sets the deceased state of the patient.
     *
     * @param deceased if {@code true} indicates the patient is deceased
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
     * @return {@code true} if this is active; otherwise {@code false}
     */
    @Override
    public boolean isActive() {
        return !deceased && super.isActive();
    }
}
