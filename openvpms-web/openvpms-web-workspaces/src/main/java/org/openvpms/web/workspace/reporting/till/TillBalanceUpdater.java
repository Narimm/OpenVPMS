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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.till;

import org.openvpms.archetype.rules.finance.till.TillBalanceRules;
import org.openvpms.archetype.rules.finance.till.TillBalanceStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;

/**
 * Links an act.tillBalanceAdjustment, act.customerAccountPayment, or act.customerAccountRefund to
 * an act.tillBalance, and updates the balance.
 * <p>
 * This is designed to be used within an editor.
 *
 * @author Tim Anderson
 */
class TillBalanceUpdater {

    /**
     * The act to link.
     */
    private final FinancialAct act;

    /**
     * The original balance act.
     */
    private final FinancialAct balance;

    /**
     * The current balance. If non-null, acts will be linked to the balance.
     */
    private FinancialAct currentBalance;

    /**
     * The till balance rules.
     */
    private final TillBalanceRules rules;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs a {@link TillBalanceUpdater}.
     *
     * @param act     the act
     * @param balance the balance
     */
    public TillBalanceUpdater(FinancialAct act, FinancialAct balance) {
        this.act = act;
        this.balance = balance;
        service = ServiceHelper.getArchetypeService();
        rules = new TillBalanceRules(service);
    }

    /**
     * Verifies that the act can be added to the balance.
     *
     * @return {@code true} if the act can be added to the balance
     */
    public boolean validate() {
        boolean result = true;
        currentBalance = IMObjectHelper.reload(balance);   // make sure we have the latest instance
        if (currentBalance == null) {
            ErrorDialog.show(Messages.format("imobject.noexist", DescriptorHelper.getDisplayName(balance)));
            result = false;
        } else if (TillBalanceStatus.CLEARED.equals(currentBalance.getStatus())) {
            ErrorDialog.show(Messages.get("till.adjustment.error.clearedBalance"));
            result = false;
        }
        return result;
    }

    /**
     * Adds the act to the balance, it the balance exists.
     * <p>
     * This should be invoked just prior to saving the act.
     *
     * @throws IllegalStateException if there is no current till balance, or it has been cleared
     */
    public void prepare() {
        if (currentBalance == null) {
            throw new IllegalStateException("There is no current balance");

        }
        if (TillBalanceStatus.CLEARED.equals(currentBalance.getStatus())) {
            throw new IllegalStateException("The current till balance has been cleared");
        }
        List<Act> changed = rules.addToBalance(act, currentBalance);
        if (!changed.isEmpty()) {
            service.save(changed);
        }
    }

    /**
     * Updates the balance after the act has been saved.
     */
    public void commit() {
        rules.updateBalance(currentBalance);
    }
}
