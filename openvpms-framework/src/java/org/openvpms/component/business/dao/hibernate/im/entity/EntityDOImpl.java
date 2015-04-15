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

import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDOImpl;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDO;
import org.openvpms.component.business.domain.archetype.ArchetypeId;

import java.util.HashSet;
import java.util.Set;


/**
 * Implementation of the {@link EntityDO} interface.
 *
 * @author Tim Anderson
 */
public class EntityDOImpl extends IMObjectDOImpl implements EntityDO {

    /**
     * The identities of the entity.
     */
    private Set<EntityIdentityDO> identities = new HashSet<EntityIdentityDO>();

    /**
     * The relationships where the entity is the source.
     */
    private Set<EntityRelationshipDO> sourceEntityRelationships =
            new HashSet<EntityRelationshipDO>();

    /**
     * The relationships where the entity is the target.
     */
    private Set<EntityRelationshipDO> targetEntityRelationships =
            new HashSet<EntityRelationshipDO>();

    /**
     * The entity links.
     */
    private Set<EntityLinkDO> links = new HashSet<EntityLinkDO>();

    /**
     * The entity classifications.
     */
    private Set<LookupDO> classifications = new HashSet<LookupDO>();


    /**
     * Default constructor.
     */
    public EntityDOImpl() {
        // do nothing
    }

    /**
     * Constructs an {@link EntityDOImpl}.
     *
     * @param archetypeId the archetype id
     */
    public EntityDOImpl(ArchetypeId archetypeId) {
        super(archetypeId);
    }

    /**
     * Returns the entity identities.
     *
     * @return the entity identities
     */
    public Set<EntityIdentityDO> getIdentities() {
        return identities;
    }

    /**
     * Adds an identity.
     *
     * @param identity the entity identity to add
     */
    public void addIdentity(EntityIdentityDO identity) {
        identity.setEntity(this);
        identities.add(identity);
    }

    /**
     * Removes the identity.
     *
     * @param identity the identity to remove
     * @return {@code true} if the identity existed
     */
    public boolean removeIdentity(EntityIdentityDO identity) {
        identity.setEntity(null);
        return identities.remove(identity);
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
     * Returns all the entity relationships.
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
     * Returns the links for this entity.
     *
     * @return the links
     */
    @Override
    public Set<EntityLinkDO> getEntityLinks() {
        return links;
    }

    /**
     * Adds a link to another entity.
     *
     * @param link the link
     */
    @Override
    public void addEntityLink(EntityLinkDO link) {
        link.setSource(this);
        links.add(link);
    }

    /**
     * Removes a link to another entity.
     *
     * @param link the link
     */
    @Override
    public void removeEntityLink(EntityLinkDO link) {
        links.remove(link);
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
     * Sets the entity links.
     *
     * @param links the links
     */
    protected void setEntityLinks(Set<EntityLinkDO> links) {
        this.links = links;
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
     * Sets the identities for this entity.
     *
     * @param identities the identities to set
     */
    protected void setIdentities(Set<EntityIdentityDO> identities) {
        this.identities = identities;
    }

}
