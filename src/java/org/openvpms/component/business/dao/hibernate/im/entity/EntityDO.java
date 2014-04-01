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

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDO;
import org.openvpms.component.business.domain.im.common.Entity;

import java.util.Set;


/**
 * Data object interface corresponding to the {@link Entity} class.
 *
 * @author Tim Anderson
 */
public interface EntityDO extends IMObjectDO {

    /**
     * Returns the entity identities.
     *
     * @return the entity identities
     */
    Set<EntityIdentityDO> getIdentities();

    /**
     * Adds an identity.
     *
     * @param identity the entity identity to add
     */
    void addIdentity(EntityIdentityDO identity);

    /**
     * Removes the identity.
     *
     * @param identity the identity to remove
     * @return <tt>true</tt> if the identity existed
     */
    boolean removeIdentity(EntityIdentityDO identity);

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
     * Returns all the entity relationships.
     *
     * @return the relationships
     */
    Set<EntityRelationshipDO> getEntityRelationships();

    /**
     * Returns the links for this entity.
     *
     * @return the links
     */
    Set<EntityLinkDO> getEntityLinks();

    /**
     * Adds a link to another entity.
     *
     * @param link the link
     */
    void addEntityLink(EntityLinkDO link);

    /**
     * Removes a link to another entity.
     *
     * @param link the link
     */
    void removeEntityLink(EntityLinkDO link);

    /**
     * Returns the classifications for this entity.
     *
     * @return the classifications
     */
    Set<LookupDO> getClassifications();

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

}
