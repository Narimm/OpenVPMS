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

package org.openvpms.web.workspace.patient.mr;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Editor for <em>act.patientClinicalEvent</em> acts.
 * <p/>
 * This disables editing of "items" nodes.
 *
 * @author Tim Anderson
 */
public class PatientClinicalEventActEditor extends AbstractPatientClinicalActEditor {

    /**
     * Constructs a new {@link PatientClinicalEventActEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public PatientClinicalEventActEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, ActStatus.COMPLETED, context);
        initParticipant("customer", context.getContext().getCustomer());
        initParticipant("location", context.getContext().getLocation());
    }

}
