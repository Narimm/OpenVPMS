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
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.map.UBLDocument;
import org.openvpms.esci.adapter.map.UBLType;
import org.openvpms.esci.adapter.util.ESCIAdapterException;
import org.openvpms.esci.ubl.common.aggregate.OrderReferenceType;
import org.openvpms.esci.ubl.common.basic.AcceptedIndicatorType;
import org.openvpms.esci.ubl.common.basic.RejectionNoteType;
import org.openvpms.esci.ubl.order.OrderResponseSimpleType;


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
     * @throws ESCIAdapterException if the identifier isn't set
     */
    public String getID() {
        return getId(response.getID(), "ID");
    }

    /**
     * Returns the UBL version identifier.
     *
     * @return the UBL version
     * @throws ESCIAdapterException if the identifier isn't set
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
     * @throws ESCIAdapterException      if the order was not found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public FinancialAct getOrder() {
        OrderReferenceType reference = getRequired(response.getOrderReference(), "OrderReference");
        FinancialAct result = (FinancialAct) getObject(ORDER_ID, reference.getID(), "OrderReference");
        if (result == null) {
            throw new ESCIAdapterException(ESCIAdapterMessages.invalidOrder(
                    getType(), getID(), reference.getID().getValue()));
        }
        return result;
    }

    /**
     * Returns the supplier, if the <tt>SellerSupplierParty/CustomerAssignedAccountID</tt> is provided.
     *
     * @return the supplier, or <tt>null</tt> if the CustomerAssignedAccountID is not present
     * @throws ESCIAdapterException      if the supplier was specified but not found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getSupplier() {
        return getSupplier(response.getSellerSupplierParty(), "SellerSupplierParty");
    }

    /**
     * Returns the supplier assigned account id for the supplier, if one is provided.
     *
     * @return the supplier assigned account id for the supplier, or <tt>null</tt> if none is specified
     */
    public String getSupplierId() {
        return getSupplierId(response.getSellerSupplierParty(), "SellerSupplierParty");
    }

    /**
     * Verifies that the supplier matches that expected.
     *
     * @param expectedSupplier  the expected supplier
     * @param expectedAccountId the expected account identifier. May be <tt>null</tt>
     * @throws ESCIAdapterException if the supplier is invalid
     */
    public void checkSupplier(Party expectedSupplier, String expectedAccountId) {
        Party supplier = getSupplier();
        String accountId = getSupplierId();
        checkSupplier(expectedSupplier, expectedAccountId, supplier, accountId, "SellerSupplierParty");
    }

    /**
     * Returns the stock location, if the <tt>AccountingCustomerParty/CustomerAssignedAccountID</tt> is provided.
     *
     * @return the stock location, or <tt>null</tt> if the CustomerAssignedAccountID is not present
     * @throws ESCIAdapterException      if the stock location was not found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getStockLocation() {
        return getStockLocation(response.getBuyerCustomerParty(), "BuyerCustomerParty");
    }

    /**
     * Returns the supplier assigned account id for the stock location, if one is provided.
     *
     * @return the supplier assigned account id for the stock location, or <tt>null</tt> if none is specified
     */
    public String getStockLocationId() {
        return getStockLocationId(response.getBuyerCustomerParty(), "BuyerCustomerParty");
    }

    /**
     * Verifies that the stock location matches that expected.
     *
     * @param expectedStockLocation the expected stock location
     * @param expectedAccountId     the expected account identifier. May be <tt>null</tt>
     * @throws ESCIAdapterException if the supplier is invalid
     */
    public void checkStockLocation(Party expectedStockLocation, String expectedAccountId) {
        Party stockLocation = getStockLocation();
        String accountId = getStockLocationId();
        checkStockLocation(expectedStockLocation, expectedAccountId, stockLocation, accountId, "BuyerCustomerParty");
    }

}
