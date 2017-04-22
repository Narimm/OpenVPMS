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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.history;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.client.HospitalizationService;
import org.openvpms.smartflow.i18n.FlowSheetMessages;
import org.openvpms.web.echo.button.CheckBox;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

/**
 * Flow Sheets Reports Import dialog.
 * <p/>
 * Imports Smart Flow Sheet reports associated with a visit, and links them to the visit.
 *
 * @author Tim Anderson
 */
public class FlowSheetReportsDialog extends PopupDialog {

    /**
     * The patient context.
     */
    private final PatientContext context;

    /**
     * Determines if the medical records report is imported.
     */
    private final CheckBox medicalRecords;

    /**
     * Determines if the billing report is imported.
     */
    private final CheckBox billing;

    /**
     * Determines if the notes report is imported.
     */
    private final CheckBox notes;

    /**
     * Determines if the flow sheet report is imported.
     */
    private final CheckBox flowSheet;

    /**
     * Constructs a {@link FlowSheetReportsDialog}.
     *
     * @param context the patient context
     */
    public FlowSheetReportsDialog(PatientContext context) {
        this(context, false);
    }

    /**
     * Constructs a {@link FlowSheetReportsDialog}.
     *
     * @param context the patient context
     * @param skip    if {@code true}, display a skip button, otherwise display a cancel button
     */
    public FlowSheetReportsDialog(PatientContext context, boolean skip) {
        super(Messages.get("patient.record.flowsheet.import.title"), "MessageDialog", (skip) ? OK_SKIP : OK_CANCEL);
        this.context = context;
        ActionListener listener = new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                boolean enable = medicalRecords.isSelected() || billing.isSelected() || notes.isSelected()
                                 || flowSheet.isSelected();
                getButtons().setEnabled(OK_ID, enable);
            }
        };
        medicalRecords = createCheckBox(FlowSheetMessages.medicalRecordsReportName(), listener);
        billing = createCheckBox(FlowSheetMessages.billingReportName(), listener);
        notes = createCheckBox(FlowSheetMessages.notesReportName(), listener);
        flowSheet = createCheckBox(FlowSheetMessages.flowSheetReportName(), listener);
    }

    /**
     * Invoked when the 'OK' button is pressed. This sets the action and closes
     * the window.
     */
    @Override
    protected void onOK() {
        FlowSheetServiceFactory factory = ServiceHelper.getBean(FlowSheetServiceFactory.class);
        HospitalizationService service = factory.getHospitalizationService(context.getLocation());
        if (medicalRecords.isSelected()) {
            service.saveMedicalRecords(context);
        }
        if (billing.isSelected()) {
            service.saveBillingReport(context);
        }
        if (notes.isSelected()) {
            service.saveNotesReport(context);
        }
        if (flowSheet.isSelected()) {
            service.saveFlowSheetReport(context);
        }
        super.onOK();
    }

    /**
     * Lays out the component prior to display.
     * This implementation is a no-op.
     * +
     */
    @Override
    protected void doLayout() {
        Label label = LabelFactory.create("patient.record.flowsheet.import.message", Styles.BOLD);
        Column column = ColumnFactory.create(Styles.WIDE_CELL_SPACING, label, medicalRecords, billing, notes,
                                             flowSheet);
        getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, column));
    }

    /**
     * Creates a new check box, initially selected.
     *
     * @param text     the text
     * @param listener the listener
     * @return a new check box
     */
    private CheckBox createCheckBox(String text, ActionListener listener) {
        CheckBox checkBox = CheckBoxFactory.create(true);
        checkBox.setText(text);
        checkBox.addActionListener(listener);
        return checkBox;
    }

}
