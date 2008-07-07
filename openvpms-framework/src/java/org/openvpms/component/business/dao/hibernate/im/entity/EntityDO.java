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
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;

import java.util.HashSet;
import java.util.Set;

/**
 * A class representing all named things in the business.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2008-05-22 15:14:34 +1000 (Thu, 22 May 2008) $
 */
public class EntityDO extends IMObjectDO {

    /**
     * The {@link Lookup} classifications this entity. An {@link EntityDO} can
     * have to zero, one or more {@link Lookup} clasification.
     */
    private Set<LookupDO> classifications = new HashSet<LookupDO>();

    /**
     * Return the set of {@link EntityIdentity} instance for this entity
     */
    private Set<EntityIdentityDO> identities =
            new HashSet<EntityIdentityDO>();

    /**
     * Return a set of source {@link EntityRelationship}s that this entity
     * participates in.
     */
    private Set<EntityRelationshipDO> sourceEntityRelationships =
            new HashSet<EntityRelationshipDO>();

    /**
     * Return a set of target {@link EntityRelationship}s that this entity
     * participates in.
     */
    private Set<EntityRelationshipDO> targetEntityRelationships =
            new HashSet<EntityRelationshipDO>();


    /**
     * Default constructor.
     */
    public EntityDO() {
        // do nothing
    }

    /**
     * Creates a new <tt>EntityDO</tt>.
     *
     * @param archetypeId the archetype id
     */
    public EntityDO(ArchetypeId archetypeId) {
        super(archetypeId);
    }

    /**
     * Add a new {@link EntityIdentity}
     *
     * @param identity the entity identity to add
     */
    public void addIdentity(EntityIdentityDO identity) {
        identity.setEntity(this);
        identities.add(identity);
    }

    /**
     * Remove the specified {@link EntityIdentity}
     *
     * @param identity the identity to remove
     * @return boolean
     */
    public boolean removeIdentity(EntityIdentityDO identity) {
        identity.setEntity(null);
        return (identities.remove(identity));
    }

    /**
     * Return the {@link EntityIdentity} as an array
     *
     * @return EntityIdentity[]
     */
    public Set<EntityIdentityDO> getIdentities() {
        return identities;
    }

    /**
     * Returns the relationships where this is the source entity.
     *
     * @return the source entity relationships
     */
    public Set<EntityRelationshipDO> getSourceEntityRelationships() {
        return sourceEntityRelationships;
    }

    /**
     * Adds a relationship where this is the source.
     *
     * @param source the entity relationship to add
     */
    public void addSourceEntityRelationship(EntityRelationshipDO source) {
        sourceEntityRelationships.add(source);
        source.setSource(this);
    }

    /**
     * Removes a source relationship.
     *
     * @param source the entity relationship to remove
     */
    public void removeSourceEntityRelationship(EntityRelationshipDO source) {
        sourceEntityRelationships.remove(source);
    }

    /**
     * Returns the relationships where this is the target entity.
     *
     * @return the target entity relationships
     */
    public Set<EntityRelationshipDO> getTargetEntityRelationships() {
        return targetEntityRelationships;
    }

    /**
     * Adds a relationship where this is the target.
     *
     * @param target the entity relationship to add
     */
    public void addTargetEntityRelationship(EntityRelationshipDO target) {
        targetEntityRelationships.add(target);
        target.setTarget(this);
    }

    /**
     * Removes a target relationship.
     *
     * @param target the entity relationship to remove
     */
    public void removeTargetEntityRelationship(EntityRelationshipDO target) {
        targetEntityRelationships.remove(target);
    }

    /**
     * Returns all the entity relationships. Do not use the returned set to
     *
     * @return the relationships
     */
    public Set<EntityRelationshipDO> getEntityRelationships() {
        Set<EntityRelationshipDO> relationships =
                new HashSet<EntityRelationshipDO>(sourceEntityRelationships);
        relationships.addAll(targetEntityRelationships);
        return relationships;
    }

    /**
     * Adds a classification to this entity.
     *
     * @param classification the classification to add
     */
    public void addClassification(LookupDO classification) {
        classifications.add(classification);
    }

    /**
     * Removes a classification from this entity.
     *
     * @param classification the classification to remove
     */
    public void removeClassification(LookupDO classification) {
        classifications.remove(classification);
    }

    /**
     * Returns the classifications for this entity.
     *
     * @return the classifications
     */
    public Set<LookupDO> getClassifications() {
        return classifications;
    }

    /**
     * Sets the relationships where this is the source.
     *
     * @param relationships the relationships to set
     */
    protected void setSourceEntityRelationships(
            Set<EntityRelationshipDO> relationships) {
        sourceEntityRelationships = relationships;
    }

    /**
     * Sets the relationships where this is the target.
     *
     * @param relationships the relationships to set
     */
    protected void setTargetEntityRelationships(
            Set<EntityRelationshipDO> relationships) {
        targetEntityRelationships = relationships;
    }

    /**
     * Sets the classifications for this entity.
     *
     * @param classifications the classifications to set
     */
    protected void setClassifications(Set<LookupDO> classifications) {
        this.classifications = classifications;
    }

    /**
     * Sets the identifies for this entity.
     *
     * @param identities the identities to set
     */
    protected void setIdentities(Set<EntityIdentityDO> identities) {
        this.identities = identities;
    }

}
