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

package org.openvpms.web.component.im.patient;

import nextapp.echo2.app.Component;
import org.openvpms.component.model.party.Party;
import org.openvpms.web.component.im.layout.LayoutContext;

/**
 * Factory for patient summaries.
 *
 * @author Tim Anderson
 */
public interface PatientSummaryFactory {

    /**
     * Returns a summary for a patient.
     *
     * @param patient the  patient
     * @param context the layout context
     * @return a new patient summary
     */
    Component getSummary(Party patient, LayoutContext context);
}
