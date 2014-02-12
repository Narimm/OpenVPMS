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
package org.openvpms.web.workspace.supplier.delivery;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.ActActions;


/**
 * Determines the operations that may be performed on <em>act.supplierDelivery</em> acts.
 *
 * @author Tim Anderson
 */
public class DeliveryActions extends ActActions<FinancialAct> {

    /**
     * The singleton instance.
     */
    public static final DeliveryActions INSTANCE = new DeliveryActions();


    /**
     * Default constructor.
     */
    private DeliveryActions() {
    }

    /**
     * Determines if an act can be edited.
     *
     * @param act the delivery to check
     * @return {@code true} if the delivery status is <em>IN_PROGRESS</em>
     */
    @Override
    public boolean canEdit(FinancialAct act) {
        return ActStatus.IN_PROGRESS.equals(act.getStatus());
    }

    /**
     * Determines if an act can be deleted.
     *
     * @param act the delivery to check
     * @return {@code true} if the act status is <em>IN_PROGRESS</em>
     */
    @Override
    public boolean canDelete(FinancialAct act) {
        return ActStatus.IN_PROGRESS.equals(act.getStatus());
    }

    /**
     * Determines if an act can be invoiced.
     *
     * @param act the act
     * @return {@code true} if the act is a delivery, and it is <em>POSTED</em>
     */
    public boolean canInvoice(FinancialAct act) {
        return ActStatus.POSTED.equals(act.getStatus()) && TypeHelper.isA(act, SupplierArchetypes.DELIVERY);
    }

}
