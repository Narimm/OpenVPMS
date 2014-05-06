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
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.resource.i18n.Messages;
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
     * @param act the act
     * @return the reason. May be {@code null}
     */
    @Override
    protected String getReason(Act act) {
        return LookupNameHelper.getName(act, "reason");
    }

    /**
     * Formats an act item.
     *
     * @param bean the item bean
     * @param row  the current row
     * @return a component representing the item
     */
    @Override
    protected Component formatItem(ActBean bean, int row) {
        if (bean.isA(PatientArchetypes.CLINICAL_EVENT)) {
            return formatEvent(bean, row);
        }
        return super.formatItem(bean, row);
    }

    /**
     * Returns a component for an <em>act.patientClinicalEvent</em>.
     *
     * @param bean the act
     * @param row  the current row
     * @return a component representing the act
     * @throws OpenVPMSException for any error
     */
    private Component formatEvent(ActBean bean, int row) {
        Act act = bean.getAct();
        String reason = act.getReason();
        if (StringUtils.isEmpty(reason)) {
            reason = Messages.get("patient.record.summary.reason.none");
        }
        String status = LookupNameHelper.getName(act, "status");
        String clinician = getClinician(bean, row);
        String age = getAge(bean);

        String text = Messages.format("patient.record.problem.event", reason, clinician, status, age);
        Label label = LabelFactory.create();
        label.setText(text);
        return label;
    }

}
