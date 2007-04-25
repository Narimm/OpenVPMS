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

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.datatypes.basic.StringMap;

import java.util.Map;
import java.util.HashMap;


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
    private static final long serialVersionUID = 1L;

    /**
     * The system identity
     */
    private String identity;

    /**
     * Holds details about the entity identity
     */
    private Map<String, Object> details;

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
     * @param archetypeId
     *            the archetype id constraining this object
     * @param identity
     *            the identity
     * @param details
     *            the details of this entity identity
     * @throws IllegalArgumentException
     *             thrown if the preconditions are not met.
     */
    public EntityIdentity(ArchetypeId archetypeId,
                          String identity, Map<String, Object> details) {
        super(archetypeId);
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
    public Map<String, Object> getDetails() {
        return new StringMap(details);
    }

    /**
     * @param details
     *            The details to set.
     */
    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    /**
     * @return Returns the entity.
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * @param entity The entity to set.
     */
    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        EntityIdentity copy = (EntityIdentity)super.clone();
        copy.details = (details == null) ? null : new HashMap<String, Object>(details);
        copy.entity = this.entity;
        copy.identity = this.identity;

        return copy;
    }
}
