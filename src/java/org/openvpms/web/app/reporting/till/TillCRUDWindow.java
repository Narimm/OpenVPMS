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

package org.openvpms.web.app.reporting.till;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.ListBox;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.archetype.rules.finance.till.TillBalanceQuery;
import org.openvpms.archetype.rules.finance.till.TillBalanceStatus;
import org.openvpms.archetype.rules.finance.till.TillRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.app.reporting.FinancialActCRUDWindow;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.SelectionDialog;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.print.ObjectSetReportPrinter;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;

import java.math.BigDecimal;
import java.util.List;


/**
 * CRUD window for till balances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-07-03 23:56:49Z $
 */
public class TillCRUDWindow extends FinancialActCRUDWindow {

    /**
     * The selected child act.
     */
    protected FinancialAct childAct;

    /**
     * The clear button.
     */
    private Button clear;

    /**
     * The adjust button.
     */
    private Button adjust;

    /**
     * The transfer button.
     */
    private Button transfer;

    /**
     * Clear button identifier.
     */
    private static final String CLEAR_ID = "clear";

    /**
     * Adjust button identifier.
     */
    private static final String ADJUST_ID = "adjust";

    /**
     * Transfer button identifier.
     */
    private static final String TRANSFER_ID = "transfer";

    /**
     * Till balance short name.
     */
    private static final String TILL_BALANCE = "act.tillBalance";


    /**
     * Creates a new <tt>TillCRUDWindow</tt>.
     */
    public TillCRUDWindow() {
        super(new Archetypes<FinancialAct>("act.tillBalanceAdjustment",
                                           FinancialAct.class));
    }

    /**
     * Sets the object.
     *
     * @param object the object. May be <code>null</code>
     */
    @Override
    public void setObject(FinancialAct object) {
        childAct = null;
        super.setObject(object);
    }

    /**
     * Invoked when the edit button is pressed This popups up an {@link
     * EditDialog} if the act is an <em>act.tillBalanceAdjustment</em>.
     */
    @Override
    public void edit() {
        if (TypeHelper.isA(childAct, "act.tillBalanceAdjustment")) {
            LayoutContext context = new DefaultLayoutContext(true);
            final IMObjectEditor editor = createEditor(childAct, context);
            EditDialog dialog = new EditDialog(editor);
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void windowPaneClosing(WindowPaneEvent event) {
                    onEditCompleted(editor, false);
                }
            });
            dialog.show();
        }
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        clear = ButtonFactory.create(CLEAR_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onClear();
            }
        });
        adjust = ButtonFactory.create(ADJUST_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onAdjust();
            }
        });
        transfer = ButtonFactory.create(TRANSFER_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onTransfer();
            }
        });
        buttons.add(clear);
        buttons.add(getPrintButton());
        buttons.add(adjust);
        buttons.add(getEditButton());
        buttons.add(transfer);
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
            Act act = getObject();
            boolean uncleared = false;
            if (TypeHelper.isA(act, TILL_BALANCE)) {
                uncleared = TillBalanceStatus.UNCLEARED.equals(act.getStatus());
            }
            if (uncleared) {
                buttons.add(clear);
            }
            buttons.add(getPrintButton());
            if (uncleared) {
                buttons.add(adjust);
                if (TypeHelper.isA(childAct, "act.tillBalanceAdjustment")) {
                    buttons.add(getEditButton());
                } else if (TypeHelper.isA(childAct,
                                          "act.customerAccountPayment",
                                          "act.customerAccountRefund")) {
                    buttons.add(transfer);
                }
            }
        }
    }

    /**
     * Invoked when the 'clear' button is pressed.
     */
    protected void onClear() {
        final FinancialAct act = getObject();
        try {
            ActBean actBean = new ActBean(act);
            Party till = (Party) actBean.getParticipant("participation.till");
            if (till != null) {
                IMObjectBean bean = new IMObjectBean(till);
                BigDecimal lastFloat = bean.getBigDecimal("tillFloat",
                                                          BigDecimal.ZERO);
                final ClearTillDialog dialog = new ClearTillDialog();
                dialog.setAmount(lastFloat);
                dialog.addWindowPaneListener(new WindowPaneListener() {
                    public void windowPaneClosing(WindowPaneEvent e) {
                        if (ClearTillDialog.OK_ID.equals(dialog.getAction())) {
                            doClear(act, dialog.getAmount(),
                                    dialog.getAccount());
                        }
                    }
                });
                dialog.show();
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Prints the till balance.
     */
    @Override
    protected void onPrint() {
        try {
            FinancialAct object = getObject();
            IPage<ObjectSet> set = new TillBalanceQuery(object).query();
            IMPrinter<ObjectSet> printer = new ObjectSetReportPrinter(
                    set.getResults(), TILL_BALANCE);
            String displayName = DescriptorHelper.getDisplayName(TILL_BALANCE);
            String title = Messages.get("imobject.print.title", displayName);
            InteractiveIMPrinter<ObjectSet> iPrinter
                    = new InteractiveIMPrinter<ObjectSet>(title, printer);
            iPrinter.print();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when the 'adjust' button is pressed.
     */
    protected void onAdjust() {
        create();
    }

    /**
     * Invoked when the 'transfer' button is pressed.
     */
    protected void onTransfer() {
        final FinancialAct act = getObject();
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        String[] shortNames = {"party.organisationTill"};
        IPage<IMObject> page = ArchetypeQueryHelper.get(
                service, shortNames, true, 0, ArchetypeQuery.ALL_RESULTS);
        List<IMObject> accounts = page.getResults();
        String title = Messages.get("till.transfer.title");
        String message = Messages.get("till.transfer.message");
        ListBox list = new ListBox(accounts.toArray());
        list.setCellRenderer(IMObjectListCellRenderer.NAME);

        final SelectionDialog dialog
                = new SelectionDialog(title, message, list);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent e) {
                Party selected = (Party) dialog.getSelected();
                if (selected != null) {
                    doTransfer(act, childAct, selected);
                }
            }
        });
        dialog.show();

    }

    /**
     * Invoked when a new adjustment has been created.
     *
     * @param adjustment the new adjustment
     */
    @Override
    protected void onCreated(FinancialAct adjustment) {
        // populate the adjust with the current till
        FinancialAct act = getObject();
        adjustment.setDescription(Messages.get("till.adjustment.description"));
        ActBean actBean = new ActBean(act);
        IMObjectReference till
                = actBean.getParticipantRef("participation.till");
        ActBean adjBean = new ActBean(adjustment);
        if (till != null) {
            adjBean.setParticipant("participation.till", till);
        }
        super.onCreated(adjustment);
    }

    /**
     * Invoked when the editor is closed.
     *
     * @param editor the editor
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onEditCompleted(IMObjectEditor editor, boolean isNew) {
        FinancialAct act = IMObjectHelper.reload(getObject());
        setObject(act);
    }

    /**
     * Invoked when a child act is selected/deselected.
     *
     * @param child the child act. May be <code>null</code>
     */
    @Override
    protected void onChildActSelected(FinancialAct child) {
        childAct = child;
        enableButtons(getButtons(), getObject() != null);
    }

    /**
     * Clears the current balance.
     *
     * @param act     the uncleared balance
     * @param amount  the amount to clear
     * @param account the account to deposit to
     */
    private void doClear(FinancialAct act, BigDecimal amount, Party account) {
        try {
            TillRules rules = new TillRules();
            rules.clearTill(act, amount, account);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception.getMessage(), exception);
        }
        onRefresh(getObject());
    }

    /**
     * Transfers the selected payment/refund to a different till.
     *
     * @param balance the original balance
     * @param act     the act to transfer
     * @param till    the till to transfer to
     */
    private void doTransfer(FinancialAct balance, FinancialAct act,
                            Party till) {
        try {
            TillRules rules = new TillRules();
            rules.transfer(balance, act, till);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception.getMessage(), exception);
        }
        onRefresh(getObject());
    }

}
