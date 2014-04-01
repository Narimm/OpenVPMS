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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.business.domain.archetype.ArchetypeId;


/**
 * Implementation of the {@link EntityRelationshipDO} interface.
 *
 * @author Tim Anderson
 */
public class EntityRelationshipDOImpl extends SequencedRelationshipDOImpl implements EntityRelationshipDO {

    /**
     * The entity identity.
     */
    private EntityIdentityDO identity;


    /**
     * Default constructor.
     */
    public EntityRelationshipDOImpl() {
        // do nothing
    }

    /**
     * Creates a new <tt>EntityRelationshipDOImpl</tt>.
     *
     * @param archetypeId the archetype id
     */
    public EntityRelationshipDOImpl(ArchetypeId archetypeId) {
        super(archetypeId);
    }

    /**
     * Returns the entity identity.
     *
     * @return the entity identity. May be <tt>null</tt>.
     */
    public EntityIdentityDO getIdentity() {
        return identity;
    }

    /**
     * Sets the entity identity.
     *
     * @param identity the identity. May be <tt>null</tt>
     */
    public void setIdentity(EntityIdentityDO identity) {
        this.identity = identity;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(null)
                .append("identity", identity)
                .toString();
    }
}
