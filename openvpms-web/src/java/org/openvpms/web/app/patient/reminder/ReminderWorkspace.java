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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.patient.reminder;

import java.util.List;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.web.app.patient.PatientActWorkspace;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.act.ActAmountTableModel;
import org.openvpms.web.resource.util.Messages;

/**
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */

public class ReminderWorkspace extends PatientActWorkspace {

    /**
     * Payment and refund shortnames supported by the workspace.
     */
    private static final String[] SHORT_NAMES = {"act.patientReminder",
                                                 "act.patientAlert"};

    /**
     * Construct a new <code>InvoiceWorkspace</code>.
     */
    public ReminderWorkspace() {
        super("patient", "reminder", "party", "party", "patient*");
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow createCRUDWindow() {
        String type = Messages.get("patient.reminder.createtype");
        return new ReminderCRUDWindow(type, SHORT_NAMES);
    }

    /**
     * Creates a new query.
     *
     * @param patient the customer to query acts for
     * @return a new query
     */
    protected ActQuery createQuery(Party patient) {
        IArchetypeService service
            = ArchetypeServiceHelper.getArchetypeService();
        ArchetypeDescriptor archetype
            = DescriptorHelper.getArchetypeDescriptor("act.patientReminder");
        NodeDescriptor statuses = archetype.getNodeDescriptor("status");    
        List<Lookup> lookups = LookupHelper.get(service, statuses);
        ActQuery query = new ActQuery(patient, "patient", "participation.patient",
                              SHORT_NAMES, lookups, null);
        query.setStatus("In Progress");
        return query;
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(IMObject object, boolean isNew) {
        super.onSaved(object, isNew);
    }

    /**
     * Creates a new table model to display acts.
     *
     * @return a new table model.
     */
    protected IMObjectTableModel createTableModel() {
        return new ActAmountTableModel(true, false);
    }

}
