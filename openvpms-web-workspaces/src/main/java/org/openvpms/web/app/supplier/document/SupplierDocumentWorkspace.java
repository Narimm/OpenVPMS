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
 */

package org.openvpms.web.app.supplier.document;

import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.web.app.subsystem.DocumentCRUDWindow;
import org.openvpms.web.app.supplier.SupplierActWorkspace;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.subsystem.CRUDWindow;


/**
 * Supplier document workspace.
 *
 * @author Tim Anderson
 */
public class SupplierDocumentWorkspace
    extends SupplierActWorkspace<DocumentAct> {

    /**
     * Constructs a {@code SupplierDocumentWorkspace}.
     *
     * @param context the context
     */
    public SupplierDocumentWorkspace(Context context) {
        super("supplier", "document", context);
        setChildArchetypes(DocumentAct.class, SupplierDocumentQuery.SHORT_NAMES);
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<DocumentAct> createCRUDWindow() {
        return new DocumentCRUDWindow(getChildArchetypes(), getContext(), getHelpContext());
    }

    /**
     * Creates a new query.
     *
     * @return a new query
     */
    protected ActQuery<DocumentAct> createQuery() {
        return new SupplierDocumentQuery<DocumentAct>(getObject());
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(DocumentAct object, boolean isNew) {
        super.onSaved(object, isNew);
    }

}
