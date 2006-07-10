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

package org.openvpms.web.app.financial.till;

import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.component.dialog.SelectionDialog;
import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.table.IMObjectTable;
import org.openvpms.web.component.im.table.PagedIMObjectTable;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.resource.util.Messages;

import org.openvpms.archetype.rules.till.TillHelper;
import org.openvpms.archetype.rules.till.TillRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.ListBox;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;

import java.math.BigDecimal;
import java.util.List;


/**
 * CRUD window for till balances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-07-03 23:56:49Z $
 */
public class TillCRUDWindow extends CRUDWindow {

    /**
     * The clear button.
     */
    private Button _clear;

    /**
     * The summary button.
     */
    private Button _summary;

    /**
     * The print button.
     */
    private Button _print;

    /**
     * The adjust button.
     */
    private Button _adjust;

    /**
     * The transfer button.
     */
    private Button _transfer;

    /**
     * The selected child act.
     */
    private FinancialAct _childAct;

    /**
     * Clear button identifier.
     */
    private static final String CLEAR_ID = "clear";

    /**
     * Summary button identifier.
     */
    private static final String SUMMARY_ID = "summary";

    /**
     * Print button identifier.
     */
    private static final String PRINT_ID = "print";

    /**
     * Adjust button identifier.
     */
    private static final String ADJUST_ID = "adjust";

    /**
     * Transfer button identifier.
     */
    private static final String TRANSFER_ID = "transfer";


    /**
     * Create a new <code>EstimationCRUDWindow</code>.
     *
     * @param type         display name for the types of objects that this may
     *                     create
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public TillCRUDWindow(String type, String refModelName,
                          String entityName, String conceptName) {
        super(type, new ShortNameList(refModelName, entityName, conceptName));
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(Row buttons) {
        _clear = ButtonFactory.create(CLEAR_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onClear();
            }
        });
        _print = ButtonFactory.create(PRINT_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onPrint();
            }
        });
        _summary = ButtonFactory.create(SUMMARY_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onSummary();
            }
        });
        _adjust = ButtonFactory.create(ADJUST_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onAdjust();
            }
        });
        _transfer = ButtonFactory.create(TRANSFER_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onTransfer();
            }
        });
        buttons.add(_clear);
        buttons.add(_summary);
        buttons.add(_print);
        buttons.add(_adjust);
        buttons.add(getEditButton());
        buttons.add(_transfer);
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param enable determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(boolean enable) {
        Row buttons = getButtons();
        buttons.removeAll();
        if (enable) {
            Act act = (Act) getObject();
            boolean uncleared = false;
            if (TypeHelper.isA(act, "act.tillBalance")) {
                uncleared = "Uncleared".equals(act.getStatus());
            }
            if (uncleared) {
                buttons.add(_clear);
            }
            buttons.add(_summary);
            buttons.add(_print);
            if (uncleared) {
                buttons.add(_adjust);
                if (TypeHelper.isA(_childAct, "act.tillBalanceAdjustment")) {
                    buttons.add(getEditButton());
                } else if (TypeHelper.isA(_childAct,
                                          "act.customerAccountPayment",
                                          "act.customerAccountRefund")) {
                    buttons.add(_transfer);
                }
            }
        }
    }

    /**
     * Invoked when the 'clear' button is pressed.
     */
    protected void onClear() {
        final FinancialAct act = (FinancialAct) getObject();
        try {
            Party till = TillHelper.getTill(act);
            if (till != null) {
                IMObjectBean bean = new IMObjectBean(till);
                BigDecimal lastBalance = bean.getBigDecimal("lastBalance");
                if (lastBalance == null) {
                    lastBalance = BigDecimal.ZERO;
                }
                final ClearTillDialog dialog = new ClearTillDialog();
                dialog.setAmount(lastBalance);
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
     * Invoked when the 'summary' button is pressed.
     */
    protected void onSummary() {
    }

    /**
     * Invoked when the 'print' button is pressed.
     */
    protected void onPrint() {
    }

    /**
     * Invoked when the 'adjust' button is pressed.
     */
    protected void onAdjust() {
        onCreate();
    }

    /**
     * Invoked when the 'transfer' button is pressed.
     */
    protected void onTransfer() {
        final FinancialAct act = (FinancialAct) getObject();
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        String[] shortNames = {"party.organisationTill"};
        IPage<IMObject> page = ArchetypeQueryHelper.get(
                service, shortNames, true, 0, ArchetypeQuery.ALL_ROWS);
        List<IMObject> accounts = page.getRows();
        String title = Messages.get("till.transfer.title");
        String message = Messages.get("till.transfer.message");
        ListBox list = new ListBox(accounts.toArray());
        list.setCellRenderer(new IMObjectListCellRenderer());

        final SelectionDialog dialog
                = new SelectionDialog(title, message, list);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent e) {
                Party selected = (Party) dialog.getSelected();
                if (selected != null) {
                    doTransfer(act, _childAct, selected);
                }
            }
        });
        dialog.show();

    }

    /**
     * Invoked when a new adjustment has been created.
     *
     * @param object the new adjustment
     */
    @Override
    protected void onCreated(IMObject object) {
        // populate the adjust with the current till
        FinancialAct act = (FinancialAct) getObject();
        FinancialAct adjustment = (FinancialAct) object;
        IMObjectReference till = TillHelper.getTillReference(act);
        if (till != null) {
            TillHelper.addTill(adjustment, till);
        }
        super.onCreated(object);
    }

    /**
     * Invoked when the edit button is pressed for a. This popups up an {@link
     * EditDialog}.
     */
    @Override
    protected void onEdit() {
        if (TypeHelper.isA(_childAct, "act.tillBalanceAdjustment")) {
            LayoutContext context = new DefaultLayoutContext(true);
            final IMObjectEditor editor = createEditor(_childAct, context);
            EditDialog dialog = new EditDialog(editor, context);
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void windowPaneClosing(WindowPaneEvent event) {
                    onEditCompleted(editor, false);
                }
            });
            dialog.show();
        }
    }

    /**
     * Invoked when the editor is closed.
     *
     * @param editor the editor
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onEditCompleted(IMObjectEditor editor, boolean isNew) {
        FinancialAct act = (FinancialAct) getObject();
        if (editor.isSaved()) {
            if (isNew) {
                FinancialAct adjustment = (FinancialAct) editor.getObject();
                TillHelper.addRelationship("actRelationship.tillBalanceItem",
                                           act, adjustment);
                SaveHelper.save(act);
            }
        } else if (editor.isDeleted()) {
            IArchetypeService service
                    = ArchetypeServiceHelper.getArchetypeService();
            act = (FinancialAct) ArchetypeQueryHelper.getByObjectReference(
                    service, act.getObjectReference());
        }
        setObject(act);
    }

    /**
     * Creates a new {@link IMObjectViewer} for an object.
     *
     * @param object the object to view
     */
    protected IMObjectViewer createViewer(IMObject object) {
        IMObjectLayoutStrategy strategy = new ActLayoutStrategy() {

            /**
             * Creates a component to represent the item node.
             *
             * @param object     the parent object
             * @param items      the items node descriptor
             * @param properties the properties
             * @param context    the layout context
             * @return a component to represent the items node
             */
            protected Component createItems(IMObject object,
                                            NodeDescriptor items,
                                            PropertySet properties,
                                            LayoutContext context) {
                Component component = super.createItems(object, items,
                                                        properties,
                                                        context);
                final PagedIMObjectTable paged = (PagedIMObjectTable) component;
                final IMObjectTable table = paged.getTable();
                table.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        IMObject selected = table.getSelected();
                        FinancialAct child = null;
                        if (TypeHelper.isA(selected,
                                           "actRelationship.tillBalanceItem")) {
                            ActRelationship relationship
                                    = (ActRelationship) selected;
                            child = (FinancialAct) IMObjectHelper.getObject(
                                    relationship.getTarget());
                        }
                        setSelectedChild(child);
                    }
                });
                return paged;
            }
        };
        return new IMObjectViewer(object, strategy, null);
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
            TillRules.clearTill(act, amount, account,
                                ArchetypeServiceHelper.getArchetypeService());
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
            TillRules.transfer(balance, act, till,
                               ArchetypeServiceHelper.getArchetypeService());
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception.getMessage(), exception);
        }
        onRefresh(getObject());
    }

    /**
     * Sets the selected child act.
     *
     * @param child the child act. May be <code>null</code>
     */
    private void setSelectedChild(FinancialAct child) {
        _childAct = child;
        enableButtons(getObject() != null);
    }
}
