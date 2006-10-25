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

package org.openvpms.web.app.customer.document;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.web.app.customer.CustomerActWorkspace;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.component.im.doc.DocumentCRUDWindow;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.DefaultActQuery;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.act.ActAmountTableModel;
import org.openvpms.web.resource.util.Messages;

import java.util.List;

/**
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */

public class CustomerDocumentWorkspace extends CustomerActWorkspace {

    /**
     * Customer Document shortnames supported by the workspace.
     */
    private static final String[] SHORT_NAMES = {"act.customerDocumentForm",
                                                 "act.customerDocumentLetter",
                                                 "act.customerDocumentAttachment"};

    /**
     * Construct a new <code>CustomerDocumentWorkspace</code>.
     */
    public CustomerDocumentWorkspace() {
        super("customer", "document", "party", "party", "customer*");
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow createCRUDWindow() {
        String type = Messages.get("customer.document.createtype");
        return new DocumentCRUDWindow(type, SHORT_NAMES);
    }

    /**
     * Creates a new query.
     *
     * @param customer the customer to query acts for
     * @return a new query
     */
    protected ActQuery createQuery(Party customer) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor(
                "act.customerDocumentLetter");
        NodeDescriptor statuses = archetype.getNodeDescriptor("status");
        List<Lookup> lookups = LookupHelper.getSimpleLookups(service, statuses);
        return new DefaultActQuery(customer, "customer",
                                   "participation.customer",
                                   SHORT_NAMES, lookups, null);
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
    protected IMObjectTableModel<Act> createTableModel() {
        return new ActAmountTableModel(true, false);
    }

}
