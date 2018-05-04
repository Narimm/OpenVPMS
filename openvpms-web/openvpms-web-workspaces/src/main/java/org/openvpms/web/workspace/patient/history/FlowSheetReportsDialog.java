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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.history;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.smartflow.client.FlowSheetException;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.client.HospitalizationService;
import org.openvpms.smartflow.i18n.FlowSheetMessages;
import org.openvpms.smartflow.model.Anesthetic;
import org.openvpms.smartflow.model.Anesthetics;
import org.openvpms.smartflow.model.Form;
import org.openvpms.web.echo.button.CheckBox;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.error.ErrorHandler;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Flow Sheets Reports Import dialog.
 * <p>
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
     * The forms.
     */
    private final List<Form> forms;

    /**
     * The anesthetics.
     */
    private final List<Anesthetic> anesthetics;

    /**
     * Determines if the medical records report is imported.
     */
    private final CheckBox medicalRecordsCheckBox;

    /**
     * Determines if the billing report is imported.
     */
    private final CheckBox billingCheckBox;

    /**
     * Determines if the notes report is imported.
     */
    private final CheckBox notesCheckBox;

    /**
     * Determines if the flow sheet report is imported.
     */
    private final CheckBox flowSheetCheckBox;

    /**
     * Determines if the forms reports are imported.
     */
    private final CheckBox formsCheckBox;

    /**
     * Determines if the anesthetics report is imported.
     */
    private final CheckBox anestheticsCheckBox;

    /**
     * The hospitalization service.
     */
    private HospitalizationService service;

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
        setModal(true);
        this.context = context;
        FlowSheetServiceFactory factory = ServiceHelper.getBean(FlowSheetServiceFactory.class);
        service = factory.getHospitalizationService(context.getLocation());
        forms = getForms(context);
        anesthetics = getAnesthetics(context);
        ActionListener listener = new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                boolean enable = medicalRecordsCheckBox.isSelected() || billingCheckBox.isSelected()
                                 || notesCheckBox.isSelected() || flowSheetCheckBox.isSelected()
                                 || formsCheckBox.isSelected() || anestheticsCheckBox.isSelected();
                getButtons().setEnabled(OK_ID, enable);
            }
        };
        medicalRecordsCheckBox = createCheckBox(FlowSheetMessages.medicalRecordsReportName(), listener);
        billingCheckBox = createCheckBox(FlowSheetMessages.billingReportName(), listener);
        notesCheckBox = createCheckBox(FlowSheetMessages.notesReportName(), listener);
        flowSheetCheckBox = createCheckBox(FlowSheetMessages.flowSheetReportName(), listener);
        formsCheckBox = createCheckBox(Messages.get("patient.record.flowsheet.import.forms"), listener);
        if (forms.isEmpty()) {
            formsCheckBox.setSelected(false);
            formsCheckBox.setEnabled(false);
        }
        anestheticsCheckBox = createCheckBox(FlowSheetMessages.anaestheticReportName(), listener);
        if (anesthetics.isEmpty()) {
            anestheticsCheckBox.setSelected(false);
            anestheticsCheckBox.setEnabled(false);
        }
    }

    /**
     * Invoked when the 'OK' button is pressed. This sets the action and closes
     * the window.
     */
    @Override
    protected void onOK() {
        try {
            if (medicalRecordsCheckBox.isSelected()) {
                service.saveMedicalRecords(context);
            }
            if (billingCheckBox.isSelected()) {
                service.saveBillingReport(context);
            }
            if (notesCheckBox.isSelected()) {
                service.saveNotesReport(context);
            }
            if (flowSheetCheckBox.isSelected()) {
                service.saveFlowSheetReport(context);
            }
            if (formsCheckBox.isSelected()) {
                for (Form form : forms) {
                    service.saveFormReport(context, form);
                }
            }
            if (anestheticsCheckBox.isSelected()) {
                for (Anesthetic anesthetic : anesthetics) {
                    service.saveAnestheticReports(context, anesthetic);
                }
            }
            super.onOK();
        } catch (FlowSheetException exception) {
            ErrorHandler.getInstance().error(exception.getMessage(), exception);
        }
    }

    /**
     * Lays out the component prior to display.
     * This implementation is a no-op.
     */
    @Override
    protected void doLayout() {
        Label label = LabelFactory.create("patient.record.flowsheet.import.message", Styles.BOLD);
        Column column = ColumnFactory.create(Styles.WIDE_CELL_SPACING, label, medicalRecordsCheckBox, billingCheckBox,
                                             notesCheckBox, flowSheetCheckBox, formsCheckBox, anestheticsCheckBox);
        getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, column));
    }

    /**
     * Returns the anaesthetics for a patient.
     *
     * @param context the patient context
     * @return the anaesthetics
     * @throws FlowSheetException if the sheet cannot be retrieved
     */
    private List<Anesthetic> getAnesthetics(PatientContext context) {
        List<Anesthetic> result = Collections.emptyList();
        Anesthetics anesthetics = service.getAnesthetics(context.getPatient(), context.getVisit());
        if (anesthetics.getAnesthetics() != null) {
            result = anesthetics.getAnesthetics();
        }
        return result;
    }

    /**
     * Returns the forms for a patient that have PDF content.
     *
     * @param context the patient context
     * @return the forms
     */
    private List<Form> getForms(PatientContext context) {
        List<Form> result = new ArrayList<>();
        List<Form> forms = service.getForms(context.getPatient(), context.getVisit());
        for (Form form : forms) {
            if (!form.isDeleted() && form.isFinalized()) {
                result.add(form);
            }
        }
        return result;
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
