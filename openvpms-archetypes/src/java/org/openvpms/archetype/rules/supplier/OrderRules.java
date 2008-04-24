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
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.IsActiveRelationship;
import org.openvpms.component.business.service.archetype.functor.RefEquals;
import org.openvpms.component.business.service.archetype.helper.AbstractIMObjectCopyHandler;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;

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

    /**
     * Creates a new delivery item from an order item.
     *
     * @param orderItem the order item
     * @return a new delivery item
     */
    public FinancialAct createDeliveryItem(FinancialAct orderItem) {
        IMObjectCopier copier = new IMObjectCopier(new DeliveryItemHandler(),
                                                   service);
        return (FinancialAct) copier.apply(orderItem).get(0);
    }

    /**
     * Updates orders associated with a delivery.
     *
     * @param delivery the delivery
     */
    public void updateOrders(Act delivery) {
        ActBean bean = new ActBean(delivery, service);
        Party supplier = (Party) bean.getParticipant("participation.supplier");
        List<IMObject> toUpdate = new ArrayList<IMObject>();
        for (Act deliveryItem : bean.getNodeActs("items")) {
            ActBean deliveryItemBean = new ActBean(deliveryItem, service);
            for (Act orderItem : deliveryItemBean.getNodeActs("items")) {
                ActBean orderItemBean = new ActBean(orderItem, service);
                BigDecimal quantity = deliveryItemBean.getBigDecimal(
                        "quantity");
                BigDecimal received = orderItemBean.getBigDecimal(
                        "receivedQuantity");
                BigDecimal total = received.add(quantity);
                orderItemBean.setValue("receivedQuantity", total);
                Product product = (Product) deliveryItemBean.getParticipant(
                        "participation.product");
                toUpdate.add(orderItemBean.getObject());
                if (supplier != null && product != null) {
                    EntityRelationship relationship = updateProductSupplier(
                            supplier, product, deliveryItemBean);
                    if (relationship != null) {
                        toUpdate.add(relationship);
                    }
                }
            }
        }
        if (!toUpdate.isEmpty()) {
            service.save(toUpdate);
        }
    }

    /**
     * Updates an <em>entityRelationship.productSupplier</em> from a
     * <em>act.supplierDeliveryItem</em>, if required.
     *
     * @param supplier the supplier
     * @param product
     * @param deliveryItemBean a bean wrapping the delivery item
     * @return the relationship, if it needs to be saved
     */
    private EntityRelationship updateProductSupplier(Party supplier,
                                                     Product product,
                                                     ActBean deliveryItemBean) {
        int size = deliveryItemBean.getInt("packageSize");
        String units = deliveryItemBean.getString("packageUnits");
        String reorderCode = deliveryItemBean.getString("reorderCode");
        String reorderDesc = deliveryItemBean.getString("reorderDescription");
        BigDecimal listPrice = deliveryItemBean.getBigDecimal("listPrice");
        BigDecimal nettPrice = deliveryItemBean.getBigDecimal("nettPrice");
        ProductSupplier ps = getProductSupplier(supplier, product,
                                                size, units);
        boolean save = true;
        if (ps == null) {
            // no product-supplier relationship, so create a new one
            ps = createProductSupplier(product, supplier);
            ps.setPackageSize(size);
            ps.setPackageUnits(units);
            ps.setReorderCode(reorderCode);
            ps.setReorderDescription(reorderDesc);
            ps.setListPrice(listPrice);
            ps.setNettPrice(nettPrice);
            ps.setPreferred(true);
        } else if (size != ps.getPackageSize()
                || !ObjectUtils.equals(units, ps.getPackageUnits())
                || !equals(listPrice, ps.getListPrice())
                || !equals(nettPrice, ps.getNettPrice())
                || !ObjectUtils.equals(ps.getReorderCode(), reorderCode)
                || !ObjectUtils.equals(ps.getReorderDescription(),
                                       reorderDesc)) {
            // properties are different to an existing relationship
            ps.setPackageSize(size);
            ps.setPackageUnits(units);
            ps.setReorderCode(reorderCode);
            ps.setReorderDescription(reorderDesc);
            ps.setListPrice(listPrice);
            ps.setNettPrice(nettPrice);
        } else {
            save = false;
        }
        return (save) ? ps.getRelationship() : null;
    }

    /**
     * Helper to determine if two decimals are equal.
     *
     * @param lhs the left-hand side. May be <tt>null</tt>
     * @param rhs right left-hand side. May be <tt>null</tt>
     * @return <tt>true</t> if they are equal, otherwise <tt>false</tt>
     */
    private boolean equals(BigDecimal lhs, BigDecimal rhs) {
        if (lhs != null && rhs != null) {
            return lhs.compareTo(rhs) == 0;
        }
        return ObjectUtils.equals(lhs, rhs);
    }


    /**
     * Helper to create an <em>act.supplierDeliveryItem</em> from an
     * <em>act.supplierOrderItem</em>
     */
    private static class DeliveryItemHandler
            extends AbstractIMObjectCopyHandler {

        /**
         * Determines how {@link IMObjectCopier} should treat an object.
         *
         * @param object  the source object
         * @param service the archetype service
         * @return <tt>object</tt> if the object shouldn't be copied,
         *         <tt>null</tt> if it should be replaced with
         *         <tt>null</tt>, or a new instance if the object should be
         *         copied
         */
        public IMObject getObject(IMObject object, IArchetypeService service) {
            IMObject result;
            if (object instanceof Act || object instanceof Participation) {
                String shortName = object.getArchetypeId().getShortName();
                if ("act.supplierOrderItem".equals(shortName)) {
                    shortName = "act.supplierDeliveryItem";
                }
                result = service.create(shortName);
                if (result == null) {
                    throw new ArchetypeServiceException(
                            ArchetypeServiceException.ErrorCode.FailedToCreateArchetype,
                            shortName);
                }
            } else if (object instanceof ActRelationship) {
                result = null;
            } else {
                result = object;
            }
            return result;
        }
    }


}
