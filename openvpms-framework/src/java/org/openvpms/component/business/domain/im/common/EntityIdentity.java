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

import java.util.Map;


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
     * Serialization version identifier.
     */
    private static final long serialVersionUID = 2L;

    /**
     * The system identity
     */
    private String identity;

    /**
     * Reference the Entity that this object references
     */
    private Entity entity;


    /**
     * Default constructor.
     */
    public EntityIdentity() {
        // do nothing
    }

    /**
     * Creates a new <tt>EntityIdentity</tt>.
     *
     * @param archetypeId the archetype id
     * @param identity    the identity
     */
    public EntityIdentity(ArchetypeId archetypeId, String identity) {
        super(archetypeId);
        setIdentity(identity);
    }

    /**
     * Constructs a valid instance of an entity identity.
     *
     * @param archetypeId the archetype id constraining this object
     * @param identity    the identity
     * @param details     the details of this entity identity
     * @throws IllegalArgumentException thrown if the preconditions are not met.
     */
    @Deprecated
    public EntityIdentity(ArchetypeId archetypeId,
                          String identity, Map<String, Object> details) {
        this(archetypeId, identity);
        if (details != null) {
            setDetails(details);
        }
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
        return super.clone();
    }
}
