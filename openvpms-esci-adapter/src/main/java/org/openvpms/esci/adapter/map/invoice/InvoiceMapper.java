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
 *
 *  $Id$
 */
package org.openvpms.esci.adapter.map.invoice;

import org.oasis.ubl.InvoiceType;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.esci.adapter.util.ESCIAdapterException;

/**
 * Maps UBL invoices to <em>act.supplierDelivery</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface InvoiceMapper {

    /**
     * Maps an UBL invoice to an <em>act.supplierDelivery</em>.
     *
     * @param invoice       the invoice to map
     * @param supplier      the supplier that submitted the invoice
     * @param stockLocation the stock location
     * @param accountId     the supplier assigned account identifier. May be <tt>null</tt>
     * @return the results of the mapping
     * @throws ESCIAdapterException if the invoice cannot be mapped
     * @throws OpenVPMSException    for any OpenVPMS error
     */
    Delivery map(InvoiceType invoice, Party supplier, Party stockLocation, String accountId);
}
