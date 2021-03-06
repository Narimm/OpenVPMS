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

package org.openvpms.web.workspace.reporting.deposit;

import nextapp.echo2.app.Button;
import org.openvpms.archetype.rules.finance.deposit.DepositQuery;
import org.openvpms.archetype.rules.finance.deposit.DepositRules;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.print.ObjectSetReportPrinter;
import org.openvpms.web.component.im.report.ReporterFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.servlet.DownloadServlet;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.reporting.FinancialActCRUDWindow;

import static org.openvpms.archetype.rules.finance.deposit.DepositArchetypes.BANK_DEPOSIT;
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
        Button deposit = ButtonFactory.create(DEPOSIT_ID, action(this::deposit, "deposit.deposit.title"));
        buttons.add(deposit);
        buttons.add(createPrintButton());
        buttons.add(createMailButton());
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
        enablePrintPreview(buttons, enable);
    }

    /**
     * Invoked when the 'deposit' button is pressed.
     *
     * @param object the deposit
     */
    protected void deposit(FinancialAct object) {
        if (UNDEPOSITED.equals(object.getStatus())) {
            String title = Messages.get("deposit.deposit.title");
            String message = Messages.get("deposit.deposit.message");
            ConfirmationDialog dialog = new ConfirmationDialog(title, message, getHelpContext().subtopic("deposit"));
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    doDeposit(object);
                }
            });
            dialog.show();
        } else {
            onRefresh(object);
        }
    }

    /**
     * Print an object.
     *
     * @param object the object to print
     */
    @Override
    protected void print(FinancialAct object) {
        IPage<ObjectSet> set = new DepositQuery(object).query();
        Context context = getContext();
        IMPrinter<ObjectSet> printer = new ObjectSetReportPrinter(set.getResults(), BANK_DEPOSIT, context,
                                                                  ServiceHelper.getBean(ReporterFactory.class));
        String title = Messages.format("imobject.print.title", getArchetypes().getDisplayName());
        InteractiveIMPrinter<ObjectSet> iPrinter = new InteractiveIMPrinter<>(
                title, printer, context, getHelpContext().subtopic("print"));
        iPrinter.setMailContext(getMailContext());
        iPrinter.print();
    }

    /**
     * Previews an object.
     *
     * @param object the object to preview
     */
    @Override
    protected void preview(FinancialAct object) {
        IPage<ObjectSet> set = new DepositQuery(object).query();
        Context context = getContext();
        IMPrinter<ObjectSet> printer = new ObjectSetReportPrinter(set.getResults(), BANK_DEPOSIT, context,
                                                                  ServiceHelper.getBean(ReporterFactory.class));
        Document document = printer.getDocument();
        DownloadServlet.startDownload(document);
    }

    /**
     * Deposits a <em>act.bankDeposit</em>.
     *
     * @param act the act to deposit
     */
    private void doDeposit(FinancialAct act) {
        try {
            DepositRules.deposit(act, ServiceHelper.getArchetypeService());
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
        onRefresh(act);
    }
}
