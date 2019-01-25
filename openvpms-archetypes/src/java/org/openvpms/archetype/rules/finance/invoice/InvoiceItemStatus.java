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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.invoice;

/**
 * Invoice item status.
 *
 * @author Tim Anderson
 */
public class InvoiceItemStatus {

    /**
     * Indicates that a pharmacy order has been placed for invoice item.
     */
    public static final String ORDERED = "ORDERED";

    /**
     * Indicates that notification has been sent to pharmacy services that dispensing should no longer be performed
     * for an ordered item.
     */
    public static final String DISCONTINUED = "DISCONTINUED";
}
