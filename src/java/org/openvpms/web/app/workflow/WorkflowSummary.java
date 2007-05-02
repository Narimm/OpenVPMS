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

package org.openvpms.web.app.workflow;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.app.customer.CustomerSummary;
import org.openvpms.web.app.patient.summary.PatientSummary;
import org.openvpms.web.component.util.ColumnFactory;


/**
 * Renders Workflow summary information.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class WorkflowSummary {

    /**
     * Returns summary information for a schedule or task.
     *
     * @param act the schedule/taskt. May be <code>null</code>
     * @return a summary component, or <code>null</code> if there is no summary
     */
    public static Component getSummary(Act act) {
        if (act != null) {
            Party customer;
            Party patient;
            ActBean bean = new ActBean(act);
            customer = (Party) bean.getParticipant("participation.customer");
            patient = (Party) bean.getParticipant("participation.patient");
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
                return column;
            }
        }
        return null;
    }
}
