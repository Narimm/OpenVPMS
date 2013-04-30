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
 */

package org.openvpms.web.component.im.act;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.NotPredicate;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.service.archetype.functor.IsA;
import org.openvpms.component.business.service.archetype.functor.RelationshipRef;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * Filters one level of an act hierarchy.
 *
 * @author Tim Anderson
 * @see ActHierarchyIterator
 */
public class ActHierarchyFilter<T extends Act> {

    /**
     * The predicate to filter relationships. May be {@code null}
     */
    private final Predicate predicate;

    /**
     * The context.
     */
    private final Context context;

    /**
     * Determines if items should be sorted on ascending timestamp.
     */
    private boolean sortAscending = true;


    /**
     * Constructs an {@code ActHierarchyFilter}.
     *
     * @param context the context
     */
    public ActHierarchyFilter(Context context) {
        this(null, context);
    }

    /**
     * Constructs an {@code ActHierarchyFilter}.
     *
     * @param shortNames the act short names
     * @param include    if {@code true} include the acts, otherwise exclude them
     * @param context    the context
     */
    public ActHierarchyFilter(String[] shortNames, boolean include, Context context) {
        this(createIsA(shortNames, include), context);
    }

    /**
     * Constructs an {@code ActHierarchyFilter}.
     *
     * @param predicate a predicate to filter relationships. May be {@code null}
     * @param context   the context
     */
    public ActHierarchyFilter(Predicate predicate, Context context) {
        this.predicate = predicate;
        this.context = context;
    }

    /**
     * Returns the immediate children of an act, after applying filters.
     *
     * @param act the act
     * @return the immediate children of the act, or an empty list if they have been filtered
     */
    public List<T> filter(T act) {
        List<T> result = new ArrayList<T>();
        if (include(act)) {
            List<T> items = new ArrayList<T>();
            Collection<ActRelationship> relationships = getRelationships(act);
            for (ActRelationship relationship : relationships) {
                T item = getTarget(relationship);
                if (item != null && include(item, act)) {
                    items.add(item);
                }
            }
            items = filter(act, items);
            if (include(act, items)) {
                sortItems(items);
                result.addAll(items);
            }
        }
        return result;
    }

    /**
     * Determine if items should be sorted on ascending timestamp.
     * <p/>
     * Defaults to {@code true}.
     *
     * @param ascending if {@code true} sort items on ascending timestamp; otherwise sort on descending timestamp
     */
    public void setSortItemsAscending(boolean ascending) {
        sortAscending = ascending;
    }

    /**
     * Filters relationships.
     *
     * @param act the act
     * @return the filtered relationships
     */
    protected Collection<ActRelationship> getRelationships(T act) {
        if (predicate == null) {
            return act.getSourceActRelationships();
        }
        return getRelationships(act.getSourceActRelationships(), predicate);
    }

    /**
     * Filters relationships using a predicate.
     *
     * @param relationships the relationships to filter
     * @param predicate     the predicate to use
     * @return the filtered relationships
     */
    protected Collection<ActRelationship> getRelationships(Collection<ActRelationship> relationships,
                                                           Predicate predicate) {
        Collection<ActRelationship> result = new ArrayList<ActRelationship>();
        for (ActRelationship relationship : relationships) {
            if (predicate.evaluate(relationship)) {
                result.add(relationship);
            }
        }
        return result;
    }

    /**
     * Determines if an act should be included.
     * <p/>
     * This implementation always returns {@code true}
     *
     * @param act the act
     * @return {@code true} if the act should be included
     */
    protected boolean include(T act) {
        return true;
    }

    /**
     * Filters child acts.
     * <p/>
     * This implementation returns {@code children} unmodified.
     *
     * @param parent   the top level act
     * @param children the child acts
     * @return the filtered acts
     */
    protected List<T> filter(T parent, List<T> children) {
        return children;
    }

    /**
     * Determines if an act should be included, after the child items have
     * been determined.
     * <p/>
     * This implementation always returns {@code true}
     *
     * @param parent   the top level act
     * @param children the child acts
     * @return {@code true} if the act should be included
     */
    protected boolean include(T parent, List<T> children) {
        return true;
    }

    /**
     * Determines if a child act should be included.
     * <p/>
     * This implementation always returns {@code true}
     *
     * @param child  the child act
     * @param parent the parent act
     * @return {@code true} if the child act should be included
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
    protected void sortItems(List<T> acts) {
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
        if (!sortAscending) {
            comparator = ComparatorUtils.reversedComparator(comparator);
        }
        Collections.sort(acts, comparator);
    }

    /**
     * Helper to return a predicate that includes/excludes acts based on their short name.
     *
     * @param shortNames the act short names
     * @param include    if {@code true} include the acts, otherwise exclude them
     * @return a new predicate
     */
    protected static Predicate createIsA(final String[] shortNames, boolean include) {
        Predicate result = new IsA(RelationshipRef.TARGET, shortNames);
        return (include) ? result : new NotPredicate(result);
    }

    /**
     * Helper to return the target act in a relationship.
     *
     * @param relationship the relationship
     * @return the target act or {@code null} if none can be found
     */
    @SuppressWarnings("unchecked")
    private T getTarget(ActRelationship relationship) {
        return (T) IMObjectHelper.getObject(relationship.getTarget(), context);
    }

}
