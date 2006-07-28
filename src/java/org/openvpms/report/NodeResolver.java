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

package org.openvpms.report;

import net.sf.jasperreports.engine.JRException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
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
 * Currently, a single 'magic' node-name of <em>displayName</em> is defined.
 * When encountered as a leaf node, and the archetype corrresponding to the
 * leaf has no "displayName" node, this returns the value of the archetypes
 * display name.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class NodeResolver {

    /**
     * The root object.
     */
    private final IMObject _root;

    /**
     * The archetype descriptor.
     */
    private final ArchetypeDescriptor _archetype;

    /**
     * The archetype service.
     */
    private final IArchetypeService _service;

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
        _root = root;
        _archetype = service.getArchetypeDescriptor(root.getArchetypeId());
        _service = service;

    }

    /**
     * Returns the archetype of the root object.
     *
     * @return the archetype of the root object
     */
    public ArchetypeDescriptor getArchetype() {
        return _archetype;
    }

    /**
     * Returns the object associated with a field name.
     *
     * @return the object corresponding to <code>name</code>
     * @throws JRException if the name is invalid
     */
    public Object getObject(String name) throws JRException {
        return resolve(name).getValue();
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
     * Resolves the node state corresponding to a field name.
     *
     * @param name the field name
     * @return the resolved state
     * @throws IMObjectReportException if the name is invalid
     */
    public State resolve(String name) {
        State state;
        IMObject object = _root;
        ArchetypeDescriptor archetype = _archetype;
        int index;
        while ((index = name.indexOf(".")) != -1) {
            String nodeName = name.substring(0, index);
            NodeDescriptor node = archetype.getNodeDescriptor(nodeName);
            if (node == null) {
                throw new IMObjectReportException("Name doesn't refer to a valid node: "
                        + name);
            }
            Object value = getValue(object, node);
            if (value == null) {
                // object missing.
                break;
            } else if (!(value instanceof IMObject)) {
                throw new IMObjectReportException(
                        "Name doesn't refer to an object reference: " + name);
            }
            object = (IMObject) value;
            archetype = _service.getArchetypeDescriptor(
                    object.getArchetypeId());
            name = name.substring(index + 1);
        }
        if (object != null) {
            NodeDescriptor leafNode = archetype.getNodeDescriptor(name);
            Object value = null;
            if (leafNode == null && "displayName".equals(name)) {
                value = archetype.getDisplayName();
            } else if (leafNode != null) {
                value = getValue(object, leafNode);
            }
            state = new State(object, archetype, name, leafNode, value);
        } else {
            state = new State();
        }
        return state;
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
                return ArchetypeQueryHelper.getByObjectReference(_service, ref);
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
        private final IMObject _parent;

        /**
         * The archetype descriptor of <code>_parent</code>.
         */
        private final ArchetypeDescriptor _archetype;

        /**
         * The leaf name corresponding to the last element in a composite name.
         */
        private final String _leafName;

        /**
         * The node descriptor of the leaf name.
         */
        private final NodeDescriptor _leafNode;

        /**
         * The value of the leaf node.
         */
        private final Object _value;


        public State() {
            this(null, null, null, null, null);
        }

        public State(IMObject parent, ArchetypeDescriptor archetype,
                     String leafName, NodeDescriptor leafNode, Object value) {
            _parent = parent;
            _archetype = archetype;
            _leafName = leafName;
            _leafNode = leafNode;
            _value = value;
        }

        /**
         * Returns the parent of the leaf node.
         *
         * @return the parent of the leaf node, or <code>null</code> if it
         *         couldn't be determined
         */
        public IMObject getParent() {
            return _parent;
        }

        /**
         * Returns the archetype of the parent of the leaf node.
         *
         * @return the parent archetype
         */
        public ArchetypeDescriptor getParentArchetype() {
            return _archetype;
        }

        /**
         * The leaf name corresponding to the last element in a composite name.
         *
         * @return the leaf name
         */
        public String getLeafName() {
            return _leafName;
        }

        /**
         * The leaf node descriptor corresponding to the last element in the name.
         *
         * @return the node descriptor, or <code>null</code> if there is no match
         */
        public NodeDescriptor getLeafNode() {
            return _leafNode;
        }

        /**
         * Returns the value of the leaf node.
         *
         * @return the value of the leaf node
         */
        public Object getValue() {
            return _value;
        }

    }

}
