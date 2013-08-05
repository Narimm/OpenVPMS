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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.workspace.customer.estimation;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.edit.ActActions;

import static org.openvpms.archetype.rules.act.ActStatus.CANCELLED;
import static org.openvpms.archetype.rules.act.ActStatus.COMPLETED;
import static org.openvpms.archetype.rules.act.ActStatus.IN_PROGRESS;
import static org.openvpms.archetype.rules.act.EstimateActStatus.INVOICED;


/**
 * Determines the operations that may be performed on <em>act.customerEstimation</em> acts.
 *
 * @author Tim Anderson
 */
public class EstimateActions extends ActActions<Act> {

    /**
     * Determines if an estimate can be edited.
     *
     * @param act the estimate to check
     * @return {@code true} if the estimate is {@code IN_PROGRESS}, {@code COMPLETED} or {@code CANCELLED}
     */
    @Override
    public boolean canEdit(Act act) {
        String status = act.getStatus();
        return IN_PROGRESS.equals(status) || COMPLETED.equals(status) || CANCELLED.equals(status);
    }

    /**
     * Determines if an act can be deleted.
     *
     * @param act the act to check
     * @return {@code true} if the estimate status isn't {@code POSTED} or {@code INVOICED}
     */
    @Override
    public boolean canDelete(Act act) {
        return super.canDelete(act) && !INVOICED.equals(act.getStatus());
    }

    /**
     * Determines if an act can be posted (i.e finalised).
     * <p/>
     * This implementation returns {@code true} if the act status isn't {@code POSTED}, {@code CANCELLED} or
     * {@code INVOICED}.
     *
     * @param act the estimate to check
     * @return {@code true} if the act can be posted
     */
    @Override
    public boolean canPost(Act act) {
        return super.canPost(act) && !INVOICED.equals(act.getStatus());
    }

    /**
     * Determines if the estimate can be invoiced.
     *
     * @param act the estimate to check
     * @return {@code true} if the estimate can be invoiced
     */
    public boolean canInvoice(Act act) {
        String status = act.getStatus();
        return !CANCELLED.equals(status) && !INVOICED.equals(status);
    }
}
