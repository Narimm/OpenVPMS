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
package org.openvpms.esci.adapter.i18n;

import org.openvpms.esci.ubl.common.aggregate.DocumentReferenceType;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;

import java.math.BigDecimal;


/**
 * Messages reported by ESCI Adapter.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ESCIAdapterMessages {

    /**
     * The messages.
     */
    private static Messages messages = new Messages("ESCIA", ESCIAdapterMessages.class.getName());


    /**
     * Creates a new message for when there is no <em>entityRelationship.supplierStockLocationESCI</em> relationship
     * between a supplier and stock location.
     *
     * @param supplier      the supplier
     * @param stockLocation the stock location
     * @return a new message
     */
    public static Message ESCINotConfigured(Party supplier, Party stockLocation) {
        return messages.getMessage(1, supplier.getId(), supplier.getName(), stockLocation.getId(),
                                   stockLocation.getName());
    }

    /**
     * Creates a new message for an invalid supplier service URL.
     *
     * @param supplier   the supplier
     * @param serviceURL the invalid service URL
     * @return a new message
     */
    public static Message invalidSupplierURL(Party supplier, String serviceURL) {
        return messages.getMessage(2, supplier.getId(), supplier.getName(), serviceURL);
    }

    /**
     * Creates a new message for an invalid service URL.
     *
     * @param serviceURL the invalid service URL
     * @return a new message
     */
    public static Message invalidServiceURL(String serviceURL) {
        return messages.getMessage(3, serviceURL);
    }

    /**
     * Creates a new message when a web service cannot be contacted.
     *
     * @param serviceURL the service URL
     * @return a new message
     */
    public static Message connectionFailed(String serviceURL) {
        return messages.getMessage(4, serviceURL);
    }

    /**
     * Creates a new message when a supplier web service cannot be contacted.
     *
     * @param supplier   the supplier
     * @param serviceURL the service URL
     * @return a new message
     */
    public static Message connectionFailed(Party supplier, String serviceURL) {
        return messages.getMessage(5, supplier.getId(), supplier.getName(), serviceURL);
    }

    /**
     * Creates a new message for a missing UBL element.
     *
     * @param path   the path to the element, from the parent
     * @param parent the parent element path
     * @param id     the parent element identifier
     * @return a new message
     */
    public static Message ublElementRequired(String path, String parent, String id) {
        return messages.getMessage(100, path, parent, id);
    }

    /**
     * Creates a new message for an UBL cardinality error.
     *
     * @param path     the path to the element, from the parent
     * @param parent   the parent element path
     * @param id       the parent element identifier
     * @param expected the expected cardinality (e.g "1..*", "2")
     * @param actual   the actual cardinality
     * @return a new message
     */
    public static Message ublInvalidCardinality(String path, String parent, String id, String expected,
                                                int actual) {
        return messages.getMessage(101, path, parent, id, expected, actual);
    }

    /**
     * Creates a new message for an invalid UBL identifier.
     *
     * @param path     the path to the element, from the parent
     * @param parent   the parent element path
     * @param parentId the parent element identifier
     * @param value    the invalid identifier value
     * @return a new message
     */
    public static Message ublInvalidIdentifier(String path, String parent, String parentId, String value) {
        return messages.getMessage(102, path, parent, parentId, value);
    }

    /**
     * Creates a new message for when a UBL value is different to that expected.
     *
     * @param path     the path to the element, from the parent
     * @param parent   the parent element path
     * @param parentId the parent element identifier
     * @param expected the expected value
     * @param actual   the actual value
     * @return a new message
     */
    public static Message ublInvalidValue(String path, String parent, String parentId, String expected,
                                          String actual) {
        return messages.getMessage(103, path, parent, parentId, expected, actual);
    }

    /**
     * Creates a new message for an invalid amount in an UBL document.
     *
     * @param path     the path to the amount
     * @param parent   the parent element
     * @param parentId the parent element identifier
     * @param amount   the invalid amount
     * @return a new message
     */
    public static Message invalidAmount(String path, String parent, String parentId, BigDecimal amount) {
        return messages.getMessage(104, path, parent, parentId, amount);
    }

    /**
     * Creates a new message for when an invoice element's currencyID doesn't match that expected.
     *
     * @param path     the path to the element, from the parent
     * @param parent   the parent element path
     * @param parentId the parent element identifier
     * @param expected the expected currencyID value
     * @param actual   the actual currencyID value
     * @return a new message
     */
    public static Message invalidCurrency(String path, String parent, String parentId, String expected,
                                          String actual) {
        return messages.getMessage(105, path, parent, parentId, expected, actual);
    }

    /**
     * Creates a new message for an invalid quantity in an UBL document.
     *
     * @param path     the path to the amount
     * @param parent   the parent element
     * @param parentId the parent element identifier
     * @param quantity the invalid quantity
     * @return a new message
     */
    public static Message invalidQuantity(String path, String parent, String parentId, BigDecimal quantity) {
        return messages.getMessage(106, path, parent, parentId, quantity);
    }

    /**
     * Creates a new message for an invalid supplier referenced by an UBL document.
     *
     * @param path       the path to the element
     * @param parent     the parent element
     * @param parentId   the parent element identifier
     * @param supplierId the supplier identifier
     * @return a new message
     */
    public static Message invalidSupplier(String path, String parent, String parentId, String supplierId) {
        return messages.getMessage(107, path, parent, parentId, supplierId);
    }

    /**
     * Creates a new message for when an UBL document refers to an invalid order.
     *
     * @param parent   the parent element
     * @param parentId the parent element identifier
     * @param orderId  the order identifier
     * @return a new message
     */
    public static Message invalidOrder(String parent, String parentId, String orderId) {
        return messages.getMessage(108, parent, parentId, orderId);
    }

    /**
     * Creates a new message for when a supplier doesn't match that expected.
     *
     * @param path             the path to the element
     * @param parent           the parent element
     * @param parentId         the parent element identifier
     * @param expectedSupplier the expected supplier
     * @param actualSupplier   the actual supplier
     * @return a new message
     */
    public static Message supplierMismatch(String path, String parent, String parentId, Party expectedSupplier,
                                           Party actualSupplier) {
        return messages.getMessage(109, path, parent, parentId, expectedSupplier.getId(), expectedSupplier.getName(),
                                   actualSupplier.getId(), actualSupplier.getName());
    }

    /**
     * Creates a new message for when a tax scheme and category don't correspond to an <em>lookup.taxType</em>.
     *
     * @param path       the path to the element, from the parent
     * @param parent     the parent element path
     * @param id         the parent element identifier
     * @param schemeId   the tax scheme identifier
     * @param categoryId the tax category identifier
     * @return a new message
     */
    public static Message invalidTaxSchemeAndCategory(String path, String parent, String id, String schemeId,
                                                      String categoryId) {
        return messages.getMessage(110, path, parent, id, schemeId, categoryId);
    }

    /**
     * Creates a new message for when neither a <tt>CustomerAssignedAccountId</tt> nor <tt>AdditionalAccountID</tt>
     * is present in an element that requires it.
     *
     * @param path   the path to the element, from the parent
     * @param parent the parent element path
     * @param id     the parent element identifier
     * @return a new message
     */
    public static Message noCustomerOrAdditionalAccountId(String path, String parent, String id) {
        return messages.getMessage(111, path, parent, id);
    }

    /**
     * Creates a new message for when neither a <tt>CustomerAssignedAccountID</tt> nor
     * <tt>SupplierAssignedAccountID</tt> is present in an element that requires it.
     *
     * @param path   the path to the element, from the parent
     * @param parent the parent element path
     * @param id     the parent element identifier
     * @return a new message
     */
    public static Message noCustomerOrSupplierAccountId(String path, String parent, String id) {
        return messages.getMessage(112, path, parent, id);
    }

    /**
     * Creates a new message for an invalid stock location referenced by an invoice.
     *
     * @param path            the path to the element, from the parent
     * @param parent          the parent element path
     * @param id              the parent element identifier
     * @param stockLocationId the stock location identifier
     * @return a new message
     */
    public static Message invalidStockLocation(String path, String parent, String id, String stockLocationId) {
        return messages.getMessage(113, path, parent, id, stockLocationId);
    }

    /**
     * Creates a new message for when a stock location doesn't match that expected.
     *
     * @param path                  the path to the element
     * @param parent                the parent element
     * @param parentId              the parent element identifier
     * @param expectedStockLocation the expected stock location
     * @param actualStockLocation   the actual stock location
     * @return a new message
     */
    public static Message stockLocationMismatch(String path, String parent, String parentId,
                                                Party expectedStockLocation, Party actualStockLocation) {
        return messages.getMessage(114, path, parent, parentId, expectedStockLocation.getId(),
                                   expectedStockLocation.getName(), actualStockLocation.getId(),
                                   actualStockLocation.getName());
    }

    /**
     * Creates a new message for when an amount has too many decimal places.
     *
     * @param amount the amount
     * @return a new message
     */
    public static Message amountTooManyDecimalPlaces(BigDecimal amount) {
        return messages.getMessage(115, amount);
    }

    /**
     * Creates a new message for when a quantity has too many decimal places.
     *
     * @param quantity the quantity
     * @return a new message
     */
    public static Message quantityTooManyDecimalPlaces(BigDecimal quantity) {
        return messages.getMessage(116, quantity);
    }

    /**
     * Creates a message for when a supplier reports an order as being duplicate.
     *
     * @param id       the order identifier
     * @param supplier the supplier
     * @return a new message
     */
    public static Message duplicateOrder(long id, Party supplier) {
        return messages.getMessage(200, id, supplier.getId(), supplier.getName());
    }

    /**
     * Creates a new message for when there is no supplier order code associated with a product.
     *
     * @param supplier the supplier
     * @param product  the product
     * @return a new message
     */
    public static Message noSupplierOrderCode(Party supplier, Product product) {
        return messages.getMessage(300, supplier.getId(), supplier.getName(), product.getId(), product.getName());
    }

    /**
     * Creates a new message for when there is no practice location asssociated with a stock location.
     *
     * @param stockLocation the stock location
     * @return a new message
     */
    public static Message noPracticeLocationForStockLocation(Party stockLocation) {
        return messages.getMessage(301, stockLocation.getId(), stockLocation.getName());
    }

    /**
     * Creates a new message for when an order is accepted.
     *
     * @return a new message
     */
    public static Message orderAccepted() {
        return messages.getMessage(400);
    }

    /**
     * Creates a new message for when an order is rejected.
     *
     * @param reason the reason for the rejection
     * @return a new message
     */
    public static Message orderRejected(String reason) {
        return messages.getMessage(401, reason);
    }

    /**
     * Creates a new message for when an order is rejected with no reason.
     *
     * @return a new message
     */
    public static Message orderRejectedNoReason() {
        return messages.getMessage(402);
    }

    /**
     * Creates a new message for when a duplicate order response is received.
     *
     * @param orderId    the order identifier
     * @param responseId the response identfiier
     * @return a new message
     */
    public static Message duplicateOrderResponse(long orderId, String responseId) {
        return messages.getMessage(403, orderId, responseId);
    }

    /**
     * Creates a new message for when an order response cannot be processed.
     *
     * @param reason the reason
     * @return a new message
     */
    public static Message failedToProcessOrderResponse(String reason) {
        return messages.getMessage(500, reason);
    }

    /**
     * Creates a new message for when an invoice line doesn't reference a product.
     *
     * @param invoiceLineId the invoice line identifier
     * @return a new message
     */
    public static Message invoiceNoProduct(String invoiceLineId) {
        return messages.getMessage(601, invoiceLineId);
    }

    /**
     * Creates a new message for when the payable amount on an invoice doesn't match that calculated.
     *
     * @param invoiceId     the invoice identifier
     * @param payableAmount the LegalMonetaryTotal/PayableAmount value
     * @param calculated    the calculated total
     * @return a new message
     */
    public static Message invoiceInvalidPayableAmount(String invoiceId, BigDecimal payableAmount,
                                                      BigDecimal calculated) {
        return messages.getMessage(602, invoiceId, payableAmount, calculated);
    }

    /**
     * Creates a new message for when the line extension amount doesn't match that calculated.
     *
     * @param invoiceId           the invoice identifier
     * @param lineExtensionAmount the LegalMonetaryTotal/LineExtensionAmount value
     * @param calculated          the calculated line extension amount
     * @return a new message
     */
    public static Message invoiceInvalidLineExtensionAmount(String invoiceId, BigDecimal lineExtensionAmount,
                                                            BigDecimal calculated) {
        return messages.getMessage(603, invoiceId, lineExtensionAmount, calculated);
    }

    /**
     * Creates a new message for when the invoice tax doesn't match the tax calculated from the items.
     *
     * @param invoiceId  the invoice identifier.
     * @param tax        the invoice tax
     * @param calculated the calculated tax
     * @return a new message
     */
    public static Message invoiceInvalidTax(String invoiceId, BigDecimal tax, BigDecimal calculated) {
        return messages.getMessage(604, invoiceId, tax, calculated);
    }

    /**
     * Creates a new message for when an InvoiceLine's LineExtensionAmount doesn't match the calculated amount.
     *
     * @param invoiceLineId       the invoice line identifier.
     * @param lineExtensionAmount the InvoiceLine/LineExtensionAmount value
     * @param calculated          the calculated line extension amount
     * @return a new message
     */
    public static Message invoiceLineInvalidLineExtensionAmount(String invoiceLineId, BigDecimal lineExtensionAmount,
                                                                BigDecimal calculated) {
        return messages.getMessage(605, invoiceLineId, lineExtensionAmount, calculated);
    }

    /**
     * Creates a new message for when an invoice line refers to an invalid order item.
     *
     * @param invoiceLineId the invoice identifier
     * @param orderId       the order item identifier
     * @return a new message
     */
    public static Message invoiceInvalidOrderItem(String invoiceLineId, String orderId) {
        return messages.getMessage(606, invoiceLineId, orderId);
    }

    /**
     * Creates a new message for when an invoice contains an allowance. These aren't (yet) supported.
     *
     * @param invoiceId the invoice identifier
     * @return a new message
     */
    public static Message invoiceAllowanceNotSupported(String invoiceId) {
        return messages.getMessage(607, invoiceId);
    }

    /**
     * Creates a new message for when the charge total doesn't match that calculated.
     *
     * @param invoiceId         the invoice identifier
     * @param chargeTotalAmount the LegalMonetaryTotal/ChargeTotalAmount value
     * @param calculated        the calculated charge amount
     * @return a new message
     */
    public static Message invoiceInvalidChargeTotal(String invoiceId, BigDecimal chargeTotalAmount,
                                                    BigDecimal calculated) {
        return messages.getMessage(608, invoiceId, chargeTotalAmount, calculated);
    }

    /**
     * Creates a new message for when a duplicate invoice is received.
     *
     * @param invoiceId  the invoice identifier
     * @param deliveryId the delivery identifier
     * @return a new message
     */
    public static Message duplicateInvoice(String invoiceId, long deliveryId) {
        return messages.getMessage(609, invoiceId, deliveryId);
    }

    /**
     * Creates a new message for when a duplicate invoice for an order is received.
     *
     * @param invoiceId the invoice identifier
     * @param orderId   the order identifier
     * @return a new message
     */
    public static Message duplicateInvoiceForOrder(String invoiceId, long orderId) {
        return messages.getMessage(610, invoiceId, orderId);
    }

    /**
     * Creates a new message for when the tax exclusive amount doesn't match that calculated.
     *
     * @param invoiceId          the invoice identifier
     * @param taxExclusiveAmount the LegalMonetaryTotal/TaxExclusiveAmount value
     * @param calculated         the calculated tax exclusive amount
     * @return a new message
     */
    public static Message invoiceInvalidTaxExclusiveAmount(String invoiceId, BigDecimal taxExclusiveAmount,
                                                           BigDecimal calculated) {
        return messages.getMessage(611, invoiceId, taxExclusiveAmount, calculated);
    }

    /**
     * Creates a new message for when an invoice cannot be processed.
     *
     * @param reason the reason
     * @return a new message
     */
    public static Message failedToProcessInvoice(String reason) {
        return messages.getMessage(700, reason);
    }

    /**
     * Invoked when a document is received that is unsupported.
     *
     * @param supplier  the supplier
     * @param reference the document reference
     * @return a new message
     */
    public static Message unsupportedDocument(Party supplier, DocumentReferenceType reference) {
        String id = (reference.getID()) != null ? reference.getID().getValue() : null;
        String docType = (reference.getDocumentType()) != null ? reference.getDocumentType().getValue() : null;
        return messages.getMessage(800, supplier.getId(), supplier.getName(), id, docType);
    }

    /**
     * Invoked when a document cannot be retrieved from an inbox.
     *
     * @param supplier  the supplier
     * @param reference the document reference
     * @return a new message
     */
    public static Message documentNotFound(Party supplier, DocumentReferenceType reference) {
        String id = (reference.getID()) != null ? reference.getID().getValue() : null;
        String docType = (reference.getDocumentType()) != null ? reference.getDocumentType().getValue() : null;
        return messages.getMessage(801, supplier.getId(), supplier.getName(), id, docType);
    }

    /**
     * Invoked when a document cannot be acknowledged.
     *
     * @param supplier  the supplier
     * @param reference the document reference
     * @return a new message
     */
    public static Message failedToAcknowledgeDocument(Party supplier, DocumentReferenceType reference) {
        String id = (reference.getID()) != null ? reference.getID().getValue() : null;
        String docType = (reference.getDocumentType()) != null ? reference.getDocumentType().getValue() : null;
        return messages.getMessage(802, supplier.getId(), supplier.getName(), id, docType);
    }

}
