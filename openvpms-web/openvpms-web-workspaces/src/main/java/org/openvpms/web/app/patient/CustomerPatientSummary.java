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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.patient;

import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.app.customer.CustomerSummary;
import org.openvpms.web.app.patient.summary.PatientSummary;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;


/**
 * Renders customer and patient summary information.
 *
 * @author Tim Anderson
 * @see org.openvpms.web.app.customer.CustomerSummary
 * @see PatientSummary
 */
public class CustomerPatientSummary {

    /**
     * The patient rules.
     */
    private final PatientRules rules;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The help context.
     */
    private final HelpContext help;


    /**
     * Constructs a {@code CustomerPatientSummary}.
     *
     * @param context the context
     * @param help    the help context
     */
    public CustomerPatientSummary(Context context, HelpContext help) {
        rules = new PatientRules(ServiceHelper.getArchetypeService(), ServiceHelper.getLookupService());
        this.context = context;
        this.help = help;
    }

    /**
     * Returns a component displaying customer and patient summary details.
     * <p/>
     * If the patient has an owner, the returned component will display the
     * customer summary above the patient summary. If the patient has no owner,
     * ony the patient summary will be returned.
     *
     * @param patient the patient. May be {@code null}
     * @return the component, or {@code null} if the patient is {@code null}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Component getSummary(Party patient) {
        Component result = null;
        if (patient != null) {
            Party customer = rules.getOwner(patient);
            result = getSummary(customer, patient);
        }
        return result;
    }

    /**
     * Returns summary information from the customer and patient participations
     * in an act.
     * <p/>
     * If the act has both customer and patient, the returned component will
     * display the customer summary above the patient summary.
     *
     * @param act the act. May be {@code null}
     * @return a summary component, or {@code null</codett> if there is no summary
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Component getSummary(Act act) {
        Component result = null;
        if (act != null) {
            Party customer;
            Party patient;
            ActBean bean = new ActBean(act);
            customer = (Party) bean.getParticipant("participation.customer");
            patient = (Party) bean.getParticipant("participation.patient");
            result = getSummary(customer, patient);
        }
        return result;
    }

    /**
     * Returns a component displaying customer and patient summary details.
     *
     * @param customer the customer
     * @param patient  the patient
     * @return the summary, or {@code null} if the customer and patient are both {@code null}
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Component getSummary(Party customer, Party patient) {
        Component result = null;
        Component customerSummary = null;
        if (customer != null) {
            Context local = new LocalContext(context);
            local.setCustomer(customer);
            local.setPatient(patient);
            customerSummary = new CustomerSummary(local, help).getSummary(customer);
        }
        Component patientSummary = (patient != null) ? new PatientSummary(context, help).getSummary(patient) : null;
        if (customerSummary != null || patientSummary != null) {
            result = ColumnFactory.create("CellSpacing");
            if (customerSummary != null) {
                result.add(customerSummary);
            }
            if (patientSummary != null) {
                result.add(patientSummary);
            }
        }
        return result;
    }
}
