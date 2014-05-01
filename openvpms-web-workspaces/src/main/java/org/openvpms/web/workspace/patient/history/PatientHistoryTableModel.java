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

package org.openvpms.web.workspace.patient.history;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceFunctions;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.web.component.im.doc.DocumentViewer;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.resource.i18n.format.NumberFormatter;

import java.util.Date;
import java.util.List;


/**
 * Patient medical history summary table model.
 *
 * @author Tim Anderson
 */
public class PatientHistoryTableModel extends AbstractPatientHistoryTableModel {

    /**
     * Constructs a {@link PatientHistoryTableModel}.
     *
     * @param context the layout context
     */
    public PatientHistoryTableModel(LayoutContext context) {
        super(PatientArchetypes.CLINICAL_EVENT, context);
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
        String reason = getValue(bean, "reason", "patient.record.history.reason.none");
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
            text = Messages.format("patient.record.history.singleDate", started, reason, clinician, status, age);
        } else {
            text = Messages.format("patient.record.history.dateRange", started, completed, reason, clinician, status,
                                   age);
        }
        Label summary = LabelFactory.create(null, Styles.BOLD);
        summary.setText(text);
        return summary;
    }

    /**
     * Formats an act item.
     *
     * @param bean the item bean
     * @return a component representing the item
     */
    protected Component formatItem(ActBean bean) {
        Component detail;
        if (bean.isA("act.patientInvestigation*") || bean.isA("act.patientDocument*")) {
            detail = getDocumentDetail((DocumentAct) bean.getAct());
        } else if (bean.isA(PatientArchetypes.PATIENT_MEDICATION)) {
            detail = getMedicationDetail(bean);
        } else if (bean.isA(CustomerAccountArchetypes.INVOICE_ITEM)) {
            detail = getInvoiceItemDetail(bean);
        } else {
            detail = super.formatItem(bean);
        }
        return detail;
    }

    /**
     * Returns a component for the act type.
     * <p/>
     * This indents document version acts.
     *
     * @param act the act
     * @return a component representing the act type
     */
    @Override
    protected Component getType(Act act) {
        Component result = super.getType(act);
        if (TypeHelper.isA(act, "act.patientDocument*Version")) {
            result = RowFactory.create("InsetX", result);
        }
        return result;
    }

    /**
     * Returns a component for the detail of an act.patientDocument*. or
     * act.patientInvestigation*.
     *
     * @param act the act
     * @return a new component
     */
    private Component getDocumentDetail(DocumentAct act) {
        Component result;
        Label label = getTextDetail(act);

        DocumentViewer viewer = new DocumentViewer(act, true, getContext());
        viewer.setShowNoDocument(false);

        if (StringUtils.isEmpty(label.getText())) {
            result = viewer.getComponent();
        } else {
            result = RowFactory.create(Styles.CELL_SPACING, label, viewer.getComponent());
        }
        return result;
    }

    /**
     * Returns a component for the detail of an act.patientMedication.
     * <p/>
     * This includes the invoice item amount, if one is available.
     *
     * @param bean the act
     * @return a new component
     */
    private Component getInvoiceItemDetail(ActBean bean) {
        IMObjectReference product = bean.getNodeParticipantRef("product");
        String name = IMObjectHelper.getName(product);
        FinancialAct act = (FinancialAct) bean.getAct();
        String text = Messages.format("patient.record.summary.invoiceitem", name, act.getQuantity(),
                                      NumberFormatter.formatCurrency(act.getTotal()));
        return getTextDetail(text);
    }

    /**
     * Returns a component for the detail of an act.patientMedication.
     *
     * @param act the act
     * @return a new component
     */
    private Component getMedicationDetail(ActBean act) {
        String text = getText(act.getAct());
        List<IMObjectReference> refs = act.getNodeSourceObjectRefs("invoiceItem");
        if (!refs.isEmpty()) {
            ArchetypeQuery query = new ArchetypeQuery(refs.get(0));
            query.getArchetypeConstraint().setAlias("act");
            query.add(new NodeSelectConstraint("total"));
            query.setMaxResults(1);
            ObjectSetQueryIterator iter = new ObjectSetQueryIterator(query);
            if (iter.hasNext()) {
                ObjectSet set = iter.next();
                text = Messages.format("patient.record.summary.medication", text,
                                       NumberFormatter.formatCurrency(set.getBigDecimal("act.total")));
            }

        }
        return getTextDetail(text);
    }

}
