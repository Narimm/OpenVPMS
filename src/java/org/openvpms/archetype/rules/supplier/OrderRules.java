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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.supplier;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.AndPredicate;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import static org.openvpms.component.business.service.archetype.helper.EntityBean.RefEquals;

import java.util.ArrayList;
import java.util.List;


/**
 * Supplier Order rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OrderRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Creates a new <tt>OrderRules</tt>.
     */
    public OrderRules() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>OrderRules</tt>.
     *
     * @param service the archetype service
     */
    public OrderRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Determines if a supplier supplies a particular product.
     *
     * @param supplier the supplier
     * @param product  the product
     */
    public boolean isSuppliedBy(Party supplier, Product product) {
        EntityBean bean = new EntityBean(supplier, service);
        Predicate predicate = AndPredicate.getInstance(
                EntityBean.ACTIVE, RefEquals.getSourceEquals(product));
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
                = bean.getNodeRelationships("products", EntityBean.ACTIVE);
        for (EntityRelationship relationship : relationships) {
            result.add(new ProductSupplier(relationship, service));
        }
        return result;
    }

    /**
     * Returns all active <em>entityRelationship.productSupplier</em>
     * relationships for a particular supplier and product.
     *
     * @param product  the product
     * @param supplier the supplier
     * @return the relationships, wrapped in {@link ProductSupplier} instances
     */
    public List<ProductSupplier> getProductSuppliers(Product product,
                                                     Party supplier) {
        List<ProductSupplier> result = new ArrayList<ProductSupplier>();
        EntityBean bean = new EntityBean(supplier, service);
        Predicate predicate = AndPredicate.getInstance(
                EntityBean.ACTIVE, RefEquals.getSourceEquals(product));
        List<EntityRelationship> relationships
                = bean.getNodeRelationships("products", predicate);
        for (EntityRelationship relationship : relationships) {
            result.add(new ProductSupplier(relationship, service));
        }
        return result;
    }

    /**
     * Returns an <em>entityRelationship.productSupplier</em> relationship
     * for a supplier, product and package size and units.
     *
     * @param product      the product
     * @param supplier     the supplier
     * @param packageSize  the package size
     * @param packageUnits the package units
     * @return the relationship, wrapped in a {@link ProductSupplier}, or
     *         <tt>null</tt> if none is found
     */
    public ProductSupplier getProductSupplier(Product product, Party supplier,
                                              int packageSize,
                                              String packageUnits) {
        for (ProductSupplier ps : getProductSuppliers(product, supplier)) {
            if (ps.getPackageSize() == packageSize
                    && ObjectUtils.equals(ps.getPackageUnits(),
                                          packageUnits)) {
                return ps;
            }
        }
        return null;
    }

    /**
     * Returns all active <em>entityRelationship.productSupplier</em>
     * relationships for a particular product.
     *
     * @param product the product
     * @return the relationships, wrapped in {@link ProductSupplier} instances
     */
    public List<ProductSupplier> getProductSuppliers(Product product) {
        List<ProductSupplier> result = new ArrayList<ProductSupplier>();
        EntityBean bean = new EntityBean(product, service);
        List<EntityRelationship> relationships
                = bean.getNodeRelationships("suppliers", EntityBean.ACTIVE);
        for (EntityRelationship relationship : relationships) {
            result.add(new ProductSupplier(relationship, service));
        }
        return result;
    }

    /**
     * Creates a new <em>entityRelationship.productSupplier</em> relationship
     * between a supplier and product.
     *
     * @param product  the product
     * @param supplier the supplier
     * @return the relationship, wrapped in a {@link ProductSupplier}
     */
    public ProductSupplier createProductSupplier(Product product,
                                                 Party supplier) {

        EntityBean bean = new EntityBean(product, service);
        EntityRelationship rel = bean.addRelationship(
                "entityRelationship.productSupplier", supplier);
        return new ProductSupplier(rel, service);
    }

}
