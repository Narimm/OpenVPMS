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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.act;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.NotPredicate;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.functor.IsA;
import org.openvpms.component.business.service.archetype.functor.RelationshipRef;
import org.openvpms.component.model.act.ActRelationship;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Filters one level of an act hierarchy.
 *
 * @author Tim Anderson
 * @see ActHierarchyIterator
 */
public class ActHierarchyFilter<T extends Act> extends ActFilter<T> {

    /**
     * The predicate to filter relationships. May be {@code null}
     */
    private final Predicate predicate;

    /**
     * Determines if items should be sorted on ascending timestamp.
     */
    private boolean sortAscending = true;


    /**
     * Constructs an {@code ActHierarchyFilter}.
     */
    public ActHierarchyFilter() {
        this(null);
    }

    /**
     * Constructs an {@code ActHierarchyFilter}.
     *
     * @param shortNames the act short names
     * @param include    if {@code true} include the acts, otherwise exclude them
     */
    public ActHierarchyFilter(String[] shortNames, boolean include) {
        this(createIsA(shortNames, include));
    }

    /**
     * Constructs an {@code ActHierarchyFilter}.
     *
     * @param predicate a predicate to filter relationships. May be {@code null}
     */
    public ActHierarchyFilter(Predicate predicate) {
        this.predicate = predicate;
    }

    /**
     * Returns the immediate children of an act, after applying filters.
     *
     * @param act  the act
     * @param root the root of the tree
     * @param acts the set of visited acts, keyed on reference
     * @return the immediate children of the act, or an empty list if they have been filtered
     */
    @Override
    public List<T> filter(T act, T root, Map<IMObjectReference, T> acts) {
        List<T> result = new ArrayList<>();
        if (include(act)) {
            List<T> items = getChildren(act, root, acts);
            items = filter(act, items, acts);
            if (include(act, items)) {
                result.addAll(items);
            }
        }
        return result;
    }

    /**
     * Returns a comparator to sort the children of an act.
     *
     * @param act the parent act
     * @return the comparator to sort the act's children
     */
    @Override
    public Comparator<T> getComparator(T act) {
        return getComparator(sortAscending);
    }

    /**
     * Determine if items should be sorted on ascending timestamp.
     * <p>
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
        Collection<ActRelationship> result = new ArrayList<>();
        for (ActRelationship relationship : relationships) {
            if (predicate.evaluate(relationship)) {
                result.add(relationship);
            }
        }
        return result;
    }

    /**
     * Determines if an act should be included.
     * <p>
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
     * <p>
     * This implementation returns {@code children} unmodified.
     *
     * @param parent   the parent act
     * @param children the child acts
     * @param acts     the set of visited acts, keyed on reference
     * @return the filtered acts
     */
    protected List<T> filter(T parent, List<T> children, Map<IMObjectReference, T> acts) {
        return children;
    }

    /**
     * Determines if an act should be included, after the child items have
     * been determined.
     * <p>
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
     * <p>
     * This implementation always returns {@code true}
     *
     * @param child  the child act
     * @param parent the parent act
     * @param root   the root act
     * @return {@code true} if the child act should be included
     */
    protected boolean include(T child, T parent, T root) {
        return true;
    }

    /**
     * Returns the immediate children of an act.
     * <p>
     * Each child is passed to {@link #include(Act, Act, Act)} to determine if it should be included.
     *
     * @param act  the parent act
     * @param root the root act
     * @param acts a cache of the visited acts, keyed on reference
     * @return the include target acts
     */
    @SuppressWarnings("unchecked")
    protected List<T> getChildren(T act, T root, Map<IMObjectReference, T> acts) {
        List<T> result = new ArrayList<>();
        Set<T> cached = getChildren(act, acts);
        for (Act match : cached) {
            T item = (T) match;
            if (include(item, act, root)) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Returns the immediate children of an act.
     * <p>
     * This implementation returns the targets of the relationships from {@link #getRelationships(Act)}.
     *
     * @param act  the parent act
     * @param acts a cache of the visited acts, keyed on reference
     * @return the immediate children of {@code act}
     */
    @SuppressWarnings("unchecked")
    protected Set<T> getChildren(T act, Map<IMObjectReference, T> acts) {
        Collection<ActRelationship> relationships = getRelationships(act);
        List<IMObjectReference> references = new ArrayList<>();
        for (ActRelationship relationship : relationships) {
            IMObjectReference target = (IMObjectReference) relationship.getTarget();
            if (target != null) {
                references.add(target);
            }
        }
        return getActs(references, acts);
    }

    @SuppressWarnings("unchecked")
    protected Set<T> getActs(List<IMObjectReference> references, Map<IMObjectReference, T> acts) {
        Set<T> result = new HashSet<>();
        Set<IMObjectReference> uncached = new HashSet<>();
        for (IMObjectReference reference : references) {
            T found = acts.get(reference);
            if (found != null) {
                result.add(found);
            } else {
                uncached.add(reference);
            }
        }
        if (!uncached.isEmpty()) {
            Map<IMObjectReference, T> map = (Map<IMObjectReference, T>) ActHelper.getActMap(uncached);
            acts.putAll(map);
            result.addAll(map.values());
        }
        return result;
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

}
