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
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.customer.CustomerActCRUDWindow;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;

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
    private Button _reverse;

    /**
     * The statement button.
     */
    private Button _statement;

    /**
     * The adjust button.
     */
    private Button _adjust;

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
     * Create a new <code>EstimationCRUDWindow</code>.
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
        _reverse = ButtonFactory.create(REVERSE_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onReverse();
            }
        });
        _statement = ButtonFactory.create(STATEMENT_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onStatement();
            }
        });
        _adjust = ButtonFactory.create(ADJUST_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onAdjust();
            }
        });
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
            buttons.add(_reverse);
            buttons.add(getPrintButton());
            buttons.add(_statement);
            buttons.add(_adjust);
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
