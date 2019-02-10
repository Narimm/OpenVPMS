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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.roster;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.ScheduleEvents;
import org.openvpms.archetype.rules.workflow.roster.RosterService;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Browses roster events by area.
 *
 * @author Tim Anderson
 */
public class AreaRosterBrowser extends RosterBrowser {

    /**
     * Constructs an {@link AreaRosterBrowser}.
     *
     * @param context the layout context
     */
    public AreaRosterBrowser(LayoutContext context) {
        super(new AreaRosterQuery(context.getContext()), context);
    }

    /**
     * Executes the query, returning the results in a grid.
     *
     * @param query the query
     * @return the results
     */
    @Override
    protected RosterEventGrid query(RosterQuery query) {
        RosterService service = ServiceHelper.getBean(RosterService.class);
        Date from = query.getDate();
        Date to = getTo(from);
        List<Entity> areas = ((AreaRosterQuery) query).getResults();
        Map<Entity, ScheduleEvents> events = new LinkedHashMap<>();
        for (Entity area : areas) {
            ScheduleEvents list = service.getScheduleEvents(area, from, to);
            events.put(area, list);
        }
        return new RosterEventGrid(from, to, 7, events);
    }

    /**
     * Returns the modification hash for the specified schedule and date range.
     *
     * @param entity    the entity
     * @param events    the events
     * @param startDate the start date
     * @param endDate   the end date
     * @param service   the roster service
     * @return the modification hash, or {@code -1} if the entity and range are not cached
     */
    @Override
    protected long getModHash(Entity entity, ScheduleEvents events, Date startDate, Date endDate, RosterService service) {
        return service.getModHash(entity, startDate, endDate);
    }

    /**
     * Creates a table model.
     *
     * @param grid the roster event grid
     * @return a new table model
     */
    @Override
    protected RosterTableModel createTableModel(RosterEventGrid grid) {
        return new AreaRosterTableModel(grid, getContext());
    }

    private Date getTo(Date from) {
        return DateRules.getDate(from, 7, DateUnits.DAYS);
    }

}
