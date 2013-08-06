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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.estimation;

import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.finance.estimate.EstimateArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DefaultActQuery;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.workspace.customer.CustomerActWorkspace;


/**
 * Estimate workspace.
 *
 * @author Tim Anderson
 */
public class EstimateWorkspace extends CustomerActWorkspace<Act> {

    /**
     * The act statuses.
     */
    public static final ActStatuses STATUSES;

    static {
        STATUSES = new ActStatuses(EstimateArchetypes.ESTIMATE);
        STATUSES.setDefault((String) null);
    }

    /**
     * Constructs an {@link EstimateWorkspace}.
     *
     * @param context the context
     */
    public EstimateWorkspace(Context context) {
        super("customer", "estimate", context);
        setChildArchetypes(Act.class, EstimateArchetypes.ESTIMATE);
    }


    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<Act> createCRUDWindow() {
        return new EstimateCRUDWindow(getChildArchetypes(), getContext(), getHelpContext());
    }

    /**
     * Creates a new query.
     *
     * @return a new query
     */
    protected ActQuery<Act> createQuery() {
        return new DefaultActQuery<Act>(getObject(), "customer", CustomerArchetypes.CUSTOMER_PARTICIPATION,
                                        getChildArchetypes().getShortNames(), STATUSES);
    }
}
