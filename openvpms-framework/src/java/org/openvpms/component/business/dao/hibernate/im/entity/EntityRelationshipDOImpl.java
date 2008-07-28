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
 * Describes the relationship between two entities.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2008-04-01 14:58:48 +1100 (Tue, 01 Apr 2008) $
 */
public class EntityRelationshipDOImpl extends PeriodRelationshipDOImpl
        implements EntityRelationshipDO {

    /**
     * A relationship may also have an associated entity identity.
     */
    private EntityIdentityDO identity;


    /**
     * Default constructor.
     */
    public EntityRelationshipDOImpl() {
        // do nothing
    }

    /**
     * Creates a new <tt>EntityRelationshipDO</tt>.
     *
     * @param archetypeId the archetype id
     */
    public EntityRelationshipDOImpl(ArchetypeId archetypeId) {
        super(archetypeId);
    }

    /**
     * @return Returns the entityIdentity.
     */
    public EntityIdentityDO getIdentity() {
        return identity;
    }

    /**
     * @param identity The identity to set.
     */
    public void setIdentity(EntityIdentityDO identity) {
        this.identity = identity;
    }

    /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(null)
                .append("identity", identity)
                .toString();
    }
}
