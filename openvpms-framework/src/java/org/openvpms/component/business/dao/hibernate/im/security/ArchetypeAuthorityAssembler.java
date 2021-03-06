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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.dao.hibernate.im.security;

import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectAssembler;
import org.openvpms.component.business.domain.im.security.ArchetypeAwareGrantedAuthority;


/**
 * Assembles {@link ArchetypeAwareGrantedAuthority}s from {@link ArchetypeAuthorityDO}s and vice-versa.
 *
 * @author Tim Anderson
 */
public class ArchetypeAuthorityAssembler
        extends IMObjectAssembler<ArchetypeAwareGrantedAuthority, ArchetypeAuthorityDO> {

    /**
     * Constructs an {@link ArchetypeAuthorityAssembler}.
     */
    public ArchetypeAuthorityAssembler() {
        super(null, ArchetypeAwareGrantedAuthority.class, ArchetypeAuthorityDO.class, ArchetypeAuthorityDOImpl.class);
    }

    /**
     * Assembles a data object from an object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param state   the data object state
     * @param context the assembly context
     */
    @Override
    protected void assembleDO(ArchetypeAuthorityDO target,
                              ArchetypeAwareGrantedAuthority source,
                              DOState state, Context context) {
        super.assembleDO(target, source, state, context);
        target.setMethod(source.getMethod());
        target.setServiceName(source.getServiceName());
        target.setMethod(source.getMethod());
        target.setShortName(source.getShortName());
    }

    /**
     * Assembles an object from a data object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param context the assembly context
     */
    @Override
    protected void assembleObject(ArchetypeAwareGrantedAuthority target,
                                  ArchetypeAuthorityDO source,
                                  Context context) {
        super.assembleObject(target, source, context);
        target.setMethod(source.getMethod());
        target.setServiceName(source.getServiceName());
        target.setMethod(source.getMethod());
        target.setShortName(source.getShortName());
    }

    /**
     * Creates a new object.
     *
     * @param object the source data object
     * @return a new object corresponding to the supplied data object
     */
    protected ArchetypeAwareGrantedAuthority create(ArchetypeAuthorityDO object) {
        return new ArchetypeAwareGrantedAuthority();
    }

    /**
     * Creates a new data object.
     *
     * @param object the source object
     * @return a new data object corresponding to the supplied object
     */
    protected ArchetypeAuthorityDO create(ArchetypeAwareGrantedAuthority object) {
        return new ArchetypeAuthorityDOImpl();
    }
}
