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

package org.openvpms.web.app.customer.account;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRuleException;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.tools.account.AccountBalanceTool;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.customer.CustomerActCRUDWindow;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.InformationDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.edit.DefaultActActions;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.util.UserHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.NumberFormatter;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.Date;


/**
 * CRUD window for invoices.
 *
 * @author Tim Anderson
 */
public class AccountCRUDWindow extends CustomerActCRUDWindow<FinancialAct> {

    /**
     * Reverse button identifier.
     */
    private static final String REVERSE_ID = "reverse";

    /**
     * Adjust button identifier.
     */
    private static final String ADJUST_ID = "adjust";

    /**
     * Check button identifier.
     */
    private static final String CHECK_ID = "check";

    /**
     * Opening Balance type.
     */
    private static final String OPENING_BALANCE_TYPE
        = "act.customerAccountOpeningBalance";

    /**
     * Closing Balance type.
     */
    private static final String CLOSING_BALANCE_TYPE
        = "act.customerAccountClosingBalance";


    /**
     * Constructs an {@code AccountCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param context    the context
     * @param help       the help context
     */
    public AccountCRUDWindow(Archetypes<FinancialAct> archetypes, Context context, HelpContext help) {
        super(archetypes, DefaultActActions.<FinancialAct>getInstance(), context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        Button reverse = ButtonFactory.create(REVERSE_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onReverse();
            }
        });

        Button adjust = ButtonFactory.create(ADJUST_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onAdjust();
            }
        });
        // If the logged in user is an administrator, show the Check button
        Button check = null;
        if (UserHelper.isAdmin(getContext().getUser())) {
            check = ButtonFactory.create(CHECK_ID, new ActionListener() {
                public void onAction(ActionEvent event) {
                    onCheck();
                }
            });
        }

        buttons.add(adjust);
        buttons.add(reverse);
        buttons.add(createPrintButton());

        if (check != null) {
            buttons.add(check);
        }

        enableButtons(buttons, true);
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        buttons.setEnabled(REVERSE_ID, enable);
        buttons.setEnabled(PRINT_ID, enable);
        buttons.setEnabled(CHECK_ID, enable);
    }

    /**
     * Invoked when the 'reverse' button is pressed.
     */
    protected void onReverse() {
        final FinancialAct act = getObject();
        String status = act.getStatus();
        if (!TypeHelper.isA(act, OPENING_BALANCE_TYPE, CLOSING_BALANCE_TYPE)
            && FinancialActStatus.POSTED.equals(status)) {
            String name = getArchetypeDescriptor().getDisplayName();
            String title = Messages.get("customer.account.reverse.title", name);
            String message = Messages.get("customer.account.reverse.message",
                                          name);
            HelpContext reverse = getHelpContext().createSubtopic("reverse");
            final ConfirmationDialog dialog = new ConfirmationDialog(title, message, reverse);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    reverse(act);
                }
            });
            dialog.show();
        } else {
            showStatusError(act, "customer.account.noreverse.title",
                            "customer.account.noreverse.message");
        }
    }

    /**
     * Invoked when the 'adjust' button is pressed.
     */
    protected void onAdjust() {
        String[] shortNames = {"act.customerAccountDebitAdjust",
            "act.customerAccountCreditAdjust",
            "act.customerAccountInitialBalance",
            "act.customerAccountBadDebt"};
        Archetypes<FinancialAct> archetypes = Archetypes.create(
            shortNames, FinancialAct.class,
            Messages.get("customer.account.createtype"));
        onCreate(archetypes);
    }

    /**
     * Invoked when the 'check' button is pressed.
     */
    protected void onCheck() {
        final Party customer = getContext().getCustomer();
        if (customer != null) {
            CustomerAccountRules rules = new CustomerAccountRules(ServiceHelper.getArchetypeService());
            try {
                BigDecimal expected = rules.getDefinitiveBalance(customer);
                BigDecimal actual = rules.getBalance(customer);
                if (expected.compareTo(actual) == 0) {
                    String title = Messages.get(
                        "customer.account.balancecheck.title");
                    String message = Messages.get(
                        "customer.account.balancecheck.ok");
                    InformationDialog.show(title, message);
                } else {
                    String message = Messages.get(
                        "customer.account.balancecheck.error",
                        NumberFormatter.formatCurrency(expected), NumberFormatter.formatCurrency(actual));
                    confirmRegenerate(message, customer);
                }
            } catch (CustomerAccountRuleException exception) {
                String message = Messages.get(
                    "customer.account.balancecheck.acterror",
                    exception.getMessage());
                confirmRegenerate(message, customer);
            }
        }
    }

    /**
     * Invoked when the adjustment editor is closed.
     *
     * @param editor the editor
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onEditCompleted(IMObjectEditor editor, boolean isNew) {
        if (editor.isSaved()) {
            onRefresh(getObject());
        }
    }

    /**
     * Reverse an invoice or credit act.
     *
     * @param act the act to reverse
     */
    private void reverse(FinancialAct act) {
        try {
            CustomerAccountRules rules = new CustomerAccountRules(ServiceHelper.getArchetypeService());
            rules.reverse(act, new Date(), Messages.get("customer.account.reverse.notes"));
        } catch (OpenVPMSException exception) {
            String title = Messages.get(
                "customer.account.reverse.failed",
                getArchetypeDescriptor().getDisplayName());
            ErrorHelper.show(title, exception);
        }
        onRefresh(act);
    }

    /**
     * Confirms if regeneration of a customer account balance should proceed.
     *
     * @param message  the confirmation message
     * @param customer the customer
     */
    private void confirmRegenerate(String message, final Party customer) {
        String title = Messages.get("customer.account.balancecheck.title");
        HelpContext check = getHelpContext().createSubtopic("check");
        final ConfirmationDialog dialog = new ConfirmationDialog(title, message, check);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                regenerate(customer);
            }
        });
        dialog.show();
    }

    /**
     * Regenerates the balance for a customer.
     *
     * @param customer the customer
     */
    private void regenerate(Party customer) {
        try {
            IArchetypeService service
                = ServiceHelper.getArchetypeService(false);
            AccountBalanceTool tool = new AccountBalanceTool(service);
            tool.generate(customer);
            onRefresh(getObject());
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

}
