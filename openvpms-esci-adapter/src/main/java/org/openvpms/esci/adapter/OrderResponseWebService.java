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
package org.openvpms.esci.adapter;

import org.apache.commons.lang.StringUtils;
import org.oasis.ubl.OrderResponseSimpleType;
import org.oasis.ubl.OrderResponseType;
import org.oasis.ubl.common.basic.IDType;
import org.oasis.ubl.common.basic.RejectionNoteType;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.esci.exception.ESCIException;
import org.openvpms.esci.service.OrderResponseService;

import javax.jws.WebService;


/**
 * Order response web service.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
@WebService(endpointInterface = "org.openvpms.esci.service.OrderResponseService",
            targetNamespace = "http://openvpms.org/esci", serviceName = "OrderResponseService",
            portName = "OrderResponseServicePort")
public class OrderResponseWebService implements OrderResponseService {

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * Order archetype identifier.
     */
    private ArchetypeId ORDER_ID = new ArchetypeId("act.supplierOrder");

    /**
     * Default constructor.
     */
    public OrderResponseWebService() {
    }

    /**
     * Sets the archetype service.
     *
     * @param service the archetype service
     */
    public void setArchetypeService(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Submits a simple response to an order.
     *
     * @param response the response
     * @throws ESCIException if the response is invalid or cannot be processed
     */
    public void submitSimpleResponse(OrderResponseSimpleType response) throws ESCIException {
        IMObjectReference reference = getOrderReference(response);
        Act act = (Act) service.get(reference);
        if (act == null) {
            throw new ESCIException("OrderReference with ID " + response.getOrderReference().getID().getValue()
                                    + " does not refer to a valid order");
        }
        ActBean bean = new ActBean(act, service);
        if (response.getAcceptedIndicator().isValue()) {
            bean.setValue("status", "ACCEPTED");
            bean.setValue("supplierResponse", "Order Accepted");
        } else {
            String message = null;
            RejectionNoteType note = response.getRejectionNote();
            if (note != null) {
                message = note.getValue();
            }
            if (StringUtils.isEmpty(message)) {
                message = "Order rejected without any message";
            }
            bean.setValue("status", "REJECTED");
            bean.setValue("supplierResponse", message);
        }
        bean.save();
    }

    /**
     * Submits a simple response to an order.
     *
     * @param response the response
     * @throws ESCIException if the response is invalid or cannot be processed
     */
    public void submitResponse(OrderResponseType response) throws ESCIException {
        throw new ESCIException("Unsupported operation");
    }

    private IMObjectReference getOrderReference(OrderResponseSimpleType response) {
        IDType type = response.getOrderReference().getID();
        long id;
        try {
            id = Long.valueOf(type.getValue());
        } catch (NumberFormatException exception) {
            throw new ESCIException(type.getValue() + " is not a valid order reference");
        }
        return new IMObjectReference(ORDER_ID, id);
    }

}
