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
 *  Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.supplier;

import org.openvpms.archetype.rules.finance.tax.TaxRules;
import org.openvpms.archetype.rules.supplier.OrderRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.system.ServiceHelper;


/**
 * Supplier helper methods.
 *
 * @author Tim Anderson
 */
public class SupplierHelper {

    /**
     * Helper to create an {@link OrderRules}.
     *
     * @param practice the practice
     * @return a new {@code OrderRules}
     */
    public static OrderRules createOrderRules(Party practice) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        TaxRules taxRules = new TaxRules(practice, service, ServiceHelper.getLookupService());
        return new OrderRules(taxRules, service);
    }
}
