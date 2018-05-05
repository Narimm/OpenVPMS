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

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.lookup.Lookup;

import java.util.HashSet;
import java.util.Set;


/**
 * A class representing all named things in the business.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public class Entity extends IMObject implements org.openvpms.component.model.entity.Entity {

    /**
     * Serialisation version identifier.
     */
    private static final long serialVersionUID = 2L;

    /**
     * The {@link Lookup} classifications this entity. An {@link Entity} can
     * have to zero, one or more {@link Lookup} classifications.
     */
    private Set<org.openvpms.component.model.lookup.Lookup> classifications = new HashSet<>();

    /**
     * Return the set of {@link EntityIdentity} instance for this entity
     */
    private Set<org.openvpms.component.model.entity.EntityIdentity> identities = new HashSet<>();

    /**
     * Return a set of source {@link EntityRelationship}s that this entity
     * participates in.
     */
    private Set<org.openvpms.component.model.entity.EntityRelationship> sourceEntityRelationships = new HashSet<>();

    /**
     * Return a set of target {@link EntityRelationship}s that this entity
     * participates in.
     */
    private Set<org.openvpms.component.model.entity.EntityRelationship> targetEntityRelationships = new HashSet<>();

    /**
     * The entity links.
     */
    private Set<org.openvpms.component.model.entity.EntityLink> entityLinks = new HashSet<>();

    /**
     * Default constructor.
     */
    public Entity() {
        // do nothing
    }

    /**
     * Creates a new <tt>Entity</tt>.
     *
     * @param archetypeId the archetype id constraining this object
     * @param name        the name of the entity.
     * @param description the description of the archetype
     */
    public Entity(ArchetypeId archetypeId, String name, String description) {
        super(archetypeId, name, description);
    }

    /**
     * Add a new {@link EntityIdentity}
     *
     * @param identity the entity identity to add
     */
    public void addIdentity(org.openvpms.component.model.entity.EntityIdentity identity) {
        ((EntityIdentity) identity).setEntity(this);
        identities.add(identity);
    }

    /**
     * Remove the specified {@link EntityIdentity}
     *
     * @param identity the identity to remove
     * @return boolean
     */
    public boolean removeIdentity(org.openvpms.component.model.entity.EntityIdentity identity) {
        ((EntityIdentity) identity).setEntity(null);
        return (identities.remove(identity));
    }

    /**
     * Return the {@link EntityIdentity} as an array
     *
     * @return EntityIdentity[]
     */
    public Set<org.openvpms.component.model.entity.EntityIdentity> getIdentities() {
        return identities;
    }

    /**
     * Returns the relationships where this is the source entity.
     *
     * @return the source entity relationships
     */
    public Set<org.openvpms.component.model.entity.EntityRelationship> getSourceEntityRelationships() {
        return sourceEntityRelationships;
    }

    /**
     * Returns the relationships where this is the target entity.
     *
     * @return the target entity relationships
     */
    public Set<org.openvpms.component.model.entity.EntityRelationship> getTargetEntityRelationships() {
        return targetEntityRelationships;
    }

    /**
     * @param entityRelationships The sourceEntityRelationships to set.
     */
    protected void setSourceEntityRelationships(
            Set<org.openvpms.component.model.entity.EntityRelationship> entityRelationships) {
        this.sourceEntityRelationships = entityRelationships;
    }

    /**
     * @param entityRelationships The targetEntityRelationships to set.
     */
    protected void setTargetEntityRelationships(
            Set<org.openvpms.component.model.entity.EntityRelationship> entityRelationships) {
        this.targetEntityRelationships = entityRelationships;
    }

    /**
     * Add a relationship to this entity. It will determine whether it is a
     * source or target relationship before adding it.
     *
     * @param relationship the entity relationship to add
     * @throws EntityException if this relationship cannot be added to this entity
     */
    public void addEntityRelationship(org.openvpms.component.model.entity.EntityRelationship relationship) {
        if (relationship.getSource() == null || relationship.getTarget() == null) {
            throw new EntityException(EntityException.ErrorCode.FailedToAddEntityRelationship,
                                      new Object[]{relationship.getSource(), relationship.getTarget()});
        }
        if (ObjectUtils.equals(relationship.getSource().getLinkId(), getLinkId())
            && ObjectUtils.equals(relationship.getSource().getArchetype(), getArchetype())) {
            sourceEntityRelationships.add(relationship);
        } else if (ObjectUtils.equals(relationship.getTarget().getLinkId(), getLinkId())
                   && ObjectUtils.equals(relationship.getTarget().getArchetype(), getArchetype())) {
            targetEntityRelationships.add(relationship);
        } else {
            throw new EntityException(EntityException.ErrorCode.FailedToAddEntityRelationship,
                                      new Object[]{relationship.getSource(), relationship.getTarget()});
        }
    }

    /**
     * Remove a relationship to this entity. It will determine whether it is a
     * source or target relationship before removing it.
     *
     * @param relationship the entity relationship to remove
     */
    public void removeEntityRelationship(org.openvpms.component.model.entity.EntityRelationship relationship) {
        if (relationship.getSource() != null && ObjectUtils.equals(relationship.getSource().getLinkId(), getLinkId())) {
            removeSourceEntityRelationship(relationship);
        } else if (relationship.getTarget() != null && ObjectUtils.equals(relationship.getTarget().getLinkId(), getLinkId())) {
            removeTargetEntityRelationship(relationship);
        } else {
            throw new EntityException(EntityException.ErrorCode.FailedToRemoveEntityRelationship,
                                      new Object[]{relationship.getSource(), relationship.getTarget()});
        }
    }

    /**
     * Add a source {@link EntityRelationship} to this entity.
     *
     * @param relationship the entity relationship to add
     */
    public void addSourceEntityRelationship(org.openvpms.component.model.entity.EntityRelationship relationship) {
        relationship.setSource(getObjectReference());
        sourceEntityRelationships.add(relationship);
    }

    /**
     * Remove the source {@link EntityRelationship} from this entity.
     *
     * @param relationship the entity relationship to remove
     */
    public void removeSourceEntityRelationship(org.openvpms.component.model.entity.EntityRelationship relationship) {
        sourceEntityRelationships.remove(relationship);
    }

    /**
     * Add a target {@link EntityRelationship} to this entity.
     *
     * @param relationship the entity relationship to add
     */
    public void addTargetEntityRelationship(org.openvpms.component.model.entity.EntityRelationship relationship) {
        relationship.setTarget(getObjectReference());
        targetEntityRelationships.add(relationship);
    }

    /**
     * Remove the target {@link EntityRelationship} from this entity.
     *
     * @param relationship the entity relationship to remove
     */
    public void removeTargetEntityRelationship(org.openvpms.component.model.entity.EntityRelationship relationship) {
        targetEntityRelationships.remove(relationship);
    }

    /**
     * Return all the entity relationships.
     *
     * @return Set<EntityRelationship>
     */
    public Set<org.openvpms.component.model.entity.EntityRelationship> getEntityRelationships() {
        Set<org.openvpms.component.model.entity.EntityRelationship> relationships
                = new HashSet<>(sourceEntityRelationships);
        relationships.addAll(targetEntityRelationships);
        return relationships;
    }

    /**
     * Adds an entity link.
     *
     * @param link the link to add
     */
    public void addEntityLink(org.openvpms.component.model.entity.EntityLink link) {
        link.setSource(getObjectReference());
        entityLinks.add(link);
    }

    /**
     * Removes an entity link.
     *
     * @param link the link to remove
     */
    public void removeEntityLink(org.openvpms.component.model.entity.EntityLink link) {
        entityLinks.remove(link);
    }

    /**
     * Returns the entity links.
     *
     * @return the entity links
     */
    public Set<org.openvpms.component.model.entity.EntityLink> getEntityLinks() {
        return entityLinks;
    }

    /**
     * Adds a classification to this entity.
     *
     * @param classification the classification to add
     */
    public void addClassification(org.openvpms.component.model.lookup.Lookup classification) {
        classifications.add(classification);
    }

    /**
     * Removes a classification from this entity.
     *
     * @param classification the classification to remove
     */
    public void removeClassification(org.openvpms.component.model.lookup.Lookup classification) {
        classifications.remove(classification);
    }

    /**
     * Returns the classifications for this entity.
     *
     * @return the classifications
     */
    public Set<org.openvpms.component.model.lookup.Lookup> getClassifications() {
        return classifications;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Entity copy = (Entity) super.clone();
        copy.classifications = new HashSet<>(classifications);
        copy.identities = new HashSet<>(identities);
        copy.sourceEntityRelationships = new HashSet<>(sourceEntityRelationships);
        copy.targetEntityRelationships = new HashSet<>(targetEntityRelationships);
        return copy;
    }

    /**
     * Sets the classifications for this entity.
     *
     * @param classifications the classifications to set
     */
    protected void setClassifications(Set<org.openvpms.component.model.lookup.Lookup> classifications) {
        this.classifications = classifications;
    }

    /**
     * Sets the identifies for this entity.
     *
     * @param identities the identities to set
     */
    protected void setIdentities(Set<org.openvpms.component.model.entity.EntityIdentity> identities) {
        this.identities = identities;
    }

}
