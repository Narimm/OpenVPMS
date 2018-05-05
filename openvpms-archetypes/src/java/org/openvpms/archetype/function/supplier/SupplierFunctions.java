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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.function.supplier;

import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.component.business.domain.im.party.Party;

import java.util.List;

/**
 * Supplier functions for use in xpath expressions.
 *
 * @author Tim Anderson
 */
public class SupplierFunctions {

    /**
     * The supplier rules.
     */
    private final SupplierRules rules;

    /**
     * Constructs a {@link SupplierFunctions}.
     *
     * @param rules the supplier rules
     */
    public SupplierFunctions(SupplierRules rules) {
        this.rules = rules;
    }

    /**
     * Returns the account id for a supplier - practice location relationship.
     *
     * @param supplier the supplier. This may be its identifier, or a {@link Party} object. May be {@code null}
     * @param location the practice location
     * @return the account id, or {@code null} if no relationship exists
     */
    public String accountId(Object supplier, Party location) {
        // ideally, SupplierFunctions would define overloaded methods for each accountId() form except that JXPath
        // can't determine which one to invoke in the case where a null is supplied.
        supplier = unwrap(supplier);
        if (supplier != null && location != null) {
            if (supplier instanceof Party) {
                return rules.getAccountId((Party) supplier, location);
            } else if (supplier instanceof Number) {
                return rules.getAccountId(((Number) supplier).longValue(), location);
            }
        }
        return null;
    }

    /**
     * Helper to get access to the actual object supplied by JXPath.
     *
     * @param object the object to unwrap
     * @return the unwrapped object. May be {@code null}
     */
    private Object unwrap(Object object) {
        if (object instanceof List) {
            List values = (List) object;
            object = !values.isEmpty() ? values.get(0) : null;
        }
        return object;
    }
}
