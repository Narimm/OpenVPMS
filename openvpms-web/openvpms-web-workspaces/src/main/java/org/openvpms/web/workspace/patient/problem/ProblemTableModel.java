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

package org.openvpms.web.workspace.patient.problem;

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.workspace.patient.history.AbstractPatientHistoryTableModel;


/**
 * Patient problem summary table model.
 *
 * @author Tim Anderson
 */
public class ProblemTableModel extends AbstractPatientHistoryTableModel {

    /**
     * Constructs an {@link ProblemTableModel}.
     *
     * @param context the layout context
     */
    public ProblemTableModel(LayoutContext context) {
        super(PatientArchetypes.CLINICAL_PROBLEM, context);
    }

    /**
     * Returns the reason for the parent act.
     *
     * @param bean the parent act bean
     * @return the reason. May be {@code null}
     */
    @Override
    protected String getReason(ActBean bean) {
        return bean.getString("problem");
    }
}
