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

import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.app.workflow.GetInvoiceTask;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.workflow.TaskContext;


/**
 * Task to query the most recent <em>act.customerAccountChargesInvoice</em>.
 * for the context customer. If the context has an <em>act.patientClinicalEvent</em> then the invoice associated with
 * this will be returned.
 * <p/>
 * The invoice will be added to the context.
 *
 * @author Tim Anderson
 */
class GetConsultInvoiceTask extends GetInvoiceTask {

    /**
     * Executes the task.
     *
     * @throws org.openvpms.component.system.common.exception.OpenVPMSException
     *          for any error
     */
    @Override
    public void execute(TaskContext context) {
        Act event = (Act) context.getObject(PatientArchetypes.CLINICAL_EVENT);
        Act invoice = null;
        if (event != null) {
            ActBean bean = new ActBean(event);
            for (ActRelationship relationship : bean.getRelationships(PatientArchetypes.CLINICAL_EVENT_CHARGE_ITEM)) {
                Act item = (Act) IMObjectHelper.getObject(relationship.getTarget());
                if (item != null) {
                    ActBean itemBean = new ActBean(item);
                    invoice = itemBean.getSourceAct(CustomerAccountArchetypes.INVOICE_ITEM_RELATIONSHIP);
                    if (invoice != null) {
                        break;
                    }
                }
            }
        }
        if (invoice != null) {
            context.addObject(invoice);
        } else {
            super.execute(context);
        }
    }
}
