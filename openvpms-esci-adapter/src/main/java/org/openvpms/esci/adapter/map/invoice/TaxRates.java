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
package org.openvpms.esci.adapter.map.invoice;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.business.service.lookup.ILookupService;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper to cache tax rates based on their scheme and category.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class TaxRates {

    /**
     * The tax rates, keyed on the concatentation of the scheme and category IDs.
     */
    private Map<String, BigDecimal> cache = new HashMap<String, BigDecimal>();

    /**
     * The lookup service.
     */
    private final ILookupService service;

    /**
     * The bean factory.
     */
    private final IMObjectBeanFactory factory;


    /**
     * Constructs a <tt>TaxRates</tt>.
     *
     * @param service the lookup service
     * @param factory the bean factory
     */
    public TaxRates(ILookupService service, IMObjectBeanFactory factory) {
        this.service = service;
        this.factory = factory;

    }

    /**
     * Returns the tax rate from the <em>lookup.taxType</em> for a given tax scheme and category.
     *
     * @param scheme   the scheme
     * @param category the category
     * @return the tax rate, or <tt>null</tt> if no corresponding <em>lookup.taxType</em> was found
     */
    public BigDecimal getTaxRate(String scheme, String category) {
        String key = scheme + "-" + category;
        BigDecimal result = cache.get(key);
        if (result == null) {
            Collection<Lookup> lookups = service.getLookups("lookup.taxType");
            for (Lookup lookup : lookups) {
                IMObjectBean bean = factory.createBean(lookup);
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
