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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.visit;

import echopointng.KeyStrokes;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.web.component.macro.MacroDialog;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.customer.charge.UndispensedOrderChecker;
import org.openvpms.web.workspace.patient.charge.VisitChargeEditor;

/**
 * Browser that displays clinical events and their child acts and supports editing them.
 *
 * @author Tim Anderson
 */
public class VisitEditorDialog extends PopupDialog {

    /**
     * The visit browser.
     */
    private final VisitEditor editor;

    /**
     * Constructs a {@link VisitEditorDialog}.
     *
     * @param title       the dialog title
     * @param visitEditor the visit browser
     * @param help        the help context
     */
    public VisitEditorDialog(String title, VisitEditor visitEditor, HelpContext help) {
        super(title, "BrowserDialog", APPLY_OK_CANCEL, help);
        this.editor = visitEditor;
        setModal(true);
        getLayout().add(visitEditor.getComponent());

        VisitChargeEditor chargeEditor = editor.getChargeEditor();
        if (chargeEditor != null) {
            final Property status = chargeEditor.getProperty("status");
            if (status != null) {
                onStatusChanged(status);
                status.addModifiableListener(new ModifiableListener() {
                    public void modified(Modifiable modifiable) {
                        onStatusChanged(status);
                    }
                });
            }
        }

        visitEditor.setListener(new VisitEditorListener() {
            @Override
            public void selected(int index) {
                VisitEditorDialog.this.onSelected(index);
            }
        });
        setHistoryButtons();
        getButtons().addKeyListener(KeyStrokes.ALT_MASK | KeyStrokes.VK_M, new ActionListener() {
            public void onAction(ActionEvent event) {
                onMacro();
            }
        });
    }

    /**
     * Returns the visit editor.
     *
     * @return the editor
     */
    public VisitEditor getEditor() {
        return editor;
    }

    /**
     * Returns the help context.
     * <p/>
     * This implementation returns the help context of the selected tab
     *
     * @return the help context
     */
    @Override
    public HelpContext getHelpContext() {
        return editor.getHelpContext();
    }

    /**
     * Invoked when the 'apply' button is pressed. This saves the editor, printing unprinted documents.
     */
    @Override
    protected void onApply() {
        prepare(false);
    }

    /**
     * Invoked when the 'OK' button is pressed. This saves the editor, prints unprinted documents, and closes the
     * window.
     */
    @Override
    protected void onOK() {
        prepare(true);
    }

    /**
     * Invoked when a tab is selected.
     *
     * @param index the tab index.
     */
    protected void onSelected(int index) {
        switch (index) {
            case VisitEditor.HISTORY_TAB:
                onHistorySelected();
                break;
            case VisitEditor.PROBLEM_TAB:
                onProblemsSelected();
                break;
            case VisitEditor.INVOICE_TAB:
                onInvoiceSelected();
                break;
            case VisitEditor.REMINDER_TAB:
                onRemindersSelected();
                break;
            case VisitEditor.DOCUMENT_TAB:
                onDocumentsSelected();
                break;
            case VisitEditor.PRESCRIPTION_TAB:
                onPrescriptionSelected();
                break;
            case VisitEditor.ESTIMATE_TAB:
                onEstimatesSelected();
                break;
        }
    }

    /**
     * Sets the dialog buttons to the default Apply, OK and Cancel buttons.
     *
     * @param apply if {@code true}, add an Apply button
     * @return the buttons
     */
    protected ButtonSet setDefaultButtons(boolean apply) {
        ButtonSet buttons = getButtons();
        buttons.removeAll();
        if (apply) {
            addButton(APPLY_ID);
        }
        addButton(OK_ID);
        addButton(CANCEL_ID);
        return buttons;
    }


    /**
     * Prepares to save the charge.
     * <p/>
     * This determines if an invoice is being posted, and if so, displays a confirmation dialog if there are
     * any orders waiting to be dispensed.
     * <p/>
     * If not, or the user confirms that the save should go ahead, delegates to {@link #saveCharge(boolean)}.
     *
     * @param close if {@code true}, closes the dialog when the save is successful
     */
    private void prepare(final boolean close) {
        VisitChargeEditor chargeEditor = editor.getChargeEditor();
        if (chargeEditor != null) {
            UndispensedOrderChecker checker = new UndispensedOrderChecker(chargeEditor);
            checker.confirm(getHelpContext(), new Runnable() {
                @Override
                public void run() {
                    saveCharge(close);
                }
            });
        } else if (close) {
            super.onOK();
        }
    }

    /**
     * Saves the current object.
     * <p/>
     * Any documents added as part of the save that have a template with an IMMEDIATE print mode will be printed.
     */
    private void saveCharge(boolean close) {
        if (editor.save()) {
            printNew(close);
        }
    }

    /**
     * Disables the apply button if the charge act status is <em>POSTED</em>, otherwise enables it.
     *
     * @param status the act status property
     */
    private void onStatusChanged(Property status) {
        String value = (String) status.getValue();
        Button apply = getButtons().getButton(APPLY_ID);
        if (apply != null) {
            if (ActStatus.POSTED.equals(value)) {
                apply.setEnabled(false);
            } else {
                apply.setEnabled(true);
            }
        }
    }

    /**
     * Invoked when the patient history tab is selected.
     * <p/>
     * Updates the dialog buttons
     */
    private void onHistorySelected() {
        setHistoryButtons();
    }

    /**
     * Invoked when the problems tab is selected.
     * <p/>
     * Updates the dialog buttons
     */
    private void onProblemsSelected() {
        ButtonSet buttons = setDefaultButtons(false);
        editor.setButtons(buttons);
    }

    /**
     * Invoked when the invoice tab is selected.
     * <p/>
     * Updates the dialog buttons
     */
    private void onInvoiceSelected() {
        ButtonSet buttons = getButtons();
        buttons.removeAll();
        if (getEditor().getChargeEditor() != null) {
            addButton(APPLY_ID);
        }
        addButton(OK_ID);
        addButton(CANCEL_ID);
        if (getEditor().getChargeEditor() == null) {
            buttons.add(VisitChargeCRUDWindow.NEW_ID, new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    onNew();
                }
            });
        }
        buttons.add(VisitChargeCRUDWindow.COMPLETED_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onComplete();
            }
        });
        buttons.add(VisitChargeCRUDWindow.IN_PROGRESS_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onInProgress();
            }
        });
        buttons.add(VisitChargeCRUDWindow.INVOICE_ORDERS_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                invoiceOrders();
            }
        });
        editor.setButtons(buttons);
    }

    /**
     * Invoked when the 'new' button is pressed. This creates a new invoice if the current invoice is posted.
     */
    protected void onNew() {
        editor.getCharge().create();
        onInvoiceSelected(); // need to remove the New button and add the Apply button
    }

    /**
     * Marks the invoice COMPLETED and closes the dialog if the operation is successful.
     */
    private void onComplete() {
        if (editor.saveAsCompleted()) {
            printNew(true);
        }
    }

    /**
     * Marks the invoice IN_PROGRESS, and closes the dialog if the operation is successful.
     */
    private void onInProgress() {
        if (editor.saveAsInProgress()) {
            printNew(true);
        }
    }

    /**
     * Invoices pending orders.
     */
    private void invoiceOrders() {
        editor.getCharge().chargeOrders();
    }

    /**
     * Prints any new documents set for immediate printing.
     *
     * @param close if {@code true}, close the dialog
     */
    private void printNew(boolean close) {
        VisitChargeEditor editor = this.editor.getChargeEditor();
        if (editor != null) {
            ActionListener printListener = null;
            if (close) {
                printListener = new ActionListener() {
                    @Override
                    public void onAction(ActionEvent event) {
                        VisitEditorDialog.super.onOK();
                    }
                };
            }
            if (!editor.getUnprintedDocuments().printNew(printListener)) {
                if (close) {
                    // nothing to print, so close now
                    super.onOK();
                }
            }
        }
    }

    /**
     * Invoked when the reminders/alert tab is selected.
     * <p/>
     * Updates the dialog buttons
     */
    private void onRemindersSelected() {
        editor.setButtons(setDefaultButtons(false));
    }

    /**
     * Invoked when the documents tab is selected.
     * <p/>
     * Updates the dialog buttons
     */
    private void onDocumentsSelected() {
        editor.setButtons(setDefaultButtons(false));
    }

    /**
     * Invoked when the prescription tab is selected.
     * <p/>
     * Updates the dialog buttons.
     */
    private void onPrescriptionSelected() {
        ButtonSet buttons = setDefaultButtons(false);
        editor.setButtons(buttons);
    }

    /**
     * Invoked when the estimates tab is selected.
     * <p/>
     * Updates the dialog buttons.
     */
    private void onEstimatesSelected() {
        ButtonSet buttons = setDefaultButtons(false);
        editor.setButtons(buttons);
    }

    /**
     * Sets the dialog buttons to that of the patient history summary.
     */
    private void setHistoryButtons() {
        ButtonSet buttons = setDefaultButtons(true);
        editor.setButtons(buttons);
    }

    /**
     * Displays the macros, if the invoice editor is displayed.
     */
    private void onMacro() {
        if (editor.getSelectedTab() == VisitEditor.INVOICE_TAB && editor.getChargeEditor() != null) {
            MacroDialog dialog = new MacroDialog(editor.getContext(), getHelpContext());
            dialog.show();
        }
    }
}
