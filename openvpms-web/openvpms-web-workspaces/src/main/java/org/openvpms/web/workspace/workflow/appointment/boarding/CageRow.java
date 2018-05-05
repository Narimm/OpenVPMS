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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment.boarding;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;

/**
 * Denotes a cage/schedule at a particular row in a {@link CageScheduleGrid}.
 *
 * @author Tim Anderson
 */
class CageRow {

    /**
     * The cage/schedule group, or {@code null} if the row represents a totals row.
     */
    private final CageScheduleGroup group;

    /**
     * The schedule, or {@code null} if the row represents a cage type only.
     */
    private final Schedule schedule;

    /**
     * Constructs a {@link CageRow}.
     * <p/>
     * This indicates that the row is a totals row.
     */
    public CageRow() {
        this(null, null);
    }

    /**
     * Constructs a {@link CageRow}.
     *
     * @param group    the cage/schedule group that the schedule belongs to
     * @param schedule the schedule, or {@code null} if the row represents a cage type only
     */
    public CageRow(CageScheduleGroup group, Schedule schedule) {
        this.group = group;
        this.schedule = schedule;
    }

    /**
     * Determines if this is a total.
     *
     * @return {@code true} if this is a total
     */
    public boolean isTotal() {
        return group == null;
    }

    /**
     * Determines if this is a summary.
     *
     * @return {@code true} if the schedule is unset
     */
    public boolean isSummary() {
        return schedule == null;
    }

    /**
     * Returns the row name.
     *
     * @return the name. This is the schedule name if one is present, the group name if one isn't, or a total if neither
     * is present
     */
    public String getName() {
        return schedule != null ? schedule.getName()
                                : group != null ? group.getName()
                                                : Messages.get("workflow.scheduling.appointment.total");
    }

    /**
     * Returns the schedule.
     *
     * @return the schedule, or {@code null} if this is a summary.
     */
    public Schedule getSchedule() {
        return schedule;
    }

    /**
     * Determines if the row is for a particular schedule.
     *
     * @param schedule the schedule
     * @return {@code true} if the row is for the schedule
     */
    public boolean isSchedule(Entity schedule) {
        return (this.schedule != null && ObjectUtils.equals(this.schedule.getSchedule(), schedule));
    }

    /**
     * Determines if the row is for a particular schedule.
     *
     * @param scheduleRef the schedule reference
     * @return {@code true} if the row is for the schedule
     */
    public boolean isSchedule(IMObjectReference scheduleRef) {
        return (schedule != null && schedule.getSchedule().getObjectReference().equals(scheduleRef));
    }

    /**
     * Returns the group.
     *
     * @return the group
     */
    public CageScheduleGroup getGroup() {
        return group;
    }

    /**
     * Determines if the even or odd rendering style should be used.
     *
     * @return {@code true} to use the even rendering style, {@code false} to use the odd style
     */
    public boolean renderEven() {
        return schedule == null || schedule.getRenderEven();
    }
}
