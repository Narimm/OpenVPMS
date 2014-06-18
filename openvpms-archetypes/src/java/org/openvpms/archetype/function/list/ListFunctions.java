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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.function.list;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.Pointer;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.NodeResolver;
import org.openvpms.component.business.service.archetype.helper.PropertyResolverException;
import org.openvpms.component.business.service.archetype.helper.sort.IMObjectSorter;
import org.openvpms.component.business.service.lookup.ILookupService;

import java.util.ArrayList;
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
    public <T extends IMObject> List<T> sort(List<T> objects, String node) {
        return sorter.sort(new ArrayList<T>(objects), node, true);
    }

    /**
     * Concatenates object names, comma separated.
     *
     * @param objects the objects
     * @return the concatenated names
     */
    public <T extends IMObject> String names(List<T> objects) {
        return names(objects, SEPARATOR);
    }

    /**
     * Concatenates object names, with the separator string between names.
     *
     * @param objects   the objects
     * @param separator the separator
     * @return the concatenated names
     */
    public <T extends IMObject> String names(List<T> objects, String separator) {
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
    public <T extends IMObject> String sortNames(List<T> objects) {
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
    public <T extends IMObject> String sortNames(List<T> objects, String separator) {
        return names(sort(new ArrayList<T>(objects), "name"), separator);
    }

    /**
     * Iterates through a list of objects, joining the string value of the specified node.
     *
     * @param objects the objects
     * @param node    the node name
     * @return the concatenated node
     */
    public <T extends IMObject> String join(List<T> objects, String node) {
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
    public <T extends IMObject> String join(List<T> objects, String node, String separator) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < objects.size(); ++i) {
            if (i > 0) {
                builder.append(separator);
            }
            NodeResolver resolver = new NodeResolver(objects.get(i), service);
            try {
                Object value = resolver.getObject(node);
                builder.append(value);
            } catch (PropertyResolverException ignore) {
                // do nothing
            }
        }
        return builder.toString();
    }

}
