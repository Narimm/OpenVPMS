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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.act;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;


/**
 * Filters one level of an act heirarchy.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 * @see ActHierarchyIterator
 */
public class ActHierarchyFilter<T extends Act> {

    /**
     * The predicate to filter relationships. May be <tt>null</tt>
     */
    private Predicate predicate;


    /**
     * Creates a new <tt>ActHierarchyFilter</tt>.
     */
    public ActHierarchyFilter() {
        this(null);
    }

    /**
     * Creates a new <tt>ActHierarchyFilter</tt>.
     *
     * @param predicate a predicate to filter relationships. May be
     *                  <tt>null</tt>
     */
    public ActHierarchyFilter(Predicate predicate) {
        this.predicate = predicate;
    }

    /**
     * Returns the act and its immediate children, after applying filters.
     *
     * @param act the act
     * @return the act and its immediate children, or an empty array if they
     *         have been filtered
     */
    public List<T> filter(T act) {
        List<T> result = new ArrayList<T>();
        if (include(act)) {
            List<T> items = new ArrayList<T>();
            Set<ActRelationship> relationships
                    = act.getSourceActRelationships();
            for (ActRelationship relationship : relationships) {
                if (include(relationship)) {
                    T item = getTarget(relationship);
                    if (item != null && include(item, act)) {
                        items.add(item);
                    }
                }
            }
            if (include(act, items)) {
                result.add(act);
                sortItems(items);
                result.addAll(items);
            }
        }
        return result;
    }

    /**
     * Determines if an act should be included.
     * <p/>
     * This implementation always returns <tt>true</tt>
     *
     * @param act the act
     * @return <tt>true</tt> if the act should be included
     */
    protected boolean include(T act) {
        return true;
    }

    /**
     * Determines if a relationship should be included for evaluation.
     *
     * @param relationship the relationship
     * @return <tt>true</tt> if relationship act should be included
     */
    protected boolean include(ActRelationship relationship) {
        return predicate == null || predicate.evaluate(relationship);
    }

    /**
     * Determines if an act should be included, after the child items have
     * been determined.
     * <p/>
     * This implementation always returns <tt>true</tt>
     *
     * @param parent   the top level act
     * @param children the child acts
     * @return <tt>true</tt> if the act should be included
     */
    protected boolean include(T parent, List<T> children) {
        return true;
    }

    /**
     * Determines if a child act should be included.
     * <p/>
     * This implementation always returns <tt>true</tt>
     *
     * @param child  the child act
     * @param parent the parent act
     */
    protected boolean include(T child, T parent) {
        return true;
    }

    /**
     * Sorts act on start time.
     *
     * @param acts the items to sort
     */
    @SuppressWarnings("unchecked")
    private void sortItems(List<T> acts) {
        Transformer transformer = new Transformer() {
            public Object transform(Object input) {
                Date date = ((Act) input).getActivityStartTime();
                if (date instanceof Timestamp) {
                    // to avoid ClassCastException when doing compareTo
                    date = new Date(date.getTime());
                }
                return date;
            }
        };
        Comparator comparator = ComparatorUtils.transformedComparator(
                ComparatorUtils.nullHighComparator(null), transformer);
        Collections.sort(acts, comparator);
    }

    /**
     * Helper to return the target act in a relationship.
     *
     * @param relationship the relationship
     * @return the target act or <tt>null</tt> if none can be found
     */
    @SuppressWarnings("unchecked")
    private T getTarget(ActRelationship relationship) {
        return (T) IMObjectHelper.getObject(relationship.getTarget());
    }

}
