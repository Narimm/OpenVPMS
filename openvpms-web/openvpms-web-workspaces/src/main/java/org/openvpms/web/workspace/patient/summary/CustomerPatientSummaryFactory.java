package org.openvpms.web.workspace.patient.summary;

import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.patient.CustomerPatientSummary;

/**
 * Enter description.
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