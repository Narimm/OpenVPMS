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

package org.openvpms.web.app.customer;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.DefaultActQuery;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.spring.ServiceHelper;

import java.util.List;


/**
 * Estimation workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EstimationWorkspace extends CustomerActWorkspace {

    /**
     * Construct a new <code>EstimationWorkspace</code>.
     */
    public EstimationWorkspace() {
        super("customer", "estimation", "party", "party", "customer*");
    }


    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow createCRUDWindow() {
        String type = Messages.get("customer.estimation.createtype");
        return new EstimationCRUDWindow(type, "common", "act",
                                        "customerEstimation");
    }

    /**
     * Creates a new query.
     *
     * @param customer the customer to query acts for
     * @return a new query
     */
    protected ActQuery createQuery(Party customer) {
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor(
                "act.customerEstimation");
        NodeDescriptor descriptor = archetype.getNodeDescriptor("status");
        List<Lookup> lookups = LookupHelper.getSimpleLookups(
                ServiceHelper.getArchetypeService(), descriptor);
        return new DefaultActQuery(customer, "customer",
                                   "participation.customer",
                                   "act", "customerEstimation", lookups);
    }
}
