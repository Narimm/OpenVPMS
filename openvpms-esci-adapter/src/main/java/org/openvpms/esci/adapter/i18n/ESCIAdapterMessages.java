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
 * Messages reported by ESCI Adapter.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ESCIAdapterMessages {

    private static Messages messages = new Messages("ESCIA", ESCIAdapterMessages.class.getName());


    public static Message ESCINotConfigured(Party supplier, Party stockLocation) {
        return messages.getMessage(1, supplier.getId(), supplier.getName(),
                                   stockLocation.getId(), stockLocation.getName());
    }

    public static Message invalidSupplierServiceLocatorConfig(String shortName) {
        return messages.getMessage(2, shortName);

    }

    public static Message invalidSupplierURL(Party supplier, String serviceURL) {
        return messages.getMessage(3, supplier.getId(), supplier.getName(), serviceURL);
    }

    public static Message invalidServiceURL(String serviceURL) {
        return messages.getMessage(4, serviceURL);
    }

    public static Message noProductSupplierRelationship(Party supplier, Product product) {
        return messages.getMessage(5, supplier.getName(), product.getName());
    }

    public static Message noSupplierOrderCode(Party supplier, Product product) {
        return messages.getMessage(6, supplier.getName(), product.getName());
    }

    public static Message invoiceElementRequired(String path, String parent, String id) {
        return messages.getMessage(100, path, parent, id);
    }

    public static Message invoiceInvalidCardinality(String path, String parent, String id, String expected,
                                                    int actual) {
        return messages.getMessage(101, path, parent, id, expected, actual);
    }

    public static Message invoiceInvalidIdentifier(String path, String parent, String parentId, String value) {
        return messages.getMessage(102, path, parent, parentId, value);
    }

    public static Message invoiceInvalidSupplier(String path, String invoiceId, String supplierId) {
        return messages.getMessage(103, path, invoiceId, supplierId);
    }

    public static Message invoiceInvalidStockLocation(String path, String invoiceId, String stockLocationId) {
        return messages.getMessage(104, path, invoiceId, stockLocationId);
    }

    public static Message invoiceNoProduct(String invoiceLineId) {
        return messages.getMessage(105, invoiceLineId);
    }

    public static Message invoiceInvalidPayableAmount(String invoiceId, BigDecimal expected, BigDecimal actual) {
        return messages.getMessage(106, invoiceId, expected, actual);
    }

    public static Message invoiceInvalidLineExtensionAmount(String invoiceId, BigDecimal expected, BigDecimal actual) {
        return messages.getMessage(107, invoiceId, expected, actual);
    }

    public static Message invoiceInvalidTax(String invoiceId, BigDecimal expected, BigDecimal actual) {
        return messages.getMessage(108, invoiceId, expected, actual);
    }

    public static Message invoiceLineInvalidLineExtensionAmount(String invoiceLineId, BigDecimal expected,
                                                                BigDecimal actual) {
        return messages.getMessage(109, invoiceLineId, expected, actual);
    }

    public static Message invoiceInvalidCurrency(String path, String parent, String parentId, String expected,
                                                 String actual) {
        return messages.getMessage(110, path, parent, parentId, expected, actual);
    }

    public static Message invoiceUnitCodeMismatch(String invoiceLineId, String invoicedUnitCode,
                                                  String baseQuantityUnitCode) {
        return messages.getMessage(111, invoiceLineId, invoicedUnitCode, baseQuantityUnitCode);
    }

}
