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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.customer.account;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.finance.account.CustomerBalanceGenerator;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.customer.CustomerActCRUDWindow;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.InformationDialog;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.account.EditAccountActDialog;
import org.openvpms.web.component.im.util.UserHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.Date;


/**
 * CRUD window for invoices.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class AccountCRUDWindow extends CustomerActCRUDWindow<FinancialAct> {

    /**
     * The reverse button.
     */
    private Button reverse;

    /**
     * The statement button.
     */
    private Button statement;

    /**
     * The adjust button.
     */
    private Button adjust;

    /**
     * The check button.
     */
    private Button check;

    /**
     * Reverse button identifier.
     */
    private static final String REVERSE_ID = "reverse";

    /**
     * Statement button identifier.
     */
    private static final String STATEMENT_ID = "statement";

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
     * Create a new <tt>AccountCRUDWindow</tt>.
     *
     * @param type      display name for the types of objects that this may
     *                  create
     * @param shortName the archetype short name
     */
    public AccountCRUDWindow(String type, String shortName) {
        super(type, new ShortNameList(shortName));
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        reverse = ButtonFactory.create(REVERSE_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onReverse();
            }
        });
        statement = ButtonFactory.create(STATEMENT_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onStatement();
            }
        });
        adjust = ButtonFactory.create(ADJUST_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onAdjust();
            }
        });
        // If thelogged in user is an administrator, show the Check button
        if (UserHelper.isAdmin(GlobalContext.getInstance().getUser())) {
            check = ButtonFactory.create(CHECK_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onCheck();
                }
            });
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
        buttons.removeAll();
        if (enable) {
            buttons.add(reverse);
            buttons.add(getPrintButton());
            buttons.add(statement);
            buttons.add(adjust);
            if (check != null) {
                buttons.add(check);
            }
        }
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
            final ConfirmationDialog dialog
                    = new ConfirmationDialog(title, message);
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void windowPaneClosing(WindowPaneEvent e) {
                    if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
                        reverse(act);
                    }
                }
            });
            dialog.show();
        } else {
            showStatusError(act, "customer.account.noreverse.title",
                            "customer.account.noreverse.message");
        }
    }

    /**
     * Invoked when the 'statement' button is pressed.
     */
    protected void onStatement() {
    }

    /**
     * Invoked when the 'adjust' button is pressed.
     */
    protected void onAdjust() {
        String[] shortNames = {"act.customerAccountDebitAdjust",
                               "act.customerAccountCreditAdjust",
                               "act.customerAccountInitialBalance",
                               "act.customerAccountBadDebt"};
        onCreate(Messages.get("customer.account.createtype"),
                 new ShortNameList(shortNames));
    }

    /**
     * Invoked when the 'check' button is pressed.
     */
    protected void onCheck() {
        final Party customer = GlobalContext.getInstance().getCustomer();
        if (customer != null) {
            CustomerAccountRules rules = new CustomerAccountRules();
            BigDecimal expected = rules.getDefinitiveBalance(customer);
            BigDecimal actual = rules.getBalance(customer);
            String title = Messages.get("customer.account.balancecheck.title");
            if (expected.compareTo(actual) == 0) {
                String message = Messages.get(
                        "customer.account.balancecheck.ok");
                InformationDialog.show(title, message);
            } else {
                String message = Messages.get(
                        "customer.account.balancecheck.error",
                        expected, actual);
                final ConfirmationDialog dialog = new ConfirmationDialog(title,
                                                                         message);
                dialog.addWindowPaneListener(new WindowPaneListener() {
                    public void windowPaneClosing(WindowPaneEvent e) {
                        if (PopupDialog.OK_ID.equals(dialog.getAction())) {
                            try {
                                IArchetypeService service
                                        = ServiceHelper.getArchetypeService(
                                        false);
                                CustomerBalanceGenerator gen = new CustomerBalanceGenerator(
                                        service);
                                gen.generate(customer);
                            } catch (Throwable exception) {
                                ErrorHelper.show(exception);
                            }
                        }
                    }
                });
                dialog.show();
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
     * Creates a new edit dialog with Apply button disabled for <em>POSTED</em>
     * acts, to workaround OVPMS-733.
     *
     * @param editor the editor
     */
    @Override
    protected EditDialog createEditDialog(IMObjectEditor editor) {
        return new EditAccountActDialog(editor);
    }

    /**
     * Reverse an invoice or credit act.
     *
     * @param act the act to reverse
     */
    private void reverse(FinancialAct act) {
        try {
            CustomerAccountRules rules = new CustomerAccountRules();
            rules.reverse(act, new Date());
        } catch (OpenVPMSException exception) {
            String title = Messages.get(
                    "customer.account.reverse.failed",
                    getArchetypeDescriptor().getDisplayName());
            ErrorHelper.show(title, exception);
        }
        onRefresh(act);
    }


}
