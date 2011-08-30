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
 *
 *  $Id$
 */

package org.openvpms.web.app.patient;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.app.customer.CustomerSummary;
import org.openvpms.web.app.patient.summary.PatientSummary;
import org.openvpms.web.component.util.ColumnFactory;


/**
 * Renders customer and patient summary information.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 * @see CustomerSummary
 * @see PatientSummary
 */
public class CustomerPatientSummary {

    /**
     * Returns a component displaying customer and patient summary details.
     * <p/>
     * If the patient has an owner, the returned component will display the
     * customer summary above the patient summary. If the patient has no owner,
     * ony the patient summary will be returned.
     *
     * @param patient the patient. May be <tt>null</tt>
     * @return the component, or <tt>null</tt> if the patient is <tt>null</tt>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static Component getSummary(Party patient) {
        Component result = null;
        if (patient != null) {
            PatientRules rules = new PatientRules();
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
     * @param act the act. May be <tt>null</tt>
     * @return a summary component, or <tt>null</codett> if there is no summary
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static Component getSummary(Act act) {
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
     * @return the summary, or <tt>null</tt> if the customer and patient are
     *         both <tt>null</tt>
     * @throws ArchetypeServiceException for any archetype service error
     */
    private static Component getSummary(Party customer, Party patient) {
        Component result = null;
        Component customerSummary = (customer != null) ?
                CustomerSummary.getSummary(customer) : null;
        Component patientSummary = (patient != null) ?
                new PatientSummary().getSummary(patient) : null;
        if (customerSummary != null || patientSummary != null) {
            Column column = ColumnFactory.create();
            if (customerSummary != null) {
                column.add(customerSummary);
            }
            if (patientSummary != null) {
                column.add(patientSummary);
            }
            result = column;
        }
        return result;
    }
}
