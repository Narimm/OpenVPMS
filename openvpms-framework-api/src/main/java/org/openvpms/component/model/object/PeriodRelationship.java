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

package org.openvpms.component.model.object;

import java.util.Date;

/**
 * Describes a relationship between two {@link IMObject}s that is active for a period of time.
 *
 * @author Tim Anderson
 */
public interface PeriodRelationship extends Relationship {

    /**
     * Returns the active start time.
     *
     * @return the active start time. If {@code null}, indicates that the start time is unbounded
     */
    Date getActiveStartTime();

    /**
     * Sets the active start time.
     *
     * @param time the active start time. May be {@code null}
     */
    void setActiveStartTime(Date time);

    /**
     * Returns the active end time.
     *
     * @return the active end time. If {@code null}, indicates that the end time is unbounded
     */
    Date getActiveEndTime();

    /**
     * Sets the active end time.
     *
     * @param time the active end time. May be {@code null}
     */
    void setActiveEndTime(Date time);

    /**
     * Determines if the relationship is active as of the current time.
     * <p>
     * The relationship is active if:
     * <ul>
     * <li>its start time is {@code null} or &lt;= the current time; and</li>
     * <li>its end time is {@code null} or &gt;= the current time</li>
     * </ul>
     *
     * @return {@code true} if the relationship is active, otherwise {@code false}
     */
    @Override
    boolean isActive();

    /**
     * Determines if the relationship is active as of the specified time.
     * <p>
     * The relationship is active if:
     * <ul>
     * <li>its start time is {@code null} or &lt;= {@code time}; and</li>
     * <li>its end time is {@code null} or &gt; {@code time}</li>
     * </ul>
     *
     * @param time the time
     * @return {@code true} if the relationship is active, otherwise {@code false}
     */
    boolean isActive(Date time);

    /**
     * Determines if the relationship is active as of the specified time.
     * <p>
     * The relationship is active if:
     * <ul>
     * <li>its start time is {@code null} or &lt;= {@code time}; and</li>
     * <li>its end time is {@code null} or &gt;= {@code time}</li>
     * </ul>
     *
     * @param time the time
     * @return {@code true} if the relationship is active, otherwise {@code false}
     */
    boolean isActive(long time);

    /**
     * Determines if the relationship is active for the specified period.
     *
     * @param from the period start date. May be {@code null}
     * @param to   the period end date. May be {@code null}
     */
    boolean isActive(Date from, Date to);

    /**
     * Determines if the relationship is active.
     *
     * @param active if {@code true}, sets the end time to {@code null}, otherwise sets to it the current time
     */
    @Override
    void setActive(boolean active);

}
