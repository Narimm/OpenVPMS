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

package org.openvpms.archetype.rules.workflow;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.functors.AllPredicate;
import org.apache.commons.lang.time.DateUtils;
import org.joda.time.Period;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Queries free appointment slots.
 *
 * @author Tim Anderson
 */
public class FreeSlotQuery {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The date to query from.
     */
    private Date fromDate;

    /**
     * The date to query to.
     */
    private Date toDate;

    /**
     * If set, only return slots to those occurring on or after the specified time.
     */
    private Period fromTime;

    /**
     * If set, only return slots to those occurring prior to the specified time.
     */
    private Period toTime;

    /**
     * The schedules to query.
     */
    private Entity[] schedules = {};

    /**
     * The minimum slot size, or {@code -1} if there is no minimum slot size.
     */
    private long minSlotSize = -1;

    /**
     * Constructs a {@link FreeSlotQuery}.
     *
     * @param service the archetype service
     */
    public FreeSlotQuery(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Sets the date to query slots from.
     *
     * @param from the from date
     */
    public void setFromDate(Date from) {
        this.fromDate = from;
    }

    /**
     * Sets the date to query slots to.
     *
     * @param to the to date
     */
    public void setToDate(Date to) {
        this.toDate = to;
    }

    /**
     * Sets the time to include slots from.
     *
     * @param time the time. May be {@code null} to include all times up to {@link #setToTime(Period)}.
     */
    public void setFromTime(Period time) {
        this.fromTime = time;
    }

    /**
     * Sets the time to include slots to.
     *
     * @param time the time. May be {@code null} to include all times from {@link #setFromTime(Period)}.
     */
    public void setToTime(Period time) {
        this.toTime = time;
    }

    /**
     * Sets the schedules to query.
     *
     * @param schedules the schedules
     */
    public void setSchedules(Entity... schedules) {
        this.schedules = schedules;
    }

    /**
     * Sets the minimum slot size.
     *
     * @param size  the size, or {@code 0} to include all slots
     * @param units the size units
     */
    public void setMinSlotSize(int size, DateUnits units) {
        switch (units) {
            case WEEKS:
                minSlotSize = size * DateUtils.MILLIS_PER_DAY * 7;
                break;
            case DAYS:
                minSlotSize = size * DateUtils.MILLIS_PER_DAY;
                break;
            case HOURS:
                minSlotSize = size * DateUtils.MILLIS_PER_HOUR;
                break;
            case MINUTES:
                minSlotSize = size * DateUtils.MILLIS_PER_MINUTE;
                break;
            default:
                minSlotSize = 0;
        }
    }

    /**
     * Queries available slots.
     *
     * @return an iterator over the available slots
     */
    public Iterator<Slot> query() {
        if (fromDate != null && toDate != null && schedules.length > 0) {
            Predicate<Slot> predicate = getPredicate();
            List<FreeSlotIterator> list = new ArrayList<FreeSlotIterator>();
            for (Entity schedule : schedules) {
                list.add(new FreeSlotIterator(schedule, fromDate, toDate, fromTime, toTime, service));
            }
            return new FreeSlotIterators(list, predicate);
        }
        return Collections.<Slot>emptyList().iterator();
    }

    private Predicate<Slot> getPredicate() {
        List<Predicate<Slot>> predicates = new ArrayList<Predicate<Slot>>();
        if (minSlotSize > 0) {
            predicates.add(new SlotSizePredicate());
        }
        return AllPredicate.allPredicate(predicates);
    }

    private class SlotSizePredicate implements Predicate<Slot> {

        @Override
        public boolean evaluate(Slot slot) {
            long duration = slot.getEndTime().getTime() - slot.getStartTime().getTime();
            return duration >= minSlotSize;
        }
    }


}
