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
import org.openvpms.component.business.dao.hibernate.im.common.MapAssembler;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;

import java.util.Map;


/**
 * An {@link Assembler} responsible for assembling {@link ArchetypeDescriptorDO} instances from
 * {@link ArchetypeDescriptor}s and vice-versa.
 *
 * @author Tim Anderson
 */
public class ArchetypeDescriptorAssembler extends IMObjectAssembler<ArchetypeDescriptor, ArchetypeDescriptorDO> {

    /**
     * Assembles sets of node descriptors.
     */
    private static final MapAssembler<String, NodeDescriptor, NodeDescriptorDO> NODES
            = MapAssembler.create(NodeDescriptor.class);


    /**
     * Constructs an {@link ArchetypeDescriptorAssembler}.
     */
    public ArchetypeDescriptorAssembler() {
        super(org.openvpms.component.model.archetype.ArchetypeDescriptor.class, ArchetypeDescriptor.class,
              ArchetypeDescriptorDO.class, ArchetypeDescriptorDOImpl.class);
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
    @SuppressWarnings("unchecked")
    protected void assembleDO(ArchetypeDescriptorDO target, ArchetypeDescriptor source, DOState state,
                              Context context) {
        super.assembleDO(target, source, state, context);
        target.setClassName(source.getClassName());
        target.setDisplayName(source.getDisplayName());
        target.setLatest(source.isLatest());
        target.setPrimary(source.isPrimary());

        Map nodes = source.getNodeDescriptors();
        NODES.assembleDO(target.getNodeDescriptors(), (Map<String, NodeDescriptor>) nodes, state, context);
    }

    /**
     * Assembles an object from a data object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param context the assembly context
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void assembleObject(ArchetypeDescriptor target, ArchetypeDescriptorDO source, Context context) {
        super.assembleObject(target, source, context);
        target.setClassName(source.getClassName());
        target.setDisplayName(source.getDisplayName());
        target.setLatest(source.isLatest());
        target.setPrimary(source.isPrimary());

        Map nodes = target.getNodeDescriptors();
        NODES.assembleObject((Map<String, NodeDescriptor>) nodes, source.getNodeDescriptors(), context);
    }

    /**
     * Creates a new object.
     *
     * @param object the source data object
     * @return a new object corresponding to the supplied data object
     */
    protected ArchetypeDescriptor create(ArchetypeDescriptorDO object) {
        return new ArchetypeDescriptor();
    }

    /**
     * Creates a new data object.
     *
     * @param object the source object
     * @return a new data object corresponding to the supplied object
     */
    protected ArchetypeDescriptorDO create(ArchetypeDescriptor object) {
        return new ArchetypeDescriptorDOImpl();
    }
}
