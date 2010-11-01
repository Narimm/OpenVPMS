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
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.i18n.Message;
import org.openvpms.esci.adapter.map.UBLDocument;
import org.openvpms.esci.adapter.map.UBLType;
import org.openvpms.esci.exception.ESCIException;


/**
 * Wrapper for the UBL <tt>OrderResponseSimpleType<tt>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class UBLOrderResponseSimple extends UBLType implements UBLDocument {

    /**
     * The response to wrap.
     */
    private final OrderResponseSimpleType response;

    /**
     * Order archetype identifier.
     */
    private ArchetypeId ORDER_ID = new ArchetypeId("act.supplierOrder");


    /**
     * Constructs an <tt>UBLOrderResponseSimple</tt>.
     *
     * @param response the order response type
     * @param service  the archetype service
     */
    public UBLOrderResponseSimple(OrderResponseSimpleType response, IArchetypeService service) {
        super(null, service);
        this.response = response;
    }

    /**
     * Returns the type name.
     *
     * @return the type name
     */
    public String getType() {
        return "OrderResponseSimple";
    }

    /**
     * Returns the response identifier.
     *
     * @return the response identifier
     * @throws ESCIException if the identifier isn't set
     */
    public String getID() {
        return getId(response.getID(), "ID");
    }

    /**
     * Returns the UBL version identifier.
     *
     * @return the UBL version
     * @throws ESCIException if the identifier isn't set
     */
    public String getUBLVersionID() {
        return getId(response.getUBLVersionID(), "UBLVersionID");
    }

    /**
     * Determines if the order was accepted.
     *
     * @return <tt>true</tt> if the order was accepted, <tt>false</tt> if it was rejected
     */
    public boolean isAccepted() {
        AcceptedIndicatorType indicator = getRequired(response.getAcceptedIndicator(), "AcceptedIndicator");
        return indicator.isValue();
    }

    /**
     * Returns the rejection note.
     *
     * @return the rejection note, if one was specified, otherwise <tt>null</tt>
     */
    public String getRejectionNote() {
        String result = null;
        RejectionNoteType note = response.getRejectionNote();
        if (note != null && !StringUtils.isEmpty(note.getValue())) {
            result = note.getValue();
        }
        return result;

    }

    /**
     * Returns the order referred to in the response.
     *
     * @return the order
     * @throws ESCIException             if the order was not found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public FinancialAct getOrder() {
        OrderReferenceType reference = getRequired(response.getOrderReference(), "OrderReference");
        FinancialAct result = (FinancialAct) getObject(ORDER_ID, reference.getID(), "OrderReference");
        if (result == null) {
            Message message = ESCIAdapterMessages.invalidOrder(getType(), getID(), reference.getID().getValue());
            throw new ESCIException(message.toString());
        }
        return result;
    }

    /**
     * Returns the supplier.
     *
     * @return the supplier
     * @throws ESCIException             if the supplier was not found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getSupplier() {
        return getSupplier(response.getSellerSupplierParty(), "SellerSupplierParty");
    }


}
