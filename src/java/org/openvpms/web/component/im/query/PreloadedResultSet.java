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

package org.openvpms.web.component.im.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.FunctorException;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.collections.comparators.TransformingComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.DescriptorException;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeProperty;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeSortConstraint;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.component.system.common.search.PagingCriteria;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Paged result set where the results are pre-loaded.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PreloadedResultSet<T extends IMObject>
        extends AbstractResultSet<T> {

    /**
     * The logger.
     */
    private static final Log _log = LogFactory.getLog(PreloadedResultSet.class);

    /**
     * The query objects.
     */
    private final List<T> _objects;

    /**
     * Determines if the set is sorted ascending or descending.
     */
    private boolean _sortAscending = true;


    /**
     * Construct a new <code>PreloadedResultSet</code>.
     *
     * @param objects the objects
     * @param rows    the maximum no. of rows per page
     */
    public PreloadedResultSet(List<T> objects, int rows) {
        super(rows);
        _objects = objects;

        reset();
    }

    /**
     * Sorts the set. This resets the iterator.
     *
     * @param sort the sort criteria
     */
    public void sort(SortConstraint[] sort) {
        if (!_objects.isEmpty()) {
            ComparatorChain comparator = new ComparatorChain();
            for (SortConstraint constraint : sort) {
                if (constraint instanceof NodeSortConstraint) {
                    Comparator node
                            = getComparator((NodeSortConstraint) constraint);
                    comparator.addComparator(node);
                } else if (constraint instanceof ArchetypeSortConstraint) {
                    Comparator type =
                            getComparator((ArchetypeSortConstraint) constraint);
                    comparator.addComparator(type);
                    break;
                }
                _sortAscending = constraint.isAscending();
            }
            Collections.sort(_objects, comparator);
        }
        reset();
    }

    /**
     * Determines if the node is sorted ascending or descending.
     *
     * @return <code>true</code> if the node is sorted ascending;
     *         <code>false</code> if it is sorted descending
     */
    public boolean isSortedAscending() {
        return _sortAscending;
    }

    /**
     * Returns the specified page.
     *
     * @param firstRow
     * @param maxRows
     * @return the page corresponding to <code>page</code>, or <code>null</code>
     *         if none exists
     */
    protected IPage<T> getPage(int firstRow, int maxRows) {
        int to;
        if (maxRows == ArchetypeQuery.ALL_ROWS
            || ((firstRow + maxRows) >= _objects.size())) {
            to = _objects.size();
        } else {
            to = firstRow + maxRows;
        }
        List<T> rows = new ArrayList<T>(_objects.subList(firstRow, to));
        return new Page<T>(rows, new PagingCriteria(firstRow, maxRows),
                           _objects.size());
    }

    /**
     * Returns a new comparator.
     *
     * @param sort the sort criteria
     * @return a new comparator
     */
    protected Comparator<Object> getComparator(NodeSortConstraint sort) {
        Comparator comparator = getComparator(sort.isAscending());
        Transformer transformer = new NodeTransformer(
                sort.getNodeName(), ServiceHelper.getArchetypeService());
        return new TransformingComparator(transformer, comparator);
    }

    /**
     * Returns a new comparator for an archetype property.
     *
     * @param sort the sort criteria
     */
    protected Comparator<Object> getComparator(ArchetypeSortConstraint sort) {
        Comparator comparator = getComparator(sort.isAscending());
        Transformer transformer = new ArchetypeTransformer(
                sort.getProperty());
        return new TransformingComparator(transformer, comparator);
    }

    /**
     * Returns a new comparator.
     *
     * @param ascending if <code>true</code> sort in ascending order; otherwise
     *                  sort in descending order
     */
    protected Comparator getComparator(boolean ascending) {
        Comparator comparator = ComparatorUtils.naturalComparator();

        // handle nulls.
        comparator = ComparatorUtils.nullLowComparator(comparator);
        if (ascending) {
            comparator = ComparatorUtils.reversedComparator(comparator);
        }
        return comparator;
    }

    private class NodeTransformer implements Transformer {

        /**
         * The node name.
         */
        private final String _node;

        /**
         * The archetype service.
         */
        private final IArchetypeService _service;

        /**
         * Cached archetype descriptor.
         */
        private ArchetypeDescriptor _archetype;

        /**
         * Cached node descriptor.
         */
        private NodeDescriptor _descriptor;


        /**
         * Construct a new <code>NodeTransformer</code>.
         *
         * @param node    the node name
         * @param service the archetype service
         */
        public NodeTransformer(String node, IArchetypeService service) {
            _node = node;
            _service = service;
        }

        /**
         * Transforms the input object (leaving it unchanged) into some output
         * object.
         *
         * @param input the object to be transformed, should be left unchanged
         * @return a transformed object
         * @throws ClassCastException       (runtime) if the input is the wrong
         *                                  class
         * @throws IllegalArgumentException (runtime) if the input is invalid
         * @throws FunctorException         (runtime) if the transform cannot be
         *                                  completed
         */
        public Object transform(Object input) {
            Object result = null;
            IMObject object = (IMObject) input;
            NodeDescriptor descriptor = getDescriptor(object);
            if (descriptor != null) {
                try {
                    result = _descriptor.getValue(object);
                } catch (DescriptorException exception) {
                    _log.error(exception);
                }
            }
            return result;
        }

        private NodeDescriptor getDescriptor(IMObject object) {
            ArchetypeDescriptor archetype
                    = DescriptorHelper.getArchetypeDescriptor(object, _service);
            if (archetype != _archetype && archetype != null) {
                _archetype = archetype;
                _descriptor = _archetype.getNodeDescriptor(_node);
            }
            return _descriptor;
        }

    }

    private class ArchetypeTransformer implements Transformer {

        /**
         * The archetype property to return.
         */
        private final ArchetypeProperty _property;

        /**
         * Construct a new <code>ArchetypeTransformer</code>.
         *
         * @param property the property to return
         */
        public ArchetypeTransformer(ArchetypeProperty property) {
            _property = property;
        }

        /**
         * Transforms the input object (leaving it unchanged) into some output
         * object.
         *
         * @param input the object to be transformed, should be left unchanged
         * @return a transformed object
         * @throws ClassCastException       (runtime) if the input is the wrong
         *                                  class
         * @throws IllegalArgumentException (runtime) if the input is invalid
         * @throws FunctorException         (runtime) if the transform cannot be
         *                                  completed
         */
        public Object transform(Object input) {
            Object result = null;
            IMObject object = (IMObject) input;
            switch (_property) {
                case ReferenceModelName:
                    result = object.getArchetypeId().getRmName();
                    break;
                case EntityName:
                    result = object.getArchetypeId().getEntityName();
                    break;
                case ConceptName:
                    result = object.getArchetypeId().getConcept();
                    break;
            }
            return result;
        }
    }


}
