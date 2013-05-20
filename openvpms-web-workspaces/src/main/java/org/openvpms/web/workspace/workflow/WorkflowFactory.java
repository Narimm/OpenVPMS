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

package org.openvpms.web.workspace.workflow;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.workflow.Workflow;
import org.openvpms.web.echo.help.HelpContext;


/**
 * Workflow factory.
 *
 * @author Tim Anderson
 */
public interface WorkflowFactory {

    /**
     * Creates a check-in workflow.
     *
     * @param customer  the customer
     * @param patient   the patient
     * @param clinician the user. May be {@code null}
     * @param context   the external context to access and update
     * @param help      the help context
     */
    Workflow createCheckInWorkflow(Party customer, Party patient, User clinician, Context context, HelpContext help);

    /**
     * Creates a check-in workflow from an appointment.
     *
     * @param appointment the appointment
     * @param context     the external context to access and update
     * @param help        the help context
     */
    Workflow createCheckInWorkflow(Act appointment, Context context, HelpContext help);

    /**
     * Creates a consult workflow from an <em>act.customerAppointment</em> or <em>act.customerTask</em>.
     *
     * @param act     the act
     * @param context the external context to access and update
     * @param help    the help context
     */
    Workflow createConsultWorkflow(Act act, Context context, HelpContext help);

    /**
     * Creates a check-out workflow from an <em>act.customerAppointment</em> or <em>act.customerTask</em>.
     *
     * @param act     the act
     * @param context the external context to access and update
     * @param help    the help context
     */
    Workflow createCheckOutWorkflow(Act act, Context context, HelpContext help);
}