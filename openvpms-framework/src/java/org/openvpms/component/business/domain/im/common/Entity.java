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

// java core
import java.util.HashSet;
import java.util.Set;

// commons-lang
import org.apache.commons.lang.StringUtils;

// log4j
import org.apache.log4j.Logger;

//openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;


/**
 * A class representing all namned things in the business.
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
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Entity.class);

    /**
     * This is the name that this entity is known by. Each concrete instance 
     * must supply this.
     */
    private String name;
    
    /**
     * Description of this entity
     */
    private String description;
    
    /**
     * A placeholder for all entity details, which denotes the dynamic and
     * adaptive details of the entity.
     */
    private DynamicAttributeMap details;
    
    /**
     * Return a set of {@link EntityClassification} for this entity. An 
     * {@link Entity} can belong to zero, one or more {@link Classification}
     * 
     */
    private Set<EntityClassification> classifications;
    
    /**
     * Return the set of {@link EntityIdentity} instance for this entity
     */
    private Set<EntityIdentity> identities;
    
    /**
     * Return a set of {@link Participation} that this entity is part off
     */
    private Set<Participation> participations;
    
    /**
     * Return a set of {@link EntityRelationships} that this entity is the 
     * source off.
     */
    private Set<EntityRelationship> sourceRelationships;
    
    /**
     * The {@link EntityRelationship} instances where this entity is a
     * target
     */
    private Set<EntityRelationship> targetRelationships;

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
        super(archetypeId);

        // check that a name was specified
        if (StringUtils.isEmpty(name)) {
            throw new EntityException(EntityException.ErrorCode.NoNameSpecified);
        }
        
        this.name = name;
        this.description = description;
        this.identities = new HashSet<EntityIdentity>();
        this.classifications = new HashSet<EntityClassification>();
        this.participations = new HashSet<Participation>();
        this.sourceRelationships = new HashSet<EntityRelationship>();
        this.targetRelationships = new HashSet<EntityRelationship>();
        this.details = details;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param participations The participations to set.
     */
    public void setParticipations(Set<Participation> participations) {
        this.participations = participations;
    }

    /**
     * @return Returns the targetRelationships.
     */
    public Set<EntityRelationship> getTargetRelationships() {
        return targetRelationships;
    }

    /**
     * @param targetRelationships The targetRelationships to set.
     */
    public void setTargetRelationships(Set<EntityRelationship> targetRelationships) {
        this.targetRelationships = targetRelationships;
    }
    
    /**
     * Add a new {@link EntityIdentity}
     * 
     * @param entityIdentity
     *            the entity identity to add
     */
    public void addEntityIdentity(EntityIdentity entityIdentity) {
        entityIdentity.setEntity(this);
        identities.add(entityIdentity);
    }

    /**
     * Remove the specified {@link EntityIdentity}
     * 
     * @param entityIdentity
     *          the identity to remove
     * @return boolean          
     */
    public boolean removeEntityIdentity(EntityIdentity entityIdentity) {
        entityIdentity.setEntity(null);
        return(identities.remove(entityIdentity));
    }
    
    /**
     * Return the {@link EntityIdentity} as an array
     * 
     * @return EntityIdentity[]
     */
    public EntityIdentity[] getEntityIdentities() {
        return (EntityIdentity[])identities.toArray(
                new EntityIdentity[identities.size()]);
    }

    /**
     * Add an {@link EntityRelationship} to this entity
     * 
     * @param entityRel 
     *            the entity relationship to add
     */
    public void addSourceEntityRelationship(EntityRelationship entityRel) {
        entityRel.setSourceEntity(this);
        this.sourceRelationships.add(entityRel);
    }
    
    /**
     * Remove the {@link EntityRelationship} from this entity
     * 
     * TODO Don't think we need the remove and add source entity relationship
     * methods.
     * 
     * @oparam entityRel
     *            the entity relationship to remove
     */
    public void removeSourceEntityRelationship(EntityRelationship entityRel) {
        entityRel.setSourceEntity(null);
        this.sourceRelationships.remove(entityRel);
    }
    
    /**
     * Return all the {@link EntityRelationship} objects that this entity is 
     * the source off
     * 
     * @return EntityRelationship[]
     */
    public EntityRelationship[] getSourceEntityRelationships() {
        return (EntityRelationship[])sourceRelationships.toArray(
                new EntityRelationship[sourceRelationships.size()]);
    }
    
    /**
     * Add a target {@link EntityRelationship} to this entity
     * 
     * @param entityRel 
     *            the entity relationship to add
     */
    public void addTargetEntityRelationship(EntityRelationship entityRel) {
        entityRel.setTargetEntity(this);
        this.targetRelationships.add(entityRel);
    }
    
    /**
     * Remove the target {@link EntityRelationship} from this entity
     * 
     * TODO Don't think we need the remove and add source entity relationship
     * methods.
     * 
     * @oparam entityRel
     *            the entity relationship to remove
     */
    public void removeTargetEntityRelationship(EntityRelationship entityRel) {
        entityRel.setTargetEntity(null);
        this.targetRelationships.remove(entityRel);
    }
    
    /**
     * Return all the target {@link EntityRelationship} for this entity
     * 
     * @return EntityRelationship[]
     */
    public EntityRelationship[] getTargetEntityRelationships() {
        return (EntityRelationship[])targetRelationships.toArray(
                new EntityRelationship[targetRelationships.size()]);
    }
    
    /**
     * Add an {@link EntityClassification} to this entity
     * 
     * @param entityClass 
     *            the entity classification to add
     */
    public void addEntityClassification(EntityClassification entityClass) {
        entityClass.setEntity(this);
        this.classifications.add(entityClass);
    }
    
    /**
     * Remove the {@link EntityClassification} from this entity
     * 
     * @oparam entityClass
     *            the entity classification to remove
     */
    public void removeEntityClassification(EntityClassification entityClass) {
        entityClass.setEntity(null);
        this.classifications.remove(entityClass);
    }
    
    /**
     * Return all the {@link EntityClassification} objects that this entity is 
     * the source off
     * 
     * @return EntityClassification[]
     */
    public EntityClassification[] getClassifications() {
        return (EntityClassification[])classifications.toArray(
                new EntityClassification[classifications.size()]);
    }
    
    /**
     * Add this entity to the specified participation
     * 
     * @param participation
     *            the participation to add  
     */
    public void addParticipation(Participation participation) {
        participation.setEntity(this);
        participations.add(participation);
    }
    
    /**
     * Remove the {@link Participation} from this entity
     * 
     * @oparam participation
     *            the entity classification to remove
     */
    public boolean removeParticipation(Participation participation) {
        participation.setEntity(null);
        return participations.remove(participation);
    }
    
    /**
     * Return all the {@link Participation} objects that this entity is 
     * the associated with.
     * 
     * @return Participation[]
     */
    public Participation[] getParticipations() {
        return (Participation[])participations.toArray(
                new Participation[participations.size()]);
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

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

}
