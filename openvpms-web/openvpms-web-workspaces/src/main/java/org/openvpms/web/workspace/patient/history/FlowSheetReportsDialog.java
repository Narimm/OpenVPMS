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

package org.openvpms.web.workspace.patient.history;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.client.HospitalizationService;
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
     * Medical records report label.
     */
    private static final String MEDICAL = "patient.record.flowsheet.import.medical";

    /**
     * Billing report label.
     */
    private static final String BILLING = "patient.record.flowsheet.import.billing";

    /**
     * Notes report label.
     */
    private static final String NOTES = "patient.record.flowsheet.import.notes";

    /**
     * Flow sheet report label.
     */
    private static final String FLOW_SHEET = "patient.record.flowsheet.import.flowsheet";

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
        medicalRecords = CheckBoxFactory.create(MEDICAL, true, listener);
        billing = CheckBoxFactory.create(BILLING, true, listener);
        notes = CheckBoxFactory.create(NOTES, true, listener);
        flowSheet = CheckBoxFactory.create(FLOW_SHEET, true, listener);
    }

    /**
     * Invoked when the 'OK' button is pressed. This sets the action and closes
     * the window.
     */
    @Override
    protected void onOK() {
        FlowSheetServiceFactory factory = ServiceHelper.getBean(FlowSheetServiceFactory.class);
        HospitalizationService service = factory.getHospitalisationService(context.getLocation());
        if (medicalRecords.isSelected()) {
            service.saveMedicalRecords(getName(MEDICAL), context);
        }
        if (billing.isSelected()) {
            service.saveBillingReport(getName(BILLING), context);
        }
        if (notes.isSelected()) {
            service.saveNotesReport(getName(NOTES), context);
        }
        if (flowSheet.isSelected()) {
            service.saveFlowSheetReport(getName(FLOW_SHEET), context);
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
     * Formats a report name.
     *
     * @param key the resource bundle key for the report name
     * @return the report name
     */
    private String getName(String key) {
        return Messages.format("patient.record.flowsheet.import.name", Messages.get(key));
    }

}
