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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.estimate;


import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.workflow.consult.ClinicianSelectionDialog;

/**
 * A clinician selection dialog used when estimates are being invoiced.
 *
 * @author Tim Anderson
 */
public class EstimateInvoiceClinicianSelectionDialog extends ClinicianSelectionDialog {

    /**
     * Constructs an {@link EstimateInvoiceClinicianSelectionDialog}.
     *
     * @param context the context
     * @param help    the parent help context
     */
    public EstimateInvoiceClinicianSelectionDialog(Context context, HelpContext help) {
        super(context, Messages.get("customer.estimate.invoice.title"),
              Messages.get("customer.estimate.invoicewithclinician"), help.subtopic("invoice"));

    }
}
