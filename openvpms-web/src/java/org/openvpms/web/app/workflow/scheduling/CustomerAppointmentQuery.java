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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.workflow.scheduling;

import org.openvpms.archetype.rules.workflow.AppointmentQuery;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.app.workflow.WorkflowQuery;
import org.openvpms.web.component.im.query.AbstractArchetypeServiceResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.IMObjectHelper;


/**
 * Queries <em>act.customerAppointment</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerAppointmentQuery extends WorkflowQuery<ObjectSet> {

    /**
     * Construct a new <code>CustomerAppointmentQuery</code>.
     *
     * @param schedule the schedule
     */
    public CustomerAppointmentQuery(Party schedule) {
        super(schedule, "schedule", "participation.schedule",
              new String[]{"act.customerAppointment"}, new String[0]);
    }

    /**
     * Creates a new result set.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return a new result set
     */
    @Override
    protected ResultSet<ObjectSet> createResultSet(SortConstraint[] sort) {
        return new ActObjectResultSet(sort);
    }

    private class ActObjectResultSet
            extends AbstractArchetypeServiceResultSet<ObjectSet> {

        public ActObjectResultSet(SortConstraint[] sort) {
            super(ArchetypeQuery.ALL_RESULTS, sort);
        }

        /**
         * Returns the specified page.
         *
         * @param firstResult the first result of the page to retrieve
         * @param maxResults  the maximun no of results in the page
         * @return the page corresponding to <code>firstResult</code>, or
         *         <code>null</code> if none exists
         */
        protected IPage<ObjectSet> getPage(int firstResult, int maxResults) {
            Party schedule = (Party) IMObjectHelper.getObject(getEntityId());
            Party clinician = (Party) IMObjectHelper.getObject(getClinician());
            AppointmentQuery query = new AppointmentQuery();
            query.setSchedule(schedule);
            query.setClinician(clinician);
            query.setDateRange(getStartFrom(), getStartTo());
            return query.query();
        }

    }
}
