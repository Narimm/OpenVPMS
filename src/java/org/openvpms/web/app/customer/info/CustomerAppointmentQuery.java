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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.customer.info;

import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DateRangeActQuery;

/**
 * Customer appointment query.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerAppointmentQuery extends DateRangeActQuery<Act> {

    /**
     * The act statuses to query.
     */
    private static final ActStatuses STATUSES = new ActStatuses(ScheduleArchetypes.APPOINTMENT);


    /**
     * Constructs a <tt>CustomerAppointmentQuery</tt>.
     *
     * @param customer the customer
     */
    public CustomerAppointmentQuery(Party customer) {
        super(customer, "customer", CustomerArchetypes.CUSTOMER_PARTICIPATION,
              new String[]{ScheduleArchetypes.APPOINTMENT}, STATUSES, Act.class);
    }

}
