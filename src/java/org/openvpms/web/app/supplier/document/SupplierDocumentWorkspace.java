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

import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.DocumentCRUDWindow;
import org.openvpms.web.app.supplier.SupplierActWorkspace;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DefaultActQuery;


/**
 * Supplier document workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class SupplierDocumentWorkspace
        extends SupplierActWorkspace<DocumentAct> {

    /**
     * Supplier Document shortnames supported by the workspace.
     */
    private static final String[] SHORT_NAMES = {
            "act.supplierDocumentForm", "act.supplierDocumentLetter",
            "act.supplierDocumentAttachment"};

    /**
     * The act statuses.
     */
    private static final ActStatuses STATUSES = new ActStatuses(
            "act.supplierDocumentLetter");


    /**
     * Constructs a new <tt>SupplierDocumentWorkspace</tt>.
     */
    public SupplierDocumentWorkspace() {
        super("supplier", "document");
        setChildArchetypes(DocumentAct.class, SHORT_NAMES);
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<DocumentAct> createCRUDWindow() {
        return new DocumentCRUDWindow(getChildArchetypes());
    }

    /**
     * Creates a new query.
     *
     * @return a new query
     */
    protected ActQuery<DocumentAct> createQuery() {
        return new DefaultActQuery<DocumentAct>(getObject(), "supplier",
                                                "participation.supplier",
                                                SHORT_NAMES, STATUSES);
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
