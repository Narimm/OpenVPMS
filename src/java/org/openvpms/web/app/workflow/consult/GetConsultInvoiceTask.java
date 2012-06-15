/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.workflow.consult;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.workflow.GetInvoiceTask;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.workflow.TaskContext;


/**
 * Task to query the most recent <em>act.customerAccountChargesInvoice</em>, for the context customer.
 * If the context has an <em>act.patientClinicalEvent</em> then the invoice associated with this will be returned.
 * <p/>
 * The invoice will be added to the context.
 *
 * @author Tim Anderson
 */
class GetConsultInvoiceTask extends GetInvoiceTask {

    /**
     * Executes the task.
     *
     * @throws OpenVPMSException for any error
     */
    @Override
    public void execute(TaskContext context) {
        Act invoice = getInvoiceForEvent(context);
        if (invoice != null) {
            context.addObject(invoice);
        } else {
            super.execute(context);
        }
    }

    /**
     * Returns an invoice associated with the current event.
     * <p/>
     * This will select non-POSTED invoices in preference to POSTED ones, if available.
     *
     * @param context the context
     * @return an invoice linked to the event, or {@code null} if none is found.
     */
    protected Act getInvoiceForEvent(TaskContext context) {
        Act event = (Act) context.getObject(PatientArchetypes.CLINICAL_EVENT);
        Act invoice = null;
        if (event != null) {
            ActBean bean = new ActBean(event);
            for (ActRelationship relationship : bean.getRelationships(PatientArchetypes.CLINICAL_EVENT_CHARGE_ITEM)) {
                Act item = (Act) IMObjectHelper.getObject(relationship.getTarget());
                if (item != null) {
                    ActBean itemBean = new ActBean(item);
                    IMObjectReference invoiceRef = itemBean.getSourceObjectRef(
                            item.getTargetActRelationships(), CustomerAccountArchetypes.INVOICE_ITEM_RELATIONSHIP);
                    if (invoiceRef != null
                            && (invoice == null || !ObjectUtils.equals(invoice.getObjectReference(), invoiceRef))) {
                        invoice = (Act) IMObjectHelper.getObject(invoiceRef);
                        if (invoice != null && !ActStatus.POSTED.equals(invoice.getStatus())) {
                            // now if there are multiple non-POSTED invoices, which one to select? TODO
                            break;
                        }
                    }
                }
            }
        }
        return invoice;
    }
}
