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
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
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
import org.openvpms.component.business.service.archetype.helper.IMObjectCopyHandler;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


/**
 * Supplier Order rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OrderRules {

    /**
     * Supplier order act short name.
     */
    public static final String ORDER = "act.supplierOrder";

    /**
     * Supplier order item act short name.
     */
    public static final String ORDER_ITEM = "act.supplierOrderItem";

    /**
     * Supplier order item relationship short name.
     */
    public static final String ORDER_ITEM_RELATIONSHIP
            = "actRelationship.supplierOrderItem";

    /**
     * Supplier delivery act short name.
     */
    public static final String DELIVERY = "act.supplierDelivery";

    /**
     * Supplier delivery item act short name.
     */
    public static final String DELIVERY_ITEM = "act.supplierDeliveryItem";

    /**
     * Supplier delivery item relationship short name.
     */
    public static final String DELIVERY_ITEM_RELATIONSHIP
            = "actRelationship.supplierDeliveryItem";

    /**
     * Supplier delivery-order item relationship short name.
     */
    public static final String DELIVERY_ORDER_ITEM_RELATIONSHIP
            = "actRelationship.supplierDeliveryOrderItem";

    /**
     * Supplier invoice act short name.
     */
    public static final String INVOICE = "act.supplierAccountChargesInvoice";

    /**
     * Supplier invoice item act short name.
     */
    public static final String INVOICE_ITEM = "act.supplierAccountInvoiceItem";

    /**
     * Supplier invoice item relationship short name.
     */
    public static final String INVOICE_ITEM_RELATIONSHIP
            = "actRelationship.supplierAccountInvoiceItem";

    /**
     * Supplier return act short name.
     */
    public static final String RETURN = "act.supplierReturn";

    /**
     * Supplier return item act short name.
     */
    public static final String RETURN_ITEM = "act.supplierReturnItem";

    /**
     * Supplier return item relationship short name.
     */
    public static final String RETURN_ITEM_RELATIONSHIP
            = "actRelationship.supplierReturnItem";

    /**
     * Supplier credit act short name.
     */
    public static final String CREDIT = "act.supplierAccountChargesCredit";

    /**
     * Supplier credit item act short name.
     */
    public static final String CREDIT_ITEM = "act.supplierAccountCreditItem";

    /**
     * Supplier credit item relationship short name.
     */
    public static final String CREDIT_ITEM_RELATIONSHIP
            = "actRelationship.supplierAccountCreditItem";

    /**
     * Stock location participation.
     */
    public static final String STOCK_LOCATION = "participation.stockLocation";

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
     * Copies an order.
     * <p/>
     * The copied order will have an <em>IN_PROGRESS</em> status.
     * The copy is saved.
     *
     * @param order the order to copy
     * @return the copy of the order
     * @throws ArchetypeServiceException for any archetype service error
     */
    public FinancialAct copyOrder(FinancialAct order) {
        List<IMObject> objects = copy(order, ORDER,
                                      new OrderHandler(), new Date(), true);
        return (FinancialAct) objects.get(0);
    }

    /**
     * Creates a new delivery item from an order item.
     *
     * @param orderItem the order item
     * @return a new delivery item
     * @throws ArchetypeServiceException for any archetype service error
     */
    public FinancialAct createDeliveryItem(FinancialAct orderItem) {
        List<IMObject> objects = copy(orderItem, ORDER_ITEM,
                                      new DeliveryItemHandler(),
                                      orderItem.getActivityStartTime(), false);
        return (FinancialAct) objects.get(0);
    }

    /**
     * Invoices a supplier from an <em>act.supplierDelivery</em> act.
     * <p/>
     * The invoice is saved.
     *
     * @param supplierDelivery the supplier delivery act
     * @param startTime        the start time of the invoice act
     * @return the invoice corresponding to the delivery
     * @throws ArchetypeServiceException for any archetype service error
     */
    public FinancialAct invoiceSupplier(Act supplierDelivery, Date startTime) {
        List<IMObject> objects = copy(supplierDelivery, DELIVERY,
                                      new DeliveryHandler(), startTime, true);
        return (FinancialAct) objects.get(0);
    }

    /**
     * Credits a supplier from an <em>act.supplierReturn</em> act.
     * <p/>
     * The credit is saved.
     *
     * @param supplierReturn the supplier return act
     * @param startTime      the start time of the credit act
     * @return the credit corresponding to the return
     * @throws ArchetypeServiceException for any archetype service error
     */
    public FinancialAct creditSupplier(Act supplierReturn, Date startTime) {
        List<IMObject> objects = copy(supplierReturn, RETURN,
                                      new ReturnHandler(), startTime, true);
        return (FinancialAct) objects.get(0);
    }

    /**
     * Reverses a delivery.
     *
     * @param supplierDelivery the delivery to reverse
     * @param startTime        the time to assign the reversal
     * @return a new return
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Act reverseDelivery(Act supplierDelivery, Date startTime) {
        List<IMObject> objects = copy(supplierDelivery, DELIVERY,
                                      new ReverseHandler(true), startTime,
                                      true);
        return (Act) objects.get(0);
    }

    /**
     * Reverses a return.
     *
     * @param supplierReturn the return to reverse
     * @param startTime      the time to assign the reversal
     * @return a new delivery
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Act reverseReturn(Act supplierReturn, Date startTime) {
        List<IMObject> objects = copy(supplierReturn, RETURN,
                                      new ReverseHandler(false), startTime,
                                      true);
        return (Act) objects.get(0);
    }

    /**
     * Updates orders associated with a delivery or return.
     *
     * @param act the delivery or return act
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void updateOrders(Act act) {
        ActBean bean = new ActBean(act, service);
        Party supplier = (Party) bean.getNodeParticipant("supplier");
        Party stockLocation = (Party) bean.getNodeParticipant("stockLocation");
        Set<IMObject> toUpdate = new LinkedHashSet<IMObject>();
        boolean delivery = TypeHelper.isA(act, DELIVERY);
        for (Act item : bean.getNodeActs("items")) {
            ActBean itemBean = new ActBean(item, service);
            BigDecimal receivedQuantity = itemBean.getBigDecimal("quantity");
            int receivedPackSize = itemBean.getInt("packageSize");
            if (!delivery) {
                receivedQuantity = receivedQuantity.negate();
            }
            Product product = (Product) itemBean.getNodeParticipant("product");

            for (Act orderItem : itemBean.getNodeActs("items")) {
                updateReceivedQuantity(orderItem, receivedQuantity,
                                       receivedPackSize);
                toUpdate.add(orderItem);
            }
            if (delivery && supplier != null && product != null) {
                EntityRelationship relationship = updateProductSupplier(
                        supplier, product, itemBean);
                if (relationship != null) {
                    toUpdate.add(relationship);
                }
            }

            if (product != null && stockLocation != null) {
                EntityRelationship relationship = updateStockQuantity(
                        product, stockLocation, receivedQuantity,
                        receivedPackSize);
                if (relationship != null) {
                    if (relationship.isNew()) {
                        toUpdate.add(product);
                        toUpdate.add(stockLocation);
                    } else {
                        toUpdate.add(relationship);
                    }
                }
            }
        }
        if (!toUpdate.isEmpty()) {
            service.save(toUpdate);
        }
    }

    private void updateReceivedQuantity(Act orderItem,
                                        BigDecimal quantity,
                                        int packageSize) {
        ActBean orderItemBean = new ActBean(orderItem, service);
        int orderedPackSize = orderItemBean.getInt("packageSize");
        if (packageSize != orderedPackSize && orderedPackSize != 0) {
            // need to convert the quantity to the order package quantity
            quantity = quantity.multiply(BigDecimal.valueOf(packageSize));
            quantity = quantity.divide(BigDecimal.valueOf(orderedPackSize));
        }
        BigDecimal received = orderItemBean.getBigDecimal(
                "receivedQuantity");
        BigDecimal total = received.add(quantity);
        orderItemBean.setValue("receivedQuantity", total);
    }

    /**
     * @param product
     * @param stockLocation
     * @param quantity
     * @return the stock location relationship, or <tt>null</tt> if none exists
     */
    private EntityRelationship updateStockQuantity(Product product,
                                                   Party stockLocation,
                                                   BigDecimal quantity,
                                                   int packageSize) {
        EntityRelationship relationship = null;
        EntityBean bean = new EntityBean(product, service);
        if (bean.hasNode("stockLocations")) {
            Predicate predicate = AndPredicate.getInstance(
                    IsActiveRelationship.ACTIVE_NOW,
                    RefEquals.getTargetEquals(stockLocation));
            relationship = bean.getNodeRelationship(
                    "stockLocations", predicate);
            if (relationship == null) {
                relationship = bean.addRelationship(
                        "entityRelationship.productStockLocation",
                        stockLocation);
            }
            BigDecimal units
                    = quantity.multiply(BigDecimal.valueOf(packageSize));
            IMObjectBean relBean = new IMObjectBean(relationship, service);
            BigDecimal stockQuantity = relBean.getBigDecimal("quantity");
            stockQuantity = stockQuantity.add(units);
            relBean.setValue("quantity", stockQuantity);
        }
        return relationship;
    }

    /**
     * Updates an <em>entityRelationship.productSupplier</em> from a
     * <em>act.supplierDeliveryItem</em>, if required.
     *
     * @param supplier         the supplier
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
     * Helper to copy an act.
     *
     * @param object    the object to copy
     * @param type      the expected type of the object
     * @param handler   the copy handler
     * @param startTime the start time of the copied object
     * @param save      if <tt>true</tt>, save the copied objects
     * @return the copied objects
     */
    private List<IMObject> copy(Act object, String type,
                                IMObjectCopyHandler handler, Date startTime,
                                boolean save) {
        if (!TypeHelper.isA(object, type)) {
            throw new IllegalArgumentException(
                    "Expected a " + type + " for argument 'object'"
                            + ", but got a"
                            + object.getArchetypeId().getShortName());
        }
        IMObjectCopier copier = new IMObjectCopier(handler, service);
        List<IMObject> objects = copier.apply(object);
        Act act = (Act) objects.get(0);
        act.setActivityStartTime(startTime);
        if (save) {
            service.save(objects);
        }
        return objects;
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

    private abstract static class CopyHandler
            extends AbstractIMObjectCopyHandler {

        private final String[][] typeMap;
        private final boolean reverse;

        public CopyHandler(String[][] typeMap) {
            this(typeMap, false);
        }

        public CopyHandler(String[][] typeMap, boolean reverse) {
            this.typeMap = typeMap;
            this.reverse = reverse;
        }

        /**
         * Determines how {@link IMObjectCopier} should treat an object. This
         * implementation always returns a new instance, of the same archetype as
         * <tt>object</tt>.
         *
         * @param object  the source object
         * @param service the archetype service
         * @return <tt>object</tt> if the object shouldn't be copied,
         *         <tt>null</tt> if it should be replaced with <tt>null</tt>,
         *         or a new instance if the object should be copied
         */
        @Override
        public IMObject getObject(IMObject object, IArchetypeService service) {
            IMObject result;
            if (object instanceof Act || object instanceof ActRelationship
                    || object instanceof Participation) {
                String shortName = object.getArchetypeId().getShortName();
                for (String[] map : typeMap) {
                    String from;
                    String to;
                    if (!reverse) {
                        from = map[0];
                        to = map[1];
                    } else {
                        from = map[1];
                        to = map[0];
                    }
                    if (from.equals(shortName)) {
                        shortName = to;
                        break;
                    }
                }
                if (shortName != null) {
                    result = service.create(shortName);
                    if (result == null) {
                        throw new ArchetypeServiceException(
                                ArchetypeServiceException.ErrorCode.FailedToCreateArchetype,
                                shortName);
                    }
                } else {
                    result = null;
                }
            } else {
                result = object;
            }
            return result;
        }

        /**
         * Helper to determine if a node is copyable.
         *
         * @param node   the node descriptor
         * @param source if <tt>true</tt> the node is the source; otherwise its
         *               the target
         * @return <tt>true</tt> if the node is copyable; otherwise <tt>false</tt>
         */
        @Override
        protected boolean isCopyable(NodeDescriptor node, boolean source) {
            boolean result = super.isCopyable(node, source);
            if (result) {
                String name = node.getName();
                result = !"startTime".equals(name) && !"status".equals(name)
                        && !"printed".equals(name);
            }
            return result;
        }
    }

    /**
     * Helper to copy an <em>act.supplierOrder</em>.
     */
    private static class OrderHandler extends CopyHandler {

        private static final String[][] TYPE_MAP
                = {{ORDER, ORDER},
                   {ORDER_ITEM_RELATIONSHIP, ORDER_ITEM_RELATIONSHIP},
                   {ORDER_ITEM, ORDER_ITEM}};

        public OrderHandler() {
            super(TYPE_MAP);
        }
    }

    /**
     * Helper to create an <em>act.supplierDeliveryItem</em> from an
     * <em>act.supplierOrderItem</em>
     */
    private static class DeliveryItemHandler
            extends CopyHandler {

        private static final String[][] TYPE_MAP
                = {{ORDER_ITEM, DELIVERY_ITEM}};

        public DeliveryItemHandler() {
            super(TYPE_MAP);
        }

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
                result = super.getObject(object, service);
            } else if (object instanceof ActRelationship) {
                result = null;
            } else {
                result = object;
            }
            return result;
        }
    }

    private static class DeliveryHandler extends CopyHandler {

        /**
         * Map of delivery types to their corresponding invoice types.
         */
        private static final String[][] TYPE_MAP = {
                {DELIVERY, INVOICE},
                {DELIVERY_ITEM, INVOICE_ITEM},
                {DELIVERY_ITEM_RELATIONSHIP, INVOICE_ITEM_RELATIONSHIP},
                {DELIVERY_ORDER_ITEM_RELATIONSHIP, null},
                {STOCK_LOCATION, null}};

        public DeliveryHandler() {
            super(TYPE_MAP);
        }
    }

    private static class ReturnHandler extends CopyHandler {

        /**
         * Map of return types to their corresponding credit types.
         */
        private static final String[][] TYPE_MAP = {
                {RETURN, CREDIT},
                {RETURN_ITEM, CREDIT_ITEM},
                {RETURN_ITEM_RELATIONSHIP, CREDIT_ITEM_RELATIONSHIP},
                {STOCK_LOCATION, null}};

        public ReturnHandler() {
            super(TYPE_MAP);
        }
    }

    private static class ReverseHandler extends CopyHandler {

        private static final String[][] TYPE_MAP = {
                {DELIVERY, RETURN},
                {DELIVERY_ITEM, RETURN_ITEM},
                {DELIVERY_ITEM_RELATIONSHIP, RETURN_ITEM_RELATIONSHIP}};

        public ReverseHandler(boolean delivery) {
            super(TYPE_MAP, !delivery);
        }
    }

}
