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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.export;

import org.openvpms.archetype.rules.export.ExportArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DateRangeActQuery;

/**
 * @author benjamincharlton on 19/03/2015.
 */
public class ExportQuery extends DateRangeActQuery<Act> {

    /**
     * The act statuses.
     */
    private static final ActStatuses STATUSES;

    static {
        STATUSES = new ActStatuses("act.customerExport");
        STATUSES.setDefault((String) null);
    }

    /**
     * Constructs an Export Query
     *
     * @param customer Party Customer to constrain to.
     */
    public ExportQuery(Party customer){
        super(customer,"customer", ExportArchetypes.EXPORTER_PARTICIPATION,
                new String[]{ExportArchetypes.EXPORT},STATUSES,Act.class);

    }
}
