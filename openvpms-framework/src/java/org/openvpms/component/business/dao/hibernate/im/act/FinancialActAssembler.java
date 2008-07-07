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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.act;

import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.domain.im.act.FinancialAct;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class FinancialActAssembler
        extends AbstractActAssembler<FinancialAct, FinancialActDO> {


    public FinancialActAssembler() {
        super(FinancialAct.class, FinancialActDO.class);
    }

    @Override
    protected void assembleDO(FinancialActDO result, FinancialAct source,
                              Context context) {
        super.assembleDO(result, source, context);
        result.setAllocatedAmount(source.getAllocatedAmount());
        result.setCredit(source.isCredit());
        result.setFixedAmount(source.getFixedAmount());
        result.setPrinted(source.isPrinted());
        result.setQuantity(source.getQuantity());
        result.setTaxAmount(source.getTaxAmount());
        result.setTotal(source.getTotal());
        result.setUnitAmount(source.getUnitAmount());
    }

    protected FinancialAct create(FinancialActDO object) {
        return new FinancialAct();
    }

    protected FinancialActDO create(FinancialAct object) {
        return new FinancialActDO();
    }
}
