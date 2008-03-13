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


package org.openvpms.component.business.service.archetype.query;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;
import static org.openvpms.component.business.service.archetype.query.QueryBuilderException.ErrorCode.*;
import org.openvpms.component.business.service.archetype.query.QueryContext.LogicalOperator;
import org.openvpms.component.system.common.query.AndConstraint;
import org.openvpms.component.system.common.query.ArchetypeConstraint;
import org.openvpms.component.system.common.query.ArchetypeIdConstraint;
import org.openvpms.component.system.common.query.ArchetypeNodeConstraint;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeSortConstraint;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.IdConstraint;
import static org.openvpms.component.system.common.query.JoinConstraint.JoinType.LeftOuterJoin;
import org.openvpms.component.system.common.query.LongNameConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.component.system.common.query.SelectConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * The builder is responsible for building the HQL from an
 * {@link ArchetypeQuery} instance.
 * <p/>
 * NOTE Ths archetype service is dependent on HQL. We need to make this
 * runtime configurable.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class QueryBuilder {

    /**
     * The archetype descriptor cache.
     */
    private final IArchetypeDescriptorCache cache;

    /**
     * List of select constraints encountered while processing the query.
     * These must be handled last in order to resolve types associated with
     * aliases.
     */
    private List<SelectConstraint> select = new ArrayList<SelectConstraint>();


    /**
     * Create an instance of the builder.
     *
     * @param cache the archetype descriptor cache
     */
    public QueryBuilder(IArchetypeDescriptorCache cache) {
        this.cache = cache;
    }

    /**
     * Build the HQL from the specified {@link ArchetypeQuery}.
     *
     * @param query archetype service
     * @return QueryContext the built hql
     */
    public QueryContext build(ArchetypeQuery query) {
        select.clear();
        if (query == null || query.getArchetypeConstraint() == null) {
            throw new QueryBuilderException(
                    QueryBuilderException.ErrorCode.NullQuery);
        }

        QueryContext context = new QueryContext(query.isDistinct());
        processConstraint(query.getArchetypeConstraint(), context);
        for (SelectConstraint constraint : select) {
            process(constraint, context);
        }

        return context;
    }

    /**
     * Process an {@link SelectConstraint}.
     *
     * @param constraint the constraint to process
     * @param context    the hql context
     */
    private void process(SelectConstraint constraint, QueryContext context) {
        if (StringUtils.isEmpty(constraint.getNodeName())
                && StringUtils.isEmpty(constraint.getAlias())) {
            throw new QueryBuilderException(
                    InvalidQualifiedName, constraint.getName());
        }

        TypeSet types = context.getTypeSet(constraint.getAlias());
        if (types == null) {
            throw new QueryBuilderException(
                    InvalidQualifiedName, constraint.getName());
        }
        String property = null;
        if (constraint.getNodeName() != null) {
            NodeDescriptor ndesc = getMatchingNodeDescriptor(
                    types.getDescriptors(), constraint.getNodeName());
            if (ndesc == null) {
                throw new QueryBuilderException(NoNodeDescriptorForName,
                                                constraint.getNodeName());
            }
            // get the name of the attribute
            property = getProperty(ndesc);
        }

        context.addSelectConstraint(types.getAlias(), constraint.getNodeName(),
                                    property);
    }

    /**
     * Process an {@link ArchetypeIdConstraint}.
     *
     * @param constraint the archetype id constraint to process
     * @param context    the hql context
     */
    private void process(ArchetypeIdConstraint constraint,
                         QueryContext context) {

        context.pushLogicalOperator(LogicalOperator.And);
        context.pushTypeSet(getTypeSet(constraint));

        // process common portion of constraint
        processArchetypeConstraint(constraint, context);

        // pop the stack when we have finished processing this constraint
        context.popTypeSet();
        context.popLogicalOperator();
    }

    /**
     * Process the common portion of {@link ArchetypeIdConstraint}
     *
     * @param constraint the archetype id constrain to process
     * @param context    the hql context
     */
    private void processArchetypeConstraint(ArchetypeIdConstraint constraint,
                                            QueryContext context) {
        ArchetypeId id = constraint.getArchetypeId();
        String alias = constraint.getAlias();

        boolean leftOuterJoin = context.peekJoinType() == LeftOuterJoin;
        if (leftOuterJoin) {
            context.pushLogicalOperator(LogicalOperator.Or);
            context.addWhereConstraint(alias, "archetypeId.shortName",
                                       RelationalOp.EQ,  id.getShortName());
            context.addWhereConstraint(alias, "archetypeId.shortName",
                                       RelationalOp.IsNULL, null);
            context.popLogicalOperator();
        } else {
            context.addWhereConstraint(alias, "archetypeId.shortName",
                                       RelationalOp.EQ, id.getShortName());
        }

        // process the active flag
        addActiveConstraint(constraint, context);

        // process the embedded constraints.
        for (IConstraint oc : constraint.getConstraints()) {
            processConstraint(oc, context);
        }
    }

    /**
     * Process the archetype short name constraint
     *
     * @param constraint the archetype short name constraint
     * @param context    the hql context
     */
    private void process(ShortNameConstraint constraint,
                         QueryContext context) {
        // check that at least one short name is specified
        if (constraint.getShortNames() == null
                || constraint.getShortNames().length == 0) {
            throw new QueryBuilderException(
                    QueryBuilderException.ErrorCode.NoShortNamesSpeified);
        }

        context.pushLogicalOperator(LogicalOperator.And);
        context.pushTypeSet(getTypeSet(constraint));

        // process the common portion of the archetype constraint
        processArchetypeConstraint(constraint, context);

        // pop the stack when we have finished processing this constraint
        context.popTypeSet();
        context.popLogicalOperator();
    }

    /**
     * Process the common portion of the archetype short name constraint.
     *
     * @param constraint the archetype short name constraint
     * @param context    the hql context
     */
    private void processArchetypeConstraint(ShortNameConstraint constraint,
                                            QueryContext context) {

        String alias = constraint.getAlias();
        String[] shortNames = constraint.getShortNames();
        boolean leftOuterJoin = context.peekJoinType() == LeftOuterJoin;
        if (shortNames.length > 1 || leftOuterJoin) {
            context.pushLogicalOperator(LogicalOperator.Or);
        }

        for (String shortName : shortNames) {
            // process the entity name
            if (shortName.endsWith("*") || shortName.startsWith("*")) {
                context.addWhereConstraint(alias, "archetypeId.shortName",
                                           RelationalOp.EQ,
                                           shortName.replace("*", "%"));
            } else {
                context.addWhereConstraint(alias, "archetypeId.shortName",
                                           RelationalOp.EQ, shortName);
            }
        }
        if (leftOuterJoin) {
            context.addWhereConstraint(alias, "archetypeId.shortName",
                                       RelationalOp.IsNULL, null);
        }

        if (shortNames.length > 1 || leftOuterJoin) {
            context.popLogicalOperator();
        }

        // process the active flag
        addActiveConstraint(constraint, context);

        // process the embedded constraints.
        for (IConstraint oc : constraint.getConstraints()) {
            processConstraint(oc, context);
        }
    }

    private void addActiveConstraint(BaseArchetypeConstraint constraint,
                                     QueryContext context) {
        boolean leftOuterJoin = context.peekJoinType() == LeftOuterJoin;
        if (constraint.isActiveOnly()) {
            String alias = constraint.getAlias();
            if (leftOuterJoin) {
                context.pushLogicalOperator(LogicalOperator.Or);
                context.addWhereConstraint(alias, "active", RelationalOp.EQ,
                                           true);
                context.addWhereConstraint(alias, "active", RelationalOp.IsNULL,
                                           null);
                context.popLogicalOperator();
            } else {
                context.addWhereConstraint(alias, "active", RelationalOp.EQ,
                                           true);
            }
        }
    }

    /**
     * Process this archetype constraint
     *
     * @param constraint a fundamental archetype constraint
     * @param context    the hql context
     */
    private void processArchetypeConstraint(ArchetypeConstraint constraint,
                                            QueryContext context) {
        // process the active flag
        addActiveConstraint(constraint, context);

        // process the embedded constraints.
        for (IConstraint oc : constraint.getConstraints()) {
            processConstraint(oc, context);
        }
    }

    /**
     * Process an archetype constraint where the reference model name, entity
     * name and concept name are specified.
     *
     * @param constraint the constraint
     * @param context    the hql context
     */
    private void process(LongNameConstraint constraint, QueryContext context) {
        context.pushLogicalOperator(LogicalOperator.And);
        context.pushTypeSet(getTypeSet(constraint));

        // process the common portion of the archetype constraint
        processArchetypeConstraint(constraint, context);

        // pop the stack when we have finished processing this constraint
        context.popTypeSet();
        context.popLogicalOperator();
    }

    /**
     * Process an common portion of this archetype constraint
     *
     * @param constraint the constraint
     * @param context    the hql context
     */
    private void processArchetypeConstraint(LongNameConstraint constraint,
                                            QueryContext context) {
        String alias = constraint.getAlias();
        String entityName = constraint.getEntityName();
        String conceptName = constraint.getConceptName();

        StringBuffer shortName = new StringBuffer();
        if (StringUtils.isEmpty(entityName)) {
            shortName.append("*.");
        } else {
            shortName.append(entityName);
            shortName.append(".");
        }
        if (StringUtils.isEmpty(conceptName)) {
            shortName.append("*");
        } else {
            shortName.append(conceptName);
        }
        context.addWhereConstraint(alias, "archetypeId.shortName",
                                   RelationalOp.EQ,
                                   shortName.toString().replace("*", "%"));
        // process the active flag
        if (constraint.isActiveOnly()) {
            context.addWhereConstraint(alias, "active", RelationalOp.EQ, true);
        }

        // process the embedded constraints.
        for (IConstraint oc : constraint.getConstraints()) {
            processConstraint(oc, context);
        }

    }

    /**
     * Process the embedded constraints using the logical 'or' operator
     *
     * @param constraint the or constraint
     * @param context    the hql context
     */
    private void process(OrConstraint constraint, QueryContext context) {
        // push the operator
        context.pushLogicalOperator(LogicalOperator.Or);

        // process the embedded constraints.
        for (IConstraint oc : constraint.getConstraints()) {
            processConstraint(oc, context);
        }

        // pop the stack attributes
        context.popLogicalOperator();
    }

    /**
     * Prcoess the embedded constraints using the logical 'and' operator
     *
     * @param constraint the and constraint
     * @param context    the hql context
     */
    private void process(AndConstraint constraint, QueryContext context) {
        // push the operator
        context.pushLogicalOperator(LogicalOperator.And);

        // process the embedded constraints.
        for (IConstraint oc : constraint.getConstraints()) {
            processConstraint(oc, context);
        }

        // pop the stack attributes
        context.popLogicalOperator();
    }

    /**
     * Process the node constraint.
     *
     * @param constraint the node constraint
     * @param context    the context
     */
    private void process(NodeConstraint constraint, QueryContext context) {
        String property = getQualifiedPropertyName(constraint.getNodeName(),
                                                   constraint.getAlias(),
                                                   context);
        context.addWhereConstraint(property, constraint);
    }

    /**
     * Process the archetype node constraint
     *
     * @param constraint the archetype node constraint
     * @param context    the context
     */
    private void process(ArchetypeNodeConstraint constraint,
                         QueryContext context) {
        context.addWhereConstraint(null, "archetypeId.shortName",
                                   constraint.getOperator(),
                                   constraint.getParameter());
    }

    /**
     * Process the specified constraint on an object reference node.
     *
     * @param constraint the object reference node constraint
     * @param context    the query context
     */
    private void process(ObjectRefNodeConstraint constraint,
                         QueryContext context) {
        // get the name of the attribute
        RelationalOp op = constraint.getOperator();
        String property = getQualifiedPropertyName(constraint.getNodeName(),
                                                   constraint.getAlias(),
                                                   context);
        if (constraint.getObjectReference() != null) {
            IMObjectReference ref = constraint.getObjectReference();
            context.addWhereConstraint(property + ".linkId", op,
                                       ref.getLinkId());
        } else {
            ArchetypeId id = constraint.getArchetypeId();
            context.addWhereConstraint(property + ".archetypeId.shortName", op,
                                       id.getShortName());
        }
    }

    /**
     * Process a link constraint.
     *
     * @param constraint the link constraint
     * @param context    the query context
     */
    private void process(IdConstraint constraint, QueryContext context) {
        String source = getAliasOrQualifiedName(constraint.getSourceName(),
                                                context);
        String target = getAliasOrQualifiedName(constraint.getTargetName(),
                                                context);
        context.addPropertyWhereConstraint(source + ".linkId",
                                           constraint.getOperator(),
                                           target + ".linkId");
    }

    /**
     * Process the specified object reference constraint.
     *
     * @param constraint the object reference constraint
     * @param context    the query context
     */
    private void process(ObjectRefConstraint constraint,
                         QueryContext context) {
        if (constraint.getArchetypeId() == null) {
            throw new QueryBuilderException(InvalidObjectReferenceConstraint,
                                            constraint);
        }

        ArchetypeId id = constraint.getArchetypeId();
        TypeSet types = TypeSet.create(constraint, cache);

        context.pushLogicalOperator(LogicalOperator.And);
        context.pushTypeSet(types);

        String alias = constraint.getAlias();
        context.addWhereConstraint(alias, "archetypeId.shortName",
                                   RelationalOp.EQ, id.getShortName());
        context.addWhereConstraint(alias, "linkId", RelationalOp.EQ,
                                   constraint.getLinkId());

        // process the embedded constraints.
        for (IConstraint oc : constraint.getConstraints()) {
            processConstraint(oc, context);
        }

        // pop the stack when we have finished processing this constraint
        context.popTypeSet();
        context.popLogicalOperator();
    }

    /**
     * Process the collection constraints, which involves a join across tables.
     *
     * @param constraint the collection constraint
     * @param context    the context
     */
    private void process(CollectionNodeConstraint constraint,
                         QueryContext context) {
        TypeSet types = context.peekTypeSet();
        final String nodeName = getUnqualifiedName(constraint.getNodeName());
        NodeDescriptor ndesc = getMatchingNodeDescriptor(types.getDescriptors(),
                                                         nodeName);

        if (ndesc == null) {
            throw new QueryBuilderException(NoNodeDescriptorForName,
                                            nodeName);
        }

        // push the new type
        BaseArchetypeConstraint archetypeConstraint
                = constraint.getArchetypeConstraint();
        if (archetypeConstraint instanceof ArchetypeConstraint) {
            types = TypeSet.create((ArchetypeConstraint) archetypeConstraint,
                                   ndesc, cache);
        } else {
            types = getTypeSet(archetypeConstraint);
        }
        context.pushTypeSet(types, getProperty(ndesc),
                            constraint.getJoinType());

        // process common portion of constraint
        if (archetypeConstraint != null) {
            processArchetypeConstraint(archetypeConstraint, context);
        }

        // pop the stack when we have finished processing this constraint
        context.popTypeSet();
    }

    /**
     * Process the specified archetype sort constraint.
     *
     * @param constraint an archetype sort constraint
     * @param context    the context
     */
    private void process(ArchetypeSortConstraint constraint,
                         QueryContext context) {
        context.addSortConstraint(constraint.getAlias(), "" +
                "archetypeId.shortName", constraint.isAscending());
    }

    /**
     * Process the specified sort constraint on a property.
     *
     * @param constraint a sort constraint on a node
     * @param context    the context
     */
    private void process(NodeSortConstraint constraint, QueryContext context) {
        TypeSet types = context.getTypeSet(constraint.getAlias());
        if (types == null) {
            throw new QueryBuilderException(
                    InvalidQualifiedName, constraint.getNodeName());
        }

        NodeDescriptor ndesc = getMatchingNodeDescriptor(
                types.getDescriptors(), constraint.getNodeName());
        if (ndesc == null) {
            throw new QueryBuilderException(
                    NoNodeDescriptorForName, constraint.getNodeName());
        }

        // get the name of the attribute
        String property = getProperty(ndesc);
        context.addSortConstraint(constraint.getAlias(), property,
                                  constraint.isAscending());
    }

    /**
     * Return the property for the specified node descriptor. This will only
     * return a top level property. The property is derived from the descriptor's
     * path.
     *
     * @param ndesc the node descriptor
     * @return String
     */
    private String getProperty(NodeDescriptor ndesc) {
        // stip the leading /, if it exists
        String aprop = ndesc.getPath();
        if (aprop.startsWith("/")) {
            aprop = ndesc.getPath().substring(1);
        }

        // now check for any more / characters
        if (aprop.contains("/")) {
            throw new QueryBuilderException(
                    QueryBuilderException.ErrorCode.CanOnlySortOnTopLevelNodes,
                    ndesc.getName());
        }

        return aprop;
    }

    /**
     * Iterate through all the {@link ArchetypeDescriptor} instances and return
     * a typical {@link NodeDescriptor}. The only requirement is that the path
     * and the type are same for every archetype descriptor
     *
     * @param adescs   the set of archetype descriptors
     * @param nodeName the node to search for
     * @return a typical node descriptor
     * @throws ArchetypeServiceException
     */
    private NodeDescriptor getMatchingNodeDescriptor(
            Set<ArchetypeDescriptor> adescs, String nodeName) {
        NodeDescriptor matching = null;

        if (StringUtils.isEmpty(nodeName)) {
            return null;
        }

        // ensure the property is defined in all archetypes
        NodeDescriptor ndesc;

        for (ArchetypeDescriptor adesc : adescs) {
            ndesc = adesc.getNodeDescriptor(nodeName);
            if (ndesc == null) {
                throw new QueryBuilderException(
                        NoNodeDescWithName, adesc.getName(), nodeName);
            }

            // now check against the matching node descriptor
            if (matching == null) {
                matching = ndesc;
            } else {
                if (ndesc.getPath().equals(matching.getPath()) &&
                        ndesc.getType().equals(matching.getType())) {
                    // this descriptor matches the node descriptor
                } else {
                    throw new QueryBuilderException(NodeDescriptorsDoNotMatch,
                                                    nodeName);
                }
            }
        }

        return matching;
    }

    /**
     * Delegate to the appropraite type
     *
     * @param constraint
     * @return TypeSet
     */
    private TypeSet getTypeSet(BaseArchetypeConstraint constraint) {
        if (constraint instanceof ArchetypeIdConstraint) {
            return TypeSet.create((ArchetypeIdConstraint) constraint, cache);
        } else if (constraint instanceof ShortNameConstraint) {
            return TypeSet.create((ShortNameConstraint) constraint, cache);
        } else if (constraint instanceof LongNameConstraint) {
            return TypeSet.create((LongNameConstraint) constraint, cache);
        }

        return null;
    }

    /**
     * Delegate to the appropriate type
     *
     * @param constraint the constraint
     * @param context
     */
    private void processArchetypeConstraint(BaseArchetypeConstraint constraint,
                                            QueryContext context) {
        if (constraint instanceof ArchetypeIdConstraint) {
            processArchetypeConstraint((ArchetypeIdConstraint) constraint,
                                       context);
        } else if (constraint instanceof ShortNameConstraint) {
            processArchetypeConstraint(
                    (ShortNameConstraint) constraint,
                    context);
        } else if (constraint instanceof LongNameConstraint) {
            processArchetypeConstraint((LongNameConstraint) constraint,
                                       context);
        } else if (constraint instanceof ArchetypeConstraint) {
            processArchetypeConstraint((ArchetypeConstraint) constraint,
                                       context);
        } else {
            // raise an exception
        }
    }

    /**
     * Process the appropriate constraint
     *
     * @param constraint the constraint to process
     * @param context    the hql context
     */
    private void processConstraint(IConstraint constraint,
                                   QueryContext context) {
        if (constraint instanceof SelectConstraint) {
            select.add((SelectConstraint) constraint);
        } else if (constraint instanceof ObjectRefConstraint) {
            process((ObjectRefConstraint) constraint, context);
        } else if (constraint instanceof ArchetypeIdConstraint) {
            process((ArchetypeIdConstraint) constraint, context);
        } else if (constraint instanceof LongNameConstraint) {
            process((LongNameConstraint) constraint, context);
        } else if (constraint instanceof ShortNameConstraint) {
            process((ShortNameConstraint) constraint, context);
        } else if (constraint instanceof CollectionNodeConstraint) {
            process((CollectionNodeConstraint) constraint, context);
        } else if (constraint instanceof ArchetypeNodeConstraint) {
            process((ArchetypeNodeConstraint) constraint, context);
        } else if (constraint instanceof NodeConstraint) {
            process((NodeConstraint) constraint, context);
        } else if (constraint instanceof ObjectRefNodeConstraint) {
            process((ObjectRefNodeConstraint) constraint, context);
        } else if (constraint instanceof IdConstraint) {
            process((IdConstraint) constraint, context);
        } else if (constraint instanceof AndConstraint) {
            process((AndConstraint) constraint, context);
        } else if (constraint instanceof OrConstraint) {
            process((OrConstraint) constraint, context);
        } else if (constraint instanceof NodeSortConstraint) {
            process((NodeSortConstraint) constraint, context);
        } else if (constraint instanceof ArchetypeSortConstraint) {
            process((ArchetypeSortConstraint) constraint, context);
        } else {
            throw new QueryBuilderException(
                    QueryBuilderException.ErrorCode.ConstraintTypeNotSupported,
                    constraint.getClass().getName());
        }
    }

    /**
     * Returns a property name for a node name, qualified by its type alias.
     *
     * @param nodeName the node name
     * @param alias    the type alias. If <code>null</code> uses the current
     *                 type
     * @param context  the query context
     * @return the property corresponding to <code>name</code> prefixed with
     *         the type alias
     */
    private String getQualifiedPropertyName(String nodeName, String alias,
                                            QueryContext context) {
        if (StringUtils.isEmpty(nodeName)) {
            throw new QueryBuilderException(MustSpecifyNodeName);
        }

        String result;
        TypeSet types = context.getTypeSet(alias);
        if (types == null) {
            throw new QueryBuilderException(NoNodeDescriptorForName, nodeName);
        }
        NodeDescriptor desc = getMatchingNodeDescriptor(types.getDescriptors(),
                                                        nodeName);
        if (desc == null) {
            throw new QueryBuilderException(NoNodeDescriptorForName, nodeName);
        }

        // get the name of the attribute
        String property = getProperty(desc);
        if (alias == null) {
            alias = types.getAlias();
        }
        result = alias + "." + property;
        return result;
    }

    /**
     * Returns a node name minus any type alias.
     *
     * @param nodeName the node name
     * @return the node name minus any type alias
     */
    private String getUnqualifiedName(String nodeName) {
        int index = nodeName.indexOf(".");
        return (index != -1) ? nodeName.substring(index + 1) : nodeName;
    }

    /**
     * Returns a type alias or qualified property name, depending on whether the
     * supplied name refers to a type alias or a node name.
     *
     * @param name    the name. May be a type alias or node name
     * @param context the query context
     * @return the property corresponding to <code>name</code> prefixed with
     *         the type alias if <code>name</code> refers to a node, or
     *         the <code>name</code> unchanged if it refers to a type alias
     */
    private String getAliasOrQualifiedName(String name, QueryContext context) {
        String property;
        int index = name.indexOf(".");
        if (index == -1) {
            if (context.getTypeSet(name) == null) {
                throw new QueryBuilderException(InvalidQualifiedName, name);
            }
            property = name;
        } else {
            String alias = name.substring(0, index);
            String nodeName = name.substring(index + 1);
            property = getQualifiedPropertyName(nodeName, alias, context);
        }
        return property;
    }

}
