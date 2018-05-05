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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.function.list;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.NodeResolver;
import org.openvpms.component.business.service.archetype.helper.PropertyResolver;
import org.openvpms.component.business.service.archetype.helper.PropertySetResolver;
import org.openvpms.component.business.service.archetype.helper.sort.IMObjectSorter;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.jxpath.AbstractObjectFunctions;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Functions operating on lists of {@link IMObject}s.
 *
 * @author Tim Anderson
 */
public class ListFunctions extends AbstractObjectFunctions {

    /**
     * Default separator.
     */
    private static final String SEPARATOR = ", ";

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The sorter.
     */
    private final IMObjectSorter sorter;


    /**
     * Constructs a {@link ListFunctions}.
     *
     * @param service the archetype service
     * @param lookups the lookups
     */
    public ListFunctions(IArchetypeService service, ILookupService lookups) {
        super("list");
        setObject(this);
        this.service = service;
        this.lookups = lookups;
        sorter = new IMObjectSorter(service, lookups);
    }

    /**
     * Returns the first object in a collection.
     *
     * @param objects the objects. May be a collection of {@link IMObject} or {@link PropertySet}
     * @return the first object, or {@code null} if the collection is empty
     */
    public Object first(Iterable<Object> objects) {
        if (objects != null) {
            Iterator<Object> iterator = ((Iterable) objects).iterator();
            return iterator.hasNext() ? iterator.next() : null;
        }
        return null;
    }

    /**
     * Returns the first object in a collection.
     *
     * @param object the root object. May be an {@link IMObject} or {@link PropertySet}, or a collection of these
     * @param name   the node name. May be a composite node
     * @return the first object, or {@code null} if the collection is empty
     */
    public Object first(Object object, String name) {
        List<Object> objects = values(object, name);
        return !objects.isEmpty() ? objects.get(0) : null;
    }

    /**
     * Sorts objects on a node.
     *
     * @param objects the objects to sort. These are not modified
     * @param node    the node to sort on
     * @return the sorted objects
     */
    public <T extends IMObject> List<T> sort(Iterable<T> objects, String node) {
        return sorter.sort(asList(objects), node, true);
    }

    /**
     * Concatenates object names, comma separated.
     *
     * @param objects the objects. May be an {@link IMObject}, {@link PropertySet}, or a collection of these
     * @return the concatenated names
     */
    public String names(Object objects) {
        return names(objects, SEPARATOR);
    }

    /**
     * Concatenates object names, with the separator string between names.
     *
     * @param objects   the objects. May be an {@link IMObject}, {@link PropertySet}, or a collection of these
     * @param separator the separator
     * @return the concatenated names
     */
    public String names(Object objects, String separator) {
        return join(objects, "name", separator);
    }

    /**
     * Concatenates object names, with the separator string between names.
     * <p/>
     * The last separator is used to separate the list two names in the list e.g. given {@code separator=',' and
     * lastSeparator='and'}:<br/>
     * {@code apple, orange, pear and grapefruit}
     *
     * @param objects   the objects. May be an {@link IMObject}, {@link PropertySet}, or a collection of these
     * @param separator the separator
     * @return the concatenated names
     */
    public String names(Object objects, String separator, String lastSeparator) {
        return names(objects, separator, lastSeparator, null, null);
    }

    /**
     * Concatenates object names, with the separator string between names.
     * <p/>
     * The last separator is used to separate the list two names in the list e.g. given {@code separator=',' and
     * lastSeparator='and'}:<br/>
     * {@code apple, orange, pear and grapefruit}
     *
     * @param objects   the objects. May be an {@link IMObject}, {@link PropertySet}, or a collection of these
     * @param separator the separator
     * @param singular  if non-null, is appended to the string if there is only a single name
     * @param plural    if non-null, is appended to the string if there are multiple names
     * @return the concatenated names
     */
    public String names(Object objects, String separator, String lastSeparator, String singular, String plural) {
        return join(objects, "name", separator, lastSeparator, singular, plural);
    }

    /**
     * Convenience function to sort and concatenate the object names of the specified property, separated by commas.
     *
     * @param context the context. May be an {@link IMObject}, {@link PropertySet}, or a collection of these
     * @param name    the property name
     * @return the concatenated names, or {@code null} if the context doesn't refer to an {@link IMObject}
     */
    public String sortNamesOf(ExpressionContext context, String name) {
        return sortNamesOf(context.getContextNodePointer().getValue(), name);
    }

    /**
     * Convenience function to sort and concatenate the object names of the specified property, separated by commas.
     *
     * @param objects the objects. May be an {@link IMObject}, {@link PropertySet}, or a collection of these
     * @param name    the property name
     * @return the concatenated names, or {@code null} if the context doesn't refer to an {@link IMObject}
     */
    public String sortNamesOf(Object objects, String name) {
        return sortNamesOf(objects, name, SEPARATOR);
    }

    /**
     * Convenience function to sort and concatenate the object names of the specified property, separated by the
     * specified separator.
     *
     * @param objects   the objects. May be an {@link IMObject}, {@link PropertySet}, or a collection of these
     * @param name      the property name
     * @param separator the separator
     * @return the concatenated names, or {@code null} if the context doesn't refer to an {@link IMObject}
     */
    public String sortNamesOf(Object objects, String name, String separator) {
        return sortNamesOf(objects, name, separator, separator);
    }

    /**
     * Convenience function to sort and concatenate the object names of the specified property, separated by the
     * specified separator.
     *
     * @param objects       the objects. May be an {@link IMObject}, {@link PropertySet}, or a collection of these
     * @param name          the property name
     * @param separator     the separator
     * @param lastSeparator the last separator
     * @return the concatenated names, or {@code null} if the context doesn't refer to an {@link IMObject}
     */
    public String sortNamesOf(Object objects, String name, String separator, String lastSeparator) {
        return sortNamesOf(objects, name, separator, lastSeparator, null, null);
    }

    /**
     * Convenience function to sort and concatenate the object names of the specified property, separated by the
     * specified separator.
     *
     * @param objects       the objects. May be an {@link IMObject}, {@link PropertySet}, or a collection of these
     * @param name          the property name
     * @param separator     the separator
     * @param lastSeparator the last separator
     * @param singular      if non-null, is appended to the string if there is only a single name
     * @param plural        if non-null, is appended to the string if there are multiple names
     * @return the concatenated names, or {@code null} if the context doesn't refer to an {@link IMObject}
     */
    public String sortNamesOf(Object objects, String name, String separator, String lastSeparator, String singular,
                              String plural) {
        List<Object> values = values(objects, name);
        return sortNames(values, separator, lastSeparator, singular, plural);
    }

    /**
     * Convenience function to sort and concatenate the names of the specified objects, separated by commas.
     *
     * @param objects the objects. May be a collection of {@link IMObject} or {@link PropertySet}
     * @return the concatenated names
     */
    public String sortNames(Object objects) {
        return sortNames(objects, SEPARATOR);
    }

    /**
     * Convenience function to sort and concatenate the names of the specified objects, with the separator between
     * names.
     *
     * @param objects   the objects. May be an {@link IMObject}, {@link PropertySet}, or a collection of these
     * @param separator the separator
     * @return the concatenated names
     */
    public String sortNames(Object objects, String separator) {
        return sortNames(objects, separator, separator);
    }

    /**
     * Convenience function to sort and concatenate the names of the specified objects, with the separator between
     * names.
     *
     * @param objects       the objects. May be an {@link IMObject}, {@link PropertySet}, or a collection of these
     * @param separator     the separator
     * @param lastSeparator the last separator
     * @return the concatenated names
     */
    public String sortNames(Object objects, String separator, String lastSeparator) {
        return sortNames(objects, separator, lastSeparator, null, null);
    }

    /**
     * Convenience function to sort and concatenate the names of the specified objects, with the separator between
     * names.
     *
     * @param objects       the objects. May be an {@link IMObject}, {@link PropertySet}, or a collection of these
     * @param separator     the separator
     * @param lastSeparator the last separator
     * @param singular      if non-null, is appended to the string if there is only a single name
     * @param plural        if non-null, is appended to the string if there are multiple names
     * @return the concatenated names
     */
    public String sortNames(Object objects, String separator, String lastSeparator, String singular, String plural) {
        return join(objects, "name", separator, lastSeparator, singular, plural, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                String v1 = (o1 != null) ? o1.toString() : null;
                String v2 = (o2 != null) ? o2.toString() : null;
                return ObjectUtils.compare(v1, v2);
            }
        });
    }

    /**
     * Iterates through a list of objects, joining the string value of the specified node.
     *
     * @param objects the objects. May be a collection of {@link IMObject} or {@link PropertySet}
     * @param node    the node name
     * @return the concatenated node
     */
    public String join(Object objects, String node) {
        return join(objects, node, SEPARATOR);
    }

    /**
     * Iterates through a list of objects, joining the string value of the specified node.
     *
     * @param objects   the objects. May be a collection of {@link IMObject} or {@link PropertySet}
     * @param name      the node name
     * @param separator the separator
     * @return the concatenated node
     */
    public String join(Object objects, String name, String separator) {
        return join(objects, name, separator, separator);
    }

    /**
     * Iterates through a list of objects, joining the string value of the specified property.
     *
     * @param objects       the objects. May be a collection of {@link IMObject} or {@link PropertySet}
     * @param name          the property name
     * @param separator     the separator
     * @param lastSeparator the last separator
     * @return the concatenated node
     */
    public String join(Object objects, String name, String separator, String lastSeparator) {
        return join(objects, name, separator, lastSeparator, null, null);
    }

    /**
     * Iterates through a list of objects, joining the string value of the specified property.
     *
     * @param objects       the objects. May be a collection of {@link IMObject} or {@link PropertySet}
     * @param name          the property name
     * @param separator     the separator
     * @param lastSeparator the last separator
     * @param singular      if non-null, is appended to the string if there is only a single string
     * @param plural        if non-null, is appended to the string if there are multiple strings
     * @return the concatenated node
     */
    public String join(Object objects, String name, String separator, String lastSeparator, String singular,
                       String plural) {
        return join(objects, name, separator, lastSeparator, singular, plural, null);
    }

    /**
     * Returns the unique values of a collection.
     *
     * @param context the expression context. May refer to an {@link IMObject}, {@link PropertySet}, or a collection
     *                of these
     * @param name    the node name. May be a composite node
     * @return the collection values
     */
    public Set<Object> set(ExpressionContext context, String name) {
        Pointer pointer = context.getContextNodePointer();
        Object value = pointer.getValue();
        return set(value, name);
    }

    /**
     * Returns the unique values of a collection.
     *
     * @param object the expression context. May refer to an {@link IMObject}, {@link PropertySet}, or a collection
     *               of these
     * @param name   the node name. May be a composite node
     * @return the collection values
     */
    public Set<Object> set(Object object, String name) {
        return collect(object, name, new LinkedHashSet<>());
    }

    /**
     * Returns the values of a collection.
     *
     * @param context the expression context. May refer to an {@link IMObject}, {@link PropertySet}, or a collection
     *                of these
     * @param name    the node name. May be a composite node
     * @return the collection values
     */
    public List<Object> values(ExpressionContext context, String name) {
        Pointer pointer = context.getContextNodePointer();
        Object value = pointer.getValue();
        return values(value, name);
    }

    /**
     * Returns the values of a collection.
     *
     * @param object the expression context. May refer to an {@link IMObject}, {@link PropertySet}, or a collection
     *               of these
     * @param name   the node name. May be a composite node
     * @return the collection values
     */
    public List<Object> values(Object object, String name) {
        return collect(object, name, new ArrayList<>());
    }

    /**
     * Returns the distinct values of a collection node.
     *
     * @param context the expression context. May refer to an {@link IMObject}, {@link PropertySet}, or a collection
     *                of these
     * @param node    the node name. May be a composite node
     * @return the distinct collection values
     */
    public List<Object> distinct(ExpressionContext context, String node) {
        return new ArrayList<>(set(context, node));
    }

    /**
     * Returns the distinct values of a collection node.
     *
     * @param object the object
     * @param node   the node name. May be a composite node
     * @return the distinct collection values
     */
    public List<Object> distinct(Object object, String node) {
        return new ArrayList<>(set(object, node));
    }

    /**
     * Iterates through a list of objects, joining the string value of the specified property.
     *
     * @param objects       the objects
     * @param name          the property name
     * @param separator     the separator
     * @param lastSeparator the last separator
     * @param singular      if non-null, is appended to the string if there is only a single string
     * @param plural        if non-null, is appended to the string if there are multiple strings
     * @param comparator    comparator to order values. May be {@code null}
     * @return the concatenated values
     */
    protected String join(Object objects, String name, String separator, String lastSeparator,
                          String singular, String plural, Comparator<Object> comparator) {
        StringBuilder builder = new StringBuilder();
        List<Object> values = new ArrayList<>();
        collect(objects, name, values);
        int size = values.size();
        if (comparator != null && size > 1) {
            Collections.sort(values, comparator);
        }
        for (int i = 0; i < size; ++i) {
            if (i > 0) {
                if (i == size - 1) {
                    builder.append(lastSeparator);
                } else {
                    builder.append(separator);
                }
            }
            builder.append(values.get(i));
        }
        if (size == 1 && singular != null) {
            builder.append(singular);
        } else if (size > 1 && plural != null) {
            builder.append(plural);
        }
        return builder.toString();
    }

    /**
     * Helper to convert an iterable to a list.
     *
     * @param objects the iterable
     * @return a list
     */
    protected <T extends IMObject> List<T> asList(Iterable<T> objects) {
        List<T> list;
        if (objects instanceof Collection) {
            list = new ArrayList<>((Collection<T>) objects);
        } else {
            list = new ArrayList<>();
            CollectionUtils.addAll(list, objects);
        }
        return list;
    }

    /**
     * Collects all values for a node.
     *
     * @param object the root object. May be an {@link IMObject}, {@link PropertySet}, or a collection of these
     * @param name   the node name. May be a composite node
     * @param result the collection to collect values in
     * @return {@code result}
     */
    protected <T extends Collection<Object>> T collect(Object object, String name, T result) {
        if (object instanceof IMObject || object instanceof PropertySet) {
            getValues(object, name, result);
        } else if (object instanceof Iterable) {
            for (Object element : (Iterable) object) {
                getValues(element, name, result);
            }
        }
        return result;
    }

    /**
     * Collects all values for a node.
     *
     * @param object the root object. May be an {@link IMObject} or {@link PropertySet}.
     * @param name   the node name. May be a composite node
     * @param result the collection to collect values in
     * @return {@code result}
     */
    protected <T extends Collection<Object>> T getValues(Object object, String name, T result) {
        PropertyResolver resolver = createResolver(object);
        if (resolver != null) {
            List<Object> values = resolver.getObjects(name);
            if (values != null) {
                result.addAll(values);
            }
        }
        return result;
    }

    /**
     * Creates a resolver for the specified object.
     *
     * @param object the object
     * @return a new resolver or {@code null} if the object isn't supported
     */
    private PropertyResolver createResolver(Object object) {
        PropertyResolver resolver = null;
        if (object instanceof IMObject) {
            resolver = new NodeResolver((IMObject) object, service, lookups);
        } else if (object instanceof ObjectSet) {
            resolver = new PropertySetResolver((ObjectSet) object, service, lookups);
        }
        return resolver;
    }

}
