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
 *  Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.component.workspace;


import org.openvpms.web.component.app.Context;

/**
 * Factory for {@link Workspaces}.
 *
 * @author Tim Anderson
 */
public interface WorkspacesFactory {

    /**
     * Creates the customer workspaces.
     *
     * @param context the context
     * @return the customer workspaces
     */
    Workspaces createCustomerWorkspaces(Context context);

    /**
     * Creates the patient workspaces.
     *
     * @param context the context
     * @return the patient workspaces
     */
    Workspaces createPatientWorkspaces(Context context);

    /**
     * Creates the supplier workspaces.
     *
     * @param context the context
     * @return the supplier workspaces
     */
    Workspaces createSupplierWorkspaces(Context context);

    /**
     * Creates the workflow workspaces.
     *
     * @param context the context
     * @return the workflow workspaces
     */
    Workspaces createWorkflowWorkspaces(Context context);

    /**
     * Creates the product workspaces.
     *
     * @param context the context
     * @return the product workspaces
     */
    Workspaces createProductWorkspaces(Context context);

    /**
     * Creates the reporting workspaces.
     *
     * @param context the context
     * @return the reporting workspaces
     */
    Workspaces createReportingWorkspaces(Context context);

    /**
     * Creates the administration workspaces.
     *
     * @param context the context
     * @return the administration workspaces
     */
    Workspaces createAdminWorkspaces(Context context);

}