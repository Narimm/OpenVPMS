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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.patient;

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * An editor for acts with a patient.
 *
 * @author Tim Anderson
 */
public class PatientActEditor extends AbstractActEditor {

    /**
     * Constructs a {@link PatientActEditor}.
     * <p/>
     * If a parent act is specified, the following applies:
     * <ul>
     * <li>if {@code act} is new, its start time defaults to that of the parent</li>
     * <li>if the parent has a <em>patient</em> node, its value will used to set the patient</li>
     * </ul>
     * <li>If the parent has no <em>patient</em> node or there is no parent, the patient will be set from the context.
     *
     * @param act     the act to edit
     * @param parent  the parent act. May be {@code null}
     * @param context the layout context
     */
    public PatientActEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
        if (parent != null) {
            if (act.isNew()) {
                // default the act start time to that of the parent
                act.setActivityStartTime(parent.getActivityStartTime());
            }

            ActBean bean = new ActBean(parent);
            if (bean.hasNode("patient")) {
                setPatient(bean.getParticipantRef(PatientArchetypes.PATIENT_PARTICIPATION));
            } else {
                initParticipant("patient", context.getContext().getPatient());
            }
        } else {
            initParticipant("patient", context.getContext().getPatient());
        }

        if (act.isNew()) {
            User clinician = context.getContext().getClinician();
            if (clinician != null) {
                initParticipant("clinician", clinician);
            }
        }
    }

    /**
     * Sets the patient.
     *
     * @param patient the patient. May be {@code null}
     */
    public void setPatient(Party patient) {
        setPatient(patient != null ? patient.getObjectReference() : null);
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
     * Sets the patient.
     *
     * @param patient the patient reference. May be {@code null}
     */
    public void setPatient(IMObjectReference patient) {
        setParticipant("patient", patient);
    }

    /**
     * Returns the patient reference.
     *
     * @return the patient reference. May be {@code null}
     */
    public IMObjectReference getPatientRef() {
        return getParticipantRef("patient");
    }

    /**
     * Sets the clinician.
     *
     * @param clinician the clinician reference. May be {@code null}.
     */
    public void setClinician(IMObjectReference clinician) {
        setParticipant("clinician", clinician);
    }
}
