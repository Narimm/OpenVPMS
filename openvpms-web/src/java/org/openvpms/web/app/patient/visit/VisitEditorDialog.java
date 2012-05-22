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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.patient.visit;

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.event.ActionListener;

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
     */
    public VisitEditorDialog(String title, VisitEditor visitEditor) {
        super(title, "BrowserDialog", APPLY_OK_CANCEL);
        this.editor = visitEditor;
        setModal(true);
        getLayout().add(visitEditor.getComponent());

        visitEditor.setListener(new VisitEditorListener() {
            public void summarySelected() {
                onSummarySelected();
            }

            public void invoiceSelected() {
                onInvoiceSelected();
            }

            public void remindersSelected() {
                onRemindersSelected();
            }

            public void documentsSelected() {
                onDocumentsSelected();
            }
        });
        setSummaryButtons();
    }

    /**
     * Invoked when the 'apply' button is pressed. This saves the editor.
     */
    @Override
    protected void onApply() {
        editor.save();
    }

    /**
     * Invoked when the 'OK' button is pressed. This saves the editor and closes the window.
     */
    @Override
    protected void onOK() {
        if (editor.save()) {
            super.onOK();
        }
    }

    /**
     * Invoked when the summary tab is selected.
     * <p/>
     * Updates the dialog buttons
     */
    private void onSummarySelected() {
        setSummaryButtons();
    }

    /**
     * Invoked when the invoice tab is selected.
     * <p/>
     * Updates the dialog buttons
     */
    private void onInvoiceSelected() {
        ButtonSet buttons = getButtons();
        buttons.removeAll();
        addButton(APPLY_ID);
        addButton(OK_ID);
        addButton(CANCEL_ID);
        buttons.add(VisitChargeCRUDWindow.POSTED_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onPost();
            }
        });
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
     * Marks the invoice POSTED closes the dialog if the operation is successful.
     */
    private void onPost() {
        if (editor.getInvoice().post()) {
            onOK();
        }
    }

    /**
     * Marks the invoice COMPLETED and closes the dialog if the operation is successful.
     */
    private void onComplete() {
        if (editor.getInvoice().complete()) {
            onOK();
        }
    }

    /**
     * Marks the invoice IN_PROGRESS, and closes the dialog if the operation is successful.
     */
    private void onInProgress() {
        if (editor.getInvoice().inProgress()) {
            onOK();
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
     * Sets the dialog buttons to that of the patient medical record summary.
     */
    private void setSummaryButtons() {
        ButtonSet buttons = getButtons();
        buttons.removeAll();
        addButton(OK_ID);
        addButton(CANCEL_ID);
        editor.setButtons(buttons);
    }

    /**
     * Sets the dialog buttons to the default Apply, OK and Cancel buttons.
     *
     * @return the buttons
     */
    private ButtonSet setDefaultButtons() {
        ButtonSet buttons = getButtons();
        buttons.removeAll();
        addButton(OK_ID);
        addButton(CANCEL_ID);
        return buttons;
    }

}
