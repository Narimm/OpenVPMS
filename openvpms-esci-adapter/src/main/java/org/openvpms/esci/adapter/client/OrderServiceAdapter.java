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
package org.openvpms.esci.adapter.client;

import org.openvpms.component.business.domain.im.act.FinancialAct;


/**
 * Adapts <em>act.supplierorder</em> acts to UBL order documents, so they can be submitted to a supplier
 * via its {@link org.openvpms.esci.service.OrderService}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface OrderServiceAdapter {

    /**
     * Submits an order to a supplier.
     *
     * @param order the <em>act.supplierOrder</em> to submit
     * @throws org.openvpms.component.system.common.exception.OpenVPMSException
     *          for any error
     */
    void submitOrder(FinancialAct order);
}
