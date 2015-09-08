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
 * <p>
 * Imports Smart Flow Sheet reports associated with a visit, and links them to the visit.
 *
 * @author Tim Anderson
 */
class FlowSheetReportsDialog extends PopupDialog {

    /**
     * The patient context.
     */
    private final PatientContext context;

    /**
     * Determines if the medical records report is imported.
     */
    private final CheckBox medicalRecords;

    /**
     * Determines if the inventory report is imported.
     */
    private final CheckBox inventory;

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
        super(Messages.get("patient.record.flowsheet.import.title"), "MessageDialog", OK_CANCEL);
        this.context = context;
        ActionListener listener = new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                boolean enable = medicalRecords.isSelected() || inventory.isSelected() || flowSheet.isSelected();
                getButtons().setEnabled(OK_ID, enable);
            }
        };
        medicalRecords = CheckBoxFactory.create("patient.record.flowsheet.import.medical", true, listener);
        inventory = CheckBoxFactory.create("patient.record.flowsheet.import.inventory", true, listener);
        flowSheet = CheckBoxFactory.create("patient.record.flowsheet.import.flowsheet", true, listener);
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
            service.saveMedicalRecords("Smart Flow Sheet Medical Records", context);
        }
        if (inventory.isSelected()) {
            service.saveInventoryReport("Smart Flow Sheet Inventory", context);
        }
        if (flowSheet.isSelected()) {
            service.saveFlowSheetReport("Smart Flow Sheet", context);
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
        Column column = ColumnFactory.create(Styles.WIDE_CELL_SPACING, label, medicalRecords, inventory, flowSheet);
        getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, column));
    }
}
