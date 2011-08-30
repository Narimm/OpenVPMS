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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.SortConstraint;

import java.util.Date;


/**
 * An act query that enables acts to be queried for a particular date range.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class DateRangeActQuery<T extends Act> extends ActQuery<T> {

    /**
     * Determines if acts should be filtered on type.
     */
    private final boolean selectType;

    /**
     * The date range.
     */
    private DateRange dateRange;


    /**
     * Constructs a new <tt>DateRangeActQuery</tt>.
     *
     * @param shortNames the act short names to query
     * @param statuses   the act status lookups. May be <tt>null</tt>
     * @param type       the type that this query returns
     */
    public DateRangeActQuery(String[] shortNames, ActStatuses statuses,
                             Class type) {
        this(null, null, null, shortNames, statuses, type);
    }

    /**
     * Constructs a new <tt>DateRangeActQuery</tt>.
     *
     * @param entity        the entity to search for
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param shortNames    the act short names
     * @param statuses      the act status lookups. May be <tt>null</tt>
     * @param type          the type that this query returns
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public DateRangeActQuery(Entity entity, String participant,
                             String participation, String[] shortNames,
                             ActStatuses statuses, Class type) {
        super(entity, participant, participation, shortNames, statuses, type);
        selectType = true;
        QueryFactory.initialise(this);
    }

    /**
     * Constructs a new <tt>DateRangeActQuery</tt>.
     *
     * @param entity        the entity to search for
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param shortNames    the act short names
     * @param type          the type that this query returns
     */
    public DateRangeActQuery(Entity entity, String participant,
                             String participation, String[] shortNames,
                             Class type) {
        this(entity, participant, participation, shortNames, true,
             new String[0], type);
    }

    /**
     * Constructs a new <tt>DateRangeActQuery</tt>.
     *
     * @param entity        the entity to search for
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param shortNames    the act short names
     * @param statuses      the act statuses to search on. May be
     *                      <tt>empty</tt>
     * @param type          the type that this query returns
     */
    public DateRangeActQuery(Entity entity, String participant,
                             String participation, String[] shortNames,
                             String[] statuses, Class type) {
        this(entity, participant, participation, shortNames, true, statuses,
             type);
    }

    /**
     * Constructs a new <tt>DateRangeActQuery</tt>.
     *
     * @param entity        the entity to search for
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param shortNames    the act short names
     * @param primaryOnly   if <tt>true</tt> only primary archetypes will be
     *                      queried
     * @param statuses      the act statuses to search on. May be
     *                      <tt>empty</tt>
     * @param type          the type that this query returns
     */
    public DateRangeActQuery(Entity entity, String participant,
                             String participation, String[] shortNames,
                             boolean primaryOnly, String[] statuses,
                             Class type) {
        super(entity, participant, participation, shortNames, primaryOnly,
              statuses, type);
        selectType = true;
        QueryFactory.initialise(this);
    }

    /**
     * Lays out the component in a container.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        if (selectType) {
            addShortNameSelector(container);
        }

        addStatusSelector(container);
        addDateRange(container);
    }

    /**
     * Returns the 'from' date.
     *
     * @return the 'from' date, or <tt>null</tt> to query all dates
     */
    @Override
    protected Date getFrom() {
        return getDateRange().getFrom();
    }

    /**
     * Sets the 'from' date.
     *
     * @param date the 'from' date
     */
    protected void setFrom(Date date) {
        getDateRange().setFrom(date);
    }

    /**
     * Returns the 'to' date.
     *
     * @return the 'to' date, or <tt>null</tt> to query all dates
     */
    @Override
    protected Date getTo() {
        return getDateRange().getTo();
    }

    /**
     * Sets the 'to' date.
     *
     * @param date the 'to' date
     */
    protected void setTo(Date date) {
        getDateRange().setTo(date);
    }

    /**
     * Creates a new result set.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return a new result set
     */
    @Override
    protected ResultSet<T> createResultSet(SortConstraint[] sort) {
        return new ActResultSet<T>(getArchetypeConstraint(),
                                   getParticipantConstraint(),
                                   getFrom(), getTo(), getStatuses(),
                                   excludeStatuses(), getConstraints(),
                                   getMaxResults(), sort);
    }

    /**
     * Adds a date range to the container.
     *
     * @param container the container
     */
    protected void addDateRange(Component container) {
        container.add(getDateRange().getComponent());
    }

    /**
     * Returns the date range.
     *
     * @return the date range
     */
    protected DateRange getDateRange() {
        if (dateRange == null) {
            dateRange = createDateRange();
        }
        return dateRange;
    }

    /**
     * Creates the date range.
     *
     * @return a new date range
     */
    protected DateRange createDateRange() {
        return new DateRange(getFocusGroup());
    }

}
