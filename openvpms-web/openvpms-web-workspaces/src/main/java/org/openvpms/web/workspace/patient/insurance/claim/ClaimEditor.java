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

package org.openvpms.web.workspace.patient.insurance.claim;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;

/**
 * Editor for <em>act.patientInsuranceClaim</em>.
 *
 * @author Tim Anderson
 */
public class ClaimEditor extends AbstractActEditor {

    /**
     * Constructs an {@link ClaimEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public ClaimEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
        if (act.isNew()) {
            initParticipant("patient", context.getContext().getPatient());
            initParticipant("location", context.getContext().getLocation());
            initParticipant("clinician", context.getContext().getClinician());
        }
    }

    /**
     * Creates a new instance of the editor, with the latest instance of the object to edit.
     *
     * @return {@code null}
     */
    @Override
    public IMObjectEditor newInstance() {
        return new ClaimEditor(reload(getObject()), getParent(), getLayoutContext());
    }

}
