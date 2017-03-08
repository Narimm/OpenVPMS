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

package org.openvpms.web.workspace.customer.account;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.ArrayUtils;
import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRuleException;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.archetype.tools.account.AccountBalanceTool;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.FinancialActions;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.util.UserHelper;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.NumberFormatter;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.CustomerActCRUDWindow;

import java.math.BigDecimal;


/**
 * CRUD window for invoices.
 *
 * @author Tim Anderson
 */
public class AccountCRUDWindow extends CustomerActCRUDWindow<FinancialAct> {

    /**
     * The customer account rules.
     */
    private final CustomerAccountRules rules;

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
     * Hide button identifier.
     */
    private static final String HIDE_ID = "button.hide";

    /**
     * Unhide button identifier.
     */
    private static final String UNHIDE_ID = "button.unhide";


    /**
     * Constructs an {@link AccountCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param context    the context
     * @param help       the help context
     */
    public AccountCRUDWindow(Archetypes<FinancialAct> archetypes, Context context, HelpContext help) {
        super(archetypes, null, context, help);
        rules = ServiceHelper.getBean(CustomerAccountRules.class);
        setActions(new AccountActActions());
    }

    /**
     * Sets the object.
     *
     * @param object the object. May be {@code null}
     */
    @Override
    public void setObject(FinancialAct object) {
        super.setObject(object);
        updateContext(CustomerAccountArchetypes.INVOICE, object);
    }

    /**
     * Invoked when the 'print' button is pressed.
     */
    @Override
    protected void onPrint() {
        final FinancialAct object = IMObjectHelper.reload(getObject());
        if (object == null) {
            ErrorDialog.show(Messages.format("imobject.noexist", getArchetypes().getDisplayName()));
        } else {
            IMObjectBean bean = new IMObjectBean(object);
            if (bean.hasNode("hide") && bean.getBoolean("hide")) {
                String displayName = DescriptorHelper.getDisplayName(object);
                String title = Messages.format("customer.account.printhidden.title", displayName);
                String message = Messages.format("customer.account.printhidden.message", displayName, object.getId());
                ConfirmationDialog.show(title, message, ConfirmationDialog.YES_NO, new PopupDialogListener() {
                    @Override
                    public void onYes() {
                        print(object);
                    }
                });
            } else {
                print(object);
            }
        }
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

        buttons.add(adjust);
        buttons.add(reverse);
        buttons.add(createPrintButton());

        if (UserHelper.isAdmin(getContext().getUser())) {
            // If the logged in user is an administrator, show the Check, Hide, Unhide buttons
            Button check = ButtonFactory.create(CHECK_ID, new ActionListener() {
                public void onAction(ActionEvent event) {
                    onCheck();
                }
            });
            Button hide = ButtonFactory.create(HIDE_ID, new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    onHide();
                }
            });
            Button unhide = ButtonFactory.create(UNHIDE_ID, new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    onUnhide();
                }
            });
            buttons.add(check);
            buttons.add(hide);
            buttons.add(unhide);
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
        enablePrintPreview(buttons, enable);
        buttons.setEnabled(CHECK_ID, enable);
        buttons.setEnabled(HIDE_ID, enable && getActions().canHide(getObject()));
        buttons.setEnabled(UNHIDE_ID, enable && getActions().canUnhide(getObject()));
    }

    /**
     * Invoked when the 'reverse' button is pressed.
     */
    protected void onReverse() {
        final FinancialAct act = IMObjectHelper.reload(getObject());
        if (act != null) {
            String status = act.getStatus();
            if (TypeHelper.isA(act, CustomerAccountArchetypes.OPENING_BALANCE,
                               CustomerAccountArchetypes.CLOSING_BALANCE)
                || !FinancialActStatus.POSTED.equals(status)) {
                showStatusError(act, "customer.account.noreverse.title", "customer.account.noreverse.message");
            } else {
                Reverser reverser = new Reverser(getContext().getPractice(), getHelpContext().subtopic("reverse"));
                reverser.reverse(act, new Reverser.Listener() {
                    @Override
                    public void completed() {
                        onRefresh(act);
                    }
                });
            }
        }
    }

    /**
     * Invoked when the 'adjust' button is pressed.
     */
    protected void onAdjust() {
        Party customer = getContext().getCustomer();
        CustomerAccountRules rules = ServiceHelper.getBean(CustomerAccountRules.class);
        if (customer != null) {
            String[] shortNames = {CustomerAccountArchetypes.DEBIT_ADJUST,
                                   CustomerAccountArchetypes.CREDIT_ADJUST,
                                   CustomerAccountArchetypes.BAD_DEBT};
            if (!rules.hasAccountActs(customer)) {
                // only allow Initial Balance acts if there are no other customer account acts
                shortNames = (String[]) ArrayUtils.add(shortNames, 0, CustomerAccountArchetypes.INITIAL_BALANCE);
            }
            Archetypes<FinancialAct> archetypes = Archetypes.create(shortNames, FinancialAct.class,
                                                                    Messages.get("customer.account.createtype"));
            onCreate(archetypes);
        }
    }

    /**
     * Invoked when the 'check' button is pressed.
     */
    protected void onCheck() {
        final Party customer = getContext().getCustomer();
        if (customer != null) {
            try {
                BigDecimal expected = rules.getDefinitiveBalance(customer);
                BigDecimal actual = rules.getBalance(customer);
                if (expected.compareTo(actual) == 0) {
                    String title = Messages.get("customer.account.balancecheck.title");
                    String message = Messages.get("customer.account.balancecheck.ok");
                    InformationDialog.show(title, message);
                } else {
                    String message = Messages.format("customer.account.balancecheck.error",
                                                     NumberFormatter.formatCurrency(expected),
                                                     NumberFormatter.formatCurrency(actual));
                    confirmRegenerate(message, customer);
                }
            } catch (CustomerAccountRuleException exception) {
                String message = Messages.format("customer.account.balancecheck.acterror", exception.getMessage());
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
     * Returns the actions that may be performed on the selected object.
     *
     * @return the actions
     */
    @Override
    protected AccountActActions getActions() {
        return (AccountActActions) super.getActions();
    }

    /**
     * Invoked to hide a transaction.
     */
    private void onHide() {
        rules.setHidden(getObject(), true);
        onRefresh(getObject());
    }

    /**
     * Invoked to unhide a transaction.
     */
    private void onUnhide() {
        rules.setHidden(getObject(), false);
        onRefresh(getObject());
    }

    /**
     * Confirms if regeneration of a customer account balance should proceed.
     *
     * @param message  the confirmation message
     * @param customer the customer
     */
    private void confirmRegenerate(String message, final Party customer) {
        String title = Messages.get("customer.account.balancecheck.title");
        HelpContext check = getHelpContext().subtopic("check");
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
            IArchetypeService service = ServiceHelper.getArchetypeService(false);
            AccountBalanceTool tool = new AccountBalanceTool(service);
            tool.generate(customer);
            onRefresh(getObject());
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Determines if the current user is an administrator.
     *
     * @return {@code true} if the current user is an administrator
     */
    private boolean isAdmin() {
        return ServiceHelper.getBean(UserRules.class).isAdministrator(getContext().getUser());
    }


    private class AccountActActions extends FinancialActions<FinancialAct> {

        /**
         * Determines if an act can be unhidden.
         *
         * @param act the act
         * @return {@code true} if the act can be unhidden
         */
        public boolean canUnhide(FinancialAct act) {
            return act != null && isAdmin() && rules.isHidden(act);
        }

        /**
         * Determines if an act can be hidden.
         *
         * @param act the act
         * @return {@code true} if the act can be hidden
         */
        public boolean canHide(FinancialAct act) {
            return act != null && isAdmin() && rules.canHide(act) && !rules.isHidden(act);
        }
    }

}
