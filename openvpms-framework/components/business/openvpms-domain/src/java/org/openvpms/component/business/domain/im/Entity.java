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
import java.util.Set;

//openehr java kernel
import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;
import org.openehr.rm.common.archetyped.Archetyped;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.identification.ObjectID;

/**
 * A class representing all namned things in the business.
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class Entity extends Locatable {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = -1885076845700353861L;

    /**
     * Description of this entity
     */
    private DvText description;
    
    /**
     * A placeholder for all entity details, which denotes the dynamic and
     * adaptive details of the entity.
     */
    private ItemStructure details;
    
    /**
     * Return a set of {@link Classification} for this entity
     * 
     * TODO We can probably remove this attribute from the class since
     * it is now part of {@link EntityClassification}.
     */
    private Set<Classification> classifications;
    
    /**
     * Return the set of {@link EntityIdentity} instance for this entity
     */
    private Set<EntityIdentity> identities;
    
    /**
     * Return a set of {@link Participation} that this entity is part off
     */
    private Set<Participation> participations;
    
    /**
     * Return a set of {@link EntityRelationships} that this entity is part 
     * off.
     */
    private Set<EntityRelationship> relationships;

    /**
     * Constructs an instance of a base entity.
     * 
     * @param uid
     *            a unique object identity
     * @param archetypeNodeId
     *            the node id for this archetype
     * @param name
     *            the name of this archetype
     * @param archetypeDetails
     *            descriptive meta data for the achetype
     * @param details
     *            a compound item that describes the details of this
     *            archetype.
     * @throws IllegalArgumentException
     *            thrown if the preconditions are not met.
     */
    @FullConstructor
    public Entity(
            @Attribute(name = "uid", required = true) ObjectID uid, 
            @Attribute(name = "archetypeNodeId", required = true) String archetypeNodeId, 
            @Attribute(name = "name", required = true)DvText name, 
            @Attribute(name = "archetypeDetails") Archetyped archetypeDetails, 
            @Attribute(name = "details") ItemStructure details) {
        super(uid, archetypeNodeId, name, archetypeDetails, null, null);
        this.details = details;
    }

    /**
     * @return Returns the description.
     */
    public DvText getDescription() {
        return description;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(DvText description) {
        this.description = description;
    }

    /**
     * @return Returns the classifications.
     */
    public Set<Classification> getClassifications() {
        return classifications;
    }

    /**
     * @param classifications The classifications to set.
     */
    public void setClassifications(Set<Classification> classifications) {
        this.classifications = classifications;
    }

    /**
     * @return Returns the identities.
     */
    public Set<EntityIdentity> getIdentities() {
        return identities;
    }

    /**
     * @param identities The identities to set.
     */
    public void setIdentities(Set<EntityIdentity> identities) {
        this.identities = identities;
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
     * @return Returns the relationships.
     */
    public Set<EntityRelationship> getRelationships() {
        return relationships;
    }

    /**
     * @param relationships The relationships to set.
     */
    public void setRelationships(Set<EntityRelationship> relationships) {
        this.relationships = relationships;
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
