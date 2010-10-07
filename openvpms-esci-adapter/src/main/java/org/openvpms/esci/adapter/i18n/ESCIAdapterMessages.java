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

import java.math.BigDecimal;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ESCIAdapterMessages {

    public static int ESCI_NOT_CONFIGURED = 1;

    public static int INVALID_SUPPLIER_SERVICE_LOCATOR_CONFIG = 2;

    public static int INVALID_SUPPLIER_SERVICE_URL = 3;

    public static int INVALID_SERVICE_URL = 4;

    public static int NO_PRODUCT_SUPPLIER = 5;

    public static int NO_SUPPLIER_ORDER_CODE = 6;

    public static int INVOICE_ELEMENT_REQUIRED = 100;

    public static int INVOICE_INVALID_CARDINALITY = 101;

    public static int INVOICE_INVALID_IDENTIFIER = 102;

    public static int INVOICE_INVALID_SUPPLIER = 103;

    public static int INVOICE_INVALID_STOCK_LOCATION = 104;

    public static int INVOICE_INVALID_PRODUCT = 105;

    public static int INVOICE_INVALID_PAYABLE_AMOUNT = 106;

    public static int INVOICE_INVALID_LINE_EXTENSION_AMOUNT = 107;

    public static int INVOICE_INVALID_TAX = 108;

    public static int INVOICE_INVALID_CURRENCY = 109;


    private static Messages messages = new Messages("EAD", ESCIAdapterMessages.class.getName());


    public static Message ESCINotConfigured(Party supplier, Party stockLocation) {
        return messages.getMessage(ESCI_NOT_CONFIGURED, supplier.getId(), supplier.getName(),
                                   stockLocation.getId(), stockLocation.getName());
    }

    public static Message invalidSupplierServiceLocatorConfig(String shortName) {
        return messages.getMessage(INVALID_SUPPLIER_SERVICE_LOCATOR_CONFIG, shortName);

    }

    public static Message invalidSupplierURL(Party supplier, String serviceURL) {
        return messages.getMessage(INVALID_SUPPLIER_SERVICE_URL, supplier.getId(), supplier.getName(), serviceURL);
    }

    public static Message invalidServiceURL(String serviceURL) {
        return messages.getMessage(INVALID_SERVICE_URL, serviceURL);
    }

    public static Message noProductSupplierRelationship(Party supplier, Product product) {
        return messages.getMessage(NO_PRODUCT_SUPPLIER, supplier.getName(), product.getName());
    }

    public static Message noSupplierOrderCode(Party supplier, Product product) {
        return messages.getMessage(NO_SUPPLIER_ORDER_CODE, supplier.getName(), product.getName());
    }

    public static Message invoiceElementRequired(String path, String parent, String id) {
        return messages.getMessage(INVOICE_ELEMENT_REQUIRED, path, parent, id);
    }

    public static Message invoiceInvalidCardinality(String path, String parent, String id, String expected,
                                                    int actual) {
        return messages.getMessage(INVOICE_INVALID_CARDINALITY, path, parent, id, expected, actual);
    }

    public static Message invoiceInvalidIdentifier(String path, String parent, String parentId, String value) {
        return messages.getMessage(INVOICE_INVALID_IDENTIFIER, path, parent, parentId, value);
    }

    public static Message invoiceInvalidSupplier(String path, String invoiceId, String supplierId) {
        return messages.getMessage(INVOICE_INVALID_PRODUCT, path, invoiceId, supplierId);
    }

    public static Message invoiceInvalidStockLocation(String path, String invoiceId, String stockLocationId) {
        return messages.getMessage(INVOICE_INVALID_STOCK_LOCATION, path, invoiceId, stockLocationId);
    }

    public static Message invoiceLineInvalidProduct(String path, String invoiceLineId, String productId) {
        return messages.getMessage(INVOICE_INVALID_PRODUCT, path, invoiceLineId, productId);
    }

    public static Message invoiceInvalidPayableAmount(String invoiceId, BigDecimal expected, BigDecimal actual) {
        return messages.getMessage(INVOICE_INVALID_PAYABLE_AMOUNT, invoiceId, expected, actual);
    }

    public static Message invoiceInvalidLineExtensionAmount(String invoiceId, BigDecimal expected, BigDecimal actual) {
        return messages.getMessage(INVOICE_INVALID_LINE_EXTENSION_AMOUNT, invoiceId, expected, actual);
    }

    public static Message invoiceInvalidTax(String invoiceId, BigDecimal expected, BigDecimal actual) {
        return messages.getMessage(INVOICE_INVALID_TAX, invoiceId, expected, actual);
    }

    public static Message invalidCurrency(String path, String parent, String parentId, String expected, String actual) {
        return messages.getMessage(INVOICE_INVALID_CURRENCY, path, parent, parentId, expected, actual);
    }

}
