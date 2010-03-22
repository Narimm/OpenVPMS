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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.supplier;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.AndPredicate;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.IsActiveRelationship;
import org.openvpms.component.business.service.archetype.functor.RefEquals;
import org.openvpms.component.business.service.archetype.helper.EntityBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Supplier rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SupplierRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Constructs a new <tt>SupplierRules</tt>.
     *
     * @throws ArchetypeServiceException if the archetype service is not
     *                                   configured
     */
    public SupplierRules() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Construct a new <tt>SupplierRules/code>.
     *
     * @param service the archetype service
     */
    public SupplierRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returhs the referral vet practice for a vet overlapping the specified
     * time.
     *
     * @param vet  the vet
     * @param time the time
     * @return the practice the vet is associated with or <tt>null</tt> if
     *         the vet is not associated with any practice for the time frame
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
     * @return <tt>true</tt> if <tt>supplier</tt> supplies <tt>product</tt>; otherwise <tt>false</tt>
     */
    public boolean isSuppliedBy(Party supplier, Product product) {
        EntityBean bean = new EntityBean(supplier, service);
        Predicate predicate = AndPredicate.getInstance(
                IsActiveRelationship.ACTIVE_NOW,
                RefEquals.getSourceEquals(product));
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
        List<EntityRelationship> relationships
                = bean.getNodeRelationships("products",
                                            IsActiveRelationship.ACTIVE_NOW);
        for (EntityRelationship relationship : relationships) {
            result.add(new ProductSupplier(relationship, service));
        }
        return result;
    }

}
