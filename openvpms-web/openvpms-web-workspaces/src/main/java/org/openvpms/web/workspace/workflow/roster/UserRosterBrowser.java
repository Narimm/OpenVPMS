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
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Browses roster events by user.
 *
 * @author Tim Anderson
 */
class UserRosterBrowser extends RosterBrowser {

    /**
     * Constructs a {@link UserRosterBrowser}.
     *
     * @param context the layout context
     */
    UserRosterBrowser(LayoutContext context) {
        super(new UserRosterQuery(context.getContext()), context);
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
        int days = 7;
        Date to = DateRules.getDate(from, days, DateUnits.DAYS);
        List<User> users = ((UserRosterQuery) query).getResults();
        Map<Entity, ScheduleEvents> events = new LinkedHashMap<>();
        for (User user : users) {
            ScheduleEvents userEvents = service.getUserEvents(user, from, to);
            events.put(user, userEvents);
        }
        return new RosterEventGrid(from, to, days, events);
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
    protected long getModHash(Entity entity, ScheduleEvents events, Date startDate, Date endDate,
                              RosterService service) {
        return service.getUserModHash((User) entity, startDate, endDate);
    }

    /**
     * Creates a table model.
     *
     * @param grid the roster event grid
     * @return a new table model
     */
    @Override
    protected RosterTableModel createTableModel(RosterEventGrid grid) {
        return new UserRosterTableModel(grid, getContext());
    }

}
