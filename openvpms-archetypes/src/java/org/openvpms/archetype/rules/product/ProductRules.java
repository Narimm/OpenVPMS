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

package org.openvpms.archetype.rules.product;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.AndPredicate;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.math.Weight;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.RefEquals;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.JoinConstraint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.openvpms.archetype.rules.math.MathRules.isZero;
import static org.openvpms.component.business.service.archetype.functor.IsActiveRelationship.isActiveNow;
import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.gte;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.lt;
import static org.openvpms.component.system.common.query.Constraints.sort;

/**
 * Product rules.
 *
 * @author Tim Anderson
 */
public class ProductRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * Constructs a {@link ProductRules}.
     *
     * @param service the archetype service
     * @param lookups the lookup service
     */
    public ProductRules(IArchetypeService service, ILookupService lookups) {
        this.service = service;
        this.lookups = lookups;
    }

    /**
     * Copies a product.
     *
     * @param product the product to copy
     * @return a copy of {@code product}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Product copy(Product product) {
        return copy(product, product.getName());
    }

    /**
     * Copies a product.
     *
     * @param product the product to copy
     * @param name    the new product name
     * @return a copy of {@code product}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Product copy(Product product, String name) {
        IMObjectCopier copier = new IMObjectCopier(new ProductCopyHandler(product));
        List<IMObject> objects = copier.apply(product);
        Product copy = (Product) objects.get(0);
        copy.setName(name);
        service.save(objects);
        return copy;
    }

    /**
     * Returns the dose of a product for the specified patient weight and species.
     *
     * @param product the product
     * @param weight  the patient weight
     * @param species the patient species code. May be {@code null}
     * @return the dose or {@code 0} if there is no dose for the patient weight and species
     */
    public BigDecimal getDose(Product product, Weight weight, String species) {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal concentration = getConcentration(product);
        if (!isZero(concentration)) {
            IMObjectBean match = null;
            WeightUnits matchUnits = null;
            IMObjectBean bean = new IMObjectBean(product, service);
            List<IMObject> doses = bean.getNodeTargetObjects("doses");
            for (IMObject dose : doses) {
                IMObjectBean doseBean = new IMObjectBean(dose, service);
                BigDecimal minWeight = doseBean.getBigDecimal("minWeight", BigDecimal.ZERO);
                BigDecimal maxWeight = doseBean.getBigDecimal("maxWeight", BigDecimal.ZERO);
                WeightUnits units = WeightUnits.fromString(doseBean.getString("weightUnits"), WeightUnits.KILOGRAMS);
                if (weight.between(minWeight, maxWeight, units)) {
                    List<Lookup> values = doseBean.getValues("species", Lookup.class);
                    if (values.isEmpty()) {
                        if (species == null || match == null) {
                            match = doseBean;
                            matchUnits = units;
                            if (species == null) {
                                break;
                            }
                        }
                    } else if (values.get(0).getCode().equals(species)) {
                        match = doseBean;
                        matchUnits = units;
                        break;
                    }
                }
            }
            if (match != null) {
                BigDecimal converted = weight.convert(matchUnits);
                BigDecimal rate = match.getBigDecimal("rate", BigDecimal.ZERO);
                BigDecimal quantity = match.getBigDecimal("quantity", BigDecimal.ONE);
                int places = match.getInt("roundTo");
                if (!isZero(concentration) && !isZero(rate) && !isZero(quantity)) {
                    // math here is (rate (per weight unit) * concentration (per dispensing unit)) * quantity
                    result = converted.multiply(rate).divide(concentration, places, RoundingMode.HALF_UP)
                            .multiply(quantity);
                }
            }
        }
        return result;
    }

    /**
     * Returns all active <em>entityLink.productSupplier</em>
     * relationships for a particular product and supplier.
     *
     * @param product  the product
     * @param supplier the supplier
     * @return the relationships, wrapped in {@link ProductSupplier} instances
     */
    public List<ProductSupplier> getProductSuppliers(Product product, Party supplier) {
        List<ProductSupplier> result = new ArrayList<>();
        EntityBean bean = new EntityBean(product, service);
        Predicate predicate = AndPredicate.getInstance(isActiveNow(), RefEquals.getTargetEquals(supplier));
        List<EntityLink> relationships = bean.getValues("suppliers", predicate, EntityLink.class);
        for (EntityLink relationship : relationships) {
            result.add(new ProductSupplier(relationship, service));
        }
        return result;
    }

    /**
     * Returns an <em>entityLink.productSupplier</em> relationship
     * for a product, supplier, reorder code, package size and units.
     * <p/>
     * If there is a match on reorder code
     * If there is a match on supplier and product, but no match on package
     * size, but there is a relationship where the size is {@code 0}, then
     * this will be returned.
     *
     * @param product      the product
     * @param supplier     the supplier
     * @param reorderCode  the reorder code. May be {@code null}
     * @param packageSize  the package size
     * @param packageUnits the package units. May be {@code null}
     * @return the relationship, wrapped in a {@link ProductSupplier}, or {@code null} if none is found
     */
    public ProductSupplier getProductSupplier(Product product, Party supplier, String reorderCode,
                                              int packageSize, String packageUnits) {
        ProductSupplier result = null;
        ProductSupplier reorderMatch = null;
        ProductSupplier packageMatch = null;
        ProductSupplier sizeMatch = null;
        ProductSupplier zeroMatch = null;
        List<ProductSupplier> list = getProductSuppliers(product, supplier);
        for (ProductSupplier ps : list) {
            boolean reorderEq = ObjectUtils.equals(reorderCode, ps.getReorderCode());
            boolean sizeEq = packageSize == ps.getPackageSize();
            boolean unitEq = ObjectUtils.equals(packageUnits, ps.getPackageUnits());
            if (reorderEq && sizeEq && unitEq) {
                result = ps;
                break;
            } else if (reorderEq && reorderCode != null) {
                reorderMatch = ps;
            } else if (sizeEq && unitEq) {
                packageMatch = ps;
            } else if (sizeEq) {
                sizeMatch = ps;
            } else if (ps.getPackageSize() == 0) {
                zeroMatch = ps;
            }
        }
        if (result == null) {
            if (reorderMatch != null) {
                result = reorderMatch;
            } else if (packageMatch != null) {
                result = packageMatch;
            } else if (sizeMatch != null) {
                result = sizeMatch;
            } else if (zeroMatch != null) {
                result = zeroMatch;
            }
        }
        return result;
    }

    /**
     * Returns all active <em>entityLink.productSupplier</em>
     * relationships for a particular product.
     *
     * @param product the product
     * @return the relationships, wrapped in {@link ProductSupplier} instances
     */
    public List<ProductSupplier> getProductSuppliers(Product product) {
        List<ProductSupplier> result = new ArrayList<>();
        EntityBean bean = new EntityBean(product, service);
        List<EntityLink> relationships = bean.getValues("suppliers", isActiveNow(), EntityLink.class);
        for (EntityLink relationship : relationships) {
            result.add(new ProductSupplier(relationship, service));
        }
        return result;
    }

    /**
     * Creates a new <em>entityLink.productSupplier</em> relationship
     * between a supplier and product.
     *
     * @param product  the product
     * @param supplier the supplier
     * @return the relationship, wrapped in a {@link ProductSupplier}
     */
    public ProductSupplier createProductSupplier(Product product, Party supplier) {
        EntityBean bean = new EntityBean(product, service);
        IMObjectRelationship rel = bean.addNodeTarget("suppliers", supplier);
        return new ProductSupplier(rel, service);
    }

    /**
     * Returns all product batches matching the criteria.
     *
     * @param product      the product
     * @param batchNumber  the batch number
     * @param expiryDate   the expiry date. May be {@code null}
     * @param manufacturer the manufacturer. May be {@code null}
     * @return the batches
     */
    public List<Entity> getBatches(Product product, String batchNumber, Date expiryDate, Party manufacturer) {
        return getBatches(product.getObjectReference(), batchNumber, expiryDate,
                          (manufacturer != null) ? manufacturer.getObjectReference() : null);
    }

    /**
     * Returns all product batches matching the criteria, ordered on batch number and entity Id.
     * <p/>
     * Note that this should not be used if the expected number of results is large; use a query instead.
     *
     * @param product      the product
     * @param batchNumber  the batch number. May be {@code null}
     * @param expiryDate   the expiry date. May be {@code null}
     * @param manufacturer the manufacturer. May be {@code null}
     * @return the batches
     */
    public List<Entity> getBatches(IMObjectReference product, String batchNumber, Date expiryDate,
                                   IMObjectReference manufacturer) {
        ArchetypeQuery query = new ArchetypeQuery(ProductArchetypes.PRODUCT_BATCH, false, false);
        if (!StringUtils.isEmpty(batchNumber)) {
            query.add(eq("name", batchNumber));
        }
        JoinConstraint join = join("product", "product");
        query.add(join.add(eq("target", product)));
        if (expiryDate != null) {
            join.add(gte("activeEndTime", DateRules.getDate(expiryDate)));   // need to ignore any time components
            join.add(lt("activeEndTime", DateRules.getNextDate(expiryDate)));
        }
        if (manufacturer != null) {
            query.add(join("manufacturer").add(eq("target", manufacturer)));
        }
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        IPage<IMObject> page = service.get(query);
        query.add(sort("name"));
        query.add(sort("id"));

        List<Entity> batches = new ArrayList<>();
        for (IMObject object : page.getResults()) {
            batches.add((Entity) object);
        }
        return batches;
    }


    /**
     * Creates a new batch.
     *
     * @param product      the product
     * @param batchNumber  the batch number
     * @param expiryDate   the expiry date. May be {@code null}
     * @param manufacturer the manufacturer. May be {@code null}
     * @return a new batch
     */
    public Entity createBatch(Product product, String batchNumber, Date expiryDate, Party manufacturer) {
        return createBatch(product.getObjectReference(), batchNumber, expiryDate,
                           (manufacturer != null) ? manufacturer.getObjectReference() : null);
    }

    /**
     * Creates a new batch.
     *
     * @param product      the product
     * @param batchNumber  the batch number
     * @param expiryDate   the expiry date. May be {@code null}
     * @param manufacturer the manufacturer. May be {@code null}
     * @return a new batch
     */
    public Entity createBatch(IMObjectReference product, String batchNumber, Date expiryDate,
                              IMObjectReference manufacturer) {
        Entity result = (Entity) service.create(ProductArchetypes.PRODUCT_BATCH);
        EntityBean bean = new EntityBean(result, service);
        bean.setValue("name", batchNumber);
        IMObjectRelationship relationship = bean.addNodeTarget("product", product);
        IMObjectBean relBean = new IMObjectBean(relationship, service);
        relBean.setValue("activeEndTime", expiryDate);
        if (manufacturer != null) {
            bean.addNodeTarget("manufacturer", manufacturer);
        }
        return result;
    }

    /**
     * Returns the expiry date of a batch.
     *
     * @param batch the batch
     * @return the batch expiry date, or {@code null} if none is set
     */
    public Date getBatchExpiry(Entity batch) {
        Date result = null;
        EntityBean bean = new EntityBean(batch, service);
        List<EntityLink> product = bean.getValues("product", EntityLink.class);
        if (!product.isEmpty()) {
            result = product.get(0).getActiveEndTime();
        }
        return result;
    }

    /**
     * Determines if a product can be used at the specified location.
     * <p/>
     * This is only applicable to service and template products. For other products, it always returns {@code true}.
     * For medication and merchandise products, the stock location must be checked.
     *
     * @param product  the product
     * @param location the practice location
     * @return {@code true} if the product can be used at the location
     */
    public boolean canUseProductAtLocation(Product product, Party location) {
        IMObjectBean bean = new IMObjectBean(product, service);
        return !bean.getNodeTargetObjectRefs("locations").contains(location.getObjectReference());
    }

    /**
     * Determines if a drug is classed as restricted.
     *
     * @param product the product
     * @return {@code true} if the product has a drug schedule that is restricted, otherwise {@code false}
     */
    public boolean isRestricted(Product product) {
        boolean result = false;
        if (TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
            Lookup schedule = lookups.getLookup(product, "drugSchedule");
            if (schedule != null) {
                IMObjectBean bean = new IMObjectBean(schedule, service);
                result = bean.getBoolean("restricted", false);
            }
        }
        return result;
    }

    /**
     * Returns the product concentration.
     *
     * @param product the product
     * @return the product concentration
     */
    private BigDecimal getConcentration(Product product) {
        if (TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
            IMObjectBean bean = new IMObjectBean(product, service);
            return bean.getBigDecimal("concentration", BigDecimal.ZERO);
        }
        return BigDecimal.ZERO;
    }

}
