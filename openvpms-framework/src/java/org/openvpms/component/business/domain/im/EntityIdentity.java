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

// openehr-kernel
import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datatypes.text.DvText;

/**
 * A class representing the various internal and external identifiers for a 
 * particular entity including associations to a particular Entity Relationship.  
 * For example a Product Entity could be related to a Supplier entity with a 
 * Entity Relationship type of "Supplies" which has a associated identification 
 * number representing the suppliers product code.  
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EntityIdentity extends IMObject {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 2603291478704772424L;

    /**
     * The system identity
     */
    private String identity;
    
    /**
     * Holds details about the entity identity
     */
    private ItemStructure details;
    
    /**
     * Reference the Entity that this object references
     */
    private Entity entity;
    
    /**
     * Default constructor
     */
    public EntityIdentity() {
        // do nothing
    }

    /**
     * Constructs a valid instance of an entity identity.
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
     * @param identity
     *            the identity
     * @param details
     *            the details of this entty identity
     * @throws IllegalArgumentException
     *             thrown if the preconditions are not met.
     */
    @FullConstructor
    public EntityIdentity(
            @Attribute(name = "uid", required=true) String uid, 
            @Attribute(name = "archetypeId", required=true) String archetypeId, 
            @Attribute(name = "imVersion", required=true) String imVersion, 
            @Attribute(name = "archetypeNodeId", required = true) String archetypeNodeId, 
            @Attribute(name = "name", required = true) DvText name, 
            @Attribute(name = "identity") String identity,
            @Attribute(name = "details") ItemStructure details) {
        super(uid, archetypeId, imVersion, archetypeNodeId, name);
        this.identity = identity;
        this.details = details;
    }
    
    /**
     * @return Returns the identity.
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * @param identity The identity to set.
     */
    public void setIdentity(String identity) {
        this.identity = identity;
    }

    /**
     * @return Returns the details.
     */
    public ItemStructure getDetails() {
        return details;
    }

    /**
     * @param details
     *            The details to set.
     */
    public void setDetails(ItemStructure details) {
        this.details = details;
    }
    
    /**
     * @return Returns the entity.
     */
    protected Entity getEntity() {
        return entity;
    }

    /**
     * @param entity The entity to set.
     */
    protected void setEntity(Entity entity) {
        this.entity = entity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openehr.rm.common.archetyped.Locatable#pathOfItem(org.openehr.rm.common.archetyped.Locatable)
     */
    @Override
    public String pathOfItem(Locatable item) {
        // TODO Auto-generated method stub
        return null;
    }

}
