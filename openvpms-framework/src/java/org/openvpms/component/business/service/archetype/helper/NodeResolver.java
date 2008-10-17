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

package org.openvpms.component.business.service.archetype.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import static org.openvpms.component.business.service.archetype.helper.NodeResolverException.ErrorCode.InvalidNode;
import static org.openvpms.component.business.service.archetype.helper.NodeResolverException.ErrorCode.InvalidObject;
import org.openvpms.component.system.common.exception.OpenVPMSException;

import java.util.List;


/**
 * Resolves node values given a root <code>IMObject</code> and a field name of
 * the form <em>node1.node2.nodeN</em>.
 * <p/>
 * Where a node refers to an <code>IMObjectReference</code>, or a collection
 * of 0..1 IMObject instances, these will be treated as an <code>IMObject</code>.
 * e.g. given an <code>Act</code> object, with archetype act.customerEstimation,
 * the field name <em>customer.entity.name</em>, this will:
 * <li>
 * <ul>get the <code>Participation</code> instance corresponding to the
 * "customer" node</ul>
 * <ul>get the Entity instance corresponding to the "entity" node of the
 * Participation</ul>
 * <ul>get the value of the "name" node of the entity.</ul>
 * </li>
 * <p/>
 * Several special node names are defined:
 * <ul>
 * <li><em>shortName</em> - returns the value of the archetypes short name</li>
 * <li><em>displayName</em> - returns the value of the archetypes display
 * name</li>
 * </ul>
 * These are only evaluated when they appear as leaf nodes and the archetype
 * corresponding to the leaf has doesn't define the node.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class NodeResolver {

    /**
     * The root object.
     */
    private final IMObject root;

    /**
     * The archetype descriptor.
     */
    private final ArchetypeDescriptor archetype;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The logger.
     */
    private static final Log _log = LogFactory.getLog(NodeResolver.class);


    /**
     * Construct a new <code>NodeResolver</code>.
     *
     * @param root    the root object
     * @param service the archetype service
     */
    public NodeResolver(IMObject root, IArchetypeService service) {
        this.root = root;
        archetype = service.getArchetypeDescriptor(root.getArchetypeId());
        this.service = service;

    }

    /**
     * Returns the archetype of the root object.
     *
     * @return the archetype of the root object
     */
    public ArchetypeDescriptor getArchetype() {
        return archetype;
    }

    /**
     * Returns the object associated with a field name.
     *
     * @return the object corresponding to <code>name</code>
     * @throws NodeResolverException if the name is invalid
     */
    public Object getObject(String name) {
        return resolve(name).getValue();
    }

    /**
     * Resolves the node state corresponding to a field name.
     *
     * @param name the field name
     * @return the resolved state
     * @throws NodeResolverException if the name is invalid
     */
    public State resolve(String name) {
        State state;
        IMObject object = root;
        ArchetypeDescriptor archetype = this.archetype;
        int index;
        while ((index = name.indexOf(".")) != -1) {
            String nodeName = name.substring(0, index);
            NodeDescriptor node = archetype.getNodeDescriptor(nodeName);
            if (node == null) {
                throw new NodeResolverException(InvalidNode, name);
            }
            Object value = getValue(object, node);
            if (value == null) {
                // object missing.
                object = null;
                break;
            } else if (!(value instanceof IMObject)) {
                throw new NodeResolverException(InvalidObject, name);
            }
            object = (IMObject) value;
            archetype = service.getArchetypeDescriptor(
                    object.getArchetypeId());
            name = name.substring(index + 1);
        }
        if (object != null) {
            NodeDescriptor leafNode = archetype.getNodeDescriptor(name);
            Object value;
            if (leafNode == null) {
                if ("displayName".equals(name)) {
                    value = archetype.getDisplayName();
                } else if ("shortName".equals(name)) {
                    value = object.getArchetypeId().getShortName();
                } else if ("uid".equals(name)) {
                    value = object.getId();
                } else {
                    throw new NodeResolverException(InvalidNode, name);
                }
            } else {
                value = getValue(object, leafNode);
            } 
            state = new State(object, archetype, name, leafNode, value);
        } else {
            state = new State();
        }
        return state;
    }

    /**
     * Returns the value of a node, converting any object references or
     * single element arrays to their corresponding IMObject instance.
     *
     * @param parent     the parent object
     * @param descriptor the node descriptor
     */
    private Object getValue(IMObject parent, NodeDescriptor descriptor) {
        Object result;
        if (descriptor.isObjectReference()) {
            result = getObject(parent, descriptor);
        } else if (descriptor.isCollection() &&
                descriptor.getMaxCardinality() == 1) {
            List<IMObject> values = descriptor.getChildren(parent);
            result = (!values.isEmpty()) ? values.get(0) : null;
        } else {
            result = descriptor.getValue(parent);
        }
        return result;
    }

    /**
     * Resolve a reference.
     *
     * @param parent     the parent object
     * @param descriptor the reference descriptor
     */
    private IMObject getObject(IMObject parent, NodeDescriptor descriptor) {
        IMObjectReference ref = (IMObjectReference) descriptor.getValue(parent);
        if (ref != null) {
            try {
                return service.get(ref);
            } catch (OpenVPMSException exception) {
                _log.warn(exception, exception);
            }
        }
        return null;
    }

    public static class State {

        /**
         * The parent of the leaf node. Null if the parent can't be determined.
         */
        private final IMObject parent;

        /**
         * The archetype descriptor of <code>_parent</code>.
         */
        private final ArchetypeDescriptor archetype;

        /**
         * The leaf name corresponding to the last element in a composite name.
         */
        private final String leafName;

        /**
         * The node descriptor of the leaf name.
         */
        private final NodeDescriptor leafNode;

        /**
         * The value of the leaf node.
         */
        private final Object value;


        public State() {
            this(null, null, null, null, null);
        }

        public State(IMObject parent, ArchetypeDescriptor archetype,
                     String leafName, NodeDescriptor leafNode, Object value) {
            this.parent = parent;
            this.archetype = archetype;
            this.leafName = leafName;
            this.leafNode = leafNode;
            this.value = value;
        }

        /**
         * Returns the parent of the leaf node.
         *
         * @return the parent of the leaf node, or <code>null</code> if it
         *         couldn't be determined
         */
        public IMObject getParent() {
            return parent;
        }

        /**
         * Returns the archetype of the parent of the leaf node.
         *
         * @return the parent archetype
         */
        public ArchetypeDescriptor getParentArchetype() {
            return archetype;
        }

        /**
         * The leaf name corresponding to the last element in a composite name.
         *
         * @return the leaf name
         */
        public String getLeafName() {
            return leafName;
        }

        /**
         * The leaf node descriptor corresponding to the last element in the name.
         *
         * @return the node descriptor, or <code>null</code> if there is no match
         */
        public NodeDescriptor getLeafNode() {
            return leafNode;
        }

        /**
         * Returns the value of the leaf node.
         *
         * @return the value of the leaf node
         */
        public Object getValue() {
            return value;
        }

    }

}
