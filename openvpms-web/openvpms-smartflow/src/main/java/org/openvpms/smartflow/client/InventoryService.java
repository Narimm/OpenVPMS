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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.smartflow.client;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.iterators.FilterIterator;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.SequencedRelationship;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.SequenceComparator;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.i18n.Message;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.smartflow.i18n.FlowSheetMessages;
import org.openvpms.smartflow.model.InventoryItem;
import org.openvpms.smartflow.model.InventoryItems;
import org.openvpms.smartflow.service.Inventory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
     * Constructs a {@link InventoryService}.
     *
     * @param url          the Smart Flow Sheet URL
     * @param emrApiKey    the EMR API key
     * @param clinicApiKey the clinic API key
     * @param timeZone     the timezone. This determines how dates are serialized
     */
    public InventoryService(String url, String emrApiKey, String clinicApiKey, TimeZone timeZone,
                            IArchetypeService service) {
        super(url, emrApiKey, clinicApiKey, timeZone, LogFactory.getLog(InventoryService.class));
        this.service = service;
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
            public Message failed(Exception exception) {
                return FlowSheetMessages.failedToGetInventory();
            }
        };
        return call(Inventory.class, call);
    }

    /**
     * Updates inventory items.
     *
     * @param items the items to update
     * @param uuid  a unique identifier for this update
     */
    public void update(final List<InventoryItem> items, final UUID uuid) {
        Call<Void, Inventory> call = new Call<Void, Inventory>() {
            @Override
            public Void call(Inventory service) throws Exception {
                InventoryItems inventory = new InventoryItems();
                inventory.setInventoryitems(items);
                inventory.setId(uuid.toString());
                service.update(inventory);
                return null;
            }

            @Override
            public Message failed(Exception exception) {
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
            public Message failed(Exception exception) {
                return FlowSheetMessages.failedToRemoveInventoryItem(item.getId(), item.getName());
            }
        };
        call(Inventory.class, call);
    }

    /**
     * Synchronises products with inventory items.
     * <p/>
     * This updates existing inventory items and removes those no longer in use.
     *
     * @return the result of the synchronisation
     */
    public SyncState synchronise() {
        int added = 0;
        int updated = 0;
        int removed = 0;
        Map<String, InventoryItem> inventoryItems = getInventoryItems();
        List<InventoryItem> add = new ArrayList<>();
        Iterator<Product> products = getProducts();
        while (products.hasNext()) {
            Product product = products.next();
            String id = Long.toString(product.getId());
            InventoryItem currentItem = inventoryItems.remove(id);
            InventoryItem updatedItem = synchronise(product, currentItem, id);
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
            update(add, UUID.randomUUID());
        }
        for (InventoryItem item : inventoryItems.values()) {
            remove(item);
            removed++;
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
     * Returns all active products.
     *
     * @return the active products
     */
    private Iterator<Product> getProducts() {
        String[] shortNames = {ProductArchetypes.MEDICATION, ProductArchetypes.SERVICE, ProductArchetypes.MERCHANDISE};
        ArchetypeQuery query = new ArchetypeQuery(shortNames, true, true);
        query.add(Constraints.sort("id"));
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);
        IMObjectQueryIterator<Product> iterator = new IMObjectQueryIterator<>(service, query);

        // exclude all templateOnly products
        Predicate<Product> predicate = new Predicate<Product>() {
            @Override
            public boolean evaluate(Product object) {
                IMObjectBean bean = new IMObjectBean(object, service);
                return !bean.getBoolean("templateOnly");
            }
        };
        return new FilterIterator<>(iterator, predicate);
    }

    /**
     * Synchronise an inventory item with its product.
     *
     * @param product the product
     * @param item    the item, or {@code null} if it doesn't exist
     * @param id      the item identifier
     * @return the synchronised item, or {@code null} if no synchronisation is required
     */
    private InventoryItem synchronise(Product product, InventoryItem item, String id) {
        InventoryItem result = null;

        String name = product.getName();
        Double concentration = null;
        String dispensingUnits = null;
        String weightUnits = null;
        if (TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
            IMObjectBean bean = new IMObjectBean(product, service);
            BigDecimal value = bean.getBigDecimal("concentration");
            if (value != null) {
                concentration = value.doubleValue();
                dispensingUnits = bean.getString("dispensingUnits");

                List<SequencedRelationship> relationships = bean.getValues("doses", SequencedRelationship.class);
                if (relationships.size() > 1) {
                    Collections.sort(relationships, SequenceComparator.INSTANCE);
                }
                if (!relationships.isEmpty()) {
                    IMObjectReference reference = relationships.get(0).getTarget();
                    IMObject dose = (reference != null) ? service.get(reference) : null;
                    if (dose != null) {
                        IMObjectBean doseBean = new IMObjectBean(dose, service);
                        weightUnits = doseBean.getString("weightUnits");
                    }
                }
            }
        }
        if (item == null || !ObjectUtils.equals(name, item.getName())
            || !ObjectUtils.equals(concentration, item.getConcentration())
            || !ObjectUtils.equals(dispensingUnits, item.getConcentrationUnits())
            || !ObjectUtils.equals(weightUnits, item.getConcentrationVolume())) {
            result = createItem(id, name, concentration, dispensingUnits, weightUnits);
        }
        return result;
    }

    /**
     * Creates a new inventory item.
     *
     * @param id              the item identifier
     * @param name            the item name
     * @param concentration   the concentration. May be {@code null}
     * @param dispensingUnits the dispensing units. May be {@code null}
     * @param weightUnits     the weight units. May be {@code null}
     * @return a new inventory item
     */
    private InventoryItem createItem(String id, String name, Double concentration, String dispensingUnits,
                                     String weightUnits) {
        InventoryItem item = new InventoryItem();
        item.setId(id);
        item.setName(name);
        item.setConcentration(concentration);
        item.setConcentrationUnits(dispensingUnits);
        item.setConcentrationVolume(weightUnits);
        return item;
    }

}
