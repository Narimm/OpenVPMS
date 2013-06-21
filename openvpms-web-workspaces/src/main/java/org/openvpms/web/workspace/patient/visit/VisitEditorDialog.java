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

package org.openvpms.web.workspace.patient.visit;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.customer.charge.CustomerChargeDocuments;
import org.openvpms.web.workspace.patient.charge.VisitChargeEditor;

import java.util.List;

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
     * Constructs a {@code VisitEditorDialog}.
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
        if (editor.getChargeEditor() != null) {
            CustomerChargeDocuments docs = new CustomerChargeDocuments(editor.getChargeEditor(), getHelpContext());
            List<Act> existing = docs.getUnprinted();
            if (editor.save()) {
                docs.printNew(existing, null);
            }
        }
    }

    /**
     * Invoked when the 'OK' button is pressed. This saves the editor, prints unprinted documents, and closes the
     * window.
     */
    @Override
    protected void onOK() {
        if (editor.getChargeEditor() != null) {
            CustomerChargeDocuments docs = new CustomerChargeDocuments(editor.getChargeEditor(), getHelpContext());
            List<Act> existing = docs.getUnprinted();
            if (editor.save()) {
                printNew(docs, existing);
            }
        } else {
            super.onOK();
        }
    }

    /**
     * Invoked when a tab is selected.
     *
     * @param index the tab index.
     */
    protected void onSelected(int index) {
        switch (index) {
            case VisitEditor.HISTORY_INDEX:
                onHistorySelected();
                break;
            case VisitEditor.INVOICE_INDEX:
                onInvoiceSelected();
                break;
            case VisitEditor.REMINDERS_INDEX:
                onRemindersSelected();
                break;
            case VisitEditor.DOCUMENT_INDEX:
                onDocumentsSelected();
                break;
        }
    }

    /**
     * Sets the dialog buttons to the default Apply, OK and Cancel buttons.
     *
     * @return the buttons
     */
    protected ButtonSet setDefaultButtons() {
        ButtonSet buttons = getButtons();
        buttons.removeAll();
        addButton(OK_ID);
        addButton(CANCEL_ID);
        return buttons;
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
        CustomerChargeDocuments docs = new CustomerChargeDocuments(editor.getChargeEditor(), getHelpContext());
        List<Act> existing = docs.getUnprinted();
        if (editor.saveAsCompleted()) {
            printNew(docs, existing);
        }
    }

    /**
     * Marks the invoice IN_PROGRESS, and closes the dialog if the operation is successful.
     */
    private void onInProgress() {
        CustomerChargeDocuments docs = new CustomerChargeDocuments(editor.getChargeEditor(), getHelpContext());
        List<Act> existing = docs.getUnprinted();
        if (editor.saveAsInProgress()) {
            printNew(docs, existing);
        }
    }

    /**
     * Prints any new documents set for immediate printing.
     *
     * @param docs     the documents
     * @param existing the existing documents
     */
    private void printNew(CustomerChargeDocuments docs, List<Act> existing) {
        ActionListener printListener = new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                VisitEditorDialog.super.onOK();
            }
        };
        if (!docs.printNew(existing, printListener)) {
            // nothing to print, so close now
            super.onOK();
        }
    }

    /**
     * Invoked when the reminders/alert tab is selected.
     * <p/>
     * Updates the dialog buttons
     */
    private void onRemindersSelected() {
        editor.setButtons(setDefaultButtons());
    }

    /**
     * Invoked when the documents tab is selected.
     * <p/>
     * Updates the dialog buttons
     */
    private void onDocumentsSelected() {
        editor.setButtons(setDefaultButtons());
    }

    /**
     * Sets the dialog buttons to that of the patient history summary.
     */
    private void setHistoryButtons() {
        ButtonSet buttons = getButtons();
        buttons.removeAll();
        addButton(OK_ID);
        addButton(CANCEL_ID);
        editor.setButtons(buttons);
    }

}
