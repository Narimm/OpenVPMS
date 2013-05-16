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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.domain.im.common;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.lookup.Lookup;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * A class representing all named things in the business.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public class Entity extends IMObject {

    /**
     * Serialisation version identifier.
     */
    private static final long serialVersionUID = 2L;

    /**
     * The {@link Lookup} classifications this entity. An {@link Entity} can
     * have to zero, one or more {@link Lookup} classifications.
     */
    private Set<Lookup> classifications = new HashSet<Lookup>();

    /**
     * Return the set of {@link EntityIdentity} instance for this entity
     */
    private Set<EntityIdentity> identities = new HashSet<EntityIdentity>();

    /**
     * Return a set of source {@link EntityRelationship}s that this entity
     * participates in.
     */
    private Set<EntityRelationship> sourceEntityRelationships = new HashSet<EntityRelationship>();

    /**
     * Return a set of target {@link EntityRelationship}s that this entity
     * participates in.
     */
    private Set<EntityRelationship> targetEntityRelationships = new HashSet<EntityRelationship>();

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
     * Constructs an instance of a base entity.
     *
     * @param archetypeId the archetype id constraining this object
     * @param name        the name of the entity.
     * @param description the description of the archetype
     * @param details     dynamic details of the act.
     * @deprecated no replacement
     */
    @Deprecated
    public Entity(ArchetypeId archetypeId, String name,
                  String description, Map<String, Object> details) {
        super(archetypeId, name, description);
        if (details != null) {
            setDetails(details);
        }

        // check that a name was specified
        if (StringUtils.isEmpty(name)) {
            throw new EntityException(
                    EntityException.ErrorCode.NoNameSpecified);
        }
    }

    /**
     * Add a new {@link EntityIdentity}
     *
     * @param identity the entity identity to add
     */
    public void addIdentity(EntityIdentity identity) {
        identity.setEntity(this);
        identities.add(identity);
    }

    /**
     * Remove the specified {@link EntityIdentity}
     *
     * @param identity the identity to remove
     * @return boolean
     */
    public boolean removeIdentity(EntityIdentity identity) {
        identity.setEntity(null);
        return (identities.remove(identity));
    }

    /**
     * Return the {@link EntityIdentity} as an array
     *
     * @return EntityIdentity[]
     */
    public Set<EntityIdentity> getIdentities() {
        return identities;
    }

    /**
     * Returns the relationships where this is the source entity.
     *
     * @return the source entity relationships
     */
    public Set<EntityRelationship> getSourceEntityRelationships() {
        return sourceEntityRelationships;
    }

    /**
     * Returns the relationships where this is the target entity.
     *
     * @return the target entity relationships
     */
    public Set<EntityRelationship> getTargetEntityRelationships() {
        return targetEntityRelationships;
    }

    /**
     * @param entityRelationships The sourceEntityRelationships to set.
     */
    protected void setSourceEntityRelationships(
            Set<EntityRelationship> entityRelationships) {
        this.sourceEntityRelationships = entityRelationships;
    }

    /**
     * @param entityRelationships The targetEntityRelationships to set.
     */
    protected void setTargetEntityRelationships(
            Set<EntityRelationship> entityRelationships) {
        this.targetEntityRelationships = entityRelationships;
    }

    /**
     * Add a relationship to this entity. It will determine whether it is a
     * source or target relationship before adding it.
     *
     * @param relationship the entity relationship to add
     * @throws EntityException if this relationship cannot be added to this entity
     */
    public void addEntityRelationship(EntityRelationship relationship) {
        if (relationship.getSource() == null || relationship.getTarget() == null) {
            throw new EntityException(EntityException.ErrorCode.FailedToAddEntityRelationship,
                                      new Object[]{relationship.getSource(), relationship.getTarget()});
        }
        if (ObjectUtils.equals(relationship.getSource().getLinkId(), getLinkId())
            && ObjectUtils.equals(relationship.getSource().getArchetypeId(), getArchetypeId())) {
            sourceEntityRelationships.add(relationship);
        } else if (ObjectUtils.equals(relationship.getTarget().getLinkId(), getLinkId())
                   && ObjectUtils.equals(relationship.getTarget().getArchetypeId(), getArchetypeId())) {
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
     * @param entityRel the entity relationship to remove
     */
    public void removeEntityRelationship(EntityRelationship entityRel) {
        if (entityRel.getSource() == null || entityRel.getTarget() == null) {
            throw new EntityException(EntityException.ErrorCode.FailedToRemoveEntityRelationship,
                                      new Object[]{entityRel.getSource(), entityRel.getTarget()});
        }
        if (ObjectUtils.equals(entityRel.getSource().getLinkId(), getLinkId())
            && ObjectUtils.equals(entityRel.getSource().getArchetypeId(), getArchetypeId())) {
            removeSourceEntityRelationship(entityRel);
        } else if (ObjectUtils.equals(entityRel.getTarget().getLinkId(), getLinkId())
                   && ObjectUtils.equals(entityRel.getTarget().getArchetypeId(), getArchetypeId())) {
            removeTargetEntityRelationship(entityRel);
        } else {
            throw new EntityException(EntityException.ErrorCode.FailedToRemoveEntityRelationship,
                                      new Object[]{entityRel.getSource(), entityRel.getTarget()});
        }
    }

    /**
     * Add a source {@link EntityRelationship} to this entity.
     *
     * @param relationship the entity relationship to add
     */
    public void addSourceEntityRelationship(EntityRelationship relationship) {
        relationship.setSource(getObjectReference());
        sourceEntityRelationships.add(relationship);
    }

    /**
     * Remove the source {@link EntityRelationship} from this entity.
     *
     * @param relationship the entity relationship to remove
     */
    public void removeSourceEntityRelationship(EntityRelationship relationship) {
        sourceEntityRelationships.remove(relationship);
    }

    /**
     * Add a target {@link EntityRelationship} to this entity.
     *
     * @param relationship the entity relationship to add
     */
    public void addTargetEntityRelationship(EntityRelationship relationship) {
        relationship.setTarget(getObjectReference());
        targetEntityRelationships.add(relationship);
    }

    /**
     * Remove the target {@link EntityRelationship} from this entity.
     *
     * @param relationship the entity relationship to remove
     */
    public void removeTargetEntityRelationship(EntityRelationship relationship) {
        targetEntityRelationships.remove(relationship);
    }

    /**
     * Return all the entity relationships. Do not use the returned set to
     * add and remove entity relationships. Instead use {@link #addEntityRelationship(EntityRelationship)}
     * and {@link #removeEntityRelationship(EntityRelationship)} repsectively.
     *
     * @return Set<EntityRelationship>
     */
    public Set<EntityRelationship> getEntityRelationships() {
        Set<EntityRelationship> relationships = new HashSet<EntityRelationship>(sourceEntityRelationships);
        relationships.addAll(targetEntityRelationships);
        return relationships;
    }

    /**
     * Adds a classification to this entity.
     *
     * @param classification the classification to add
     */
    public void addClassification(Lookup classification) {
        classifications.add(classification);
    }

    /**
     * Removes a classification from this entity.
     *
     * @param classification the classification to remove
     */
    public void removeClassification(Lookup classification) {
        classifications.remove(classification);
    }

    /**
     * Returns the classifications for this entity.
     *
     * @return the classifications
     */
    public Set<Lookup> getClassifications() {
        return classifications;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Entity copy = (Entity) super.clone();
        copy.classifications = new HashSet<Lookup>(classifications);
        copy.identities = new HashSet<EntityIdentity>(identities);
        copy.sourceEntityRelationships = new HashSet<EntityRelationship>(
                sourceEntityRelationships);
        copy.targetEntityRelationships = new HashSet<EntityRelationship>(
                targetEntityRelationships);

        return copy;
    }

    /**
     * Sets the classifications for this entity.
     *
     * @param classifications the classifications to set
     */
    protected void setClassifications(Set<Lookup> classifications) {
        this.classifications = classifications;
    }

    /**
     * Sets the identifies for this entity.
     *
     * @param identities the identities to set
     */
    protected void setIdentities(Set<EntityIdentity> identities) {
        this.identities = identities;
    }

}
