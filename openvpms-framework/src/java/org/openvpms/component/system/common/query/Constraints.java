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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.component.system.common.query;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObjectReference;


/**
 * Helper class for creating constraints.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Constraints {

    /**
     * Creates an <em>equal</em> constraint for the named node.
     *
     * @param name  the node name
     * @param value the value
     * @return a new <em>equal</em> constraint
     */
    public static NodeConstraint eq(String name, Object value) {
        return new NodeConstraint(name, value);
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
     * Creates an <em>not-equal</em> constraint for the named node.
     *
     * @param name  the node name
     * @param value the value
     * @return a new <em>not-equal</em> constraint
     */
    public static NodeConstraint ne(String name, Object value) {
        return new NodeConstraint(name, RelationalOp.NE, value);
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
     * @param shortName the short name to constrain on. May contain wildcards.
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
     * Creates a new <em>short-name</em> constraint for active/inactive instances.
     *
     * @param alias     the constraint alias
     * @param shortName the short name to constrain on. May contain wildcards.
     * @param activeOnly if <tt>true</tt> only query active objects
     * @return a new <em>short-name</em> constraint
     */
    public static ShortNameConstraint shortName(String alias, String shortName, boolean activeOnly) {
        return new ShortNameConstraint(alias, shortName, false, activeOnly);
    }

    /**
     * Creates a new <em>short-name</em> constraint for both active/inactive and primary/non-primary and instances.
     *
     * @param shortNames the short names to constrain on. May contain wildcards.
     * @return a new <em>short-name</em> constraint
     */
    public static ShortNameConstraint shortName(String[] shortNames) {
        return shortName(shortNames, false);
    }

    /**
     * Creates a new <em>short-name</em> constraint for primary/non-primary and instances.
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
        return shortName(alias, shortNames,  false);
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
     * Creates a new object reference constraint.
     *
     * @param name      the node name
     * @param reference the object reference
     * @return a new object reference constraint
     */
    public static ObjectRefNodeConstraint ref(String name, IMObjectReference reference) {
        return new ObjectRefNodeConstraint(name, reference);
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
     * @param name       the node name
     * @param constraint the archetype constraint to use for the collection node
     * @return a new left outer join constraint
     */
    public static JoinConstraint leftJoin(String name, BaseArchetypeConstraint constraint) {
        return new CollectionNodeConstraint(name, constraint).setJoinType(JoinConstraint.JoinType.LeftOuterJoin);
    }

    /**
     * Creates a new <em>and</em> constraint.
     *
     * @param lhs the left hand side
     * @param rhs the right hand side
     * @return a new <em>and</em> constraint
     */
    public static AndConstraint and(IConstraint lhs, IConstraint rhs) {
        AndConstraint result = new AndConstraint();
        result.add(lhs);
        result.add(rhs);
        return result;
    }

    /**
     * Creates a new <em>or</em> constraint.
     *
     * @param lhs the left hand side
     * @param rhs the right hand side
     * @return a new <em>or</em> constraint
     */
    public static OrConstraint or(IConstraint lhs, IConstraint rhs) {
        OrConstraint result = new OrConstraint();
        result.add(lhs);
        result.add(rhs);
        return result;
    }

}
