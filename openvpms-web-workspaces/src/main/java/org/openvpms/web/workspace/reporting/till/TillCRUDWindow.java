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

package org.openvpms.web.workspace.reporting.till;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.ListBox;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.rules.finance.till.TillArchetypes;
import org.openvpms.archetype.rules.finance.till.TillBalanceQuery;
import org.openvpms.archetype.rules.finance.till.TillBalanceStatus;
import org.openvpms.archetype.rules.finance.till.TillRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
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
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.print.ObjectSetReportPrinter;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.component.print.BasicPrinterListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.dialog.SelectionDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ComponentFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.reporting.FinancialActCRUDWindow;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND;
import static org.openvpms.archetype.rules.finance.till.TillBalanceStatus.IN_PROGRESS;
import static org.openvpms.archetype.rules.finance.till.TillBalanceStatus.UNCLEARED;


/**
 * CRUD window for till balances.
 *
 * @author Tim Anderson
 */
public class TillCRUDWindow extends FinancialActCRUDWindow {

    /**
     * The selected child act.
     */
    private FinancialAct childAct;

    /**
     * Start clear button identifier.
     */
    private static final String START_CLEAR_ID = "startClear";

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
    private final TillRules rules;


    /**
     * Constructs a {@link TillCRUDWindow}.
     *
     * @param context the context
     * @param help    the help context
     */
    public TillCRUDWindow(Context context, HelpContext help) {
        super(new Archetypes<FinancialAct>("act.tillBalanceAdjustment", FinancialAct.class), context, help);
        rules = ServiceHelper.getBean(TillRules.class);
    }

    /**
     * Sets the object.
     *
     * @param object the object. May be {@code null}
     */
    @Override
    public void setObject(FinancialAct object) {
        childAct = null;
        super.setObject(object);
    }

    /**
     * Invoked when the edit button is pressed This popups up an {@link EditDialog} if the act is an
     * <em>act.tillBalanceAdjustment</em>.
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
        Button startClear = ButtonFactory.create(START_CLEAR_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onStartClear();
            }
        });
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
        buttons.add(startClear);
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
        boolean inProgress = false;
        boolean enableEdit = false;
        boolean enableTransfer = false;
        if (enable) {
            Act act = getObject();
            if (TypeHelper.isA(act, TILL_BALANCE)) {
                uncleared = UNCLEARED.equals(act.getStatus());
                inProgress = IN_PROGRESS.equals(act.getStatus());
            }
            if (uncleared) {
                if (TypeHelper.isA(childAct, "act.tillBalanceAdjustment")) {
                    enableEdit = true;
                } else if (TypeHelper.isA(childAct, PAYMENT, REFUND)) {
                    enableTransfer = true;
                }
            }
        }
        buttons.setEnabled(START_CLEAR_ID, uncleared);
        buttons.setEnabled(CLEAR_ID, uncleared || inProgress);
        buttons.setEnabled(PRINT_ID, enable);
        buttons.setEnabled(ADJUST_ID, uncleared || inProgress);
        buttons.setEnabled(EDIT_ID, enableEdit);
        buttons.setEnabled(TRANSFER_ID, enableTransfer);
    }

    /**
     * Invoked when the 'start clear' button is pressed.
     */
    protected void onStartClear() {
        try {
            final FinancialAct act = getObject();
            ActBean balanceBean = new ActBean(act);
            Entity till = balanceBean.getNodeParticipant("till");
            if (!rules.isClearInProgress(till)) {
                IMObjectBean tillBean = new IMObjectBean(till);
                BigDecimal lastFloat = tillBean.getBigDecimal("tillFloat", ZERO);
                HelpContext help = getHelpContext().subtopic("startClear");
                final StartClearTillDialog dialog = new StartClearTillDialog(help);
                dialog.setCashFloat(lastFloat);
                dialog.addWindowPaneListener(new PopupDialogListener() {
                    @Override
                    public void onOK() {
                        rules.startClearTill(act, dialog.getCashFloat());
                        onRefresh(act);
                    }
                });
                dialog.show();
            } else {
                ErrorDialog.show(Messages.get("till.clear.title"),
                                 Messages.get("till.clear.error.clearInProgress"));
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception.getMessage(), exception);
        }
        onRefresh(getObject());
    }

    /**
     * Invoked when the 'clear' button is pressed.
     */
    protected void onClear() {
        try {
            final FinancialAct act = getObject();
            ActBean actBean = new ActBean(act);
            Party till = (Party) actBean.getNodeParticipant("till");
            Party location = getContext().getLocation();
            if (till != null && location != null) {
                boolean uncleared = UNCLEARED.equals(act.getStatus());
                boolean inProgress = IN_PROGRESS.equals(act.getStatus());
                HelpContext help = getHelpContext().subtopic("clear");
                if (uncleared) {
                    if (rules.isClearInProgress(till)) {
                        ErrorDialog.show(Messages.get("till.clear.title"),
                                         Messages.get("till.clear.error.clearInProgress"));
                    } else {
                        IMObjectBean bean = new IMObjectBean(till);
                        BigDecimal lastFloat = bean.getBigDecimal("tillFloat", ZERO);
                        final ClearTillDialog dialog = new ClearTillDialog(location, true, getContext(), help);
                        dialog.setCashFloat(lastFloat);
                        dialog.addWindowPaneListener(new PopupDialogListener() {
                            @Override
                            public void onOK() {
                                rules.clearTill(act, dialog.getCashFloat(), dialog.getAccount());
                                onRefresh(act);
                            }
                        });
                        dialog.show();
                    }
                } else if (inProgress) {
                    final ClearTillDialog dialog = new ClearTillDialog(location, false, getContext(), help);
                    dialog.addWindowPaneListener(new PopupDialogListener() {
                        @Override
                        public void onOK() {
                            rules.clearTill(act, dialog.getAccount());
                            onRefresh(act);
                        }
                    });
                    dialog.show();
                }
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
            final FinancialAct object = getObject();
            IPage<ObjectSet> set = new TillBalanceQuery(object).query();
            IMPrinter<ObjectSet> printer = new ObjectSetReportPrinter(set.getResults(), TILL_BALANCE, getContext());
            String displayName = DescriptorHelper.getDisplayName(TILL_BALANCE);
            String title = Messages.format("imobject.print.title", displayName);
            HelpContext help = getHelpContext().subtopic("print");
            InteractiveIMPrinter<ObjectSet> iPrinter =
                    new InteractiveIMPrinter<ObjectSet>(title, printer, getContext(), help);
            iPrinter.setMailContext(getMailContext());
            iPrinter.setListener(new BasicPrinterListener() {
                @Override
                public void printed(String printer) {
                    if (getActions().setPrinted(object)) {
                        onSaved(object, false);
                    }
                }
            });
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
        FinancialAct balance = getObject();
        adjustment.setDescription(Messages.get("till.adjustment.description"));
        ActBean actBean = new ActBean(balance);
        IMObjectReference till = actBean.getParticipantRef("participation.till");
        ActBean adjBean = new ActBean(adjustment);
        if (till != null) {
            adjBean.addNodeParticipation("till", till);
        }

        super.onCreated(adjustment);
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit.
     * @param context the layout context
     * @return a new editor
     */
    @Override
    protected IMObjectEditor createEditor(FinancialAct object, LayoutContext context) {
        // pass in the parent balance, if it isn't CLEARED
        FinancialAct balance = getObject();
        if (balance != null && !TillBalanceStatus.CLEARED.equals(balance.getStatus())) {
            return IMObjectEditorFactory.create(object, balance, context);
        } else {
            return super.createEditor(object, context);
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
        FinancialAct act = IMObjectHelper.reload(getObject());
        setObject(act);
    }

    /**
     * Invoked when a child act is selected/deselected.
     *
     * @param child the child act. May be {@code null}
     */
    @Override
    protected void onChildActSelected(FinancialAct child) {
        childAct = child;
        enableButtons(getButtons(), getObject() != null);
    }

    /**
     * Creates a new {@link IMObjectViewer} for an object.
     *
     * @param object the object to view
     */
    @Override
    protected IMObjectViewer createViewer(IMObject object) {
        LayoutContext context = createViewLayoutContext();
        LayoutStrategy layout = new TillBalanceActLayoutStrategy();
        return new IMObjectViewer(object, null, layout, context);
    }

    /**
     * Transfers the selected payment/refund to a different till.
     *
     * @param balance the original balance
     * @param act     the act to transfer
     * @param till    the till to transfer to
     */
    private void doTransfer(FinancialAct balance, FinancialAct act, Party till) {
        try {
            TillRules rules = ServiceHelper.getBean(TillRules.class);
            rules.transfer(balance, act, till);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception.getMessage(), exception);
        }
        onRefresh(getObject());
    }

    /**
     * Layout strategy for till balance acts that displays the start and end times as date/times.
     */
    private class TillBalanceActLayoutStrategy extends LayoutStrategy {

        /**
         * Apply the layout strategy.
         * <p/>
         * This renders an object in a {@code Component}, using a factory to create the child components.
         *
         * @param object     the object to apply
         * @param properties the object's properties
         * @param parent     the parent object. May be {@code null}
         * @param context    the layout context
         * @return the component containing the rendered {@code object}
         */
        @Override
        public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
            if (!context.isEdit()) {
                DateFormat format = DateFormatter.getDateTimeFormat(false);
                int maxColumns = DateFormatter.getLength(format);
                addComponent(createDate("startTime", properties, maxColumns, format));
                addComponent(createDate("endTime", properties, maxColumns, format));
            }
            return super.apply(object, properties, parent, context);
        }

        /**
         * Creates a date component for a property.
         *
         * @param name       the property name
         * @param properties the properties
         * @param maxColumns the maximum columns to display
         * @param format     the date format
         * @return a new component
         */
        private ComponentState createDate(String name, PropertySet properties, int maxColumns, DateFormat format) {
            Property property = properties.get(name);
            TextField result = BoundTextComponentFactory.create(property, maxColumns, format);
            result.setEnabled(false);
            ComponentFactory.setStyle(result, Styles.DEFAULT);
            return new ComponentState(result, property);
        }
    }
}
