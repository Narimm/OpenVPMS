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

package org.openvpms.web.workspace.patient.summary;

import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.patient.CustomerPatientSummary;


/**
 * Factory for customer and patient summary information.
 *
 * @author Tim Anderson
 */
public interface CustomerPatientSummaryFactory {

    /**
     * Creates a component to summarise customer and patient details.
     *
     * @param context the context
     * @param help    the help context
     * @return the summary
     */
    CustomerPatientSummary createCustomerPatientSummary(Context context, HelpContext help);

    /**
     * Creates a component to summarise patient details.
     *
     * @param context the context
     * @param help    the help context
     * @return the summary
     */
    PatientSummary createPatientSummary(Context context, HelpContext help);

}