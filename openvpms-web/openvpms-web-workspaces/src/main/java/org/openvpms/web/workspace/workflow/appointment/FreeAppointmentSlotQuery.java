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

package org.openvpms.web.workspace.workflow.appointment;

import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.FreeSlotQuery;
import org.openvpms.archetype.rules.workflow.Slot;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.bound.BoundTimeField;
import org.openvpms.web.component.im.query.DateRange;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.scheduling.BaseScheduleQuery;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Query to find free appointment slots.
 *
 * @author Tim Anderson
 */
public class FreeAppointmentSlotQuery extends BaseScheduleQuery {

    private Date date;
    private DateRange dateRange;

    private BoundTimeField fromTime;
    private BoundTimeField toTime;

    /**
     * Constructs a {@link FreeAppointmentSlotQuery}.
     *
     * @param location the current location. May be {@code null}
     * @param view     the current schedule view. May be {@code null}
     * @param schedule the current schedule. May be {@code null}
     * @param date     the current schedule date. May be {@code null}
     */
    public FreeAppointmentSlotQuery(Party location, Entity view, Party schedule, Date date) {
        super(new AppointmentSchedules(location));
        this.date = date;
        setScheduleView(view);
        setSchedule(schedule);
    }


    public Iterator<Slot> query() {
        FreeSlotQuery query = new FreeSlotQuery(ServiceHelper.getArchetypeService());
        Entity schedule = getSchedule();
        if (schedule != null) {
            query.setSchedules(schedule);
        } else {
            List<Entity> schedules = getSelectedSchedules();
            query.setSchedules(schedules.toArray(new Entity[schedules.size()]));
        }
        query.setFromDate(dateRange.getFrom());
        query.setToDate(dateRange.getTo());
        return query.query();
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(final Component container) {
        super.doLayout(container);
        dateRange = new DateRange(getFocusGroup(), false) {
            @Override
            protected Component getContainer() {
                // will lay the date range out in the supplied container instead of creating its own
                return container;
            }
        };
        if (date != null) {
            dateRange.setFrom(date);
        } else {
            dateRange.setFrom(new Date());
        }
        dateRange.setTo(DateRules.getDate(dateRange.getFrom(), 1, DateUnits.MONTHS));
        dateRange.getComponent();
    }
}
