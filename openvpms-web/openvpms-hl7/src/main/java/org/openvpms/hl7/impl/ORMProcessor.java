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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.message.ORM_O01;
import ca.uhn.hl7v2.model.v25.segment.ORC;
import ca.uhn.hl7v2.model.v25.segment.PID;
import org.openvpms.archetype.rules.finance.order.CustomerOrder;
import org.openvpms.archetype.rules.finance.order.OrderArchetypes;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.util.List;

/**
 * Processes ORM messages.
 * <p/>
 * This generates <em>act.customerReturnInvestigation</em> messages for each ORM message received that has a CA
 * orderType.
 *
 * @author Tim Anderson
 */
public class ORMProcessor extends OrderMessageProcessor {

    /**
     * Constructs an {@link ORMProcessor}.
     *
     * @param service the archetype service
     * @param rules   the patient rules
     */
    public ORMProcessor(IArchetypeService service, PatientRules rules, UserRules userRules) {
        super(service, rules, userRules);
    }

    /**
     * Processes a dispense message.
     *
     * @param message  the message
     * @param location the practice location reference
     * @return the customer order and/or return
     * @throws HL7Exception for any HL7 error
     */
    public List<Act> process(ORM_O01 message, IMObjectReference location) throws HL7Exception {
        PID pid = message.getPATIENT().getPID();
        CustomerOrder state = createState(pid, location);
        ORC orc = message.getORDER().getORC();
        String orderControl = orc.getOrderControl().getValue();
        if (!"CA".equals(orderControl)) {
            throw new HL7Exception("Unsupported order control: " + orderControl);
        }
        addItem(orc, state);
        return state.getActs();
    }

    /**
     * Adds an order item.
     *
     * @param item  the order group
     * @param state the state
     */
    private void addItem(ORC item, CustomerOrder state) {
        ActBean bean = state.getReturn();
        ActBean itemBean = state.createReturnItem();
        Act investigation = getOrder(InvestigationArchetypes.PATIENT_INVESTIGATION, item, bean, state);
        if (investigation != null) {
            itemBean.setValue("sourceInvestigation", investigation.getObjectReference());
            ActBean investigationBean = new ActBean(investigation, getService());
            itemBean.setNodeParticipant("product", investigationBean.getNodeParticipantRef("product"));
            itemBean.setNodeParticipant("investigationType",
                                        investigationBean.getNodeParticipantRef("investigationType"));
            itemBean.setValue("sourceInvoiceItem", investigationBean.getNodeSourceObjectRef("invoiceItem"));
        }
    }

    @Override
    protected CustomerOrder createState(Party patient, Party customer, String note, IMObjectReference location,
                                     IArchetypeService service) {
        return new State(patient, customer, note, location, service);
    }

    private class State extends CustomerOrder {

        /**
         * Constructs a {@link CustomerOrder}.
         *
         * @param patient  the patient. May be {@code null}
         * @param customer the customer. May be {@code null}
         * @param note     the note. May be {@code null}
         * @param location the practice location. May be {@code null}
         * @param service  the archetype service
         */
        public State(Party patient, Party customer, String note, IMObjectReference location, IArchetypeService service) {
            super(patient, customer, note, location, service);
        }

        public ActBean createOrderItem() {
            throw new UnsupportedOperationException("Orders aren't supported");
        }

        public ActBean createReturnItem() {
            return createItem(OrderArchetypes.INVESTIGATION_RETURN_ITEM, getReturn());
        }

        @Override
        protected ActBean createOrder() {
            throw new UnsupportedOperationException("Orders aren't supported");
        }

        @Override
        protected ActBean createReturn() {
            return createParent(OrderArchetypes.INVESTIGATION_RETURN);
        }

    }

}
