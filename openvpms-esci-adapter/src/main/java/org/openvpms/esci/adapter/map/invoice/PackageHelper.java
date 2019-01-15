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

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.product.ProductRules;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.lookup.Lookup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Maps unit of measure codes to package units.
 *
 * @author Tim Anderson
 */
class PackageHelper {

    /**
     * Product rules.
     */
    private final ProductRules rules;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * Cache of lookup.uom codes to unit codes.
     */
    private Map<String, String> unitCodes;

    /**
     * Constructs a {@link PackageHelper}.
     *
     * @param rules   the product rules
     * @param service the archetype service
     * @param lookups the lookup service
     */
    public PackageHelper(ProductRules rules, IArchetypeService service, ILookupService lookups) {
        this.rules = rules;
        this.lookups = lookups;
        this.service = service;
    }

    /**
     * Tries to determine the package information give the order, product and supplier.
     *
     * @param orderItem the order item. May be {@code null}
     * @param product   the product. May be {@code null}
     * @param supplier  the supplier
     * @return the package information, or {@code null} if none is available
     */
    public Package getPackage(FinancialAct orderItem, Product product, Party supplier) {
        Package result = null;
        int packageSize = 0;
        String packageUnits = null;
        if (orderItem != null) {
            IMObjectBean bean = service.getBean(orderItem);
            packageSize = bean.getInt("packageSize");
            packageUnits = bean.getString("packageUnits");
        }
        if (packageSize != 0 && !StringUtils.isEmpty(packageUnits)) {
            result = new Package(packageSize, packageUnits);
        } else if (product != null) {
            List<ProductSupplier> list = rules.getProductSuppliers(product, supplier);
            if (list.size() == 1) {
                ProductSupplier ps = list.get(0);
                result = new Package(ps);
            }
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
        List<String> result = new ArrayList<>();
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
     * Returns a map of package unit codes to their corresponding UN/ECE codes.
     *
     * @return the map
     */
    private Map<String, String> getUnitCodes() {
        Map<String, String> result = new HashMap<>();
        for (Lookup lookup : lookups.getLookups("lookup.uom")) {
            IMObjectBean lookupBean = service.getBean(lookup);
            String unitCode = lookupBean.getString("unitCode");
            if (unitCode != null) {
                result.put(lookup.getCode(), unitCode);
            }
        }
        return result;
    }


}
