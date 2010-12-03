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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.im.query;

import org.apache.commons.collections.CollectionUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.util.VirtualNodeSortConstraint;

import java.util.ArrayList;
import java.util.List;


/**
 * An {@link ResultSet} that sorts the set in memory if any of the sort criteria are {@link VirtualNodeSortConstraint}s,
 * but uses the sets underlying sort facility if it doesn't.
 * <p/>
 * In order to sort, this caches all objects in memory (as opposed to an approach which just caches their references).
 * It should therefore only be used for small result sets.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LocalSortResultSet<T extends IMObject> extends ResultSetAdapter<T, T> {

    /**
     * The original result set.
     */
    private ResultSet<T> original;

    /**
     * The in-memory result set, when sorting virtual nodes.
     */
    private IMObjectListResultSet<T> local;


    /**
     * Constructs an <tt>InMemorySortResultSet</tt>.
     *
     * @param set the set to delegate to
     */
    public LocalSortResultSet(ResultSet<T> set) {
        super(set);
        this.original = set;
    }

    /**
     * Sorts the set. This resets the iterator.
     *
     * @param sort the sort criteria. May be <tt>null</tt>
     */
    public void sort(SortConstraint[] sort) {
        ResultSet<T> set;
        if (!sortLocally(sort)) {
            // revert to the original set, using its sorting mechanism
            set = original;
            local = null;
        } else {
            if (local == null) {
                // load all of the objects
                List<T> objects = new ArrayList<T>();
                original.reset();
                CollectionUtils.addAll(objects, new ResultSetIterator<T>(original));
                local = new IMObjectListResultSet<T>(objects, getPageSize());
            }
            set = local;
        }
        set.sort(sort);
        setResultSet(set);
    }

    /**
     * Determines if duplicate results should be filtered.
     *
     * @param distinct if true, remove duplicate results
     */
    public void setDistinct(boolean distinct) {
        original.setDistinct(distinct);
    }

    /**
     * Determines if duplicate results should be filtered.
     *
     * @return <tt>true</tt> if duplicate results should be removed;
     *         otherwise <tt>false</tt>
     */
    public boolean isDistinct() {
        return original.isDistinct();
    }

    /**
     * Sets the nodes to query.
     *
     * @param nodes the nodes to query
     */
    public void setNodes(String[] nodes) {
        original.setNodes(nodes);
    }

    /**
     * Clones this result set.
     * <p/>
     * This copies the state of iterators.
     *
     * @return a clone of this
     * @throws CloneNotSupportedException if the set cannot be cloned
     */
    @SuppressWarnings("unchecked")
    public ResultSet<T> clone() throws CloneNotSupportedException {
        LocalSortResultSet<T> result = (LocalSortResultSet<T>) super.clone();
        if (original != getResultSet()) {
            result.original = original.clone();
        }
        return result;
    }

    /**
     * Converts a page.
     *
     * @param page the page to convert
     * @return the converted page
     */
    protected IPage<T> convert(IPage<T> page) {
        return page;
    }

    /**
     * Determines if the set needs to be sorted in memory.
     *
     * @param sort the sort constraints
     * @return <tt>true</tt>  if the set needs to be sorted in memory
     */
    private boolean sortLocally(SortConstraint[] sort) {
        if (sort != null) {
            for (SortConstraint s : sort) {
                if (s instanceof VirtualNodeSortConstraint) {
                    return true;
                }
            }
        }
        return false;
    }

}
