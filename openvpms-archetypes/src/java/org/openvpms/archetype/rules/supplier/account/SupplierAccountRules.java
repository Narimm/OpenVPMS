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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.supplier.account;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.system.common.query.NamedQuery;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Supplier account rules.
 *
 * @author Tim Anderson
 */
public class SupplierAccountRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs an {@link SupplierAccountRules}.
     *
     * @param service the archetype service
     */
    public SupplierAccountRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns an account balance for a supplier.
     *
     * @param supplier the supplier
     * @return the account balance for {@code supplier}
     */
    public BigDecimal getBalance(Party supplier) {
        BigDecimal result = BigDecimal.ZERO;
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("supplierId", supplier.getId());
        NamedQuery query = new NamedQuery("getSupplierAccountBalance", Arrays.asList("balance"), parameters);
        ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(service, query);
        if (iterator.hasNext()) {
            ObjectSet set = iterator.next();
            result = set.getBigDecimal("balance", BigDecimal.ZERO);
        }
        return result;
    }

    /**
     * Reverses an account act.
     *
     * @param act       the act to reverse
     * @param startTime the start time of the reversal
     * @return the reversal of {@code act}
     */
    public FinancialAct reverse(FinancialAct act, Date startTime) {
        IMObjectCopier copier = new IMObjectCopier(new SupplierActReversalHandler(act));
        List<IMObject> objects = copier.apply(act);
        FinancialAct reversal = (FinancialAct) objects.get(0);
        reversal.setStatus(ActStatus.POSTED);
        reversal.setActivityStartTime(startTime);
        service.save(objects);
        return reversal;
    }
}
