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

package org.openvpms.archetype.rules.supplier;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.AndPredicate;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.RefEquals;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.openvpms.component.business.service.archetype.functor.IsActiveRelationship.isActiveNow;


/**
 * Supplier rules.
 *
 * @author Tim Anderson
 */
public class SupplierRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Constructs a {@code SupplierRules}.
     *
     * @param service the archetype service
     */
    public SupplierRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns the referral vet practice for a vet overlapping the specified time.
     *
     * @param vet  the vet
     * @param time the time
     * @return the practice the vet is associated with or {@code null} if the vet is not associated with any practice
     *         for the time frame
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getReferralVetPractice(Party vet, Date time) {
        EntityBean bean = new EntityBean(vet, service);
        return (Party) bean.getNodeSourceEntity("practices", time);
    }

    /**
     * Determines if a supplier supplies a particular product.
     *
     * @param supplier the supplier
     * @param product  the product
     * @return {@code true} if {@code supplier} supplies {@code product}; otherwise {@code false}
     */
    public boolean isSuppliedBy(Party supplier, Product product) {
        EntityBean bean = new EntityBean(supplier, service);
        Predicate predicate = AndPredicate.getInstance(isActiveNow(), RefEquals.getSourceEquals(product));
        return bean.getNodeRelationship("products", predicate) != null;
    }

    /**
     * Returns all active <em>entityRelationship.productSupplier</em>
     * relationships for a particular supplier.
     *
     * @param supplier the supplier
     * @return the relationships, wrapped in {@link ProductSupplier} instances
     */
    public List<ProductSupplier> getProductSuppliers(Party supplier) {
        List<ProductSupplier> result = new ArrayList<ProductSupplier>();
        EntityBean bean = new EntityBean(supplier, service);
        List<EntityRelationship> relationships = bean.getNodeRelationships("products", isActiveNow());
        for (EntityRelationship relationship : relationships) {
            result.add(new ProductSupplier(relationship, service));
        }
        return result;
    }

    /**
     * Returns the <em>entityRelationship.supplierStockLocation*</em> between the supplier and stock location
     * associated with an order.
     *
     * @param order an <em>act.supplierOrder</em>
     * @return the supplier's <em>entityRelationship.supplierStockLocation*</em> or {@code null} if none is found
     */
    public EntityRelationship getSupplierStockLocation(Act order) {
        ActBean bean = new ActBean(order, service);
        Party supplier = (Party) bean.getNodeParticipant("supplier");
        Party stockLocation = (Party) bean.getNodeParticipant("stockLocation");
        return (supplier != null && stockLocation != null) ? getSupplierStockLocation(supplier, stockLocation) : null;
    }

    /**
     * Returns the <em>entityRelationship.supplierStockLocation*</em> between a supplier and stock location.
     *
     * @param supplier      a <em>party.supplier*</em>
     * @param stockLocation a <em>party.organisationStockLocation</em>
     * @return the supplier's <em>entityRelationship.supplierStockLocation*</em> or {@code null} if none is found
     */
    public EntityRelationship getSupplierStockLocation(Party supplier, Party stockLocation) {
        EntityBean bean = new EntityBean(supplier, service);
        Predicate active = AndPredicate.getInstance(isActiveNow(), RefEquals.getTargetEquals(stockLocation));
        List<EntityRelationship> relationships = bean.getNodeRelationships("stockLocations", active);
        return (!relationships.isEmpty()) ? relationships.get(0) : null;
    }

}
