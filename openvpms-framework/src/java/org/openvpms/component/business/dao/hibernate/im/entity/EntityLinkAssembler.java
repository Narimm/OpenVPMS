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

import org.openvpms.component.business.domain.im.common.EntityLink;


/**
 * Assembles {@link EntityLink}s from {@link EntityLinkDO}s and vice-versa.
 *
 * @author Tim Anderson
 */
public class EntityLinkAssembler extends SequencedRelationshipAssembler<EntityLink, EntityLinkDO> {

    /**
     * Constructs an {@link EntityLinkAssembler}.
     */
    public EntityLinkAssembler() {
        super(EntityLink.class, EntityLinkDO.class, EntityLinkDOImpl.class, EntityDO.class, EntityDOImpl.class);
    }

    /**
     * Creates a new object.
     *
     * @param object the source data object
     * @return a new object corresponding to the supplied data object
     */
    protected EntityLink create(EntityLinkDO object) {
        return new EntityLink();
    }

    /**
     * Creates a new data object.
     *
     * @param object the source object
     * @return a new data object corresponding to the supplied object
     */
    protected EntityLinkDO create(EntityLink object) {
        return new EntityLinkDOImpl();
    }
}
