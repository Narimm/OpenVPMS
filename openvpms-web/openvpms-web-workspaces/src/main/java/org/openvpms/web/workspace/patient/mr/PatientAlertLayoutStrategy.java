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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.mr;

import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.workspace.alert.AlertLayoutStrategy;


/**
 * Layout strategy for <em>act.patientAlert</em>.
 * This includes a field to display the associated alert type's priority and colour.
 *
 * @author Tim Anderson
 */
public class PatientAlertLayoutStrategy extends AlertLayoutStrategy {

    /**
     * The field to display the alert priority and colour.
     */
    private TextField priority;

    /**
     * Constructs a {@link PatientAlertLayoutStrategy}.
     */
    public PatientAlertLayoutStrategy() {
        super();
    }

    /**
     * Constructs a {@link PatientAlertLayoutStrategy}.
     *
     * @param priority the field to display the priority and colour
     */
    public PatientAlertLayoutStrategy(TextField priority) {
        super(priority);
    }

}
