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

package org.openvpms.component.business.service.archetype.functor;

import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


/**
 * A <tt>Predicate</tt> that evaluates <tt>true</tt> if a node equals the
 * specified value, otherwise evaluates <tt>false</tt>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class NodeEquals implements Predicate {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The node.
     */
    private final String node;

    /**
     * The value to compare.
     */
    private final Object value;

    /**
     * Cached node descriptors.
     */
    private final Map<String, NodeDescriptor> nodes
            = new HashMap<String, NodeDescriptor>();


    /**
     * Creates a new <tt>NodePredicate</tt>, that uses the archetype service
     * returned by {@link ArchetypeServiceHelper#getArchetypeService()}.
     *
     * @param node  the node name
     * @param value the node value
     * @throws ArchetypeServiceException if there is no archetype service
     *                                   registered
     */
    public NodeEquals(String node, Object value) {
        this(node, value, ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>NodePredicate</tt>.
     *
     * @param node    the node name
     * @param value   the value to compare
     * @param service the archetype service
     */
    public NodeEquals(String node, Object value, IArchetypeService service) {
        this.node = node;
        this.value = value;
        this.service = service;
    }

    /**
     * Compares the value of the node of the supplied object with that passed
     * at construnction to determine if they are equal.
     *
     * @param object the object to evaluate. Must be an
     *               {@link IMObject IMObject}
     * @return <tt>true</tt> if the node value equals that supplied at
     *         construction
     * @throws ClassCastException        if the input is the wrong class
     * @throws ArchetypeServiceException for any archetype service error
     */
    public boolean evaluate(Object object) {
        boolean result = false;
        IMObject obj = (IMObject) object;
        NodeDescriptor descriptor = getDescriptor(obj);
        if (descriptor != null) {
            Object other = descriptor.getValue(obj);
            if (value instanceof BigDecimal && other instanceof BigDecimal) {
                result = ((BigDecimal) value).compareTo(
                        (BigDecimal) other) == 0;
            } else {
                result = ObjectUtils.equals(value, other);
            }
        }
        return result;
    }

    /**
     * Returns the node descriptor for the supplied object, caching it
     * to reduce archetype service accesses.
     *
     * @param object the object
     * @return the node descriptor, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    private NodeDescriptor getDescriptor(IMObject object) {
        String shortName = object.getArchetypeId().getShortName();
        NodeDescriptor desc = nodes.get(shortName);
        if (desc == null) {
            ArchetypeDescriptor archetype
                    = DescriptorHelper.getArchetypeDescriptor(shortName,
                                                              service);
            if (archetype != null) {
                desc = archetype.getNodeDescriptor(node);
                if (desc != null) {
                    nodes.put(node, desc);
                }
            }
        }
        return desc;
    }
}
