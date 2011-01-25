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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.supplier;


/**
 * Supplier archetype short names.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SupplierArchetypes {

    /**
     * Supplier person short name.
     */
    public static final String SUPPLIER_PERSON = "party.supplierperson";

    /**
     * Supplier organisation short name.
     */
    public static final String SUPPLIER_ORGANISATION
            = "party.supplierorganisation";

    /**
     * Supplier order act short name.
     */
    public static final String ORDER = "act.supplierOrder";

    /**
     * Supplier order item act short name.
     */
    public static final String ORDER_ITEM = "act.supplierOrderItem";

    /**
     * Supplier order item relationship short name.
     */
    public static final String ORDER_ITEM_RELATIONSHIP
            = "actRelationship.supplierOrderItem";

    /**
     * Supplier delivery act short name.
     */
    public static final String DELIVERY = "act.supplierDelivery";

    /**
     * Supplier delivery item act short name.
     */
    public static final String DELIVERY_ITEM = "act.supplierDeliveryItem";

    /**
     * Supplier delivery item relationship short name.
     */
    public static final String DELIVERY_ITEM_RELATIONSHIP
            = "actRelationship.supplierDeliveryItem";

    /**
     * Supplier delivery-order relationship short name.
     */
    public static final String DELIVERY_ORDER_RELATIONSHIP = "actRelationship.supplierDeliveryOrder";

    /**
     * Supplier delivery-order item relationship short name.
     */
    public static final String DELIVERY_ORDER_ITEM_RELATIONSHIP
            = "actRelationship.supplierDeliveryOrderItem";

    /**
     * Supplier invoice act short name.
     */
    public static final String INVOICE = "act.supplierAccountChargesInvoice";

    /**
     * Supplier invoice item act short name.
     */
    public static final String INVOICE_ITEM = "act.supplierAccountInvoiceItem";

    /**
     * Supplier invoice item relationship short name.
     */
    public static final String INVOICE_ITEM_RELATIONSHIP
            = "actRelationship.supplierAccountInvoiceItem";

    /**
     * Supplier return act short name.
     */
    public static final String RETURN = "act.supplierReturn";

    /**
     * Supplier return item act short name.
     */
    public static final String RETURN_ITEM = "act.supplierReturnItem";

    /**
     * Supplier return item relationship short name.
     */
    public static final String RETURN_ITEM_RELATIONSHIP
            = "actRelationship.supplierReturnItem";

    /**
     * Supplier return order item relationship short name.
     */
    public static final String RETURN_ORDER_ITEM_RELATIONSHIP
            = "actRelationship.supplierReturnOrderItem";

    /**
     * Supplier credit act short name.
     */
    public static final String CREDIT = "act.supplierAccountChargesCredit";

    /**
     * Supplier credit item act short name.
     */
    public static final String CREDIT_ITEM = "act.supplierAccountCreditItem";

    /**
     * Supplier credit item relationship short name.
     */
    public static final String CREDIT_ITEM_RELATIONSHIP
            = "actRelationship.supplierAccountCreditItem";

    /**
     * Supplier participation short name.
     */
    public static final String SUPPLIER_PARTICIPATION
            = "participation.supplier";

    /**
     * Supplier document attachment act short name.
     */
    public static final String DOCUMENT_ATTACHMENT = "act.supplierDocumentAttachment";

    /**
     * Supplier document attachment version act short name.
     */
    public static final String DOCUMENT_ATTACHMENT_VERSION = "act.supplierDocumentAttachmentVersion";

    /**
     * Supplier document form act short name.
     */
    public static final String DOCUMENT_FORM = "act.supplierDocumentForm";

    /**
     * Supplier document image act short name.
     */
    public static final String DOCUMENT_IMAGE = "act.supplierDocumentImage";

    /**
     * Supplier document image version act short name.
     */
    public static final String DOCUMENT_IMAGE_VERSION = "act.supplierDocumentImageVersion";

    /**
     * Supplier document letter act short name.
     */
    public static final String DOCUMENT_LETTER = "act.supplierDocumentLetter";

    /**
     * Supplier document letter version act short name.
     */
    public static final String DOCUMENT_LETTER_VERSION = "act.supplierDocumentLetterVersion";

    /**
     * Supplier/stock location relationship for e-Supply Chain Intefacing.
     */
    public static final String SUPPLIER_STOCK_LOCATION_RELATIONSHIP_ESCI
            = "entityRelationship.supplierStockLocationESCI";
}
