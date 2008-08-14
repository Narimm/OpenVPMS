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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.common;

import org.openvpms.component.business.domain.archetype.ArchetypeId;

import java.util.Date;


/**
 * Implementation of the {@link PeriodRelationshipDO} interface.
 * *
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class PeriodRelationshipDOImpl
        extends IMObjectRelationshipDOImpl
        implements PeriodRelationshipDO {

    /**
     * The active end time for the relationship. May be <tt>null</tt>.
     */
    private Date activeEndTime;

    /**
     * The active start time for the relationship. May be <tt>null</tt>.
     */
    private Date activeStartTime;


    /**
     * Default constructor.
     */
    public PeriodRelationshipDOImpl() {
        // do nothing
    }

    /**
     * Creates a new <tt>PeriodRelationshipDOImpl</tt>.
     *
     * @param archetypeId the archetype identifier
     */
    public PeriodRelationshipDOImpl(ArchetypeId archetypeId) {
        super(archetypeId);
    }

    /**
     * Returns the active start time.
     *
     * @return the active start time. If <tt>null</tt>, indicates that the start
     *         time is unbounded
     */
    public Date getActiveStartTime() {
        return activeStartTime;
    }

    /**
     * Sets the active start time.
     *
     * @param time the active start time. May be <tt>null</tt>
     */
    public void setActiveStartTime(Date time) {
        activeStartTime = time;
    }

    /**
     * Returns the active end time.
     *
     * @return the active end time. If <tt>null</tt>, indicates that the end
     *         time is unbounded
     */
    public Date getActiveEndTime() {
        return activeEndTime;
    }

    /**
     * Sets the active end time.
     *
     * @param time the active end time. May be <tt>null</tt>
     */
    public void setActiveEndTime(Date time) {
        activeEndTime = time;
    }

    /**
     * Determines if the relationship is active as of the current time.
     * <p/>
     * The relationship is active if:
     * <ul>
     * <li>its start time is <tt>null</tt> or &lt;= the current time; and</li>
     * <li>its end time is <tt>null</tt> or &gt;= the curent time</li>
     * </ul>
     *
     * @return <tt>true</tt> if the relationship is active,
     *         otherwise <tt>false</tt>
     */
    @Override
    public boolean isActive() {
        return isActive(System.currentTimeMillis());
    }

    /**
     * Determines if the relationship is active as of the specified time.
     * <p/>
     * The relationship is active if:
     * <ul>
     * <li>its start time is <tt>null</tt> or &lt;= <tt>time</tt>; and</li>
     * <li>its end time is <tt>null</tt> or &gt;= <tt>time</tt></li>
     * </ul>
     *
     * @param time the time
     * @return <tt>true</tt> if the relationship is active, otherwise
     *         <tt>false</tt>
     */
    public boolean isActive(Date time) {
        return isActive(time.getTime());
    }

    /**
     * Determines if the relationship is active as of the specified time.
     * <p/>
     * The relationship is active if:
     * <ul>
     * <li>its start time is <tt>null</tt> or &lt;= <tt>time</tt>; and</li>
     * <li>its end time is <tt>null</tt> or &gt;= <tt>time</tt></li>
     * </ul>
     *
     * @param time the time
     * @return <tt>true</tt> if the relationship is active, otherwise
     *         <tt>false</tt>
     */
    public boolean isActive(long time) {
        return compare(activeStartTime, time) <= 0
                && compare(activeEndTime, time) >= 0;
    }

    /**
     * Determines if the relationship is active.
     *
     * @param active if <tt>true</tt>, sets the end time to <tt>null</tt>,
     *               otherwise sets to it the current time
     */
    @Override
    public void setActive(boolean active) {
        if (active) {
            activeEndTime = null;
        } else {
            activeEndTime = new Date();
        }
    }

    /**
     * Helper to compare a date with a timestamp.
     *
     * @param date the date. May be <tt>null</tt>, indicating an unbounded date
     * @param time the timestamp
     * @return -1 if date &lt; time, 0, if date == time or is <tt>null<tt>,
     *         otherwise 1
     */
    private int compare(Date date, long time) {
        if (date == null) {
            return 0;
        }
        long other = date.getTime();
        return (other < time ? -1 : (other == time ? 0 : 1));
    }

}
