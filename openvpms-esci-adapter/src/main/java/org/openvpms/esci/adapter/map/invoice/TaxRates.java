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
package org.openvpms.esci.adapter.map.invoice;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.lookup.Lookup;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper to cache tax rates based on their scheme and category.
 *
 * @author Tim Anderson
 */
class TaxRates {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The tax rates, keyed on the concatenation of the scheme and category IDs.
     */
    private Map<String, BigDecimal> cache = new HashMap<>();


    /**
     * Constructs a {@link TaxRates}.
     *
     * @param service the archetype service
     * @param lookups the lookup service
     */
    public TaxRates(IArchetypeService service, ILookupService lookups) {
        this.service = service;
        this.lookups = lookups;

    }

    /**
     * Returns the tax rate from the <em>lookup.taxType</em> for a given tax scheme and category.
     *
     * @param scheme   the scheme
     * @param category the category
     * @return the tax rate, or {@code null} if no corresponding <em>lookup.taxType</em> was found
     */
    public BigDecimal getTaxRate(String scheme, String category) {
        String key = scheme + "-" + category;
        BigDecimal result = cache.get(key);
        if (result == null) {
            for (Lookup lookup : lookups.getLookups("lookup.taxType")) {
                IMObjectBean bean = service.getBean(lookup);
                if (ObjectUtils.equals(scheme, bean.getValue("taxScheme"))
                    && ObjectUtils.equals(category, bean.getValue("taxCategory"))) {
                    result = bean.getBigDecimal("rate");
                    cache.put(key, result);
                    break;
                }
            }
        }
        return result;
    }

}
