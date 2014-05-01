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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.ArchetypeServiceFunctions;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.workspace.patient.history.AbstractPatientHistoryTableModel;

import java.util.Date;


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
     * Returns a component for a parent act.
     *
     * @param act the parent act
     * @param row the current row
     * @return a component representing the act
     * @throws OpenVPMSException for any error
     */
    protected Component formatParent(Act act, int row) {
        ActBean bean = new ActBean(act);
        String started = null;
        String completed = null;
        String clinician;
        String status = ArchetypeServiceFunctions.lookup(act, "status");

        Date startTime = bean.getDate("startTime");
        if (startTime != null) {
            started = DateFormatter.formatDate(startTime, false);
        }

        Date endTime = bean.getDate("endTime");
        if (endTime != null) {
            completed = DateFormatter.formatDate(endTime, false);
        }

        clinician = getClinician(bean, row);
        String age = getAge(bean);

        String text;
        if (completed == null || ObjectUtils.equals(started, completed)) {
            text = Messages.format("patient.record.problem.singleDate", started, clinician, status, age);
        } else {
            text = Messages.format("patient.record.problem.dateRange", started, completed, clinician, status, age);
        }
        Label summary = LabelFactory.create(null, Styles.BOLD);
        summary.setText(text);
        return summary;
    }

}
