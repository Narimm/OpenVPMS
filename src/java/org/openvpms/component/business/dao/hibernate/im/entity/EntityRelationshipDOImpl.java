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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.business.dao.hibernate.im.common.PeriodRelationshipDOImpl;
import org.openvpms.component.business.domain.archetype.ArchetypeId;


/**
 * Implementation of the {@link EntityRelationshipDO} interface.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2008-04-01 14:58:48 +1100 (Tue, 01 Apr 2008) $
 */
public class EntityRelationshipDOImpl extends PeriodRelationshipDOImpl
        implements EntityRelationshipDO {

    /**
     * The entity identity.
     */
    private EntityIdentityDO identity;

    /**
     * The relationship sequence.
     */
    private int sequence;


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
     * Returns the relationship sequence.
     * <p/>
     * This may be used to order relationships.
     *
     * @return the relationship sequence
     */
    public int getSequence() {
        return sequence;
    }

    /**
     * Sets the relationship sequence.
     * <p/>
     * This may be used to order relationships.
     *
     * @param sequence the relationship sequence
     */
    public void setSequence(int sequence) {
        this.sequence = sequence;
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
