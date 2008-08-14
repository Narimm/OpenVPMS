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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.openvpms.component.business.dao.im.common.ResultCollector;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Abstract implementation of the {@link ResultCollector} interface,
 * used to collect nodes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
abstract class AbstractNodeResultCollector<T>
        extends HibernateResultCollector<T> {

    /**
     * The archetype descriptor cache.
     */
    private final IArchetypeDescriptorCache cache;

    /**
     * The names of the nodes to collect.
     */
    private final Collection<String> names;

    /**
     * Constructs a new <tt>AbstractNodeResultCollector</tt>.
     *
     * @param cache the archetype descriptor cache
     * @param nodes the names of the nodes to collect
     */
    public AbstractNodeResultCollector(IArchetypeDescriptorCache cache,
                                       Collection<String> nodes) {
        this.cache = cache;
        this.names = nodes;
    }

    /**
     * Returns node descriptors corresponding to those named at construction.
     *
     * @param object the object
     * @return node descriptors for the object
     */
    protected List<NodeDescriptor> getDescriptors(IMObject object) {
        List<NodeDescriptor> result;
        String shortName = object.getArchetypeId().getShortName();
        ArchetypeDescriptor archetype = cache.getArchetypeDescriptor(
                shortName);
        if (archetype == null) {
            result = Collections.emptyList();
        } else {
            result = new ArrayList<NodeDescriptor>();
            for (String name : names) {
                NodeDescriptor node = archetype.getNodeDescriptor(name);
                if (node != null) {
                    result.add(node);
                }
            }
        }
        return result;
    }

    /**
     * Loads the value of a node.
     *
     * @param descriptor the node descriptor
     * @param object     the object
     * @return the value of the node
     */
    protected Object loadValue(NodeDescriptor descriptor, IMObject object) {
        Object value = descriptor.getValue(object);
        if (value instanceof Collection) {
            for (Object elt : (Collection) value) {
                if (elt instanceof IMObject) {
                    getLoader().load(elt);
                }
            }
        }
        return value;
    }

}
