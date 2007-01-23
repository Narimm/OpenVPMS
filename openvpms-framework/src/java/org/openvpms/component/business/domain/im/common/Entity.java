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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.domain.im.common;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;

import java.util.HashSet;
import java.util.Set;


/**
 * A class representing all named things in the business.
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class Entity extends IMObject {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * A placeholder for all entity details, which denotes the dynamic and
     * adaptive details of the entity.
     */
    private DynamicAttributeMap details;
    
    /**
     * Return a set of {@link Classification} for this entity. An 
     * {@link Entity} can have to zero, one or more {@link Classification}
     * 
     */
    private Set<Classification> classifications =
        new HashSet<Classification>();
    
    /**
     * Return the set of {@link EntityIdentity} instance for this entity
     */
    private Set<EntityIdentity> identities = 
        new HashSet<EntityIdentity>();
    
    /**
     * Return a set of source {@link EntityRelationship}s that this entity
     * participates in.
     */
    private Set<EntityRelationship> sourceEntityRelationships = 
        new HashSet<EntityRelationship>();
    
    /**
     * Return a set of target {@link EntityRelationship}s that this entity
     * participates in.
     */
    private Set<EntityRelationship> targetEntityRelationships = 
        new HashSet<EntityRelationship>();
    
    /**
     * Default constructor
     */
    public Entity() {
        // do nothing
    }
    
    /**
     * Constructs an instance of a base entity.
     * 
     * @param archetypeId
     *            the archetype id constraining this object
     * @param name
     *            the name of the entity.            
     * @param description
     *            the description of the archetype            
     * @param details
     *            dynamic details of the act.
     */
    public Entity(ArchetypeId archetypeId, String name, 
        String description,  DynamicAttributeMap details) {
        super(archetypeId, name, description);

        // check that a name was specified
        if (StringUtils.isEmpty(name)) {
            throw new EntityException(EntityException.ErrorCode.NoNameSpecified);
        }
        
        this.details = details;
    }

    /**
     * Add a new {@link EntityIdentity}
     * 
     * @param identity
     *            the entity identity to add
     */
    public void addIdentity(EntityIdentity identity) {
        identity.setEntity(this);
        identities.add(identity);
    }

    /**
     * Remove the specified {@link EntityIdentity}
     * 
     * @param identity
     *          the identity to remove
     * @return boolean          
     */
    public boolean removeIdentity(EntityIdentity identity) {
        identity.setEntity(null);
        return(identities.remove(identity));
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
     * @return Returns the sourceEntityRelationships.
     */
    protected Set<EntityRelationship> getSourceEntityRelationships() {
        return sourceEntityRelationships;
    }

    /**
     * @param entityRelationships The sourceEntityRelationships to set.
     */
    protected void setSourceEntityRelationships(Set<EntityRelationship> entityRelationships) {
        this.sourceEntityRelationships = entityRelationships;
    }

    /**
     * Add a source {@link EntityRelationship} to this entity
     * 
     * @param entityRel 
     *            the entity relationship to add
     */
    private void addSourceEntityRelationship(EntityRelationship entityRel) {
        this.sourceEntityRelationships.add(entityRel);
    }
    
    /**
     * Remove the source {@link EntityRelationship} from this entity
     * 
     * @param entityRel
     *            the entity relationship to remove
     */
    private void removeSourceEntityRelationship(EntityRelationship entityRel) {
        this.sourceEntityRelationships.remove(entityRel);
    }
    

    /**
     * @return Returns the targetEntityRelationships.
     */
    protected Set<EntityRelationship> getTargetEntityRelationships() {
        return targetEntityRelationships;
    }
    
    /**
     * @param entityRelationships The targetEntityRelationships to set.
     */
    protected void setTargetEntityRelationships(Set<EntityRelationship> entityRelationships) {
        this.targetEntityRelationships = entityRelationships;
    }
    
    /**
     * Add a tarrget {@link EntityRelationship} to this entity
     * 
     * @param entityRel 
     *            the entity relationship to add
     */
    private void addTargetEntityRelationship(EntityRelationship entityRel) {
        this.targetEntityRelationships.add(entityRel);
    }
    
    /**
     * Remove the tarrget {@link EntityRelationship} from this entity
     * 
     * @param entityRel
     *            the entity relationship to remove
     */
    private void removeTargetEntityRelationship(EntityRelationship entityRel) {
        this.targetEntityRelationships.remove(entityRel);
    }
    
    
    
    /**
     * Add a relationship to this entity. It will determine whether it is a 
     * source or target relationship before adding it.
     * 
     * @param entityRel
     *            the entity relationship to add
     * @throws EntityException
     *            if this relationship cannot be added to this entity            
     */
    public void addEntityRelationship(EntityRelationship entityRel) {
        for (EntityRelationship rel : this.getEntityRelationships()) {
            if (rel.getSource().getLinkId().equals(entityRel.getSource().getLinkId()) &&
                rel.getTarget().getLinkId().equals(entityRel.getTarget().getLinkId()) &&
                rel.getArchetypeId().equals(entityRel.getArchetypeId()) &&
                rel.getActiveEndTime() == null &&
                entityRel.getActiveEndTime() == null) {
                throw new EntityException(
                        EntityException.ErrorCode.DuplicateEntityRelationship,
                        new Object[] { entityRel.getArchetypeId().getShortName()});
            }
        }
        if ((entityRel.getSource().getLinkId().equals(this.getLinkId())) &&
            (entityRel.getSource().getArchetypeId().equals(this.getArchetypeId()))){
            addSourceEntityRelationship(entityRel);
        } else if ((entityRel.getTarget().getLinkId().equals(this.getLinkId())) &&
            (entityRel.getTarget().getArchetypeId().equals(this.getArchetypeId()))){
            addTargetEntityRelationship(entityRel);
        } else {
            throw new EntityException(
                    EntityException.ErrorCode.FailedToAddEntityRelationship,
                    new Object[] { entityRel.getSource(), entityRel.getTarget()});
        }
    }

    /**
     * Remove a relationship to this entity. It will determine whether it is a 
     * source or target relationship before removing it.
     * 
     * @param entityRel
     *            the entity relationship to remove
     */
    public void removeEntityRelationship(EntityRelationship entityRel) {
        if ((entityRel.getSource().getLinkId().equals(this.getLinkId())) &&
            (entityRel.getSource().getArchetypeId().equals(this.getArchetypeId()))){
            removeSourceEntityRelationship(entityRel);
        } else if ((entityRel.getTarget().getLinkId().equals(this.getLinkId())) &&
            (entityRel.getTarget().getArchetypeId().equals(this.getArchetypeId()))){
            removeTargetEntityRelationship(entityRel);
        } else {
            throw new EntityException(
                    EntityException.ErrorCode.FailedToRemoveEntityRelationship,
                    new Object[] { entityRel.getSource(), entityRel.getTarget()});
        }
    }
    
    /**
     * Return all the entity relationships. Do not use the returned set to 
     * add and remove entity relationships. Instead use {@link #addEntityRelationship(EntityRelationship)}
     * and {@link #removeEntityRelationship(EntityRelationship)} repsectively.
     * 
     * @return Set<EntityRelationship>
     */
    public Set<EntityRelationship> getEntityRelationships() {
        Set<EntityRelationship> relationships = 
            new HashSet<EntityRelationship>(sourceEntityRelationships);
        relationships.addAll(targetEntityRelationships);
        
        return relationships;
    }

    /**
     * Add an {@link Classification} to this entity
     * 
     * @param classification 
     *            the classification to add
     */
    public void addClassification(Classification classification) {
        classifications.add(classification);
    }
    
    /**
     * Remove the {@link Classification} from this entity
     * 
     * @param classification
     *            the classification to remove
     */
    public void removeClassification(Classification classification) {
        classifications.remove(classification);
    }
    
    /**
     * Return all the {@link Classification} objects that this entity is 
     * belongs too.
     * 
     * @return Set<Classification>
     */
    public Set<Classification> getClassifications() {
        return this.classifications;
    }
    
    /**
     * @param classifications The classifications to set.
     */
    protected void setClassifications(Set<Classification> classifications) {
        this.classifications = classifications;
    }

    /**
     * @return Returns the details.
     */
    public DynamicAttributeMap getDetails() {
        return details;
    }

    /**
     * @param details The details to set.
     */
    public void setDetails(DynamicAttributeMap details) {
        this.details = details;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Entity copy = (Entity)super.clone();
        copy.classifications = new HashSet<Classification>(this.classifications);
        copy.details = (DynamicAttributeMap)(this.details == null ?
                null : this.details.clone());
        copy.identities = new HashSet<EntityIdentity>(this.identities);
        copy.sourceEntityRelationships = new HashSet<EntityRelationship>(this.sourceEntityRelationships);
        copy.targetEntityRelationships = new HashSet<EntityRelationship>(this.targetEntityRelationships);

        return copy;
    }

    /**
     * @param identities The identities to set.
     */
    protected void setIdentities(Set<EntityIdentity> identities) {
        this.identities = identities;
    }

}
