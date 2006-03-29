package org.openvpms.web.component.im.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.FunctorException;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.comparators.TransformingComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.DescriptorException;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.search.IPage;
import org.openvpms.component.system.common.search.PagingCriteria;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Paged result set where the results are pre-loaded.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
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
     * The sort node.
     */
    private String _node;

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
     * @param node      the node to sort on
     * @param ascending if <code>true</code> sort the set in ascending order;
     *                  otherwise sort it in <code>descebding</code> order
     */
    public void sort(String node, boolean ascending) {
        if (!_objects.isEmpty()) {
            Comparator<Object> comparator = getComparator(node, ascending);
            Collections.sort(_objects, comparator);
            _node = node;
            _sortAscending = ascending;
        }
        reset();
    }

    /**
     * Returns the node the set was sorted on.
     *
     * @return the sort node, or <code>null</code> if the set is unsorted
     */
    public String getSortNode() {
        return _node;
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
     * @param criteria the paging criteria
     * @return the page corresponding to <code>page</code>, or <code>null</code>
     *         if none exists
     */
    protected IPage<T> getPage(PagingCriteria criteria) {
        int from = criteria.getFirstRow();
        int count = criteria.getNumOfRows();
        int to;
        if (count == PagingCriteria.ALL_ROWS
            || ((from + count) >= _objects.size())) {
            to = _objects.size();
        } else {
            to = from + count;
        }
        List<T> rows = new ArrayList<T>(_objects.subList(from, to));
        return new Page<T>(rows, criteria, _objects.size());
    }

    /**
     * Returns a new comparator.
     *
     * @param node      the node to sort on
     * @param ascending if <code>true</code> sort the node in ascending order;
     *                  otherwise sort it in <code>descebding</code> order
     */
    protected Comparator<Object> getComparator(String node, boolean ascending) {
        Comparator comparator = ComparatorUtils.naturalComparator();

        // handle nulls.
        comparator = ComparatorUtils.nullLowComparator(comparator);
        if (!ascending) {
            comparator = ComparatorUtils.reversedComparator(comparator);
        }
        Transformer transformer = new NodeTransformer(
                node, ServiceHelper.getArchetypeService());
        return new TransformingComparator(transformer, comparator);
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
}
