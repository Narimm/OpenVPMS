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

package org.openvpms.web.app.financial.deposit;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.archetype.rules.finance.deposit.DepositQuery;
import org.openvpms.archetype.rules.finance.deposit.DepositRules;
import static org.openvpms.archetype.rules.finance.deposit.DepositStatus.UNDEPOSITED;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.app.financial.FinancialActCRUDWindow;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.print.ObjectSetReportPrinter;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * CRUD window for bank deposits.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-07-03 23:56:49Z $
 */
public class DepositCRUDWindow extends FinancialActCRUDWindow {

    /**
     * The deposit button.
     */
    private Button _deposit;

    /**
     * Deposit button identifier.
     */
    private static final String DEPOSIT_ID = "deposit";

    /**
     * Bank Deposit short name.
     */
    private static final String BANK_DEPOSIT = "act.bankDeposit";


    /**
     * Create a new <tt>EstimationCRUDWindow</tt>.
     *
     * @param type      display name for the types of objects that this may
     *                  create
     * @param shortName the archetype short name
     */
    public DepositCRUDWindow(String type, String shortName) {
        super(type, new ShortNameList(shortName));
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        _deposit = ButtonFactory.create(DepositCRUDWindow.DEPOSIT_ID,
                                        new ActionListener() {
                                            public void actionPerformed(
                                                    ActionEvent event) {
                                                onDeposit();
                                            }
                                        });
        buttons.add(_deposit);
        buttons.add(getPrintButton());
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
            FinancialAct act = getObject();
            if (UNDEPOSITED.equals(act.getStatus())) {
                buttons.add(_deposit);
            }
            buttons.add(getPrintButton());
        }
    }

    /**
     * Invoked when the 'clear' button is pressed.
     */
    protected void onDeposit() {
        final FinancialAct act = getObject();
        String title = Messages.get("deposit.deposit.title");
        String message = Messages.get("deposit.deposit.message");
        final ConfirmationDialog dialog
                = new ConfirmationDialog(title, message);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent e) {
                if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
                    doDeposit(act);
                }
            }
        });
        dialog.show();
    }

    /**
     * Deposits a <em>act.bankDeposit</em>.
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
            IMPrinter<ObjectSet> printer = new ObjectSetReportPrinter(
                    set.getResults(), BANK_DEPOSIT);
            String displayName = DescriptorHelper.getDisplayName(BANK_DEPOSIT);
            String title = Messages.get("imobject.print.title", displayName);
            InteractiveIMPrinter<ObjectSet> iPrinter
                    = new InteractiveIMPrinter<ObjectSet>(title, printer);
            iPrinter.print();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

}
