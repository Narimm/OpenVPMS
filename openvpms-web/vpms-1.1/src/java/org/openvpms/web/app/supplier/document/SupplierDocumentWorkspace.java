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

package org.openvpms.web.app.supplier.document;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.supplier.SupplierActWorkspace;
import org.openvpms.web.component.im.doc.DocumentCRUDWindow;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.DefaultActQuery;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;
import org.openvpms.web.component.im.util.FastLookupHelper;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


/**
 * Supplier document workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class SupplierDocumentWorkspace extends SupplierActWorkspace<Act> {

    /**
     * Supplier Document shortnames supported by the workspace.
     */
    private static final String[] SHORT_NAMES = {
            "act.supplierDocumentForm", "act.supplierDocumentLetter",
            "act.supplierDocumentAttachment"};

    /**
     * Constructs a new <tt>SupplierDocumentWorkspace</tt>.
     */
    public SupplierDocumentWorkspace() {
        super("supplier", "document");
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<Act> createCRUDWindow() {
        String type = Messages.get("supplier.document.createtype");
        return new DocumentCRUDWindow(type, SHORT_NAMES);
    }

    /**
     * Creates a new query.
     *
     * @param supplier the customer to query acts for
     * @return a new query
     */
    protected ActQuery<Act> createQuery(Party supplier) {
        List<Lookup> lookups = FastLookupHelper.getLookups(
                "act.supplierDocumentLetter", "status");
        return new DefaultActQuery<Act>(supplier, "supplier",
                                        "participation.supplier",
                                        SHORT_NAMES, lookups);
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(Act object, boolean isNew) {
        super.onSaved(object, isNew);
    }

    /**
     * Creates a new table model to display acts.
     *
     * @return a new table model.
     */
    protected IMObjectTableModel<Act> createTableModel() {
        return IMObjectTableModelFactory.create(SHORT_NAMES, null);
    }

}
