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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.till;

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Validator;

/**
 * An editor for <em>act.tillBalanceAdjustment</em> acts.
 * <p>
 * This links the act to the <em>act.tillBalance</em> supplied at construction.
 *
 * @author Tim Anderson
 */
public class TillBalanceAdjustmentEditor extends AbstractActEditor {

    /**
     * Updates the till balance with the adjustment.
     */
    private final TillBalanceUpdater updater;

    /**
     * Constructs a {@link TillBalanceAdjustmentEditor}.
     *
     * @param act     the act to edit
     * @param balance the parent balance
     * @param context the layout context
     */
    public TillBalanceAdjustmentEditor(FinancialAct act, FinancialAct balance, LayoutContext context) {
        super(act, null, context);
        updater = new TillBalanceUpdater(act, balance);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        return super.doValidation(validator) && updater.validate();
    }

    /**
     * Save any edits.
     * <p>
     * This links the adjustment to the <em>act.tillBalance</em> and forces a recalculation, if one is present.
     *
     * @throws OpenVPMSException if the save fails
     */
    @Override
    protected void doSave() {
        updater.prepare();
        super.doSave();
        updater.commit();
    }
}
