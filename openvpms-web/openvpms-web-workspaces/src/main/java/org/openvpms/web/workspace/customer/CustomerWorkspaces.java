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

package org.openvpms.web.workspace.customer;

import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.workspace.AbstractWorkspaces;
import org.openvpms.web.workspace.customer.account.AccountWorkspace;
import org.openvpms.web.workspace.customer.charge.ChargeWorkspace;
import org.openvpms.web.workspace.customer.communication.CommunicationWorkspace;
import org.openvpms.web.workspace.customer.document.CustomerDocumentWorkspace;
import org.openvpms.web.workspace.customer.estimate.EstimateWorkspace;
import org.openvpms.web.workspace.customer.info.InformationWorkspace;
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
     * @param context     the context
     * @param preferences the user preferences
     */
    public CustomerWorkspaces(Context context, Preferences preferences) {
        super("customer");

        addWorkspace(new InformationWorkspace(context, preferences));
        addWorkspace(new CustomerDocumentWorkspace(context, preferences));
        addWorkspace(new EstimateWorkspace(context, preferences));
        addWorkspace(new ChargeWorkspace(context, preferences));
        addWorkspace(new PaymentWorkspace(context, preferences));
        addWorkspace(new AccountWorkspace(context, preferences));
        addWorkspace(new CommunicationWorkspace(context, preferences));
    }

}
