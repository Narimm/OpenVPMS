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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.workspace.workflow.consult;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.object.Reference;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.GetInvoiceTask;


/**
 * Task to query the most recent <em>act.customerAccountChargesInvoice</em>, for the context customer.
 * If the context has an <em>act.patientClinicalEvent</em> then the invoice associated with this will be returned.
 * <p/>
 * The invoice will be added to the context.
 *
 * @author Tim Anderson
 */
public class GetConsultInvoiceTask extends GetInvoiceTask {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs a new {@link GetConsultInvoiceTask}.
     */
    public GetConsultInvoiceTask() {
        this.service = ServiceHelper.getArchetypeService();
    }

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
            invoice = getInvoice(event, context);
        }
        return invoice;
    }

    /**
     * Returns the invoice associated with an event, if it belongs to the current customer.
     *
     * @param event   the event
     * @param context the context
     * @return the invoice associated with the event, or {@code null} if none is present. The invoice may be POSTED
     */
    protected Act getInvoice(Act event, TaskContext context) {
        Act invoice = null;
        Party customer = context.getCustomer();
        if (customer == null) {
            throw new IllegalStateException("Context has no customer");
        }
        Reference customerRef = customer.getObjectReference();
        IMObjectBean bean = service.getBean(event);
        for (ActRelationship relationship : bean.getValues("chargeItems", ActRelationship.class)) {
            Act item = (Act) IMObjectHelper.getObject(relationship.getTarget(), context);
            if (item != null) {
                IMObjectBean itemBean = service.getBean(item);
                Reference invoiceRef = itemBean.getSourceRef("invoice");
                if (invoiceRef != null && (invoice == null
                                           || !ObjectUtils.equals(invoice.getObjectReference(), invoiceRef))) {
                    Act act = (Act) IMObjectHelper.getObject(invoiceRef, context);
                    if (act != null) {
                        IMObjectBean invoiceBean = service.getBean(act);
                        if (ObjectUtils.equals(customerRef, invoiceBean.getTargetRef("customer"))) {
                            if (invoice == null) {
                                invoice = act;
                            }
                            if (!ActStatus.POSTED.equals(act.getStatus())) {
                                // now if there are multiple non-POSTED invoices, which one to select? TODO
                                break;
                            }
                        }
                    }
                }
            }
        }
        return invoice;
    }
}
