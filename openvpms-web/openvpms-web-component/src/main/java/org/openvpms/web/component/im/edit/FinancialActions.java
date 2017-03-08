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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;

/**
 * Determines the operations that may be performed on financial acts such as <em>act.customerAccountCharge*</em>
 * and <em>act.customerEstimation</em>.
 *
 * @author Tim Anderson
 */
public class FinancialActions<T extends Act> extends ActActions<T> {

    /**
     * Sets an act's print status.
     * <p/>
     * For charges and estimates, the printed node cannot be set unless the act is POSTED.
     *
     * @param act     the act to update
     * @param printed the print status
     * @return {@code true} if the print status was changed, or {@code false} if the act doesn't have a 'printed' node
     * or its value is the same as that supplied, or the act status isn't {@link ActStatus#POSTED}.
     */
    @Override
    public boolean setPrinted(T act, boolean printed) {
        return ActStatus.POSTED.equals(act.getStatus()) && super.setPrinted(act, printed);
    }
}
