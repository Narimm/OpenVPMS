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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.reporting.wip;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountActTypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.DateRangeActQuery;
import org.openvpms.web.component.im.util.FastLookupHelper;

import java.util.List;


/**
 * Query for incomplete charges. i.e invoices, credit and counter acts that
 * are IN_PROGRESS, COMPLETE, or ON_HOLD.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IncompleteChargesQuery extends DateRangeActQuery<Act> {

    /**
     * The act short names.
     */
    public static final String[] SHORT_NAMES = new String[]{
            CustomerAccountActTypes.CHARGES_INVOICE,
            CustomerAccountActTypes.CHARGES_CREDIT,
            CustomerAccountActTypes.CHARGES_COUNTER};

    /**
     * The default sort constraint.
     */
    private final SortConstraint[] DEFAULT_SORT = {
            new NodeSortConstraint("customer")
    };


    /**
     * Creates a new <tt>IncompleteChargesQuery</tt>.
     */
    public IncompleteChargesQuery() {
        super(null, null, null, SHORT_NAMES, getLookups(), ActStatus.POSTED);
        setDefaultSortConstraint(DEFAULT_SORT);
    }

    /**
     * Helper to return the status lookups.
     *
     * @return the status lookups
     */
    private static List<Lookup> getLookups() {
        return FastLookupHelper.getLookups(
                CustomerAccountActTypes.CHARGES_INVOICE, "status");
    }
}
