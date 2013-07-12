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

package org.openvpms.web.workspace.patient.visit;

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.patient.charge.VisitChargeEditor;
import org.openvpms.web.workspace.patient.mr.PatientPrescriptionQuery;


/**
 * Links a patient reminder/alerts browser to a CRUD window.
 *
 * @author Tim Anderson
 */
public class PrescriptionBrowserCRUDWindow extends BrowserCRUDWindow<Act> {

    /**
     * Constructs a {@link PrescriptionBrowserCRUDWindow}.
     *
     * @param patient the patient
     * @param context the context
     * @param help    the help context
     */
    public PrescriptionBrowserCRUDWindow(Party patient, Context context, HelpContext help) {
        Archetypes<Act> archetypes = Archetypes.create(PatientArchetypes.PRESCRIPTION, Act.class);

        Browser<Act> browser = BrowserFactory.create(new PatientPrescriptionQuery(patient),
                                                     new DefaultLayoutContext(context, help));
        setBrowser(browser);

        setWindow(new VisitPrescriptionCRUDWindow(archetypes, context, help));
    }

    /**
     * Registers the charge editor.
     *
     * @param chargeEditor the charge editor. May be {@code null}
     */
    public void setChargeEditor(VisitChargeEditor chargeEditor) {
        ((VisitPrescriptionCRUDWindow) getWindow()).setChargeEditor(chargeEditor);
    }
}
