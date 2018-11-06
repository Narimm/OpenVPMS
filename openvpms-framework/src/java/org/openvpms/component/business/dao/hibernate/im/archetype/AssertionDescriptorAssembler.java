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

package org.openvpms.component.business.dao.hibernate.im.archetype;

import org.openvpms.component.business.dao.hibernate.im.common.Assembler;
import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectAssembler;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;


/**
 * An {@link Assembler} responsible for assembling
 * {@link AssertionDescriptorDO} instances from {@link AssertionDescriptor}s
 * and vice-versa.
 *
 * @author Tim Anderson
 */
public class AssertionDescriptorAssembler extends IMObjectAssembler<AssertionDescriptor, AssertionDescriptorDO> {

    /**
     * The archetype descriptor cache, used to resolve {@code AssertionTypeDescriptor}s by name.
     */
    private final IArchetypeDescriptorCache cache;

    /**
     * Constructs an {@link AssertionDescriptorAssembler}.
     *
     * @param cache the archetype descriptor cache, used to resolve {@code AssertionTypeDescriptor}s by name
     */
    public AssertionDescriptorAssembler(IArchetypeDescriptorCache cache) {
        super(null, AssertionDescriptor.class, AssertionDescriptorDO.class, AssertionDescriptorDOImpl.class);
        this.cache = cache;
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
    protected void assembleDO(AssertionDescriptorDO target, AssertionDescriptor source, DOState state,
                              Context context) {
        super.assembleDO(target, source, state, context);
        target.setErrorMessage(source.getErrorMessage());
        target.setIndex(source.getIndex());
        target.setPropertyMap(source.getPropertyMap());
    }

    /**
     * Assembles an object from a data object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param context the assembly context
     */
    @Override
    protected void assembleObject(AssertionDescriptor target,
                                  AssertionDescriptorDO source,
                                  Context context) {
        super.assembleObject(target, source, context);
        target.setErrorMessage(source.getErrorMessage());
        target.setIndex(source.getIndex());
        target.setPropertyMap(source.getPropertyMap());
        target.setDescriptor(cache.getAssertionTypeDescriptor(target.getName()));
    }

    /**
     * Creates a new object.
     *
     * @param object the source data object
     * @return a new object corresponding to the supplied data object
     */
    protected AssertionDescriptor create(AssertionDescriptorDO object) {
        return new AssertionDescriptor();
    }

    /**
     * Creates a new data object.
     *
     * @param object the source object
     * @return a new data object corresponding to the supplied object
     */
    protected AssertionDescriptorDO create(AssertionDescriptor object) {
        return new AssertionDescriptorDOImpl();
    }
}
