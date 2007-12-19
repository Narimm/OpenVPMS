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

package org.openvpms.web.app.customer;

import org.openvpms.web.app.customer.account.AccountWorkspace;
import org.openvpms.web.app.customer.charge.InvoiceWorkspace;
import org.openvpms.web.app.customer.document.CustomerDocumentWorkspace;
import org.openvpms.web.app.customer.estimation.EstimationWorkspace;
import org.openvpms.web.app.customer.info.InformationWorkspace;
import org.openvpms.web.app.customer.note.NoteWorkspace;
import org.openvpms.web.app.customer.payment.PaymentWorkspace;
import org.openvpms.web.component.subsystem.AbstractSubsystem;


/**
 * Customer subsystem.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class CustomerSubsystem extends AbstractSubsystem {

    /**
     * Construct a new <tt>CustomerSubsystem</tt>.
     */
    public CustomerSubsystem() {
        super("customer");
        addWorkspace(new InformationWorkspace());
        addWorkspace(new CustomerDocumentWorkspace());
        addWorkspace(new EstimationWorkspace());
        addWorkspace(new InvoiceWorkspace());
        addWorkspace(new PaymentWorkspace());
        addWorkspace(new AccountWorkspace());
        addWorkspace(new NoteWorkspace());
    }
}
