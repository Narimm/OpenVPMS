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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */


package org.openvpms.component.system.common.query;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.archetype.ArchetypeId;


/**
 * Defines a constraint on a collection node, where the elements of the
 * collection are archetyped instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class CollectionNodeConstraint extends JoinConstraint {

    /**
     * The node name.
     */
    private final String nodeName;


    /**
     * Constructor to initialize the node name.
     *
     * @param nodeName   the node name, optionaly prefixed by the type alias
     * @param activeOnly constraint to active only instances
     */
    public CollectionNodeConstraint(String nodeName, boolean activeOnly) {
        this(nodeName, new ArchetypeConstraint(getUnqualifiedName(nodeName), false, activeOnly));
    }

    /**
     * Constructor to initialize the node name. It implies that activeOnly is
     * false.
     *
     * @param nodeName the node name, optionaly prefixed by the type alias
     */
    public CollectionNodeConstraint(String nodeName) {
        this(nodeName, false);
    }

    /**
     * Create a query for the specified archetype id.
     *
     * @param nodeName    the node name, optionaly prefixed by the type alias
     * @param archetypeId a valid archetype identity
     * @param activeOnly  constraint to active only objects
     */
    public CollectionNodeConstraint(String nodeName, ArchetypeId archetypeId, boolean activeOnly) {
        this(nodeName, new ArchetypeIdConstraint(getUnqualifiedName(nodeName), archetypeId, activeOnly));
    }

    /**
     * Create an instance of this query specifying one or more elements.
     * Any of the parameters can be null or may  include the wild card
     * character.
     *
     * @param nodeName    the node name, optionaly prefixed by the type alias
     * @param rmName      the reference model name (optional)
     * @param entityName  the entity name (optional)
     * @param conceptName the concept name (optional)
     * @param primaryOnly only deal with primary archetypes
     * @param activeOnly  constraint to active only objects
     */
    @Deprecated
    public CollectionNodeConstraint(String nodeName, String rmName, String entityName, String conceptName,
                                    boolean primaryOnly, boolean activeOnly) {
        this(nodeName, new LongNameConstraint(rmName, entityName, conceptName, primaryOnly, activeOnly));
    }

    /**
     * Create an instance of this constraint with the specified short name.
     *
     * @param nodeName    the node name, optionally prefixed by the type alias
     * @param shortName   the short name
     * @param primaryOnly only deal with primary archetypes
     * @param activeOnly  constraint to active only objects
     */
    public CollectionNodeConstraint(String nodeName, String shortName, boolean primaryOnly, boolean activeOnly) {
        this(nodeName, new ShortNameConstraint(getUnqualifiedName(nodeName), shortName, primaryOnly, activeOnly));
    }

    /**
     * Create an instance of this class with the specified archetype short names.
     *
     * @param nodeName    the node name, optionally prefixed by the type alias
     * @param shortNames  an array of archetype short names
     * @param primaryOnly only deal with primary archetypes
     * @param activeOnly  constraint to active only objects
     */
    public CollectionNodeConstraint(String nodeName, String[] shortNames, boolean primaryOnly, boolean activeOnly) {
        this(nodeName, new ShortNameConstraint(getUnqualifiedName(nodeName), shortNames, primaryOnly, activeOnly));
    }

    /**
     * Construct a constraint on a collection node with the specified constraint.
     *
     * @param nodeName   the node name, optionally prefixed by the type alias
     * @param constraint the archetype constraint to use for the collection node
     */
    public CollectionNodeConstraint(String nodeName, BaseArchetypeConstraint constraint) {
        super(constraint);
        if (StringUtils.isEmpty(nodeName)) {
            throw new ArchetypeQueryException(
                    ArchetypeQueryException.ErrorCode.MustSpecifyNodeName);
        }
        if (constraint.getAlias() == null) {
            setAlias(getUnqualifiedName(nodeName));
        }
        this.nodeName = nodeName;
    }

    /**
     * Returns the node name.
     *
     * @return the node name
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * Returns a node name minus any type alias.
     *
     * @return the node name minus any type alias
     */
    public String getUnqualifiedName() {
        return getUnqualifiedName(nodeName);
    }

    /**
     * Add the specified constraint to the container.
     *
     * @param constraint the constraint to add
     * @return this constraint
     */
    @Override
    public CollectionNodeConstraint add(IConstraint constraint) {
        super.add(constraint);
        return this;
    }

    /**
     * Remove the specified constraint from the container.
     *
     * @param constraint the constraint to remove
     * @return this constraint
     */
    @Override
    public CollectionNodeConstraint remove(IConstraint constraint) {
        super.remove(constraint);
        return this;
    }

    /**
     * Returns a node name minus any type alias.
     *
     * @param nodeName the node name
     * @return the node name minus any type alias
     */
    private static String getUnqualifiedName(String nodeName) {
        int index = nodeName.indexOf(".");
        return (index != -1) ? nodeName.substring(index + 1) : nodeName;
    }

}
