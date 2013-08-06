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

package org.openvpms.web.workspace.customer;

import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.workspace.AbstractWorkspaces;
import org.openvpms.web.workspace.customer.account.AccountWorkspace;
import org.openvpms.web.workspace.customer.charge.ChargeWorkspace;
import org.openvpms.web.workspace.customer.document.CustomerDocumentWorkspace;
import org.openvpms.web.workspace.customer.estimation.EstimateWorkspace;
import org.openvpms.web.workspace.customer.info.InformationWorkspace;
import org.openvpms.web.workspace.customer.note.NoteAlertWorkspace;
import org.openvpms.web.workspace.customer.payment.PaymentWorkspace;


/**
 * Customer workspaces.
 *
 * @author Tim Anderson
 */
public class CustomerWorkspaces extends AbstractWorkspaces {

    /**
     * Constructs a {@code CustomerWorkspaces}.
     *
     * @param context the context
     */
    public CustomerWorkspaces(Context context) {
        super("customer");

        addWorkspace(new InformationWorkspace(context));
        addWorkspace(new CustomerDocumentWorkspace(context));
        addWorkspace(new EstimateWorkspace(context));
        addWorkspace(new ChargeWorkspace(context));
        addWorkspace(new PaymentWorkspace(context));
        addWorkspace(new AccountWorkspace(context));
        addWorkspace(new NoteAlertWorkspace(context));
    }

}
