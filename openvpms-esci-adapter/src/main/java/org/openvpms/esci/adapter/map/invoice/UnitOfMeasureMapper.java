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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.esci.adapter.map.invoice;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.product.ProductRules;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.business.service.lookup.ILookupService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Maps unit of measure codes to package units.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
class UnitOfMeasureMapper {

    /**
     * Cache of lookup.uom codes to unit codes.
     */
    private Map<String, String> unitCodes;

    /**
     * Product rules.
     */
    private final ProductRules rules;

    /**
     * The lookup service.
     */
    private final ILookupService service;

    /**
     * The bean factory.
     */
    private final IMObjectBeanFactory factory;


    /**
     * Constructs a <tt>UnitOfMeasureMapper</tt>.
     *
     * @param rules   the product rules
     * @param service the lookup service
     * @param factory the bean factory
     */
    public UnitOfMeasureMapper(ProductRules rules, ILookupService service, IMObjectBeanFactory factory) {
        this.rules = rules;
        this.service = service;
        this.factory = factory;
    }

    /**
     * Returns package units matching the supplier unit of measure code.
     * <p/>
     * This takes into account any existing product/supplier relationship with a matching unit code and package size,
     * where multiple matches exist.
     * <p/>
     * If there is more than one match, the resulting list will be sorted on package unit code.
     *
     * @param unitCode    the unit of measure code
     * @param product     the product. May be <tt>null</tt>
     * @param supplier    the supplier
     * @return matching package units
     */
    public List<String> getPackageUnits(String unitCode, Product product, Party supplier) {
        List<String> result;
        List<String> matches = getPackageUnits(unitCode);
        if (!matches.isEmpty() && product != null) {
            List<ProductSupplier> productSuppliers = rules.getProductSuppliers(product, supplier);
            if (!productSuppliers.isEmpty()) {
                result = getPackageUnits(productSuppliers, matches);
            } else {
                // no product supplier relationship
                result = matches;
            }
        } else {
            result = matches;
        }
        return result;
    }

    /**
     * Returns the package units corresponding to a unit code.
     * <p/>
     * If there is more than one match, the resulting list will be sorted on package unit code.
     *
     * @param unitCode the unit code
     * @return the matching package units
     */
    public List<String> getPackageUnits(String unitCode) {
        List<String> result = new ArrayList<String>();
        if (unitCodes == null) {
            unitCodes = getUnitCodes();
        }
        for (Map.Entry<String, String> entry : unitCodes.entrySet()) {
            if (StringUtils.equals(entry.getValue(), unitCode)) {
                result.add(entry.getKey());
            }
        }
        if (result.size() > 1) {
            Collections.sort(result);
        }
        return result;
    }

    /**
     * Tries to find a package unit matching the supplied packageUnits, in a list of product/supplier
     * relationships.
     *
     * @param productSuppliers the product/supplier relationships
     * @param packageUnits     the package units
     * @return a list containing the single match, or <tt>packageUnits</tt> if there are no matches
     */
    private List<String> getPackageUnits(List<ProductSupplier> productSuppliers, List<String> packageUnits) {
        for (ProductSupplier ps : productSuppliers) {
            for (String units : packageUnits) {
                if (StringUtils.equals(ps.getPackageUnits(), units)) {
                    return Arrays.asList(units);
                }
            }
        }
        return packageUnits;
    }

    /**
     * Returns a map of package unit codes to their corresponding UN/ECE codes.
     *
     * @return the map
     */
    private Map<String, String> getUnitCodes() {
        Map<String, String> result = new HashMap<String, String>();
        Collection<Lookup> lookups = service.getLookups("lookup.uom");
        for (Lookup lookup : lookups) {
            IMObjectBean lookupBean = factory.createBean(lookup);
            String unitCode = lookupBean.getString("unitCode");
            if (unitCode != null) {
                result.put(lookup.getCode(), unitCode);
            }
        }
        return result;
    }

}
