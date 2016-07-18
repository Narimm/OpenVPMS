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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.mr;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.doc.DocumentActEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.workspace.patient.history.PatientHistoryActions;


/**
 * Editor for <em>act.patientDocument*</em> acts.
 *
 * @author Tim Anderson
 */
public class PatientDocumentActEditor extends DocumentActEditor {

    /**
     * Constructs a {@link PatientDocumentActEditor}.
     *
     * @param act     the act
     * @param parent  the parent
     * @param context the layout context
     */
    public PatientDocumentActEditor(DocumentAct act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
        boolean initPatient = false;
        if (parent != null && parent instanceof Act) {
            ActBean bean = new ActBean((Act) parent);
            if (bean.hasNode("patient")) {
                initParticipant("patient", bean.getParticipantRef(PatientArchetypes.PATIENT_PARTICIPATION));
                initPatient = true;
            }
        }
        if (!initPatient) {
            initParticipant("patient", context.getContext().getPatient());
        }

        ActRelationshipCollectionEditor versions = getVersionsEditor();
        if (versions != null && isLocked()) {
            // prevent additions and deletions if the record is locked. Additions may still occur by editing the
            // document; this will version the existing document
            versions.setCardinalityReadOnly(true);
        }
    }

    /**
     * Determines if the record is locked.
     *
     * @return {@code true} if the record is locked
     */
    public boolean isLocked() {
        String status = getStatus();
        return ActStatus.POSTED.equals(status) || ActStatus.CANCELLED.equals(status)
               || PatientHistoryActions.needsLock(getObject());
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new PatientDocumentActLayoutStrategy(getDocumentEditor(), getVersionsEditor(), isLocked());
    }

}
