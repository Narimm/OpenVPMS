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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.patient.charge;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.app.customer.charge.AbstractCustomerChargeActItemEditor;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * An editor for <em>act.customerAccountInvoiceItem</em> acts, in the context of a patient visit.
 *
 * @author Tim Anderson
 */
public class VisitChargeItemEditor extends AbstractCustomerChargeActItemEditor {

    /**
     * Constructs a {@code VisitChargeActItemEditor}.
     * <p/>
     * This recalculates the tax amount.
     *
     * @param act     the act to edit
     * @param parent  the parent act
     * @param context the layout context
     */
    public VisitChargeItemEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
    }
}
