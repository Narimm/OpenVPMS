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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.function.list;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.Pointer;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.NodeResolver;
import org.openvpms.component.business.service.archetype.helper.PropertyResolverException;
import org.openvpms.component.business.service.archetype.helper.sort.IMObjectSorter;
import org.openvpms.component.business.service.lookup.ILookupService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Functions operating on lists of {@link IMObject}s.
 *
 * @author Tim Anderson
 */
public class ListFunctions {

    /**
     * Default separator.
     */
    private static final String SEPARATOR = ", ";

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The sorter.
     */
    private final IMObjectSorter sorter;


    /**
     * Constructs an {@link ListFunctions}.
     *
     * @param service the archetype service
     * @param lookups the lookups
     */
    public ListFunctions(IArchetypeService service, ILookupService lookups) {
        this.service = service;
        sorter = new IMObjectSorter(service, lookups);
    }

    /**
     * Sorts objects on a node.
     *
     * @param objects the objects to sort. These are not modified
     * @param node    the node to sort on
     * @return the sorted objects
     */
    public <T extends IMObject> List<T> sort(Iterable<T> objects, String node) {
        return sorter.sort(toList(objects), node, true);
    }

    /**
     * Concatenates object names, comma separated.
     *
     * @param objects the objects
     * @return the concatenated names
     */
    public <T extends IMObject> String names(Iterable<T> objects) {
        return names(objects, SEPARATOR);
    }

    /**
     * Concatenates object names, with the separator string between names.
     *
     * @param objects   the objects
     * @param separator the separator
     * @return the concatenated names
     */
    public <T extends IMObject> String names(Iterable<T> objects, String separator) {
        return join(objects, "name", separator);
    }

    /**
     * Convenience function to sort and concatenate the object names of the specified node, separated by commas.
     *
     * @param context the expression context. Must refer to an {@link IMObject}.
     * @param node    the collection node name
     * @return the concatenated names, or {@code null} if the context doesn't refer to an {@link IMObject}
     */
    public String sortNamesOf(ExpressionContext context, String node) {
        Pointer pointer = context.getContextNodePointer();
        Object value = pointer.getValue();
        if (value instanceof IMObject) {
            IMObjectBean bean = new IMObjectBean((IMObject) value, service);
            return sortNames(bean.getValues(node, IMObject.class));
        }
        return null;
    }

    /**
     * Convenience function to sort and concatenate the names of the specified objects, separated by commas.
     *
     * @param objects the objects
     * @return the concatenated names
     */
    public <T extends IMObject> String sortNames(Iterable<T> objects) {
        return sortNames(objects, SEPARATOR);
    }

    /**
     * Convenience function to sort and concatenate the names of the specified objects, with the separator between
     * names.
     *
     * @param objects   the objects
     * @param separator the separator
     * @return the concatenated names
     */
    public <T extends IMObject> String sortNames(Iterable<T> objects, String separator) {
        return names(sort(objects, "name"), separator);
    }

    /**
     * Iterates through a list of objects, joining the string value of the specified node.
     *
     * @param objects the objects
     * @param node    the node name
     * @return the concatenated node
     */
    public <T extends IMObject> String join(Iterable<T> objects, String node) {
        return join(objects, node, SEPARATOR);
    }

    /**
     * Iterates through a list of objects, joining the string value of the specified node.
     *
     * @param objects   the objects
     * @param node      the node name
     * @param separator the separator
     * @return the concatenated node
     */
    public <T extends IMObject> String join(Iterable<T> objects, String node, String separator) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (T object : objects) {
            if (i > 0) {
                builder.append(separator);
            }
            NodeResolver resolver = new NodeResolver(object, service);
            try {
                Object value = resolver.getObject(node);
                builder.append(value);
            } catch (PropertyResolverException ignore) {
                // do nothing
            }
            ++i;
        }
        return builder.toString();
    }

    /**
     * Returns the values of a collection node.
     *
     * @param context the expression context. Must refer to an {@link IMObject}.
     * @param node    the node name. May be a composite node
     * @return the collection values
     */
    public List<Object> values(ExpressionContext context, String node) {
        Pointer pointer = context.getContextNodePointer();
        Object value = pointer.getValue();
        if (value instanceof IMObject) {
            return values((IMObject) value, node);
        }
        return Collections.emptyList();
    }

    /**
     * Returns the values of a collection node.
     *
     * @param object the object
     * @param node   the node name. May be a composite node
     * @return the collection values
     */
    public List<Object> values(IMObject object, String node) {
        List<Object> result = new ArrayList<>();
        values(object, node, result);
        return result;
    }

    /**
     * Returns the distinct values of a collection node.
     *
     * @param context the expression context. Must refer to an {@link IMObject}.
     * @param node    the node name. May be a composite node
     * @return the distinct collection values
     */
    public List<Object> distinct(ExpressionContext context, String node) {
        Pointer pointer = context.getContextNodePointer();
        Object value = pointer.getValue();
        if (value instanceof IMObject) {
            return distinct((IMObject) value, node);
        }
        return Collections.emptyList();
    }

    /**
     * Returns the distinct values of a collection node.
     *
     * @param object the object
     * @param node   the node name. May be a composite node
     * @return the distinct collection values
     */
    public List<Object> distinct(IMObject object, String node) {
        LinkedHashSet<Object> result = new LinkedHashSet<>();
        values(object, node, result);
        return new ArrayList<>(result);
    }

    /**
     * Collects the values of a collection node.
     *
     * @param object the object
     * @param node   the node name. May be a composite node
     * @param result the collected values
     */
    protected void values(IMObject object, String node, Collection<Object> result) {
        List<IMObject> parents = new ArrayList<>();
        parents.add(object);
        String[] nodes = node.split("\\.");
        for (int i = 0; i < nodes.length; ++i) {
            boolean last = (i + 1 == nodes.length);
            List<IMObject> children = new ArrayList<>();
            for (IMObject parent : parents) {
                IMObjectBean bean = new IMObjectBean(parent, service);
                String name = nodes[i];
                NodeDescriptor descriptor = bean.getDescriptor(name);
                if (descriptor == null) {
                    throw new PropertyResolverException(PropertyResolverException.ErrorCode.InvalidProperty, name);
                }
                if (descriptor.isCollection()) {
                    for (IMObject child : bean.getValues(name)) {
                        add(child, last, result, children, name);
                    }
                } else {
                    Object child = bean.getValue(name);
                    add(child, last, result, children, name);
                }
            }
            parents = children;
        }
    }

    /**
     * Adds a value to a collection.
     * <p/>
     * If the value is a reference, the corresponding object is retrieved.
     *
     * @param value    the value to add
     * @param last     determines if the last node is being retrieved
     * @param result   the result collection, if its the last node
     * @param children the children collection, used if the last node is not being retrieved
     * @param name     the node name, for error reporting purposes
     */
    private void add(Object value, boolean last, Collection<Object> result, List<IMObject> children, String name) {
        if (value instanceof IMObjectReference) {
            value = service.get((IMObjectReference) value);
        }
        if (value != null) {
            if (last) {
                result.add(value);
            } else if (value instanceof IMObject) {
                children.add((IMObject) value);
            } else {
                // not the last node in the composite node, but not an IMObject
                throw new PropertyResolverException(PropertyResolverException.ErrorCode.InvalidProperty, name);
            }
        }
    }

    /**
     * Helper to convert an iterable to a list.
     *
     * @param objects the iterable
     * @return a list
     */
    protected <T extends IMObject> List<T> toList(Iterable<T> objects) {
        List<T> list;
        if (objects instanceof Collection) {
            list = new ArrayList<>((Collection<T>) objects);
        } else {
            list = new ArrayList<>();
            CollectionUtils.addAll(list, objects);
        }
        return list;
    }

}
