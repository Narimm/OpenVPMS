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

package org.openvpms.component.business.service.cache;


import org.ehcache.Cache;
import org.ehcache.core.spi.service.StatisticsService;
import org.ehcache.spi.loaderwriter.CacheLoaderWriter;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;

/**
 * Implementation of the {@link EhcacheManager} interface that makes cache configurations persistent.
 *
 * @author Tim Anderson
 */
public class ConfigurableEhcacheManager extends AbstractEhcacheManager {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The archetype.
     */
    private final String archetype;

    /**
     * Constructs a {@link ConfigurableEhcacheManager}.
     *
     * @param service   the archetype service
     * @param archetype the singleton archetype, used to make cache configurations persistent
     */
    public ConfigurableEhcacheManager(IArchetypeService service, String archetype) {
        this(null, service, archetype);
    }

    /**
     * Constructs a {@link ConfigurableEhcacheManager}.
     *
     * @param statistics the statistics service, or {@code null} if statistics aren't being collected
     * @param service    the archetype service
     * @param archetype  the singleton archetype, used to make cache configurations persistent
     */
    public ConfigurableEhcacheManager(StatisticsService statistics, IArchetypeService service, String archetype) {
        super(statistics);
        this.service = service;
        this.archetype = archetype;
    }

    /**
     * Creates an eternal cache.
     *
     * @param name         the cache name. Must be unique
     * @param keyType      the key type
     * @param valueType    the value type
     * @param loaderWriter the loader writer
     * @return a new cache
     */
    @Override
    public <K, V> Cache<K, V> create(String name, Class<K> keyType, Class<V> valueType, CacheLoaderWriter<K, V> loaderWriter) {
        return super.create(name, keyType, valueType, loaderWriter);
    }

    /**
     * Sets the maximum number of elements that a cache may hold in memory.
     * <br/>
     * This will be made persistent for use when the cache is next constructed.
     * <br/>
     * This requires the configuration to exist, and have a Long node named {@code <cache name>MaxElements}.
     *
     * @param cache       the cache
     * @param name        the cache name
     * @param maxElements the maximum number of elements
     */
    @Override
    public <K, V> void setMaxElements(Cache<K, V> cache, String name, long maxElements) {
        super.setMaxElements(cache, name, maxElements);
        IMObjectBean config = getConfig();
        String node = getMaxElementsNode(name);
        if (config != null && config.hasNode(node)) {
            config.setValue(node, maxElements);
            config.save();
        }
    }

    /**
     * Returns the maximum number of elements for the named cache.
     *
     * @param name the cache name
     * @return the maximum elements, or {@code <=0} to use the default
     */
    protected long getMaxElements(String name) {
        long result = -1;
        IMObjectBean config = getConfig();
        String node = getMaxElementsNode(name);
        if (config != null && config.hasNode(node)) {
            result = config.getLong(node);
            if (result == 0) {
                // use the default value, if any
                Object value = config.getDefaultValue(node);
                if (value instanceof Number) {
                    result = ((Number) value).longValue();
                }
            }
        }
        return result;
    }

    /**
     * Returns the 'max elements' node name for the cache.
     *
     * @param name the cache
     * @return the 'max elements' node name
     */
    private String getMaxElementsNode(String name) {
        return name + "MaxElements";
    }

    /**
     * Returns the configuration. This must be an existing singleton object.
     *
     * @return the configuration or {@code null} if none is defined
     */
    private IMObjectBean getConfig() {
        ArchetypeQuery query = new ArchetypeQuery(archetype, true, true);
        query.add(Constraints.sort("id"));
        query.setMaxResults(1);
        IMObjectQueryIterator<Party> iterator = new IMObjectQueryIterator<>(service, query);
        return iterator.hasNext() ? service.getBean(iterator.next()) : null;
    }

}
