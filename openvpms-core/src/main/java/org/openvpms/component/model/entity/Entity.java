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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.model.entity;

import org.openvpms.component.model.lookup.Lookup;

import java.util.Set;

/**
 * .
 *
 * @author Tim Anderson
 */
public interface Entity {

    /**
     * Adds an identity for this entity.
     *
     * @param identity the identity to add
     */
    void addIdentity(EntityIdentity identity);

    /**
     * Removes an identity.
     *
     * @param identity the identity to remove
     * @return {@code true} if the identity was removed
     */
    boolean removeIdentity(EntityIdentity identity);

    /**
     * Returns the identities for this entity.
     *
     * @return the identities
     */
    Set<EntityIdentity> getIdentities();

    /**
     * Returns the relationships where this is the source entity.
     *
     * @return the source entity relationships
     */
    Set<EntityRelationship> getSourceEntityRelationships();

    /**
     * Returns the relationships where this is the target entity.
     *
     * @return the target entity relationships
     */
    Set<EntityRelationship> getTargetEntityRelationships();

    /**
     * Add a relationship to this entity. It will determine whether it is a
     * source or target relationship before adding it.
     *
     * @param relationship the entity relationship to add
     */
    public void addEntityRelationship(EntityRelationship relationship);

    /**
     * Remove a relationship to this entity. It will determine whether it is a
     * source or target relationship before removing it.
     *
     * @param entityRel the entity relationship to remove
     */
    void removeEntityRelationship(EntityRelationship entityRel);

    /**
     * Add a source {@link EntityRelationship} to this entity.
     *
     * @param relationship the entity relationship to add
     */
    void addSourceEntityRelationship(EntityRelationship relationship);

    /**
     * Remove the source {@link EntityRelationship} from this entity.
     *
     * @param relationship the entity relationship to remove
     */
    void removeSourceEntityRelationship(EntityRelationship relationship);

    /**
     * Add a target {@link EntityRelationship} to this entity.
     *
     * @param relationship the entity relationship to add
     */
    void addTargetEntityRelationship(EntityRelationship relationship);

    /**
     * Remove the target {@link EntityRelationship} from this entity.
     *
     * @param relationship the entity relationship to remove
     */
    void removeTargetEntityRelationship(EntityRelationship relationship);

    /**
     * Return all the entity relationships. Do not use the returned set to
     * add and remove entity relationships. Instead use {@link #addEntityRelationship(EntityRelationship)}
     * and {@link #removeEntityRelationship(EntityRelationship)} repsectively.
     *
     * @return Set<EntityRelationship>
     */
    Set<EntityRelationship> getEntityRelationships();

    /**
     * Adds an entity link.
     *
     * @param link the link to add
     */
    void addEntityLink(EntityLink link);

    /**
     * Removes an entity link.
     *
     * @param link the link to remove
     */
    void removeEntityLink(EntityLink link);

    /**
     * Returns the entity links.
     *
     * @return the entity links
     */
    Set<EntityLink> getEntityLinks();

    /**
     * Adds a classification to this entity.
     *
     * @param classification the classification to add
     */
    void addClassification(Lookup classification);

    /**
     * Removes a classification from this entity.
     *
     * @param classification the classification to remove
     */
    void removeClassification(Lookup classification);

    /**
     * Returns the classifications for this entity.
     *
     * @return the classifications
     */
    Set<Lookup> getClassifications();

}
