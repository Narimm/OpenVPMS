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

package org.openvpms.web.app.customer.estimation;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.app.customer.CustomerActWorkspace;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DefaultActQuery;


/**
 * Estimation workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EstimationWorkspace extends CustomerActWorkspace<Act> {

    /**
     * The act statuses.
     */
    private static final ActStatuses STATUSES;
    static {
        STATUSES = new ActStatuses("act.customerEstimation");
        STATUSES.setDefault((String)null);
    }
    
    /**
     * Constructs a new <tt>EstimationWorkspace</tt>.
     */
    public EstimationWorkspace() {
        super("customer", "estimation");
        setChildArchetypes(Act.class, "act.customerEstimation");
    }


    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<Act> createCRUDWindow() {
        return new EstimationCRUDWindow(getChildArchetypes());
    }

    /**
     * Creates a new query.
     *
     * @return a new query
     */
    protected ActQuery<Act> createQuery() {
        return new DefaultActQuery<Act>(getObject(), "customer",
                                        "participation.customer",
                                        getChildArchetypes().getShortNames(),
                                        STATUSES);
    }
}
