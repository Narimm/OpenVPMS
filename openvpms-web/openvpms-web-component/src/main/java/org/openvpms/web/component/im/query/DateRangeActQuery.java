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

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.property.ModifiableListener;

import java.util.Date;


/**
 * An act query that enables acts to be queried for a particular date range.
 *
 * @author Tim Anderson
 */
public abstract class DateRangeActQuery<T extends Act> extends ActQuery<T> {

    /**
     * Determines if acts should be filtered on type.
     */
    private final boolean selectType;

    /**
     * The date range listener.
     */
    private final ModifiableListener dateRangeListener;

    /**
     * The date range.
     */
    private DateRange dateRange;


    /**
     * Constructs a {@code DateRangeActQuery}.
     *
     * @param shortNames the act short names to query
     * @param statuses   the act status lookups. May be {@code null}
     * @param type       the type that this query returns
     */
    public DateRangeActQuery(String[] shortNames, ActStatuses statuses, Class type) {
        this(null, null, null, shortNames, statuses, type);
    }

    /**
     * Constructs a {@code DateRangeActQuery}.
     *
     * @param entity        the entity to search for
     * @param participant   the participant node name
     * @param participation the entity participation short name
     * @param shortNames    the act short names
     * @param statuses      the act status lookups. May be {@code null}
     * @param type          the type that this query returns
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public DateRangeActQuery(Entity entity, String participant,
                             String participation, String[] shortNames,
                             ActStatuses statuses, Class type) {
        super(entity, participant, participation, shortNames, statuses, type);
        selectType = true;
        dateRangeListener = modifiable -> onQuery();
        QueryFactory.initialise(this);
    }

    /**
     * Constructs a {@code DateRangeActQuery}.
     *
     * @param entity        the entity to search for
     * @param participant   the participant node name
     * @param participation the entity participation short name
     * @param shortNames    the act short names
     * @param type          the type that this query returns
     */
    public DateRangeActQuery(Entity entity, String participant, String participation, String[] shortNames, Class type) {
        this(entity, participant, participation, shortNames, true, new String[0], type);
    }

    /**
     * Constructs a {@code DateRangeActQuery}.
     *
     * @param entity        the entity to search for
     * @param participant   the participant node name
     * @param participation the entity participation short name
     * @param shortNames    the act short names
     * @param statuses      the act statuses to search on. May be
     *                      {@code empty}
     * @param type          the type that this query returns
     */
    public DateRangeActQuery(Entity entity, String participant, String participation, String[] shortNames,
                             String[] statuses, Class type) {
        this(entity, participant, participation, shortNames, true, statuses, type);
    }

    /**
     * Constructs a {@code DateRangeActQuery}.
     *
     * @param entity        the entity to search for
     * @param participant   the participant node name
     * @param participation the entity participation short name
     * @param shortNames    the act short names
     * @param primaryOnly   if {@code true} only primary archetypes will be queried
     * @param statuses      the act statuses to search on. May be empty
     * @param type          the type that this query returns
     */
    public DateRangeActQuery(Entity entity, String participant, String participation, String[] shortNames,
                             boolean primaryOnly, String[] statuses, Class type) {
        super(entity, participant, participation, shortNames, primaryOnly, statuses, type);
        selectType = true;
        dateRangeListener = modifiable -> onQuery();
        QueryFactory.initialise(this);
    }

    /**
     * Determines if all dates are being selected.
     *
     * @return {@code true} if all dates are being selected
     */
    public boolean getAllDates() {
        return getDateRange().getAllDates();
    }

    /**
     * Sets the state of the <em>allDates</em> checkbox, if present.
     *
     * @param selected the state of the <em>allDates</em> checkbox
     */
    public void setAllDates(boolean selected) {
        DateRange dateRange = getDateRange();
        dateRange.removeListener(dateRangeListener);
        dateRange.setAllDates(selected);
        dateRange.addListener(dateRangeListener);
    }

    /**
     * Returns the 'from' date.
     *
     * @return the 'from' date, or {@code null} to query all dates
     */
    @Override
    public Date getFrom() {
        return getDateRange().getFrom();
    }

    /**
     * Sets the 'from' date.
     *
     * @param date the 'from' date
     */
    public void setFrom(Date date) {
        DateRange dateRange = getDateRange();
        dateRange.removeListener(dateRangeListener);
        dateRange.setFrom(date);
        dateRange.addListener(dateRangeListener);
    }

    /**
     * Returns the 'to' date.
     *
     * @return the 'to' date, or {@code null} to query all dates
     */
    @Override
    public Date getTo() {
        return getDateRange().getTo();
    }

    /**
     * Sets the 'to' date.
     *
     * @param date the 'to' date
     */
    public void setTo(Date date) {
        DateRange dateRange = getDateRange();
        dateRange.removeListener(dateRangeListener);
        dateRange.setTo(date);
        dateRange.addListener(dateRangeListener);
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
     * Creates a new result set.
     *
     * @param sort the sort constraint. May be {@code null}
     * @return a new result set
     */
    @Override
    protected ResultSet<T> createResultSet(SortConstraint[] sort) {
        return new ActResultSet<>(getArchetypeConstraint(), getParticipantConstraint(), getFrom(), getTo(),
                                  getStatuses(), excludeStatuses(), getConstraints(), getMaxResults(), sort);
    }

    /**
     * Adds a date range to the container.
     *
     * @param container the container
     */
    protected void addDateRange(Component container) {
        DateRange range = getDateRange();
        container.add(range.getComponent());
        getFocusGroup().add(range.getFocusGroup());
    }

    /**
     * Returns the date range.
     *
     * @return the date range
     */
    protected DateRange getDateRange() {
        if (dateRange == null) {
            dateRange = createDateRange();
            dateRange.addListener(dateRangeListener);
        }
        return dateRange;
    }

    /**
     * Creates the date range.
     *
     * @return a new date range
     */
    protected DateRange createDateRange() {
        return new DateRange();
    }

}
