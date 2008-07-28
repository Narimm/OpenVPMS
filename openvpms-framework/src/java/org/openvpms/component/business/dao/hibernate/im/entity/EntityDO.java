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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDO;
import org.openvpms.component.business.domain.im.common.EntityIdentity;

import java.util.Set;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface EntityDO extends IMObjectDO {
    /**
     * Add a new {@link EntityIdentity}
     *
     * @param identity the entity identity to add
     */
    void addIdentity(EntityIdentityDO identity);

    /**
     * Remove the specified {@link EntityIdentity}
     *
     * @param identity the identity to remove
     * @return boolean
     */
    boolean removeIdentity(EntityIdentityDO identity);

    /**
     * Return the {@link EntityIdentity} as an array
     *
     * @return EntityIdentity[]
     */
    Set<EntityIdentityDO> getIdentities();

    /**
     * Returns the relationships where this is the source entity.
     *
     * @return the source entity relationships
     */
    Set<EntityRelationshipDO> getSourceEntityRelationships();

    /**
     * Adds a relationship where this is the source.
     *
     * @param source the entity relationship to add
     */
    void addSourceEntityRelationship(EntityRelationshipDO source);

    /**
     * Removes a source relationship.
     *
     * @param source the entity relationship to remove
     */
    void removeSourceEntityRelationship(EntityRelationshipDO source);

    /**
     * Returns the relationships where this is the target entity.
     *
     * @return the target entity relationships
     */
    Set<EntityRelationshipDO> getTargetEntityRelationships();

    /**
     * Adds a relationship where this is the target.
     *
     * @param target the entity relationship to add
     */
    void addTargetEntityRelationship(EntityRelationshipDO target);

    /**
     * Removes a target relationship.
     *
     * @param target the entity relationship to remove
     */
    void removeTargetEntityRelationship(EntityRelationshipDO target);

    /**
     * Returns all the entity relationships. Do not use the returned set to
     *
     * @return the relationships
     */
    Set<EntityRelationshipDO> getEntityRelationships();

    /**
     * Adds a classification to this entity.
     *
     * @param classification the classification to add
     */
    void addClassification(LookupDO classification);

    /**
     * Removes a classification from this entity.
     *
     * @param classification the classification to remove
     */
    void removeClassification(LookupDO classification);

    /**
     * Returns the classifications for this entity.
     *
     * @return the classifications
     */
    Set<LookupDO> getClassifications();
}
