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

package org.openvpms.web.workspace.workflow.order;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DateRangeActQuery;

/**
 * Query for <em>act.customerOrder*</em> parent acts.
 *
 * @author Tim Anderson
 */
public class CustomerOrderQuery extends DateRangeActQuery<Act> {

    /**
     * The statuses to query.
     */
    private static final ActStatuses statuses = new ActStatuses("act.customerOrderPharmacy");

    /**
     * Constructs a {@link CustomerOrderQuery}.
     *
     * @param shortNames the act short names to query
     */
    public CustomerOrderQuery(String[] shortNames) {
        super(shortNames, statuses, Act.class);
    }
}
