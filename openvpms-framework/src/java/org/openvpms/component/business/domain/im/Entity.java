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

package org.openvpms.component.business.domain.im;

// java core
import java.util.HashSet;
import java.util.Set;

//openehr kernel
import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datatypes.text.DvText;


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
     * Description of this entity
     */
    private String description;
    
    /**
     * A placeholder for all entity details, which denotes the dynamic and
     * adaptive details of the entity.
     */
    private ItemStructure details;
    
    /**
     * Return a set of {@link EntityClassification} for this entity. An 
     * {@link Entity} can belong to zero, one or more {@link Classification}
     * 
     */
    private Set<EntityClassification> entityClassifications;
    
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
     * @param uid
     *            uniquely identifies this object
     * @param archetypeId
     *            the archietype that is constraining this object
     * @param imVersion
     *            the version of the reference model
     * @param archetypeNodeId
     *            the id of this node                        
     * @param name
     *            the name 
     * @param details
     *            details associated with the archetype.
     * @throws IllegalArgumentException
     *            thrown if the preconditions are not met.
     */
    @FullConstructor
    public Entity(
            @Attribute(name = "uid", required=true) String uid, 
            @Attribute(name = "archetypeId", required=true) String archetypeId, 
            @Attribute(name = "imVersion", required=true) String imVersion, 
            @Attribute(name = "archetypeNodeId", required = true) String archetypeNodeId, 
            @Attribute(name = "name", required = true) DvText name, 
            @Attribute(name = "description") String description,
            @Attribute(name = "details") ItemStructure details) {
        super(uid, archetypeId, imVersion, archetypeNodeId, name);
        this.description = description;
        this.identities = new HashSet<EntityIdentity>();
        this.entityClassifications = new HashSet<EntityClassification>();
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
     * @return Returns the participations.
     */
    public Set<Participation> getParticipations() {
        return participations;
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
        this.identities.add(entityIdentity);
    }

    /**
     * Remove the specified {@link EntityIdentity}
     * 
     * @param entityIdentity
     *          the identity to remove
     */
    public void removeEntityIdentity(EntityIdentity entityIdentity) {
        entityIdentity.setEntity(null);
        this.identities.remove(entityIdentity);
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
        this.entityClassifications.add(entityClass);
    }
    
    /**
     * Remove the {@link EntityClassification} from this entity
     * 
     * @oparam entityClass
     *            the entity classification to remove
     */
    public void removeEntityClassification(EntityClassification entityClass) {
        entityClass.setEntity(null);
        this.entityClassifications.remove(entityClass);
    }
    
    /**
     * Return all the {@link EntityClassification} objects that this entity is 
     * the source off
     * 
     * @return EntityClassification[]
     */
    public EntityClassification[] getEntityClassifications() {
        return (EntityClassification[])entityClassifications.toArray(
                new EntityClassification[entityClassifications.size()]);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.openehr.rm.common.archetyped.Locatable#pathOfItem(org.openehr.rm.common.archetyped.Locatable)
     */
    @Override
    public String pathOfItem(Locatable item) {
        // TODO Implement this method
        return null;
    }

    /**
     * @return Returns the details.
     */
    public ItemStructure getDetails() {
        return details;
    }

    /**
     * @param details The details to set.
     */
    public void setDetails(ItemStructure details) {
        this.details = details;
    }
}
