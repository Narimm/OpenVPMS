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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.summary;

import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.component.model.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextSwitchListener;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.patient.PatientSummaryFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.customer.CustomerSummary;
import org.openvpms.web.workspace.patient.CustomerPatientSummary;


/**
 * Default implementation of the {@link CustomerPatientSummaryFactory} interface .
 *
 * @author Tim Anderson
 */
public class DefaultCustomerPatientSummaryFactory implements CustomerPatientSummaryFactory, PatientSummaryFactory {

    /**
     * Creates a customer/patient summary.
     *
     * @param context     the context
     * @param help        the help context
     * @param preferences user preferences
     * @return the summary
     */
    @Override
    public CustomerPatientSummary createCustomerPatientSummary(Context context, HelpContext help,
                                                               Preferences preferences) {
        return new CustomerPatientSummary(context, help, preferences);
    }

    /**
     * Creates a component to summarise customer details.
     *
     * @param context     the context
     * @param help        the help context
     * @param preferences user preferences
     * @return the summary
     */
    @Override
    public CustomerSummary createCustomerSummary(Context context, HelpContext help, Preferences preferences) {
        return new CustomerSummary(context, help, preferences);
    }

    /**
     * Creates a component to summarise patient details.
     *
     * @param context     the context
     * @param help        the help context
     * @param preferences user preferences
     * @param listener    the context switch listener, or {@code null} to disable context switching
     * @return the summary
     */
    @Override
    public PatientSummary createPatientSummary(Context context, HelpContext help, Preferences preferences,
                                               ContextSwitchListener listener) {
        return new PatientSummary(context, help, preferences, listener);
    }

    /**
     * Returns a summary for a patient.
     *
     * @param patient the  patient
     * @param context the layout context
     * @return a new patient summary
     */
    @Override
    public Component getSummary(Party patient, LayoutContext context) {
        PatientSummary patientSummary = createPatientSummary(context.getContext(), context.getHelpContext(),
                                                             context.getPreferences(),
                                                             context.getContextSwitchListener());
        return patientSummary.getSummary((org.openvpms.component.business.domain.im.party.Party) patient);
    }

}
