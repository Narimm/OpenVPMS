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
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Entity.class);

    /**
     * An alternative name for this object, unique wirhin the domain of
     * objects it belongs too.
     */
    private String code;
    
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
     * Return a set of {@link Participation} that this entity is part off
     */
    private Set<Participation> participations = 
        new HashSet<Participation>();
    
    /**
     * Return a set of {@link EntityRelationships} that this entity participates
     * in.
     */
    private Set<EntityRelationship> entityRelationships = 
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
     * @return Returns the code.
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code The code to set.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @param participations The participations to set.
     */
    public void setParticipations(Set<Participation> participations) {
        this.participations = participations;
    }

    /**
     * @return Returns the entityRelationships.
     */
    public Set<EntityRelationship> getEntityRelationships() {
        return entityRelationships;
    }

    /**
     * @param targetRelationships The entityRelationships to set.
     */
    public void setEntityRelationships(Set<EntityRelationship> entityRelationships) {
        this.entityRelationships = entityRelationships;
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
     * Add an {@link EntityRelationship} to this entity
     * 
     * @param entityRel 
     *            the entity relationship to add
     */
    public void addEntityRelationship(EntityRelationship entityRel) {
        entityRel.setEntity(this);
        this.entityRelationships.add(entityRel);
    }
    
    /**
     * Remove the {@link EntityRelationship} from this entity
     * 
     * @param entityRel
     *            the entity relationship to remove
     */
    public void removeEntityRelationship(EntityRelationship entityRel) {
        entityRel.setEntity(null);
        this.entityRelationships.remove(entityRel);
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
     * @oparam classification
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
    @SuppressWarnings("unused")
    private void setClassifications(Set<Classification> classifications) {
        this.classifications = classifications;
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
    public Set<Participation> getParticipations() {
        return participations;
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
     * @param identities The identities to set.
     */
    @SuppressWarnings("unused")
    public void setIdentities(Set<EntityIdentity> identities) {
        this.identities = identities;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Entity copy = (Entity)super.clone();
        copy.classifications = new HashSet<Classification>(this.classifications);
        copy.code = this.code;
        copy.details = (DynamicAttributeMap)(this.details == null ?
                null : this.details.clone());
        copy.identities = new HashSet<EntityIdentity>(this.identities);
        copy.participations = new HashSet<Participation>(this.participations);
        copy.entityRelationships = new HashSet<EntityRelationship>(this.entityRelationships);

        return copy;
    }

}
