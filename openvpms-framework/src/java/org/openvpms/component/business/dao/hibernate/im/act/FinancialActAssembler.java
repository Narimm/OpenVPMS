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

import org.openvpms.component.business.dao.hibernate.im.common.Assembler;
import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.domain.im.act.FinancialAct;


/**
 * An {@link Assembler} responsible for assembling {@link FinancialActDO}
 * instances from {@link FinancialAct}s and vice-versa.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class FinancialActAssembler
        extends AbstractActAssembler<FinancialAct, FinancialActDO> {

    /**
     * Creates a new <tt>FinancialActAssembler</tt>.
     */
    public FinancialActAssembler() {
        super(FinancialAct.class, FinancialActDO.class,
              FinancialActDOImpl.class);
    }

    /**
     * Assembles a data object from an object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param state   the data object state
     * @param context the assembly context
     */
    @Override
    protected void assembleDO(FinancialActDO target, FinancialAct source,
                              DOState state, Context context) {
        super.assembleDO(target, source, state, context);
        target.setAllocatedAmount(source.getAllocatedAmount());
        target.setCredit(source.isCredit());
        target.setFixedAmount(source.getFixedAmount());
        target.setUnitAmount(source.getUnitAmount());
        target.setFixedCost(source.getFixedCost());
        target.setUnitCost(source.getUnitCost());
        target.setPrinted(source.isPrinted());
        target.setQuantity(source.getQuantity());
        target.setTaxAmount(source.getTaxAmount());
        target.setTotal(source.getTotal());
    }

    /**
     * Assembles an object from a data object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param context the assembly context
     */
    @Override
    protected void assembleObject(FinancialAct target, FinancialActDO source,
                                  Context context) {
        super.assembleObject(target, source, context);
        target.setAllocatedAmount(source.getAllocatedAmount());
        target.setCredit(source.isCredit());
        target.setFixedAmount(source.getFixedAmount());
        target.setUnitAmount(source.getUnitAmount());
        target.setFixedCost(source.getFixedCost());
        target.setUnitCost(source.getUnitCost());
        target.setPrinted(source.isPrinted());
        target.setQuantity(source.getQuantity());
        target.setTaxAmount(source.getTaxAmount());
        target.setTotal(source.getTotal());
    }

    /**
     * Creates a new object.
     *
     * @param object the source data object
     * @return a new object corresponding to the supplied data object
     */
    protected FinancialAct create(FinancialActDO object) {
        return new FinancialAct();
    }

    /**
     * Creates a new data object.
     *
     * @param object the source object
     * @return a new data object corresponding to the supplied object
     */
    protected FinancialActDO create(FinancialAct object) {
        return new FinancialActDOImpl();
    }
}
