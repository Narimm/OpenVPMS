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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment;

import nextapp.echo2.app.Component;
import org.openvpms.component.model.party.Party;
import org.openvpms.web.component.alert.Alert;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.alert.AlertSummary;

import java.util.List;

/**
 * An alert summary for displaying in appointments.
 *
 * @author Tim Anderson
 */
public class AppointmentAlertSummary extends AlertSummary {

    /**
     * Constructs an {@link AppointmentAlertSummary}.
     *
     * @param party   the party the alerts are for
     * @param alerts  the alerts
     * @param key     resource bundle key
     * @param context the context
     * @param help    the help context
     */
    public AppointmentAlertSummary(Party party, List<Alert> alerts, String key, Context context, HelpContext help) {
        super(party, alerts, key, context, help);
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    @Override
    public Component getComponent() {
        Component component = super.getComponent();
        component.setStyleName("AppointmentActEditor.Alerts");
        return component;
    }

}
