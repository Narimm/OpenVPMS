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
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.NumberFormatter;

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
     * Formats an act item.
     *
     * @param bean the item bean
     * @param row  the current row
     * @return a component representing the item
     */
    @Override
    protected Component formatItem(ActBean bean, int row) {
        Component detail;
        if (bean.isA(PatientArchetypes.PATIENT_MEDICATION)) {
            detail = getMedicationDetail(bean);
        } else if (bean.isA(CustomerAccountArchetypes.INVOICE_ITEM)) {
            detail = getInvoiceItemDetail(bean);
        } else {
            detail = super.formatItem(bean, row);
        }
        return detail;
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
