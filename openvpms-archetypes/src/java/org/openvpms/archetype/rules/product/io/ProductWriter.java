/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.product.io;

import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.product.Product;

import java.util.Date;
import java.util.Iterator;


/**
 * Writes product data.
 *
 * @author Tim Anderson
 */
public interface ProductWriter {

    /**
     * Writes product data to a document.
     *
     * @param products the products to write
     * @param latest   if {@code true}, output the latest price, else output all prices
     * @return the document
     */
    Document write(Iterator<Product> products, boolean latest);

    /**
     * Writes product data to a document.
     * <p/>
     * This writes prices active within a date range
     *
     * @param products the products to write
     * @param from     the price start date. May be {@code null}
     * @param to       the price end date. May be {@code null}
     * @return the document
     */
    Document write(Iterator<Product> products, Date from, Date to);

}
