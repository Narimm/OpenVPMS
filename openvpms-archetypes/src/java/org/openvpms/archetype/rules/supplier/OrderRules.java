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
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.IsActiveRelationship;
import org.openvpms.component.business.service.archetype.functor.RefEquals;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
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
        Predicate predicate = RefEquals.getSourceEquals(product);
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

    /**
     * Returns all active <em>entityRelationship.productSupplier</em>
     * relationships for a particular supplier and product.
     *
     * @param supplier the supplier
     * @param product  the product
     * @return the relationships, wrapped in {@link ProductSupplier} instances
     */
    public List<ProductSupplier> getProductSuppliers(Party supplier,
                                                     Product product) {
        List<ProductSupplier> result = new ArrayList<ProductSupplier>();
        EntityBean bean = new EntityBean(supplier, service);
        Predicate predicate = AndPredicate.getInstance(
                IsActiveRelationship.ACTIVE_NOW,
                RefEquals.getSourceEquals(product));
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
     * <p/>
     * If there is a match on supplier and product, but no match on package
     * size, but there is a relationship where the size is <tt>0</tt>, then
     * this will be returned.
     *
     * @param supplier     the supplier
     * @param product      the product
     * @param packageSize  the package size
     * @param packageUnits the package units
     * @return the relationship, wrapped in a {@link ProductSupplier}, or
     *         <tt>null</tt> if none is found
     */
    public ProductSupplier getProductSupplier(Party supplier, Product product,
                                              int packageSize,
                                              String packageUnits) {
        for (ProductSupplier ps : getProductSuppliers(supplier, product)) {
            if (ps.getPackageSize() == packageSize
                    && ObjectUtils.equals(ps.getPackageUnits(),
                                          packageUnits)) {
                return ps;
            } else if (ps.getPackageSize() == 0) {
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
                = bean.getNodeRelationships("suppliers",
                                            IsActiveRelationship.ACTIVE_NOW);
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

    /**
     * Determines the delivery status of an order item.
     *
     * @param orderItem an <em>act.supplierOrderItem</em>
     * @return the delivery status
     */
    public DeliveryStatus getDeliveryStatus(FinancialAct orderItem) {
        DeliveryStatus result = DeliveryStatus.PENDING;
        IMObjectBean bean = new IMObjectBean(orderItem, service);
        BigDecimal quantity = bean.getBigDecimal("quantity", BigDecimal.ZERO);
        BigDecimal received = bean.getBigDecimal("receivedQuantity",
                                                 BigDecimal.ZERO);
        BigDecimal cancelled = bean.getBigDecimal("cancelledQuantity",
                                                  BigDecimal.ZERO);
        if (quantity.compareTo(BigDecimal.ZERO) != 0) {
            // can only be PART or FULL delivery status if there is an expected
            // quantity
            BigDecimal sum = received.add(cancelled);
            if (sum.compareTo(BigDecimal.ZERO) != 0) {
                int status = sum.compareTo(quantity);
                if (status == -1) {
                    if (received.compareTo(BigDecimal.ZERO) != 0) {
                        result = DeliveryStatus.PART;
                    }
                } else if (status == 0) {
                    result = DeliveryStatus.FULL;
                } else {
                    // the sum should never be greater than the quantity
                }
            }
        }
        return result;
    }

}
