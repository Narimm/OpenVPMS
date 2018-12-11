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

package org.openvpms.component.business.dao.hibernate.im.query;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.dao.hibernate.im.common.CompoundAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.model.archetype.NodeDescriptor;
import org.openvpms.component.model.object.IMObject;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.query.criteria.AggregateExpression;
import org.openvpms.component.system.common.query.criteria.BetweenPredicate;
import org.openvpms.component.system.common.query.criteria.ComparisonPredicate;
import org.openvpms.component.system.common.query.criteria.CriteriaQueryImpl;
import org.openvpms.component.system.common.query.criteria.ExistsPredicate;
import org.openvpms.component.system.common.query.criteria.ExpressionImpl;
import org.openvpms.component.system.common.query.criteria.FromImpl;
import org.openvpms.component.system.common.query.criteria.InPredicate;
import org.openvpms.component.system.common.query.criteria.JoinImpl;
import org.openvpms.component.system.common.query.criteria.MappedCriteriaQuery;
import org.openvpms.component.system.common.query.criteria.NotPredicate;
import org.openvpms.component.system.common.query.criteria.NullPredicate;
import org.openvpms.component.system.common.query.criteria.PathImpl;
import org.openvpms.component.system.common.query.criteria.PredicateImpl;
import org.openvpms.component.system.common.query.criteria.RootImpl;
import org.openvpms.component.system.common.query.criteria.SubqueryImpl;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A factory for creating {@link MappedCriteriaQuery} from {@link CriteriaQueryImpl}.
 *
 * @author Tim Anderson
 */
public class MappedCriteriaQueryFactory {

    /**
     * The JPA criteria builder.
     */
    private final CriteriaBuilder builder;

    /**
     * The assembler.
     */
    private final CompoundAssembler assembler;

    /**
     * Constructs a {@link MappedCriteriaQueryFactory}.
     *
     * @param builder   the JPA criteria builder
     * @param assembler the assemble
     */
    public MappedCriteriaQueryFactory(CriteriaBuilder builder, CompoundAssembler assembler) {
        this.builder = builder;
        this.assembler = assembler;
    }

    /**
     * Creates a JPA {@code CriteriaQuery} from an {@link CriteriaQueryImpl}.
     *
     * @param query the query
     * @return the new JPA query
     */
    public <X, Y> MappedCriteriaQuery<Y> createCriteriaQuery(CriteriaQueryImpl<X> query) {
        Class<Y> impl = (Class<Y>) getImpl(query.getResultType());
        CriteriaQuery<Y> result = builder.createQuery(impl);
        Map<TupleElement<?>, TupleElement<?>> built = new HashMap<>();
        List<Predicate> predicates = new ArrayList<>();

        // build roots
        for (RootImpl<? extends IMObject> root : query.getRoots()) {
            Class<? extends IMObject> type = root.getJavaType();
            Root<IMObjectDO> from = result.from((Class<IMObjectDO>) getImpl(type));
            if (root.getAlias() != null) {
                from.alias(root.getAlias());
            }
            built.put(root, from);
            Predicate archetype = getArchetypePredicate(from, root.getArchetypes());
            predicates.add(archetype);
            for (JoinImpl<?, ?> join : root.getJoins()) {
                buildJoin(from, join, built);
            }
        }

        // build subqueries
        for (SubqueryImpl<?> subquery : query.getSubqueries()) {
            buildSubquery(subquery, result, built);
        }

        // build selection
        if (query.getSelection() != null) {
            buildSelect(query.getSelection(), result, built);
        } else if (query.getMultiselect() != null) {
            buildMultiselect(query.getMultiselect(), result, built);
        }
        result.distinct(query.getDistinct());

        // build where
        Expression<Boolean> where = query.getWhere();
        if (where != null) {
            Expression expression = buildExpression(where, built);
            if (expression instanceof Predicate) {
                predicates.add((Predicate) expression);
                result.where(predicates.toArray(new Predicate[0]));
            } else {
                Predicate and = builder.and(predicates.toArray(new Predicate[0]));
                result.where(builder.and(and, expression));
            }
        } else {
            result.where(predicates.toArray(new Predicate[0]));
        }

        // build group by
        if (query.getGroupBy() != null) {
            List<Expression<?>> grouping = new ArrayList<>();
            for (Expression<?> expression : query.getGroupBy()) {
                grouping.add(buildExpression(expression, built));
            }
            result.groupBy(grouping);
        }

        // build having
        if (query.getHaving() != null) {
            Expression<?> expression = buildExpression(query.getHaving(), built);
            result.having(cast(expression, Boolean.class));
        }

        // build order by
        if (query.getOrderBy() != null) {
            List<Order> orderBy = new ArrayList<>();
            for (Order order : query.getOrderBy()) {
                Expression<?> expression = buildExpression(order.getExpression(), built);
                if (order.isAscending()) {
                    orderBy.add(builder.asc(expression));
                } else {
                    orderBy.add(builder.desc(expression));
                }
            }
            result.orderBy(orderBy);
        }
        return new MappedCriteriaQuery<>(result, built);
    }

    /**
     * Builds a subquery.
     *
     * @param subquery the subquery to map from
     * @param query    the query to add to
     * @param built    the set of built elements
     *                 \
     */
    private <X, Y> void buildSubquery(SubqueryImpl<X> subquery, CriteriaQuery<?> query,
                                      Map<TupleElement<?>, TupleElement<?>> built) {
        Class<Y> impl = (Class<Y>) getImpl(subquery.getJavaType());
        Subquery<Y> target = query.subquery(impl);
        built.put(subquery, target);
        List<Predicate> predicates = new ArrayList<>();
        for (RootImpl<? extends IMObject> root : subquery.getRoots()) {
            Class<? extends IMObject> type = root.getJavaType();
            Root<IMObjectDO> from = target.from((Class<IMObjectDO>) getImpl(type));
            if (root.getAlias() != null) {
                from.alias(root.getAlias());
            }
            built.put(root, from);
            Predicate archetype = getArchetypePredicate(from, root.getArchetypes());
            predicates.add(archetype);
            for (JoinImpl<?, ?> join : root.getJoins()) {
                buildJoin(from, join, built);
            }
        }

        if (subquery.getSelect() != null) {
            Expression<Y> selection = (Expression<Y>) buildSelection(subquery.getSelect(), built);
            target.select(selection);
        }
        target.distinct(subquery.getDistinct());

        Expression<Boolean> where = subquery.getWhere();
        if (where != null) {
            Expression<Boolean> expression = cast(buildExpression(where, built), Boolean.class);
            if (expression instanceof Predicate) {
                predicates.add((Predicate) expression);
                target.where(predicates.toArray(new Predicate[0]));
            } else {
                Predicate and = builder.and(predicates.toArray(new Predicate[0]));
                target.where(builder.and(and, expression));
            }
        } else {
            target.where(predicates.toArray(new Predicate[0]));
        }
    }

    /**
     * Builds a select clause.
     *
     * @param selection the selection to map from
     * @param query     the query to add to
     * @param built     the map of built from clauses to their JPA equivalents
     */
    @SuppressWarnings("unchecked")
    private <X, Y> void buildSelect(Selection<? super X> selection, CriteriaQuery<Y> query,
                                    Map<TupleElement<?>, TupleElement<?>> built) {
        Selection<? extends Y> target = (Selection<? extends Y>) buildSelection(selection, built);
        query.select(target);
    }

    /**
     * Builds a multi-select clause.
     *
     * @param selections the selections to map from
     * @param query      the query to add to
     * @param built      the map of built from clauses to their JPA equivalents
     */
    private <Y> void buildMultiselect(Selection<?>[] selections, CriteriaQuery<Y> query,
                                      Map<TupleElement<?>, TupleElement<?>> built) {
        Selection<?>[] targets = new Selection<?>[selections.length];
        for (int i = 0; i < selections.length; ++i) {
            targets[i] = buildSelection(selections[i], built);
        }
        query.multiselect(targets);
    }

    /**
     * Builds a selection.
     *
     * @param selection the selection to map from
     * @param built     the map of built from clauses to their JPA equivalents
     */
    private Selection<?> buildSelection(Selection<?> selection, Map<TupleElement<?>, TupleElement<?>> built) {
        Selection<?> result;
        if (selection instanceof FromImpl) {
            result = (Selection<?>) built.get(selection);
            if (result == null) {
                throw new IllegalArgumentException("Selection doesn't map to a From instance: " + selection);
            }
        } else if (selection instanceof PathImpl) {
            PathImpl path = (PathImpl) selection;
            if (Reference.class.isAssignableFrom(path.getJavaType())) {
                From<?, ?> parent = getParent(path, built);
                if (parent == null) {
                    throw new IllegalStateException("Can't select reference. Not a known root");
                }
                Path<?> reference;
                if (path.getNode() == null) {
                    // reference to self
                    reference = parent;
                } else {
                    // reference node
                    String[] parts = getNodePath(path);
                    if (parts.length == 1) {
                        reference = parent.get(parts[0]);
                        if (path.getAlias() != null) {
                            reference.alias(path.getAlias());
                        }
                    } else {
                        throw new IllegalArgumentException("Unsupported node " + path.getNode().getPath());
                    }
                }
                result = builder.construct(IMObjectReference.class, reference.get("archetypeId"), reference.get("id"),
                                           reference.get("linkId"));
            } else {
                From<?, ?> parent = getParent(path, built);
                if (parent == null) {
                    throw new IllegalStateException("Can't select path. Not a known root");
                }
                String[] parts = getNodePath(path);
                if (parts.length == 1) {
                    result = parent.get(parts[0]);
                } else if (parts.length == 2) {
                    // map node.
                    result = getDetailsValuePath(path, parent, parts[1]);
                } else {
                    throw new IllegalArgumentException("Unsupported node " + path.getNode().getPath());
                }
                if (path.getAlias() != null) {
                    result.alias(path.getAlias());
                }
            }
            built.put(selection, result);
        } else if (selection instanceof AggregateExpression) {
            result = buildAggregate((AggregateExpression) selection, built);
            built.put(selection, result);
        } else {
            throw new IllegalStateException("Selections of type " + selection.getClass() + " are not supported");
        }
        return result;
    }

    /**
     * Returns the path to the value of a node stored in a 'details' map.
     *
     * @param path   the path
     * @param parent the parent from clause
     * @param key    the map key
     * @return the path to the map value
     */
    private Path<?> getDetailsValuePath(PathImpl path, From<?, ?> parent, String key) {
        JoinType joinType = (parent instanceof Join) ? ((Join) parent).getJoinType() : JoinType.INNER;
        MapJoin<Object, Object, Object> mapJoin = parent.joinMap("details", joinType);
        mapJoin.on(builder.equal(mapJoin.key(), key));
        if (path.getAlias() != null) {
            mapJoin.alias(path.getAlias());
        }
        return mapJoin.value().get("value");
    }

    /**
     * Builds a join.
     *
     * @param from  the object being joined
     * @param join  the join to map from
     * @param built a map of already built JPA objects, keyed on their source objects
     */
    private void buildJoin(From<? extends IMObjectDO, ? extends IMObjectDO> from, JoinImpl<?, ?> join,
                           Map<TupleElement<?>, TupleElement<?>> built) {
        JoinType joinType = (join.getJoinType() == JoinImpl.JoinType.INNER) ? JoinType.INNER : JoinType.LEFT;
        String[] parts = getNodePath(join);
        if (parts.length != 1) {
            throw new IllegalArgumentException("Cannot join on " + join);
        }
        Join<? extends IMObjectDO, ? extends IMObjectDO> targetJoin = from.join(parts[0], joinType);
        if (join.getAlias() != null) {
            targetJoin.alias(join.getAlias());
        }
        built.put(join, targetJoin);
        Predicate archetypePredicate = getArchetypePredicate(targetJoin, join.getArchetypes());
        if (join.getExpression() != null) {
            Expression<Boolean> expression
                    = cast(buildExpression(join.getExpression(), built), Boolean.class);
            if (expression instanceof Predicate) {
                targetJoin.on(archetypePredicate, (Predicate) expression);
            } else {
                targetJoin.on(builder.and(archetypePredicate, expression));
            }
        } else {
            targetJoin.on(archetypePredicate);
        }
        for (JoinImpl<?, ?> nested : join.getJoins()) {
            buildJoin(targetJoin, nested, built);
        }
    }

    /**
     * Returns the parent {@code From} of a path.
     *
     * @param path  the path
     * @param built the built elements
     * @return the parent root
     */
    private From<?, ?> getParent(PathImpl path, Map<TupleElement<?>, TupleElement<?>> built) {
        PathImpl<?> parent = path.getParent();
        if (parent == null) {
            throw new IllegalStateException("Can't have an null parent when selecting by reference");
        }
        return (From<?, ?>) built.get(parent);
    }

    /**
     * Returns a predicate that restricts a path to a set of archetypes.
     *
     * @param path       the path
     * @param archetypes the archetypes
     * @return a new predicate
     */
    private Predicate getArchetypePredicate(Path<?> path, Set<String> archetypes) {
        Predicate result;
        String[] list = archetypes.toArray(new String[0]);
        if (list.length == 1) {
            result = builder.equal(path.get("archetypeId").get("shortName"), list[0]);
        } else {
            result = path.get("archetypeId").get("shortName").in((Object[]) list);
        }
        return result;
    }

    /**
     * Returns the implementation of a type.
     * <ul>
     * <li>for {@link IMObject} and sub-interfaces, this returns the corresponding {@link IMObjectDO} class</li>
     * <li>for {@link Tuple} this returns the {@code javax.persistence.Tuple} class</li>
     * <li>all other types are returned unchanged</li>
     * </ul>
     *
     * @param type the type
     * @return the implementation of the type
     */
    private Class<?> getImpl(Class<?> type) {
        Class result;
        if (IMObject.class.isAssignableFrom(type)) {
            result = assembler.getDOClass((Class<IMObject>) type);
            if (result == null) {
                throw new IllegalStateException("Invalid type " + type.getName());
            }
        } else if (Tuple.class.isAssignableFrom(type)) {
            result = javax.persistence.Tuple.class;
        } else {
            result = type;
        }
        return result;
    }

    /**
     * Builds a JPA expression from an {@link Expression}.
     *
     * @param expression the expression to map from
     * @param built      the map of built from clauses to their JPA equivalents
     * @return a JPA expression corresponding to that supplied
     */
    private Expression<?> buildExpression(Expression<?> expression, Map<TupleElement<?>, TupleElement<?>> built) {
        Expression<?> result = (Expression<?>) built.get(expression);
        if (result == null) {
            if (expression instanceof ComparisonPredicate) {
                result = buildComparison((ComparisonPredicate) expression, built);
            } else if (expression instanceof PathImpl) {
                result = buildPath((PathImpl) expression, built);
            } else if (expression instanceof AggregateExpression) {
                result = buildAggregate((AggregateExpression) expression, built);
            } else if (expression instanceof BetweenPredicate) {
                result = buildBetween((BetweenPredicate) expression, built);
            } else if (expression instanceof InPredicate) {
                result = buildIn((InPredicate) expression, built);
            } else if (expression instanceof NullPredicate) {
                result = buildNull((NullPredicate) expression, built);
            } else if (expression instanceof ExistsPredicate) {
                result = buildExists((ExistsPredicate) expression, built);
            } else if (expression instanceof NotPredicate) {
                result = buildNot((NotPredicate) expression, built);
            } else if (expression instanceof PredicateImpl) {
                result = buildPredicate((PredicateImpl) expression, built);
            } else {
                throw new IllegalArgumentException("Unsupported argument " + expression);
            }
        }
        return result;
    }

    /**
     * Builds an 'in' predicate.
     *
     * @param predicate the predicate to map from
     * @param built     the map of built elements to their JPA equivalents
     * @return a JPA expression corresponding to that supplied
     */
    private Expression<?> buildIn(InPredicate predicate, Map<TupleElement<?>, TupleElement<?>> built) {
        Expression<?> expression = buildExpression(predicate.getExpression(), built);
        if (predicate.getValues() != null) {
            return expression.in(predicate.getValues());
        }
        List<Expression<?>> values = new ArrayList<>();
        for (Expression<?> value : predicate.getExpressionValues()) {
            values.add(buildExpression(value, built));
        }
        return expression.in(values);
    }

    /**
     * Builds a 'null' predicate.
     *
     * @param predicate the predicate to map from
     * @param built     the map of built elements to their JPA equivalents
     * @return a JPA expression corresponding to that supplied
     */
    private Predicate buildNull(NullPredicate predicate, Map<TupleElement<?>, TupleElement<?>> built) {
        Predicate result;
        Expression<?> expression = buildExpression(predicate.getExpression(), built);
        if (predicate.isNegated()) {
            result = builder.isNotNull(expression);
        } else {
            result = builder.isNull(expression);
        }
        return result;
    }

    /**
     * Builds an 'exists' predicate.
     *
     * @param predicate the predicate to map from
     * @param built     the map of built elements to their JPA equivalents
     * @return a JPA expression corresponding to that supplied
     */
    private Expression<?> buildExists(ExistsPredicate predicate, Map<TupleElement<?>, TupleElement<?>> built) {
        Subquery<?> subquery = (Subquery<?>) built.get(predicate.getSubquery());
        if (subquery == null) {
            throw new IllegalArgumentException("Subquery not found");
        }
        return builder.exists(subquery);
    }

    /**
     * Builds a 'not' predicate.
     *
     * @param predicate the predicate to map from
     * @param built     the map of built elements to their JPA equivalents
     * @return a JPA expression corresponding to that supplied
     */
    private Expression<?> buildNot(NotPredicate predicate, Map<TupleElement<?>, TupleElement<?>> built) {
        Expression<Boolean> expression = cast(buildExpression(predicate.getExpression(), built), Boolean.class);
        return builder.not(expression);
    }

    /**
     * Builds a boolean expression.
     *
     * @param expression the predicate to map from
     * @param built      the map of built elements to their JPA equivalents
     * @return a JPA expression corresponding to that supplied
     */
    private Expression<Boolean> buildBooleanExpression(Expression<Boolean> expression,
                                                       Map<TupleElement<?>, TupleElement<?>> built) {
        return cast(buildExpression(expression, built), Boolean.class);
    }

    /**
     * Builds a predicate expression.
     *
     * @param expression the expression to map
     * @param built      the map of built elements to their JPA equivalents
     * @return the built expression
     */
    private Expression<?> buildPredicate(PredicateImpl expression, Map<TupleElement<?>, TupleElement<?>> built) {
        Expression<?> result;
        List<Expression<Boolean>> expressions = expression.getExpressions();
        switch (expression.getOperator()) {
            case AND:
                if (expressions.size() == 2) {
                    result = builder.and(buildBooleanExpression(expressions.get(0), built),
                                         buildBooleanExpression(expressions.get(1), built));
                } else {
                    result = builder.and(buildPredicates(expressions, built));
                }
                break;
            case OR:
                if (expressions.size() == 2) {
                    result = builder.or(buildBooleanExpression(expressions.get(0), built),
                                        buildBooleanExpression(expressions.get(1), built));
                } else {
                    result = builder.or(buildPredicates(expressions, built));
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported operator: " + expression.getOperator());
        }
        return result;
    }

    /**
     * Builds a list of predicates.
     *
     * @param expressions the expressions to map from
     * @param built       the map of built elements to their JPA equivalents
     * @return JPA predicates corresponding to those supplied
     */
    private Predicate[] buildPredicates(List<? extends Expression<Boolean>> expressions,
                                        Map<TupleElement<?>, TupleElement<?>> built) {
        List<Predicate> predicates = new ArrayList<>();
        for (Expression<Boolean> predicate : expressions) {
            predicates.add((Predicate) buildExpression(predicate, built));
        }
        return predicates.toArray(new Predicate[0]);
    }

    /**
     * Builds an aggregate expression.
     *
     * @param expression the expression to map
     * @param built      the map of built elements to their JPA equivalents
     * @return the built expression
     */
    private Expression<?> buildAggregate(AggregateExpression expression, Map<TupleElement<?>, TupleElement<?>> built) {
        Expression<?> result;
        switch (expression.getFunction()) {
            case COUNT:
                result = builder.count(buildExpression(expression.getExpression(), built));
                break;
            case COUNT_DISTINCT:
                result = builder.countDistinct(buildExpression(expression.getExpression(), built));
                break;
            case SUM:
                result = builder.sum(cast(buildExpression(expression.getExpression(), built), Number.class));
                break;
            case MAX:
                result = builder.max(cast(buildExpression(expression.getExpression(), built), Number.class));
                break;
            case MIN:
                result = builder.min(cast(buildExpression(expression.getExpression(), built), Number.class));
                break;
            case GREATEST:
                result = builder.greatest(cast(buildExpression(expression.getExpression(), built), Comparable.class));
                break;
            case LEAST:
                result = builder.least(cast(buildExpression(expression.getExpression(), built), Comparable.class));
                break;
            default:
                throw new IllegalArgumentException("Unsupported function " + expression.getFunction());
        }
        return result;
    }

    /**
     * Builds a path.
     *
     * @param path  the path to map
     * @param built the map of built elements to their JPA equivalents
     * @return a JPA path corresponding to that supplied
     */
    private <X extends IMObject> Path<?> buildPath(PathImpl<?> path, Map<TupleElement<?>, TupleElement<?>> built) {
        Path<?> result = (Path<?>) built.get(path);
        if (result == null) {
            PathImpl<?> parent = path.getParent();
            From<?, ?> from = (From<?, ?>) built.get(parent);
            if (from == null) {
                throw new IllegalStateException("Failed to find From for path=" + path.getName());
            }
            boolean reference = path.isReference();
            if (reference && path.getNode() == null) {
                // created using Path.reference()
                result = from;
            } else {
                String[] parts = getNodePath(path);
                if (parts.length == 1) {
                    result = from.get(parts[0]);
                } else if (parts.length == 2) {
                    result = getDetailsValuePath(path, from, parts[1]);
                } else {
                    throw new IllegalArgumentException("Unsupported node " + path.getNode().getPath());
                }
            }
            if (path.getAlias() != null) {
                result.alias(path.getAlias());
            }
            built.put(path, result);
        }
        return result;
    }

    /**
     * Returns the path for a node.
     * <p>
     * This splits the {@link NodeDescriptor#getPath()} on '/' which should contain at least one and at most two
     * parts.<br/>
     * Where there is a single part, it refers to the field on the implementation class.<br/>
     * Where there is two parts, the first part refers to a map field (i.e. 'details'), and the second, a key to that
     * map.
     *
     * @param path the path
     * @return the path parts
     * @throws IllegalArgumentException if the path doesn't have a node, or the node is incorrectly defined
     */
    private String[] getNodePath(PathImpl<?> path) {
        NodeDescriptor node = path.getNode();
        if (node == null) {
            throw new IllegalArgumentException("Path doesn't have a node");
        }
        String[] parts = StringUtils.split(node.getPath(), '/');
        if (parts.length == 0 || parts.length > 2) {
            throw new IllegalArgumentException("Unsupported path=" + node.getPath() + " for node "
                                               + path.getName());
        }
        if (parts.length == 2 && !"details".equals(parts[0])) {
            throw new IllegalArgumentException("Unsupported path=" + node.getPath()
                                               + " associated with node: " + node.getName());
        }
        return parts;
    }

    /**
     * Determines if an expression is a reference.
     *
     * @param expression the expression
     * @return {@code true} if the expression is a reference
     */
    private boolean isReference(Object expression) {
        return expression instanceof PathImpl && ((PathImpl) expression).isReference();
    }

    /**
     * Builds an archetype path for a reference path.
     * <p>
     * NOTE: the results of these are not added to {@code built} as they would replace the id paths.
     *
     * @param path  the path to map
     * @param built the map of built elements to their JPA equivalents
     * @return a JPA path corresponding to that supplied
     */
    private Path<?> buildArchetypePath(PathImpl<?> path, Map<TupleElement<?>, TupleElement<?>> built) {
        PathImpl<?> parent = path.getParent();
        Path<?> result;
        From<?, ?> from = (From<?, ?>) built.get(parent);
        if (from == null) {
            throw new IllegalStateException("Failed to find From for path=" + path.getName());
        }
        if (path.getNode() == null) {
            // created using Path.reference()
            result = from.get("archetypeId").get("shortName");
        } else {
            String[] parts = getNodePath(path);
            if (parts.length == 1) {
                result = from.get(parts[0]).get("archetypeId").get("shortName");
            } else {
                throw new IllegalArgumentException("Unsupported node " + path.getNode().getPath());
            }
        }
        return result;
    }


    /**
     * Builds a comparison predicate.
     *
     * @param comparison the comparison to map
     * @param built      the map of built elements to their JPA equivalents
     * @return the built expression
     */
    private Predicate buildComparison(ComparisonPredicate comparison, Map<TupleElement<?>, TupleElement<?>> built) {
        Predicate result;
        if (isReference(comparison.getLHS())) {
            // handle reference comparison. For equality, both the id and archetype must match
            if (comparison.getComparisonOperator() != ComparisonPredicate.Operator.EQ
                && comparison.getComparisonOperator() != ComparisonPredicate.Operator.NE) {
                throw new IllegalArgumentException("Cannot perform " + comparison.getComparisonOperator()
                                                   + " on reference expressions");
            }
            boolean equal = comparison.getComparisonOperator() == ComparisonPredicate.Operator.EQ;
            PathImpl lhs = (PathImpl) comparison.getLHS();
            Path<?> lhsId = buildPath(lhs, built).get("id");
            if (isReference(comparison.getRHS())) {
                PathImpl rhs = (PathImpl) comparison.getRHS();
                Path<?> rhsId = buildPath(rhs, built).get("id");
                if (equal) {
                    Path<?> lhsArchetype = buildArchetypePath(lhs, built);
                    Path<?> rhsArchetype = buildArchetypePath(rhs, built);
                    result = builder.and(builder.equal(lhsId, rhsId), builder.equal(lhsArchetype, rhsArchetype));
                } else {
                    result = builder.notEqual(lhsId, rhsId);
                }
            } else if (comparison.getRHS() instanceof Reference) {
                Reference reference = (Reference) comparison.getRHS();
                if (equal) {
                    Path<?> lhsArchetype = buildArchetypePath(lhs, built);
                    result = builder.and(builder.equal(lhsId, reference.getId()),
                                         builder.equal(lhsArchetype, reference.getArchetype()));
                } else {
                    result = builder.notEqual(lhsId, reference.getId());
                }
            } else {
                throw new IllegalArgumentException("Cannot compare reference with " + comparison.getRHS());
            }
        } else {
            Expression lhs = buildExpression(comparison.getLHS(), built);
            if (comparison.getRHS() instanceof ExpressionImpl) {
                Expression rhs = buildExpression((Expression) comparison.getRHS(), built);
                switch (comparison.getComparisonOperator()) {
                    case EQ:
                        result = builder.equal(lhs, rhs);
                        break;
                    case NE:
                        result = builder.notEqual(lhs, rhs);
                        break;
                    case GT:
                        result = builder.greaterThan(lhs, rhs);
                        break;
                    case GTE:
                        result = builder.greaterThanOrEqualTo(lhs, rhs);
                        break;
                    case LT:
                        result = builder.lessThan(lhs, rhs);
                        break;
                    case LTE:
                        result = builder.lessThanOrEqualTo(lhs, rhs);
                        break;
                    case LIKE:
                        result = builder.like(cast(lhs, String.class), cast(rhs, String.class));
                        break;
                    default:
                        throw new IllegalStateException("Unsupported operator: " + comparison.getComparisonOperator());
                }
            } else if (comparison.getLHS() instanceof ExpressionImpl) {
                Comparable<Object> rhs = (Comparable<Object>) comparison.getRHS();
                switch (comparison.getComparisonOperator()) {
                    case EQ:
                        result = builder.equal(lhs, rhs);
                        break;
                    case NE:
                        result = builder.notEqual(lhs, rhs);
                        break;
                    case GT:
                        result = builder.greaterThan(cast(lhs, Comparable.class), rhs);
                        break;
                    case GTE:
                        result = builder.greaterThanOrEqualTo(cast(lhs, Comparable.class), rhs);
                        break;
                    case LT:
                        result = builder.lessThan(cast(lhs, Comparable.class), rhs);
                        break;
                    case LTE:
                        result = builder.lessThanOrEqualTo(cast(lhs, Comparable.class), rhs);
                        break;
                    case LIKE:
                        result = builder.like(cast(lhs, String.class), rhs.toString());
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported operator: "
                                                           + comparison.getComparisonOperator());
                }
            } else {
                throw new IllegalArgumentException("Unsupported operation");
            }
        }
        return result;
    }

    /**
     * Build a between expression.
     *
     * @param between the expression to map
     * @param built   the map of built elements to their JPA equivalents
     * @return the built expression
     */
    private Predicate buildBetween(BetweenPredicate between, Map<TupleElement<?>, TupleElement<?>> built) {
        Predicate result;
        Expression value = buildExpression(between.getValue(), built);
        Object lowerBound = between.getLowerBound();
        Object upperBound = between.getUpperBound();
        if (lowerBound instanceof Expression) {
            Expression lower = buildExpression((Expression) lowerBound, built);
            Expression upper = buildExpression((Expression) upperBound, built);
            result = builder.between(value, lower, upper);
        } else {
            result = builder.between(value, (Comparable) lowerBound, (Comparable) upperBound);
        }
        return result;
    }

    /**
     * Casts an expression to the required type.
     *
     * @param expression the expression to cast
     * @param type       the type
     * @return the cast expression
     */
    @SuppressWarnings("unchecked")
    private <T> Expression<T> cast(Expression<?> expression, Class<T> type) {
        Class<?> exprType = expression.getJavaType();
        if (!type.isAssignableFrom(exprType)) {
            Class wrapper = ClassUtils.primitiveToWrapper(exprType);
            if (wrapper == null || !type.isAssignableFrom(wrapper)) {
                throw new IllegalArgumentException("Expression type is not a " + type.getName() + ": "
                                                   + exprType.getName());
            }
        }
        return (Expression<T>) expression;
    }

}
