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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.product;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.AndPredicate;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.RefEquals;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.JoinConstraint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
     * Constructs a {@link ProductRules}.
     *
     * @param service the archetype service
     */
    public ProductRules(IArchetypeService service) {
        this.service = service;
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
     * Returns all active <em>entityRelationship.productSupplier</em>
     * relationships for a particular product and supplier.
     *
     * @param product  the product
     * @param supplier the supplier
     * @return the relationships, wrapped in {@link ProductSupplier} instances
     */
    public List<ProductSupplier> getProductSuppliers(Product product, Party supplier) {
        List<ProductSupplier> result = new ArrayList<ProductSupplier>();
        EntityBean bean = new EntityBean(product, service);
        Predicate predicate = AndPredicate.getInstance(isActiveNow(), RefEquals.getTargetEquals(supplier));
        List<EntityRelationship> relationships
                = bean.getNodeRelationships("suppliers", predicate);
        for (EntityRelationship relationship : relationships) {
            result.add(new ProductSupplier(relationship, service));
        }
        return result;
    }

    /**
     * Returns an <em>entityRelationship.productSupplier</em> relationship
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
            } else if (reorderMatch != null) {
                result = reorderMatch;
            } else if (sizeMatch != null) {
                result = sizeMatch;
            } else if (zeroMatch != null) {
                result = zeroMatch;
            }
        }
        return result;
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
        List<EntityRelationship> relationships = bean.getNodeRelationships("suppliers", isActiveNow());
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
    public ProductSupplier createProductSupplier(Product product, Party supplier) {
        EntityBean bean = new EntityBean(product, service);
        EntityRelationship rel = bean.addRelationship(
                ProductArchetypes.PRODUCT_SUPPLIER_RELATIONSHIP, supplier);
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

        List<Entity> batches = new ArrayList<Entity>();
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

}
