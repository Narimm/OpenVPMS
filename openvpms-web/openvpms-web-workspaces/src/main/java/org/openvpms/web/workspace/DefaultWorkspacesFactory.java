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

package org.openvpms.web.workspace;

import org.openvpms.archetype.rules.prefs.Preferences;
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
     * @param context     the context
     * @param preferences the user preferences
     * @return the customer workspaces
     */
    @Override
    public Workspaces createCustomerWorkspaces(Context context, Preferences preferences) {
        return new CustomerWorkspaces(context, preferences);
    }

    /**
     * Creates the patient workspaces.
     *
     * @param context     the context
     * @param preferences the user preferences
     * @return the patient workspaces
     */
    @Override
    public Workspaces createPatientWorkspaces(Context context, Preferences preferences) {
        return new PatientWorkspaces(context, preferences);
    }

    /**
     * Creates the supplier workspaces.
     *
     * @param context the context
     * @return the supplier workspaces
     */
    @Override
    public Workspaces createSupplierWorkspaces(Context context) {
        return new SupplierWorkspaces(context);
    }

    /**
     * Creates the workflow workspaces.
     *
     * @param context     the context
     * @param preferences user preferences
     * @return the workflow workspaces
     */
    @Override
    public Workspaces createWorkflowWorkspaces(Context context, Preferences preferences) {
        return new WorkflowWorkspaces(context, preferences);
    }

    /**
     * Creates the product workspaces.
     *
     * @param context the context
     * @return the product workspaces
     */
    @Override
    public Workspaces createProductWorkspaces(Context context) {
        return new ProductWorkspaces(context);
    }

    /**
     * Creates the reporting workspaces.
     *
     * @param context     the context
     * @param preferences user preferences
     * @return the reporting workspaces
     */
    @Override
    public Workspaces createReportingWorkspaces(Context context, Preferences preferences) {
        return new ReportingWorkspaces(context, preferences);
    }

    /**
     * Creates the administration workspaces.
     *
     * @param context the context
     * @return the administration workspaces
     */
    @Override
    public Workspaces createAdminWorkspaces(Context context) {
        return new AdminWorkspaces(context);
    }
}
