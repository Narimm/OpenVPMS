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
package org.openvpms.component.system.common.query;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;

import java.util.Arrays;


/**
 * Helper class for creating constraints.
 *
 * @author Tim Anderson
 */
public class Constraints {


    /**
     * Creates a constraint to compare object on their identifiers.
     *
     * @param source the source object
     * @param target the target object
     * @return a new constraint
     */
    public static IdConstraint idEq(String source, String target) {
        return new IdConstraint(source, target);
    }

    /**
     * Creates an <em>equal</em> constraint for the named node.
     *
     * @param name  the node name
     * @param value the value
     * @return a new <em>equal</em> constraint
     */
    public static AbstractNodeConstraint eq(String name, Object value) {
        return (value instanceof IMObject) ? eq(name, ((IMObject) value).getObjectReference())
                                           : new NodeConstraint(name, value);
    }

    /**
     * Creates an <em>equal</em> constraint for the named object reference node.
     *
     * @param name  the node name
     * @param value the value
     * @return a new <em>equal</em> constraint
     */
    public static ObjectRefNodeConstraint eq(String name, IMObjectReference value) {
        return new ObjectRefNodeConstraint(name, value);
    }

    /**
     * Creates an <em>equal</em> constraint for the named object reference node.
     *
     * @param name  the node name
     * @param value the value
     * @return a new <em>equal</em> constraint
     */
    public static ObjectRefNodeConstraint eq(String name, ArchetypeId value) {
        return new ObjectRefNodeConstraint(name, value);
    }

    /**
     * Creates a <em>not-equal</em> constraint for the named node.
     *
     * @param name  the node name
     * @param value the value
     * @return a new <em>not-equal</em> constraint
     */
    public static AbstractNodeConstraint ne(String name, Object value) {
        return (value instanceof IMObject) ? ne(name, ((IMObject) value).getObjectReference())
                                           : new NodeConstraint(name, RelationalOp.NE, value);
    }

    /**
     * Creates a <em>not-equal</em> constraint for the named object reference node.
     *
     * @param name  the node name
     * @param value the value
     * @return a new <em>not-equal</em> constraint
     */
    public static ObjectRefNodeConstraint ne(String name, IMObjectReference value) {
        return new ObjectRefNodeConstraint(name, RelationalOp.NE, value);
    }

    /**
     * Creates a <em>less-than</em> constraint for the named node.
     *
     * @param name  the node name
     * @param value the value
     * @return a new <em>less-than</em> constraint
     */
    public static NodeConstraint lt(String name, Object value) {
        return new NodeConstraint(name, RelationalOp.LT, value);
    }

    /**
     * Creates a <em>less-than-or-equal-to</em> constraint for the named node.
     *
     * @param name  the node name
     * @param value the value
     * @return a new <em>less-than-or-equal-to</em> constraint
     */
    public static NodeConstraint lte(String name, Object value) {
        return new NodeConstraint(name, RelationalOp.LTE, value);
    }

    /**
     * Creates a <em>greater-than</em> constraint for the named node.
     *
     * @param name  the node name
     * @param value the value
     * @return a new <em>greater-than</em> constraint
     */
    public static NodeConstraint gt(String name, Object value) {
        return new NodeConstraint(name, RelationalOp.GT, value);
    }

    /**
     * Creates a <em>greater-than-or-equal-to</em> constraint for the named node.
     *
     * @param name  the node name
     * @param value the value
     * @return a new <em>greater-than-or-equal-to</em> constraint
     */
    public static NodeConstraint gte(String name, Object value) {
        return new NodeConstraint(name, RelationalOp.GTE, value);
    }

    /**
     * Creates a <em>between</em> constraint for the named node.
     *
     * @param name the node name
     * @param lo   the low value
     * @param hi   the high value
     * @return a new <em>between</em> constraint
     */
    public static NodeConstraint between(String name, Object lo, Object hi) {
        return new NodeConstraint(name, RelationalOp.BTW, lo, hi);
    }

    /**
     * Creates an <em>in</em> constraint for the named node.
     *
     * @param name   the node name
     * @param values the values
     * @return a new <em>in</em> constraint
     */
    public static NodeConstraint in(String name, Object... values) {
        return new NodeConstraint(name, RelationalOp.IN, values);
    }

    /**
     * Creates an <em>is-null</em> constraint for the named node.
     *
     * @param name the node name
     * @return a new <em>is-null</em> constraint
     */
    public static NodeConstraint isNull(String name) {
        return new NodeConstraint(name, RelationalOp.IS_NULL);
    }

    /**
     * Creates a <em>not-null</em> constraint for the named node.
     *
     * @param name the node name
     * @return a new <em>not-null</em> constraint
     */
    public static NodeConstraint notNull(String name) {
        return new NodeConstraint(name, RelationalOp.NOT_NULL);
    }

    /**
     * Creates a new <em>short-name</em> constraint for both active/inactive and primary/non-primary instances.
     *
     * @param shortName the short name to constrain on. May contain wildcards.
     * @return a new <em>short-name</em> constraint
     */
    public static ShortNameConstraint shortName(String shortName) {
        return shortName(shortName, false);
    }

    /**
     * Creates a new <em>short-name</em> constraint for primary/non-primary instances.
     *
     * @param shortName  the short name to constrain on. May contain wildcards.
     * @param activeOnly if <tt>true</tt> only query active objects
     * @return a new <em>short-name</em> constraint
     */
    public static ShortNameConstraint shortName(String shortName, boolean activeOnly) {
        return new ShortNameConstraint(shortName, false, activeOnly);
    }

    /**
     * Creates a new <em>short-name</em> constraint for both active/inactive and primary/non-primary instances.
     *
     * @param alias     the constraint alias
     * @param shortName the short name to constrain on. May contain wildcards.
     * @return a new <em>short-name</em> constraint
     */
    public static ShortNameConstraint shortName(String alias, String shortName) {
        return shortName(alias, shortName, false);
    }

    /**
     * Creates a new <em>short-name</em> constraint for primary/non-primary instances.
     *
     * @param alias      the constraint alias
     * @param shortName  the short name to constrain on. May contain wildcards.
     * @param activeOnly if <tt>true</tt> only query active objects
     * @return a new <em>short-name</em> constraint
     */
    public static ShortNameConstraint shortName(String alias, String shortName, boolean activeOnly) {
        return new ShortNameConstraint(alias, shortName, false, activeOnly);
    }

    /**
     * Creates a new <em>short-name</em> constraint for both active/inactive and primary/non-primary instances.
     *
     * @param shortNames the short names to constrain on. May contain wildcards.
     * @return a new <em>short-name</em> constraint
     */
    public static ShortNameConstraint shortName(String[] shortNames) {
        return shortName(shortNames, false);
    }

    /**
     * Creates a new <em>short-name</em> constraint for primary/non-primary instances.
     *
     * @param shortNames the short names to constrain on. May contain wildcards.
     * @param activeOnly if <tt>true</tt> only query active objects
     * @return a new <em>short-name</em> constraint
     */
    public static ShortNameConstraint shortName(String[] shortNames, boolean activeOnly) {
        return new ShortNameConstraint(shortNames, false, activeOnly);
    }

    /**
     * Creates a new <em>short-name</em> constraint for both primary/non-primary and active/inactive instances.
     *
     * @param alias      the constraint alias
     * @param shortNames the short names to constrain on. May contain wildcards.
     * @return a new <em>short-name</em> constraint
     */
    public static ShortNameConstraint shortName(String alias, String[] shortNames) {
        return shortName(alias, shortNames, false);
    }

    /**
     * Creates a new <em>short-name</em> constraint for primary/non-primary instances.
     *
     * @param alias      the constraint alias
     * @param shortNames the short names to constrain on. May contain wildcards.
     * @param activeOnly if <tt>true</tt> only query active objects
     * @return a new <em>short-name</em> constraint
     */
    public static ShortNameConstraint shortName(String alias, String[] shortNames, boolean activeOnly) {
        return new ShortNameConstraint(alias, shortNames, false, activeOnly);
    }

    /**
     * Creates an inner join on a node.
     *
     * @param name the node name
     * @return a new inner join constraint
     */
    public static JoinConstraint join(String name) {
        return new CollectionNodeConstraint(name);
    }

    /**
     * Creates an inner join on a node.
     *
     * @param name  the node name
     * @param alias an alias for the join. May be <tt>null</tt>
     * @return a new inner join constraint
     */
    public static JoinConstraint join(String name, String alias) {
        JoinConstraint result = join(name);
        result.setAlias(alias);
        return result;
    }

    /**
     * Creates an inner join on a node.
     *
     * @param name       the node name
     * @param constraint the archetype constraint to use for the collection node
     * @return a new inner join constraint
     */
    public static JoinConstraint join(String name, BaseArchetypeConstraint constraint) {
        return new CollectionNodeConstraint(name, constraint);
    }

    /**
     * Creates a left outer join on a node.
     *
     * @param name the node name
     * @return a new left outer join constraint
     */
    public static JoinConstraint leftJoin(String name) {
        return new CollectionNodeConstraint(name).setJoinType(JoinConstraint.JoinType.LeftOuterJoin);
    }

    /**
     * Creates a left outer join on a node.
     *
     * @param name  the node name
     * @param alias an alias for the join. May be <tt>null</tt>
     * @return a new left outer join constraint
     */
    public static JoinConstraint leftJoin(String name, String alias) {
        JoinConstraint result = leftJoin(name);
        result.setAlias(alias);
        return result;
    }

    /**
     * Creates a left outer join on a node.
     *
     * @param name       the node name
     * @param constraint the archetype constraint to use for the collection node
     * @return a new left outer join constraint
     */
    public static JoinConstraint leftJoin(String name, BaseArchetypeConstraint constraint) {
        return new CollectionNodeConstraint(name, constraint).setJoinType(JoinConstraint.JoinType.LeftOuterJoin);
    }

    /**
     * Creates a left outer join on a node.
     *
     * @param name       the node name
     * @param alias      an alias for the join. May be <tt>null</tt>
     * @param constraint the archetype constraint to use for the collection node
     * @return a new left outer join constraint
     */
    public static JoinConstraint leftJoin(String name, String alias, BaseArchetypeConstraint constraint) {
        JoinConstraint result = leftJoin(name, constraint);
        result.setAlias(alias);
        return result;
    }

    /**
     * Creates a new <em>and</em> constraint.
     *
     * @param constraints the constraints to AND together
     * @return a new <em>and</em> constraint
     */
    public static AndConstraint and(IConstraint... constraints) {
        AndConstraint result = new AndConstraint();
        result.setConstraints(Arrays.asList(constraints));
        return result;
    }

    /**
     * Creates a new <em>or</em> constraint.
     *
     * @param constraints the constraints to OR together
     * @return a new <em>or</em> constraint
     */
    public static OrConstraint or(IConstraint... constraints) {
        OrConstraint result = new OrConstraint();
        result.setConstraints(Arrays.asList(constraints));
        return result;
    }

    /**
     * Creates a new <em>sort</em> constraint on a node.
     * <p/>
     * The node will be sorted in ascending order.
     *
     * @param name the node name. May be qualified
     * @return a new sort constraint
     */
    public static SortConstraint sort(String name) {
        return sort(null, name);
    }

    /**
     * Creates a new <em>sort</em> constraint on a node.
     * <p/>
     * The node will be sorted in ascending order.
     *
     * @param alias the type alias. May be <tt>null</tt>
     * @param name  the node name
     * @return a new sort constraint
     */
    public static SortConstraint sort(String alias, String name) {
        return sort(alias, name, true);
    }

    /**
     * Creates a new <em>sort</em> constraint on a node.
     *
     * @param name      the node name. May be qualified
     * @param ascending whether to sort in ascending or descending order
     * @return a new sort constraint
     */
    public static SortConstraint sort(String name, boolean ascending) {
        return sort(null, name, ascending);
    }

    /**
     * Creates a new <em>sort</em> constraint on a node.
     *
     * @param alias     the type alias. May be <tt>null</tt>
     * @param name      the node name
     * @param ascending whether to sort in ascending or descending order
     * @return a new sort constraint
     */
    public static SortConstraint sort(String alias, String name, boolean ascending) {
        return new NodeSortConstraint(alias, name, ascending);
    }

    /**
     * Creates a new <em>not</em> constraint.
     *
     * @param constraint the constraint to negate
     * @return a new not constraint
     */
    public static NotConstraint not(IConstraint constraint) {
        return new NotConstraint(constraint);
    }

    /**
     * Creates a new <em>exists</em> constraint.
     *
     * @param query the sub-query
     * @return a new exists constraint
     */
    public static ExistsConstraint exists(ArchetypeQuery query) {
        return new ExistsConstraint(query);
    }

    /**
     * Creates a new <em> not exists</em> constraint.
     *
     * @param query the sub-query
     * @return a new not exists constraint
     */
    public static NotConstraint notExists(ArchetypeQuery query) {
        return not(exists(query));
    }

    /**
     * Creates a new sub-query.
     *
     * @param shortName the archetype short name to query
     * @param alias     the alias to use
     * @return a new sub-query
     */
    public static ArchetypeQuery subQuery(String shortName, String alias) {
        return new ArchetypeQuery(shortName(alias, shortName));
    }

    /**
     * Creates a new sub-query.
     *
     * @param shortNames the archetype short name to query
     * @param alias      the alias to use
     * @return a new sub-query
     */
    public static ArchetypeQuery subQuery(String[] shortNames, String alias) {
        ShortNameConstraint constraint = shortName(shortNames);
        constraint.setAlias(alias);
        return new ArchetypeQuery(constraint);
    }

}
