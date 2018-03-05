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

package org.openvpms.web.component.im.view.act;

import org.apache.commons.collections.Transformer;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IdConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractListResultSet;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.component.im.util.VirtualNodeSortConstraint;
import org.openvpms.web.component.util.ErrorHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * A result set for {@link ActRelationship} instances, that provides sorting
 * of target acts using ArchetypeQuery, to improve performance.
 * <p/>
 * If any of the sort constraints are {@link VirtualNodeSortConstraint}, it will fall back a less efficient
 * memory sort.
 *
 * @author Tim Anderson
 */
public class ActRelationshipResultSet extends AbstractListResultSet<IMObject> {

    /**
     * The parent act.
     */
    private final Act parent;

    /**
     * The sort criteria.
     */
    private SortConstraint[] sort = new SortConstraint[0];

    /**
     * The act relationship short names.
     */
    private final String[] relationshipShortNames;

    /**
     * The target act short names.
     */
    private final String[] shortNames;

    /**
     * Determines if the set is sorted ascending or descending.
     */
    private boolean sortAscending = true;


    /**
     * Constructs an {@link ActRelationshipResultSet}.
     *
     * @param parent                 the parent act
     * @param relationships          the act relationships
     * @param relationshipShortNames the act relationship short names to use
     * @param pageSize               the maximum no. of results per page
     */
    public ActRelationshipResultSet(Act parent, List<IMObject> relationships, String[] relationshipShortNames,
                                    int pageSize) {
        super(relationships, pageSize);
        this.parent = parent;
        this.relationshipShortNames = relationshipShortNames;
        shortNames = DescriptorHelper.getNodeShortNames(relationshipShortNames, "target");
    }

    /**
     * Sorts the set. This resets the iterator.
     *
     * @param sort the sort criteria. May be {@code null}
     */
    public void sort(SortConstraint[] sort) {
        if (sort != null && sort.length != 0 && !getObjects().isEmpty()) {
            List<NodeSortConstraint> nodes = new ArrayList<>();
            boolean memorySort = false;
            sortAscending = sort[0].isAscending();
            for (SortConstraint constraint : sort) {
                if (constraint instanceof VirtualNodeSortConstraint) {
                    memorySort = true;
                    break;
                }
                if (constraint instanceof NodeSortConstraint) {
                    nodes.add(((NodeSortConstraint) constraint));
                }
            }
            if (memorySort) {
                memorySort(sort);
            } else if (!nodes.isEmpty()) {
                querySort(nodes);
            }
            this.sort = sort;
        }
        reset();
    }

    /**
     * Determines if the node is sorted ascending or descending.
     *
     * @return {@code true} if the node is sorted ascending or no sort
     * constraint was specified; {@code false} if it is sorted
     * descending
     */
    public boolean isSortedAscending() {
        return sortAscending;
    }

    /**
     * Returns the sort criteria.
     *
     * @return the sort criteria. Never null
     */
    public SortConstraint[] getSortConstraints() {
        return sort;
    }

    /**
     * Performs an in-memory sort of the act relationships.
     * <p/>
     * Note that the sort constraints must be relative to the target acts.
     *
     * @param sort the sort constraints
     */
    protected void memorySort(SortConstraint[] sort) {
        IMObjectSorter.sort(getObjects(), sort, new Transformer() {
            @Override
            public Object transform(Object input) {
                ActRelationship relationship = (ActRelationship) input;
                return IMObjectHelper.getObject(relationship.getTarget());
            }
        });
    }

    /**
     * Sorts the act relationships on or one more target nodes.
     *
     * @param nodes the nodes
     */
    protected void querySort(List<NodeSortConstraint> nodes) {
        try {
            List<IMObject> sorted = new ArrayList<>();
            ArchetypeQuery query = createQuery(nodes);
            Map<Long, ActRelationship> relsById = new LinkedHashMap<>();
            for (IMObject object : getObjects()) {
                relsById.put(object.getId(), (ActRelationship) object);
            }

            Iterator<ObjectSet> iter = new ObjectSetQueryIterator(query);
            while (iter.hasNext()) {
                ObjectSet set = iter.next();
                long id = set.getLong("rel.id");
                ActRelationship relationship = relsById.remove(id);
                if (relationship != null) {
                    sorted.add(relationship);
                }
            }

            // for all relationships not persistent, just add to the sorted set.
            // TODO. Should fall back and use in memory sorting with a binary
            // search to determine where they should go, or a Collection.sort()
            // if it is more efficient to do so.
            for (IMObject relationship : relsById.values()) {
                sorted.add(relationship);
            }
            getObjects().clear();
            getObjects().addAll(sorted);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Creates a new archetype ordered on the specified nodes.
     *
     * @param nodes the target act nodes to sort on
     * @return a new archetype query
     */
    protected ArchetypeQuery createQuery(List<NodeSortConstraint> nodes) {
        ShortNameConstraint relationships = new ShortNameConstraint("rel", relationshipShortNames, false, false);
        ObjectRefConstraint source = new ObjectRefConstraint("source", parent.getObjectReference());
        ShortNameConstraint target = new ShortNameConstraint("target", shortNames, false, false);

        ArchetypeQuery query = new ArchetypeQuery(relationships);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        query.add(source);
        query.add(target);
        query.add(new IdConstraint("rel.source", "source"));
        query.add(new IdConstraint("rel.target", "target"));
        query.add(new NodeSelectConstraint("rel.id"));
        for (NodeSortConstraint node : nodes) {
            String name = node.getNodeName();
            NodeDescriptor descriptor = QueryHelper.getDescriptor(target, name);
            if (descriptor != null) {
                if (QueryHelper.isParticipationNode(descriptor)) {
                    QueryHelper.addSortOnParticipation(target, query, descriptor, node.isAscending());
                } else {
                    query.add(new NodeSortConstraint(target.getAlias(), name, node.isAscending()));
                }
            }
        }
        return query;
    }

}
