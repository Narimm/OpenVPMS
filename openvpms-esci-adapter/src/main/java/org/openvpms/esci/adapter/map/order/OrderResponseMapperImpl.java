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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.esci.adapter.map.order;

import org.apache.commons.lang.StringUtils;
import org.oasis.ubl.OrderResponseSimpleType;
import org.oasis.ubl.common.aggregate.OrderReferenceType;
import org.oasis.ubl.common.basic.AcceptedIndicatorType;
import org.oasis.ubl.common.basic.RejectionNoteType;
import org.openvpms.archetype.rules.supplier.OrderStatus;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.i18n.Message;
import org.openvpms.esci.adapter.map.AbstractUBLMapper;
import org.openvpms.esci.exception.ESCIException;

import javax.annotation.Resource;


/**
 * Maps UBL order responses to their corresponding orders.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OrderResponseMapperImpl extends AbstractUBLMapper implements OrderResponseMapper {

    /**
     * The bean factory.
     */
    private IMObjectBeanFactory factory;

    /**
     * Order archetype identifier.
     */
    private ArchetypeId ORDER_ID = new ArchetypeId("act.supplierOrder");


    /**
     * Registers the bean factory.
     *
     * @param factory the bean factory
     */
    @Resource
    public void setBeanFactory(IMObjectBeanFactory factory) {
        this.factory = factory;
    }

    /**
     * Maps an <tt>OrderResponseSimpleType</tt> to its corresponding order.
     *
     * @param response the reponse
     * @param user     the ESCI user that submitted the reponse
     * @return the corresponding order
     */
    public FinancialAct map(OrderResponseSimpleType response, User user) {
        String responseId = getResponseId(response);
        checkUBLVersion(response.getUBLVersionID(), "OrderResponseSimple", responseId);
        Party supplier = getSupplier(response, responseId);
        checkSupplier(supplier, user, factory);
        FinancialAct order = getOrder(response, supplier, responseId);
        AcceptedIndicatorType indicator = getRequired(response.getAcceptedIndicator(), "AcceptedIndicator",
                                                      "OrderResponseSimple", responseId);
        Message message;
        String status;
        if (indicator.isValue()) {
            status = OrderStatus.ACCEPTED;
            message = ESCIAdapterMessages.orderAccepted();
        } else {
            status = OrderStatus.REJECTED;
            RejectionNoteType note = response.getRejectionNote();
            if (note != null && !StringUtils.isEmpty(note.getValue())) {
                message = ESCIAdapterMessages.orderRejected(note.getValue());
            } else {
                message = ESCIAdapterMessages.orderRejectedNoReason();
            }
        }
        ActBean bean = factory.createActBean(order);
        bean.setValue("status", status);
        bean.setValue("supplierResponse", message.getMessage());
        return order;
    }

    /**
     * Returns the order referred to in the response.
     * This only returns orders associated with the specified supplier.
     *
     * @param response   the response
     * @param supplier   the supplier
     * @param responseId the response identifier
     * @return the corresponding order
     * @throws ESCIException if the order was not found or was not created
     * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
     *                       for any archetype service error
     */
    protected FinancialAct getOrder(OrderResponseSimpleType response, Party supplier, String responseId) {
        IMObjectReference reference = getOrderReference(response, responseId);
        return getOrder(reference, supplier, "OrderResponseSimple", responseId);
    }

    /**
     * Returns the order response identifier identifier.
     *
     * @param response the response
     * @return the invoice identifier
     * @throws ESCIException if the identifier isn't set
     */
    protected String getResponseId(OrderResponseSimpleType response) {
        return getId(response.getID(), "ID", "OrderResponseSimple", null);
    }

    /**
     * Returns the order reference.
     *
     * @param response   the order response
     * @param responseId the response identifier
     * @return the order reference
     * @throws ESCIException if the order reference is not specified or invalid
     */
    private IMObjectReference getOrderReference(OrderResponseSimpleType response, String responseId) {
        OrderReferenceType reference = getRequired(response.getOrderReference(), "OrderReference",
                                                   "OrderResponseSimple", responseId);
        return getReference(ORDER_ID, reference.getID(), "OrderReference", "OrderResponseSimple", responseId);
    }

    /**
     * Returns the order response supplier.
     *
     * @param response   the response
     * @param responseId the invoice identifier
     * @return the supplier
     * @throws ESCIException if the supplier was not found
     */
    protected Party getSupplier(OrderResponseSimpleType response, String responseId) {
        return getSupplier(response.getAccountingSupplierParty(), "AccountingSupplierParty", "OrderResponseSimple",
                           responseId);
    }

}
