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

import java.util.Date;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface PeriodRelationshipDO extends IMObjectRelationshipDO {
    /**
     * Returns the active start time.
     *
     * @return the active start time. If <tt>null</tt>, indicates that the start
     *         time is unbounded
     */
    Date getActiveStartTime();

    /**
     * Sets the active start time.
     *
     * @param time the active start time. May be <tt>null</tt>
     */
    void setActiveStartTime(Date time);

    /**
     * Returns the active end time.
     *
     * @return the active end time. If <tt>null</tt>, indicates that the end
     *         time is unbounded
     */
    Date getActiveEndTime();

    /**
     * Sets the active end time.
     *
     * @param time the active end time. May be <tt>null</tt>
     */
    void setActiveEndTime(Date time);

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
    boolean isActive();

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
    boolean isActive(Date time);

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
    boolean isActive(long time);

    /**
     * Determines if the relationship is active.
     *
     * @param active if <tt>true</tt>, sets the end time to <tt>null</tt>,
     *               otherwise sets to it the current time
     */
    void setActive(boolean active);
}
