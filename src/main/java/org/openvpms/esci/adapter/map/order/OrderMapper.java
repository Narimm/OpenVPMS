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
package org.openvpms.esci.adapter.map.order;

import org.oasis.ubl.OrderType;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.esci.adapter.util.ESCIAdapterException;


/**
 * Maps an <em>act.supplierOrder</em> act to an UBL order.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface OrderMapper {

    /**
     * Maps an <em>act.suppplierOrder</em> to an UBL order.
     *
     * @param order the order to map
     * @return the UBL equivalent of the order
     * @throws ESCIAdapterException for mapping errors
     * @throws ArchetypeServiceException for any archetype service error
     */
    OrderType map(FinancialAct order);
}
