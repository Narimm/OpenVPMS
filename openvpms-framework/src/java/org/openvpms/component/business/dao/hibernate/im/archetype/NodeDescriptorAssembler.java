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

import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.MapAssembler;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class NodeDescriptorAssembler
        extends IMObjectAssembler<NodeDescriptor, NodeDescriptorDO> {

    private static final MapAssembler<String, NodeDescriptor, NodeDescriptorDO>
            NODES = MapAssembler.create(NodeDescriptor.class,
                                        NodeDescriptorDO.class);

    private static final
    MapAssembler<String, AssertionDescriptor, AssertionDescriptorDO>
            ASSERTION = MapAssembler.create(AssertionDescriptor.class,
                                            AssertionDescriptorDO.class);

    public NodeDescriptorAssembler() {
        super(NodeDescriptor.class, NodeDescriptorDO.class);
    }

    @Override
    protected void assembleDO(NodeDescriptorDO result,
                              NodeDescriptor source,
                              Context context) {
        super.assembleDO(result, source, context);

        result.setArchetypeDescriptor(getDO(source.getArchetypeDescriptor(),
                                            ArchetypeDescriptorDO.class,
                                            context));
        result.setBaseName(source.getBaseName());
        result.setDefaultValue(source.getDefaultValue());
        result.setDerived(source.isDerived());
        result.setDerivedValue(source.getDerivedValue());
        result.setDisplayName(source.getDisplayName());
        result.setFilter(source.getFilter());
        result.setHidden(source.isHidden());
        result.setIndex(source.getIndex());
        result.setMaxCardinality(source.getMaxCardinality());
        result.setMaxLength(source.getMaxLength());
        result.setMinCardinality(source.getMinCardinality());
        result.setMinLength(source.getMinLength());
        result.setParent(getDO(source.getParent(), NodeDescriptorDO.class,
                               context));
        result.setParentChild(source.isParentChild());
        result.setPath(source.getPath());
        result.setReadOnly(source.isReadOnly());
        result.setType(source.getType());

        NODES.assembleDO(result.getNodeDescriptors(),
                         source.getNodeDescriptors(),
                         context);

        ASSERTION.assembleDO(result.getAssertionDescriptors(),
                             source.getAssertionDescriptors(),
                             context);
    }

    @Override
    protected void assembleObject(NodeDescriptor result,
                                  NodeDescriptorDO source, Context context) {
        super.assembleObject(result, source, context);

        result.setArchetypeDescriptor(getObject(source.getArchetypeDescriptor(),
                                                ArchetypeDescriptor.class,
                                                context));
        result.setBaseName(source.getBaseName());
        result.setDefaultValue(source.getDefaultValue());
        result.setDerived(source.isDerived());
        result.setDerivedValue(source.getDerivedValue());
        result.setDisplayName(source.getDisplayName());
        result.setFilter(source.getFilter());
        result.setHidden(source.isHidden());
        result.setIndex(source.getIndex());
        result.setMaxCardinality(source.getMaxCardinality());
        result.setMaxLength(source.getMaxLength());
        result.setMinCardinality(source.getMinCardinality());
        result.setMinLength(source.getMinLength());
        result.setParent(getObject(source.getParent(), NodeDescriptor.class,
                                   context));
        result.setParentChild(source.isParentChild());
        result.setPath(source.getPath());
        result.setReadOnly(source.isReadOnly());
        result.setType(source.getType());

        NODES.assembleObject(result.getNodeDescriptors(),
                             source.getNodeDescriptors(),
                             context);

        ASSERTION.assembleObject(result.getAssertionDescriptors(),
                                 source.getAssertionDescriptors(),
                                 context);
    }

    protected NodeDescriptor create(NodeDescriptorDO object) {
        return new NodeDescriptor();
    }

    protected NodeDescriptorDO create(NodeDescriptor object) {
        return new NodeDescriptorDO();
    }
}
