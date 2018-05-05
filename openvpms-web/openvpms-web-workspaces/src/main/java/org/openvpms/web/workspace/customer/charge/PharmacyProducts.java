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

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.cache.IMObjectCache;
import org.openvpms.hl7.pharmacy.Pharmacies;
import org.openvpms.hl7.util.HL7Archetypes;

/**
 * Helper to return the pharmacies that dispense products for a practice location.
 *
 * @author Tim Anderson
 */
class PharmacyProducts {

    /**
     * The pharmacies.
     */
    private final Pharmacies pharmacies;

    /**
     * The location.
     */
    private final Party location;

    /**
     * The cache.
     */
    private final IMObjectCache cache;


    /**
     * Constructs a {@link PharmacyProducts}.
     *
     * @param pharmacies the pharmacies
     * @param location   the practice location
     * @param cache      the cache
     */
    public PharmacyProducts(Pharmacies pharmacies, Party location, IMObjectCache cache) {
        this.pharmacies = pharmacies;
        this.location = location;
        this.cache = cache;
    }

    /**
     * Returns the pharmacy for a product and location.
     *
     * @param product the product. May be {@code null}
     * @return the pharmacy, or {@code null} if none is present
     */
    public Entity getPharmacy(Product product) {
        Entity pharmacy = null;
        if (TypeHelper.isA(product, ProductArchetypes.MEDICATION, ProductArchetypes.MERCHANDISE)) {
            IMObjectBean bean = new IMObjectBean(product);
            pharmacy = (Entity) cache.get(bean.getNodeTargetObjectRef("pharmacy"));
            if (pharmacy == null) {
                // use the pharmacy linked to the product type, if present
                Entity type = (Entity) cache.get(bean.getNodeTargetObjectRef("type"));
                if (type != null) {
                    IMObjectBean typeBean = new IMObjectBean(type);
                    pharmacy = (Entity) cache.get(typeBean.getNodeTargetObjectRef("pharmacy"));
                }
            }
            if (pharmacy != null && TypeHelper.isA(pharmacy, HL7Archetypes.PHARMACY_GROUP)) {
                pharmacy = pharmacies.getService(pharmacy, location.getObjectReference());
            }
        }
        return pharmacy;
    }

}
