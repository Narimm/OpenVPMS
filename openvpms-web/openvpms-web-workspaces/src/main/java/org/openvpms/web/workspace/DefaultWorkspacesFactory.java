package org.openvpms.web.workspace;

import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.workspace.Workspaces;
import org.openvpms.web.component.workspace.WorkspacesFactory;
import org.openvpms.web.workspace.admin.AdminWorkspaces;
import org.openvpms.web.workspace.customer.CustomerWorkspaces;
import org.openvpms.web.workspace.patient.PatientWorkspaces;
import org.openvpms.web.workspace.product.ProductWorkspaces;
import org.openvpms.web.workspace.reporting.ReportingWorkspaces;
import org.openvpms.web.workspace.supplier.SupplierWorkspaces;
import org.openvpms.web.workspace.workflow.WorkflowWorkspaces;


/**
 * Default implementation of the {@link WorkspacesFactory}.
 *
 * @author Tim Anderson
 */
public class DefaultWorkspacesFactory implements WorkspacesFactory {

    /**
     * Creates the customer workspaces.
     *
     * @param context the context
     * @return the customer workspaces
     */
    public Workspaces createCustomerWorkspaces(Context context) {
        return new CustomerWorkspaces(context);
    }

    /**
     * Creates the patient workspaces.
     *
     * @param context the context
     * @return the patient workspaces
     */
    public Workspaces createPatientWorkspaces(Context context) {
        return new PatientWorkspaces(context);
    }

    /**
     * Creates the supplier workspaces.
     *
     * @param context the context
     * @return the supplier workspaces
     */
    public Workspaces createSupplierWorkspaces(Context context) {
        return new SupplierWorkspaces(context);
    }

    /**
     * Creates the workflow workspaces.
     *
     * @param context the context
     * @return the workflow workspaces
     */
    public Workspaces createWorkflowWorkspaces(Context context) {
        return new WorkflowWorkspaces(context);
    }

    /**
     * Creates the product workspaces.
     *
     * @param context the context
     * @return the product workspaces
     */
    public Workspaces createProductWorkspaces(Context context) {
        return new ProductWorkspaces(context);
    }

    /**
     * Creates the reporting workspaces.
     *
     * @param context the context
     * @return the reporting workspaces
     */
    public Workspaces createReportingWorkspaces(Context context) {
        return new ReportingWorkspaces(context);
    }

    /**
     * Creates the administration workspaces.
     *
     * @param context the context
     * @return the administration workspaces
     */
    public Workspaces createAdminWorkspaces(Context context) {
        return new AdminWorkspaces(context);
    }
}
