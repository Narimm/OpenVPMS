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

package org.openvpms.web.workspace.customer.account;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.finance.statement.StatementRules;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;
import java.util.List;

/**
 * Reverses customer debit and credit acts.
 *
 * @author Tim Anderson
 */
public class Reverser {

    /**
     * The practice.
     */
    private final Party practice;

    /**
     * The customer account rules.
     */
    private final CustomerAccountRules rules;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * Listener used to notify of successful reversal.
     */
    public interface Listener {
        void completed();
    }

    /**
     * Constructs a {@link Reverser}.
     *
     * @param practice the practice
     * @param help     the help context
     */
    public Reverser(Party practice, HelpContext help) {
        this.practice = practice;
        this.rules = ServiceHelper.getBean(CustomerAccountRules.class);
        this.help = help;
    }

    /**
     * Reverse a debit or credit act.
     *
     * @param act      the act to reverse
     * @param listener the listener to notify on successful reversal
     */
    public void reverse(FinancialAct act, Listener listener) {
        reverse(act, null, listener);
    }

    /**
     * Reverse a debit or credit act.
     *
     * @param act         the act to reverse
     * @param tillBalance the till balance to add the reversal to. Only applies to payments and refunds.
     *                    May be {@code null}
     * @param listener    the listener to notify on successful reversal
     */
    public void reverse(final FinancialAct act, final FinancialAct tillBalance, final Listener listener) {
        if (rules.isReversed(act)) {
            ActBean bean = new ActBean(act);
            List<ActRelationship> reversal = bean.getValues("reversal", ActRelationship.class);
            if (!reversal.isEmpty()) {
                IMObjectReference target = reversal.get(0).getTarget();
                String reversalDisplayName = DescriptorHelper.getDisplayName(
                        target.getArchetypeId().getShortName());
                String displayName = DescriptorHelper.getDisplayName(act);
                String title = Messages.format("customer.account.reverse.title", displayName);
                String message = Messages.format("customer.account.reversed.message", displayName,
                                                 reversalDisplayName, target.getId());
                ErrorDialog.show(title, message);
            }
        } else {
            String name = DescriptorHelper.getDisplayName(act);
            String title = Messages.format("customer.account.reverse.title", name);
            String message = Messages.format("customer.account.reverse.message", name);
            final String notes = Messages.format("customer.account.reverse.notes",
                                                 DescriptorHelper.getDisplayName(act), act.getId());
            final String reference = Long.toString(act.getId());

            boolean canHide = canHideReversal(act);
            final ReverseConfirmationDialog dialog = new ReverseConfirmationDialog(title, message, help, notes,
                                                                                   reference, canHide);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    String reversalNotes = dialog.getNotes();
                    if (StringUtils.isEmpty(reversalNotes)) {
                        reversalNotes = notes;
                    }
                    String reversalRef = dialog.getReference();
                    if (StringUtils.isEmpty(reversalRef)) {
                        reversalRef = reference;
                    }
                    if (reverse(act, reversalNotes, reversalRef, dialog.getHide(), tillBalance)) {
                        listener.completed();
                    }
                }
            });
            dialog.show();
        }
    }

    /**
     * Reverse a debit or credit act.
     *
     * @param act         the act to reverse
     * @param notes       the reversal notes
     * @param reference   the reference
     * @param hide        if {@code true} flag the transaction and its reversal as hidden, so they don't appear in the
     *                    statement
     * @param tillBalance the till balance to add the reversal to. Only applies to payments and refunds.
     *                    May be {@code null}
     * @return {@code true} if the reverse was successful
     */
    private boolean reverse(FinancialAct act, String notes, String reference, boolean hide, FinancialAct tillBalance) {
        boolean result = false;
        try {
            rules.reverse(act, new Date(), notes, reference, hide, tillBalance);
            result = true;
        } catch (OpenVPMSException exception) {
            String title = Messages.format("customer.account.reverse.failed", DescriptorHelper.getDisplayName(act));
            ErrorHelper.show(title, exception);
        }
        return result;
    }

    /**
     * Determines if a reversal can be hidden in the customer statement.
     *
     * @param act the act to reverse
     * @return {@code true} if the reversal can be hidden
     */
    private boolean canHideReversal(FinancialAct act) {
        if (!rules.isHidden(act)) {
            StatementRules statementRules = new StatementRules(practice, ServiceHelper.getArchetypeService(), rules);
            ActBean bean = new ActBean(act);
            Party customer = (Party) bean.getNodeParticipant("customer");
            return customer != null && !statementRules.hasStatement(customer, act.getActivityStartTime());
        }
        return false;
    }

}
