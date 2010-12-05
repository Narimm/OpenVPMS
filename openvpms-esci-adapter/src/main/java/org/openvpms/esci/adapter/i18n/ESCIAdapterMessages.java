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

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;

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
     * Creates a new message for when a user has no relationship to a supplier.
     *
     * @param user     the ESCI user
     * @param supplier the supplier
     * @return a new message
     */
    public static Message userNotLinkedToSupplier(User user, Party supplier) {
        return messages.getMessage(109, user.getId(), user.getName(), supplier.getId(), supplier.getName());
    }

    /**
     * Creates a new message for when a tax scheme and category don't correspond to an <em>loookup.taxType</em>.
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
     * Creates a new message for when no ESCI user can be determined from the current context.
     *
     * @return a new message
     */
    public static Message noESCIUser() {
        return messages.getMessage(200);
    }

    /**
     * Creates a new message for when there is no relationship between a supplier and product.
     *
     * @param supplier the supplier
     * @param product  the product
     * @return a new message
     */
    public static Message noProductSupplierRelationship(Party supplier, Product product) {
        return messages.getMessage(300, supplier.getId(), supplier.getName(), product.getId(), product.getName());
    }

    /**
     * Creates a new message for when there is no supplier order code associated with a product.
     *
     * @param supplier the supplier
     * @param product  the product
     * @return a new message
     */
    public static Message noSupplierOrderCode(Party supplier, Product product) {
        return messages.getMessage(301, supplier.getId(), supplier.getName(), product.getId(), product.getName());
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
     * Creates a new message for when an order response cannot be submitted to OpenVPMS.
     *
     * @param reason the reason
     * @return a new message
     */
    public static Message failedToSubmitOrderResponse(String reason) {
        return messages.getMessage(500, reason);
    }

    /**
     * Creates a new message for an invalid stock location referenced by an invoice.
     *
     * @param path            the path to the element
     * @param invoiceId       the invoice identifier
     * @param stockLocationId the stock location identifier
     * @return a new message
     */
    public static Message invoiceInvalidStockLocation(String path, String invoiceId, String stockLocationId) {
        return messages.getMessage(600, path, invoiceId, stockLocationId);
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
     * Creates a new message for when a duplicate invoice is received for an order.
     *
     * @param invoiceId the invoice identifier
     * @param orderId   the order identifier
     * @return a new message
     */
    public static Message duplicateInvoice(String invoiceId, long orderId) {
        return messages.getMessage(609, invoiceId, orderId);
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
        return messages.getMessage(610, invoiceId, taxExclusiveAmount, calculated);
    }

    /**
     * Creates a new message for when an invoice cannot be submitted to OpenVPMS.
     *
     * @param reason the reason
     * @return a new message
     */
    public static Message failedToSubmitInvoice(String reason) {
        return messages.getMessage(700, reason);
    }

}