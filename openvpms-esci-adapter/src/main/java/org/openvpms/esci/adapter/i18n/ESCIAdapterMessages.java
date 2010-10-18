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
        return messages.getMessage(3, supplier.getId(), supplier.getName(), serviceURL);
    }

    /**
     * Creates a new message for an invalid service URL.
     *
     * @param serviceURL the invalid service URL
     * @return a new message
     */
    public static Message invalidServiceURL(String serviceURL) {
        return messages.getMessage(4, serviceURL);
    }

    /**
     * Creates a new message for when there is no relationship between a supplier and product.
     *
     * @param supplier the supplier
     * @param product  the product
     * @return a new message
     */
    public static Message noProductSupplierRelationship(Party supplier, Product product) {
        return messages.getMessage(5, supplier.getId(), supplier.getName(), product.getId(), product.getName());
    }

    /**
     * Creates a new message for when there is no supplier order code associated with a product.
     *
     * @param supplier the supplier
     * @param product  the product
     * @return a new message
     */
    public static Message noSupplierOrderCode(Party supplier, Product product) {
        return messages.getMessage(6, supplier.getId(), supplier.getName(), product.getId(), product.getName());
    }

    /**
     * Creates a new message for when no ESCI user can be determined from the current context.
     *
     * @return a new message
     */
    public static Message noESCIUser() {
        return messages.getMessage(7);
    }

    /**
     * Creates a new message for when a user has no relationship to a supplier.
     *
     * @param user     the ESCI user
     * @param supplier the supplier
     * @return a new message
     */
    public static Message userNotLinkedToSupplier(User user, Party supplier) {
        return messages.getMessage(8, user.getId(), user.getName(), supplier.getId(), supplier.getName());
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
     * Creates a new message for an invalid supplier referenced by an invoice.
     *
     * @param path       the path to the element
     * @param invoiceId  the invoice identifier
     * @param supplierId the supplier identifier
     * @return a new message
     */
    public static Message invoiceInvalidSupplier(String path, String invoiceId, String supplierId) {
        return messages.getMessage(300, path, invoiceId, supplierId);
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
        return messages.getMessage(301, path, invoiceId, stockLocationId);
    }

    /**
     * Creates a new message for when an invoice line doesn't reference a product.
     *
     * @param invoiceLineId the invoice line identifier
     * @return a new message
     */
    public static Message invoiceNoProduct(String invoiceLineId) {
        return messages.getMessage(302, invoiceLineId);
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
        return messages.getMessage(303, invoiceId, payableAmount, calculated);
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
        return messages.getMessage(304, invoiceId, lineExtensionAmount, calculated);
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
        return messages.getMessage(305, invoiceId, tax, calculated);
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
        return messages.getMessage(306, invoiceLineId, lineExtensionAmount, calculated);
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
    public static Message invoiceInvalidCurrency(String path, String parent, String parentId, String expected,
                                                 String actual) {
        return messages.getMessage(307, path, parent, parentId, expected, actual);
    }

    /**
     * Creates a new message for when an invoice line specifies both an InvoicedQuantity unit code and a BaseQuantity
     * unit code, and they are different.
     *
     * @param invoiceLineId        the invoice line identifier
     * @param invoicedUnitCode     the InvoicedQuantity unit code
     * @param baseQuantityUnitCode the BaseQuantity unit code
     * @return a new message
     */
    public static Message invoiceUnitCodeMismatch(String invoiceLineId, String invoicedUnitCode,
                                                  String baseQuantityUnitCode) {
        return messages.getMessage(308, invoiceLineId, invoicedUnitCode, baseQuantityUnitCode);
    }

    /**
     * Creates a new message for when an invoice refers to an invalid order.
     *
     * @param invoiceId the invoice identifier
     * @param orderId   the order identifier
     * @return a new message
     */
    public static Message invoiceInvalidOrder(String invoiceId, String orderId) {
        return messages.getMessage(309, invoiceId, orderId);
    }

    /**
     * Creates a new message for when an invoice line refers to an invalid order item.
     *
     * @param invoiceLineId the invoice identifier
     * @param orderId       the order item identifier
     * @return a new message
     */
    public static Message invoiceInvalidOrderItem(String invoiceLineId, String orderId) {
        return messages.getMessage(310, invoiceLineId, orderId);
    }

    /**
     * Creates a new message for when an invoice cannot be submitted to OpenVPMS.
     *
     * @param reason the reason
     * @return a new message
     */
    public static Message failedToSubmitInvoice(String reason) {
        return messages.getMessage(400, reason);
    }

}
