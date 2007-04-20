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
 *
 *  $Id$
 */

package org.openvpms.etl.load;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import static org.openvpms.etl.load.LoaderException.ErrorCode.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Maps an {@link ETLRow} to one or more {@link IMObject} instances, using
 * mappings defined by an {@link Mappings}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
            if (!mapping.getExcludeNull()
                    || (mapping.getExcludeNull() && value != null)) {
                mapValue(value, mapping);
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
     * @throws LoaderException           for any loader error
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void mapValue(Object value, Mapping mapping) {
        Node node = nodes.get(mapping.getTarget());
        Node parentNode;
        IMObject object = getObject(node, null, null, mapping);
        while (node.getChild() != null) {
            parentNode = node;
            node = node.getChild();
            object = getObject(node, parentNode, object, mapping);
        }
        ArchetypeDescriptor archetype
                = service.getArchetypeDescriptor(node.getArchetype());
        if (archetype == null) {
            throw new LoaderException(ArchetypeNotFound, node.getArchetype());
        }
        String name = node.getName();
        NodeDescriptor descriptor = archetype.getNodeDescriptor(name);
        if (descriptor == null) {
            throw new LoaderException(InvalidNode, node.getArchetype(), name);
        }
        if (descriptor.isObjectReference()) {
            String targetValue = getStringValue(value, mapping);
            descriptor.setValue(object, handler.getReference(targetValue));
        } else if (descriptor.isCollection()) {
            String targetValue = getStringValue(value, mapping);
            descriptor.addChildToCollection(object,
                                            handler.getObject(targetValue));
        } else if (lookupHandler != null && descriptor.isLookup()) {
            String targetValue = getStringValue(value, mapping);
            if (targetValue != null) {
                String code = lookupHandler.getCode(targetValue);
                descriptor.setValue(object, code);
                if (lookupHandler.isGeneratedLookup(descriptor)) {
                    lookups.put(descriptor, new CodeName(code, targetValue));
                }
            }
        } else {
            Object targetValue = getValue(value, mapping);
            descriptor.setValue(object, targetValue);
        }
    }

    /**
     * Gets an object for the specified node, creating it if it doesn't
     * exist.
     *
     * @param node       the node
     * @param parentNode the parent node. May be <tt>null</tt>
     * @param parent     the parent object. May be <tt>null</tt>
     * @param mapping    the mapping
     * @return the object corresponding to the node
     */
    private IMObject getObject(Node node, Node parentNode, IMObject parent,
                               Mapping mapping) {
        IMObject object = objects.get(node.getObjectPath());
        if (object == null) {
            object = create(node, mapping);
            objects.put(node.getObjectPath(), object);
            int index = (parent != null) ? parentNode.getIndex() : -1;
            if (parent == null) {
                handler.add(rowId, object, index);
            } else {
                IMObjectBean bean = new IMObjectBean(parent, service);
                String name = parentNode.getName();
                if (!bean.hasNode(name)) {
                    throw new LoaderException(InvalidNode, node.getArchetype(),
                                              name);
                }
                NodeDescriptor descriptor = bean.getDescriptor(name);
                if (descriptor.isCollection()) {
                    bean.addValue(name, object);
                } else if (descriptor.isObjectReference()) {
                    bean.setValue(name, object.getObjectReference());
                    handler.add(rowId, object, index);
                } else {
                    bean.setValue(name, object);
                }
            }
        }
        return object;
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
            ArchetypeDescriptor descriptor
                    = DescriptorHelper.getArchetypeDescriptor(result,
                                                              service);
            for (NodeDescriptor nodeDesc : descriptor.getAllNodeDescriptors()) {
                if (nodeDesc.isCollection()) {
                    IMObject[] children = nodeDesc.getChildren(result).toArray(
                            new IMObject[0]);
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

}
