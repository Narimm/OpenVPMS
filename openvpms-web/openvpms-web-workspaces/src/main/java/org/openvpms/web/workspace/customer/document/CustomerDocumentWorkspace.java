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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.document;

import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.component.workspace.DocumentCRUDWindow;
import org.openvpms.web.workspace.customer.CustomerActWorkspace;


/**
 * Workspace for <em>act.customerDocument*</em> acts.
 *
 * @author Tim Anderson
 */
public class CustomerDocumentWorkspace extends CustomerActWorkspace<DocumentAct> {

    /**
     * Constructs a {@link CustomerDocumentWorkspace}.
     *
     * @param context     the context
     * @param preferences user preferences
     */
    public CustomerDocumentWorkspace(Context context, Preferences preferences) {
        super("customer.document", context, preferences);
        setChildArchetypes(DocumentAct.class, CustomerDocumentQuery.SHORT_NAMES);
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
        return new CustomerDocumentQuery<DocumentAct>(getObject());
    }

}
