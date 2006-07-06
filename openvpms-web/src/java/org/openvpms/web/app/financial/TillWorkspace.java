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

package org.openvpms.web.app.financial;

import org.openvpms.web.app.subsystem.ActWorkspace;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.resource.util.Messages;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;

import nextapp.echo2.app.Component;

import java.util.List;


/**
 * Till workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-19 07:20:38Z $
 */
public class TillWorkspace extends ActWorkspace {

    /**
     * Construct a new <code>TillWorkspace</code>.
     */
    public TillWorkspace() {
        super("financial", "till", "party", "party", "organisationTill");
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <code>null</code>
     */
    @Override
    public void setObject(IMObject object) {
        super.setObject(object);
        Party till = (Party) object;
        layoutWorkspace(till, getRootComponent());
        initQuery(till);
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow createCRUDWindow() {
        String type = Messages.get("financial.till.createtype");
        return new TillCRUDWindow(type, "common", "act",
                                  "tillBalanceAdjustment");
    }

    /**
     * Creates a new query.
     *
     * @param till the till to query acts for
     * @return a new query
     */
    protected ActQuery createQuery(Party till) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor("act.tillBalance");
        NodeDescriptor statuses = archetype.getNodeDescriptor("status");
        List<Lookup> lookups = LookupHelper.get(service, statuses);
        ActQuery query = new ActQuery(till, "till", "participation.till",
                                      "act", "tillBalance", lookups, null);
        query.setStatus("Uncleared");
        return query;
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
    }
}
