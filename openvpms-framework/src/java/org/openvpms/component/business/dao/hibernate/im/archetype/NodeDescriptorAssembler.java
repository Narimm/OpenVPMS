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

package org.openvpms.component.business.dao.hibernate.im.archetype;

import org.openvpms.component.business.dao.hibernate.im.common.Assembler;
import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.MapAssembler;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;


/**
 * An {@link Assembler} responsible for assembling {@link NodeDescriptorDO}
 * instances from {@link NodeDescriptor}s and vice-versa.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class NodeDescriptorAssembler
        extends IMObjectAssembler<NodeDescriptor, NodeDescriptorDO> {

    /**
     * Assembles node descriptors.
     */
    private static final MapAssembler<String, NodeDescriptor, NodeDescriptorDO>
            NODES = MapAssembler.create(NodeDescriptor.class);

    /**
     * Assembles assertion descriptors.
     */
    private static final
    MapAssembler<String, AssertionDescriptor, AssertionDescriptorDO>
            ASSERTION = MapAssembler.create(AssertionDescriptor.class);


    /**
     * Creates a new <tt>NodeDescriptorAssembler</tt>.
     */
    public NodeDescriptorAssembler() {
        super(NodeDescriptor.class, NodeDescriptorDO.class,
              NodeDescriptorDOImpl.class);
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
    protected void assembleDO(NodeDescriptorDO target, NodeDescriptor source,
                              DOState state, Context context) {
        super.assembleDO(target, source, state, context);

        ArchetypeDescriptorDO archetype = null;
        DOState descState = getDO(source.getArchetypeDescriptor(),
                                  context);
        if (descState != null) {
            archetype = (ArchetypeDescriptorDO) descState.getObject();
            state.addState(descState);
        }
        target.setArchetypeDescriptor(archetype);

        NodeDescriptorDO parent = null;
        DOState parentState = getDO(source.getParent(),
                                    context);
        if (parentState != null) {
            parent = (NodeDescriptorDO) parentState.getObject();
            state.addState(parentState);
        }
        target.setParent(parent);

        target.setBaseName(source.getBaseName());
        target.setDefaultValue(source.getDefaultValue());
        target.setDerived(source.isDerived());
        target.setDerivedValue(source.getDerivedValue());
        target.setDisplayName(source.getDisplayName());
        target.setFilter(source.getFilter());
        target.setHidden(source.isHidden());
        target.setIndex(source.getIndex());
        target.setMaxCardinality(source.getMaxCardinality());
        target.setMaxLength(source.getMaxLength());
        target.setMinCardinality(source.getMinCardinality());
        target.setMinLength(source.getMinLength());
        target.setParentChild(source.isParentChild());
        target.setPath(source.getPath());
        target.setReadOnly(source.isReadOnly());
        target.setType(source.getType());

        NODES.assembleDO(target.getNodeDescriptors(),
                         source.getNodeDescriptors(),
                         state, context);

        ASSERTION.assembleDO(target.getAssertionDescriptors(),
                             source.getAssertionDescriptors(),
                             state, context);
    }

    /**
     * Assembles an object from a data object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param context the assembly context
     */
    @Override
    protected void assembleObject(NodeDescriptor target,
                                  NodeDescriptorDO source, Context context) {
        super.assembleObject(target, source, context);

        target.setArchetypeDescriptor(getObject(source.getArchetypeDescriptor(),
                                                ArchetypeDescriptor.class,
                                                context));
        target.setBaseName(source.getBaseName());
        target.setDefaultValue(source.getDefaultValue());
        target.setDerived(source.isDerived());
        target.setDerivedValue(source.getDerivedValue());
        target.setDisplayName(source.getDisplayName());
        target.setFilter(source.getFilter());
        target.setHidden(source.isHidden());
        target.setIndex(source.getIndex());
        target.setMaxCardinality(source.getMaxCardinality());
        target.setMaxLength(source.getMaxLength());
        target.setMinCardinality(source.getMinCardinality());
        target.setMinLength(source.getMinLength());
        target.setParent(getObject(source.getParent(), NodeDescriptor.class,
                                   context));
        target.setParentChild(source.isParentChild());
        target.setPath(source.getPath());
        target.setReadOnly(source.isReadOnly());
        target.setType(source.getType());

        NODES.assembleObject(target.getNodeDescriptors(),
                             source.getNodeDescriptors(),
                             context);

        ASSERTION.assembleObject(target.getAssertionDescriptors(),
                                 source.getAssertionDescriptors(),
                                 context);
    }

    /**
     * Creates a new object.
     *
     * @param object the source data object
     * @return a new object corresponding to the supplied data object
     */
    protected NodeDescriptor create(NodeDescriptorDO object) {
        return new NodeDescriptor();
    }

    /**
     * Creates a new data object.
     *
     * @param object the source object
     * @return a new data object corresponding to the supplied object
     */
    protected NodeDescriptorDO create(NodeDescriptor object) {
        return new NodeDescriptorDOImpl();
    }
}
