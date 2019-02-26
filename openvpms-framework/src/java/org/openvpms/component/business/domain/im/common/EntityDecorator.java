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

package org.openvpms.component.business.domain.im.common;

import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.entity.EntityIdentity;
import org.openvpms.component.model.entity.EntityLink;
import org.openvpms.component.model.entity.EntityRelationship;
import org.openvpms.component.model.lookup.Lookup;

import java.util.Set;

/**
 * Decorator for {@link Entity}.
 *
 * @author Tim Anderson
 */
public class EntityDecorator extends IMObjectDecorator implements Entity {

    /**
     * Constructs an {@link EntityDecorator}.
     *
     * @param peer the peer to delegate to
     */
    public EntityDecorator(Entity peer) {
        super(peer);
    }

    /**
     * Adds an identity for this entity.
     *
     * @param identity the identity to add
     */
    @Override
    public void addIdentity(EntityIdentity identity) {
        getPeer().addIdentity(identity);
    }

    /**
     * Removes an identity.
     *
     * @param identity the identity to remove
     * @return {@code true} if the identity was removed
     */
    @Override
    public boolean removeIdentity(EntityIdentity identity) {
        return getPeer().removeIdentity(identity);
    }

    /**
     * Returns the identities for this entity.
     *
     * @return the identities
     */
    @Override
    public Set<EntityIdentity> getIdentities() {
        return getPeer().getIdentities();
    }

    /**
     * Returns the relationships where this entity is the source.
     *
     * @return the source entity relationships
     */
    @Override
    public Set<EntityRelationship> getSourceEntityRelationships() {
        return getPeer().getSourceEntityRelationships();
    }

    /**
     * Add a relationship where this entity is the source.
     *
     * @param relationship the entity relationship to add
     */
    @Override
    public void addSourceEntityRelationship(EntityRelationship relationship) {
        getPeer().addSourceEntityRelationship(relationship);
    }

    /**
     * Removes a relationship where this entity is the source.
     *
     * @param relationship the entity relationship to remove
     */
    @Override
    public void removeSourceEntityRelationship(EntityRelationship relationship) {
        getPeer().removeSourceEntityRelationship(relationship);
    }

    /**
     * Returns the relationships where this entity is the target.
     *
     * @return the target entity relationships
     */
    @Override
    public Set<EntityRelationship> getTargetEntityRelationships() {
        return getPeer().getTargetEntityRelationships();
    }

    /**
     * Add a relationship where this entity is the target.
     *
     * @param relationship the entity relationship to add
     */
    @Override
    public void addTargetEntityRelationship(EntityRelationship relationship) {
        getPeer().addTargetEntityRelationship(relationship);
    }

    /**
     * Removes a relationship where this entity is the target.
     *
     * @param relationship the entity relationship to remove
     */
    @Override
    public void removeTargetEntityRelationship(EntityRelationship relationship) {
        getPeer().removeTargetEntityRelationship(relationship);
    }

    /**
     * Return all the relationships that the entity has.
     * <p>
     * NOTE: the returned set cannot be used to add or remove relationships.
     *
     * @return the relationships
     */
    @Override
    public Set<EntityRelationship> getEntityRelationships() {
        return getPeer().getEntityRelationships();
    }

    /**
     * Adds a relationship between this entity and another.
     * <p>
     * It will determine if this is a source or target of the relationship and invoke
     * {@link #addSourceEntityRelationship} or {@link #addTargetEntityRelationship} accordingly.
     *
     * @param relationship the entity relationship to add
     */
    @Override
    public void addEntityRelationship(EntityRelationship relationship) {
        getPeer().addEntityRelationship(relationship);
    }

    /**
     * Remove a relationship between this activity and another.
     * <p>
     * It will determine if this is a source or target of the relationship and invoke
     * {@link #removeSourceEntityRelationship} or {@link #removeTargetEntityRelationship} accordingly.
     *
     * @param relationship the entity relationship to remove
     */
    @Override
    public void removeEntityRelationship(EntityRelationship relationship) {
        getPeer().removeEntityRelationship(relationship);
    }

    /**
     * Adds an entity link.
     *
     * @param link the link to add
     */
    @Override
    public void addEntityLink(EntityLink link) {
        getPeer().addEntityLink(link);
    }

    /**
     * Removes an entity link.
     *
     * @param link the link to remove
     */
    @Override
    public void removeEntityLink(EntityLink link) {
        getPeer().removeEntityLink(link);
    }

    /**
     * Returns the entity links.
     *
     * @return the entity links
     */
    @Override
    public Set<EntityLink> getEntityLinks() {
        return getPeer().getEntityLinks();
    }

    /**
     * Adds a classification to this entity.
     *
     * @param classification the classification to add
     */
    @Override
    public void addClassification(Lookup classification) {
        getPeer().addClassification(classification);
    }

    /**
     * Removes a classification from this entity.
     *
     * @param classification the classification to remove
     */
    @Override
    public void removeClassification(Lookup classification) {
        getPeer().removeClassification(classification);
    }

    /**
     * Returns the classifications for this entity.
     *
     * @return the classifications
     */
    @Override
    public Set<Lookup> getClassifications() {
        return getPeer().getClassifications();
    }

    /**
     * Returns the peer.
     *
     * @return the peer
     */
    @Override
    protected Entity getPeer() {
        return (Entity) super.getPeer();
    }
}
