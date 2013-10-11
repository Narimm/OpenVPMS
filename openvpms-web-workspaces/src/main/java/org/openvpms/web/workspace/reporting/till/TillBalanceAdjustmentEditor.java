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

package org.openvpms.web.workspace.reporting.till;

import org.openvpms.archetype.rules.finance.till.TillBalanceStatus;
import org.openvpms.archetype.rules.finance.till.TillRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

/**
 * An editor for <em>act.tillBalanceAdjustment</em> acts.
 * <p/>
 * This links the act to an <em>act.tillBalance</em> if one is supplied at construction.
 *
 * @author Tim Anderson
 */
public class TillBalanceAdjustmentEditor extends AbstractActEditor {

    /**
     * The current balance. If non-null, the adjustment will be linked to the balance.
     */
    private FinancialAct currentBalance;

    /**
     * Constructs an {@link TillBalanceAdjustmentEditor}.
     *
     * @param act     the act to edit
     * @param balance the parent balance. May be {@code null}
     * @param context the layout context
     */
    public TillBalanceAdjustmentEditor(Act act, FinancialAct balance, LayoutContext context) {
        super(act, balance, context);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        boolean result = super.doValidation(validator);
        if (result) {
            FinancialAct balance = (FinancialAct) getParent();
            if (balance != null) {
                currentBalance = IMObjectHelper.reload(balance);   // make sure we have the latest instance
                if (currentBalance == null) {
                    ErrorDialog.show(Messages.format("imobject.noexist", DescriptorHelper.getDisplayName(balance)));
                    result = false;
                } else if (TillBalanceStatus.CLEARED.equals(currentBalance.getStatus())) {
                    ErrorDialog.show(Messages.get("till.adjustment.error.clearedBalance"));
                    result = false;
                }
            }
        }
        return result;
    }

    /**
     * Save any edits.
     * <p/>
     * This links the adjustment to the <em>act.tillBalance</em> and forces a recalculation, if one is present.
     *
     * @return {@code true} if the save was successful
     */
    @Override
    protected boolean doSave() {
        boolean result;
        if (currentBalance != null && !TillBalanceStatus.CLEARED.equals(currentBalance.getStatus())) {
            ActBean bean = new ActBean(currentBalance);
            bean.addNodeRelationship("items", (Act) getObject());
            bean.save();
            result = super.doSave();
            if (result) {
                // need to update the balance after the adjustment is saved
                TillRules rules = ServiceHelper.getBean(TillRules.class);
                rules.updateBalance(currentBalance);
            }
        } else {
            result = super.doSave();
        }
        return result;
    }
}
