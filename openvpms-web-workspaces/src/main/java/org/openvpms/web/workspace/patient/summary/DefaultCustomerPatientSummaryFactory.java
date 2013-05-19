package org.openvpms.web.workspace.patient.summary;

import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.patient.CustomerPatientSummary;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class DefaultCustomerPatientSummaryFactory implements CustomerPatientSummaryFactory {

    /**
     * Creates a customer/patient summary.
     *
     * @param context the context
     * @param help    the help context
     * @return the summary
     */
    @Override
    public CustomerPatientSummary createCustomerPatientSummary(Context context, HelpContext help) {
        return new CustomerPatientSummary(context, help);
    }

    /**
     * Creates a component to summarise patient details.
     *
     * @param context the context
     * @param help    the help context
     * @return the summary
     */
    @Override
    public PatientSummary createPatientSummary(Context context, HelpContext help) {
        return new PatientSummary(context, help);
    }
}
