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
 */

package org.openvpms.web.app.reporting.deposit;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.finance.deposit.DepositQuery;
import org.openvpms.archetype.rules.finance.deposit.DepositRules;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.app.reporting.FinancialActCRUDWindow;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.print.ObjectSetReportPrinter;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;

import static org.openvpms.archetype.rules.finance.deposit.DepositStatus.UNDEPOSITED;


/**
 * CRUD window for bank deposits.
 *
 * @author Tim Anderson
 */
public class DepositCRUDWindow extends FinancialActCRUDWindow {

    /**
     * Deposit button identifier.
     */
    private static final String DEPOSIT_ID = "deposit";

    /**
     * Bank Deposit short name.
     */
    private static final String BANK_DEPOSIT = "act.bankDeposit";


    /**
     * Constructs a {@code DepositCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param context    the context
     * @param help       the help context
     */
    public DepositCRUDWindow(Archetypes<FinancialAct> archetypes, Context context, HelpContext help) {
        super(archetypes, context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        Button deposit = ButtonFactory.create(DepositCRUDWindow.DEPOSIT_ID,
                                              new ActionListener() {
                                                  public void onAction(ActionEvent event) {
                                                      onDeposit();
                                                  }
                                              });
        buttons.add(deposit);
        buttons.add(createPrintButton());
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        boolean enableDeposit = false;
        if (enable) {
            FinancialAct act = getObject();
            enableDeposit = UNDEPOSITED.equals(act.getStatus());
        }
        buttons.setEnabled(DEPOSIT_ID, enableDeposit);
        buttons.setEnabled(PRINT_ID, enable);
    }

    /**
     * Invoked when the 'deposit' button is pressed.
     */
    protected void onDeposit() {
        final FinancialAct act = getObject();
        String title = Messages.get("deposit.deposit.title");
        String message = Messages.get("deposit.deposit.message");
        final ConfirmationDialog dialog
                = new ConfirmationDialog(title, message);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                doDeposit(act);
            }
        });
        dialog.show();
    }

    /**
     * Deposits a <em>act.bankDeposit</em>.
     *
     * @param act the act to deposit
     */
    private void doDeposit(FinancialAct act) {
        try {
            DepositRules.deposit(act,
                                 ArchetypeServiceHelper.getArchetypeService());
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
        onRefresh(getObject());
    }

    /**
     * Prints the deposit slip.
     */
    @Override
    protected void onPrint() {
        FinancialAct object = getObject();
        try {
            IPage<ObjectSet> set = new DepositQuery(object).query();
            Context context = getContext();
            IMPrinter<ObjectSet> printer = new ObjectSetReportPrinter(set.getResults(), BANK_DEPOSIT, context);
            String title = Messages.get("imobject.print.title", getArchetypes().getDisplayName());
            InteractiveIMPrinter<ObjectSet> iPrinter = new InteractiveIMPrinter<ObjectSet>(title, printer, context,
                                                                                           getHelpContext());
            iPrinter.setMailContext(getMailContext());
            iPrinter.print();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

}
