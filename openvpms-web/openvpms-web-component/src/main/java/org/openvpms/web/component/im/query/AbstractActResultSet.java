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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.AndConstraint;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.IConstraintContainer;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.ParticipationConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;

import java.util.Date;

import static org.openvpms.component.system.common.query.ParticipationConstraint.Field.ActShortName;


/**
 * Result set for performing queries on {@link Act}s.
 *
 * @author Tim Anderson
 */
public abstract class AbstractActResultSet<T> extends AbstractArchetypeServiceResultSet<T> {

    /**
     * The act archetype criteria.
     */
    private final ShortNameConstraint archetypes;

    /**
     * The participant constraints.
     */
    private ParticipantConstraint[] participants;

    /**
     * The status criteria. May be {@code null}.
     */
    private final IConstraint statuses;

    /**
     * The time criteria. May be {@code null}.
     */
    private final IConstraint times;


    /**
     * Constructs a new {@code AbstractActResultSet}.
     *
     * @param archetypes the act archetype constraint
     * @param pageSize   the maximum no. of results per page
     * @param sort       the sort criteria. May be {@code null}
     * @param executor   the query executor
     */
    public AbstractActResultSet(ShortNameConstraint archetypes, int pageSize, SortConstraint[] sort,
                                QueryExecutor<T> executor) {
        this(archetypes, null, null, null, null, pageSize, sort, executor);
    }

    /**
     * Constructs a new {@code AbstractActResultSet}.
     *
     * @param archetypes  the act archetype constraint
     * @param participant the participant constraint. May be {@code null}
     * @param from        the act from date. May be {@code null}
     * @param to          the act to date, inclusive. May be {@code null}
     * @param statuses    the act statuses. If empty, indicates all acts
     * @param pageSize    the maximum no. of results per page
     * @param sort        the sort criteria. May be {@code null}
     * @param executor    the query executor
     */
    public AbstractActResultSet(ShortNameConstraint archetypes,
                                ParticipantConstraint participant,
                                Date from,
                                Date to,
                                String[] statuses, int pageSize,
                                SortConstraint[] sort,
                                QueryExecutor<T> executor) {
        this(archetypes, participant, from, to, statuses, false, null, pageSize,
             sort, executor);
    }

    /**
     * Constructs a new {@code AbstractActResultSet}.
     *
     * @param archetypes  the act archetype constraint
     * @param participant the participant constraint. May be {@code null}
     * @param from        the act from date. May be {@code null}
     * @param to          the act to date, inclusive. May be {@code null}
     * @param statuses    the act statuses. If {@code null} or empty, indicates all acts
     * @param exclude     if {@code true} exclude acts with status in {@code statuses}; otherwise include them.
     * @param constraints additional query constraints. May be {@code null}
     * @param pageSize    the maximum no. of results per page
     * @param sort        the sort criteria. May be {@code null}
     * @param executor    the query executor
     */
    public AbstractActResultSet(ShortNameConstraint archetypes,
                                ParticipantConstraint participant,
                                Date from,
                                Date to,
                                String[] statuses, boolean exclude,
                                IConstraint constraints, int pageSize,
                                SortConstraint[] sort,
                                QueryExecutor<T> executor) {
        this(archetypes, (participant != null) ? new ParticipantConstraint[]{participant} : null,
             from, to, statuses, exclude, constraints, pageSize, sort, executor);
    }

    /**
     * Constructs a new {@code AbstractActResultSet}.
     *
     * @param archetypes   the act archetype constraint
     * @param participants the participant constraints. May be {@code null}
     * @param from         the act from date. May be {@code null}
     * @param to           the act to date, inclusive. May be {@code null}
     * @param statuses     the act statuses. If {@code null} or empty, indicates all acts
     * @param exclude      if {@code true} exclude acts with status in {@code statuses}; otherwise include them.
     * @param constraints  additional query constraints. May be {@code null}
     * @param pageSize     the maximum no. of results per page
     * @param sort         the sort criteria. May be {@code null}
     * @param executor     the query executor
     */
    public AbstractActResultSet(ShortNameConstraint archetypes,
                                ParticipantConstraint[] participants,
                                Date from,
                                Date to,
                                String[] statuses, boolean exclude,
                                IConstraint constraints, int pageSize,
                                SortConstraint[] sort,
                                QueryExecutor<T> executor) {
        this(archetypes, participants, createDateConstraint(from, to),
             statuses, exclude, constraints, pageSize, sort, executor);
    }

    /**
     * Constructs a new {@code AbstractActResultSet}.
     *
     * @param archetypes   the act archetype constraint
     * @param participants the participant constraints. May be {@code null}
     * @param times        the time constraints. May be {@code null}
     * @param statuses     the act statuses. If {@code null} or empty, indicates all acts
     * @param exclude      if {@code true} exclude acts with status in {@code statuses}; otherwise include them.
     * @param constraints  additional query constraints. May be {@code null}
     * @param pageSize     the maximum no. of results per page
     * @param sort         the sort criteria. May be {@code null}
     * @param executor     the query executor
     */
    public AbstractActResultSet(ShortNameConstraint archetypes,
                                ParticipantConstraint[] participants,
                                IConstraint times, String[] statuses,
                                boolean exclude, IConstraint constraints,
                                int pageSize, SortConstraint[] sort,
                                QueryExecutor<T> executor) {
        super(constraints, pageSize, sort, executor);
        this.participants = participants;
        this.archetypes = archetypes;

        this.statuses = createStatusConstraint(statuses, exclude);
        this.times = times;
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
        ArchetypeQuery query = new ArchetypeQuery(archetypes);
        if (statuses != null) {
            query.add(statuses);
        }
        if (times != null) {
            query.add(times);
        }

        if (participants != null) {
            String[] shortNames = DescriptorHelper.getShortNames(
                    archetypes.getShortNames(), archetypes.isPrimaryOnly());
            try {
                for (ParticipantConstraint participant : participants) {
                    ParticipantConstraint p = (ParticipantConstraint) participant.clone();
                    if (shortNames.length > 1) {
                        OrConstraint or = new OrConstraint();
                        for (String shortName : shortNames) {
                            or.add(new ParticipationConstraint(ActShortName, shortName));
                        }
                        p.add(or);
                    } else if (shortNames.length == 1) {
                        p.add(new ParticipationConstraint(ActShortName, shortNames[0]));
                    }
                    query.add(p);
                }
            } catch (CloneNotSupportedException exception) {
                throw new ArchetypeQueryException(ArchetypeQueryException.ErrorCode.CloneNotSupported,
                                                  exception);
            }
        }

        return query;
    }

    /**
     * Returns a new archetype query.
     * This implementation delegates creation to {@link #createQuery}, before adding any {@link #getConstraints()} and
     * {@link #getSortConstraints()}.
     *
     * @param firstResult the first result of the page to retrieve
     * @param maxResults  the maximun no of results in the page
     * @return a new query
     */
    protected ArchetypeQuery createQuery(int firstResult, int maxResults) {
        ArchetypeQuery query = createQuery();
        query.setFirstResult(firstResult);
        query.setMaxResults(maxResults);
        query.setDistinct(isDistinct());
        query.setCountResults(false);
        IConstraint constraints = getConstraints();
        if (constraints != null) {
            query.add(constraints);
        }
        if (getReferenceConstraint() != null) {
            addReferenceConstraint(query, getReferenceConstraint());
        }
        for (SortConstraint sort : getSortConstraints()) {
            if (sort instanceof NodeSortConstraint) {
                NodeSortConstraint node = (NodeSortConstraint) sort;
                NodeDescriptor descriptor
                        = QueryHelper.getDescriptor(archetypes,
                                                    node.getNodeName());
                if (descriptor != null
                    && QueryHelper.isParticipationNode(descriptor)) {
                    ShortNameConstraint shortNames = (ShortNameConstraint)
                            query.getArchetypeConstraint();
                    QueryHelper.addSortOnParticipation(shortNames, query,
                                                       descriptor,
                                                       node.isAscending());
                } else {
                    query.add(sort);
                }
            } else {
                query.add(sort);
            }
        }
        return query;
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
     * Creates a status constraint.
     *
     * @param statuses the statuses. May be {@code null}
     * @param exclude  if {@code true}, exclude acts with the status, otherwise include them
     * @return the status constraint, or {@code null} if there are no statuses
     */
    protected IConstraint createStatusConstraint(String[] statuses, boolean exclude) {
        IConstraint result = null;
        if (statuses != null && statuses.length > 1) {
            IConstraintContainer constraint;
            RelationalOp op;
            if (exclude) {
                constraint = new AndConstraint();
                op = RelationalOp.NE;
            } else {
                constraint = new OrConstraint();
                op = RelationalOp.EQ;
            }
            for (String status : statuses) {
                constraint.add(new NodeConstraint("status", op, status));
            }
            result = constraint;
        } else if (statuses != null && statuses.length == 1) {
            RelationalOp op = RelationalOp.EQ;
            if (exclude) {
                op = RelationalOp.NE;
            }
            result = new NodeConstraint("status", op, statuses[0]);
        }
        return result;
    }

    /**
     * Helper to create a constraint on startTime.
     * <p/>
     * If:
     * <ul>
     * <li>{@code from} and {@code to} are {@code null} no constraint is created</li>
     * <li>{@code from} is non-null and {@code to} is {@code null}, a constraint {@code startTime >= from}
     * is returned
     * <li>{@code from} is null and {@code to} is {@code null}, a constraint {@code startTime <= to}
     * is returned
     * <li>{@code from} is non-null and {@code to} is {@code non-null}, a constraint
     * {@code startTime >= from && startTime <= to} is returned
     * </ul>
     *
     * @param from the act from date. May be {@code null}
     * @param to   the act to date, inclusive. May be {@code null}
     * @return a new constraint, or {@code null} if both dates are null
     */
    private static IConstraint createDateConstraint(Date from, Date to) {
        return QueryHelper.createDateConstraint("startTime", from, to);
    }
}
