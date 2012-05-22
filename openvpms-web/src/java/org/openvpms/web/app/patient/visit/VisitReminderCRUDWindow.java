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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.patient.visit;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.patient.mr.ReminderCRUDWindow;

/**
 * Visit reminder/alert CRUD window.
 *
 * @author Tim Anderson
 */
public class VisitReminderCRUDWindow extends ReminderCRUDWindow{

    /**
     * Constructs a {@code VisitReminderCRUDWindow}.
     *
     * @param patient the patient
     */
    public VisitReminderCRUDWindow(Party patient) {
        super(patient);
    }

    /**
     * Lays out the component.
     */
    @Override
    protected Component doLayout() {
        return getContainer();
    }
}
