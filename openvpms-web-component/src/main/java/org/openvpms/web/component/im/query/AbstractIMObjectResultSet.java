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

package org.openvpms.web.component.im.query;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Result set for {@code IMObject} instances.
 *
 * @author Tim Anderson
 */
public abstract class AbstractIMObjectResultSet<T> extends AbstractArchetypeServiceResultSet<T> {

    /**
     * The archetypes to query.
     */
    private final ShortNameConstraint archetypes;

    /**
     * The value to search on. May be {@code null}.
     */
    private String value;

    /**
     * The nodes to search for the value.
     */
    private List<String> nodes = Collections.emptyList();

    /**
     * The ID node name.
     */
    protected static final String ID = "id";

    /**
     * The 'name' node name.
     */
    protected static final String NAME = "name";


    /**
     * Constructs an {@link AbstractIMObjectResultSet}.
     *
     * @param archetypes  the archetypes to query
     * @param value       the value to query on. May be {@code null}
     * @param constraints additional query constraints. May be {@code null}
     * @param sort        the sort criteria. May be {@code null}
     * @param rows        the maximum no. of rows per page
     * @param distinct    if {@code true} filter duplicate rows
     * @param executor    the query executor
     */
    public AbstractIMObjectResultSet(ShortNameConstraint archetypes, String value, IConstraint constraints,
                                     SortConstraint[] sort, int rows, boolean distinct, QueryExecutor<T> executor) {
        super(constraints, rows, sort, executor);
        this.archetypes = archetypes;
        setSearch(value, ID, NAME);
        setDistinct(distinct);
    }

    /**
     * Sets the nodes to search for a particular value on.
     * </p/>
     * This resets the iterator.
     *
     * @param value the value to search for. May be {@code null}
     * @param nodes the nodes to search
     */
    public void setSearch(String value, String... nodes) {
        this.value = value;
        this.nodes = new ArrayList<String>();
        List<ArchetypeDescriptor> descriptors = getArchetypes(archetypes);
        for (String node : nodes) {
            if (hasNode(descriptors, node)) {
                this.nodes.add(node);
            }
        }
        reset();
    }

    /**
     * Returns the nodes to search on.
     *
     * @return the nodes
     */
    public List<String> getSearch() {
        return nodes;
    }

    /**
     * Returns the value being searched on.
     *
     * @return the search value. May be {@code null}
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the archetypes.
     *
     * @return the archetypes
     */
    protected ShortNameConstraint getArchetypes() {
        return archetypes;
    }

    /**
     * Creates a new archetype query.
     *
     * @return a new archetype query
     */
    protected ArchetypeQuery createQuery() {
        ArchetypeQuery query = new ArchetypeQuery(getArchetypes());
        addValueConstraints(query);
        return query;
    }

    /**
     * Adds constraints to query nodes on a value.
     * <p/>
     * This creates the constraints with {@link #createValueConstraints(String, List)}. If multiple constraints
     * are returned, an or constraint is used to add them to the query.
     *
     * @param query the query to add the constraints to
     */
    protected void addValueConstraints(ArchetypeQuery query) {
        if (!StringUtils.isEmpty(value) && !nodes.isEmpty()) {
            List<IConstraint> constraints = createValueConstraints(value, nodes);
            if (constraints.size() > 1) {
                OrConstraint or = new OrConstraint();
                for (IConstraint constraint : constraints) {
                    or.add(constraint);
                }
                query.add(or);
            } else if (constraints.size() == 1) {
                query.add(constraints.get(0));
            }
        }
    }

    /**
     * Creates constraints to query nodes on a value.
     *
     * @param value the value to query
     * @param nodes the nodes to query
     * @return the constraints
     */
    protected List<IConstraint> createValueConstraints(String value, List<String> nodes) {
        String alias = getArchetypes().getAlias();
        List<IConstraint> constraints = new ArrayList<IConstraint>();
        for (String node : nodes) {
            if (ID.equals(node)) {
                Long id = getId(value);
                if (id != null) {
                    String name = alias != null ? alias + "." + ID : ID;
                    constraints.add(Constraints.eq(name, id));
                }
            } else {
                String name = alias != null ? alias + "." + node : node;
                constraints.add(Constraints.eq(name, value));
            }
        }
        return constraints;
    }

    /**
     * Adds sort constraints.
     * This implementation adds all those returned by
     * {@link #getSortConstraints()}, and finally adds a sort on <em>id</em>
     * to guarantee that subsequent queries will return results in the same
     * order.
     *
     * @param query the query to add the constraints to
     */
    @Override
    protected void addSortConstraints(ArchetypeQuery query) {
        super.addSortConstraints(query);
        String alias = getArchetypes().getAlias();
        query.add(new NodeSortConstraint(alias, "id"));
    }

    /**
     * Attempts to extract an ID from a value.
     *
     * @param value the value
     * @return the corresponding ID, or {@code null} if {@code value} is not a valid ID
     */
    protected Long getId(String value) {
        if (!StringUtils.isEmpty(value)) {
            value = value.replaceAll(",", "").replaceAll("\\*", ""); // remove any commas and wildcards.
            try {
                return Long.valueOf(value);
            } catch (NumberFormatException ignore) {
            }
        }
        return null;
    }

    /**
     * Adds a reference constraint.
     *
     * @param query     the archetype query
     * @param reference the reference to constrain the query on
     */
    @Override
    protected void addReferenceConstraint(ArchetypeQuery query, IMObjectReference reference) {
        query.add(new ObjectRefConstraint(archetypes.getAlias(), reference));
    }

    /**
     * Returns all archetypes matching supplied constraint.
     *
     * @param archetypes the archetype constraint
     * @return the archetypes matching the constraint
     * @throws OpenVPMSException for any error
     */
    protected List<ArchetypeDescriptor> getArchetypes(ShortNameConstraint archetypes) {
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        String[] shortNames = DescriptorHelper.getShortNames(archetypes.getShortNames());
        List<ArchetypeDescriptor> result = new ArrayList<ArchetypeDescriptor>(shortNames.length);
        for (String shortName : shortNames) {
            ArchetypeDescriptor archetype = service.getArchetypeDescriptor(shortName);
            if (archetype != null) {
                result.add(archetype);
            }
        }

        return result;
    }

    /**
     * Determines if all archetypes have a particular node descriptor.
     *
     * @param archetypes the archetypes
     * @param node       the node to search for
     * @return {@code true} if the archetypes all have the node
     */
    protected boolean hasNode(List<ArchetypeDescriptor> archetypes, String node) {
        for (ArchetypeDescriptor archetype : archetypes) {
            NodeDescriptor descriptor = archetype.getNodeDescriptor(node);
            if (descriptor == null) {
                return false;
            }
        }
        return true;
    }

}
