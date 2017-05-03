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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Editor for <em>act.patientAlert</em> acts.
 *
 * @author Tim Anderson
 */
public class PatientAlertEditor extends AbstractActEditor {

    /**
     * Constructs a {@link PatientAlertEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent act. May be {@code null}
     * @param context the layout context
     */
    public PatientAlertEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
        initParticipant("patient", context.getContext().getPatient());
    }

    /**
     * Sets the patient.
     *
     * @param patient the patient. May be {@code null}
     */
    public void setPatient(Party patient) {
        setParticipant("patient", patient);
    }

    /**
     * Returns the patient.
     *
     * @return the patient. May be {@code null}
     */
    public Party getPatient() {
        return (Party) getParticipant("patient");
    }

    /**
     * Sets the alert type.
     *
     * @param alertType the alert type. May be {@code null}
     */
    public void setAlertType(Entity alertType) {
        setParticipant("alertType", alertType);
    }

    /**
     * Returns the alert type.
     *
     * @return the alert type
     */
    public Entity getAlertType() {
        return (Entity) getParticipant("alertType");
    }

}