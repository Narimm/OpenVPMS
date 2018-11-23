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

package org.openvpms.web.workspace.patient.mr;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;

/**
 * Edit dialog for <em>act.patientClinicalNote</em>.
 *
 * @author Tim Anderson
 */
public class PatientVisitNoteEditDialog extends AbstractPatientClinicalNoteEditDialog {

    /**
     * Constructs a {@link PatientVisitNoteEditDialog}.
     *
     * @param editor  the editor
     * @param context the context
     */
    public PatientVisitNoteEditDialog(PatientVisitNoteEditor editor, Context context) {
        super(editor, context);
    }

    /**
     * Returns the patient.
     *
     * @return the patient
     */
    @Override
    protected Party getPatient() {
        return ((PatientVisitNoteEditor) getEditor()).getPatient();
    }

    /**
     * Returns the split pane layout style to use.
     *
     * @return te layout style name
     */
    @Override
    protected String getLayoutStyleName() {
        return "PatientVisitNote.layout";
    }

    /**
     * Returns the current event.
     *
     * @return the current event. May be {@code null}
     */
    @Override
    protected Act getCurrentEvent() {
        Act event = (Act) getEditor().getObject();
        return !event.isNew() ? event : null;
    }
}
