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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.esci.adapter.dispatcher.invoice;

import org.openvpms.archetype.rules.workflow.SystemMessageReason;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.esci.adapter.dispatcher.AbstractSystemMessageFactory;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;


/**
 * An {@link InvoiceListener} that creates a new <em>act.systemMessage</em> with a link to the
 * <em>act.supplierDelivery</em>, and addressed to the author of the delivery.
 * <p/>
 * If the delivery has no author participation and there is no default author associated with the
 * delivery's stock location, then no message is created.
 *
 * @author Tim Anderson
 */
public class SystemMessageInvoiceListener extends AbstractSystemMessageFactory implements InvoiceListener {

    /**
     * Invoked when an invoice has been received and mapped to a delivery.
     *
     * @param delivery the delivery
     */
    public void receivedInvoice(FinancialAct delivery) {
        String subject = ESCIAdapterMessages.orderInvoiced().getMessage();
        createMessage(delivery, subject, SystemMessageReason.ORDER_INVOICED);
    }
}
