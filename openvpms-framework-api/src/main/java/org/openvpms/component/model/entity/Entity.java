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
import org.openvpms.component.model.object.IMObject;

import java.util.Set;

/**
 * An entity represents all named things in the business.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public interface Entity extends IMObject {

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
     * Returns the relationships where this entity is the source.
     *
     * @return the source entity relationships
     */
    Set<EntityRelationship> getSourceEntityRelationships();

    /**
     * Add a relationship where this entity is the source.
     *
     * @param relationship the entity relationship to add
     */
    void addSourceEntityRelationship(EntityRelationship relationship);

    /**
     * Removes a relationship where this entity is the source.
     *
     * @param relationship the entity relationship to remove
     */
    void removeSourceEntityRelationship(EntityRelationship relationship);

    /**
     * Returns the relationships where this entity is the target.
     *
     * @return the target entity relationships
     */
    Set<EntityRelationship> getTargetEntityRelationships();

    /**
     * Add a relationship where this entity is the target.
     *
     * @param relationship the entity relationship to add
     */
    void addTargetEntityRelationship(EntityRelationship relationship);

    /**
     * Removes a relationship where this entity is the target.
     *
     * @param relationship the entity relationship to remove
     */
    void removeTargetEntityRelationship(EntityRelationship relationship);

    /**
     * Return all the relationships that the entity has.
     * <p>
     * NOTE: the returned set cannot be used to add or remove relationships.
     *
     * @return the relationships
     */
    Set<EntityRelationship> getEntityRelationships();

    /**
     * Adds a relationship between this entity and another.
     * <p>
     * It will determine if this is a source or target of the relationship and invoke
     * {@link #addSourceEntityRelationship} or {@link #addTargetEntityRelationship} accordingly.
     *
     * @param relationship the entity relationship to add
     */
    void addEntityRelationship(EntityRelationship relationship);

    /**
     * Remove a relationship between this activity and another.
     * <p>
     * It will determine if this is a source or target of the relationship and invoke
     * {@link #removeSourceEntityRelationship} or {@link #removeTargetEntityRelationship} accordingly.
     *
     * @param relationship the entity relationship to remove
     */
    void removeEntityRelationship(EntityRelationship relationship);

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
