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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.smartflow.client;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.iterators.FilterIterator;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductQueryFactory;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.i18n.Message;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectBeanQueryIterator;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.smartflow.i18n.FlowSheetMessages;
import org.openvpms.smartflow.model.InventoryItem;
import org.openvpms.smartflow.model.InventoryItems;
import org.openvpms.smartflow.service.Inventory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Smart Flow Sheet inventory service.
 *
 * @author Tim Anderson
 */
public class InventoryService extends FlowSheetService {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookups.
     */
    private final ILookupService lookups;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(InventoryService.class);

    /**
     * Constructs a {@link InventoryService}.
     *
     * @param url          the Smart Flow Sheet URL
     * @param emrApiKey    the EMR API key
     * @param clinicApiKey the clinic API key
     * @param timeZone     the timezone. This determines how dates are serialized
     * @param service      the archetype service
     * @param lookups      the lookup service
     */
    public InventoryService(String url, String emrApiKey, String clinicApiKey, TimeZone timeZone,
                            IArchetypeService service, ILookupService lookups) {
        super(url, emrApiKey, clinicApiKey, timeZone, LogFactory.getLog(InventoryService.class));
        this.service = service;
        this.lookups = lookups;
    }

    /**
     * Returns the inventory.
     *
     * @return the inventory
     */
    public List<InventoryItem> getInventory() {
        Call<List<InventoryItem>, Inventory> call = new Call<List<InventoryItem>, Inventory>() {
            @Override
            public List<InventoryItem> call(Inventory service) throws Exception {
                List<InventoryItem> result = service.getInventory();
                if (result == null) {
                    result = Collections.emptyList();
                }
                return result;
            }

            @Override
            public Message getMessage(Exception exception) {
                return FlowSheetMessages.failedToGetInventory();
            }
        };
        return call(Inventory.class, call);
    }

    /**
     * Updates inventory items.
     *
     * @param items   the items to update
     * @param eventId a unique identifier for this update. This will be returned in inventory update events
     */
    public void update(final List<InventoryItem> items, final UUID eventId) {
        Call<Void, Inventory> call = new Call<Void, Inventory>() {
            @Override
            public Void call(Inventory service) throws Exception {
                InventoryItems inventory = new InventoryItems();
                inventory.setInventoryitems(items);
                inventory.setId(eventId.toString());
                service.update(inventory);
                return null;
            }

            @Override
            public Message getMessage(Exception exception) {
                return FlowSheetMessages.failedToUpdateInventory();
            }
        };
        call(Inventory.class, call);
    }

    /**
     * Removes an inventory item.
     *
     * @param item the item to remove
     */
    public void remove(final InventoryItem item) {
        Call<Void, Inventory> call = new Call<Void, Inventory>() {
            @Override
            public Void call(Inventory service) throws Exception {
                service.remove(item.getId());
                return null;
            }

            @Override
            public Message getMessage(Exception exception) {
                return FlowSheetMessages.failedToRemoveInventoryItem(item.getId(), item.getName());
            }
        };
        call(Inventory.class, call);
    }

    /**
     * Synchronises products with inventory items.
     * <p>
     * This updates existing inventory items and removes those no longer in use.
     *
     * @param useLocationProducts if {@code true}, products should be restricted to those available at the location or
     *                            stock location
     * @param location            the location used to exclude service products. May be {@code null}.
     *                            Only relevant when {@code useLocationProducts == true}
     * @param stockLocation       if {@code useLocationProducts == false}, products must either have the stock location,
     *                            or no stock location. If {@code useLocationProducts == true}, products must have the
     *                            stock location
     * @return the result of the synchronisation
     */
    public SyncState synchronise(boolean useLocationProducts, Party location, Party stockLocation) {
        int added = 0;
        int updated = 0;
        int removed = 0;
        Map<String, InventoryItem> inventoryItems = getInventoryItems();
        List<InventoryItem> add = new ArrayList<>();
        Iterator<Product> products = getProducts(useLocationProducts, location, stockLocation);
        Map<String, String> units = getUnits();
        while (products.hasNext()) {
            Product product = products.next();
            String id = Long.toString(product.getId());
            InventoryItem currentItem = inventoryItems.remove(id);
            InventoryItem updatedItem = synchronise(product, currentItem, id, units);
            if (updatedItem != null) {
                add.add(updatedItem);
                if (currentItem == null) {
                    added++;
                } else {
                    updated++;
                }
            }
        }
        if (!add.isEmpty()) {
            log.info("synchronise: adding " + added + " new products, updating " + updated + " existing products");
            update(add, UUID.randomUUID());
        } else {
            log.info("synchronise: there are no products to add/update");
        }

        if (!inventoryItems.isEmpty()) {
            log.info("synchronise: removing " + inventoryItems.size() + " products");
            for (InventoryItem item : inventoryItems.values()) {
                remove(item);
                removed++;
            }
        } else {
            log.info("synchronise: there are no products to remove");
        }
        return new SyncState(added, updated, removed);
    }

    /**
     * Returns all inventory items, keyed on their identifier.
     *
     * @return the inventory items
     */
    private Map<String, InventoryItem> getInventoryItems() {
        Map<String, InventoryItem> result = new HashMap<>();
        List<InventoryItem> items = getInventory();
        for (InventoryItem item : items) {
            result.put(item.getId(), item);
        }
        return result;
    }

    /**
     * Returns all active products that can be synchronised.
     *
     * @param useLocationProducts if {@code true}, only return products available at the location/stock location
     * @param location            the practice location
     * @param stockLocation       the stock location
     * @return the active products
     */
    private Iterator<Product> getProducts(boolean useLocationProducts, Party location, Party stockLocation) {
        final Set<IMObjectReference> productTypes = getSynchronisableProductTypes();
        String[] archetypes = {ProductArchetypes.MEDICATION, ProductArchetypes.SERVICE, ProductArchetypes.MERCHANDISE};
        ArchetypeQuery query = ProductQueryFactory.create(archetypes, null, null, useLocationProducts, location,
                                                          stockLocation);
        query.add(Constraints.sort("id"));
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);
        IMObjectQueryIterator<Product> iterator = new IMObjectQueryIterator<>(service, query);

        // exclude all templateOnly products, and product types that don't get synchronised
        Predicate<Product> predicate = new Predicate<Product>() {
            @Override
            public boolean evaluate(Product object) {
                boolean result = false;
                IMObjectBean bean = new IMObjectBean(object, service);
                if (!bean.getBoolean("templateOnly")) {
                    IMObjectReference type = bean.getNodeTargetObjectRef("type");
                    result = type == null || productTypes.contains(type);
                }
                return result;
            }
        };
        return new FilterIterator<>(iterator, predicate);
    }

    /**
     * Returns active product types that have their synchroniseWithSFS flag set.
     *
     * @return the product type references
     */
    private Set<IMObjectReference> getSynchronisableProductTypes() {
        Set<IMObjectReference> result = new HashSet<>();
        ArchetypeQuery query = new ArchetypeQuery(ProductArchetypes.PRODUCT_TYPE, true);
        IMObjectBeanQueryIterator iterator = new IMObjectBeanQueryIterator(service, query);
        while (iterator.hasNext()) {
            IMObjectBean bean = iterator.next();
            if (bean.getBoolean("synchroniseWithSFS")) {
                result.add(bean.getObject().getObjectReference());
            }
        }
        return result;
    }

    /**
     * Synchronise an inventory item with its product.
     *
     * @param product the product
     * @param item    the item, or {@code null} if it doesn't exist
     * @param id      the item identifier
     * @param units   the unit of measure cache
     * @return the synchronised item, or {@code null} if no synchronisation is required
     */
    private InventoryItem synchronise(Product product, InventoryItem item, String id, Map<String, String> units) {
        InventoryItem result = null;

        String name = product.getName();
        BigDecimal concentration = null;
        String concentrationUnits = null;
        String dispensingUnits = null;
        if (TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
            IMObjectBean bean = new IMObjectBean(product, service);
            concentration = bean.getBigDecimal("concentration");
            concentrationUnits = getUnit(bean.getString("concentrationUnits"), units);
            dispensingUnits = getUnit(bean.getString("dispensingUnits"), units);

            if (concentration == null || concentrationUnits == null || dispensingUnits == null) {
                // need all three values when specifying SFS concentration
                concentration = null;
                concentrationUnits = null;
                dispensingUnits = null;
            }
        }
        if (item == null || !ObjectUtils.equals(name, item.getName())
            || !MathRules.equals(concentration, item.getConcentration())
            || !ObjectUtils.equals(concentrationUnits, item.getConcentrationUnits())
            || !ObjectUtils.equals(dispensingUnits, item.getConcentrationVolume())) {
            result = createItem(id, name, concentration, concentrationUnits, dispensingUnits);
        }
        return result;
    }

    /**
     * Creates a new inventory item.
     *
     * @param id                 the item identifier
     * @param name               the item name
     * @param concentration      the concentration. May be {@code null}
     * @param concentrationUnits the concentration units. May be {@code null}
     * @param dispensingUnits    the dispensing units. May be {@code null}
     * @return a new inventory item
     */
    private InventoryItem createItem(String id, String name, BigDecimal concentration, String concentrationUnits,
                                     String dispensingUnits) {
        InventoryItem item = new InventoryItem();
        item.setId(id);
        item.setName(name);
        item.setConcentration(concentration);
        item.setConcentrationUnits(concentrationUnits);
        item.setConcentrationVolume(dispensingUnits);
        return item;
    }

    /**
     * Returns the unit of measure name given its code.
     *
     * @param code  the code. May be {@code null}
     * @param units the unit names, keyed on code
     * @return the name, or {@code code} if not found
     */
    private String getUnit(String code, Map<String, String> units) {
        String name = units.get(code);
        return name != null ? name.toLowerCase() : code;
    }

    /**
     * Returns the unit of measure names.
     *
     * @return the unit of measure names, keyed on code
     */
    private Map<String, String> getUnits() {
        Map<String, String> result = new HashMap<>();
        for (Lookup lookup : lookups.getLookups("lookup.uom")) {
            result.put(lookup.getCode(), lookup.getName());
        }
        return result;
    }

}
