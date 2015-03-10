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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.problem;

import echopointng.LabelEx;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
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
        super(PatientArchetypes.CLINICAL_PROBLEM, context, DEFAULT_CACHE_SIZE);
    }

    /**
     * Returns a component for the act type.
     * <p/>
     * This indents the type depending on the act's depth in the act hierarchy.
     *
     * @param bean the act
     * @return a component representing the act type
     */
    @Override
    protected Component getType(ActBean bean) {
        if (bean.isA(PatientArchetypes.CLINICAL_EVENT)) {
            return getHyperlinkedType(bean);
        }
        return super.getType(bean);
    }

    /**
     * Returns a component for a parent act.
     *
     * @param bean the parent act
     * @return a component representing the act
     * @throws OpenVPMSException for any error
     */
    @Override
    protected Component formatParent(ActBean bean) {
        Component result;
        String presentingComplaint = LookupNameHelper.getName(bean.getAct(), "presentingComplaint");

        if (presentingComplaint != null) {
            String date = formatDateRange(bean);
            String title = formatProblemText(bean);
            Label dateLabel = LabelFactory.create(null, Styles.BOLD);
            Label titleLabel = LabelFactory.create(null, Styles.BOLD);
            Label complaintLabel = LabelFactory.create();

            dateLabel.setText(date);
            titleLabel.setText(title);
            complaintLabel.setText(Messages.format("patient.record.summary.presentingComplaint", presentingComplaint));

            // hack to pad the presenting complaint to line up with the item text
            Row row1 = RowFactory.create(Styles.CELL_SPACING, dateLabel, titleLabel);
            Row padding = RowFactory.create(Styles.INSET, new Label(""));
            LabelEx spacer1 = new LabelEx("");
            LabelEx spacer2 = new LabelEx("");
            spacer1.setStyleName("MedicalRecordSummary.date");
            spacer2.setStyleName("MedicalRecordSummary.type");
            Row row2 = RowFactory.create(Styles.CELL_SPACING, padding, spacer1, spacer2, complaintLabel);
            result = ColumnFactory.create(Styles.CELL_SPACING, row1, row2);
        } else {
            String date = formatDateRange(bean);
            String text = formatProblemText(bean);
            Label summary = LabelFactory.create(null, Styles.BOLD);
            summary.setText(Messages.format("patient.record.summary.datedTitle", date, text));
            result = summary;
        }
        return result;
    }

    /**
     * Formats the text for a clinical event.
     *
     * @param bean the act
     * @return the formatted text
     */
    protected String formatProblemText(ActBean bean) {
        String reason = LookupNameHelper.getName(bean.getAct(), "reason");
        if (reason == null) {
            reason = Messages.get("patient.record.summary.diagnosis.none");
        }
        return formatParentText(bean, reason);
    }

    /**
     * Formats an act item.
     *
     * @param bean the item bean
     * @return a component representing the item
     */
    @Override
    protected Component formatItem(ActBean bean) {
        if (bean.isA(PatientArchetypes.CLINICAL_EVENT)) {
            return formatEvent(bean);
        }
        return super.formatItem(bean);
    }

    /**
     * Returns a component for an <em>act.patientClinicalEvent</em>.
     *
     * @param bean the act
     * @return a component representing the act
     * @throws OpenVPMSException for any error
     */
    private Component formatEvent(ActBean bean) {
        String text = formatEventText(bean);
        Label summary = LabelFactory.create();
        summary.setText(text);
        return summary;
    }

}
