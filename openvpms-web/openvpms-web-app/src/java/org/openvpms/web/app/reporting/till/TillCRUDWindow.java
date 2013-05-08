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

package org.openvpms.web.app.reporting.till;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.ListBox;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.till.TillArchetypes;
import org.openvpms.archetype.rules.finance.till.TillBalanceQuery;
import org.openvpms.archetype.rules.finance.till.TillBalanceStatus;
import org.openvpms.archetype.rules.finance.till.TillRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.app.reporting.FinancialActCRUDWindow;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.dialog.SelectionDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.print.ObjectSetReportPrinter;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.List;


/**
 * CRUD window for till balances.
 *
 * @author Tim Anderson
 */
public class TillCRUDWindow extends FinancialActCRUDWindow {

    /**
     * The selected child act.
     */
    protected FinancialAct childAct;

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
     * Constructs a {@code TillCRUDWindow}.
     *
     * @param context the context
     * @param help    the help context
     */
    public TillCRUDWindow(Context context, HelpContext help) {
        super(new Archetypes<FinancialAct>("act.tillBalanceAdjustment", FinancialAct.class), context, help);
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
            LayoutContext context = createLayoutContext(createEditTopic(childAct));
            final IMObjectEditor editor = createEditor(childAct, context);
            EditDialog dialog = new EditDialog(editor, getContext());
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void onClose(WindowPaneEvent event) {
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
        Button clear = ButtonFactory.create(CLEAR_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onClear();
            }
        });
        Button adjust = ButtonFactory.create(ADJUST_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onAdjust();
            }
        });
        Button transfer = ButtonFactory.create(TRANSFER_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onTransfer();
            }
        });
        buttons.add(clear);
        buttons.add(createPrintButton());
        buttons.add(adjust);
        buttons.add(createEditButton());
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
        boolean uncleared = false;
        boolean enableEdit = false;
        boolean enableTransfer = false;
        if (enable) {
            Act act = getObject();
            if (TypeHelper.isA(act, TILL_BALANCE)) {
                uncleared = TillBalanceStatus.UNCLEARED.equals(act.getStatus());
            }
            if (uncleared) {
                if (TypeHelper.isA(childAct, "act.tillBalanceAdjustment")) {
                    enableEdit = true;
                } else if (TypeHelper.isA(childAct,
                                          CustomerAccountArchetypes.PAYMENT, CustomerAccountArchetypes.REFUND)) {
                    enableTransfer = true;
                }
            }
        }
        buttons.setEnabled(CLEAR_ID, uncleared);
        buttons.setEnabled(PRINT_ID, enable);
        buttons.setEnabled(ADJUST_ID, uncleared);
        buttons.setEnabled(EDIT_ID, enableEdit);
        buttons.setEnabled(TRANSFER_ID, enableTransfer);
    }

    /**
     * Invoked when the 'clear' button is pressed.
     */
    protected void onClear() {
        final FinancialAct act = getObject();
        try {
            ActBean actBean = new ActBean(act);
            Party till = (Party) actBean.getParticipant("participation.till");
            Party location = getContext().getLocation();
            if (till != null && location != null) {
                IMObjectBean bean = new IMObjectBean(till);
                BigDecimal lastFloat = bean.getBigDecimal("tillFloat", BigDecimal.ZERO);
                HelpContext help = getHelpContext().subtopic("clear");
                final ClearTillDialog dialog = new ClearTillDialog(location, getContext(), help);
                dialog.setAmount(lastFloat);
                dialog.addWindowPaneListener(new PopupDialogListener() {
                    @Override
                    public void onOK() {
                        doClear(act, dialog.getAmount(), dialog.getAccount());
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
            IMPrinter<ObjectSet> printer = new ObjectSetReportPrinter(set.getResults(), TILL_BALANCE, getContext());
            String displayName = DescriptorHelper.getDisplayName(TILL_BALANCE);
            String title = Messages.get("imobject.print.title", displayName);
            HelpContext help = getHelpContext().subtopic("print");
            InteractiveIMPrinter<ObjectSet> iPrinter =
                new InteractiveIMPrinter<ObjectSet>(title, printer, getContext(), help);
            iPrinter.setMailContext(getMailContext());
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
        ActBean bean = new ActBean(act);
        Party till = (Party) bean.getNodeParticipant("till");
        List<IMObject> tills = getTillsExcluding(till);

        String title = Messages.get("till.transfer.title");
        String message = Messages.get("till.transfer.message");
        ListBox list = new ListBox(tills.toArray());
        list.setStyleName("default");
        list.setHeight(new Extent(10, Extent.EM));
        list.setCellRenderer(IMObjectListCellRenderer.NAME);

        final SelectionDialog dialog = new SelectionDialog(title, message, list,
                                                           getHelpContext().subtopic("transfer"));
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void onClose(WindowPaneEvent e) {
                Party selected = (Party) dialog.getSelected();
                if (selected != null) {
                    doTransfer(act, childAct, selected);
                }
            }
        });
        dialog.show();

    }

    /**
     * Returns all tills except that supplied.
     *
     * @param till the till to exclude
     * @return the list of available tills
     */
    private List<IMObject> getTillsExcluding(Party till) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        ArchetypeQuery query = new ArchetypeQuery(TillArchetypes.TILL, true).setMaxResults(ArchetypeQuery.ALL_RESULTS);
        List<IMObject> tills = service.get(query).getResults();
        tills.remove(till);
        return tills;
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
