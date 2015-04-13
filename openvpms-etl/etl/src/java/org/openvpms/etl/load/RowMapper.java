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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.etl.load;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.openvpms.etl.load.LoaderException.ErrorCode.ArchetypeNotFound;
import static org.openvpms.etl.load.LoaderException.ErrorCode.InvalidMapping;
import static org.openvpms.etl.load.LoaderException.ErrorCode.MissingRowValue;
import static org.openvpms.etl.load.LoaderException.ErrorCode.NullReference;


/**
 * Maps an {@link ETLRow} to one or more {@link IMObject} instances, using
 * mappings defined by an {@link Mappings}.
 *
 * @author Tim Anderson
 */
class RowMapper {

    /**
     * The mappings.
     */
    private final Mappings mappings;

    /**
     * The object resolver.
     */
    private final ObjectHandler handler;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The current legacy row identifier.
     */
    private String rowId;

    /**
     * The nodes to map, keyed on input field name.
     */
    private final Map<String, Node> nodes = new HashMap<String, Node>();

    /**
     * Mapped objects, keyed on object {@link Node#getObjectPath}, in
     * the order they were mapped.
     */
    private Map<String, IMObject> objects
            = new LinkedHashMap<String, IMObject>();

    /**
     * Lookup codes for the current row.
     */
    private Map<NodeDescriptor, CodeName> lookups
            = new HashMap<NodeDescriptor, CodeName>();

    /**
     * Map of archetype short names and their corresponding nodes descriptors,
     * cached for performance reasons.
     */
    private final Map<String, Map<String, NodeDescriptor>> descriptors
            = new HashMap<String, Map<String, NodeDescriptor>>();

    /**
     * Lookup handler.
     */
    private LookupHandler lookupHandler;


    /**
     * Constructs a new <tt>RowMapper</tt>.
     *
     * @param mappings      the row mappings
     * @param handler       the object handler
     * @param lookupHandler the lookup handler, or <tt>null</tt> if lookups
     *                      aren't being generated
     * @param service       the archetype service
     */
    public RowMapper(Mappings mappings, ObjectHandler handler,
                     LookupHandler lookupHandler, IArchetypeService service) {
        this.mappings = mappings;
        this.handler = handler;
        this.service = service;

        for (Mapping mapping : mappings.getMapping()) {
            String target = mapping.getTarget();
            Node node = NodeParser.parse(target);
            if (node == null) {
                throw new LoaderException(InvalidMapping, target);
            }
            nodes.put(target, node);

            // cache node descriptors
            while (node != null) {
                String shortName = node.getArchetype();
                if (!descriptors.containsKey(shortName)) {
                    ArchetypeDescriptor archetype
                            = service.getArchetypeDescriptor(shortName);
                    if (archetype == null) {
                        throw new LoaderException(ArchetypeNotFound, shortName);
                    }
                    Map<String, NodeDescriptor> nodes
                            = new HashMap<String, NodeDescriptor>();
                    for (NodeDescriptor descriptor :
                            archetype.getAllNodeDescriptors()) {
                        nodes.put(descriptor.getName(), descriptor);
                    }
                    descriptors.put(shortName, nodes);
                }
                node = node.getChild();
            }
        }
        this.lookupHandler = lookupHandler;
    }

    /**
     * Maps a row to one or more objects.
     *
     * @param row the row to map
     * @return the mapped objects
     * @throws LoaderException           for any loader error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<IMObject> map(ETLRow row) {
        objects.clear();
        lookups.clear();
        rowId = row.getRowId();
        for (Mapping mapping : mappings.getMapping()) {
            if (!row.exists(mapping.getSource())) {
                throw new LoaderException(MissingRowValue, mapping.getSource());
            }
            Object value = row.get(mapping.getSource());
            if (!mapping.getExcludeNull() || value != null) {
                mapValue(value, mapping, row);
            }
        }
        if (lookupHandler != null && !lookups.isEmpty()) {
            lookupHandler.add(lookups);
        }
        List<IMObject> result = new ArrayList<IMObject>(objects.values());
        objects.clear();
        return result;
    }

    /**
     * Maps a value.
     *
     * @param value   the value to map
     * @param mapping the mapping
     * @param row     the row being mapped
     * @throws LoaderException           for any loader error
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void mapValue(Object value, Mapping mapping, ETLRow row) {
        Node node = nodes.get(mapping.getTarget());
        Node parentNode;
        IMObject object = getObject(node, null, null, mapping, row);
        while (node.getChild() != null) {
            parentNode = node;
            node = node.getChild();
            object = getObject(node, parentNode, object, mapping, row);
        }
        NodeDescriptor descriptor = getNode(node.getArchetype(),
                                            node.getName());
        if (descriptor.isObjectReference()) {
            String targetValue = getStringValue(value, mapping);
            descriptor.setValue(object, handler.getReference(targetValue));
        } else if (descriptor.isCollection()) {
            String targetValue = getStringValue(value, mapping);
            descriptor.addChildToCollection(object,
                                            handler.getObject(targetValue));
        } else if (lookupHandler != null && descriptor.isLookup() && lookupHandler.isGeneratedLookup(descriptor)) {
            String targetValue = getStringValue(value, mapping);
            if (targetValue != null) {
                String code = lookupHandler.getCode(targetValue);
                descriptor.setValue(object, code);
                lookups.put(descriptor, new CodeName(code, targetValue));
            }
        } else {
            Object targetValue = getValue(value, mapping);
            descriptor.setValue(object, targetValue);
        }
    }

    /**
     * Gets an object for the specified node.
     * If the node references an existing object, this will be returned,
     * otherwise the object will be created.
     *
     * @param node       the node
     * @param parentNode the parent node. May be <tt>null</tt>
     * @param parent     the parent object. May be <tt>null</tt>
     * @param mapping    the mapping
     * @param row        the row being mapped
     * @return the object corresponding to the node
     */
    private IMObject getObject(Node node, Node parentNode, IMObject parent,
                               Mapping mapping, ETLRow row) {
        IMObject object;
        object = objects.get(node.getObjectPath());
        if (object == null) {
            if (node.getField() != null) {
                object = getObject(node, row);
            } else {
                object = create(node, mapping);
            }
            objects.put(node.getObjectPath(), object);
            int index = (parent != null) ? parentNode.getIndex() : -1;
            if (parent == null) {
                handler.add(rowId, object, index);
            } else {
                String archetype = parent.getArchetypeId().getShortName();
                String name = parentNode.getName();
                NodeDescriptor descriptor = getNode(archetype, name);
                if (descriptor.isCollection()) {
                    descriptor.addChildToCollection(parent, object);
                } else if (descriptor.isObjectReference()) {
                    descriptor.setValue(parent,
                                        object.getObjectReference());
                    handler.add(rowId, object, index);
                } else {
                    descriptor.setValue(parent, object);
                }
            }
        }
        return object;
    }

    /**
     * Returns an object identified by reference.
     *
     * @param node the node containing the reference
     * @param row  the row being mapped
     * @return the object corresponding to the reference
     */
    private IMObject getObject(Node node, ETLRow row) {
        Object value = row.get(node.getField());
        if (value == null) {
            throw new LoaderException(NullReference, node.getArchetype(),
                                      node.getField());
        }
        String ref = Reference.create(node.getArchetype(), value.toString());
        return handler.getObject(ref);
    }

    /**
     * Creates a new {@link IMObject}.
     *
     * @param node    the node
     * @param mapping the node mapping
     * @return a new object, or <tt>null</tt> if there is no corresponding
     *         archetype descriptor for <tt>archetype</tt>
     * @throws LoaderException           if an archetype short name is invalid
     * @throws ArchetypeServiceException if the object can't be created
     */
    protected IMObject create(Node node, Mapping mapping) {
        IMObject result;
        result = service.create(node.getArchetype());
        if (result == null) {
            throw new LoaderException(ArchetypeNotFound, node.getArchetype());
        }
        if (mapping.getRemoveDefaultObjects()) {
            Map<String, NodeDescriptor> nodes
                    = descriptors.get(node.getArchetype());
            for (NodeDescriptor nodeDesc : nodes.values()) {
                if (nodeDesc.isCollection()) {
                    IMObject[] children = nodeDesc.getChildren(result).toArray(
                            new IMObject[nodeDesc.getChildren(result).size()]);
                    for (IMObject child : children) {
                        nodeDesc.removeChildFromCollection(result, child);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns a value based on the corresponding {@link Mapping}.
     *
     * @param object  the object
     * @param mapping the mapping
     * @return the value
     */
    private Object getValue(Object object, Mapping mapping) {
        Object result;
        if (!StringUtils.isEmpty(mapping.getValue())) {
            String value = mapping.getValue();
            if (object != null) {
                result = LoaderHelper.replaceValue(value, object.toString());
            } else {
                result = value;
            }
        } else {
            result = object;
        }
        return result;
    }

    /**
     * Returns a stringified version of an object.
     * If the mapping specifies a value, any occurrences of <em>$value</em>
     * are replaced with <tt>value</tt> and the result returned.
     *
     * @param object  the value to transform
     * @param mapping the mapping
     * @return the transformed value
     */
    private String getStringValue(Object object, Mapping mapping) {
        Object result = getValue(object, mapping);
        return (result != null) ? result.toString() : null;
    }

    /**
     * Helper to get the named node descriptor from an archetype.
     *
     * @param archetype the archetype
     * @param name      the node name
     * @return the corresponding node descriptor
     */
    private NodeDescriptor getNode(String archetype, String name) {
        Map<String, NodeDescriptor> nodes = descriptors.get(archetype);
        return nodes.get(name);
    }

}
